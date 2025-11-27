package com.ryuqq.fileflow.adapter.out.persistence.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SoftDeletableEntity 단위 테스트")
class SoftDeletableEntityTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("감사 정보만으로 생성하면 deletedAt은 null이다")
        void create_WithAuditInfoOnly_ShouldHaveNullDeletedAt() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 26, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5);

            // when
            TestSoftDeletableEntity entity = new TestSoftDeletableEntity(createdAt, updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(entity.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("전체 필드로 생성하면 deletedAt이 설정된다")
        void create_WithAllFields_ShouldHaveDeletedAt() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 26, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5);
            LocalDateTime deletedAt = LocalDateTime.of(2025, 11, 26, 11, 0);

            // when
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(createdAt, updatedAt, deletedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        }

        @Test
        @DisplayName("deletedAt을 null로 전달하면 활성 상태이다")
        void create_WithNullDeletedAt_ShouldBeActive() {
            // given
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime updatedAt = LocalDateTime.now();

            // when
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(createdAt, updatedAt, null);

            // then
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("삭제 상태 확인 테스트")
    class DeleteStatusTest {

        @Test
        @DisplayName("deletedAt이 null이면 isDeleted는 false")
        void isDeleted_WhenDeletedAtNull_ShouldReturnFalse() {
            // given
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("deletedAt이 있으면 isDeleted는 true")
        void isDeleted_WhenDeletedAtNotNull_ShouldReturnTrue() {
            // given
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(
                            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("deletedAt이 null이면 isActive는 true")
        void isActive_WhenDeletedAtNull_ShouldReturnTrue() {
            // given
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(entity.isActive()).isTrue();
        }

        @Test
        @DisplayName("deletedAt이 있으면 isActive는 false")
        void isActive_WhenDeletedAtNotNull_ShouldReturnFalse() {
            // given
            TestSoftDeletableEntity entity =
                    new TestSoftDeletableEntity(
                            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(entity.isActive()).isFalse();
        }

        @Test
        @DisplayName("isDeleted와 isActive는 항상 반대 결과")
        void isDeletedAndIsActive_ShouldBeOpposite() {
            // given
            TestSoftDeletableEntity activeEntity =
                    new TestSoftDeletableEntity(LocalDateTime.now(), LocalDateTime.now());
            TestSoftDeletableEntity deletedEntity =
                    new TestSoftDeletableEntity(
                            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

            // when & then
            assertThat(activeEntity.isDeleted()).isNotEqualTo(activeEntity.isActive());
            assertThat(deletedEntity.isDeleted()).isNotEqualTo(deletedEntity.isActive());
        }
    }

    @Nested
    @DisplayName("상속 테스트")
    class InheritanceTest {

        @Test
        @DisplayName("BaseAuditEntity를 상속받아 감사 필드를 사용할 수 있다")
        void inheritance_ShouldProvideAuditFields() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 6, 1, 0, 0);

            // when
            TestSoftDeletableEntity entity = new TestSoftDeletableEntity(createdAt, updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    /**
     * 테스트용 구체 클래스
     *
     * <p>SoftDeletableEntity는 추상 클래스이므로 테스트용 구체 클래스 필요
     */
    private static class TestSoftDeletableEntity extends SoftDeletableEntity {

        TestSoftDeletableEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
            super(createdAt, updatedAt);
        }

        TestSoftDeletableEntity(
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
            super(createdAt, updatedAt, deletedAt);
        }
    }
}
