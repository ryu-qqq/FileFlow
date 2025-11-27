package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ApiResponse 단위 테스트")
class ApiResponseTest {

    @Nested
    @DisplayName("성공 응답 생성 테스트")
    class SuccessResponseTest {

        @Test
        @DisplayName("데이터와 함께 성공 응답을 생성할 수 있다")
        void ofSuccess_WithData_ShouldCreateSuccessResponse() {
            // given
            String data = "test data";

            // when
            ApiResponse<String> response = ApiResponse.ofSuccess(data);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isEqualTo(data);
            assertThat(response.error()).isNull();
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).startsWith("req-");
        }

        @Test
        @DisplayName("데이터 없이 성공 응답을 생성할 수 있다")
        void ofSuccess_WithoutData_ShouldCreateSuccessResponse() {
            // when
            ApiResponse<Void> response = ApiResponse.ofSuccess();

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isNull();
            assertThat(response.error()).isNull();
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).startsWith("req-");
        }

        @Test
        @DisplayName("복잡한 객체로 성공 응답을 생성할 수 있다")
        void ofSuccess_WithComplexObject_ShouldCreateSuccessResponse() {
            // given
            record TestDto(String name, int value) {}
            TestDto data = new TestDto("test", 123);

            // when
            ApiResponse<TestDto> response = ApiResponse.ofSuccess(data);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isEqualTo(data);
            assertThat(response.data().name()).isEqualTo("test");
            assertThat(response.data().value()).isEqualTo(123);
        }
    }

    @Nested
    @DisplayName("실패 응답 생성 테스트")
    class FailureResponseTest {

        @Test
        @DisplayName("ErrorInfo로 실패 응답을 생성할 수 있다")
        void ofFailure_WithErrorInfo_ShouldCreateFailureResponse() {
            // given
            ErrorInfo error = new ErrorInfo("TEST_ERROR", "테스트 에러입니다");

            // when
            ApiResponse<Void> response = ApiResponse.ofFailure(error);

            // then
            assertThat(response.success()).isFalse();
            assertThat(response.data()).isNull();
            assertThat(response.error()).isEqualTo(error);
            assertThat(response.error().errorCode()).isEqualTo("TEST_ERROR");
            assertThat(response.error().message()).isEqualTo("테스트 에러입니다");
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).startsWith("req-");
        }

        @Test
        @DisplayName("에러 코드와 메시지로 실패 응답을 생성할 수 있다")
        void ofFailure_WithCodeAndMessage_ShouldCreateFailureResponse() {
            // given
            String errorCode = "VALIDATION_ERROR";
            String message = "검증 실패";

            // when
            ApiResponse<Void> response = ApiResponse.ofFailure(errorCode, message);

            // then
            assertThat(response.success()).isFalse();
            assertThat(response.data()).isNull();
            assertThat(response.error()).isNotNull();
            assertThat(response.error().errorCode()).isEqualTo(errorCode);
            assertThat(response.error().message()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("requestId 생성 테스트")
    class RequestIdTest {

        @Test
        @DisplayName("requestId는 req- 접두사로 시작한다")
        void requestId_ShouldStartWithReqPrefix() {
            // when
            ApiResponse<Void> response = ApiResponse.ofSuccess();

            // then
            assertThat(response.requestId()).startsWith("req-");
        }

        @Test
        @DisplayName("각 응답마다 다른 requestId가 생성된다")
        void requestId_ShouldBeUniqueForEachResponse() throws InterruptedException {
            // when
            ApiResponse<Void> response1 = ApiResponse.ofSuccess();
            Thread.sleep(1); // 최소 1ms 대기하여 timestamp가 다르도록 보장
            ApiResponse<Void> response2 = ApiResponse.ofSuccess();

            // then
            assertThat(response1.requestId()).isNotEqualTo(response2.requestId());
        }
    }

    @Nested
    @DisplayName("timestamp 테스트")
    class TimestampTest {

        @Test
        @DisplayName("timestamp는 현재 시각으로 설정된다")
        void timestamp_ShouldBeSetToCurrentTime() {
            // when
            ApiResponse<Void> response = ApiResponse.ofSuccess();

            // then
            assertThat(response.timestamp()).isNotNull();
        }
    }
}
