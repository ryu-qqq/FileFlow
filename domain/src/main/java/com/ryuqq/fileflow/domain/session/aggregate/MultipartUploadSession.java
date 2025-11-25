package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.exception.IncompletePartsException;
import com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException;
import com.ryuqq.fileflow.domain.session.exception.SessionExpiredException;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multipart 업로드 세션 Aggregate Root.
 *
 * <p>S3 Multipart Upload를 통한 대용량 파일 업로드를 관리합니다. CompletedPart는 별도 Aggregate로 분리되어 독립적으로 조회/업데이트됩니다.
 *
 * <p><strong>생명주기</strong>:
 *
 * <ul>
 *   <li>PREPARING: 세션 준비 중 (S3 Multipart Upload ID 발급 전)
 *   <li>ACTIVE: Part별 업로드 진행 중 (Presigned URL 발급 완료)
 *   <li>COMPLETED: 모든 Part 업로드 및 병합 완료
 *   <li>EXPIRED: 세션 만료 (24시간)
 *   <li>FAILED: 업로드 실패
 * </ul>
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Part 크기는 5MB ~ 5GB 사이여야 한다.
 *   <li>Part 개수는 1 ~ 10,000개 사이여야 한다.
 *   <li>세션은 24시간 후 자동 만료된다.
 *   <li>모든 Part가 완료되어야 업로드를 완료할 수 있다.
 * </ul>
 */
public class MultipartUploadSession implements UploadSession {

    // ==================== 필드 ====================

    private final UploadSessionId id;
    private final UserContext userContext;
    private final FileName fileName;
    private final FileSize fileSize;
    private final ContentType contentType;
    private final S3Bucket bucket;
    private final S3Key s3Key;
    private final S3UploadId s3UploadId;
    private final TotalParts totalParts;
    private final PartSize partSize;
    private final ExpirationTime expirationTime;
    private final LocalDateTime createdAt;
    private final Clock clock;

    private SessionStatus status;
    private LocalDateTime completedAt;
    private ETag mergedETag;
    private Long version;

    // 도메인 이벤트
    private final List<FileUploadCompletedEvent> domainEvents = new ArrayList<>();

    // ==================== 생성 메서드 ====================

    /**
     * 신규 Multipart 세션 생성 (S3 Multipart Upload ID 발급 후).
     *
     * @param userContext 사용자 컨텍스트 (토큰 기반)
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param s3UploadId S3 Multipart Upload ID
     * @param totalParts 총 Part 개수
     * @param partSize 각 Part 크기
     * @param expirationTime 만료 시각 (24시간 후)
     * @param clock 시간 소스
     * @return 신규 MultipartUploadSession (status: PREPARING)
     */
    public static MultipartUploadSession forNew(
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            S3UploadId s3UploadId,
            TotalParts totalParts,
            PartSize partSize,
            ExpirationTime expirationTime,
            Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new MultipartUploadSession(
                UploadSessionId.forNew(),
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                s3UploadId,
                totalParts,
                partSize,
                expirationTime,
                now,
                SessionStatus.PREPARING,
                null,
                null, // version: 신규 생성 시 null
                clock);
    }

    /**
     * 영속성 복원 (Mapper 전용).
     *
     * @param id 세션 ID (null 불가)
     * @param userContext 사용자 컨텍스트
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param s3UploadId S3 Multipart Upload ID
     * @param totalParts 총 Part 개수
     * @param partSize 각 Part 크기
     * @param expirationTime 만료 시각
     * @param createdAt 생성 시각
     * @param status 세션 상태
     * @param completedAt 완료 시각 (선택적)
     * @param version 낙관락 버전 (선택적)
     * @param clock 시간 소스
     * @return MultipartUploadSession
     * @throws IllegalArgumentException id가 null인 경우
     */
    public static MultipartUploadSession reconstitute(
            UploadSessionId id,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            S3UploadId s3UploadId,
            TotalParts totalParts,
            PartSize partSize,
            ExpirationTime expirationTime,
            LocalDateTime createdAt,
            SessionStatus status,
            LocalDateTime completedAt,
            Long version,
            Clock clock) {
        if (id == null) {
            throw new IllegalArgumentException("ID는 null일 수 없습니다.");
        }
        return new MultipartUploadSession(
                id,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                s3UploadId,
                totalParts,
                partSize,
                expirationTime,
                createdAt,
                status,
                completedAt,
                version,
                clock);
    }

    /** 생성자 (private). */
    private MultipartUploadSession(
            UploadSessionId id,
            UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            S3UploadId s3UploadId,
            TotalParts totalParts,
            PartSize partSize,
            ExpirationTime expirationTime,
            LocalDateTime createdAt,
            SessionStatus status,
            LocalDateTime completedAt,
            Long version,
            Clock clock) {
        this.id = id;
        this.userContext = userContext;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.s3UploadId = s3UploadId;
        this.totalParts = totalParts;
        this.partSize = partSize;
        this.expirationTime = expirationTime;
        this.createdAt = createdAt;
        this.status = status;
        this.completedAt = completedAt;
        this.version = version;
        this.clock = clock;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 세션을 활성화한다.
     *
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    public void activate() {
        validateStatusTransition(SessionStatus.ACTIVE);
        this.status = SessionStatus.ACTIVE;
    }

    /**
     * Multipart 업로드 완료 처리 (모든 Part 업로드 완료 후 S3 병합 요청).
     *
     * @param mergedETag S3에서 반환한 병합된 ETag
     * @param completedParts 완료된 Part 목록 (검증용)
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     * @throws SessionExpiredException 세션이 만료된 경우
     * @throws IncompletePartsException 모든 Part가 완료되지 않은 경우
     */
    public void complete(ETag mergedETag, List<CompletedPart> completedParts) {
        validateStatusTransition(SessionStatus.COMPLETED);

        if (!areAllPartsCompleted(completedParts)) {
            int completedCount =
                    (int) completedParts.stream().filter(CompletedPart::isCompleted).count();
            throw new IncompletePartsException(completedCount, totalParts.value());
        }

        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now(clock);
        this.mergedETag = mergedETag;

        // 도메인 이벤트 생성
        domainEvents.add(
                FileUploadCompletedEvent.of(
                        this.id,
                        this.fileName,
                        this.fileSize,
                        this.contentType,
                        this.bucket,
                        this.s3Key,
                        this.mergedETag,
                        this.userContext.userId(),
                        this.userContext.organization().id(),
                        this.userContext.tenant().id(),
                        this.completedAt));
    }

    /**
     * 세션 만료 처리.
     *
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    public void expire() {
        validateStatusTransition(SessionStatus.EXPIRED);
        this.status = SessionStatus.EXPIRED;
    }

    /**
     * 업로드 실패 처리.
     *
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    public void fail() {
        validateStatusTransition(SessionStatus.FAILED);
        this.status = SessionStatus.FAILED;
    }

    /**
     * 세션이 만료되지 않았는지 검증한다.
     *
     * @throws SessionExpiredException 세션이 만료된 경우
     */
    public void validateNotExpired() {
        if (expirationTime.isExpired(clock)) {
            throw new SessionExpiredException(expirationTime.value());
        }
    }

    /**
     * 세션이 활성 상태인지 검증한다.
     *
     * @throws InvalidSessionStatusException 상태가 ACTIVE가 아닌 경우
     */
    public void validateActiveStatus() {
        if (this.status != SessionStatus.ACTIVE) {
            throw new InvalidSessionStatusException(this.status, SessionStatus.ACTIVE);
        }
    }

    // ==================== private 검증 메서드 ====================

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

    /**
     * 모든 Part가 완료되었는지 확인한다.
     *
     * @param completedParts 완료된 Part 목록
     * @return 모든 Part가 완료되었으면 true
     */
    private boolean areAllPartsCompleted(List<CompletedPart> completedParts) {
        if (completedParts == null || completedParts.size() != totalParts.value()) {
            return false;
        }
        return completedParts.stream().allMatch(CompletedPart::isCompleted);
    }

    // ==================== Getter ====================

    public UploadSessionId getId() {
        return id;
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
     * @return 조직 ID
     */
    public long getOrganizationId() {
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

    public S3UploadId getS3UploadId() {
        return s3UploadId;
    }

    public String getS3UploadIdValue() {
        return s3UploadId.value();
    }

    public TotalParts getTotalParts() {
        return totalParts;
    }

    public int getTotalPartsValue() {
        return totalParts.value();
    }

    public PartSize getPartSize() {
        return partSize;
    }

    public long getPartSizeValue() {
        return partSize.bytes();
    }

    public ExpirationTime getExpirationTime() {
        return expirationTime;
    }

    public LocalDateTime getExpiresAt() {
        return expirationTime.value();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * S3에서 반환한 병합된 ETag를 반환한다.
     *
     * @return 병합된 ETag (완료 전이면 null)
     */
    public ETag getMergedETag() {
        return mergedETag;
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
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return expirationTime.isExpired(clock);
    }

    /**
     * Part 번호가 유효한지 확인한다.
     *
     * @param partNumber Part 번호
     * @return 유효하면 true
     */
    public boolean isValidPartNumber(int partNumber) {
        return totalParts.isValidPartNumber(partNumber);
    }

    /**
     * 낙관락 버전을 반환한다.
     *
     * @return 버전 (신규 생성 시 null)
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 도메인 이벤트를 반환하고 내부 목록을 비운다.
     *
     * @return 도메인 이벤트 목록 (수정 불가)
     */
    public List<FileUploadCompletedEvent> pollDomainEvents() {
        List<FileUploadCompletedEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
