package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadStatus Enum 테스트")
class UploadStatusTest {

    @Nested
    @DisplayName("진행률 테스트")
    class ProgressTest {

        @Test
        @DisplayName("PENDING 상태의 진행률은 0%이다")
        void pendingProgress() {
            assertThat(UploadStatus.PENDING.getProgress()).isEqualTo(0);
        }

        @Test
        @DisplayName("UPLOADING 상태의 진행률은 50%이다")
        void uploadingProgress() {
            assertThat(UploadStatus.UPLOADING.getProgress()).isEqualTo(50);
        }

        @Test
        @DisplayName("COMPLETED 상태의 진행률은 100%이다")
        void completedProgress() {
            assertThat(UploadStatus.COMPLETED.getProgress()).isEqualTo(100);
        }

        @Test
        @DisplayName("FAILED 상태의 진행률은 0%이다")
        void failedProgress() {
            assertThat(UploadStatus.FAILED.getProgress()).isEqualTo(0);
        }

        @Test
        @DisplayName("CANCELLED 상태의 진행률은 0%이다")
        void cancelledProgress() {
            assertThat(UploadStatus.CANCELLED.getProgress()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Enum 기본 동작 테스트")
    class EnumBehaviorTest {

        @Test
        @DisplayName("모든 UploadStatus 값을 조회할 수 있다")
        void allValues() {
            UploadStatus[] statuses = UploadStatus.values();
            assertThat(statuses).hasSize(5);
            assertThat(statuses).containsExactlyInAnyOrder(
                    UploadStatus.PENDING,
                    UploadStatus.UPLOADING,
                    UploadStatus.COMPLETED,
                    UploadStatus.FAILED,
                    UploadStatus.CANCELLED
            );
        }

        @Test
        @DisplayName("문자열로 UploadStatus를 조회할 수 있다")
        void valueOf() {
            assertThat(UploadStatus.valueOf("PENDING")).isEqualTo(UploadStatus.PENDING);
            assertThat(UploadStatus.valueOf("COMPLETED")).isEqualTo(UploadStatus.COMPLETED);
        }

        @Test
        @DisplayName("Enum의 name()은 정확한 이름을 반환한다")
        void name() {
            assertThat(UploadStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(UploadStatus.UPLOADING.name()).isEqualTo("UPLOADING");
        }
    }
}
