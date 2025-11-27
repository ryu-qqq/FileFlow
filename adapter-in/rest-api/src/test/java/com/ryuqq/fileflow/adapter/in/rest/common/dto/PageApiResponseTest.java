package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PageApiResponse 단위 테스트")
class PageApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드를 가진 PageApiResponse를 생성할 수 있다")
        void constructor_WithAllFields_ShouldCreateResponse() {
            // given
            List<String> content = List.of("item1", "item2", "item3");
            int page = 0;
            int size = 20;
            long totalElements = 100;
            int totalPages = 5;
            boolean first = true;
            boolean last = false;

            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(
                            content, page, size, totalElements, totalPages, first, last);

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.page()).isEqualTo(page);
            assertThat(response.size()).isEqualTo(size);
            assertThat(response.totalElements()).isEqualTo(totalElements);
            assertThat(response.totalPages()).isEqualTo(totalPages);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("빈 콘텐츠로도 PageApiResponse를 생성할 수 있다")
        void constructor_WithEmptyContent_ShouldCreateResponse() {
            // given
            List<String> content = List.of();

            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(content, 0, 20, 0, 0, true, true);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("content는 불변 복사본으로 저장된다")
        void content_ShouldBeImmutableCopy() {
            // given
            List<String> originalContent = new java.util.ArrayList<>();
            originalContent.add("item1");
            originalContent.add("item2");

            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(originalContent, 0, 20, 2, 1, true, true);

            // then - 원본 변경해도 response의 content는 영향 없음
            originalContent.add("item3");
            assertThat(response.content()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("from 메서드 테스트")
    class FromMethodTest {

        @Test
        @DisplayName("PageResponse로부터 PageApiResponse를 생성할 수 있다")
        void from_WithPageResponse_ShouldCreatePageApiResponse() {
            // given
            List<String> content = List.of("item1", "item2");
            PageResponse<String> appResponse =
                    new PageResponse<>(content, 1, 10, 25, 3, false, false);

            // when
            PageApiResponse<String> apiResponse = PageApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).isEqualTo(content);
            assertThat(apiResponse.page()).isEqualTo(1);
            assertThat(apiResponse.size()).isEqualTo(10);
            assertThat(apiResponse.totalElements()).isEqualTo(25);
            assertThat(apiResponse.totalPages()).isEqualTo(3);
            assertThat(apiResponse.first()).isFalse();
            assertThat(apiResponse.last()).isFalse();
        }

        @Test
        @DisplayName("매퍼 함수와 함께 PageResponse로부터 변환할 수 있다")
        void from_WithMapper_ShouldTransformContent() {
            // given
            record SourceDto(int id, String name) {}
            record TargetDto(String formattedName) {}

            List<SourceDto> sourceContent =
                    List.of(new SourceDto(1, "Alpha"), new SourceDto(2, "Beta"));

            PageResponse<SourceDto> appResponse =
                    new PageResponse<>(sourceContent, 0, 10, 2, 1, true, true);

            // when
            PageApiResponse<TargetDto> apiResponse =
                    PageApiResponse.from(appResponse, src -> new TargetDto("Name: " + src.name()));

            // then
            assertThat(apiResponse.content()).hasSize(2);
            assertThat(apiResponse.content().get(0).formattedName()).isEqualTo("Name: Alpha");
            assertThat(apiResponse.content().get(1).formattedName()).isEqualTo("Name: Beta");
            assertThat(apiResponse.page()).isEqualTo(0);
            assertThat(apiResponse.totalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("페이지 상태 테스트")
    class PageStateTest {

        @Test
        @DisplayName("첫 페이지 상태를 확인할 수 있다")
        void firstPage_ShouldHaveCorrectState() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 0, 10, 50, 5, true, false);

            // then
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();
            assertThat(response.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("마지막 페이지 상태를 확인할 수 있다")
        void lastPage_ShouldHaveCorrectState() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 4, 10, 50, 5, false, true);

            // then
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isTrue();
            assertThat(response.page()).isEqualTo(4);
        }

        @Test
        @DisplayName("단일 페이지면 first와 last 모두 true이다")
        void singlePage_ShouldBeFirstAndLast() {
            // when
            PageApiResponse<String> response =
                    new PageApiResponse<>(List.of("item"), 0, 10, 1, 1, true, true);

            // then
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 PageApiResponse는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            List<String> content = List.of("item1", "item2");
            PageApiResponse<String> response1 =
                    new PageApiResponse<>(content, 0, 10, 2, 1, true, true);
            PageApiResponse<String> response2 =
                    new PageApiResponse<>(content, 0, 10, 2, 1, true, true);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }
}
