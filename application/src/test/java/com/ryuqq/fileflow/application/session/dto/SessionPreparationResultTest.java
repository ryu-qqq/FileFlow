package com.ryuqq.fileflow.application.session.dto;

import com.ryuqq.fileflow.domain.iam.vo.*;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SessionPreparationResult DTO 테스트
 * <p>
 * 세션 준비 결과를 담는 DTO로, 멱등성 구분을 위한 Factory Methods 제공
 * </p>
 */
class SessionPreparationResultTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
    }

    @Test
    @DisplayName("새 세션 결과를 생성해야 한다")
    void shouldCreateNewSessionResult() {
        // Given
        UploadSession session = createSession(clock);
        FileId fileId = FileId.generate();
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.ADMIN,
            "connectly",
            FileCategory.of("banner", UploaderType.ADMIN),
            fileId,
            FileName.of("example.jpg")
        );
        S3Bucket s3Bucket = S3Bucket.forTenant(TenantId.of(1L));

        // When
        SessionPreparationResult result = SessionPreparationResult.newSession(
            session,
            fileId,
            s3Key,
            s3Bucket
        );

        // Then
        assertThat(result.isExistingSession()).isFalse();
        assertThat(result.session()).isNotNull();
        assertThat(result.fileId()).isNotNull();
        assertThat(result.s3Key()).isNotNull();
        assertThat(result.s3Bucket()).isNotNull();
    }

    @Test
    @DisplayName("기존 세션 결과를 생성해야 한다")
    void shouldCreateExistingSessionResult() {
        // Given
        UploadSession session = createSession(clock);
        FileId fileId = FileId.generate();

        // When
        SessionPreparationResult result = SessionPreparationResult.existingSession(
            session,
            fileId,
            null,
            null
        );

        // Then
        assertThat(result.isExistingSession()).isTrue();
        assertThat(result.session()).isNotNull();
        assertThat(result.fileId()).isNotNull();
    }

    @Test
    @DisplayName("새 세션과 기존 세션을 구분할 수 있어야 한다")
    void shouldDistinguishNewAndExistingSessions() {
        // Given
        UploadSession session = createSession(clock);
        FileId fileId = FileId.generate();
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.ADMIN,
            "connectly",
            FileCategory.of("banner", UploaderType.ADMIN),
            fileId,
            FileName.of("example.jpg")
        );
        S3Bucket s3Bucket = S3Bucket.forTenant(TenantId.of(1L));

        // When
        SessionPreparationResult newResult = SessionPreparationResult.newSession(
            session, fileId, s3Key, s3Bucket
        );
        SessionPreparationResult existingResult = SessionPreparationResult.existingSession(
            session, fileId, null, null
        );

        // Then
        assertThat(newResult.isExistingSession()).isFalse();
        assertThat(existingResult.isExistingSession()).isTrue();
    }

    private UploadSession createSession(Clock clock) {
        return UploadSession.initiate(
            SessionId.generate(),
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );
    }
}
