package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
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
                            .filename("document.pdf")
                            .contentType("application/pdf")
                            .fileSize(1024L)
                            .category("DOCUMENT")
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
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single/init")))
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
                    InitSingleUploadRequest.builder().filename("test.txt").build();

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
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single/init")))
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
                    InitSingleUploadRequest.builder().filename("").build();

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/upload-sessions/single/init")))
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
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/session-to-complete/complete")))
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
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/non-existent-session/complete")))
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
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/already-completed-session/complete")))
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

            stubFor(
                    withAuth(
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/upload-sessions/session-to-cancel/cancel")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then - 예외가 발생하지 않으면 성공
            uploadSessionApi.cancel(sessionId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 취소 요청하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenCancelNonExistent() {
            // given
            String sessionId = "non-existent-session";

            stubFor(
                    withAuth(
                                    post(
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
                                    post(
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
        @DisplayName("서비스 토큰이 X-Service-Token 헤더에 포함된다")
        void shouldIncludeServiceTokenInHeader() {
            // given
            InitSingleUploadRequest request =
                    InitSingleUploadRequest.builder()
                            .filename("auth-test.pdf")
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

            // X-Service-Token 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    post(urlPathEqualTo("/api/v1/file/upload-sessions/single/init"))
                            .withHeader(
                                    "X-Service-Token",
                                    com.github.tomakehurst.wiremock.client.WireMock.equalTo(
                                            "test-service-token"))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            InitSingleUploadResponse response = uploadSessionApi.initSingle(request);

            // then - 인증 헤더가 올바르게 전송되었음을 확인
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo("auth-session");
        }
    }
}
