package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse.PartInfoApiResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * InitMultipartUploadApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("InitMultipartUploadApiResponse 단위 테스트")
class InitMultipartUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String uploadId = "upload-456";
            int totalParts = 5;
            long partSize = 5242880L; // 5MB
            Instant expiresAt = Instant.now().plusSeconds(86400);
            String bucket = "fileflow-bucket";
            String key = "uploads/large-file.zip";
            List<PartInfoApiResponse> parts =
                    List.of(
                            PartInfoApiResponse.of(1, "https://url1"),
                            PartInfoApiResponse.of(2, "https://url2"),
                            PartInfoApiResponse.of(3, "https://url3"),
                            PartInfoApiResponse.of(4, "https://url4"),
                            PartInfoApiResponse.of(5, "https://url5"));

            // when
            InitMultipartUploadApiResponse response =
                    new InitMultipartUploadApiResponse(
                            sessionId,
                            uploadId,
                            totalParts,
                            partSize,
                            expiresAt,
                            bucket,
                            key,
                            parts);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.uploadId()).isEqualTo(uploadId);
            assertThat(response.totalParts()).isEqualTo(totalParts);
            assertThat(response.partSize()).isEqualTo(partSize);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.parts()).hasSize(5);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            List<PartInfoApiResponse> parts =
                    List.of(
                            PartInfoApiResponse.of(1, "https://url1"),
                            PartInfoApiResponse.of(2, "https://url2"));

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            2,
                            5242880L,
                            Instant.now().plusSeconds(86400),
                            "bucket",
                            "key",
                            parts);

            // then
            assertThat(response.sessionId()).isEqualTo("session");
            assertThat(response.uploadId()).isEqualTo("upload-id");
            assertThat(response.totalParts()).isEqualTo(2);
            assertThat(response.parts()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("PartInfoApiResponse 테스트")
    class PartInfoApiResponseTest {

        @Test
        @DisplayName("Part 정보를 생성할 수 있다")
        void create_PartInfo_ShouldSucceed() {
            // given
            int partNumber = 1;
            String presignedUrl = "https://s3.amazonaws.com/bucket/key?partNumber=1";

            // when
            PartInfoApiResponse partInfo = new PartInfoApiResponse(partNumber, presignedUrl);

            // then
            assertThat(partInfo.partNumber()).isEqualTo(partNumber);
            assertThat(partInfo.presignedUrl()).isEqualTo(presignedUrl);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 Part 정보를 생성할 수 있다")
        void create_PartInfo_WithOfMethod_ShouldSucceed() {
            // when
            PartInfoApiResponse partInfo = PartInfoApiResponse.of(3, "https://presigned");

            // then
            assertThat(partInfo.partNumber()).isEqualTo(3);
            assertThat(partInfo.presignedUrl()).isEqualTo("https://presigned");
        }

        @Test
        @DisplayName("Part 정보의 동등성을 확인할 수 있다")
        void equals_PartInfo_ShouldWork() {
            // given
            PartInfoApiResponse part1 = PartInfoApiResponse.of(1, "url");
            PartInfoApiResponse part2 = PartInfoApiResponse.of(1, "url");

            // then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }
    }

    @Nested
    @DisplayName("Part 개수 테스트")
    class TotalPartsTest {

        @Test
        @DisplayName("최소 Part 개수(1)로 응답을 생성할 수 있다")
        void create_WithMinimumParts_ShouldSucceed() {
            // given
            List<PartInfoApiResponse> parts = List.of(PartInfoApiResponse.of(1, "url"));

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            1,
                            5242880L,
                            Instant.now().plusSeconds(86400),
                            "bucket",
                            "key",
                            parts);

            // then
            assertThat(response.totalParts()).isEqualTo(1);
            assertThat(response.parts()).hasSize(1);
        }

        @Test
        @DisplayName("대용량 파일을 위한 많은 Part로 응답을 생성할 수 있다")
        void create_WithManyParts_ShouldSucceed() {
            // given - 5GB file / 5MB part = 1000 parts
            int totalParts = 1000;
            List<PartInfoApiResponse> parts =
                    java.util.stream.IntStream.rangeClosed(1, totalParts)
                            .mapToObj(i -> PartInfoApiResponse.of(i, "https://url" + i))
                            .toList();

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            totalParts,
                            5242880L,
                            Instant.now().plusSeconds(86400),
                            "bucket",
                            "key",
                            parts);

            // then
            assertThat(response.totalParts()).isEqualTo(1000);
            assertThat(response.parts()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("Part 크기 테스트")
    class PartSizeTest {

        @Test
        @DisplayName("5MB Part 크기로 응답을 생성할 수 있다")
        void create_With5MBPartSize_ShouldSucceed() {
            // given
            long partSize = 5 * 1024 * 1024L; // 5MB

            // when
            InitMultipartUploadApiResponse response = createResponse(partSize);

            // then
            assertThat(response.partSize()).isEqualTo(5242880L);
        }

        @Test
        @DisplayName("최대 Part 크기(5GB)로 응답을 생성할 수 있다")
        void create_WithMaxPartSize_ShouldSucceed() {
            // given
            long maxPartSize = 5L * 1024 * 1024 * 1024; // 5GB

            // when
            InitMultipartUploadApiResponse response = createResponse(maxPartSize);

            // then
            assertThat(response.partSize()).isEqualTo(maxPartSize);
        }
    }

    @Nested
    @DisplayName("만료 시각 테스트")
    class ExpiresAtTest {

        @Test
        @DisplayName("24시간 후 만료 시각으로 응답을 생성할 수 있다")
        void create_With24HoursExpiry_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(86400); // 24시간

            // when
            InitMultipartUploadApiResponse response =
                    InitMultipartUploadApiResponse.of(
                            "session",
                            "upload-id",
                            2,
                            5242880L,
                            expiresAt,
                            "bucket",
                            "key",
                            List.of());

            // then
            assertThat(response.expiresAt()).isAfter(now);
            assertThat(response.expiresAt()).isBefore(now.plusSeconds(86401));
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant expiresAt = Instant.parse("2025-11-27T10:00:00Z");
            List<PartInfoApiResponse> parts = List.of(PartInfoApiResponse.of(1, "url"));

            InitMultipartUploadApiResponse response1 =
                    new InitMultipartUploadApiResponse(
                            "session", "upload-id", 1, 5242880L, expiresAt, "bucket", "key", parts);
            InitMultipartUploadApiResponse response2 =
                    new InitMultipartUploadApiResponse(
                            "session", "upload-id", 1, 5242880L, expiresAt, "bucket", "key", parts);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 uploadId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadId_ShouldNotBeEqual() {
            // given
            InitMultipartUploadApiResponse response1 = createResponseWithUploadId("upload-1");
            InitMultipartUploadApiResponse response2 = createResponseWithUploadId("upload-2");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }

    private InitMultipartUploadApiResponse createResponse(long partSize) {
        return InitMultipartUploadApiResponse.of(
                "session",
                "upload-id",
                2,
                partSize,
                Instant.now().plusSeconds(86400),
                "bucket",
                "key",
                List.of(PartInfoApiResponse.of(1, "url1"), PartInfoApiResponse.of(2, "url2")));
    }

    private InitMultipartUploadApiResponse createResponseWithUploadId(String uploadId) {
        return InitMultipartUploadApiResponse.of(
                "session",
                uploadId,
                1,
                5242880L,
                Instant.now().plusSeconds(86400),
                "bucket",
                "key",
                List.of(PartInfoApiResponse.of(1, "url")));
    }
}
