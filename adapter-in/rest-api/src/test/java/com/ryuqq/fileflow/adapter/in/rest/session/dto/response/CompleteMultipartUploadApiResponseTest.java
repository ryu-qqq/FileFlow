package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse.CompletedPartInfoApiResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CompleteMultipartUploadApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CompleteMultipartUploadApiResponse 단위 테스트")
class CompleteMultipartUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String status = "COMPLETED";
            String bucket = "fileflow-bucket";
            String key = "uploads/large-file.zip";
            String uploadId = "upload-456";
            int totalParts = 5;
            Instant completedAt = Instant.now();
            List<CompletedPartInfoApiResponse> completedParts =
                    List.of(
                            CompletedPartInfoApiResponse.of(
                                    1, "\"etag1\"", 5242880L, completedAt.minusSeconds(300)),
                            CompletedPartInfoApiResponse.of(
                                    2, "\"etag2\"", 5242880L, completedAt.minusSeconds(200)),
                            CompletedPartInfoApiResponse.of(
                                    3, "\"etag3\"", 5242880L, completedAt.minusSeconds(100)));

            // when
            CompleteMultipartUploadApiResponse response =
                    new CompleteMultipartUploadApiResponse(
                            sessionId,
                            status,
                            bucket,
                            key,
                            uploadId,
                            totalParts,
                            completedParts,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.uploadId()).isEqualTo(uploadId);
            assertThat(response.totalParts()).isEqualTo(totalParts);
            assertThat(response.completedParts()).hasSize(3);
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            Instant completedAt = Instant.now();
            List<CompletedPartInfoApiResponse> completedParts =
                    List.of(CompletedPartInfoApiResponse.of(1, "\"etag\"", 5242880L, completedAt));

            // when
            CompleteMultipartUploadApiResponse response =
                    CompleteMultipartUploadApiResponse.of(
                            "session-789",
                            "COMPLETED",
                            "my-bucket",
                            "path/to/file.zip",
                            "upload-xyz",
                            1,
                            completedParts,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-789");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.uploadId()).isEqualTo("upload-xyz");
            assertThat(response.totalParts()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("CompletedPartInfoApiResponse 테스트")
    class CompletedPartInfoApiResponseTest {

        @Test
        @DisplayName("Part 정보를 생성할 수 있다")
        void create_PartInfo_ShouldSucceed() {
            // given
            int partNumber = 1;
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            long size = 5242880L;
            Instant uploadedAt = Instant.now();

            // when
            CompletedPartInfoApiResponse partInfo =
                    new CompletedPartInfoApiResponse(partNumber, etag, size, uploadedAt);

            // then
            assertThat(partInfo.partNumber()).isEqualTo(partNumber);
            assertThat(partInfo.etag()).isEqualTo(etag);
            assertThat(partInfo.size()).isEqualTo(size);
            assertThat(partInfo.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void create_PartInfo_WithOfMethod_ShouldSucceed() {
            // given
            Instant uploadedAt = Instant.now();

            // when
            CompletedPartInfoApiResponse partInfo =
                    CompletedPartInfoApiResponse.of(3, "\"etag-value\"", 10485760L, uploadedAt);

            // then
            assertThat(partInfo.partNumber()).isEqualTo(3);
            assertThat(partInfo.etag()).isEqualTo("\"etag-value\"");
            assertThat(partInfo.size()).isEqualTo(10485760L);
            assertThat(partInfo.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("Part 정보의 동등성을 확인할 수 있다")
        void equals_PartInfo_ShouldWork() {
            // given
            Instant uploadedAt = Instant.parse("2025-11-26T10:00:00Z");
            CompletedPartInfoApiResponse part1 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5242880L, uploadedAt);
            CompletedPartInfoApiResponse part2 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5242880L, uploadedAt);

            // then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }
    }

    @Nested
    @DisplayName("Part 개수 테스트")
    class TotalPartsTest {

        @Test
        @DisplayName("빈 Part 목록으로 응답을 생성할 수 있다")
        void create_WithEmptyParts_ShouldSucceed() {
            // when
            CompleteMultipartUploadApiResponse response =
                    CompleteMultipartUploadApiResponse.of(
                            "session",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            0,
                            List.of(),
                            Instant.now());

            // then
            assertThat(response.completedParts()).isEmpty();
            assertThat(response.totalParts()).isZero();
        }

        @Test
        @DisplayName("많은 Part로 응답을 생성할 수 있다")
        void create_WithManyParts_ShouldSucceed() {
            // given - 100 parts
            Instant now = Instant.now();
            List<CompletedPartInfoApiResponse> parts =
                    java.util.stream.IntStream.rangeClosed(1, 100)
                            .mapToObj(
                                    i ->
                                            CompletedPartInfoApiResponse.of(
                                                    i,
                                                    "\"etag" + i + "\"",
                                                    5242880L,
                                                    now.minusSeconds(100 - i)))
                            .toList();

            // when
            CompleteMultipartUploadApiResponse response =
                    CompleteMultipartUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "upload-id", 100, parts, now);

            // then
            assertThat(response.totalParts()).isEqualTo(100);
            assertThat(response.completedParts()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant completedAt = Instant.parse("2025-11-26T10:00:00Z");
            List<CompletedPartInfoApiResponse> parts =
                    List.of(CompletedPartInfoApiResponse.of(1, "etag", 5242880L, completedAt));

            CompleteMultipartUploadApiResponse response1 =
                    new CompleteMultipartUploadApiResponse(
                            "session",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            1,
                            parts,
                            completedAt);
            CompleteMultipartUploadApiResponse response2 =
                    new CompleteMultipartUploadApiResponse(
                            "session",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            1,
                            parts,
                            completedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            Instant completedAt = Instant.now();
            CompleteMultipartUploadApiResponse response1 = createResponse("session-1");
            CompleteMultipartUploadApiResponse response2 = createResponse("session-2");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 uploadId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadId_ShouldNotBeEqual() {
            // given
            Instant completedAt = Instant.now();
            CompleteMultipartUploadApiResponse response1 =
                    createResponseWithUploadId("upload-1", completedAt);
            CompleteMultipartUploadApiResponse response2 =
                    createResponseWithUploadId("upload-2", completedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }

    private CompleteMultipartUploadApiResponse createResponse(String sessionId) {
        return CompleteMultipartUploadApiResponse.of(
                sessionId, "COMPLETED", "bucket", "key", "upload-id", 1, List.of(), Instant.now());
    }

    private CompleteMultipartUploadApiResponse createResponseWithUploadId(
            String uploadId, Instant completedAt) {
        return CompleteMultipartUploadApiResponse.of(
                "session", "COMPLETED", "bucket", "key", uploadId, 1, List.of(), completedAt);
    }
}
