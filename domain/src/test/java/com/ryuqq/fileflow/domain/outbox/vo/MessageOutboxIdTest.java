package com.ryuqq.fileflow.domain.outbox.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MessageOutboxId Value Object 테스트
 */
class MessageOutboxIdTest {

    @Test
    @DisplayName("유효한 ID 값으로 MessageOutboxId를 생성해야 한다")
    void shouldCreateValidMessageOutboxId() {
        // given
        String validId = "01JCQM5K3P9XYZ123456ABCD";

        // when
        MessageOutboxId messageOutboxId = MessageOutboxId.of(validId);

        // then
        assertThat(messageOutboxId).isNotNull();
        assertThat(messageOutboxId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // given
        String nullId = null;

        // when & then
        assertThatThrownBy(() -> MessageOutboxId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MessageOutbox ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsBlank() {
        // given
        String blankId = "   ";

        // when & then
        assertThatThrownBy(() -> MessageOutboxId.of(blankId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MessageOutbox ID는 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "01JCQM5K3P9XYZ123456ABCD";
        MessageOutboxId messageOutboxId = MessageOutboxId.of(expectedValue);

        // when
        String actualValue = messageOutboxId.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 MessageOutboxId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        MessageOutboxId messageOutboxId1 = MessageOutboxId.of(id);
        MessageOutboxId messageOutboxId2 = MessageOutboxId.of(id);

        // when & then
        assertThat(messageOutboxId1).isEqualTo(messageOutboxId2);
    }

    @Test
    @DisplayName("같은 값을 가진 MessageOutboxId는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        MessageOutboxId messageOutboxId1 = MessageOutboxId.of(id);
        MessageOutboxId messageOutboxId2 = MessageOutboxId.of(id);

        // when & then
        assertThat(messageOutboxId1.hashCode()).isEqualTo(messageOutboxId2.hashCode());
    }

    @Test
    @DisplayName("forNew()는 null 값을 가진 MessageOutboxId를 생성해야 한다")
    void shouldCreateNullMessageOutboxIdWithForNew() {
        // when
        MessageOutboxId messageOutboxId = MessageOutboxId.forNew();

        // then
        assertThat(messageOutboxId).isNotNull();
        assertThat(messageOutboxId.getValue()).isNull();
        assertThat(messageOutboxId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 null인 경우 true를 반환해야 한다")
    void shouldReturnTrueWhenIsNewForNullValue() {
        // given
        MessageOutboxId messageOutboxId = MessageOutboxId.forNew();

        // when & then
        assertThat(messageOutboxId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 있는 경우 false를 반환해야 한다")
    void shouldReturnFalseWhenIsNewForNonNullValue() {
        // given
        MessageOutboxId messageOutboxId = MessageOutboxId.of("01JCQM5K3P9XYZ123456ABCD");

        // when & then
        assertThat(messageOutboxId.isNew()).isFalse();
    }
}
