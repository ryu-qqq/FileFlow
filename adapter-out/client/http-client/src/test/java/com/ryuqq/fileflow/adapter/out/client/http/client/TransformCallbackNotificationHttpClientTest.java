package com.ryuqq.fileflow.adapter.out.client.http.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Tag("unit")
@DisplayName("TransformCallbackNotificationHttpClient 단위 테스트")
class TransformCallbackNotificationHttpClientTest {

    private RestClient restClient;
    private TransformCallbackNotificationHttpClient sut;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        sut = new TransformCallbackNotificationHttpClient(restClient);
    }

    @Nested
    @DisplayName("notify 메서드")
    class Notify {

        @Test
        @DisplayName("성공: 콜백 URL로 POST 요청을 전송한다")
        void shouldSendPostRequestToCallbackUrl() {
            // given
            String callbackUrl = "https://example.com/webhook/transform";
            TransformCallbackPayload payload =
                    TransformCallbackPayload.ofCompleted(
                            "request-001", "asset-001", "asset-002", "RESIZE", 800, 600, 85, "jpg");

            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec =
                    mock(RestClient.RequestBodySpec.class, org.mockito.Answers.RETURNS_SELF);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            given(restClient.post()).willReturn(bodyUriSpec);
            given(bodyUriSpec.uri(any(URI.class))).willReturn(bodySpec);
            given(bodySpec.retrieve()).willReturn(responseSpec);
            given(responseSpec.toBodilessEntity()).willReturn(ResponseEntity.ok().build());

            // when
            sut.notify(callbackUrl, payload);

            // then
            verify(restClient).post();
            verify(bodyUriSpec).uri(URI.create(callbackUrl));
        }

        @Test
        @DisplayName("실패: 4xx 응답 시 PermanentCallbackFailureException 발생")
        void shouldThrowPermanentFailureOn4xx() {
            // given
            String callbackUrl = "https://example.com/webhook/transform";
            TransformCallbackPayload payload =
                    TransformCallbackPayload.ofCompleted(
                            "request-001", "asset-001", "asset-002", "RESIZE", 800, 600, 85, "jpg");

            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec =
                    mock(RestClient.RequestBodySpec.class, org.mockito.Answers.RETURNS_SELF);

            given(restClient.post()).willReturn(bodyUriSpec);
            given(bodyUriSpec.uri(any(URI.class))).willReturn(bodySpec);
            given(bodySpec.retrieve())
                    .willThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            // when & then
            assertThatThrownBy(() -> sut.notify(callbackUrl, payload))
                    .isInstanceOf(PermanentCallbackFailureException.class);
        }

        @Test
        @DisplayName("실패: 네트워크 오류 시 예외가 그대로 전파된다")
        void shouldPropagateExceptionOnNetworkError() {
            // given
            String callbackUrl = "https://example.com/webhook/transform";
            TransformCallbackPayload payload =
                    TransformCallbackPayload.ofFailed(
                            "request-001", "asset-001", "Connection timeout");

            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);

            given(restClient.post()).willReturn(bodyUriSpec);
            given(bodyUriSpec.uri(any(URI.class)))
                    .willThrow(new RestClientException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> sut.notify(callbackUrl, payload))
                    .isInstanceOf(RestClientException.class);
        }
    }
}
