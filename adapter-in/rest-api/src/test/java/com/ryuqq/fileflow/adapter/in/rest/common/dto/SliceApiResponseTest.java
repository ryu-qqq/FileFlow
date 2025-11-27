package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.common.dto.response.SliceResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SliceApiResponse 단위 테스트")
class SliceApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드를 가진 SliceApiResponse를 생성할 수 있다")
        void constructor_WithAllFields_ShouldCreateResponse() {
            // given
            List<String> content = List.of("item1", "item2", "item3");
            int size = 20;
            boolean hasNext = true;
            String nextCursor = "cursor-abc123";

            // when
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(content, size, hasNext, nextCursor);

            // then
            assertThat(response.content()).hasSize(3);
            assertThat(response.size()).isEqualTo(size);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo(nextCursor);
        }

        @Test
        @DisplayName("다음 페이지가 없으면 hasNext는 false이다")
        void constructor_WithoutNextPage_ShouldHaveNextFalse() {
            // given
            List<String> content = List.of("item1", "item2");

            // when
            SliceApiResponse<String> response = new SliceApiResponse<>(content, 20, false, null);

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 콘텐츠로도 SliceApiResponse를 생성할 수 있다")
        void constructor_WithEmptyContent_ShouldCreateResponse() {
            // given
            List<String> content = List.of();

            // when
            SliceApiResponse<String> response = new SliceApiResponse<>(content, 20, false, null);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.hasNext()).isFalse();
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
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(originalContent, 20, false, null);

            // then - 원본 변경해도 response의 content는 영향 없음
            originalContent.add("item3");
            assertThat(response.content()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("from 메서드 테스트")
    class FromMethodTest {

        @Test
        @DisplayName("SliceResponse로부터 SliceApiResponse를 생성할 수 있다")
        void from_WithSliceResponse_ShouldCreateSliceApiResponse() {
            // given
            List<String> content = List.of("item1", "item2");
            SliceResponse<String> appResponse =
                    new SliceResponse<>(content, 10, true, "next-cursor-xyz");

            // when
            SliceApiResponse<String> apiResponse = SliceApiResponse.from(appResponse);

            // then
            assertThat(apiResponse.content()).isEqualTo(content);
            assertThat(apiResponse.size()).isEqualTo(10);
            assertThat(apiResponse.hasNext()).isTrue();
            assertThat(apiResponse.nextCursor()).isEqualTo("next-cursor-xyz");
        }

        @Test
        @DisplayName("매퍼 함수와 함께 SliceResponse로부터 변환할 수 있다")
        void from_WithMapper_ShouldTransformContent() {
            // given
            record SourceDto(int id, String name) {}
            record TargetDto(String formattedName) {}

            List<SourceDto> sourceContent =
                    List.of(new SourceDto(1, "Alpha"), new SourceDto(2, "Beta"));

            SliceResponse<SourceDto> appResponse =
                    new SliceResponse<>(sourceContent, 10, true, "cursor-123");

            // when
            SliceApiResponse<TargetDto> apiResponse =
                    SliceApiResponse.from(appResponse, src -> new TargetDto("Item: " + src.name()));

            // then
            assertThat(apiResponse.content()).hasSize(2);
            assertThat(apiResponse.content().get(0).formattedName()).isEqualTo("Item: Alpha");
            assertThat(apiResponse.content().get(1).formattedName()).isEqualTo("Item: Beta");
            assertThat(apiResponse.hasNext()).isTrue();
            assertThat(apiResponse.nextCursor()).isEqualTo("cursor-123");
        }
    }

    @Nested
    @DisplayName("커서 테스트")
    class CursorTest {

        @Test
        @DisplayName("다음 페이지가 있으면 nextCursor가 존재한다")
        void hasNext_WithCursor_ShouldReturnCursor() {
            // when
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(List.of("item"), 10, true, "abc123");

            // then
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("abc123");
        }

        @Test
        @DisplayName("마지막 슬라이스면 nextCursor는 null이다")
        void lastSlice_ShouldHaveNullCursor() {
            // when
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(List.of("item"), 10, false, null);

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 SliceApiResponse는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            List<String> content = List.of("item1", "item2");
            SliceApiResponse<String> response1 =
                    new SliceApiResponse<>(content, 10, true, "cursor");
            SliceApiResponse<String> response2 =
                    new SliceApiResponse<>(content, 10, true, "cursor");

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 커서를 가진 SliceApiResponse는 동등하지 않다")
        void equals_WithDifferentCursor_ShouldNotBeEqual() {
            // given
            List<String> content = List.of("item");
            SliceApiResponse<String> response1 =
                    new SliceApiResponse<>(content, 10, true, "cursor1");
            SliceApiResponse<String> response2 =
                    new SliceApiResponse<>(content, 10, true, "cursor2");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
