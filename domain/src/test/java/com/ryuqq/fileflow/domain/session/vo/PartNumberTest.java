package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.PartNumberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PartNumber 단위 테스트")
class PartNumberTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 PartNumber를 생성할 수 있다")
        void of_WithValidValue_ShouldCreatePartNumber() {
            // given
            int number = 500;

            // when
            PartNumber partNumber = PartNumber.of(number);

            // then
            assertThat(partNumber.number()).isEqualTo(number);
        }

        @Test
        @DisplayName("최소값보다 작은 경우 예외가 발생한다")
        void of_WithSmallerThanMin_ShouldThrowException() {
            // given
            int invalidNumber = 0;

            // when & then
            assertThatThrownBy(() -> PartNumber.of(invalidNumber))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 번호는 1 ~ 10000 사이여야 합니다");
        }

        @Test
        @DisplayName("최대값보다 큰 경우 예외가 발생한다")
        void of_WithGreaterThanMax_ShouldThrowException() {
            // given
            int invalidNumber = 10_001;

            // when & then
            assertThatThrownBy(() -> PartNumber.of(invalidNumber))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 번호는 1 ~ 10000 사이여야 합니다");
        }
    }

    @Nested
    @DisplayName("첫 번째 Part 판별 테스트")
    class IsFirstTest {

        @Test
        @DisplayName("Part 번호가 1이면 첫 번째 Part이다")
        void isFirst_WithMinNumber_ShouldReturnTrue() {
            // given
            PartNumber partNumber = PartNumber.of(1);

            // when & then
            assertThat(partNumber.isFirst()).isTrue();
        }

        @Test
        @DisplayName("Part 번호가 1보다 크면 첫 번째 Part가 아니다")
        void isFirst_WithGreaterThanMin_ShouldReturnFalse() {
            // given
            PartNumber partNumber = PartNumber.of(2);

            // when & then
            assertThat(partNumber.isFirst()).isFalse();
        }
    }

    @Nested
    @DisplayName("마지막 Part 판별 테스트")
    class IsLastTest {

        @Test
        @DisplayName("Part 번호가 TotalParts와 같으면 마지막 Part이다")
        void isLast_WithSameValue_ShouldReturnTrue() {
            // given
            PartNumber partNumber = PartNumber.of(3);
            TotalParts totalParts = TotalParts.of(3);

            // when & then
            assertThat(partNumber.isLast(totalParts)).isTrue();
        }

        @Test
        @DisplayName("Part 번호가 TotalParts보다 작으면 마지막 Part가 아니다")
        void isLast_WithLessThanTotal_ShouldReturnFalse() {
            // given
            PartNumber partNumber = PartNumber.of(2);
            TotalParts totalParts = TotalParts.of(3);

            // when & then
            assertThat(partNumber.isLast(totalParts)).isFalse();
        }

        @Test
        @DisplayName("TotalParts가 null이면 예외가 발생한다")
        void isLast_WithNullTotalParts_ShouldThrowException() {
            // given
            PartNumber partNumber = PartNumber.of(1);

            // when & then
            assertThatThrownBy(() -> partNumber.isLast(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TotalParts는 null일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("다음 Part 생성 테스트")
    class NextTest {

        @Test
        @DisplayName("다음 PartNumber를 생성할 수 있다")
        void next_WithValidNumber_ShouldReturnNextPartNumber() {
            // given
            PartNumber partNumber = PartNumber.of(5);

            // when
            PartNumber next = partNumber.next();

            // then
            assertThat(next.number()).isEqualTo(6);
        }

        @Test
        @DisplayName("최대 PartNumber에서 next 호출 시 예외가 발생한다")
        void next_WithMaxNumber_ShouldThrowException() {
            // given
            PartNumber partNumber = PartNumber.of(10_000);

            // when & then
            assertThatThrownBy(partNumber::next)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("마지막 Part 10000 다음은 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("이전 Part 생성 테스트")
    class PreviousTest {

        @Test
        @DisplayName("이전 PartNumber를 생성할 수 있다")
        void previous_WithValidNumber_ShouldReturnPreviousPartNumber() {
            // given
            PartNumber partNumber = PartNumber.of(5);

            // when
            PartNumber previous = partNumber.previous();

            // then
            assertThat(previous.number()).isEqualTo(4);
        }

        @Test
        @DisplayName("최소 PartNumber에서 previous 호출 시 예외가 발생한다")
        void previous_WithMinNumber_ShouldThrowException() {
            // given
            PartNumber partNumber = PartNumber.of(1);

            // when & then
            assertThatThrownBy(partNumber::previous)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("첫 번째 Part 1 이전은 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가지면 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            PartNumber partNumber1 = PartNumber.of(7);
            PartNumber partNumber2 = PartNumber.of(7);

            // when & then
            assertThat(partNumber1).isEqualTo(partNumber2);
            assertThat(partNumber1.hashCode()).isEqualTo(partNumber2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가지면 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            PartNumber partNumber1 = PartNumber.of(7);
            PartNumber partNumber2 = PartNumber.of(8);

            // when & then
            assertThat(partNumber1).isNotEqualTo(partNumber2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 PartNumber가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            PartNumber defaultPartNumber = PartNumberFixture.defaultPartNumber();
            PartNumber firstPartNumber = PartNumberFixture.firstPartNumber();
            PartNumber lastPartNumber = PartNumberFixture.lastPartNumber();
            PartNumber customPartNumber = PartNumberFixture.customPartNumber(3);

            // then
            assertThat(defaultPartNumber.number()).isEqualTo(1);
            assertThat(firstPartNumber.isFirst()).isTrue();
            assertThat(lastPartNumber.number()).isEqualTo(5);
            assertThat(customPartNumber.number()).isEqualTo(3);
        }
    }
}
