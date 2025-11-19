package com.ryuqq.fileflow.domain.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UploadSession Aggregate Tests")
class UploadSessionTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.systemDefault());
    private static final LocalDateTime FIXED_TIME = LocalDateTime.now(FIXED_CLOCK);

    @Test
    @DisplayName("forNew()로 신규 세션을 생성할 수 있어야 한다")
    void shouldCreateNewSessionWithForNew() {
        // given
        UploadType uploadType = UploadType.SINGLE;
        String customPath = "uploads";
        FileName fileName = FileName.from("test.jpg");
        FileSize fileSize = FileSize.of(1024 * 1024); // 1MB
        MimeType mimeType = MimeType.of("image/jpeg");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when
        UploadSession session = UploadSession.forNew(
            uploadType,
            customPath,
            fileName,
            fileSize,
            mimeType,
            userId,
            tenantId,
            role,
            sellerName,
            FIXED_CLOCK
        );

        // then
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getSessionId().isNew()).isFalse();
        assertThat(session.getUserId()).isEqualTo(userId);
        assertThat(session.getTenantId()).isEqualTo(tenantId);
        assertThat(session.getRole()).isEqualTo(role);
        assertThat(session.getSellerName()).isEqualTo(sellerName);
        assertThat(session.getUploadType()).isEqualTo(uploadType);
        assertThat(session.getCustomPath()).isEqualTo(customPath);
        assertThat(session.getFileName()).isEqualTo(fileName);
        assertThat(session.getFileSize()).isEqualTo(fileSize);
        assertThat(session.getMimeType()).isEqualTo(mimeType);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.PREPARING);
        assertThat(session.getCreatedAt()).isEqualTo(FIXED_TIME);
        assertThat(session.getUpdatedAt()).isEqualTo(FIXED_TIME);
        assertThat(session.getExpiresAt()).isEqualTo(FIXED_TIME.plusMinutes(15));
        assertThat(session.getS3Path()).isNotNull();
    }

    @Test
    @DisplayName("파일 크기가 SINGLE 타입의 최대 크기를 초과하면 예외가 발생해야 한다")
    void shouldValidateFileSizeForUploadType() {
        // given
        UploadType uploadType = UploadType.SINGLE;
        FileSize oversizedFile = FileSize.of(UploadType.SINGLE.getMaxSize() + 1);
        String customPath = "uploads";
        FileName fileName = FileName.from("test.jpg");
        MimeType mimeType = MimeType.of("image/jpeg");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when & then
        assertThatThrownBy(() -> UploadSession.forNew(
            uploadType,
            customPath,
            fileName,
            oversizedFile,
            mimeType,
            userId,
            tenantId,
            role,
            sellerName,
            FIXED_CLOCK
        )).isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    @DisplayName("지원하지 않는 MIME 타입이면 예외가 발생해야 한다")
    void shouldValidateMimeType() {
        // given
        UploadType uploadType = UploadType.SINGLE;
        String customPath = "uploads";
        FileName fileName = FileName.from("test.pdf");
        FileSize fileSize = FileSize.of(1024 * 1024); // 1MB
        MimeType unsupportedMimeType = MimeType.of("application/pdf");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when & then
        assertThatThrownBy(() -> UploadSession.forNew(
            uploadType,
            customPath,
            fileName,
            fileSize,
            unsupportedMimeType,
            userId,
            tenantId,
            role,
            sellerName,
            FIXED_CLOCK
        )).isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    @DisplayName("expiresAt은 createdAt에서 15분 후여야 한다")
    void shouldSetExpiresAt15Minutes() {
        // given
        UploadType uploadType = UploadType.SINGLE;
        String customPath = "uploads";
        FileName fileName = FileName.from("test.jpg");
        FileSize fileSize = FileSize.of(1024 * 1024); // 1MB
        MimeType mimeType = MimeType.of("image/jpeg");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when
        UploadSession session = UploadSession.forNew(
            uploadType,
            customPath,
            fileName,
            fileSize,
            mimeType,
            userId,
            tenantId,
            role,
            sellerName,
            FIXED_CLOCK
        );

        // then
        assertThat(session.getExpiresAt()).isEqualTo(session.getCreatedAt().plusMinutes(15));
    }

    @Test
    @DisplayName("신규 세션의 상태는 PREPARING이어야 한다")
    void shouldInitializeStatusAsPreparing() {
        // given
        UploadType uploadType = UploadType.SINGLE;
        String customPath = "uploads";
        FileName fileName = FileName.from("test.jpg");
        FileSize fileSize = FileSize.of(1024 * 1024); // 1MB
        MimeType mimeType = MimeType.of("image/jpeg");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when
        UploadSession session = UploadSession.forNew(
            uploadType,
            customPath,
            fileName,
            fileSize,
            mimeType,
            userId,
            tenantId,
            role,
            sellerName,
            FIXED_CLOCK
        );

        // then
        assertThat(session.getStatus()).isEqualTo(SessionStatus.PREPARING);
    }
}

