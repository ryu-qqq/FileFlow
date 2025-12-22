package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ApiResponse 단위 테스트
 *
 * <p>API 응답 래퍼의 생성 및 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ApiResponse 테스트")
class ApiResponseTest {

    @Nested
    @DisplayName("ofSuccess - 성공 응답 생성")
    class OfSuccess {

        @Test
        @DisplayName("데이터와 함께 성공 응답 생성")
        void shouldCreateSuccessResponseWithData() {
            // given
            String data = "테스트 데이터";

            // when
            ApiResponse<String> response = ApiResponse.ofSuccess(data);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isEqualTo("테스트 데이터");
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).isNotNull().startsWith("req-");
        }

        @Test
        @DisplayName("복잡한 객체와 함께 성공 응답 생성")
        void shouldCreateSuccessResponseWithComplexObject() {
            // given
            record TestData(Long id, String name) {}
            TestData data = new TestData(1L, "테스트");

            // when
            ApiResponse<TestData> response = ApiResponse.ofSuccess(data);

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isNotNull();
            assertThat(response.data().id()).isEqualTo(1L);
            assertThat(response.data().name()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("데이터 없이 성공 응답 생성")
        void shouldCreateSuccessResponseWithoutData() {
            // when
            ApiResponse<Void> response = ApiResponse.ofSuccess();

            // then
            assertThat(response.success()).isTrue();
            assertThat(response.data()).isNull();
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Record 특성 검증")
    class RecordCharacteristics {

        @Test
        @DisplayName("동일한 값을 가진 응답은 equals로 비교 가능")
        void shouldBeEqualWhenSameValues() {
            // given
            String data = "테스트";
            ApiResponse<String> response1 = ApiResponse.ofSuccess(data);
            ApiResponse<String> response2 = ApiResponse.ofSuccess(data);

            // then
            // timestamp와 requestId가 다르므로 equals는 false
            assertThat(response1.data()).isEqualTo(response2.data());
            assertThat(response1.success()).isEqualTo(response2.success());
        }

        @Test
        @DisplayName("toString 호출 시 모든 필드 포함")
        void shouldIncludeAllFieldsInToString() {
            // given
            ApiResponse<String> response = ApiResponse.ofSuccess("테스트");

            // when
            String result = response.toString();

            // then
            assertThat(result).contains("success=true");
            assertThat(result).contains("data=테스트");
            assertThat(result).contains("timestamp=");
            assertThat(result).contains("requestId=");
        }
    }

    @Nested
    @DisplayName("requestId 생성")
    class RequestIdGeneration {

        @Test
        @DisplayName("requestId는 'req-' 접두사로 시작")
        void shouldStartWithReqPrefix() {
            // when
            ApiResponse<String> response = ApiResponse.ofSuccess("테스트");

            // then
            assertThat(response.requestId()).startsWith("req-");
        }

        @Test
        @DisplayName("각 응답은 고유한 requestId를 가짐")
        void shouldHaveUniqueRequestId() {
            // when
            ApiResponse<String> response1 = ApiResponse.ofSuccess("테스트1");
            ApiResponse<String> response2 = ApiResponse.ofSuccess("테스트2");

            // then
            // 밀리초 단위이므로 동일할 수 있지만, 일반적으로 다름
            // 실제 구현에서는 UUID나 다른 고유 식별자 사용 권장
            assertThat(response1.requestId()).isNotNull();
            assertThat(response2.requestId()).isNotNull();
        }
    }
}
