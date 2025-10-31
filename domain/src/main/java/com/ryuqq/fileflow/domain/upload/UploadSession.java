package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

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

    private final Long id;
    private final SessionKey sessionKey;

    // ===== 기본 정보 =====

    private final TenantId tenantId;
    private final FileName fileName;
    private final FileSize fileSize;

    // ===== 업로드 타입 지원 (SINGLE/MULTIPART) =====

    private UploadType uploadType;
    private MultipartUpload multipartUpload;  // MULTIPART 타입인 경우만 사용

    // ===== 상태 관리 =====

    private SessionStatus status;
    private Long fileId;  // 완료 후 생성된 File ID
    private FailureReason failureReason;

    // ===== 타임스탬프 =====

    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    /**
     * 업로드 타입 Enum
     */
    public enum UploadType {
        SINGLE,     // 단일 파일 업로드
        MULTIPART   // 대용량 파일 분할 업로드
    }

    /**
     * 세션 상태 Enum
     */
    public enum SessionStatus {
        PENDING,      // 초기 생성 상태
        IN_PROGRESS,  // 업로드 진행 중
        COMPLETED,    // 업로드 완료
        FAILED        // 업로드 실패
    }

    // ===== Private 생성자 =====

    /**
     * Private 생성자 - Static Factory Method를 통해서만 생성
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     */
    private UploadSession(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize
    ) {
        this.id = null;  // DB에서 생성
        this.sessionKey = SessionKey.generate();
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = SessionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Reconstitute 생성자 - DB에서 읽을 때 사용
     *
     * @param id 세션 ID
     * @param sessionKey 세션 키
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param uploadType 업로드 타입
     * @param status 상태
     * @param fileId 파일 ID
     * @param failureReason 실패 사유
     * @param createdAt 생성 시간
     * @param completedAt 완료 시간
     * @param failedAt 실패 시간
     */
    private UploadSession(
        Long id,
        SessionKey sessionKey,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        UploadType uploadType,
        SessionStatus status,
        Long fileId,
        FailureReason failureReason,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        this.id = id;
        this.sessionKey = sessionKey;
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadType = uploadType;
        this.status = status;
        this.fileId = fileId;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
    }

    // ===== Static Factory Methods =====

    /**
     * 단일 파일 업로드 세션 생성
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return 단일 업로드 세션
     */
    public static UploadSession create(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize
    ) {
        UploadSession session = new UploadSession(tenantId, fileName, fileSize);
        session.uploadType = UploadType.SINGLE;
        return session;
    }

    /**
     * Multipart 업로드 세션 생성
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @return Multipart 업로드 세션
     */
    public static UploadSession createForMultipart(
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize
    ) {
        UploadSession session = new UploadSession(tenantId, fileName, fileSize);
        session.uploadType = UploadType.MULTIPART;
        return session;
    }

    /**
     * DB에서 읽어온 데이터로 객체 재구성 (Reconstitute)
     *
     * @param id 세션 ID
     * @param sessionKey 세션 키
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param uploadType 업로드 타입
     * @param status 상태
     * @param fileId 파일 ID
     * @param failureReason 실패 사유
     * @param createdAt 생성 시간
     * @param completedAt 완료 시간
     * @param failedAt 실패 시간
     * @return 재구성된 UploadSession
     */
    public static UploadSession reconstitute(
        Long id,
        SessionKey sessionKey,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        UploadType uploadType,
        SessionStatus status,
        Long fileId,
        FailureReason failureReason,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime failedAt
    ) {
        return new UploadSession(
            id,
            sessionKey,
            tenantId,
            fileName,
            fileSize,
            uploadType,
            status,
            fileId,
            failureReason,
            createdAt,
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
     */
    public void attachMultipart(MultipartUpload multipart) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException(
                "Upload type is not MULTIPART: " + uploadType
            );
        }

        if (!multipart.getUploadSessionId().value().equals(this.id)) {
            throw new IllegalArgumentException(
                "Multipart session ID mismatch: expected=" + this.id
                    + ", actual=" + multipart.getUploadSessionId().value()
            );
        }

        this.multipartUpload = multipart;
    }

    /**
     * Multipart 업로드 초기화
     * 상태: PENDING → IN_PROGRESS
     *
     * @param totalParts 전체 파트 수
     */
    public void initMultipart(Integer totalParts) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }

        if (this.multipartUpload == null) {
            throw new IllegalStateException("Multipart not attached");
        }

        if (this.status != SessionStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot initialize: session already started or completed"
            );
        }

        this.status = SessionStatus.IN_PROGRESS;
    }

    /**
     * 파트 업로드 완료 마킹
     * Tell, Don't Ask: 내부에서 MultipartUpload에 위임
     *
     * @param part 업로드된 파트
     */
    public void markPartUploaded(UploadPart part) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }

        if (this.multipartUpload == null) {
            throw new IllegalStateException("Multipart not initialized");
        }

        if (this.status != SessionStatus.IN_PROGRESS) {
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
        if (this.uploadType != UploadType.MULTIPART) {
            return false;
        }

        if (this.multipartUpload == null) {
            return false;
        }

        if (this.status != SessionStatus.IN_PROGRESS) {
            return false;
        }

        return this.multipartUpload.canComplete();
    }

    // ===== 공통 비즈니스 메서드 =====

    /**
     * 업로드 세션 시작
     * 상태: PENDING → IN_PROGRESS
     */
    public void start() {
        if (this.status != SessionStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot start: session already started or completed"
            );
        }

        this.status = SessionStatus.IN_PROGRESS;
    }

    /**
     * 업로드 세션 완료
     * 상태: IN_PROGRESS → COMPLETED
     *
     * @param fileId 생성된 파일 ID
     */
    public void complete(Long fileId) {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot complete: session not in progress"
            );
        }

        // MULTIPART 타입인 경우 추가 검증
        if (this.uploadType == UploadType.MULTIPART) {
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
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 업로드 세션 실패 처리
     * 상태: * → FAILED
     *
     * @param reason 실패 사유
     */
    public void fail(FailureReason reason) {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot fail completed session");
        }

        // MULTIPART 타입인 경우 MultipartUpload도 실패 처리
        if (this.uploadType == UploadType.MULTIPART && this.multipartUpload != null) {
            this.multipartUpload.fail();
        }

        this.failureReason = reason;
        this.status = SessionStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }

    // ===== Tell, Don't Ask 패턴 메서드 =====

    /**
     * Multipart 업로드 타입 여부
     *
     * @return MULTIPART 타입이면 true
     */
    public boolean isMultipart() {
        return uploadType == UploadType.MULTIPART;
    }

    /**
     * 진행 중 상태 여부
     *
     * @return IN_PROGRESS 상태면 true
     */
    public boolean isInProgress() {
        return status == SessionStatus.IN_PROGRESS;
    }

    /**
     * 완료 상태 여부
     *
     * @return COMPLETED 상태면 true
     */
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    /**
     * 실패 상태 여부
     *
     * @return FAILED 상태면 true
     */
    public boolean isFailed() {
        return status == SessionStatus.FAILED;
    }

    // ===== Getter (필요한 것만 제공, NO Setter) =====

    public Long getId() {
        return id;
    }

    public SessionKey getSessionKey() {
        return sessionKey;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    /**
     * MultipartUpload 반환
     * MULTIPART 타입이 아니면 null
     *
     * @return MultipartUpload (nullable)
     */
    public MultipartUpload getMultipartUpload() {
        return multipartUpload;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Long getFileId() {
        return fileId;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }
}
