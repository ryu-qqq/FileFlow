package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitMultipartUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedRequest;
import com.ryuqq.fileflow.sdk.model.session.MarkPartUploadedResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionDetailResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionApi WireMock 통합 테스트")
@WireMockTest
class UploadSessionApiIntegrationTest extends WireMockTestSupport {

    private UploadSessionApi uploadSessionApi;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        FileFlowClient client = createClient(wmRuntimeInfo);
        uploadSessionApi = client.uploadSessions();
    }

    @Nested
    @DisplayName("initSingle 메서드")
    class InitSingleTest {

        @Test
        @DisplayName("단일 파일 업로드 세션을 초기화할 수 있다")
        void shouldInitializeSingleUploadSession() {
            // given
            InitSingleUploadRequest request =
                    InitSingleUploadRequest.builder()
                            .fileName("document.pdf")
                            .contentType("application/pdf")
                            .fileSize(1024L)
                            .uploadCategory("DOCUMENT")
                            .build();

            String responseData =
                    """
                    {
                        "sessionId": "session-123",
                        "presignedUrl": "https://s3.amazonaws.com/bucket/upload?signed=xyz",
                        "expiresAt": "2025-01-15T11:00:00",
                        "s3Key": "uploads/document.pdf"
                    }
                    """;

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            InitSingleUploadResponse response = uploadSessionApi.initSingle(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("session-123");
            assertThat(response.getPresignedUrl())
                    .isEqualTo("https://s3.amazonaws.com/bucket/upload?signed=xyz");
            assertThat(response.getExpiresAt()).isNotNull();
            assertThat(response.getS3Key()).isEqualTo("uploads/document.pdf");
        }

        @Test
        @DisplayName("필수 필드만으로 업로드 세션을 초기화할 수 있다")
        void shouldInitializeWithRequiredFieldsOnly() {
            // given
            InitSingleUploadRequest request =
                    InitSingleUploadRequest.builder().fileName("test.txt").build();

            String responseData =
                    """
                    {
                        "sessionId": "session-456",
                        "presignedUrl": "https://s3.amazonaws.com/bucket/upload2",
                        "expiresAt": "2025-01-15T12:00:00",
                        "s3Key": "uploads/test.txt"
                    }
                    """;

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            InitSingleUploadResponse response = uploadSessionApi.initSingle(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("session-456");
        }

        @Test
        @DisplayName("잘못된 요청 시 FileFlowBadRequestException이 발생한다")
        void shouldThrowClientExceptionOnBadRequest() {
            // given
            InitSingleUploadRequest request =
                    InitSingleUploadRequest.builder().fileName("").build();

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single")))
                            .willReturn(
                                    errorResponse(
                                            400, "INVALID_REQUEST", "Filename cannot be empty")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.initSingle(request))
                    .isInstanceOf(FileFlowBadRequestException.class);
        }
    }

    @Nested
    @DisplayName("completeSingle 메서드")
    class CompleteSingleTest {

        @Test
        @DisplayName("업로드 세션을 완료 처리할 수 있다")
        void shouldCompleteUploadSession() {
            // given
            String sessionId = "session-to-complete";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/session-to-complete/single/complete")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then - 예외가 발생하지 않으면 성공
            uploadSessionApi.completeSingle(sessionId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 완료 요청하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenSessionNotExists() {
            // given
            String sessionId = "non-existent-session";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/non-existent-session/single/complete")))
                            .willReturn(
                                    errorResponse(
                                            404, "SESSION_NOT_FOUND", "Upload session not found")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.completeSingle(sessionId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }

        @Test
        @DisplayName("이미 완료된 세션에 대해 다시 완료 요청하면 예외가 발생한다")
        void shouldThrowExceptionWhenSessionAlreadyCompleted() {
            // given
            String sessionId = "already-completed-session";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/already-completed-session/single/complete")))
                            .willReturn(
                                    errorResponse(
                                            400, "INVALID_STATE", "Session is already completed")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.completeSingle(sessionId))
                    .isInstanceOf(FileFlowBadRequestException.class);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class CancelTest {

        @Test
        @DisplayName("업로드 세션을 취소할 수 있다")
        void shouldCancelUploadSession() {
            // given
            String sessionId = "session-to-cancel";

            String responseData =
                    """
                    {
                        "sessionId": "session-to-cancel",
                        "status": "FAILED",
                        "bucket": "fileflow-bucket",
                        "key": "uploads/cancelled-file.pdf"
                    }
                    """;

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/session-to-cancel/cancel")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            var response = uploadSessionApi.cancel(sessionId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("session-to-cancel");
            assertThat(response.getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 취소 요청하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenCancelNonExistent() {
            // given
            String sessionId = "non-existent-session";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/non-existent-session/cancel")))
                            .willReturn(
                                    errorResponse(
                                            404, "SESSION_NOT_FOUND", "Upload session not found")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.cancel(sessionId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }

        @Test
        @DisplayName("이미 취소된 세션에 대해 다시 취소 요청하면 예외가 발생한다")
        void shouldThrowExceptionWhenSessionAlreadyCancelled() {
            // given
            String sessionId = "already-cancelled-session";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/already-cancelled-session/cancel")))
                            .willReturn(
                                    errorResponse(
                                            400, "INVALID_STATE", "Session is already cancelled")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.cancel(sessionId))
                    .isInstanceOf(FileFlowBadRequestException.class);
        }
    }

    @Nested
    @DisplayName("인증 헤더 테스트")
    class AuthenticationTest {

        @Test
        @DisplayName("Bearer 토큰이 Authorization 헤더에 포함된다")
        void shouldIncludeBearerTokenInAuthorizationHeader() {
            // given
            InitSingleUploadRequest request =
                    InitSingleUploadRequest.builder()
                            .fileName("auth-test.pdf")
                            .contentType("application/pdf")
                            .build();

            String responseData =
                    """
                    {
                        "sessionId": "auth-session",
                        "presignedUrl": "https://s3.amazonaws.com/bucket/auth",
                        "expiresAt": "2025-01-15T11:00:00",
                        "s3Key": "uploads/auth-test.pdf"
                    }
                    """;

            // Authorization 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    post(urlPathEqualTo("/api/v1/file/upload-sessions/single"))
                            .withHeader(
                                    "Authorization",
                                    com.github.tomakehurst.wiremock.client.WireMock.equalTo(
                                            "Bearer test-service-token"))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            InitSingleUploadResponse response = uploadSessionApi.initSingle(request);

            // then - 인증 헤더가 올바르게 전송되었음을 확인
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("auth-session");
        }
    }

    @Nested
    @DisplayName("initMultipart 메서드")
    class InitMultipartTest {

        @Test
        @DisplayName("멀티파트 업로드 세션을 초기화할 수 있다")
        void shouldInitializeMultipartUploadSession() {
            // given
            InitMultipartUploadRequest request =
                    InitMultipartUploadRequest.builder()
                            .fileName("large-video.mp4")
                            .fileSize(100_000_000L)
                            .contentType("video/mp4")
                            .partSize(10_000_000L)
                            .uploadCategory("VIDEO")
                            .build();

            String responseData =
                    """
{
    "sessionId": "multipart-session-123",
    "uploadId": "s3-upload-id-abc",
    "totalParts": 10,
    "partSize": 10000000,
    "expiresAt": "2025-01-15T12:00:00Z",
    "bucket": "fileflow-bucket",
    "key": "uploads/large-video.mp4",
    "parts": [
        {"partNumber": 1, "presignedUrl": "https://s3.amazonaws.com/bucket/part1"},
        {"partNumber": 2, "presignedUrl": "https://s3.amazonaws.com/bucket/part2"}
    ]
}
""";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/multipart")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            InitMultipartUploadResponse response = uploadSessionApi.initMultipart(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("multipart-session-123");
            assertThat(response.getUploadId()).isEqualTo("s3-upload-id-abc");
            assertThat(response.getTotalParts()).isEqualTo(10);
            assertThat(response.getPartSize()).isEqualTo(10_000_000L);
            assertThat(response.getParts()).hasSize(2);
            assertThat(response.getParts().get(0).getPartNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("markPartUploaded 메서드")
    class MarkPartUploadedTest {

        @Test
        @DisplayName("파트 업로드 완료를 기록할 수 있다")
        void shouldMarkPartAsUploaded() {
            // given
            String sessionId = "multipart-session-123";
            MarkPartUploadedRequest request =
                    MarkPartUploadedRequest.builder()
                            .partNumber(1)
                            .etag("\"abc123def456\"")
                            .size(10_000_000L)
                            .build();

            String responseData =
                    """
                    {
                        "sessionId": "multipart-session-123",
                        "partNumber": 1,
                        "etag": "\\"abc123def456\\"",
                        "uploadedAt": "2025-01-15T10:30:00Z"
                    }
                    """;

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/multipart-session-123/parts")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            MarkPartUploadedResponse response =
                    uploadSessionApi.markPartUploaded(sessionId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("multipart-session-123");
            assertThat(response.getPartNumber()).isEqualTo(1);
            assertThat(response.getUploadedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("completeMultipart 메서드")
    class CompleteMultipartTest {

        @Test
        @DisplayName("멀티파트 업로드를 완료할 수 있다")
        void shouldCompleteMultipartUpload() {
            // given
            String sessionId = "multipart-session-123";

            String responseData =
                    """
{
    "sessionId": "multipart-session-123",
    "status": "COMPLETED",
    "bucket": "fileflow-bucket",
    "key": "uploads/large-video.mp4",
    "uploadId": "s3-upload-id-abc",
    "totalParts": 10,
    "completedParts": [
        {"partNumber": 1, "etag": "abc123", "size": 10000000, "uploadedAt": "2025-01-15T10:30:00Z"}
    ],
    "completedAt": "2025-01-15T11:00:00Z"
}
""";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/multipart-session-123/multipart/complete")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            CompleteMultipartUploadResponse response =
                    uploadSessionApi.completeMultipart(sessionId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("multipart-session-123");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getTotalParts()).isEqualTo(10);
            assertThat(response.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("세션 ID로 업로드 세션 상세 정보를 조회할 수 있다")
        void shouldGetUploadSessionDetail() {
            // given
            String sessionId = "session-123";

            String responseData =
                    """
                    {
                        "sessionId": "session-123",
                        "fileName": "document.pdf",
                        "fileSize": 1024,
                        "contentType": "application/pdf",
                        "uploadType": "SINGLE",
                        "status": "COMPLETED",
                        "bucket": "fileflow-bucket",
                        "key": "uploads/document.pdf",
                        "uploadId": null,
                        "totalParts": null,
                        "uploadedParts": null,
                        "parts": null,
                        "etag": "abc123",
                        "createdAt": "2025-01-15T10:00:00Z",
                        "expiresAt": "2025-01-15T11:00:00Z",
                        "completedAt": "2025-01-15T10:30:00Z"
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/upload-sessions/session-123")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            UploadSessionDetailResponse response = uploadSessionApi.get(sessionId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("session-123");
            assertThat(response.getFileName()).isEqualTo("document.pdf");
            assertThat(response.getUploadType()).isEqualTo("SINGLE");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 조회하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenSessionNotExists() {
            // given
            String sessionId = "non-existent-session";

            stubFor(
                    withAuth(
                                    get(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/non-existent-session")))
                            .willReturn(
                                    errorResponse(
                                            404, "SESSION_NOT_FOUND", "Upload session not found")));

            // when & then
            assertThatThrownBy(() -> uploadSessionApi.get(sessionId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("업로드 세션 목록을 페이지네이션으로 조회할 수 있다")
        void shouldListUploadSessions() {
            // given
            UploadSessionSearchRequest request =
                    UploadSessionSearchRequest.builder().page(0).size(10).build();

            String responseData =
                    """
                    {
                        "content": [
                            {
                                "sessionId": "session-1",
                                "fileName": "file1.pdf",
                                "fileSize": 1024,
                                "contentType": "application/pdf",
                                "uploadType": "SINGLE",
                                "status": "COMPLETED",
                                "bucket": "fileflow-bucket",
                                "key": "uploads/file1.pdf",
                                "createdAt": "2025-01-15T10:00:00Z",
                                "expiresAt": "2025-01-15T11:00:00Z"
                            }
                        ],
                        "page": 0,
                        "size": 10,
                        "totalElements": 1,
                        "totalPages": 1,
                        "first": true,
                        "last": true
                    }
                    """;

            stubFor(
                    withAuth(get(urlEqualTo("/api/v1/file/upload-sessions?page=0&size=10")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            PageResponse<UploadSessionResponse> response = uploadSessionApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getSessionId()).isEqualTo("session-1");
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태 필터로 업로드 세션을 조회할 수 있다")
        void shouldListUploadSessionsWithStatusFilter() {
            // given
            UploadSessionSearchRequest request =
                    UploadSessionSearchRequest.builder()
                            .page(0)
                            .size(10)
                            .status(UploadSessionSearchRequest.SessionStatus.COMPLETED)
                            .build();

            String responseData =
                    """
                    {
                        "content": [],
                        "page": 0,
                        "size": 10,
                        "totalElements": 0,
                        "totalPages": 0,
                        "first": true,
                        "last": true
                    }
                    """;

            stubFor(
                    withAuth(
                                    get(
                                            urlEqualTo(
                                                    "/api/v1/file/upload-sessions?page=0&size=10&status=COMPLETED")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            PageResponse<UploadSessionResponse> response = uploadSessionApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
        }
    }
}
