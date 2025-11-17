package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadSession Aggregate 테스트
 */
class UploadSessionTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2025-01-15T12:00:00Z"),
            ZoneId.systemDefault()
    );

    @Test
    @DisplayName("SINGLE 업로드 타입으로 UploadSession을 생성해야 한다")
    void shouldCreateUploadSessionWithSingleUploadType() {
        // given
        SessionId sessionId = SessionId.generate();
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L); // 50MB
        MimeType mimeType = MimeType.of("image/jpeg");

        // when
        UploadSession session = UploadSession.create(
                sessionId,
                fileName,
                fileSize,
                mimeType,
                FIXED_CLOCK
        );

        // then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.fileSize()).isEqualTo(fileSize);
        assertThat(session.mimeType()).isEqualTo(mimeType);
        assertThat(session.uploadType()).isEqualTo(UploadType.SINGLE);
        assertThat(session.status()).isEqualTo(SessionStatus.INITIATED);
        assertThat(session.multipartUpload()).isEmpty();
        assertThat(session.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("MULTIPART 업로드 타입으로 UploadSession을 생성해야 한다")
    void shouldCreateUploadSessionWithMultipartUploadType() {
        // given
        SessionId sessionId = SessionId.generate();
        FileName fileName = FileName.of("large-file.zip");
        FileSize fileSize = FileSize.of(200 * 1024 * 1024L); // 200MB
        MimeType mimeType = MimeType.of("application/zip");

        // when
        UploadSession session = UploadSession.create(
                sessionId,
                fileName,
                fileSize,
                mimeType,
                FIXED_CLOCK
        );

        // then
        assertThat(session.uploadType()).isEqualTo(UploadType.MULTIPART);
        assertThat(session.multipartUpload()).isEmpty(); // 아직 Initiate 안함
    }

    @Test
    @DisplayName("IN_PROGRESS 상태로 전환할 수 있어야 한다")
    void shouldMarkAsInProgress() {
        // given
        UploadSession session = createDefaultSession();

        // when
        UploadSession updated = session.markAsInProgress();

        // then
        assertThat(updated.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(session.status()).isEqualTo(SessionStatus.INITIATED); // 원본 불변
    }

    @Test
    @DisplayName("COMPLETED 상태로 전환하고 ETag를 저장해야 한다")
    void shouldMarkAsCompletedWithETag() {
        // given
        UploadSession session = createDefaultSession().markAsInProgress();
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");

        // when
        UploadSession completed = session.markAsCompleted(etag);

        // then
        assertThat(completed.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(completed.etag()).isEqualTo(etag);
    }

    @Test
    @DisplayName("세션 만료 여부를 확인할 수 있어야 한다")
    void shouldCheckIfExpired() {
        // given
        UploadSession session = createDefaultSession();
        Clock futureTime = Clock.fixed(
                Instant.parse("2025-01-15T12:06:00Z"), // 6분 후
                ZoneId.systemDefault()
        );

        // when
        boolean expired = session.isExpired(futureTime);

        // then
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("멀티파트 업로드를 초기화할 수 있어야 한다")
    void shouldInitiateMultipartUpload() {
        // given
        UploadSession session = createLargeFileSession(); // MULTIPART
        MultipartUploadId uploadId = MultipartUploadId.of("test-multipart-upload-id");
        int totalParts = 4;

        // when
        UploadSession initiated = session.initiateMultipartUpload(uploadId, totalParts, FIXED_CLOCK);

        // then
        assertThat(initiated.multipartUpload()).isPresent();
        assertThat(initiated.multipartUpload().get().uploadId()).isEqualTo(uploadId);
        assertThat(initiated.multipartUpload().get().totalParts()).isEqualTo(totalParts);
    }

    @Test
    @DisplayName("멀티파트 파트를 추가할 수 있어야 한다")
    void shouldAddUploadedPart() {
        // given
        UploadSession session = createLargeFileSession()
                .initiateMultipartUpload(
                        MultipartUploadId.of("test-upload-id"),
                        2,
                        FIXED_CLOCK
                );
        UploadedPart part = UploadedPart.of(1, ETag.of("etag1"), 5242880L);

        // when
        UploadSession updated = session.addUploadedPart(part);

        // then
        assertThat(updated.multipartUpload()).isPresent();
        assertThat(updated.multipartUpload().get().uploadedParts()).hasSize(1);
    }

    // Helper Methods

    private UploadSession createDefaultSession() {
        return UploadSession.create(
                SessionId.generate(),
                FileName.of("test.jpg"),
                FileSize.of(50 * 1024 * 1024L), // 50MB (SINGLE)
                MimeType.of("image/jpeg"),
                FIXED_CLOCK
        );
    }

    private UploadSession createLargeFileSession() {
        return UploadSession.create(
                SessionId.generate(),
                FileName.of("large-file.zip"),
                FileSize.of(200 * 1024 * 1024L), // 200MB (MULTIPART)
                MimeType.of("application/zip"),
                FIXED_CLOCK
        );
    }
}
