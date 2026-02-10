package com.ryuqq.fileflow.adapter.out.client.http.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Tag("unit")
@DisplayName("CallbackNotificationHttpClient 단위 테스트")
class CallbackNotificationHttpClientTest {

    private RestClient restClient;
    private CallbackNotificationHttpClient sut;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        sut = new CallbackNotificationHttpClient(restClient);
    }

    @Nested
    @DisplayName("notify 메서드")
    class Notify {

        @Test
        @DisplayName("성공: 콜백 URL로 POST 요청을 전송한다")
        void shouldSendPostRequestToCallbackUrl() {
            // given
            String callbackUrl = "https://example.com/webhook/download";
            String downloadTaskId = "task-001";
            String status = "COMPLETED";

            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            RestClient.RequestBodySpec bodySpec =
                    mock(RestClient.RequestBodySpec.class, org.mockito.Answers.RETURNS_SELF);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            given(restClient.post()).willReturn(bodyUriSpec);
            given(bodyUriSpec.uri(any(URI.class))).willReturn(bodySpec);
            given(bodySpec.retrieve()).willReturn(responseSpec);
            given(responseSpec.toBodilessEntity()).willReturn(ResponseEntity.ok().build());

            // when
            sut.notify(callbackUrl, downloadTaskId, status);

            // then
            verify(restClient).post();
            verify(bodyUriSpec).uri(URI.create(callbackUrl));
        }

        @Test
        @DisplayName("실패: HTTP 요청 실패 시 예외가 전파된다")
        void shouldPropagateExceptionOnHttpFailure() {
            // given
            String callbackUrl = "https://example.com/webhook/download";

            RestClient.RequestBodyUriSpec bodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);

            given(restClient.post()).willReturn(bodyUriSpec);
            given(bodyUriSpec.uri(any(URI.class)))
                    .willThrow(new RestClientException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> sut.notify(callbackUrl, "task-001", "COMPLETED"))
                    .isInstanceOf(RestClientException.class)
                    .hasMessageContaining("Connection refused");
        }
    }
}
