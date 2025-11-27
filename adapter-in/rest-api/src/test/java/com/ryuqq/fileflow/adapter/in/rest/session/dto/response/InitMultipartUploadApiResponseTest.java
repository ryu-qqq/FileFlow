package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse.PartInfoApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InitMultipartUploadApiResponse 단위 테스트")
class InitMultipartUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 27, 12, 0);
            List<PartInfoApiResponse> parts =
                    List.of(
                            PartInfoApiResponse.of(1, "https://part1-url"),
                            PartInfoApiResponse.of(2, "https://part2-url"),
                            PartInfoApiResponse.of(3, "https://part3-url"));

            // when
            InitMultipartUploadApiResponse response =
                    new InitMultipartUploadApiResponse(
                            "session-mp",
                            "upload-id-xyz",
                            3,
                            5 * 1024 * 1024L,
                            expiresAt,
                            "bucket-name",
                            "multipart/key",
                            parts);

            // then
            assertThat(response.sessionId()).isEqualTo("session-mp");
            assertThat(response.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(response.totalParts()).isEqualTo(3);
            assertThat(response.partSize()).isEqualTo(5 * 1024 * 1024L);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo("bucket-name");
            assertThat(response.key()).isEqualTo("multipart/key");
            assertThat(response.parts()).hasSize(3);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            List<PartInfoApiResponse> parts =
                    List.of(PartInfoApiResponse.of(1, "url1"), PartInfoApiResponse.of(2, "url2"));

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session-id",
                            "s3-upload-id",
                            2,
                            10 * 1024 * 1024L,
                            expiresAt,
                            "bucket",
                            "key",
                            parts);

            // then
            assertThat(response.sessionId()).isEqualTo("session-id");
            assertThat(response.uploadId()).isEqualTo("s3-upload-id");
            assertThat(response.totalParts()).isEqualTo(2);
        }

        @Test
        @DisplayName("많은 Part로 응답을 생성할 수 있다")
        void create_WithManyParts_ShouldSucceed() {
            // given - S3는 최대 10,000 parts 지원
            List<PartInfoApiResponse> parts =
                    java.util.stream.IntStream.rangeClosed(1, 100)
                            .mapToObj(i -> PartInfoApiResponse.of(i, "https://url-" + i))
                            .toList();

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            100,
                            5_000_000L,
                            LocalDateTime.now(),
                            "bucket",
                            "key",
                            parts);

            // then
            assertThat(response.parts()).hasSize(100);
            assertThat(response.totalParts()).isEqualTo(100);
        }

        @Test
        @DisplayName("빈 Part 목록으로도 응답을 생성할 수 있다")
        void create_WithEmptyParts_ShouldSucceed() {
            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            0,
                            5_000_000L,
                            LocalDateTime.now(),
                            "bucket",
                            "key",
                            List.of());

            // then
            assertThat(response.parts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PartInfoApiResponse 테스트")
    class PartInfoApiResponseTest {

        @Test
        @DisplayName("Part 정보를 생성할 수 있다")
        void create_PartInfo_ShouldSucceed() {
            // when
            PartInfoApiResponse partInfo = new PartInfoApiResponse(1, "https://presigned-url");

            // then
            assertThat(partInfo.partNumber()).isEqualTo(1);
            assertThat(partInfo.presignedUrl()).isEqualTo("https://presigned-url");
        }

        @Test
        @DisplayName("of 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void of_ShouldCreatePartInfo() {
            // when
            PartInfoApiResponse partInfo = PartInfoApiResponse.of(5, "https://url-part-5");

            // then
            assertThat(partInfo.partNumber()).isEqualTo(5);
            assertThat(partInfo.presignedUrl()).isEqualTo("https://url-part-5");
        }

        @Test
        @DisplayName("같은 값을 가진 Part 정보는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            PartInfoApiResponse part1 = PartInfoApiResponse.of(1, "url");
            PartInfoApiResponse part2 = PartInfoApiResponse.of(1, "url");

            // when & then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }

        @Test
        @DisplayName("다른 Part 번호를 가진 Part 정보는 동등하지 않다")
        void equals_WithDifferentPartNumber_ShouldNotBeEqual() {
            // given
            PartInfoApiResponse part1 = PartInfoApiResponse.of(1, "url");
            PartInfoApiResponse part2 = PartInfoApiResponse.of(2, "url");

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
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            List<PartInfoApiResponse> parts = List.of(PartInfoApiResponse.of(1, "url"));

            InitMultipartUploadApiResponse response1 =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            1,
                            5_000_000L,
                            expiresAt,
                            "bucket",
                            "key",
                            parts);
            InitMultipartUploadApiResponse response2 =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            1,
                            5_000_000L,
                            expiresAt,
                            "bucket",
                            "key",
                            parts);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 uploadId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadId_ShouldNotBeEqual() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now();

            InitMultipartUploadApiResponse response1 =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-1",
                            1,
                            5_000_000L,
                            expiresAt,
                            "bucket",
                            "key",
                            List.of());
            InitMultipartUploadApiResponse response2 =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-2",
                            1,
                            5_000_000L,
                            expiresAt,
                            "bucket",
                            "key",
                            List.of());

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
