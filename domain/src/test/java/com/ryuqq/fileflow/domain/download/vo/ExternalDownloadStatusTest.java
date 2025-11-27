package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadStatus 단위 테스트")
class ExternalDownloadStatusTest {

    @Nested
    @DisplayName("상태 값 테스트")
    class StatusValueTest {

        @Test
        @DisplayName("모든 상태 값이 존재한다")
        void allStatusesShouldExist() {
            // given & when & then
            assertThat(ExternalDownloadStatus.PENDING).isNotNull();
            assertThat(ExternalDownloadStatus.PROCESSING).isNotNull();
            assertThat(ExternalDownloadStatus.COMPLETED).isNotNull();
            assertThat(ExternalDownloadStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("상태는 4개이다")
        void shouldHaveFourStatuses() {
            // given & when
            ExternalDownloadStatus[] statuses = ExternalDownloadStatus.values();

            // then
            assertThat(statuses).hasSize(4);
        }
    }

    @Nested
    @DisplayName("상태 전이 규칙 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("PENDING에서 PROCESSING으로 전환할 수 있다")
        void pending_CanTransitionToProcessing() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PROCESSING)).isTrue();
        }

        @Test
        @DisplayName("PENDING에서 COMPLETED로 직접 전환할 수 없다")
        void pending_CannotTransitionToCompleted() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("PENDING에서 FAILED로 직접 전환할 수 없다")
        void pending_CannotTransitionToFailed() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("PROCESSING에서 COMPLETED로 전환할 수 있다")
        void processing_CanTransitionToCompleted() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING에서 FAILED로 전환할 수 있다")
        void processing_CanTransitionToFailed() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PROCESSING에서 PENDING으로 전환할 수 있다 (재시도)")
        void processing_CanTransitionToPending() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PENDING)).isTrue();
        }

        @Test
        @DisplayName("COMPLETED에서 다른 상태로 전환할 수 없다")
        void completed_CannotTransitionToAnyStatus() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.COMPLETED;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PENDING)).isFalse();
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PROCESSING)).isFalse();
            assertThat(status.canTransitionTo(ExternalDownloadStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("FAILED에서 다른 상태로 전환할 수 없다")
        void failed_CannotTransitionToAnyStatus() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.FAILED;

            // when & then
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PENDING)).isFalse();
            assertThat(status.canTransitionTo(ExternalDownloadStatus.PROCESSING)).isFalse();
            assertThat(status.canTransitionTo(ExternalDownloadStatus.COMPLETED)).isFalse();
        }
    }

    @Nested
    @DisplayName("종료 상태 테스트")
    class TerminalStateTest {

        @Test
        @DisplayName("COMPLETED는 종료 상태이다")
        void completed_IsTerminalState() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.COMPLETED;

            // when & then
            assertThat(status.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("FAILED는 종료 상태이다")
        void failed_IsTerminalState() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.FAILED;

            // when & then
            assertThat(status.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("PENDING은 종료 상태가 아니다")
        void pending_IsNotTerminalState() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;

            // when & then
            assertThat(status.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PROCESSING은 종료 상태가 아니다")
        void processing_IsNotTerminalState() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;

            // when & then
            assertThat(status.isTerminal()).isFalse();
        }
    }
}
