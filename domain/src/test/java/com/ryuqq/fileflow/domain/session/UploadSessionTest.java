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
import com.ryuqq.fileflow.domain.session.exception.InvalidSessionStatusException;
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
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";

        // when & then - MimeType.of()에서 이미 검증되므로 예외가 발생
        assertThatThrownBy(() -> {
            MimeType unsupportedMimeType = MimeType.of("application/pdf");
            UploadSession.forNew(
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
            );
        }).isInstanceOf(UnsupportedFileTypeException.class);
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

    @Test
    @DisplayName("of()로 기존 SessionId로 세션을 생성할 수 있어야 한다")
    void shouldCreateSessionWithOf() {
        // given
        SessionId sessionId = SessionId.from("550e8400-e29b-41d4-a716-446655440000");
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
        UploadSession session = UploadSession.of(
            sessionId,
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
        assertThat(session.getSessionId()).isEqualTo(sessionId);
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
    @DisplayName("reconstitute()로 영속성 복원된 세션을 생성할 수 있어야 한다")
    void shouldReconstituteSession() {
        // given
        SessionId sessionId = SessionId.from("550e8400-e29b-41d4-a716-446655440000");
        Long userId = 1L;
        Long tenantId = 1L;
        UserRole role = UserRole.DEFAULT;
        String sellerName = "seller1";
        UploadType uploadType = UploadType.SINGLE;
        String customPath = "uploads";
        FileName fileName = FileName.from("test.jpg");
        FileSize fileSize = FileSize.of(1024 * 1024); // 1MB
        MimeType mimeType = MimeType.of("image/jpeg");
        S3Path s3Path = S3Path.from(role, tenantId, sellerName, customPath, sessionId.value(), mimeType.value());
        SessionStatus status = SessionStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2024, 1, 1, 10, 15, 0);
        LocalDateTime completedAt = null;

        // when
        UploadSession session = UploadSession.reconstitute(
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
            status,
            FIXED_CLOCK,
            createdAt,
            updatedAt,
            expiresAt,
            completedAt
        );

        // then
        assertThat(session.getSessionId()).isEqualTo(sessionId);
        assertThat(session.getUserId()).isEqualTo(userId);
        assertThat(session.getTenantId()).isEqualTo(tenantId);
        assertThat(session.getRole()).isEqualTo(role);
        assertThat(session.getSellerName()).isEqualTo(sellerName);
        assertThat(session.getUploadType()).isEqualTo(uploadType);
        assertThat(session.getCustomPath()).isEqualTo(customPath);
        assertThat(session.getFileName()).isEqualTo(fileName);
        assertThat(session.getFileSize()).isEqualTo(fileSize);
        assertThat(session.getMimeType()).isEqualTo(mimeType);
        assertThat(session.getS3Path()).isEqualTo(s3Path);
        assertThat(session.getStatus()).isEqualTo(status);
        assertThat(session.getCreatedAt()).isEqualTo(createdAt);
        assertThat(session.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(session.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(session.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    @DisplayName("reconstitute()는 검증 로직을 실행하지 않아야 한다")
    void shouldNotValidateWhenReconstituting() {
        // given - 검증을 통과하지 못하는 데이터 (파일 크기 초과)
        SessionId sessionId = SessionId.from("550e8400-e29b-41d4-a716-446655440000");
        FileSize oversizedFile = FileSize.of(UploadType.SINGLE.getMaxSize() + 1); // 검증 실패해야 할 크기
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2024, 1, 1, 10, 15, 0);
        S3Path s3Path = S3Path.from(
            UserRole.DEFAULT,
            1L,
            "seller1",
            "uploads",
            sessionId.value(),
            "image/jpeg"
        );

        // when & then - reconstitute()는 검증 없이 생성해야 함
        UploadSession session = UploadSession.reconstitute(
            sessionId,
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            oversizedFile, // 검증 실패해야 할 크기
            MimeType.of("image/jpeg"),
            s3Path,
            SessionStatus.ACTIVE,
            FIXED_CLOCK,
            createdAt,
            updatedAt,
            expiresAt,
            null
        );

        // then - 검증 없이 생성되었음을 확인
        assertThat(session.getFileSize()).isEqualTo(oversizedFile);
        assertThat(session.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("activate()로 세션을 활성화할 수 있어야 한다 (PREPARING → ACTIVE)")
    void shouldActivateSession() {
        // given
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.PREPARING,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME,
            FIXED_TIME.plusMinutes(15),
            null
        );
        LocalDateTime beforeUpdate = session.getUpdatedAt();

        // when
        session.activate();

        // then
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("complete()로 세션을 완료할 수 있어야 한다 (ACTIVE → COMPLETED)")
    void shouldCompleteSession() {
        // given
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.ACTIVE,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME,
            FIXED_TIME.plusMinutes(15),
            null
        );
        LocalDateTime beforeUpdate = session.getUpdatedAt();

        // when
        session.complete();

        // then
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getCompletedAt()).isNotNull();
        assertThat(session.getCompletedAt()).isAfterOrEqualTo(FIXED_TIME);
        assertThat(session.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("expire()로 세션을 만료시킬 수 있어야 한다 (ACTIVE → EXPIRED)")
    void shouldExpireSession() {
        // given
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.ACTIVE,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME,
            FIXED_TIME.plusMinutes(15),
            null
        );
        LocalDateTime beforeUpdate = session.getUpdatedAt();

        // when
        session.expire();

        // then
        assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
        assertThat(session.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("fail()로 세션을 실패시킬 수 있어야 한다 (ACTIVE → FAILED)")
    void shouldFailSession() {
        // given
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.ACTIVE,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME,
            FIXED_TIME.plusMinutes(15),
            null
        );
        LocalDateTime beforeUpdate = session.getUpdatedAt();

        // when
        session.fail();

        // then
        assertThat(session.getStatus()).isEqualTo(SessionStatus.FAILED);
        assertThat(session.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("유효하지 않은 상태 전환이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenInvalidTransition() {
        // given - COMPLETED 상태에서 ACTIVE로 전환 시도
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.COMPLETED,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME,
            FIXED_TIME.plusMinutes(15),
            FIXED_TIME
        );

        // when & then
        assertThatThrownBy(() -> session.activate())
            .isInstanceOf(InvalidSessionStatusException.class);
    }

    @Test
    @DisplayName("상태 전환 시 updatedAt이 자동으로 갱신되어야 한다")
    void shouldUpdateUpdatedAtOnStatusTransition() {
        // given
        UploadSession session = UploadSession.reconstitute(
            SessionId.from("550e8400-e29b-41d4-a716-446655440000"),
            1L, // userId
            1L, // tenantId
            UserRole.DEFAULT,
            "seller1",
            UploadType.SINGLE,
            "uploads",
            FileName.from("test.jpg"),
            FileSize.of(1024 * 1024),
            MimeType.of("image/jpeg"),
            S3Path.from(UserRole.DEFAULT, 1L, "seller1", "uploads", "550e8400-e29b-41d4-a716-446655440000", "image/jpeg"),
            SessionStatus.PREPARING,
            FIXED_CLOCK,
            FIXED_TIME,
            FIXED_TIME.minusHours(1), // 1시간 전
            FIXED_TIME.plusMinutes(15),
            null
        );
        LocalDateTime oldUpdatedAt = session.getUpdatedAt();

        // when
        session.activate();

        // then
        assertThat(session.getUpdatedAt()).isAfter(oldUpdatedAt);
        assertThat(session.getUpdatedAt()).isEqualTo(FIXED_TIME);
    }
}

