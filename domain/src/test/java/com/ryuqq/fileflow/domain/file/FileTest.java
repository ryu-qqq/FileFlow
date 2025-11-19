package com.ryuqq.fileflow.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.file.vo.S3Path;
import com.ryuqq.fileflow.domain.file.vo.UploadType;
import com.ryuqq.fileflow.domain.session.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.UserRole;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File Aggregate Tests")
class FileTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.systemDefault());
    private static final LocalDateTime FIXED_TIME = LocalDateTime.now(FIXED_CLOCK);

    @Test
    @DisplayName("forNew()로 신규 파일을 생성할 수 있어야 한다")
    void shouldCreateNewFileWithForNew() {
        // given
        UploadSession session = UploadSession.forNew(
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024), // 1MB
            MimeType.of("image/jpeg"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            FIXED_CLOCK
        );

        // when
        File file = File.forNew(session, FIXED_CLOCK);

        // then
        assertThat(file.getFileId()).isNotNull();
        assertThat(file.getFileId().isNew()).isFalse();
        assertThat(file.getUserId()).isEqualTo(session.getUserId());
        assertThat(file.getTenantId()).isEqualTo(session.getTenantId());
        assertThat(file.getRole()).isEqualTo(session.getRole());
        assertThat(file.getFileName()).isEqualTo(session.getFileName());
        assertThat(file.getFileSize()).isEqualTo(session.getFileSize());
        assertThat(file.getMimeType()).isEqualTo(session.getMimeType());
        assertThat(file.getS3Path()).isEqualTo(session.getS3Path());
        assertThat(file.getUploadType()).isEqualTo(session.getUploadType());
        assertThat(file.getUploadedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.getUpdatedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.isDeleted()).isFalse();
        assertThat(file.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("of()로 기존 fileId로 파일을 생성할 수 있어야 한다")
    void shouldCreateFileWithOf() {
        // given
        SessionId fileId = SessionId.from("550e8400-e29b-41d4-a716-446655440000");
        UploadSession session = UploadSession.forNew(
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            1L,
            1L,
            UserRole.DEFAULT,
            "seller1",
            FIXED_CLOCK
        );

        // when
        File file = File.of(fileId, session, FIXED_CLOCK);

        // then
        assertThat(file.getFileId()).isEqualTo(fileId);
        assertThat(file.getUserId()).isEqualTo(session.getUserId());
        assertThat(file.getTenantId()).isEqualTo(session.getTenantId());
        assertThat(file.getRole()).isEqualTo(session.getRole());
        assertThat(file.getFileName()).isEqualTo(session.getFileName());
        assertThat(file.getFileSize()).isEqualTo(session.getFileSize());
        assertThat(file.getMimeType()).isEqualTo(session.getMimeType());
        assertThat(file.getS3Path()).isEqualTo(session.getS3Path());
        assertThat(file.getUploadType()).isEqualTo(session.getUploadType());
        assertThat(file.getUploadedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.getUpdatedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.isDeleted()).isFalse();
        assertThat(file.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute()로 영속성 복원된 파일을 생성할 수 있어야 한다")
    void shouldReconstituteFile() {
        // given
        SessionId fileId = SessionId.from("550e8400-e29b-41d4-a716-446655440000");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        FileName fileName = FileName.from("test.jpg");
        FileSize fileSize = FileSize.of(1024 * 1024);
        MimeType mimeType = MimeType.of("image/jpeg");
        S3Path s3Path = S3Path.from(role, tenantId, "seller1", "uploads", fileId.value(), mimeType.value());
        UploadType uploadType = UploadType.SINGLE;
        LocalDateTime uploadedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        boolean deleted = false;
        LocalDateTime deletedAt = null;

        // when
        File file = File.reconstitute(
            fileId,
            userId,
            tenantId,
            role,
            fileName,
            fileSize,
            mimeType,
            s3Path,
            uploadType,
            FIXED_CLOCK,
            uploadedAt,
            updatedAt,
            deleted,
            deletedAt
        );

        // then
        assertThat(file.getFileId()).isEqualTo(fileId);
        assertThat(file.getUserId()).isEqualTo(userId);
        assertThat(file.getTenantId()).isEqualTo(tenantId);
        assertThat(file.getRole()).isEqualTo(role);
        assertThat(file.getFileName()).isEqualTo(fileName);
        assertThat(file.getFileSize()).isEqualTo(fileSize);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
        assertThat(file.getS3Path()).isEqualTo(s3Path);
        assertThat(file.getUploadType()).isEqualTo(uploadType);
        assertThat(file.getUploadedAt()).isEqualTo(uploadedAt);
        assertThat(file.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(file.isDeleted()).isEqualTo(deleted);
        assertThat(file.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("uploadedAt과 updatedAt은 자동으로 설정되어야 한다")
    void shouldSetUploadedAtAndUpdatedAtAutomatically() {
        // given
        UploadSession session = UploadSession.forNew(
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            1L,
            1L,
            UserRole.DEFAULT,
            "seller1",
            FIXED_CLOCK
        );

        // when
        File file = File.forNew(session, FIXED_CLOCK);

        // then
        assertThat(file.getUploadedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.getUpdatedAt()).isEqualTo(FIXED_TIME);
        assertThat(file.getUploadedAt()).isEqualTo(file.getUpdatedAt());
    }
}

