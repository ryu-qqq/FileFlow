package com.ryuqq.fileflow.adapter.out.persistence.session.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionConditionBuilder 단위 테스트")
class SessionConditionBuilderTest {

    private final SessionConditionBuilder conditionBuilder = new SessionConditionBuilder();

    @Nested
    @DisplayName("singleSessionIdEq 메서드 테스트")
    class SingleSessionIdEqTest {

        @Test
        @DisplayName("ID가 주어지면 BooleanExpression을 반환합니다")
        void singleSessionIdEq_withId_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.singleSessionIdEq("session-001");

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("ID가 null이면 null을 반환합니다")
        void singleSessionIdEq_withNull_shouldReturnNull() {
            // when
            BooleanExpression result = conditionBuilder.singleSessionIdEq(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("multipartSessionIdEq 메서드 테스트")
    class MultipartSessionIdEqTest {

        @Test
        @DisplayName("ID가 주어지면 BooleanExpression을 반환합니다")
        void multipartSessionIdEq_withId_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.multipartSessionIdEq("session-001");

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("ID가 null이면 null을 반환합니다")
        void multipartSessionIdEq_withNull_shouldReturnNull() {
            // when
            BooleanExpression result = conditionBuilder.multipartSessionIdEq(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("completedPartSessionIdEq 메서드 테스트")
    class CompletedPartSessionIdEqTest {

        @Test
        @DisplayName("sessionId가 주어지면 BooleanExpression을 반환합니다")
        void completedPartSessionIdEq_withId_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.completedPartSessionIdEq("session-001");

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("sessionId가 null이면 null을 반환합니다")
        void completedPartSessionIdEq_withNull_shouldReturnNull() {
            // when
            BooleanExpression result = conditionBuilder.completedPartSessionIdEq(null);

            // then
            assertThat(result).isNull();
        }
    }
}
