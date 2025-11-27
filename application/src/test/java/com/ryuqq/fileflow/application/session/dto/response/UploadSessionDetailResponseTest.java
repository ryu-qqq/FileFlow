package com.ryuqq.fileflow.application.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse.PartDetailResponse;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionDetailResponse 단위 테스트")
class UploadSessionDetailResponseTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String FILE_NAME = "document.pdf";
    private static final long FILE_SIZE = 1024 * 1024L;
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String BUCKET = "test-bucket";
    private static final String KEY = "uploads/document.pdf";

    @Nested
    @DisplayName("ofSingle 테스트")
    class OfSingleTest {

        @Test
        @DisplayName("단일 업로드 세션용 Response를 생성할 수 있다")
        void ofSingle_ShouldCreateSingleSessionResponse() {
            // given
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);
            LocalDateTime completedAt = LocalDateTime.now();
            String etag = "etag-123";

            // when
            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofSingle(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.COMPLETED,
                            BUCKET,
                            KEY,
                            etag,
                            createdAt,
                            expiresAt,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.fileName()).isEqualTo(FILE_NAME);
            assertThat(response.fileSize()).isEqualTo(FILE_SIZE);
            assertThat(response.contentType()).isEqualTo(CONTENT_TYPE);
            assertThat(response.uploadType()).isEqualTo("SINGLE");
            assertThat(response.status()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(response.bucket()).isEqualTo(BUCKET);
            assertThat(response.key()).isEqualTo(KEY);
            assertThat(response.etag()).isEqualTo(etag);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.completedAt()).isEqualTo(completedAt);

            assertThat(response.uploadId()).isNull();
            assertThat(response.totalParts()).isNull();
            assertThat(response.uploadedParts()).isNull();
            assertThat(response.parts()).isNull();
        }

        @Test
        @DisplayName("completedAt이 null인 경우도 생성할 수 있다")
        void ofSingle_WithNullCompletedAt_ShouldCreateResponse() {
            // given & when
            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofSingle(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.PREPARING,
                            BUCKET,
                            KEY,
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(24),
                            null);

            // then
            assertThat(response.etag()).isNull();
            assertThat(response.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("ofMultipart 테스트")
    class OfMultipartTest {

        @Test
        @DisplayName("Multipart 업로드 세션용 Response를 생성할 수 있다")
        void ofMultipart_ShouldCreateMultipartSessionResponse() {
            // given
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);
            LocalDateTime completedAt = LocalDateTime.now();
            String uploadId = "upload-id-123";
            String etag = "merged-etag-123";
            int totalParts = 5;
            int uploadedParts = 3;

            LocalDateTime partUploadedAt = LocalDateTime.now().minusMinutes(10);
            List<PartDetailResponse> parts =
                    List.of(
                            PartDetailResponse.of(1, "part-etag-1", 1024L, partUploadedAt),
                            PartDetailResponse.of(2, "part-etag-2", 2048L, partUploadedAt),
                            PartDetailResponse.of(3, "part-etag-3", 1024L, partUploadedAt));

            // when
            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofMultipart(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.COMPLETED,
                            BUCKET,
                            KEY,
                            uploadId,
                            totalParts,
                            uploadedParts,
                            parts,
                            etag,
                            createdAt,
                            expiresAt,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
            assertThat(response.uploadId()).isEqualTo(uploadId);
            assertThat(response.totalParts()).isEqualTo(totalParts);
            assertThat(response.uploadedParts()).isEqualTo(uploadedParts);
            assertThat(response.parts()).hasSize(3);
            assertThat(response.etag()).isEqualTo(etag);
        }

        @Test
        @DisplayName("진행 중인 Multipart 세션을 생성할 수 있다")
        void ofMultipart_InProgress_ShouldCreateResponse() {
            // given & when
            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofMultipart(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.ACTIVE,
                            BUCKET,
                            KEY,
                            "upload-id-123",
                            10,
                            3,
                            List.of(),
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(24),
                            null);

            // then
            assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(response.etag()).isNull();
            assertThat(response.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("PartDetailResponse 테스트")
    class PartDetailResponseTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void of_ShouldCreatePartDetailResponse() {
            // given
            int partNumber = 1;
            String etag = "part-etag-1";
            long size = 1024L;
            LocalDateTime uploadedAt = LocalDateTime.now();

            // when
            PartDetailResponse part = PartDetailResponse.of(partNumber, etag, size, uploadedAt);

            // then
            assertThat(part.partNumber()).isEqualTo(partNumber);
            assertThat(part.etag()).isEqualTo(etag);
            assertThat(part.size()).isEqualTo(size);
            assertThat(part.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("동일한 값으로 생성된 두 Part는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 27, 10, 0);
            PartDetailResponse part1 = PartDetailResponse.of(1, "etag", 1024L, uploadedAt);
            PartDetailResponse part2 = PartDetailResponse.of(1, "etag", 1024L, uploadedAt);

            // then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }
    }
}
