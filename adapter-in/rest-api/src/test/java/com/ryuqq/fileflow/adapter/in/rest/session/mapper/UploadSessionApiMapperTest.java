package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitMultipartUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest.SessionStatusFilter;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.query.UploadSessionSearchApiRequest.UploadTypeFilter;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CompleteSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitMultipartUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.MarkPartUploadedApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.UploadSessionDetailApiResponse;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionApiMapper 단위 테스트")
class UploadSessionApiMapperTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    private UploadSessionApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UploadSessionApiMapper();
    }

    @Nested
    @DisplayName("toInitSingleUploadCommand 테스트")
    class ToInitSingleUploadCommandTest {

        @Test
        @DisplayName("모든 필드가 채워진 요청을 Command로 변환할 수 있다")
        void toInitSingleUploadCommand_WithAllFields_ShouldCreateCommand() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idempotency-key-123",
                            "image.jpg",
                            1024000L,
                            "image/jpeg",
                            "PRODUCT",
                            null);

            // when
            InitSingleUploadCommand command = mapper.toInitSingleUploadCommand(request);

            // then
            assertThat(command.idempotencyKey()).isEqualTo("idempotency-key-123");
            assertThat(command.fileName()).isEqualTo("image.jpg");
            assertThat(command.fileSize()).isEqualTo(1024000L);
            assertThat(command.contentType()).isEqualTo("image/jpeg");
            assertThat(command.uploadCategory()).isEqualTo("PRODUCT");
        }

        @Test
        @DisplayName("선택적 필드가 null인 요청을 Command로 변환할 수 있다")
        void toInitSingleUploadCommand_WithNullOptionalFields_ShouldCreateCommand() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idempotency-key-456",
                            "document.pdf",
                            2048000L,
                            "application/pdf",
                            null,
                            null);

            // when
            InitSingleUploadCommand command = mapper.toInitSingleUploadCommand(request);

            // then
            assertThat(command.idempotencyKey()).isEqualTo("idempotency-key-456");
            assertThat(command.fileName()).isEqualTo("document.pdf");
            assertThat(command.uploadCategory()).isNull();
            assertThat(command.customPath()).isNull();
        }
    }

    @Nested
    @DisplayName("toInitMultipartUploadCommand 테스트")
    class ToInitMultipartUploadCommandTest {

        @Test
        @DisplayName("Multipart 업로드 초기화 요청을 Command로 변환할 수 있다")
        void toInitMultipartUploadCommand_ShouldCreateCommand() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "large-file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            "PRODUCT",
                            null);

            // when
            InitMultipartUploadCommand command = mapper.toInitMultipartUploadCommand(request);

            // then
            assertThat(command.fileName()).isEqualTo("large-file.zip");
            assertThat(command.fileSize()).isEqualTo(104857600L);
            assertThat(command.contentType()).isEqualTo("application/zip");
            assertThat(command.partSize()).isEqualTo(5242880L);
            assertThat(command.uploadCategory()).isEqualTo("PRODUCT");
            assertThat(command.customPath()).isNull();
        }
    }

    @Nested
    @DisplayName("toCompleteSingleUploadCommand 테스트")
    class ToCompleteSingleUploadCommandTest {

        @Test
        @DisplayName("단일 업로드 완료 요청을 Command로 변환할 수 있다")
        void toCompleteSingleUploadCommand_ShouldCreateCommand() {
            // given
            String sessionId = "session-123";
            CompleteSingleUploadApiRequest request =
                    new CompleteSingleUploadApiRequest("\"d41d8cd98f00b204e9800998ecf8427e\"");

            // when
            CompleteSingleUploadCommand command =
                    mapper.toCompleteSingleUploadCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo("session-123");
            assertThat(command.etag()).isEqualTo("\"d41d8cd98f00b204e9800998ecf8427e\"");
        }
    }

    @Nested
    @DisplayName("toCompleteMultipartUploadCommand 테스트")
    class ToCompleteMultipartUploadCommandTest {

        @Test
        @DisplayName("Multipart 업로드 완료 요청을 Command로 변환할 수 있다")
        void toCompleteMultipartUploadCommand_ShouldCreateCommand() {
            // given
            String sessionId = "session-456";

            // when
            CompleteMultipartUploadCommand command =
                    mapper.toCompleteMultipartUploadCommand(sessionId);

            // then
            assertThat(command.sessionId()).isEqualTo("session-456");
        }
    }

    @Nested
    @DisplayName("toMarkPartUploadedCommand 테스트")
    class ToMarkPartUploadedCommandTest {

        @Test
        @DisplayName("Part 업로드 완료 요청을 Command로 변환할 수 있다")
        void toMarkPartUploadedCommand_ShouldCreateCommand() {
            // given
            String sessionId = "session-789";
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(3, "\"part-etag-abc\"", 5242880L);

            // when
            MarkPartUploadedCommand command = mapper.toMarkPartUploadedCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo("session-789");
            assertThat(command.partNumber()).isEqualTo(3);
            assertThat(command.etag()).isEqualTo("\"part-etag-abc\"");
            assertThat(command.size()).isEqualTo(5242880L);
        }
    }

    @Nested
    @DisplayName("toCancelUploadSessionCommand 테스트")
    class ToCancelUploadSessionCommandTest {

        @Test
        @DisplayName("세션 취소 요청을 Command로 변환할 수 있다")
        void toCancelUploadSessionCommand_ShouldCreateCommand() {
            // given
            String sessionId = "session-cancel-123";

            // when
            CancelUploadSessionCommand command = mapper.toCancelUploadSessionCommand(sessionId);

            // then
            assertThat(command.sessionId()).isEqualTo("session-cancel-123");
        }
    }

    @Nested
    @DisplayName("toInitSingleUploadApiResponse 테스트")
    class ToInitSingleUploadApiResponseTest {

        @Test
        @DisplayName("단일 업로드 초기화 응답을 API 응답으로 변환할 수 있다")
        void toInitSingleUploadApiResponse_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
            InitSingleUploadResponse response =
                    InitSingleUploadResponse.of(
                            "session-123",
                            "https://s3.amazonaws.com/bucket/key?signature=...",
                            expiresAt,
                            "fileflow-bucket",
                            "uploads/image.jpg");

            // when
            InitSingleUploadApiResponse apiResponse =
                    mapper.toInitSingleUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-123");
            assertThat(apiResponse.presignedUrl())
                    .isEqualTo("https://s3.amazonaws.com/bucket/key?signature=...");
            assertThat(apiResponse.expiresAt()).isEqualTo(expiresAt);
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/image.jpg");
        }
    }

    @Nested
    @DisplayName("toInitMultipartUploadApiResponse 테스트")
    class ToInitMultipartUploadApiResponseTest {

        @Test
        @DisplayName("Multipart 업로드 초기화 응답을 API 응답으로 변환할 수 있다")
        void toInitMultipartUploadApiResponse_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
            List<InitMultipartUploadResponse.PartInfo> parts =
                    List.of(
                            InitMultipartUploadResponse.PartInfo.of(1, "https://part1-url"),
                            InitMultipartUploadResponse.PartInfo.of(2, "https://part2-url"),
                            InitMultipartUploadResponse.PartInfo.of(3, "https://part3-url"));

            InitMultipartUploadResponse response =
                    InitMultipartUploadResponse.of(
                            "session-multipart-123",
                            "upload-id-xyz",
                            20,
                            5242880L,
                            expiresAt,
                            "fileflow-bucket",
                            "uploads/large-file.zip",
                            parts);

            // when
            InitMultipartUploadApiResponse apiResponse =
                    mapper.toInitMultipartUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-multipart-123");
            assertThat(apiResponse.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(apiResponse.totalParts()).isEqualTo(20);
            assertThat(apiResponse.partSize()).isEqualTo(5242880L);
            assertThat(apiResponse.expiresAt()).isEqualTo(expiresAt);
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/large-file.zip");
            assertThat(apiResponse.parts()).hasSize(3);
            assertThat(apiResponse.parts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.parts().get(0).presignedUrl()).isEqualTo("https://part1-url");
        }

        @Test
        @DisplayName("Part 목록이 비어있는 경우에도 변환할 수 있다")
        void toInitMultipartUploadApiResponse_WithEmptyParts_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
            InitMultipartUploadResponse response =
                    InitMultipartUploadResponse.of(
                            "session-456",
                            "upload-id-abc",
                            0,
                            5242880L,
                            expiresAt,
                            "bucket",
                            "key",
                            List.of());

            // when
            InitMultipartUploadApiResponse apiResponse =
                    mapper.toInitMultipartUploadApiResponse(response);

            // then
            assertThat(apiResponse.parts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toCompleteSingleUploadApiResponse 테스트")
    class ToCompleteSingleUploadApiResponseTest {

        @Test
        @DisplayName("단일 업로드 완료 응답을 API 응답으로 변환할 수 있다")
        void toCompleteSingleUploadApiResponse_ShouldConvertCorrectly() {
            // given
            Instant completedAt = Instant.now();
            CompleteSingleUploadResponse response =
                    CompleteSingleUploadResponse.of(
                            "session-123",
                            "COMPLETED",
                            "fileflow-bucket",
                            "uploads/image.jpg",
                            "\"etag-completed\"",
                            completedAt);

            // when
            CompleteSingleUploadApiResponse apiResponse =
                    mapper.toCompleteSingleUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-123");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/image.jpg");
            assertThat(apiResponse.etag()).isEqualTo("\"etag-completed\"");
            assertThat(apiResponse.completedAt()).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("toCompleteMultipartUploadApiResponse 테스트")
    class ToCompleteMultipartUploadApiResponseTest {

        @Test
        @DisplayName("Multipart 업로드 완료 응답을 API 응답으로 변환할 수 있다")
        void toCompleteMultipartUploadApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            List<CompleteMultipartUploadResponse.CompletedPartInfo> completedParts =
                    List.of(
                            CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                    1, "\"etag1\"", 5242880L, now.minus(10, ChronoUnit.MINUTES)),
                            CompleteMultipartUploadResponse.CompletedPartInfo.of(
                                    2, "\"etag2\"", 5242880L, now.minus(5, ChronoUnit.MINUTES)));

            CompleteMultipartUploadResponse response =
                    CompleteMultipartUploadResponse.of(
                            "session-multipart-456",
                            "COMPLETED",
                            "fileflow-bucket",
                            "uploads/large-file.zip",
                            "upload-id-xyz",
                            2,
                            completedParts,
                            now);

            // when
            CompleteMultipartUploadApiResponse apiResponse =
                    mapper.toCompleteMultipartUploadApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-multipart-456");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/large-file.zip");
            assertThat(apiResponse.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(apiResponse.totalParts()).isEqualTo(2);
            assertThat(apiResponse.completedParts()).hasSize(2);
            assertThat(apiResponse.completedParts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.completedParts().get(0).etag()).isEqualTo("\"etag1\"");
            assertThat(apiResponse.completedParts().get(0).size()).isEqualTo(5242880L);
            assertThat(apiResponse.completedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("toMarkPartUploadedApiResponse 테스트")
    class ToMarkPartUploadedApiResponseTest {

        @Test
        @DisplayName("Part 업로드 완료 응답을 API 응답으로 변환할 수 있다")
        void toMarkPartUploadedApiResponse_ShouldConvertCorrectly() {
            // given
            Instant uploadedAt = Instant.now();
            MarkPartUploadedResponse response =
                    MarkPartUploadedResponse.of("session-789", 5, "\"part-etag-5\"", uploadedAt);

            // when
            MarkPartUploadedApiResponse apiResponse =
                    mapper.toMarkPartUploadedApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-789");
            assertThat(apiResponse.partNumber()).isEqualTo(5);
            assertThat(apiResponse.etag()).isEqualTo("\"part-etag-5\"");
            assertThat(apiResponse.uploadedAt()).isEqualTo(uploadedAt);
        }
    }

    @Nested
    @DisplayName("toCancelUploadSessionApiResponse 테스트")
    class ToCancelUploadSessionApiResponseTest {

        @Test
        @DisplayName("세션 취소 응답을 API 응답으로 변환할 수 있다")
        void toCancelUploadSessionApiResponse_ShouldConvertCorrectly() {
            // given
            CancelUploadSessionResponse response =
                    CancelUploadSessionResponse.of(
                            "session-cancel-123",
                            "CANCELLED",
                            "fileflow-bucket",
                            "uploads/cancelled-file.jpg");

            // when
            CancelUploadSessionApiResponse apiResponse =
                    mapper.toCancelUploadSessionApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-cancel-123");
            assertThat(apiResponse.status()).isEqualTo("CANCELLED");
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/cancelled-file.jpg");
        }
    }

    @Nested
    @DisplayName("toGetUploadSessionQuery 테스트")
    class ToGetUploadSessionQueryTest {

        @Test
        @DisplayName("단건 조회 Query를 생성할 수 있다")
        void toGetUploadSessionQuery_ShouldCreateQuery() {
            // given
            String sessionId = "session-query-123";
            String tenantId = TEST_TENANT_ID;

            // when
            GetUploadSessionQuery query = mapper.toGetUploadSessionQuery(sessionId, tenantId);

            // then
            assertThat(query.sessionId()).isEqualTo("session-query-123");
            assertThat(query.tenantId()).isEqualTo(tenantId);
        }
    }

    @Nested
    @DisplayName("toListUploadSessionsQuery 테스트")
    class ToListUploadSessionsQueryTest {

        @Test
        @DisplayName("모든 필터 조건으로 목록 조회 Query를 생성할 수 있다")
        void toListUploadSessionsQuery_WithAllFilters_ShouldCreateQuery() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(
                            SessionStatusFilter.COMPLETED, UploadTypeFilter.SINGLE, 0, 20);
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;

            // when
            ListUploadSessionsQuery query =
                    mapper.toListUploadSessionsQuery(request, tenantId, organizationId);

            // then
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.organizationId()).isEqualTo(organizationId);
            assertThat(query.status()).isEqualTo("COMPLETED");
            assertThat(query.uploadType()).isEqualTo("SINGLE");
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("필터 없이 목록 조회 Query를 생성할 수 있다")
        void toListUploadSessionsQuery_WithoutFilters_ShouldCreateQuery() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, null, null, null);
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;

            // when
            ListUploadSessionsQuery query =
                    mapper.toListUploadSessionsQuery(request, tenantId, organizationId);

            // then
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.organizationId()).isEqualTo(organizationId);
            assertThat(query.status()).isNull();
            assertThat(query.uploadType()).isNull();
            assertThat(query.page()).isEqualTo(0); // 기본값
            assertThat(query.size()).isEqualTo(20); // 기본값
        }

        @Test
        @DisplayName("상태 필터만으로 Query를 생성할 수 있다")
        void toListUploadSessionsQuery_WithStatusOnly_ShouldCreateQuery() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(SessionStatusFilter.ACTIVE, null, 1, 10);

            // when
            ListUploadSessionsQuery query =
                    mapper.toListUploadSessionsQuery(request, TEST_TENANT_ID, TEST_ORG_ID);

            // then
            assertThat(query.status()).isEqualTo("ACTIVE");
            assertThat(query.uploadType()).isNull();
            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("업로드 타입 필터만으로 Query를 생성할 수 있다")
        void toListUploadSessionsQuery_WithUploadTypeOnly_ShouldCreateQuery() {
            // given
            UploadSessionSearchApiRequest request =
                    new UploadSessionSearchApiRequest(null, UploadTypeFilter.MULTIPART, 2, 50);

            // when
            ListUploadSessionsQuery query =
                    mapper.toListUploadSessionsQuery(request, TEST_TENANT_ID, TEST_ORG_ID);

            // then
            assertThat(query.status()).isNull();
            assertThat(query.uploadType()).isEqualTo("MULTIPART");
            assertThat(query.page()).isEqualTo(2);
            assertThat(query.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("toUploadSessionApiResponse 테스트")
    class ToUploadSessionApiResponseTest {

        @Test
        @DisplayName("UploadSessionResponse를 API 응답으로 변환할 수 있다")
        void toUploadSessionApiResponse_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant expiresAt = Instant.parse("2025-11-26T10:15:00Z");

            UploadSessionResponse response =
                    UploadSessionResponse.of(
                            "session-list-123",
                            "image.jpg",
                            1024000L,
                            "image/jpeg",
                            "SINGLE",
                            "COMPLETED",
                            "fileflow-bucket",
                            "uploads/image.jpg",
                            createdAt,
                            expiresAt);

            // when
            UploadSessionApiResponse apiResponse = mapper.toUploadSessionApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-list-123");
            assertThat(apiResponse.fileName()).isEqualTo("image.jpg");
            assertThat(apiResponse.fileSize()).isEqualTo(1024000L);
            assertThat(apiResponse.contentType()).isEqualTo("image/jpeg");
            assertThat(apiResponse.uploadType()).isEqualTo("SINGLE");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.bucket()).isEqualTo("fileflow-bucket");
            assertThat(apiResponse.key()).isEqualTo("uploads/image.jpg");
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
            assertThat(apiResponse.expiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    @DisplayName("toUploadSessionDetailApiResponse 테스트")
    class ToUploadSessionDetailApiResponseTest {

        @Test
        @DisplayName("단일 업로드 세션 상세 응답을 API 응답으로 변환할 수 있다")
        void toUploadSessionDetailApiResponse_Single_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant expiresAt = Instant.parse("2025-11-26T10:15:00Z");
            Instant completedAt = Instant.parse("2025-11-26T10:05:00Z");

            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofSingle(
                            "session-detail-single",
                            "image.jpg",
                            1024000L,
                            "image/jpeg",
                            "COMPLETED",
                            "fileflow-bucket",
                            "uploads/image.jpg",
                            "\"single-etag\"",
                            createdAt,
                            expiresAt,
                            completedAt);

            // when
            UploadSessionDetailApiResponse apiResponse =
                    mapper.toUploadSessionDetailApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-detail-single");
            assertThat(apiResponse.uploadType()).isEqualTo("SINGLE");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.uploadId()).isNull();
            assertThat(apiResponse.totalParts()).isNull();
            assertThat(apiResponse.uploadedParts()).isNull();
            assertThat(apiResponse.parts()).isNull();
            assertThat(apiResponse.etag()).isEqualTo("\"single-etag\"");
            assertThat(apiResponse.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("Multipart 업로드 세션 상세 응답을 API 응답으로 변환할 수 있다")
        void toUploadSessionDetailApiResponse_Multipart_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant expiresAt = Instant.parse("2025-11-27T10:00:00Z");
            Instant partUploadedAt = Instant.parse("2025-11-26T10:30:00Z");

            List<UploadSessionDetailResponse.PartDetailResponse> parts =
                    List.of(
                            UploadSessionDetailResponse.PartDetailResponse.of(
                                    1, "\"part-etag-1\"", 5242880L, partUploadedAt),
                            UploadSessionDetailResponse.PartDetailResponse.of(
                                    2, "\"part-etag-2\"", 5242880L, partUploadedAt));

            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofMultipart(
                            "session-detail-multipart",
                            "large-file.zip",
                            104857600L,
                            "application/zip",
                            "IN_PROGRESS",
                            "fileflow-bucket",
                            "uploads/large-file.zip",
                            "upload-id-xyz",
                            20,
                            2,
                            parts,
                            null,
                            createdAt,
                            expiresAt,
                            null);

            // when
            UploadSessionDetailApiResponse apiResponse =
                    mapper.toUploadSessionDetailApiResponse(response);

            // then
            assertThat(apiResponse.sessionId()).isEqualTo("session-detail-multipart");
            assertThat(apiResponse.uploadType()).isEqualTo("MULTIPART");
            assertThat(apiResponse.status()).isEqualTo("IN_PROGRESS");
            assertThat(apiResponse.uploadId()).isEqualTo("upload-id-xyz");
            assertThat(apiResponse.totalParts()).isEqualTo(20);
            assertThat(apiResponse.uploadedParts()).isEqualTo(2);
            assertThat(apiResponse.parts()).hasSize(2);
            assertThat(apiResponse.parts().get(0).partNumber()).isEqualTo(1);
            assertThat(apiResponse.parts().get(0).etag()).isEqualTo("\"part-etag-1\"");
            assertThat(apiResponse.parts().get(0).size()).isEqualTo(5242880L);
            assertThat(apiResponse.etag()).isNull();
            assertThat(apiResponse.completedAt()).isNull();
        }

        @Test
        @DisplayName("Part 목록이 null인 Multipart 세션도 변환할 수 있다")
        void toUploadSessionDetailApiResponse_MultipartWithNullParts_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plus(24, ChronoUnit.HOURS);

            UploadSessionDetailResponse response =
                    UploadSessionDetailResponse.ofMultipart(
                            "session-no-parts",
                            "file.zip",
                            50000000L,
                            "application/zip",
                            "PENDING",
                            "bucket",
                            "key",
                            "upload-id",
                            10,
                            0,
                            null,
                            null,
                            createdAt,
                            expiresAt,
                            null);

            // when
            UploadSessionDetailApiResponse apiResponse =
                    mapper.toUploadSessionDetailApiResponse(response);

            // then
            assertThat(apiResponse.uploadType()).isEqualTo("MULTIPART");
            assertThat(apiResponse.parts()).isNull();
            assertThat(apiResponse.totalParts()).isEqualTo(10);
            assertThat(apiResponse.uploadedParts()).isEqualTo(0);
        }
    }
}
