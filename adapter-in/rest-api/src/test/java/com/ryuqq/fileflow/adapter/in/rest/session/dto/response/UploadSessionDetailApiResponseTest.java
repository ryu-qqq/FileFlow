package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionDetailApiResponse.PartDetailApiResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * UploadSessionDetailApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UploadSessionDetailApiResponse 단위 테스트")
class UploadSessionDetailApiResponseTest {

    @Nested
    @DisplayName("단일 업로드 생성 테스트")
    class SingleUploadCreateTest {

        @Test
        @DisplayName("ofSingle 팩토리 메서드로 단일 업로드 응답을 생성할 수 있다")
        void create_WithOfSingleMethod_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String fileName = "image.jpg";
            long fileSize = 1024000L;
            String contentType = "image/jpeg";
            String status = "COMPLETED";
            String bucket = "fileflow-bucket";
            String key = "uploads/image.jpg";
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(900);
            Instant completedAt = createdAt.plusSeconds(60);

            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofSingle(
                            sessionId,
                            fileName,
                            fileSize,
                            contentType,
                            status,
                            bucket,
                            key,
                            etag,
                            createdAt,
                            expiresAt,
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.fileName()).isEqualTo(fileName);
            assertThat(response.fileSize()).isEqualTo(fileSize);
            assertThat(response.contentType()).isEqualTo(contentType);
            assertThat(response.uploadType()).isEqualTo("SINGLE");
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.etag()).isEqualTo(etag);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("단일 업로드 응답은 Multipart 관련 필드가 null이다")
        void create_SingleUpload_ShouldHaveNullMultipartFields() {
            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofSingle(
                            "session",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "\"etag\"",
                            Instant.now(),
                            Instant.now().plusSeconds(900),
                            Instant.now());

            // then
            assertThat(response.uploadId()).isNull();
            assertThat(response.totalParts()).isNull();
            assertThat(response.uploadedParts()).isNull();
            assertThat(response.parts()).isNull();
        }
    }

    @Nested
    @DisplayName("Multipart 업로드 생성 테스트")
    class MultipartUploadCreateTest {

        @Test
        @DisplayName("ofMultipart 팩토리 메서드로 Multipart 업로드 응답을 생성할 수 있다")
        void create_WithOfMultipartMethod_ShouldSucceed() {
            // given
            String sessionId = "session-456";
            String fileName = "large-file.zip";
            long fileSize = 104857600L; // 100MB
            String contentType = "application/zip";
            String status = "IN_PROGRESS";
            String bucket = "fileflow-bucket";
            String key = "uploads/large-file.zip";
            String uploadId = "upload-xyz";
            int totalParts = 20;
            int uploadedParts = 5;
            Instant now = Instant.now();
            List<PartDetailApiResponse> parts =
                    List.of(
                            PartDetailApiResponse.of(
                                    1, "\"etag1\"", 5242880L, now.minusSeconds(300)),
                            PartDetailApiResponse.of(
                                    2, "\"etag2\"", 5242880L, now.minusSeconds(200)));
            Instant createdAt = now.minusSeconds(600);
            Instant expiresAt = now.plusSeconds(86400);

            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
                            sessionId,
                            fileName,
                            fileSize,
                            contentType,
                            status,
                            bucket,
                            key,
                            uploadId,
                            totalParts,
                            uploadedParts,
                            parts,
                            null,
                            createdAt,
                            expiresAt,
                            null);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
            assertThat(response.uploadId()).isEqualTo(uploadId);
            assertThat(response.totalParts()).isEqualTo(totalParts);
            assertThat(response.uploadedParts()).isEqualTo(uploadedParts);
            assertThat(response.parts()).hasSize(2);
            assertThat(response.etag()).isNull();
            assertThat(response.completedAt()).isNull();
        }

        @Test
        @DisplayName("완료된 Multipart 업로드 응답을 생성할 수 있다")
        void create_CompletedMultipart_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            List<PartDetailApiResponse> parts =
                    List.of(
                            PartDetailApiResponse.of(
                                    1, "\"etag1\"", 5242880L, now.minusSeconds(200)),
                            PartDetailApiResponse.of(
                                    2, "\"etag2\"", 5242880L, now.minusSeconds(100)));

            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "file.zip",
                            10485760L,
                            "application/zip",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            2,
                            2,
                            parts,
                            "\"final-etag-123\"",
                            now.minusSeconds(300),
                            now.plusSeconds(86400),
                            now);

            // then
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.etag()).isEqualTo("\"final-etag-123\"");
            assertThat(response.completedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("PartDetailApiResponse 테스트")
    class PartDetailApiResponseTest {

        @Test
        @DisplayName("Part 상세 정보를 생성할 수 있다")
        void create_PartDetail_ShouldSucceed() {
            // given
            int partNumber = 1;
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            long size = 5242880L;
            Instant uploadedAt = Instant.now();

            // when
            PartDetailApiResponse partDetail =
                    new PartDetailApiResponse(partNumber, etag, size, uploadedAt);

            // then
            assertThat(partDetail.partNumber()).isEqualTo(partNumber);
            assertThat(partDetail.etag()).isEqualTo(etag);
            assertThat(partDetail.size()).isEqualTo(size);
            assertThat(partDetail.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 Part 상세 정보를 생성할 수 있다")
        void create_PartDetail_WithOfMethod_ShouldSucceed() {
            // given
            Instant uploadedAt = Instant.now();

            // when
            PartDetailApiResponse partDetail =
                    PartDetailApiResponse.of(3, "\"etag-value\"", 10485760L, uploadedAt);

            // then
            assertThat(partDetail.partNumber()).isEqualTo(3);
            assertThat(partDetail.etag()).isEqualTo("\"etag-value\"");
            assertThat(partDetail.size()).isEqualTo(10485760L);
        }

        @Test
        @DisplayName("Part 상세 정보의 동등성을 확인할 수 있다")
        void equals_PartDetail_ShouldWork() {
            // given
            Instant uploadedAt = Instant.parse("2025-11-26T10:00:00Z");
            PartDetailApiResponse part1 = PartDetailApiResponse.of(1, "etag", 5242880L, uploadedAt);
            PartDetailApiResponse part2 = PartDetailApiResponse.of(1, "etag", 5242880L, uploadedAt);

            // then
            assertThat(part1).isEqualTo(part2);
            assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
        }
    }

    @Nested
    @DisplayName("Part 목록 테스트")
    class PartsListTest {

        @Test
        @DisplayName("빈 Part 목록으로 Multipart 응답을 생성할 수 있다")
        void create_WithEmptyParts_ShouldSucceed() {
            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "file.zip",
                            10485760L,
                            "application/zip",
                            "PENDING",
                            "bucket",
                            "key",
                            "upload-id",
                            10,
                            0,
                            List.of(),
                            null,
                            Instant.now(),
                            Instant.now().plusSeconds(86400),
                            null);

            // then
            assertThat(response.parts()).isEmpty();
            assertThat(response.uploadedParts()).isZero();
        }

        @Test
        @DisplayName("많은 Part로 Multipart 응답을 생성할 수 있다")
        void create_WithManyParts_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            List<PartDetailApiResponse> parts =
                    java.util.stream.IntStream.rangeClosed(1, 1000)
                            .mapToObj(
                                    i ->
                                            PartDetailApiResponse.of(
                                                    i,
                                                    "\"etag" + i + "\"",
                                                    5242880L,
                                                    now.minusSeconds(1000 - i)))
                            .toList();

            // when
            UploadSessionDetailApiResponse response =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "very-large-file.zip",
                            5L * 1024 * 1024 * 1024,
                            "application/zip",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            1000,
                            1000,
                            parts,
                            "\"final-etag\"",
                            now.minusSeconds(3600),
                            now.plusSeconds(86400),
                            now);

            // then
            assertThat(response.totalParts()).isEqualTo(1000);
            assertThat(response.parts()).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 단일 업로드 응답은 동등하다")
        void equals_SingleUpload_WithSameValues_ShouldBeEqual() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant expiresAt = Instant.parse("2025-11-26T10:15:00Z");
            Instant completedAt = Instant.parse("2025-11-26T10:05:00Z");

            UploadSessionDetailApiResponse response1 =
                    UploadSessionDetailApiResponse.ofSingle(
                            "session",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "\"etag\"",
                            createdAt,
                            expiresAt,
                            completedAt);
            UploadSessionDetailApiResponse response2 =
                    UploadSessionDetailApiResponse.ofSingle(
                            "session",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "\"etag\"",
                            createdAt,
                            expiresAt,
                            completedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("같은 값을 가진 Multipart 업로드 응답은 동등하다")
        void equals_MultipartUpload_WithSameValues_ShouldBeEqual() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant expiresAt = Instant.parse("2025-11-27T10:00:00Z");
            List<PartDetailApiResponse> parts =
                    List.of(
                            PartDetailApiResponse.of(
                                    1, "etag", 5242880L, createdAt.plusSeconds(60)));

            UploadSessionDetailApiResponse response1 =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "file.zip",
                            5242880L,
                            "application/zip",
                            "IN_PROGRESS",
                            "bucket",
                            "key",
                            "upload-id",
                            2,
                            1,
                            parts,
                            null,
                            createdAt,
                            expiresAt,
                            null);
            UploadSessionDetailApiResponse response2 =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "file.zip",
                            5242880L,
                            "application/zip",
                            "IN_PROGRESS",
                            "bucket",
                            "key",
                            "upload-id",
                            2,
                            1,
                            parts,
                            null,
                            createdAt,
                            expiresAt,
                            null);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 uploadType을 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadType_ShouldNotBeEqual() {
            // given
            Instant now = Instant.now();
            UploadSessionDetailApiResponse singleResponse =
                    UploadSessionDetailApiResponse.ofSingle(
                            "session",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "\"etag\"",
                            now,
                            now.plusSeconds(900),
                            now.plusSeconds(60));
            UploadSessionDetailApiResponse multipartResponse =
                    UploadSessionDetailApiResponse.ofMultipart(
                            "session",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            1,
                            1,
                            List.of(),
                            "\"etag\"",
                            now,
                            now.plusSeconds(900),
                            now.plusSeconds(60));

            // when & then
            assertThat(singleResponse).isNotEqualTo(multipartResponse);
        }
    }
}
