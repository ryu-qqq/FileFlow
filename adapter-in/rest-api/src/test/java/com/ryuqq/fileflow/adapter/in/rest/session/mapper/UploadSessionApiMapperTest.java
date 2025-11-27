package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MarkPartUploadedApiResponse;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionApiMapper 단위 테스트")
class UploadSessionApiMapperTest {

    private UploadSessionApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UploadSessionApiMapper();
    }

    @Nested
    @DisplayName("API Request → Command 변환 테스트")
    class RequestToCommandTest {

        @Test
        @DisplayName("InitSingleUploadApiRequest를 Command로 변환할 수 있다")
        void toInitSingleUploadCommand_ShouldConvertCorrectly() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idempotency-key-123",
                            "test-file.jpg",
                            1024L,
                            "image/jpeg",
                            1L,
                            100L,
                            null,
                            "user@test.com");

            // when
            InitSingleUploadCommand command = mapper.toInitSingleUploadCommand(request);

            // then
            assertThat(command.idempotencyKey()).isEqualTo("idempotency-key-123");
            assertThat(command.fileName()).isEqualTo("test-file.jpg");
            assertThat(command.fileSize()).isEqualTo(1024L);
            assertThat(command.contentType()).isEqualTo("image/jpeg");
            assertThat(command.tenantId()).isEqualTo(1L);
            assertThat(command.organizationId()).isEqualTo(100L);
            assertThat(command.userId()).isNull();
            assertThat(command.userEmail()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("InitMultipartUploadApiRequest를 Command로 변환할 수 있다")
        void toInitMultipartUploadCommand_ShouldConvertCorrectly() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "large-video.mp4",
                            100 * 1024 * 1024L,
                            "video/mp4",
                            5 * 1024 * 1024L,
                            1L,
                            100L,
                            null,
                            "seller@test.com");

            // when
            InitMultipartUploadCommand command = mapper.toInitMultipartUploadCommand(request);

            // then
            assertThat(command.fileName()).isEqualTo("large-video.mp4");
            assertThat(command.fileSize()).isEqualTo(100 * 1024 * 1024L);
            assertThat(command.contentType()).isEqualTo("video/mp4");
            assertThat(command.partSize()).isEqualTo(5 * 1024 * 1024L);
            assertThat(command.tenantId()).isEqualTo(1L);
            assertThat(command.organizationId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("CompleteSingleUploadApiRequest를 Command로 변환할 수 있다")
        void toCompleteSingleUploadCommand_ShouldConvertCorrectly() {
            // given
            String sessionId = "session-123";
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest("etag-abc");

            // when
            CompleteSingleUploadCommand command =
                    mapper.toCompleteSingleUploadCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.etag()).isEqualTo("etag-abc");
        }

        @Test
        @DisplayName("sessionId로 CompleteMultipartUploadCommand를 생성할 수 있다")
        void toCompleteMultipartUploadCommand_ShouldConvertCorrectly() {
            // given
            String sessionId = "session-multipart-456";

            // when
            CompleteMultipartUploadCommand command =
                    mapper.toCompleteMultipartUploadCommand(sessionId);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
        }

        @Test
        @DisplayName("MarkPartUploadedApiRequest를 Command로 변환할 수 있다")
        void toMarkPartUploadedCommand_ShouldConvertCorrectly() {
            // given
            String sessionId = "session-789";
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(3, "etag-part-3", 5 * 1024 * 1024L);

            // when
            MarkPartUploadedCommand command = mapper.toMarkPartUploadedCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.partNumber()).isEqualTo(3);
            assertThat(command.etag()).isEqualTo("etag-part-3");
            assertThat(command.size()).isEqualTo(5 * 1024 * 1024L);
        }

        @Test
        @DisplayName("sessionId로 CancelUploadSessionCommand를 생성할 수 있다")
        void toCancelUploadSessionCommand_ShouldConvertCorrectly() {
            // given
            String sessionId = "session-to-cancel";

            // when
            CancelUploadSessionCommand command = mapper.toCancelUploadSessionCommand(sessionId);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
        }
    }

    @Nested
    @DisplayName("Application Response → API Response 변환 테스트")
    class ResponseToApiResponseTest {

        @Test
        @DisplayName("InitSingleUploadResponse를 API Response로 변환할 수 있다")
        void toInitSingleUploadApiResponse_ShouldConvertCorrectly() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            InitSingleUploadResponse response =
                    InitSingleUploadResponse.of(
                            "session-123",
                            "https://s3.amazonaws.com/bucket/key?presigned",
                            expiresAt,
                            "test-bucket",
                            "uploads/test.jpg");

            // when
            InitSingleUploadApiResponse apiResponse =
                    mapper.toInitSingleUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-123");
            assertThat(apiResponse.presignedUrl()).contains("presigned");
            assertThat(apiResponse.expiresAt()).isEqualTo(expiresAt);
            assertThat(apiResponse.bucket()).isEqualTo("test-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/test.jpg");
        }

        @Test
        @DisplayName("InitMultipartUploadResponse를 API Response로 변환할 수 있다")
        void toInitMultipartUploadApiResponse_ShouldConvertCorrectly() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            List<InitMultipartUploadResponse.PartInfo> parts =
                    List.of(
                            InitMultipartUploadResponse.PartInfo.of(1, "https://part1-url"),
                            InitMultipartUploadResponse.PartInfo.of(2, "https://part2-url"),
                            InitMultipartUploadResponse.PartInfo.of(3, "https://part3-url"));

            InitMultipartUploadResponse response =
                    InitMultipartUploadResponse.of(
                            "session-multipart",
                            "upload-id-xyz",
                            3,
                            5 * 1024 * 1024L,
                            expiresAt,
                            "bucket-name",
                            "multipart/key",
                            parts);

            // when
            InitMultipartUploadApiResponse apiResponse =
                    mapper.toInitMultipartUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-multipart");
            assertThat(apiResponse.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(apiResponse.totalParts()).isEqualTo(3);
            assertThat(apiResponse.partSize()).isEqualTo(5 * 1024 * 1024L);
            assertThat(apiResponse.parts()).hasSize(3);
            assertThat(apiResponse.parts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.parts().get(0).presignedUrl()).isEqualTo("https://part1-url");
        }

        @Test
        @DisplayName("CompleteSingleUploadResponse를 API Response로 변환할 수 있다")
        void toCompleteSingleUploadApiResponse_ShouldConvertCorrectly() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 30);
            CompleteSingleUploadResponse response =
                    CompleteSingleUploadResponse.of(
                            "session-complete",
                            "COMPLETED",
                            "bucket",
                            "key/path",
                            "final-etag",
                            completedAt);

            // when
            CompleteSingleUploadApiResponse apiResponse =
                    mapper.toCompleteSingleUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-complete");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.bucket()).isEqualTo("bucket");
            assertThat(apiResponse.key()).isEqualTo("key/path");
            assertThat(apiResponse.etag()).isEqualTo("final-etag");
            assertThat(apiResponse.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("CompleteMultipartUploadResponse를 API Response로 변환할 수 있다")
        void toCompleteMultipartUploadApiResponse_ShouldConvertCorrectly() {
            // given
            LocalDateTime partUploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0);
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 0);

            List<CompleteMultipartUploadResponse.CompletedPartInfo> parts =
                    List.of(
                            CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                    1, "etag1", 5 * 1024 * 1024L, partUploadedAt),
                            CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                    2, "etag2", 3 * 1024 * 1024L, partUploadedAt));

            CompleteMultipartUploadResponse response =
                    CompleteMultipartUploadResponse.of(
                            "session-mp",
                            "COMPLETED",
                            "bucket",
                            "key",
                            "upload-id",
                            2,
                            parts,
                            completedAt);

            // when
            CompleteMultipartUploadApiResponse apiResponse =
                    mapper.toCompleteMultipartUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-mp");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.totalParts()).isEqualTo(2);
            assertThat(apiResponse.completedParts()).hasSize(2);
            assertThat(apiResponse.completedParts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.completedParts().get(0).etag()).isEqualTo("etag1");
        }

        @Test
        @DisplayName("MarkPartUploadedResponse를 API Response로 변환할 수 있다")
        void toMarkPartUploadedApiResponse_ShouldConvertCorrectly() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 30);
            MarkPartUploadedResponse response =
                    MarkPartUploadedResponse.of("session-part", 2, "part-etag", uploadedAt);

            // when
            MarkPartUploadedApiResponse apiResponse =
                    mapper.toMarkPartUploadedApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-part");
            assertThat(apiResponse.partNumber()).isEqualTo(2);
            assertThat(apiResponse.etag()).isEqualTo("part-etag");
            assertThat(apiResponse.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("CancelUploadSessionResponse를 API Response로 변환할 수 있다")
        void toCancelUploadSessionApiResponse_ShouldConvertCorrectly() {
            // given
            CancelUploadSessionResponse response =
                    CancelUploadSessionResponse.of("session-cancel", "CANCELLED", "bucket", "key");

            // when
            CancelUploadSessionApiResponse apiResponse =
                    mapper.toCancelUploadSessionApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-cancel");
            assertThat(apiResponse.status()).isEqualTo("CANCELLED");
            assertThat(apiResponse.bucket()).isEqualTo("bucket");
            assertThat(apiResponse.key()).isEqualTo("key");
        }
    }
}
