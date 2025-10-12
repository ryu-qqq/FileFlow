package com.ryuqq.fileflow.domain.upload;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.event.UploadSessionCreated;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 파일 업로드 세션을 관리하는 Entity
 * Presigned URL 발급부터 업로드 완료까지의 생명주기를 추적합니다.
 *
 * Aggregate Root:
 * - 업로드 세션의 일관성 경계를 정의
 * - 상태 전이를 관리하고 비즈니스 규칙을 강제
 */
public final class UploadSession {

    private final String sessionId;
    private final PolicyKey policyKey;
    private final UploadRequest uploadRequest;
    private final String uploaderId;
    private final UploadStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final MultipartUploadInfo multipartUploadInfo; // nullable for single file uploads
    private final List<Object> domainEvents;

    private UploadSession(
            String sessionId,
            PolicyKey policyKey,
            UploadRequest uploadRequest,
            String uploaderId,
            UploadStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            MultipartUploadInfo multipartUploadInfo,
            List<Object> domainEvents
    ) {
        this.sessionId = sessionId;
        this.policyKey = policyKey;
        this.uploadRequest = uploadRequest;
        this.uploaderId = uploaderId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.multipartUploadInfo = multipartUploadInfo;
        this.domainEvents = new ArrayList<>(domainEvents);
    }

    /**
     * 새로운 업로드 세션을 생성합니다.
     *
     * @param policyKey 정책 키
     * @param uploadRequest 업로드 요청 정보
     * @param uploaderId 업로더 ID
     * @param expirationMinutes 만료 시간 (분)
     * @return UploadSession 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static UploadSession create(
            PolicyKey policyKey,
            UploadRequest uploadRequest,
            String uploaderId,
            int expirationMinutes
    ) {
        validatePolicyKey(policyKey);
        validateUploadRequest(uploadRequest);
        validateUploaderId(uploaderId);
        validateExpirationMinutes(expirationMinutes);

        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expirationMinutes);

        List<Object> events = new ArrayList<>();

        UploadSession session = new UploadSession(
                sessionId,
                policyKey,
                uploadRequest,
                uploaderId,
                UploadStatus.PENDING,
                now,
                expiresAt,
                null, // multipartUploadInfo - set later for multipart uploads
                events
        );

        // UploadSessionCreated 이벤트 발행
        UploadSessionCreated event = UploadSessionCreated.of(
                sessionId,
                uploaderId,
                policyKey.getValue(),
                expiresAt
        );
        session.domainEvents.add(event);

        return session;
    }

    /**
     * 기존 세션을 재구성합니다 (Repository에서 사용).
     *
     * @param sessionId 세션 ID
     * @param policyKey 정책 키
     * @param uploadRequest 업로드 요청 정보
     * @param uploaderId 업로더 ID
     * @param status 상태
     * @param createdAt 생성 시간
     * @param expiresAt 만료 시간
     * @return UploadSession 인스턴스
     */
    public static UploadSession reconstitute(
            String sessionId,
            PolicyKey policyKey,
            UploadRequest uploadRequest,
            String uploaderId,
            UploadStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        return new UploadSession(
                sessionId,
                policyKey,
                uploadRequest,
                uploaderId,
                status,
                createdAt,
                expiresAt,
                null, // multipartUploadInfo - set separately if needed
                new ArrayList<>()
        );
    }

    /**
     * 멀티파트 업로드 정보와 함께 세션을 재구성합니다.
     *
     * @param sessionId 세션 ID
     * @param policyKey 정책 키
     * @param uploadRequest 업로드 요청 정보
     * @param uploaderId 업로더 ID
     * @param status 상태
     * @param createdAt 생성 시간
     * @param expiresAt 만료 시간
     * @param multipartUploadInfo 멀티파트 업로드 정보
     * @return UploadSession 인스턴스
     */
    public static UploadSession reconstituteWithMultipart(
            String sessionId,
            PolicyKey policyKey,
            UploadRequest uploadRequest,
            String uploaderId,
            UploadStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            MultipartUploadInfo multipartUploadInfo
    ) {
        return new UploadSession(
                sessionId,
                policyKey,
                uploadRequest,
                uploaderId,
                status,
                createdAt,
                expiresAt,
                multipartUploadInfo,
                new ArrayList<>()
        );
    }

    /**
     * 세션이 만료되었는지 확인합니다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 세션이 활성 상태인지 확인합니다.
     *
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return status == UploadStatus.PENDING && !isExpired();
    }

    /**
     * 업로드를 완료 상태로 전환합니다.
     *
     * @return 완료 상태의 새로운 UploadSession 인스턴스
     * @throws IllegalStateException 완료 가능한 상태가 아닌 경우
     */
    public UploadSession complete() {
        if (status != UploadStatus.PENDING && status != UploadStatus.UPLOADING) {
            throw new IllegalStateException(
                    "Cannot complete upload. Current status: " + status
            );
        }
        if (isExpired()) {
            throw new IllegalStateException("Session has expired");
        }

        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                UploadStatus.COMPLETED,
                this.createdAt,
                this.expiresAt,
                this.multipartUploadInfo,
                this.domainEvents
        );
    }

    /**
     * 클라이언트의 업로드 완료 확인을 처리합니다.
     *
     * S3 Event보다 먼저 클라이언트가 업로드 완료를 알릴 때 사용합니다.
     * PENDING 상태에서만 COMPLETED로 전환 가능합니다.
     *
     * @return 완료 상태의 새로운 UploadSession 인스턴스
     * @throws IllegalStateException 확인 가능한 상태가 아닌 경우
     */
    public UploadSession confirmUpload() {
        if (status != UploadStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot confirm upload. Current status: " + status +
                    ". Only PENDING sessions can be confirmed."
            );
        }
        if (isExpired()) {
            throw new IllegalStateException("Session has expired");
        }

        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                UploadStatus.COMPLETED,
                this.createdAt,
                this.expiresAt,
                this.multipartUploadInfo,
                this.domainEvents
        );
    }

    /**
     * 업로드를 실패 상태로 전환합니다.
     *
     * @return 실패 상태의 새로운 UploadSession 인스턴스
     */
    public UploadSession fail() {
        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                UploadStatus.FAILED,
                this.createdAt,
                this.expiresAt,
                this.multipartUploadInfo,
                this.domainEvents
        );
    }

    /**
     * 업로드를 진행 중 상태로 전환합니다.
     *
     * @return 진행 중 상태의 새로운 UploadSession 인스턴스
     * @throws IllegalStateException 진행 중으로 전환할 수 없는 상태인 경우
     */
    public UploadSession startUploading() {
        if (status != UploadStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot start uploading. Current status: " + status
            );
        }
        if (isExpired()) {
            throw new IllegalStateException("Session has expired");
        }

        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                UploadStatus.UPLOADING,
                this.createdAt,
                this.expiresAt,
                this.multipartUploadInfo,
                this.domainEvents
        );
    }

    /**
     * 업로드를 취소 상태로 전환합니다.
     *
     * @return 취소 상태의 새로운 UploadSession 인스턴스
     * @throws IllegalStateException 취소할 수 없는 상태인 경우
     */
    public UploadSession cancel() {
        if (status != UploadStatus.PENDING && status != UploadStatus.UPLOADING) {
            throw new IllegalStateException(
                    "Cannot cancel upload. Current status: " + status
            );
        }

        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                UploadStatus.CANCELLED,
                this.createdAt,
                this.expiresAt,
                this.multipartUploadInfo,
                this.domainEvents
        );
    }

    /**
     * 멀티파트 업로드 정보를 설정한 새로운 세션을 반환합니다.
     *
     * 세션 생성 후 멀티파트 업로드를 시작할 때 사용됩니다.
     *
     * @param multipartUploadInfo 멀티파트 업로드 정보
     * @return 멀티파트 업로드 정보가 설정된 새로운 UploadSession 인스턴스
     * @throws IllegalArgumentException multipartUploadInfo가 null인 경우
     */
    public UploadSession withMultipartInfo(MultipartUploadInfo multipartUploadInfo) {
        if (multipartUploadInfo == null) {
            throw new IllegalArgumentException("MultipartUploadInfo cannot be null");
        }

        return new UploadSession(
                this.sessionId,
                this.policyKey,
                this.uploadRequest,
                this.uploaderId,
                this.status,
                this.createdAt,
                this.expiresAt,
                multipartUploadInfo,
                this.domainEvents
        );
    }

    // ========== Validation Methods ==========

    private static void validatePolicyKey(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }
    }

    private static void validateUploadRequest(UploadRequest uploadRequest) {
        if (uploadRequest == null) {
            throw new IllegalArgumentException("UploadRequest cannot be null");
        }
    }

    private static void validateUploaderId(String uploaderId) {
        if (uploaderId == null || uploaderId.trim().isEmpty()) {
            throw new IllegalArgumentException("UploaderId cannot be null or empty");
        }
    }

    private static void validateExpirationMinutes(int expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("ExpirationMinutes must be positive");
        }
        if (expirationMinutes > 1440) { // 24시간
            throw new IllegalArgumentException("ExpirationMinutes cannot exceed 1440 (24 hours)");
        }
    }

    // ========== Domain Events ==========

    /**
     * 도메인 이벤트 목록을 반환합니다.
     *
     * @return 불변 이벤트 리스트
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트를 초기화합니다.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== Getters ==========

    public String getSessionId() {
        return sessionId;
    }

    public PolicyKey getPolicyKey() {
        return policyKey;
    }

    public UploadRequest getUploadRequest() {
        return uploadRequest;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 멀티파트 업로드 정보를 반환합니다.
     *
     * @return 멀티파트 업로드 정보 (없으면 empty Optional)
     */
    public Optional<MultipartUploadInfo> getMultipartUploadInfo() {
        return Optional.ofNullable(multipartUploadInfo);
    }

    /**
     * 멀티파트 업로드 여부를 확인합니다.
     *
     * @return 멀티파트 업로드 여부
     */
    public boolean isMultipartUpload() {
        return multipartUploadInfo != null;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadSession that = (UploadSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "UploadSession{" +
                "sessionId='" + sessionId + '\'' +
                ", policyKey=" + policyKey +
                ", uploaderId='" + uploaderId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", isExpired=" + isExpired() +
                '}';
    }
}
