package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.ExpirationTimeFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExpirationTime 단위 테스트")
class ExpirationTimeTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 LocalDateTime으로 생성할 수 있다")
        void of_WithValidValue_ShouldCreateExpirationTime() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 1, 2, 0, 0);

            // when
            ExpirationTime expirationTime = ExpirationTime.of(expiresAt);

            // then
            assertThat(expirationTime.value()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            LocalDateTime nullValue = null;

            // when & then
            assertThatThrownBy(() -> ExpirationTime.of(nullValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("만료 시각은 null일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("현재 시각 기준 생성 테스트")
    class FromNowTest {

        @Test
        @DisplayName("지정된 분 후의 만료 시각을 생성한다")
        void fromNow_WithMinutes_ShouldCreateFutureExpirationTime() {
            // given
            long minutesFromNow = 30L;

            // when
            ExpirationTime expirationTime = ExpirationTime.fromNow(FIXED_CLOCK, minutesFromNow);

            // then
            assertThat(expirationTime.value()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 30));
        }
    }

    @Nested
    @DisplayName("만료 여부 테스트")
    class ExpirationStatusTest {

        @Test
        @DisplayName("현재 시각이 만료 시각을 지난 경우 만료 상태이다")
        void isExpired_WhenNowIsAfterValue_ShouldReturnTrue() {
            // given
            LocalDateTime expiredAt = LocalDateTime.of(2024, 12, 31, 23, 0);
            ExpirationTime expirationTime = ExpirationTime.of(expiredAt);

            // when & then
            assertThat(expirationTime.isExpired(FIXED_CLOCK)).isTrue();
        }

        @Test
        @DisplayName("현재 시각이 만료 시각 이전이면 만료되지 않는다")
        void isNotExpired_WhenNowIsBeforeValue_ShouldReturnTrue() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 1, 1, 1, 0);
            ExpirationTime expirationTime = ExpirationTime.of(expiresAt);

            // when & then
            assertThat(expirationTime.isNotExpired(FIXED_CLOCK)).isTrue();
            assertThat(expirationTime.isExpired(FIXED_CLOCK)).isFalse();
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성한 ExpirationTime이 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            ExpirationTime defaultExpirationTime = ExpirationTimeFixture.defaultExpirationTime();
            ExpirationTime multipartExpirationTime =
                    ExpirationTimeFixture.multipartExpirationTime();
            ExpirationTime expiredExpirationTime = ExpirationTimeFixture.expiredExpirationTime();
            ExpirationTime customExpirationTime =
                    ExpirationTimeFixture.customExpirationTime(LocalDateTime.of(2025, 1, 3, 0, 0));

            // then
            assertThat(defaultExpirationTime.isNotExpired(FIXED_CLOCK)).isTrue();
            assertThat(multipartExpirationTime.value()).isAfter(LocalDateTime.now());
            assertThat(expiredExpirationTime.value()).isBefore(LocalDateTime.now());
            assertThat(customExpirationTime.value()).isEqualTo(LocalDateTime.of(2025, 1, 3, 0, 0));
        }
    }
}
