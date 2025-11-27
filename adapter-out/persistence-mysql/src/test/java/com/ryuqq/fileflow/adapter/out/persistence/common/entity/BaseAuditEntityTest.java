package com.ryuqq.fileflow.adapter.out.persistence.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BaseAuditEntity 단위 테스트")
class BaseAuditEntityTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("감사 정보 생성자로 createdAt과 updatedAt을 설정할 수 있다")
        void constructor_WithAuditFields_ShouldSetFields() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 6, 1, 12, 30, 45);

            // when
            TestAuditEntity entity = new TestAuditEntity(createdAt, updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("기본 생성자로 생성 시 감사 필드는 null이다")
        void defaultConstructor_ShouldHaveNullFields() {
            // when
            TestAuditEntity entity = new TestAuditEntity();

            // then
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("createdAt과 updatedAt이 동일한 시각으로 설정될 수 있다")
        void constructor_WithSameTimestamps_ShouldSetBothFields() {
            // given
            LocalDateTime sameTime = LocalDateTime.of(2025, 11, 26, 10, 0, 0);

            // when
            TestAuditEntity entity = new TestAuditEntity(sameTime, sameTime);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(sameTime);
            assertThat(entity.getUpdatedAt()).isEqualTo(sameTime);
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("getCreatedAt()은 생성 일시를 반환한다")
        void getCreatedAt_ShouldReturnCreatedAt() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 3, 15, 9, 30);
            TestAuditEntity entity = new TestAuditEntity(createdAt, LocalDateTime.now());

            // when
            LocalDateTime result = entity.getCreatedAt();

            // then
            assertThat(result).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("getUpdatedAt()은 수정 일시를 반환한다")
        void getUpdatedAt_ShouldReturnUpdatedAt() {
            // given
            LocalDateTime updatedAt = LocalDateTime.of(2025, 7, 20, 14, 45);
            TestAuditEntity entity = new TestAuditEntity(LocalDateTime.now(), updatedAt);

            // when
            LocalDateTime result = entity.getUpdatedAt();

            // then
            assertThat(result).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("updatedAt은 createdAt보다 나중일 수 있다")
        void updatedAt_CanBeAfterCreatedAt() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 12, 31, 23, 59);

            // when
            TestAuditEntity entity = new TestAuditEntity(createdAt, updatedAt);

            // then
            assertThat(entity.getUpdatedAt()).isAfter(entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("시간 정밀도 테스트")
    class TimestampPrecisionTest {

        @Test
        @DisplayName("나노초 정밀도의 시간을 저장할 수 있다")
        void constructor_WithNanoseconds_ShouldPreservePrecision() {
            // given
            LocalDateTime preciseTime = LocalDateTime.of(2025, 11, 26, 10, 30, 45, 123456789);

            // when
            TestAuditEntity entity = new TestAuditEntity(preciseTime, preciseTime);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(preciseTime);
            assertThat(entity.getCreatedAt().getNano()).isEqualTo(123456789);
        }

        @Test
        @DisplayName("자정 시각(00:00:00)을 저장할 수 있다")
        void constructor_WithMidnight_ShouldStoreCorrectly() {
            // given
            LocalDateTime midnight = LocalDateTime.of(2025, 11, 26, 0, 0, 0);

            // when
            TestAuditEntity entity = new TestAuditEntity(midnight, midnight);

            // then
            assertThat(entity.getCreatedAt().getHour()).isZero();
            assertThat(entity.getCreatedAt().getMinute()).isZero();
            assertThat(entity.getCreatedAt().getSecond()).isZero();
        }

        @Test
        @DisplayName("23:59:59 시각을 저장할 수 있다")
        void constructor_WithEndOfDay_ShouldStoreCorrectly() {
            // given
            LocalDateTime endOfDay = LocalDateTime.of(2025, 11, 26, 23, 59, 59);

            // when
            TestAuditEntity entity = new TestAuditEntity(endOfDay, endOfDay);

            // then
            assertThat(entity.getCreatedAt().getHour()).isEqualTo(23);
            assertThat(entity.getCreatedAt().getMinute()).isEqualTo(59);
            assertThat(entity.getCreatedAt().getSecond()).isEqualTo(59);
        }
    }

    @Nested
    @DisplayName("날짜 경계 테스트")
    class DateBoundaryTest {

        @Test
        @DisplayName("연초(1월 1일)를 저장할 수 있다")
        void constructor_WithStartOfYear_ShouldStoreCorrectly() {
            // given
            LocalDateTime startOfYear = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

            // when
            TestAuditEntity entity = new TestAuditEntity(startOfYear, startOfYear);

            // then
            assertThat(entity.getCreatedAt().getMonth().getValue()).isEqualTo(1);
            assertThat(entity.getCreatedAt().getDayOfMonth()).isEqualTo(1);
        }

        @Test
        @DisplayName("연말(12월 31일)을 저장할 수 있다")
        void constructor_WithEndOfYear_ShouldStoreCorrectly() {
            // given
            LocalDateTime endOfYear = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

            // when
            TestAuditEntity entity = new TestAuditEntity(endOfYear, endOfYear);

            // then
            assertThat(entity.getCreatedAt().getMonth().getValue()).isEqualTo(12);
            assertThat(entity.getCreatedAt().getDayOfMonth()).isEqualTo(31);
        }

        @Test
        @DisplayName("윤년(2월 29일)을 저장할 수 있다")
        void constructor_WithLeapYear_ShouldStoreCorrectly() {
            // given - 2024년은 윤년
            LocalDateTime leapDay = LocalDateTime.of(2024, 2, 29, 12, 0, 0);

            // when
            TestAuditEntity entity = new TestAuditEntity(leapDay, leapDay);

            // then
            assertThat(entity.getCreatedAt().getDayOfMonth()).isEqualTo(29);
            assertThat(entity.getCreatedAt().getMonth().getValue()).isEqualTo(2);
        }
    }

    // ==================== Test Helper Class ====================

    /**
     * BaseAuditEntity를 테스트하기 위한 구체 클래스.
     *
     * <p>BaseAuditEntity는 추상 클래스이므로 테스트를 위해 구체 클래스가 필요합니다.
     */
    private static class TestAuditEntity extends BaseAuditEntity {

        TestAuditEntity() {
            super();
        }

        TestAuditEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
            super(createdAt, updatedAt);
        }
    }
}
