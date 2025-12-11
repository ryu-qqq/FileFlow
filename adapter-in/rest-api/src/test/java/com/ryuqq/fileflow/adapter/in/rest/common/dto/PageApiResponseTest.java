package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PageApiResponse 단위 테스트
 *
 * <p>페이지 조회 REST API 응답 DTO의 생성 및 변환을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("PageApiResponse 테스트")
class PageApiResponseTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("모든 필드를 포함하는 PageApiResponse 생성")
        void shouldCreateWithAllFields() {
            // given
            List<String> content = List.of("item1", "item2", "item3");

            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(content, 0, 20, 100L, 5, true, false);

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(100L);
            assertThat(response.totalPages()).isEqualTo(5);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("content는 방어적 복사로 불변성 보장")
        void shouldDefensivelyCopyContent() {
            // given
            List<String> original = new ArrayList<>(List.of("item1", "item2"));

            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(original, 0, 20, 2L, 1, true, true);
            original.add("item3");

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(original).hasSize(3);
        }

        @Test
        @DisplayName("반환된 content는 수정 불가")
        void shouldReturnUnmodifiableContent() {
            // given
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item1"), 0, 20, 1L, 1, true, true);

            // when & then
            assertThatThrownBy(() -> response.content().add("item2"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("from - PageResponse 변환")
    class FromPageResponse {

        @Test
        @DisplayName("PageResponse를 PageApiResponse로 변환")
        void shouldConvertFromPageResponse() {
            // given
            PageResponse<String> appResponse =
                    PageResponse.of(List.of("item1", "item2"), 0, 20, 50L, 3, true, false);

            // when
            PageApiResponse<String> apiResponse = PageApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).containsExactly("item1", "item2");
            assertThat(apiResponse.page()).isZero();
            assertThat(apiResponse.size()).isEqualTo(20);
            assertThat(apiResponse.totalElements()).isEqualTo(50L);
            assertThat(apiResponse.totalPages()).isEqualTo(3);
            assertThat(apiResponse.first()).isTrue();
            assertThat(apiResponse.last()).isFalse();
        }

        @Test
        @DisplayName("빈 PageResponse를 변환")
        void shouldConvertEmptyPageResponse() {
            // given
            PageResponse<String> appResponse = PageResponse.empty(0, 20);

            // when
            PageApiResponse<String> apiResponse = PageApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).isEmpty();
            assertThat(apiResponse.page()).isZero();
            assertThat(apiResponse.size()).isEqualTo(20);
            assertThat(apiResponse.totalElements()).isZero();
            assertThat(apiResponse.totalPages()).isZero();
            assertThat(apiResponse.first()).isTrue();
            assertThat(apiResponse.last()).isTrue();
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

            PageResponse<AppDto> appResponse =
                    PageResponse.of(
                            List.of(new AppDto(1L, "이름1"), new AppDto(2L, "이름2")),
                            0,
                            20,
                            2L,
                            1,
                            true,
                            true);

            // when
            PageApiResponse<ApiDto> apiResponse =
                    PageApiResponse.from(
                            appResponse, dto -> new ApiDto(dto.id(), "[표시] " + dto.name()));

            // then
            assertThat(apiResponse.content()).hasSize(2);
            assertThat(apiResponse.content().get(0).displayName()).isEqualTo("[표시] 이름1");
            assertThat(apiResponse.content().get(1).displayName()).isEqualTo("[표시] 이름2");
        }

        @Test
        @DisplayName("빈 PageResponse에 매퍼 함수 적용")
        void shouldApplyMapperToEmptyPageResponse() {
            // given
            PageResponse<String> appResponse = PageResponse.empty(0, 20);

            // when
            PageApiResponse<Integer> apiResponse =
                    PageApiResponse.from(appResponse, String::length);

            // then
            assertThat(apiResponse.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이지네이션 플래그 검증")
    class PaginationFlags {

        @Test
        @DisplayName("첫 페이지 - first=true, last=false")
        void shouldBeFirstPage() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 0, 20, 100L, 5, true, false);

            // then
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("마지막 페이지 - first=false, last=true")
        void shouldBeLastPage() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 4, 20, 100L, 5, false, true);

            // then
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isTrue();
        }

        @Test
        @DisplayName("단일 페이지 - first=true, last=true")
        void shouldBeSinglePage() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 0, 20, 5L, 1, true, true);

            // then
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }

        @Test
        @DisplayName("중간 페이지 - first=false, last=false")
        void shouldBeMiddlePage() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 2, 20, 100L, 5, false, false);

            // then
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record 특성 검증")
    class RecordCharacteristics {

        @Test
        @DisplayName("동일한 값을 가진 PageApiResponse는 equals로 동등")
        void shouldBeEqualWhenSameValues() {
            // given
            List<String> content = List.of("item1", "item2");
            PageApiResponse<String> response1 =
                    new PageApiResponse<>(content, 0, 20, 50L, 3, true, false);
            PageApiResponse<String> response2 =
                    new PageApiResponse<>(content, 0, 20, 50L, 3, true, false);

            // then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("toString 호출 시 모든 필드 포함")
        void shouldIncludeAllFieldsInToString() {
            // given
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item1"), 0, 20, 100L, 5, true, false);

            // when
            String result = response.toString();

            // then
            assertThat(result).contains("content=");
            assertThat(result).contains("page=0");
            assertThat(result).contains("size=20");
            assertThat(result).contains("totalElements=100");
            assertThat(result).contains("totalPages=5");
            assertThat(result).contains("first=true");
            assertThat(result).contains("last=false");
        }
    }
}
