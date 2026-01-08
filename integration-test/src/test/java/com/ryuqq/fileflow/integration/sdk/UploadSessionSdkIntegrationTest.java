package com.ryuqq.fileflow.integration.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadRequest;
import com.ryuqq.fileflow.sdk.model.session.InitSingleUploadResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionSearchRequest;
import com.ryuqq.fileflow.sdk.model.session.UploadSessionSearchRequest.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * UploadSessionApi SDK 통합 테스트.
 *
 * <p>실제 서버와 통신하여 SDK의 UploadSession 기능을 검증합니다.
 */
@DisplayName("UploadSessionApi SDK 통합 테스트")
class UploadSessionSdkIntegrationTest extends SdkIntegrationTest {

    private UploadSessionApi uploadSessionApi;

    @BeforeEach
    void setUp() {
        uploadSessionApi = fileFlowClient.uploadSessions();
    }

    @Nested
    @DisplayName("initSingle 메서드")
    class InitSingleTest {

        @Test
        @DisplayName("단일 파일 업로드 세션을 초기화할 수 있다")
        void shouldInitSingleUploadSession() {
            // given
            InitSingleUploadRequest request = InitSingleUploadRequest.builder()
                    .filename("test-file.pdf")
                    .fileSize(1024L)
                    .contentType("application/pdf")
                    .category("DOCUMENT")
                    .build();

            // when
            InitSingleUploadResponse response = uploadSessionApi.initSingle(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isNotBlank();
            assertThat(response.getPresignedUrl()).isNotBlank();
            assertThat(response.getExpiresAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("업로드 세션 목록을 조회할 수 있다")
        void shouldListUploadSessions() {
            // given - 테스트 데이터 생성
            uploadSessionApi.initSingle(
                    InitSingleUploadRequest.builder()
                            .filename("list-test-1.pdf")
                            .fileSize(1024L)
                            .contentType("application/pdf")
                            .category("DOCUMENT")
                            .build()
            );
            uploadSessionApi.initSingle(
                    InitSingleUploadRequest.builder()
                            .filename("list-test-2.jpg")
                            .fileSize(2048L)
                            .contentType("image/jpeg")
                            .category("IMAGE")
                            .build()
            );

            // when
            UploadSessionSearchRequest request = UploadSessionSearchRequest.builder()
                    .page(0)
                    .size(10)
                    .build();

            PageResponse<UploadSessionResponse> response = uploadSessionApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("상태 필터로 업로드 세션을 조회할 수 있다")
        void shouldListUploadSessionsWithStatusFilter() {
            // given - 테스트 데이터 생성
            uploadSessionApi.initSingle(
                    InitSingleUploadRequest.builder()
                            .filename("pending-test.pdf")
                            .fileSize(1024L)
                            .contentType("application/pdf")
                            .category("DOCUMENT")
                            .build()
            );

            // when
            UploadSessionSearchRequest request = UploadSessionSearchRequest.builder()
                    .page(0)
                    .size(10)
                    .status(SessionStatus.PENDING)
                    .build();

            PageResponse<UploadSessionResponse> response = uploadSessionApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).allSatisfy(session ->
                    assertThat(session.getStatus()).isEqualTo("PENDING")
            );
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("세션 ID로 업로드 세션을 조회할 수 있다")
        void shouldGetUploadSessionById() {
            // given
            InitSingleUploadResponse initResponse = uploadSessionApi.initSingle(
                    InitSingleUploadRequest.builder()
                            .filename("get-test.pdf")
                            .fileSize(1024L)
                            .contentType("application/pdf")
                            .category("DOCUMENT")
                            .build()
            );

            // when
            var detailResponse = uploadSessionApi.get(initResponse.getSessionId());

            // then
            assertThat(detailResponse).isNotNull();
            assertThat(detailResponse.getSessionId()).isEqualTo(initResponse.getSessionId());
            assertThat(detailResponse.getFileName()).isEqualTo("get-test.pdf");
            assertThat(detailResponse.getFileSize()).isEqualTo(1024L);
            assertThat(detailResponse.getContentType()).isEqualTo("application/pdf");
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class CancelTest {

        @Test
        @DisplayName("업로드 세션을 취소할 수 있다")
        void shouldCancelUploadSession() {
            // given
            InitSingleUploadResponse initResponse = uploadSessionApi.initSingle(
                    InitSingleUploadRequest.builder()
                            .filename("cancel-test.pdf")
                            .fileSize(1024L)
                            .contentType("application/pdf")
                            .category("DOCUMENT")
                            .build()
            );

            // when
            uploadSessionApi.cancel(initResponse.getSessionId());

            // then
            var detailResponse = uploadSessionApi.get(initResponse.getSessionId());
            assertThat(detailResponse.getStatus()).isEqualTo("CANCELLED");
        }
    }
}
