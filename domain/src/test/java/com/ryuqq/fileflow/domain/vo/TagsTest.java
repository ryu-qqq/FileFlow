package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tags Value Object 테스트
 */
class TagsTest {

    @Test
    @DisplayName("유효한 태그 문자열로 Tags를 생성해야 한다")
    void shouldCreateValidTagsFromString() {
        // given
        String validTags = "이미지,상품,썸네일";

        // when
        Tags tags = Tags.of(validTags);

        // then
        assertThat(tags).isNotNull();
        assertThat(tags.getValue()).containsExactly("이미지", "상품", "썸네일");
    }

    @Test
    @DisplayName("유효한 태그 리스트로 Tags를 생성해야 한다")
    void shouldCreateValidTagsFromList() {
        // given
        List<String> validTags = List.of("tag1", "tag2", "tag3");

        // when
        Tags tags = Tags.of(validTags);

        // then
        assertThat(tags).isNotNull();
        assertThat(tags.getValue()).containsExactlyElementsOf(validTags);
    }

    @Test
    @DisplayName("빈 태그 생성 시 빈 리스트를 반환해야 한다")
    void shouldCreateEmptyTags() {
        // when
        Tags tags = Tags.empty();

        // then
        assertThat(tags.getValue()).isEmpty();
    }

    @Test
    @DisplayName("10개를 초과하는 태그는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenTagsExceed10() {
        // given
        List<String> tooManyTags = List.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11");

        // when & then
        assertThatThrownBy(() -> Tags.of(tooManyTags))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("태그는 최대 10개까지 허용됩니다");
    }

    @Test
    @DisplayName("20자를 초과하는 태그는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenTagLengthExceeds20() {
        // given
        String tooLongTag = "a".repeat(21);
        List<String> tags = List.of(tooLongTag);

        // when & then
        assertThatThrownBy(() -> Tags.of(tags))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("각 태그는 최대 20자까지 허용됩니다");
    }

    @Test
    @DisplayName("getTagsAsString()은 쉼표로 구분된 문자열을 반환해야 한다")
    void shouldReturnCommaSeparatedString() {
        // given
        List<String> tagList = List.of("tag1", "tag2", "tag3");
        Tags tags = Tags.of(tagList);

        // when
        String result = tags.getTagsAsString();

        // then
        assertThat(result).isEqualTo("tag1,tag2,tag3");
    }
}
