package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.session.fixture.TotalPartsFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TotalParts 단위 테스트")
class TotalPartsTest {

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 TotalParts를 생성할 수 있다")
        void of_WithValidValue_ShouldCreateTotalParts() {
            // given
            int value = 100;

            // when
            TotalParts totalParts = TotalParts.of(value);

            // then
            assertThat(totalParts.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("최소값 미만이면 예외가 발생한다")
        void of_WithLessThanMin_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> TotalParts.of(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 개수는 1 ~ 10000 사이여야 합니다");
        }

        @Test
        @DisplayName("최대값 초과 시 예외가 발생한다")
        void of_WithGreaterThanMax_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> TotalParts.of(10_001))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 개수는 1 ~ 10000 사이여야 합니다");
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("계산 테스트")
    class CalculateTest {

        @Test
        @DisplayName("파일 크기와 파트 크기로 TotalParts를 계산한다")
        void calculate_WithValidInputs_ShouldReturnTotalParts() {
            // given
            long fileSize = 10_000L;
            long partSize = 3_000L;

            // when
            TotalParts totalParts = TotalParts.calculate(fileSize, partSize);

            // then
            assertThat(totalParts.value()).isEqualTo(4); // 10000/3000 올림 = 4
        }

        @Test
        @DisplayName("파일 크기가 0 이하면 예외가 발생한다")
        void calculate_WithNonPositiveFileSize_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> TotalParts.calculate(0, 5_000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("파트 크기가 0 이하면 예외가 발생한다")
        void calculate_WithNonPositivePartSize_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> TotalParts.calculate(10_000, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("계산 결과가 최대값 초과 시 예외가 발생한다")
        void calculate_WithResultOverMax_ShouldThrowException() {
            // given
            long fileSize = 10_000_000_000L;
            long partSize = 100L;

            // when & then
            assertThatThrownBy(() -> TotalParts.calculate(fileSize, partSize))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 개수는 1 ~ 10000 사이여야 합니다");
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("Part 번호 검증 테스트")
    class PartNumberValidationTest {

        @Test
        @DisplayName("범위 내 파트 번호는 유효하다")
        void isValidPartNumber_WithValidNumber_ShouldReturnTrue() {
            // given
            TotalParts totalParts = TotalParts.of(5);

            // when & then
            assertThat(totalParts.isValidPartNumber(3)).isTrue();
        }

        @Test
        @DisplayName("범위 밖 파트 번호는 유효하지 않다")
        void isValidPartNumber_WithInvalidNumber_ShouldReturnFalse() {
            // given
            TotalParts totalParts = TotalParts.of(5);

            // when & then
            assertThat(totalParts.isValidPartNumber(0)).isFalse();
            assertThat(totalParts.isValidPartNumber(6)).isFalse();
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 TotalParts가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            TotalParts defaultTotalParts = TotalPartsFixture.defaultTotalParts();
            TotalParts smallTotalParts = TotalPartsFixture.smallTotalParts();
            TotalParts largeTotalParts = TotalPartsFixture.largeTotalParts();
            TotalParts customTotalParts = TotalPartsFixture.customTotalParts(8);

            // then
            assertThat(defaultTotalParts.value()).isEqualTo(5);
            assertThat(smallTotalParts.value()).isEqualTo(2);
            assertThat(largeTotalParts.value()).isEqualTo(100);
            assertThat(customTotalParts.value()).isEqualTo(8);
        }
    }
}
