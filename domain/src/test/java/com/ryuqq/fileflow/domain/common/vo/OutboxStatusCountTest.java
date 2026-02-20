package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("OutboxStatusCount 단위 테스트")
class OutboxStatusCountTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void createWithValidValues() {
            OutboxStatusCount count = new OutboxStatusCount(10, 20, 5);

            assertThat(count.pending()).isEqualTo(10);
            assertThat(count.sent()).isEqualTo(20);
            assertThat(count.failed()).isEqualTo(5);
        }

        @Test
        @DisplayName("모든 값이 0인 카운트를 생성한다")
        void createWithZeroValues() {
            OutboxStatusCount count = new OutboxStatusCount(0, 0, 0);

            assertThat(count.pending()).isZero();
            assertThat(count.sent()).isZero();
            assertThat(count.failed()).isZero();
        }
    }

    @Nested
    @DisplayName("empty 팩토리 메서드")
    class EmptyTest {

        @Test
        @DisplayName("모든 상태가 0인 OutboxStatusCount를 반환한다")
        void empty_ReturnsAllZeros() {
            OutboxStatusCount count = OutboxStatusCount.empty();

            assertThat(count.pending()).isZero();
            assertThat(count.sent()).isZero();
            assertThat(count.failed()).isZero();
        }
    }

    @Nested
    @DisplayName("total 메서드")
    class TotalTest {

        @Test
        @DisplayName("pending + sent + failed 합계를 반환한다")
        void total_ReturnsSumOfAllStatuses() {
            OutboxStatusCount count = new OutboxStatusCount(10, 20, 5);

            assertThat(count.total()).isEqualTo(35);
        }

        @Test
        @DisplayName("empty 카운트의 합계는 0이다")
        void total_EmptyCountReturnsZero() {
            OutboxStatusCount count = OutboxStatusCount.empty();

            assertThat(count.total()).isZero();
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValues_AreEqual() {
            OutboxStatusCount count1 = new OutboxStatusCount(10, 20, 5);
            OutboxStatusCount count2 = new OutboxStatusCount(10, 20, 5);

            assertThat(count1).isEqualTo(count2);
            assertThat(count1.hashCode()).isEqualTo(count2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValues_AreNotEqual() {
            OutboxStatusCount count1 = new OutboxStatusCount(10, 20, 5);
            OutboxStatusCount count2 = new OutboxStatusCount(10, 20, 3);

            assertThat(count1).isNotEqualTo(count2);
        }
    }
}
