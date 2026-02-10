package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DateRange")
class DateRangeTest {

    @Nested
    @DisplayName("생성 및 검증")
    class Creation {

        @Test
        @DisplayName("정상적인 시작일, 종료일로 생성한다")
        void createWithValidDates() {
            LocalDate start = LocalDate.of(2025, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 31);

            DateRange range = DateRange.of(start, end);

            assertThat(range.startDate()).isEqualTo(start);
            assertThat(range.endDate()).isEqualTo(end);
        }

        @Test
        @DisplayName("시작일과 종료일이 같아도 생성된다")
        void sameDates_createdSuccessfully() {
            LocalDate date = LocalDate.of(2025, 6, 15);

            DateRange range = DateRange.of(date, date);

            assertThat(range.startDate()).isEqualTo(date);
            assertThat(range.endDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("시작일이 종료일보다 이후이면 IllegalArgumentException이 발생한다")
        void startAfterEnd_throwsException() {
            LocalDate start = LocalDate.of(2025, 2, 1);
            LocalDate end = LocalDate.of(2025, 1, 1);

            assertThatThrownBy(() -> DateRange.of(start, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일")
                    .hasMessageContaining("종료일");
        }

        @Test
        @DisplayName("시작일이 null이면 정상 생성된다")
        void nullStartDate_createdSuccessfully() {
            DateRange range = DateRange.of(null, LocalDate.of(2025, 1, 31));

            assertThat(range.startDate()).isNull();
            assertThat(range.endDate()).isNotNull();
        }

        @Test
        @DisplayName("종료일이 null이면 정상 생성된다")
        void nullEndDate_createdSuccessfully() {
            DateRange range = DateRange.of(LocalDate.of(2025, 1, 1), null);

            assertThat(range.startDate()).isNotNull();
            assertThat(range.endDate()).isNull();
        }

        @Test
        @DisplayName("둘 다 null이면 정상 생성된다")
        void bothNull_createdSuccessfully() {
            DateRange range = DateRange.of(null, null);

            assertThat(range.startDate()).isNull();
            assertThat(range.endDate()).isNull();
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("lastDays는 오늘 기준 N일 전부터 오늘까지 범위를 생성한다")
        void lastDays_createsRangeFromNDaysAgo() {
            DateRange range = DateRange.lastDays(7);

            LocalDate today = LocalDate.now();
            assertThat(range.startDate()).isEqualTo(today.minusDays(7));
            assertThat(range.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("lastDays에 음수를 전달하면 IllegalArgumentException이 발생한다")
        void lastDays_negativeDays_throwsException() {
            assertThatThrownBy(() -> DateRange.lastDays(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }

        @Test
        @DisplayName("lastDays(0)은 오늘~오늘 범위를 생성한다")
        void lastDays_zero_todayOnly() {
            DateRange range = DateRange.lastDays(0);

            LocalDate today = LocalDate.now();
            assertThat(range.startDate()).isEqualTo(today);
            assertThat(range.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("thisMonth는 이번 달 1일부터 말일까지 범위를 생성한다")
        void thisMonth_createsCurrentMonthRange() {
            DateRange range = DateRange.thisMonth();

            LocalDate today = LocalDate.now();
            assertThat(range.startDate()).isEqualTo(today.withDayOfMonth(1));
            assertThat(range.endDate()).isEqualTo(today.withDayOfMonth(today.lengthOfMonth()));
        }

        @Test
        @DisplayName("lastMonth는 지난 달 1일부터 말일까지 범위를 생성한다")
        void lastMonth_createsLastMonthRange() {
            DateRange range = DateRange.lastMonth();

            LocalDate today = LocalDate.now();
            LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayLastMonth = today.withDayOfMonth(1).minusDays(1);
            assertThat(range.startDate()).isEqualTo(firstDayLastMonth);
            assertThat(range.endDate()).isEqualTo(lastDayLastMonth);
        }

        @Test
        @DisplayName("until은 시작일 없이 종료일까지의 범위를 생성한다")
        void until_createsRangeWithEndOnly() {
            LocalDate endDate = LocalDate.of(2025, 6, 30);

            DateRange range = DateRange.until(endDate);

            assertThat(range.startDate()).isNull();
            assertThat(range.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("from은 시작일부터 종료일 없는 범위를 생성한다")
        void from_createsRangeWithStartOnly() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);

            DateRange range = DateRange.from(startDate);

            assertThat(range.startDate()).isEqualTo(startDate);
            assertThat(range.endDate()).isNull();
        }
    }

    @Nested
    @DisplayName("startInstant / endInstant")
    class InstantConversion {

        @Test
        @DisplayName("startDate가 null이면 startInstant는 null이다")
        void nullStartDate_returnsNullInstant() {
            DateRange range = DateRange.until(LocalDate.of(2025, 1, 31));

            assertThat(range.startInstant()).isNull();
        }

        @Test
        @DisplayName("endDate가 null이면 endInstant는 null이다")
        void nullEndDate_returnsNullInstant() {
            DateRange range = DateRange.from(LocalDate.of(2025, 1, 1));

            assertThat(range.endInstant()).isNull();
        }

        @Test
        @DisplayName("startInstant는 해당 날짜 시작 시각을 반환한다")
        void startInstant_returnsStartOfDay() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DateRange range = DateRange.of(date, date);

            assertThat(range.startInstant()).isNotNull();
        }

        @Test
        @DisplayName("endInstant는 해당 날짜 마지막 나노초를 반환한다")
        void endInstant_returnsEndOfDay() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DateRange range = DateRange.of(date, date);

            assertThat(range.endInstant()).isNotNull();
            assertThat(range.endInstant()).isAfter(range.startInstant());
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        @DisplayName("시작일과 종료일이 모두 null이면 true를 반환한다")
        void bothNull_returnsTrue() {
            DateRange range = DateRange.of(null, null);

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("시작일이 있으면 false를 반환한다")
        void hasStartDate_returnsFalse() {
            DateRange range = DateRange.from(LocalDate.of(2025, 1, 1));

            assertThat(range.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("종료일이 있으면 false를 반환한다")
        void hasEndDate_returnsFalse() {
            DateRange range = DateRange.until(LocalDate.of(2025, 12, 31));

            assertThat(range.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("contains")
    class Contains {

        @Test
        @DisplayName("범위 내 날짜에 대해 true를 반환한다")
        void dateInRange_returnsTrue() {
            DateRange range = DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(range.contains(LocalDate.of(2025, 1, 15))).isTrue();
        }

        @Test
        @DisplayName("시작일과 동일한 날짜에 대해 true를 반환한다")
        void exactStartDate_returnsTrue() {
            LocalDate start = LocalDate.of(2025, 1, 1);
            DateRange range = DateRange.of(start, LocalDate.of(2025, 1, 31));

            assertThat(range.contains(start)).isTrue();
        }

        @Test
        @DisplayName("종료일과 동일한 날짜에 대해 true를 반환한다")
        void exactEndDate_returnsTrue() {
            LocalDate end = LocalDate.of(2025, 1, 31);
            DateRange range = DateRange.of(LocalDate.of(2025, 1, 1), end);

            assertThat(range.contains(end)).isTrue();
        }

        @Test
        @DisplayName("범위 밖 날짜에 대해 false를 반환한다")
        void dateOutOfRange_returnsFalse() {
            DateRange range = DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(range.contains(LocalDate.of(2025, 2, 1))).isFalse();
        }

        @Test
        @DisplayName("null 날짜에 대해 false를 반환한다")
        void nullDate_returnsFalse() {
            DateRange range = DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(range.contains(null)).isFalse();
        }

        @Test
        @DisplayName("시작일만 있는 범위에서 시작일 이후 날짜는 포함된다")
        void startOnly_afterStart_returnsTrue() {
            DateRange range = DateRange.from(LocalDate.of(2025, 1, 1));

            assertThat(range.contains(LocalDate.of(2099, 12, 31))).isTrue();
        }

        @Test
        @DisplayName("종료일만 있는 범위에서 종료일 이전 날짜는 포함된다")
        void endOnly_beforeEnd_returnsTrue() {
            DateRange range = DateRange.until(LocalDate.of(2025, 12, 31));

            assertThat(range.contains(LocalDate.of(2000, 1, 1))).isTrue();
        }

        @Test
        @DisplayName("빈 범위에서 모든 날짜가 포함된다")
        void emptyRange_containsAllDates() {
            DateRange range = DateRange.of(null, null);

            assertThat(range.contains(LocalDate.of(2025, 6, 15))).isTrue();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 날짜를 가진 DateRange는 동일하다")
        void sameDates_areEqual() {
            DateRange range1 = DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
            DateRange range2 = DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }
    }
}
