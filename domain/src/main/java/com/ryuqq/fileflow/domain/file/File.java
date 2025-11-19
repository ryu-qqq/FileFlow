package com.ryuqq.fileflow.domain.file;

import java.time.Clock;
import java.time.LocalDateTime;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

/**
 * 파일 Aggregate Root.
 */
public class File {

    private final SessionId fileId;
    private final Long userId;
    private final Long tenantId;
    private final UserRole role;
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final S3Path s3Path;
    private final UploadType uploadType;
    private final Clock clock;
    private final LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    private LocalDateTime deletedAt;

    private File(
        SessionId fileId,
        Long userId,
        Long tenantId,
        UserRole role,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Path s3Path,
        UploadType uploadType,
        Clock clock,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        boolean deleted,
        LocalDateTime deletedAt
    ) {
        this.fileId = fileId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.s3Path = s3Path;
        this.uploadType = uploadType;
        this.clock = clock;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    public static File forNew(UploadSession session, Clock clock) {
        SessionId fileId = SessionId.forNew();
        
        LocalDateTime now = LocalDateTime.now(clock);
        
        return new File(
            fileId,
            session.getUserId(),
            session.getTenantId(),
            session.getRole(),
            session.getFileName(),
            session.getFileSize(),
            session.getMimeType(),
            session.getS3Path(),
            session.getUploadType(),
            clock,
            now,
            now,
            false,
            null
        );
    }

    public static File of(SessionId fileId, UploadSession session, Clock clock) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 null일 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        
        return new File(
            fileId,
            session.getUserId(),
            session.getTenantId(),
            session.getRole(),
            session.getFileName(),
            session.getFileSize(),
            session.getMimeType(),
            session.getS3Path(),
            session.getUploadType(),
            clock,
            now,
            now,
            false,
            null
        );
    }

    public static File reconstitute(
        SessionId fileId,
        Long userId,
        Long tenantId,
        UserRole role,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Path s3Path,
        UploadType uploadType,
        Clock clock,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt,
        boolean deleted,
        LocalDateTime deletedAt
    ) {
        return new File(
            fileId,
            userId,
            tenantId,
            role,
            fileName,
            fileSize,
            mimeType,
            s3Path,
            uploadType,
            clock,
            uploadedAt,
            updatedAt,
            deleted,
            deletedAt
        );
    }

    public SessionId getFileId() {
        return fileId;
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

    public UploadType getUploadType() {
        return uploadType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}

