package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.ETagMismatchException;
import com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException;
import com.ryuqq.fileflow.domain.session.exception.SessionExpiredException;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 단일 업로드 세션 Aggregate Root.
 *
 * <p>단일 Presigned URL을 통한 파일 업로드를 관리합니다.
 *
 * <p><strong>생명주기</strong>:
 *
 * <ul>
 *   <li>PREPARING: 세션 준비 중 (Presigned URL 발급 전)
 *   <li>ACTIVE: 업로드 가능 (Presigned URL 발급 완료)
 *   <li>COMPLETED: 업로드 완료
 *   <li>EXPIRED: 세션 만료
 *   <li>FAILED: 업로드 실패
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>파일 크기는 5GB 이하여야 한다.
 *   <li>세션은 15분 후 자동 만료된다.
 *   <li>만료된 세션은 사용할 수 없다.
 *   <li>상태 전환은 SessionStatus 규칙을 따른다.
 * </ul>
 */
public class SingleUploadSession implements UploadSession {

    // ==================== 필드 ====================

    private final UploadSessionId id;
    private final IdempotencyKey idempotencyKey;
    private final UserContext userContext;
    private final FileName fileName;
    private final FileSize fileSize;
    private final ContentType contentType;
    private final S3Bucket bucket;
    private final S3Key s3Key;
    private final ExpirationTime expirationTime;
    private final Instant createdAt;

    private SessionStatus status;
    private PresignedUrl presignedUrl;
    private ETag etag;
    private Instant completedAt;
    private Instant updatedAt;
    private Long version;

    // 도메인 이벤트
    private final List<FileUploadCompletedEvent> domainEvents = new ArrayList<>();

    // ==================== 생성 메서드 ====================

    /**
     * 신규 세션 생성 (Presigned URL 발급용).
     *
     * @param idempotencyKey 멱등성 키 (클라이언트 제공)
     * @param userContext 사용자 컨텍스트 (토큰 기반)
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param expirationTime 만료 시각 (15분 후)
     * @param clock 시간 소스
     * @return 신규 SingleUploadSession (status: PREPARING, presignedUrl: null)
     */
    public static SingleUploadSession forNew(
            IdempotencyKey idempotencyKey,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ExpirationTime expirationTime,
            Clock clock) {
        Instant now = clock.instant();
        return new SingleUploadSession(
                UploadSessionId.forNew(),
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                now,
                SessionStatus.PREPARING,
                null, // presignedUrl: PREPARING 상태에서는 null
                null, // etag
                null, // completedAt
                now, // updatedAt: 생성 시각과 동일
                null); // version: 신규 생성 시 null
    }

    /**
     * ID 기반 생성 (비즈니스 로직용).
     *
     * @param id 세션 ID (null 불가)
     * @param idempotencyKey 멱등성 키
     * @param userContext 사용자 컨텍스트
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param expirationTime 만료 시각
     * @param createdAt 생성 시각
     * @param status 세션 상태
     * @param presignedUrl Presigned URL (선택적)
     * @param etag ETag (선택적)
     * @param completedAt 완료 시각 (선택적)
     * @param updatedAt 수정 시각 (선택적)
     * @param version 낙관락 버전 (선택적)
     * @return SingleUploadSession
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static SingleUploadSession of(
            UploadSessionId id,
            IdempotencyKey idempotencyKey,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ExpirationTime expirationTime,
            Instant createdAt,
            SessionStatus status,
            PresignedUrl presignedUrl,
            ETag etag,
            Instant completedAt,
            Instant updatedAt,
            Long version) {
        if (id == null) {
            throw new IllegalArgumentException("ID는 null일 수 없습니다.");
        }
        return new SingleUploadSession(
                id,
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                createdAt,
                status,
                presignedUrl,
                etag,
                completedAt,
                updatedAt,
                version);
    }

    /**
     * 영속성 복원 (Mapper 전용).
     *
     * @param id 세션 ID (null 불가)
     * @param idempotencyKey 멱등성 키
     * @param userContext 사용자 컨텍스트
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param expirationTime 만료 시각
     * @param createdAt 생성 시각
     * @param status 세션 상태
     * @param presignedUrl Presigned URL (선택적)
     * @param etag ETag (선택적)
     * @param completedAt 완료 시각 (선택적)
     * @param updatedAt 수정 시각 (선택적)
     * @param version 낙관락 버전 (선택적)
     * @return SingleUploadSession
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static SingleUploadSession reconstitute(
            UploadSessionId id,
            IdempotencyKey idempotencyKey,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ExpirationTime expirationTime,
            Instant createdAt,
            SessionStatus status,
            PresignedUrl presignedUrl,
            ETag etag,
            Instant completedAt,
            Instant updatedAt,
            Long version) {
        return of(
                id,
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                createdAt,
                status,
                presignedUrl,
                etag,
                completedAt,
                updatedAt,
                version);
    }

    /** 생성자 (private). */
    private SingleUploadSession(
            UploadSessionId id,
            IdempotencyKey idempotencyKey,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ExpirationTime expirationTime,
            Instant createdAt,
            SessionStatus status,
            PresignedUrl presignedUrl,
            ETag etag,
            Instant completedAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.userContext = userContext;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.expirationTime = expirationTime;
        this.createdAt = createdAt;
        this.status = status;
        this.presignedUrl = presignedUrl;
        this.etag = etag;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
        this.version = version;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 세션 활성화 (Presigned URL 발급 후 호출).
     *
     * @param presignedUrl S3 Presigned PUT URL
     * @param clock 시간 소스
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우 (PREPARING 아님)
     * @throws IllegalArgumentException Presigned URL이 null인 경우
     */
    public void activate(PresignedUrl presignedUrl, Clock clock) {
        validateStatusTransition(SessionStatus.ACTIVE);

        if (presignedUrl == null) {
            throw new IllegalArgumentException("Presigned URL은 null일 수 없습니다.");
        }

        this.presignedUrl = presignedUrl;
        this.status = SessionStatus.ACTIVE;
        this.updatedAt = clock.instant();
    }

    /**
     * 업로드 완료 처리.
     *
     * <p><strong>중요</strong>: 만료 검증을 하지 않습니다.
     *
     * <p>이유: 클라이언트가 S3에 업로드 성공 후 네트워크 오류로 Complete API 호출 실패 시, 세션이 만료되더라도 완료 처리할 수 있어야 합니다. S3에는
     * 파일이 존재하므로 세션만 COMPLETED로 변경하면 됩니다.
     *
     * <p><strong>ETag 검증</strong>: 클라이언트가 제공한 ETag와 S3 실제 ETag를 비교하여 데이터 무결성을 보장합니다.
     *
     * @param clientETag 클라이언트가 제공한 ETag
     * @param s3ETag S3에 실제 저장된 파일의 ETag
     * @param clock 시간 소스
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     * @throws ETagMismatchException ETag가 일치하지 않는 경우
     */
    public void complete(ETag clientETag, ETag s3ETag, Clock clock) {
        // 만료 검증 제거 - S3 업로드 성공 후 세션 만료되어도 완료 가능해야 함
        validateStatusTransition(SessionStatus.COMPLETED);

        if (clientETag == null) {
            throw new IllegalArgumentException("클라이언트 ETag는 null일 수 없습니다.");
        }

        if (s3ETag == null) {
            throw new IllegalArgumentException("S3 ETag는 null일 수 없습니다.");
        }

        // ETag 검증 (도메인 규칙: 클라이언트 ETag와 S3 ETag가 일치해야 함)
        if (!clientETag.equals(s3ETag)) {
            throw new ETagMismatchException(s3ETag, clientETag);
        }

        this.status = SessionStatus.COMPLETED;
        this.etag = clientETag;
        this.completedAt = clock.instant();
        this.updatedAt = this.completedAt;

        // 도메인 이벤트 생성
        domainEvents.add(
                FileUploadCompletedEvent.of(
                        this.id,
                        this.fileName,
                        this.fileSize,
                        this.contentType,
                        this.bucket,
                        this.s3Key,
                        this.etag,
                        this.userContext.userId(),
                        this.userContext.organization().id(),
                        this.userContext.tenant().id(),
                        this.completedAt));
    }

    /**
     * 저장된 도메인 이벤트를 반환하고 목록을 비웁니다.
     *
     * @return 도메인 이벤트 목록
     */
    public List<FileUploadCompletedEvent> pollDomainEvents() {
        List<FileUploadCompletedEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    /**
     * 세션 만료 처리.
     *
     * @param clock 시간 소스
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    @Override
    public void expire(Clock clock) {
        validateStatusTransition(SessionStatus.EXPIRED);
        this.status = SessionStatus.EXPIRED;
        this.updatedAt = clock.instant();
    }

    /**
     * 업로드 실패 처리.
     *
     * @param clock 시간 소스
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    public void fail(Clock clock) {
        validateStatusTransition(SessionStatus.FAILED);
        this.status = SessionStatus.FAILED;
        this.updatedAt = clock.instant();
    }

    // ==================== private 검증 메서드 ====================

    /**
     * 세션이 만료되지 않았는지 검증한다.
     *
     * @param clock 시간 소스
     * @throws SessionExpiredException 세션이 만료된 경우
     */
    private void validateNotExpired(Clock clock) {
        if (expirationTime.isExpired(clock)) {
            throw new SessionExpiredException(expirationTime.value());
        }
    }

    /**
     * 상태 전환이 가능한지 검증한다.
     *
     * @param nextStatus 다음 상태
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    private void validateStatusTransition(SessionStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidSessionStatusException(this.status, nextStatus);
        }
    }

    // ==================== Getter ====================

    public UploadSessionId getId() {
        return id;
    }

    public String getIdValue() {
        return id.value().toString();
    }

    public IdempotencyKey getIdempotencyKey() {
        return idempotencyKey;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    /**
     * Law of Demeter: 사용자 식별자 반환 (로깅/추적용).
     *
     * @return Admin/Seller: email, Customer: "user-{userId}"
     */
    public String getUserIdentifier() {
        return userContext.getUserIdentifier();
    }

    /**
     * Law of Demeter: 조직 ID 반환.
     *
     * @return 조직 ID (nullable - Admin/Customer는 null)
     */
    public OrganizationId getOrganizationId() {
        return userContext.getOrganizationId();
    }

    public FileName getFileName() {
        return fileName;
    }

    public String getFileNameValue() {
        return fileName.name();
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public long getFileSizeValue() {
        return fileSize.size();
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getContentTypeValue() {
        return contentType.type();
    }

    public S3Bucket getBucket() {
        return bucket;
    }

    public String getBucketValue() {
        return bucket.bucketName();
    }

    public S3Key getS3Key() {
        return s3Key;
    }

    public String getS3KeyValue() {
        return s3Key.key();
    }

    public ExpirationTime getExpirationTime() {
        return expirationTime;
    }

    public Instant getExpiresAt() {
        return expirationTime.value();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    /**
     * Presigned URL을 반환합니다.
     *
     * <p>단순 조회용 Getter입니다. 만료 검증은 비즈니스 로직에서 수행하세요.
     *
     * @return Presigned URL
     */
    public PresignedUrl getPresignedUrl() {
        return presignedUrl;
    }

    /**
     * Presigned URL 문자열 값을 반환합니다 (Law of Demeter).
     *
     * <p>단순 조회용 Getter입니다. 만료 검증은 비즈니스 로직에서 수행하세요.
     *
     * @return Presigned URL 문자열
     */
    public String getPresignedUrlValue() {
        return presignedUrl != null ? presignedUrl.value() : null;
    }

    public ETag getEtag() {
        return etag;
    }

    public String getETagValue() {
        return etag != null ? etag.value() : null;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 세션이 완료되었는지 확인한다.
     *
     * @return 완료되었으면 true
     */
    public boolean isCompleted() {
        return this.status == SessionStatus.COMPLETED;
    }

    /**
     * 세션이 활성 상태인지 확인한다.
     *
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    /**
     * 세션이 만료되었는지 확인한다.
     *
     * @param clock 시간 소스
     * @return 만료되었으면 true
     */
    public boolean isExpired(Clock clock) {
        return expirationTime.isExpired(clock);
    }

    /**
     * 낙관락 버전을 반환한다.
     *
     * @return 버전 (신규 생성 시 null)
     */
    public Long getVersion() {
        return version;
    }
}
