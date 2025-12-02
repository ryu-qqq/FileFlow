package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetStatus 단위 테스트")
class FileAssetStatusTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 상태 값이 정의되어 있다")
        void values_ShouldContainAllStatuses() {
            // when
            FileAssetStatus[] values = FileAssetStatus.values();

            // then
            assertThat(values).hasSize(5);
            assertThat(values)
                    .containsExactly(
                            FileAssetStatus.PENDING,
                            FileAssetStatus.PROCESSING,
                            FileAssetStatus.COMPLETED,
                            FileAssetStatus.FAILED,
                            FileAssetStatus.DELETED);
        }

        @Test
        @DisplayName("문자열로 상태를 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnStatus() {
            // when & then
            assertThat(FileAssetStatus.valueOf("PENDING")).isEqualTo(FileAssetStatus.PENDING);
            assertThat(FileAssetStatus.valueOf("PROCESSING")).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(FileAssetStatus.valueOf("COMPLETED")).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(FileAssetStatus.valueOf("FAILED")).isEqualTo(FileAssetStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("상태 의미 테스트")
    class StatusMeaningTest {

        @Test
        @DisplayName("PENDING은 생성됨, 가공 대기 중 상태이다")
        void pending_ShouldRepresentWaitingState() {
            // given
            FileAssetStatus status = FileAssetStatus.PENDING;

            // then
            assertThat(status.name()).isEqualTo("PENDING");
            assertThat(status.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("PROCESSING은 가공 처리 중 상태이다")
        void processing_ShouldRepresentProcessingState() {
            // given
            FileAssetStatus status = FileAssetStatus.PROCESSING;

            // then
            assertThat(status.name()).isEqualTo("PROCESSING");
            assertThat(status.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("COMPLETED는 완료됨 상태이다")
        void completed_ShouldRepresentCompletedState() {
            // given
            FileAssetStatus status = FileAssetStatus.COMPLETED;

            // then
            assertThat(status.name()).isEqualTo("COMPLETED");
            assertThat(status.ordinal()).isEqualTo(2);
        }

        @Test
        @DisplayName("FAILED는 실패 상태이다")
        void failed_ShouldRepresentFailedState() {
            // given
            FileAssetStatus status = FileAssetStatus.FAILED;

            // then
            assertThat(status.name()).isEqualTo("FAILED");
            assertThat(status.ordinal()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 상태는 동등하다")
        void equals_WithSameStatus_ShouldBeEqual() {
            // given
            FileAssetStatus status1 = FileAssetStatus.PENDING;
            FileAssetStatus status2 = FileAssetStatus.PENDING;

            // when & then
            assertThat(status1).isEqualTo(status2);
            assertThat(status1 == status2).isTrue();
        }

        @Test
        @DisplayName("다른 상태는 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            FileAssetStatus status1 = FileAssetStatus.PENDING;
            FileAssetStatus status2 = FileAssetStatus.COMPLETED;

            // when & then
            assertThat(status1).isNotEqualTo(status2);
        }
    }

    @Nested
    @DisplayName("신규 상태 테스트")
    class NewStatusTest {

        @Test
        @DisplayName("RESIZED 상태가 존재한다")
        void shouldHaveResizedStatus() {
            // when
            FileAssetStatus status = FileAssetStatus.RESIZED;

            // then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo("RESIZED");
        }

        @Test
        @DisplayName("N8N_PROCESSING 상태가 존재한다")
        void shouldHaveN8nProcessingStatus() {
            // when
            FileAssetStatus status = FileAssetStatus.N8N_PROCESSING;

            // then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo("N8N_PROCESSING");
        }

        @Test
        @DisplayName("N8N_COMPLETED 상태가 존재한다")
        void shouldHaveN8nCompletedStatus() {
            // when
            FileAssetStatus status = FileAssetStatus.N8N_COMPLETED;

            // then
            assertThat(status).isNotNull();
            assertThat(status.name()).isEqualTo("N8N_COMPLETED");
        }
    }
}
