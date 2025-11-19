package com.ryuqq.fileflow.domain.session;

import java.time.Clock;
import java.time.LocalDateTime;

import com.ryuqq.fileflow.domain.file.exception.UnsupportedFileTypeException;
import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.exception.FileSizeExceededException;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * 업로드 세션 Aggregate Root.
 *
 * <p>
 * 파일 업로드 세션을 관리하는 도메인 엔티티입니다.
 * 세션 생성, 상태 전환, 만료 시각 관리 등의 비즈니스 로직을 캡슐화합니다.
 * </p>
 *
 * <p>
 * <strong>생성 패턴</strong>:
 * </p>
 * <ul>
 *     <li>forNew(): 신규 세션 생성 (ID 자동 생성)</li>
 *     <li>of(): ID 기반 세션 생성 (비즈니스 로직용)</li>
 *     <li>reconstitute(): 영속성 복원용 (Repository 전용)</li>
 * </ul>
 *
 * <p>
 * <strong>불변식 (Invariants)</strong>:
 * </p>
 * <ul>
 *     <li>파일 크기: SINGLE ≤ 5GB, MULTIPART ≤ 5TB</li>
 *     <li>파일 타입: image/* 또는 text/html만 허용</li>
 *     <li>세션 유효시간: 15분 고정</li>
 *     <li>상태 전환 규칙: PREPARING → ACTIVE → {COMPLETED, EXPIRED, FAILED}</li>
 * </ul>
 */
public class UploadSession {

    private final SessionId sessionId;
    private final Long userId;
    private final Long tenantId;
    private final UserRole role;
    private final String sellerName;
    private final UploadType uploadType;
    private final String customPath;
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final S3Path s3Path;
    private SessionStatus status;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime expiresAt;
    private LocalDateTime completedAt;

    private UploadSession(
        SessionId sessionId,
        Long userId,
        Long tenantId,
        UserRole role,
        String sellerName,
        UploadType uploadType,
        String customPath,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Path s3Path,
        SessionStatus status,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt,
        LocalDateTime completedAt
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
        this.sellerName = sellerName;
        this.uploadType = uploadType;
        this.customPath = customPath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.s3Path = s3Path;
        this.status = status;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.completedAt = completedAt;
    }

    /**
     * 신규 업로드 세션 생성 (ID 자동 생성).
     *
     * <p>
     * 새 업로드 세션을 생성하며 다음을 수행합니다:
     * </p>
     * <ul>
     *     <li>SessionId.forNew()로 UUID 생성</li>
     *     <li>파일 크기 검증 (업로드 타입별 최대 크기)</li>
     *     <li>S3 경로 생성 (역할, 테넌트, 셀러명 기반)</li>
     *     <li>만료 시각 계산 (생성 시각 + 15분)</li>
     *     <li>상태 초기화 (PREPARING)</li>
     * </ul>
     *
     * @param uploadType 업로드 타입 (SINGLE 또는 MULTIPART)
     * @param customPath 커스텀 경로
     * @param fileName 파일 이름 VO
     * @param fileSize 파일 크기 VO
     * @param mimeType MIME 타입 VO
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param role 사용자 역할
     * @param sellerName 셀러명 (SELLER/DEFAULT 역할일 때 필수, ADMIN일 때 무시)
     * @param clock 시간 의존성 (테스트 가능성)
     * @return 신규 UploadSession
     * @throws FileSizeExceededException 파일 크기가 업로드 타입별 최대 크기를 초과한 경우
     * @throws UnsupportedFileTypeException 지원하지 않는 MIME 타입인 경우
     */
    public static UploadSession forNew(
        UploadType uploadType,
        String customPath,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        Long userId,
        Long tenantId,
        UserRole role,
        String sellerName,
        Clock clock
    ) {
        SessionId sessionId = SessionId.forNew();
        
        validateFileSize(fileSize, uploadType);
        
        String fileId = sessionId.value();
        S3Path s3Path = createS3Path(role, tenantId, sellerName, customPath, fileId, mimeType);
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = calculateExpiresAt(now);
        
        return new UploadSession(
            sessionId,
            userId,
            tenantId,
            role,
            sellerName,
            uploadType,
            customPath,
            fileName,
            fileSize,
            mimeType,
            s3Path,
            SessionStatus.PREPARING,
            clock,
            now,
            now,
            expiresAt,
            null
        );
    }

    /**
     * 파일 크기를 업로드 타입별 최대 크기와 검증한다.
     *
     * @param fileSize 파일 크기 VO
     * @param uploadType 업로드 타입
     * @throws FileSizeExceededException 파일 크기가 최대 크기를 초과한 경우
     */
    private static void validateFileSize(FileSize fileSize, UploadType uploadType) {
        fileSize.validateForUploadType(uploadType);
    }

    /**
     * S3 경로를 생성한다.
     *
     * @param role 사용자 역할
     * @param tenantId 테넌트 ID
     * @param sellerName 셀러명
     * @param customPath 커스텀 경로
     * @param fileId 파일 ID
     * @param mimeType MIME 타입 VO
     * @return S3Path VO
     */
    private static S3Path createS3Path(
        UserRole role,
        Long tenantId,
        String sellerName,
        String customPath,
        String fileId,
        MimeType mimeType
    ) {
        return S3Path.from(role, tenantId, sellerName, customPath, fileId, mimeType.value());
    }

    /**
     * 만료 시각을 계산한다 (생성 시각 + 15분).
     *
     * @param createdAt 생성 시각
     * @return 만료 시각
     */
    private static LocalDateTime calculateExpiresAt(LocalDateTime createdAt) {
        return createdAt.plusMinutes(15);
    }

    // ==================== Getter 메서드 ====================

    public SessionId getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public UserRole getRole() {
        return role;
    }

    public String getSellerName() {
        return sellerName;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public String getCustomPath() {
        return customPath;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public S3Path getS3Path() {
        return s3Path;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}

