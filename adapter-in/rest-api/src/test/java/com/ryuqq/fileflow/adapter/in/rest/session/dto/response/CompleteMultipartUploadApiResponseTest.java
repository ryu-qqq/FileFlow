package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse.CompletedPartInfoApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompleteMultipartUploadApiResponse 단위 테스트")
class CompleteMultipartUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            LocalDateTime partUploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0);
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            List<CompletedPartInfoApiResponse> parts =
                    List.of(
                            CompletedPartInfoApiResponse.of(1, "etag1", 5_000_000L, partUploadedAt),
                            CompletedPartInfoApiResponse.of(
                                    2, "etag2", 3_000_000L, partUploadedAt));

            // when
            CompleteMultipartUploadApiResponse response =
                    new CompleteMultipartUploadApiResponse(
                            "session-mp",
                            "COMPLETED",
                            "bucket-name",
                            "multipart/key",
                            "upload-id-xyz",
                            2,
                            parts,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-mp");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.bucket()).isEqualTo("bucket-name");
            assertThat(response.key()).isEqualTo("multipart/key");
            assertThat(response.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(response.totalParts()).isEqualTo(2);
            assertThat(response.completedParts()).hasSize(2);
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            LocalDateTime completedAt = LocalDateTime.now().plusMinutes(30);
            List<CompletedPartInfoApiResponse> parts =
                    List.of(CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt));

            // when
            CompleteMultipartUploadApiResponse response =
                    CompleteMultipartUploadApiResponse.of(
                            "session-id",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "s3-upload-id",
                            1,
                            parts,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-id");
            assertThat(response.uploadId()).isEqualTo("s3-upload-id");
            assertThat(response.totalParts()).isEqualTo(1);
        }

        @Test
        @DisplayName("많은 Part로 응답을 생성할 수 있다")
        void create_WithManyParts_ShouldSucceed() {
            // given
            LocalDateTime now = LocalDateTime.now();
            List<CompletedPartInfoApiResponse> parts =
                    java.util.stream.IntStream.rangeClosed(1, 20)
                            .mapToObj(
                                    i ->
                                            CompletedPartInfoApiResponse.of(
                                                    i, "etag-" + i, 5_000_000L, now))
                            .toList();

            // when
            CompleteMultipartUploadApiResponse response =
                    CompleteMultipartUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "upload-id", 20, parts, now);

            // then
            assertThat(response.completedParts()).hasSize(20);
            assertThat(response.totalParts()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("CompletedPartInfoApiResponse 테스트")
    class CompletedPartInfoApiResponseTest {

        @Test
        @DisplayName("완료된 Part 정보를 생성할 수 있다")
        void create_CompletedPartInfo_ShouldSucceed() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 30);

            // when
            CompletedPartInfoApiResponse partInfo =
                    new CompletedPartInfoApiResponse(1, "part-etag", 5 * 1024 * 1024L, uploadedAt);

            // then
            assertThat(partInfo.partNumber()).isEqualTo(1);
            assertThat(partInfo.etag()).isEqualTo("part-etag");
            assertThat(partInfo.size()).isEqualTo(5 * 1024 * 1024L);
            assertThat(partInfo.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void of_ShouldCreatePartInfo() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();

            // when
            CompletedPartInfoApiResponse partInfo =
                    CompletedPartInfoApiResponse.of(3, "etag-3", 10_000_000L, uploadedAt);

            // then
            assertThat(partInfo.partNumber()).isEqualTo(3);
            assertThat(partInfo.etag()).isEqualTo("etag-3");
            assertThat(partInfo.size()).isEqualTo(10_000_000L);
            assertThat(partInfo.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("같은 값을 가진 Part 정보는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0);
            CompletedPartInfoApiResponse part1 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt);
            CompletedPartInfoApiResponse part2 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt);

            // when & then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }

        @Test
        @DisplayName("다른 Part 번호를 가진 Part 정보는 동등하지 않다")
        void equals_WithDifferentPartNumber_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            CompletedPartInfoApiResponse part1 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt);
            CompletedPartInfoApiResponse part2 =
                    CompletedPartInfoApiResponse.of(2, "etag", 5_000_000L, uploadedAt);

            // when & then
            assertThat(part1).isNotEqualTo(part2);
        }

        @Test
        @DisplayName("다른 크기를 가진 Part 정보는 동등하지 않다")
        void equals_WithDifferentSize_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            CompletedPartInfoApiResponse part1 =
                    CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt);
            CompletedPartInfoApiResponse part2 =
                    CompletedPartInfoApiResponse.of(1, "etag", 3_000_000L, uploadedAt);

            // when & then
            assertThat(part1).isNotEqualTo(part2);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0);
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            List<CompletedPartInfoApiResponse> parts =
                    List.of(CompletedPartInfoApiResponse.of(1, "etag", 5_000_000L, uploadedAt));

            CompleteMultipartUploadApiResponse response1 =
                    CompleteMultipartUploadApiResponse.of(
                            "session",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            1,
                            parts,
                            completedAt);
            CompleteMultipartUploadApiResponse response2 =
                    CompleteMultipartUploadApiResponse.of(
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
        @DisplayName("다른 uploadId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadId_ShouldNotBeEqual() {
            // given
            LocalDateTime now = LocalDateTime.now();
            List<CompletedPartInfoApiResponse> parts = List.of();

            CompleteMultipartUploadApiResponse response1 =
                    CompleteMultipartUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "upload-1", 0, parts, now);
            CompleteMultipartUploadApiResponse response2 =
                    CompleteMultipartUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "upload-2", 0, parts, now);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
