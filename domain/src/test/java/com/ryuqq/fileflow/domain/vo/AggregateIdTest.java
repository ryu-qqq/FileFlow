package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AggregateId Value Object 테스트
 */
class AggregateIdTest {

    @Test
    @DisplayName("유효한 ID 값으로 AggregateId를 생성해야 한다")
    void shouldCreateValidAggregateId() {
        // given
        String validId = "file-uuid-v7-123";

        // when
        AggregateId aggregateId = AggregateId.of(validId);

        // then
        assertThat(aggregateId).isNotNull();
        assertThat(aggregateId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // given
        String nullId = null;

        // when & then
        assertThatThrownBy(() -> AggregateId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aggregate ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsBlank() {
        // given
        String blankId = "   ";

        // when & then
        assertThatThrownBy(() -> AggregateId.of(blankId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aggregate ID는 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "file-uuid-v7-123";
        AggregateId aggregateId = AggregateId.of(expectedValue);

        // when
        String actualValue = aggregateId.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 AggregateId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String id = "file-uuid-v7-123";
        AggregateId aggregateId1 = AggregateId.of(id);
        AggregateId aggregateId2 = AggregateId.of(id);

        // when & then
        assertThat(aggregateId1).isEqualTo(aggregateId2);
    }

    @Test
    @DisplayName("같은 값을 가진 AggregateId는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String id = "file-uuid-v7-123";
        AggregateId aggregateId1 = AggregateId.of(id);
        AggregateId aggregateId2 = AggregateId.of(id);

        // when & then
        assertThat(aggregateId1.hashCode()).isEqualTo(aggregateId2.hashCode());
    }

    @Test
    @DisplayName("forNew()는 null 값을 가진 AggregateId를 생성해야 한다")
    void shouldCreateNullAggregateIdWithForNew() {
        // when
        AggregateId aggregateId = AggregateId.forNew();

        // then
        assertThat(aggregateId).isNotNull();
        assertThat(aggregateId.getValue()).isNull();
        assertThat(aggregateId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 null인 경우 true를 반환해야 한다")
    void shouldReturnTrueWhenIsNewForNullValue() {
        // given
        AggregateId aggregateId = AggregateId.forNew();

        // when & then
        assertThat(aggregateId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 있는 경우 false를 반환해야 한다")
    void shouldReturnFalseWhenIsNewForNonNullValue() {
        // given
        AggregateId aggregateId = AggregateId.of("file-uuid-v7-123");

        // when & then
        assertThat(aggregateId.isNew()).isFalse();
    }
}
