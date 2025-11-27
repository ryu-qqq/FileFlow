package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionDetailApiResponse.PartDetailApiResponse;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionDetailApiResponse 단위 테스트")
class UploadSessionDetailApiResponseTest {

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
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofSingle(
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
        @DisplayName("completedAt과 etag이 null인 경우도 생성할 수 있다")
        void ofSingle_WithNullCompletedAtAndEtag_ShouldCreateResponse() {
            // given & when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofSingle(
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
            List<PartDetailApiResponse> parts =
                    List.of(
                            PartDetailApiResponse.of(1, "part-etag-1", 1024L, partUploadedAt),
                            PartDetailApiResponse.of(2, "part-etag-2", 2048L, partUploadedAt),
                            PartDetailApiResponse.of(3, "part-etag-3", 1024L, partUploadedAt));

            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
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
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
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

        @Test
        @DisplayName("빈 Part 목록으로 생성할 수 있다")
        void ofMultipart_EmptyParts_ShouldCreateResponse() {
            // given & when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.PREPARING,
                            BUCKET,
                            KEY,
                            "upload-id",
                            5,
                            0,
                            List.of(),
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusHours(24),
                            null);

            // then
            assertThat(response.parts()).isEmpty();
            assertThat(response.uploadedParts()).isZero();
        }
    }

    @Nested
    @DisplayName("PartDetailApiResponse 테스트")
    class PartDetailApiResponseTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void of_ShouldCreatePartDetailApiResponse() {
            // given
            int partNumber = 1;
            String etag = "part-etag-1";
            long size = 1024L;
            LocalDateTime uploadedAt = LocalDateTime.now();

            // when
            PartDetailApiResponse part =
                    PartDetailApiResponse.of(partNumber, etag, size, uploadedAt);

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
            PartDetailApiResponse part1 = PartDetailApiResponse.of(1, "etag", 1024L, uploadedAt);
            PartDetailApiResponse part2 = PartDetailApiResponse.of(1, "etag", 1024L, uploadedAt);

            // then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }

        @Test
        @DisplayName("다른 partNumber를 가진 두 Part는 동등하지 않아야 한다")
        void equals_DifferentPartNumber_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            PartDetailApiResponse part1 = PartDetailApiResponse.of(1, "etag", 1024L, uploadedAt);
            PartDetailApiResponse part2 = PartDetailApiResponse.of(2, "etag", 1024L, uploadedAt);

            // then
            assertThat(part1).isNotEqualTo(part2);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 Response는 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 27, 10, 0);
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 28, 10, 0);
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 27, 11, 0);

            UploadSessionDetailApiResponse response1 =
                    UploadSessionDetailApiResponse.ofSingle(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.COMPLETED,
                            BUCKET,
                            KEY,
                            "etag",
                            createdAt,
                            expiresAt,
                            completedAt);
            UploadSessionDetailApiResponse response2 =
                    UploadSessionDetailApiResponse.ofSingle(
                            SESSION_ID,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            SessionStatus.COMPLETED,
                            BUCKET,
                            KEY,
                            "etag",
                            createdAt,
                            expiresAt,
                            completedAt);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }
}
