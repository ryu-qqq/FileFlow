package com.ryuqq.fileflow.domain.session;

import java.time.Clock;
import java.time.LocalDateTime;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * 업로드 세션 Aggregate Root.
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
        
        fileSize.validateForUploadType(uploadType);
        
        String fileId = sessionId.value();
        S3Path s3Path = S3Path.from(role, tenantId, sellerName, customPath, fileId, mimeType.value());
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(15);
        
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

