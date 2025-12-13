package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SliceApiResponse 단위 테스트
 *
 * <p>슬라이스 조회 REST API 응답 DTO의 생성 및 변환을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SliceApiResponse 테스트")
class SliceApiResponseTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("모든 필드를 포함하는 SliceApiResponse 생성")
        void shouldCreateWithAllFields() {
            // given
            List<String> content = List.of("item1", "item2", "item3");

            // when
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(content, 20, true, "cursor-xyz");

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("cursor-xyz");
        }

        @Test
        @DisplayName("nextCursor가 null인 SliceApiResponse 생성")
        void shouldCreateWithNullCursor() {
            // given
            List<String> content = List.of("item1");

            // when
            SliceApiResponse<String> response = new SliceApiResponse<>(content, 20, false, null);

            // then
            assertThat(response.nextCursor()).isNull();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        @DisplayName("content는 방어적 복사로 불변성 보장")
        void shouldDefensivelyCopyContent() {
            // given
            List<String> original = new ArrayList<>(List.of("item1", "item2"));

            // when
            SliceApiResponse<String> response = new SliceApiResponse<>(original, 20, false, null);
            original.add("item3");

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(original).hasSize(3);
        }

        @Test
        @DisplayName("반환된 content는 수정 불가")
        void shouldReturnUnmodifiableContent() {
            // given
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(List.of("item1"), 20, false, null);

            // when & then
            assertThatThrownBy(() -> response.content().add("item2"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("from - SliceResponse 변환")
    class FromSliceResponse {

        @Test
        @DisplayName("SliceResponse를 SliceApiResponse로 변환")
        void shouldConvertFromSliceResponse() {
            // given
            SliceResponse<String> appResponse =
                    SliceResponse.of(List.of("item1", "item2"), 20, true, "next-cursor");

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).containsExactly("item1", "item2");
            assertThat(apiResponse.size()).isEqualTo(20);
            assertThat(apiResponse.hasNext()).isTrue();
            assertThat(apiResponse.nextCursor()).isEqualTo("next-cursor");
        }

        @Test
        @DisplayName("hasNext=false인 SliceResponse 변환")
        void shouldConvertSliceResponseWithNoNext() {
            // given
            SliceResponse<String> appResponse = SliceResponse.of(List.of("item1"), 20, false, null);

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.hasNext()).isFalse();
            assertThat(apiResponse.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 SliceResponse를 변환")
        void shouldConvertEmptySliceResponse() {
            // given
            SliceResponse<String> appResponse = SliceResponse.empty(20);

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).isEmpty();
            assertThat(apiResponse.size()).isEqualTo(20);
            assertThat(apiResponse.hasNext()).isFalse();
            assertThat(apiResponse.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("from with mapper - 매퍼 함수 적용")
    class FromWithMapper {

        @Test
        @DisplayName("매퍼 함수를 적용하여 변환")
        void shouldApplyMapperFunction() {
            // given
            record AppDto(Long id, String name) {}
            record ApiDto(Long id, String displayName) {}

            SliceResponse<AppDto> appResponse =
                    SliceResponse.of(
                            List.of(new AppDto(1L, "이름1"), new AppDto(2L, "이름2")),
                            20,
                            true,
                            "cursor-123");

            // when
            SliceApiResponse<ApiDto> apiResponse =
                    SliceApiResponse.from(
                            appResponse, dto -> new ApiDto(dto.id(), "[표시] " + dto.name()));

            // then
            assertThat(apiResponse.content()).hasSize(2);
            assertThat(apiResponse.content().get(0).displayName()).isEqualTo("[표시] 이름1");
            assertThat(apiResponse.content().get(1).displayName()).isEqualTo("[표시] 이름2");
            assertThat(apiResponse.nextCursor()).isEqualTo("cursor-123");
        }

        @Test
        @DisplayName("빈 SliceResponse에 매퍼 함수 적용")
        void shouldApplyMapperToEmptySliceResponse() {
            // given
            SliceResponse<String> appResponse = SliceResponse.empty(20);

            // when
            SliceApiResponse<Integer> apiResponse =
                    SliceApiResponse.from(appResponse, String::length);

            // then
            assertThat(apiResponse.content()).isEmpty();
            assertThat(apiResponse.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("무한 스크롤 시나리오")
    class InfiniteScrollScenario {

        @Test
        @DisplayName("첫 슬라이스 - 다음 페이지 있음")
        void shouldHaveNextForFirstSlice() {
            // given
            SliceResponse<String> appResponse =
                    SliceResponse.of(List.of("item1", "item2"), 20, true, "cursor-1");

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.hasNext()).isTrue();
            assertThat(apiResponse.nextCursor()).isNotNull();
        }

        @Test
        @DisplayName("마지막 슬라이스 - 다음 페이지 없음")
        void shouldNotHaveNextForLastSlice() {
            // given
            SliceResponse<String> appResponse = SliceResponse.of(List.of("lastItem"), 20, false);

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.hasNext()).isFalse();
            assertThat(apiResponse.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("Record 특성 검증")
    class RecordCharacteristics {

        @Test
        @DisplayName("동일한 값을 가진 SliceApiResponse는 equals로 동등")
        void shouldBeEqualWhenSameValues() {
            // given
            List<String> content = List.of("item1", "item2");
            SliceApiResponse<String> response1 =
                    new SliceApiResponse<>(content, 20, true, "cursor");
            SliceApiResponse<String> response2 =
                    new SliceApiResponse<>(content, 20, true, "cursor");

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("toString 호출 시 모든 필드 포함")
        void shouldIncludeAllFieldsInToString() {
            // given
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(List.of("item1"), 20, true, "cursor-abc");

            // when
            String result = response.toString();

            // then
            assertThat(result).contains("content=");
            assertThat(result).contains("size=20");
            assertThat(result).contains("hasNext=true");
            assertThat(result).contains("nextCursor=cursor-abc");
        }
    }
}
