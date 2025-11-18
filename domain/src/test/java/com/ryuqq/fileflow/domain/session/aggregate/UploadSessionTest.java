package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
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
 * UploadSession Aggregate 테스트 (가변 패턴)
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
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L); // 50MB
        MimeType mimeType = MimeType.of("image/jpeg");

        // when
        UploadSession session = UploadSession.forNew(
                fileName,
                fileSize,
                mimeType,
                FIXED_CLOCK
        );

        // then
        assertThat(session.sessionId()).isNotNull();
        assertThat(session.sessionId().isNew()).isTrue();
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
        FileName fileName = FileName.of("large-file.pdf");
        FileSize fileSize = FileSize.of(200 * 1024 * 1024L); // 200MB
        MimeType mimeType = MimeType.of("application/pdf");

        // when
        UploadSession session = UploadSession.forNew(
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
    @DisplayName("IN_PROGRESS 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldUpdateToInProgress() {
        // given
        UploadSession session = createDefaultSession();
        SessionStatus before = session.status();

        // when
        session.updateToInProgress();

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(before).isEqualTo(SessionStatus.INITIATED); // 이전 상태 확인
    }

    @Test
    @DisplayName("COMPLETED 상태로 전환하고 ETag를 저장해야 한다 (가변 패턴)")
    void shouldCompleteWithETag() {
        // given
        UploadSession session = createDefaultSession();
        session.updateToInProgress();
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");

        // when
        session.completeWithETag(etag);

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.etag()).isEqualTo(etag);
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
    @DisplayName("멀티파트 업로드를 초기화할 수 있어야 한다 (가변 패턴)")
    void shouldInitiateMultipartUpload() {
        // given
        UploadSession session = createLargeFileSession(); // MULTIPART
        MultipartUploadId uploadId = MultipartUploadId.of("test-multipart-upload-id");
        int totalParts = 4;

        // when
        session.initiateMultipartUpload(uploadId, totalParts);

        // then
        assertThat(session.multipartUpload()).isPresent();
        assertThat(session.multipartUpload().get().uploadId()).isEqualTo(uploadId);
        assertThat(session.multipartUpload().get().totalParts()).isEqualTo(totalParts);
    }

    @Test
    @DisplayName("멀티파트 파트를 추가할 수 있어야 한다 (가변 패턴)")
    void shouldAddUploadedPart() {
        // given
        UploadSession session = createLargeFileSession();
        session.initiateMultipartUpload(
                MultipartUploadId.of("test-upload-id"),
                2
        );
        UploadedPart part = UploadedPart.of(1, ETag.of("etag1"), 5242880L);

        // when
        session.addUploadedPart(part);

        // then
        assertThat(session.multipartUpload()).isPresent();
        assertThat(session.multipartUpload().get().uploadedParts()).hasSize(1);
    }

    @Test
    @DisplayName("EXPIRED 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldUpdateToExpired() {
        // given
        UploadSession session = createDefaultSession();

        // when
        session.updateToExpired();

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.EXPIRED);
        assertThat(session.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("FAILED 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldFailWithReason() {
        // given
        UploadSession session = createDefaultSession();
        String failureReason = "S3 upload timeout";

        // when
        session.fail(failureReason);

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.FAILED);
        assertThat(session.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("체크섬이 없으면 검증을 Skip해야 한다")
    void shouldSkipValidationWhenChecksumIsNull() {
        // given
        UploadSession session = createDefaultSession(); // checksum null
        ETag s3Etag = ETag.of("test-etag");

        // when
        boolean result = session.validateChecksum(s3Etag);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("of() 메서드로 기존 ID를 가진 UploadSession을 생성할 수 있어야 한다")
    void shouldCreateUploadSessionUsingOfMethod() {
        // given
        SessionId sessionId = SessionId.of("01234567-89ab-7def-0123-456789abcdef"); // 기존 ID (not new)
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("image/jpeg");
        UploadType uploadType = UploadType.SINGLE;
        SessionStatus status = SessionStatus.INITIATED;
        LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime updatedAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime expiresAt = createdAt.plusMinutes(5);

        // when
        UploadSession session = UploadSession.of(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                null, null, null, null, expiresAt, status, FIXED_CLOCK, createdAt, updatedAt
        );

        // then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.status()).isEqualTo(status);
    }

    @Test
    @DisplayName("of() 메서드는 null ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenOfMethodWithNullId() {
        // given
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("image/jpeg");

        // when & then
        assertThatThrownBy(() -> UploadSession.of(
                null, tenantId, fileName, fileSize, mimeType, UploadType.SINGLE,
                null, null, null, null, null, SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("of() 메서드는 새로운 ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenOfMethodWithNewId() {
        // given
        SessionId newId = SessionId.forNew(); // isNew() == true
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("image/jpeg");

        // when & then
        assertThatThrownBy(() -> UploadSession.of(
                newId, tenantId, fileName, fileSize, mimeType, UploadType.SINGLE,
                null, null, null, null, null, SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute() 메서드로 영속성 복원을 할 수 있어야 한다")
    void shouldReconstituteUploadSessionFromPersistence() {
        // given
        SessionId sessionId = SessionId.of("abcdef01-2345-6789-abcd-ef0123456789");
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("restored.pdf");
        FileSize fileSize = FileSize.of(100 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        UploadType uploadType = UploadType.SINGLE;
        SessionStatus status = SessionStatus.COMPLETED;
        ETag etag = ETag.of("abc123def456");
        LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK).minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime expiresAt = createdAt.plusMinutes(5);

        // when
        UploadSession session = UploadSession.reconstitute(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                null, null, etag, null, expiresAt, status, FIXED_CLOCK, createdAt, updatedAt
        );

        // then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.etag()).isEqualTo(etag);
        assertThat(session.createdAt()).isEqualTo(createdAt);
        assertThat(session.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("reconstitute() 메서드는 null ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenReconstituteWithNullId() {
        // given
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("image/jpeg");

        // when & then
        assertThatThrownBy(() -> UploadSession.reconstitute(
                null, tenantId, fileName, fileSize, mimeType, UploadType.SINGLE,
                null, null, null, null, null, SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute() 메서드는 새로운 ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenReconstituteWithNewId() {
        // given
        SessionId newId = SessionId.forNew();
        TenantId tenantId = TenantId.of(1L);
        FileName fileName = FileName.of("test.jpg");
        FileSize fileSize = FileSize.of(50 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("image/jpeg");

        // when & then
        assertThatThrownBy(() -> UploadSession.reconstitute(
                newId, tenantId, fileName, fileSize, mimeType, UploadType.SINGLE,
                null, null, null, null, null, SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");
    }

    // Helper Methods

    private UploadSession createDefaultSession() {
        return UploadSession.forNew(
                FileName.of("test.jpg"),
                FileSize.of(50 * 1024 * 1024L), // 50MB (SINGLE)
                MimeType.of("image/jpeg"),
                FIXED_CLOCK
        );
    }

    private UploadSession createLargeFileSession() {
        return UploadSession.forNew(
                FileName.of("large-file.pdf"),
                FileSize.of(200 * 1024 * 1024L), // 200MB (MULTIPART)
                MimeType.of("application/pdf"),
                FIXED_CLOCK
        );
    }
}
