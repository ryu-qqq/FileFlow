package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Upload Session Aggregate Root
 * 업로드 세션을 관리하는 집합 루트
 *
 * 비즈니스 규칙:
 * 1. UploadType에 따라 SINGLE 또는 MULTIPART 처리
 * 2. MULTIPART 타입은 반드시 MultipartUpload와 연결되어야 함
 * 3. Session은 한 번만 완료될 수 있음 (Immutable State)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSession {

    // ===== Aggregate 식별자 =====

    private final UploadSessionId id;
    private final SessionKey sessionKey;

    // ===== 기본 정보 =====

    private final TenantId tenantId;
    private final FileName fileName;
    private final Clock clock;
    private FileSize fileSize;  // External Download 시 동적으로 업데이트 가능

    // ===== 업로드 타입 지원 (SINGLE/MULTIPART) =====

    private UploadType uploadType;
    private MultipartUpload multipartUpload;  // MULTIPART 타입인 경우만 사용
    private StorageKey storageKey;  // S3 Storage Key (MULTIPART 타입인 경우 필수)

    // ===== 상태 관리 =====

    private SessionStatus status;
    private Long fileId;  // 완료 후 생성된 File ID
    private FailureReason failureReason;

    // ===== 타임스탬프 =====

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime expiresAt;  // Presigned URL 만료 시간


    // ===== Private 생성자 =====

    /**
     * Package-private 주요 생성자 (검증 포함)
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id       Upload Session ID (null 허용 - 신규 엔티티)
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param clock    시간 제공자
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    UploadSession(
        UploadSessionId id,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        Clock clock
    ) {
        if (tenantId
            == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (fileName
            == null) {
            throw new IllegalArgumentException("File Name은 필수입니다");
        }
        if (fileSize
            == null) {
            throw new IllegalArgumentException("File Size는 필수입니다");
        }

        this.id = id;
        this.sessionKey = SessionKey.generate();
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.clock = clock;
        this.status = SessionStatus.PENDING;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id            세션 ID
     * @param sessionKey    세션 키
     * @param tenantId      테넌트 ID
     * @param fileName      파일명
     * @param fileSize      파일 크기
     * @param clock         시간 제공자
     * @param uploadType    업로드 타입
     * @param storageKey    S3 Storage Key
     * @param status        상태
     * @param fileId        파일 ID
     * @param failureReason 실패 사유
     * @param createdAt     생성 시간
     * @param updatedAt     수정 시간
     * @param completedAt   완료 시간
     * @param failedAt      실패 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private UploadSession(
        UploadSessionId id,
        SessionKey sessionKey,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        Clock clock,
        UploadType uploadType,
        StorageKey storageKey,
        SessionStatus status,
        Long fileId,
        FailureReason failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        this.id = id;
        this.sessionKey = sessionKey;
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.clock = clock;
        this.uploadType = uploadType;
        this.storageKey = storageKey;
        this.status = status;
        this.fileId = fileId;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
    }

    // ===== Static Factory Methods =====

    /**
     * 신규 단일 파일 업로드 세션을 생성합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: PENDING, uploadType = SINGLE, ID = null</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 Command를 받아 새로운 Entity를 생성할 때</p>
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return 생성된 UploadSession (ID = null)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession forNew(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize
    ) {
        UploadSession session = new UploadSession(null, tenantId, fileName, fileSize, Clock.systemDefaultZone());
        session.uploadType = UploadType.SINGLE;
        return session;
    }

    /**
     * 신규 단일 파일 업로드 세션을 생성합니다 (StorageContext 기반).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: PENDING, uploadType = SINGLE, ID = null</p>
     *
     * <p><strong>StorageContext 활용:</strong></p>
     * <ul>
     *   <li>IAM 컨텍스트(Tenant, Organization, UserContext) 기반으로 StorageKey 자동 생성</li>
     *   <li>Tell, Don't Ask 패턴 준수 - StorageKey.generate()에 위임</li>
     * </ul>
     *
     * <p><strong>사용 시기:</strong> Application Layer에서 IAM 컨텍스트를 활용한 업로드 세션 생성 시</p>
     *
     * @param storageContext Storage Context (IAM 기반 스토리지 정책)
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return 생성된 UploadSession (ID = null, StorageKey 포함)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession createForSingleUpload(
        StorageContext storageContext,
        FileName fileName,
        FileSize fileSize
    ) {
        if (storageContext == null) {
            throw new IllegalArgumentException("StorageContext는 필수입니다");
        }

        // StorageContext에서 TenantId 추출 (Law of Demeter 준수)
        TenantId tenantId = storageContext.getTenantId();

        // StorageKey 생성 (Tell, Don't Ask 패턴)
        StorageKey storageKey = StorageKey.generate(storageContext, fileName);

        // Session 생성
        UploadSession session = forNew(tenantId, fileName, fileSize);
        session.storageKey = storageKey;

        return session;
    }

    /**
     * External Download용 업로드 세션을 생성합니다 (Static Factory Method).
     *
     * <p><strong>External Download 전용 세션을 생성</strong>합니다.</p>
     * <p>External Download는 사용자가 아닌 시스템이 수행하므로 Organization/UserContext가 없습니다.</p>
     * <p>초기 상태: PENDING, uploadType = SINGLE_UPLOAD, ID = null</p>
     *
     * <p><strong>사용 시기</strong>: 외부 URL에서 파일을 다운로드하여 S3에 저장할 때</p>
     * <p><strong>특징</strong>:</p>
     * <ul>
     *   <li>Organization, UserContext 없이 TenantId만 사용</li>
     *   <li>시스템이 수행하는 작업이므로 기본 StorageContext 생성</li>
     *   <li>파일 크기는 다운로드 진행 중에 업데이트됨</li>
     * </ul>
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기 (초기값, 다운로드 중 업데이트)
     * @return 생성된 UploadSession (External Download용)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession createForExternalDownload(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("FileName은 필수입니다");
        }
        if (fileSize == null) {
            fileSize = FileSize.of(0L); // External Download는 초기 크기를 모를 수 있음
        }

        // External Download용 시스템 StorageContext 생성
        // Organization과 UserContext는 null (시스템 작업)
        StorageContext storageContext = StorageContext.forSystemWithTenant(tenantId);

        // createForSingleUpload와 동일한 패턴 사용
        UploadSession session = forNew(tenantId, fileName, fileSize);  // External Download는 항상 SINGLE
        session.storageKey = StorageKey.generate(storageContext, fileName);

        return session;
    }

    /**
     * 신규 Multipart 업로드 세션을 생성합니다 (Static Factory Method).
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: PENDING, uploadType = MULTIPART, ID = null</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 대용량 파일 업로드 세션을 생성할 때</p>
     *
     * @param tenantId   테넌트 ID
     * @param fileName   파일명
     * @param fileSize   파일 크기
     * @param storageKey S3 Storage Key
     * @return 생성된 UploadSession (ID = null)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession forNewMultipart(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        StorageKey storageKey
    ) {
        UploadSession session = new UploadSession(null, tenantId, fileName, fileSize, Clock.systemDefaultZone());
        session.uploadType = UploadType.MULTIPART;
        session.storageKey = storageKey;
        return session;
    }

    /**
     * DB에서 조회한 데이터로 UploadSession 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원할 때 사용합니다.</p>
     * <p>모든 상태(status, uploadType, fileId 포함)를 그대로 복원합니다.</p>
     *
     * <p><strong>사용 시기</strong>: Persistence Layer에서 JPA Entity → Domain 변환 시</p>
     *
     * @param id            세션 ID (필수 - DB에서 조회된 ID)
     * @param sessionKey    세션 키
     * @param tenantId      테넌트 ID
     * @param fileName      파일명
     * @param fileSize      파일 크기
     * @param uploadType    업로드 타입
     * @param storageKey    S3 Storage Key
     * @param status        상태
     * @param fileId        파일 ID
     * @param failureReason 실패 사유
     * @param createdAt     생성 시간
     * @param updatedAt     수정 시간
     * @param completedAt   완료 시간
     * @param failedAt      실패 시간
     * @return 재구성된 UploadSession
     * @throws IllegalArgumentException id가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession reconstitute(
        UploadSessionId id,
        SessionKey sessionKey,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        UploadType uploadType,
        StorageKey storageKey,
        SessionStatus status,
        Long fileId,
        FailureReason failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        if (id
            == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new UploadSession(
            id,
            sessionKey,
            tenantId,
            fileName,
            fileSize,
            Clock.systemDefaultZone(),
            uploadType,
            storageKey,
            status,
            fileId,
            failureReason,
            createdAt,
            updatedAt,
            completedAt,
            failedAt
        );
    }

    // ===== Multipart 관련 비즈니스 메서드 =====

    /**
     * MultipartUpload Aggregate 연결
     * MULTIPART 타입 세션에만 사용 가능
     *
     * @param multipart MultipartUpload Aggregate
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void attachMultipart(MultipartUpload multipart) {
        if (this.uploadType
            != UploadType.MULTIPART) {
            throw new IllegalStateException(
                "Upload type is not MULTIPART: "
                    + uploadType
            );
        }

        if (!multipart.getUploadSessionIdValue().equals(this.getIdValue())) {
            throw new IllegalArgumentException(
                "Multipart session ID mismatch: expected="
                    + this.getIdValue()
                    + ", actual="
                    + multipart.getUploadSessionIdValue()
            );
        }

        this.multipartUpload = multipart;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Multipart 업로드 초기화
     * 상태: PENDING → IN_PROGRESS
     *
     * @param totalParts 전체 파트 수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void initMultipart(Integer totalParts) {
        if (this.uploadType
            != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }

        if (this.multipartUpload
            == null) {
            throw new IllegalStateException("Multipart not attached");
        }

        if (this.status
            != SessionStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot initialize: session already started or completed"
            );
        }

        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 파트 업로드 완료 마킹
     * Tell, Don't Ask: 내부에서 MultipartUpload에 위임
     *
     * @param part 업로드된 파트
     */
    public void markPartUploaded(UploadPart part) {
        if (this.uploadType
            != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }

        if (this.multipartUpload
            == null) {
            throw new IllegalStateException("Multipart not initialized");
        }

        if (this.status
            != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot mark part: session not in progress"
            );
        }

        // Delegate to MultipartUpload
        this.multipartUpload.addPart(part);
    }

    /**
     * Multipart 업로드 완료 가능 여부 확인
     * Tell, Don't Ask 패턴
     *
     * @return 완료 가능하면 true
     */
    public boolean canCompleteMultipart() {
        if (this.uploadType
            != UploadType.MULTIPART) {
            return false;
        }

        if (this.multipartUpload
            == null) {
            return false;
        }

        if (this.status
            != SessionStatus.IN_PROGRESS) {
            return false;
        }

        return this.multipartUpload.canComplete();
    }

    // ===== 공통 비즈니스 메서드 =====

    /**
     * 업로드 세션 시작
     * 상태: PENDING → IN_PROGRESS
     *
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void start() {
        if (this.status
            != SessionStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot start: session already started or completed"
            );
        }

        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 업로드 세션 완료
     * 상태: IN_PROGRESS → COMPLETED
     *
     * @param fileId 생성된 파일 ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void complete(Long fileId) {
        if (this.status
            != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot complete: session not in progress"
            );
        }

        // MULTIPART 타입인 경우 추가 검증
        if (this.uploadType
            == UploadType.MULTIPART) {
            if (!canCompleteMultipart()) {
                throw new IllegalStateException(
                    "Cannot complete: multipart upload not finished"
                );
            }
            // MultipartUpload도 완료 상태로 변경
            this.multipartUpload.complete();
        }

        this.fileId = fileId;
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 파일 크기 업데이트
     * External Download 등에서 실제 파일 크기를 알게 되었을 때 호출
     *
     * @param fileSize 실제 파일 크기
     */
    public void updateFileSize(FileSize fileSize) {
        if (this.status
            == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update file size: session already completed");
        }
        if (this.status
            == SessionStatus.FAILED) {
            throw new IllegalStateException("Cannot update file size: session already failed");
        }
        this.fileSize = fileSize;
    }

    /**
     * 업로드 세션 실패 처리
     * 상태: * → FAILED
     *
     * @param reason 실패 사유
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void fail(FailureReason reason) {
        if (this.status
            == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot fail completed session");
        }

        // MULTIPART 타입인 경우 MultipartUpload도 실패 처리
        if (this.uploadType
            == UploadType.MULTIPART
            && this.multipartUpload
            != null) {
            this.multipartUpload.fail();
        }

        this.failureReason = reason;
        this.status = SessionStatus.FAILED;
        this.failedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Presigned URL 만료 처리
     *
     * <p>Redis TTL 만료 이벤트 수신 시 호출됩니다.</p>
     *
     * <p><strong>처리 내용:</strong></p>
     * <ul>
     *   <li>상태를 EXPIRED로 변경</li>
     *   <li>FailureReason을 PRESIGNED_URL_EXPIRED로 설정</li>
     *   <li>failedAt 타임스탬프 기록</li>
     * </ul>
     *
     * <p><strong>멱등성 보장:</strong></p>
     * <ul>
     *   <li>이미 EXPIRED 상태면 무시</li>
     *   <li>이미 COMPLETED 상태면 예외 발생</li>
     * </ul>
     *
     * @throws IllegalStateException 이미 완료된 세션인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void expire() {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot expire completed session");
        }

        // 멱등성: 이미 EXPIRED 상태면 무시
        if (this.status == SessionStatus.EXPIRED) {
            return;
        }

        this.failureReason = FailureReason.PRESIGNED_URL_EXPIRED;
        this.status = SessionStatus.EXPIRED;
        this.failedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    // ===== Tell, Don't Ask 패턴 메서드 =====

    /**
     * Multipart 업로드 타입 여부
     *
     * @return MULTIPART 타입이면 true
     */
    public boolean isMultipart() {
        return uploadType
            == UploadType.MULTIPART;
    }

    /**
     * 진행 중 상태 여부
     *
     * @return IN_PROGRESS 상태면 true
     */
    public boolean isInProgress() {
        return status
            == SessionStatus.IN_PROGRESS;
    }

    /**
     * 완료 상태 여부
     *
     * @return COMPLETED 상태면 true
     */
    public boolean isCompleted() {
        return status
            == SessionStatus.COMPLETED;
    }

    /**
     * 실패 상태 여부
     *
     * @return FAILED 상태면 true
     */
    public boolean isFailed() {
        return status
            == SessionStatus.FAILED;
    }

    /**
     * Upload Session ID
     *
     * @return Upload Session ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadSessionId getId() {
        return id;
    }


    public Long getIdValue() {
        return id
            != null ?
            id.value() :
            null;
    }

    /**
     * Session Key
     *
     * @return Session Key
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public SessionKey getSessionKey() {
        return sessionKey;
    }

    /**
     * Tenant ID
     *
     * @return Tenant ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    public Long getTenantIdValue() {
        return tenantId.value();
    }

    /**
     * File Name
     *
     * @return File Name
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileName getFileName() {
        return fileName;
    }

    /**
     * File Size
     *
     * @return File Size
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FileSize getFileSize() {
        return fileSize;
    }

    /**
     * Upload Type
     *
     * @return Upload Type
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public UploadType getUploadType() {
        return uploadType;
    }

    public MultipartUpload getMultipartUpload() {
        return multipartUpload;
    }

    /**
     * Session Status
     *
     * @return Session Status
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public SessionStatus getStatus() {
        return status;
    }

    /**
     * File ID
     *
     * @return File ID
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * Failure Reason
     *
     * @return Failure Reason
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public FailureReason getFailureReason() {
        return failureReason;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public LocalDateTime getCompletedAt() {
        return completedAt;
    }


    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    /**
     * Presigned URL Expiration Time
     *
     * @return Presigned URL 만료 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Set Presigned URL Expiration Time
     *
     * <p>Presigned URL 생성 시 만료 시간을 설정합니다.</p>
     *
     * @param expiresAt Presigned URL 만료 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public StorageKey getStorageKey() {
        return storageKey;
    }

}
