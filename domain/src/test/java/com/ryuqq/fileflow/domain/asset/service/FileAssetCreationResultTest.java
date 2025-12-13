package com.ryuqq.fileflow.domain.asset.service;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetStatusHistoryFixture;
import com.ryuqq.fileflow.domain.asset.fixture.FileProcessingOutboxFixture;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetCreationResult 단위 테스트")
class FileAssetCreationResultTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void constructor_ShouldSetAllFields() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();
            FileProcessingRequestedEvent domainEvent = createDomainEvent(outbox, fileAsset);

            // when
            FileAssetCreationResult result =
                    new FileAssetCreationResult(fileAsset, statusHistory, outbox, domainEvent);

            // then
            assertThat(result.fileAsset()).isSameAs(fileAsset);
            assertThat(result.statusHistory()).isSameAs(statusHistory);
            assertThat(result.outbox()).isSameAs(outbox);
            assertThat(result.domainEvent()).isSameAs(domainEvent);
        }

        @Test
        @DisplayName("fileAssetId() 메서드는 FileAsset의 ID를 반환한다")
        void fileAssetId_ShouldReturnFileAssetId() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();
            FileProcessingRequestedEvent domainEvent = createDomainEvent(outbox, fileAsset);

            // when
            FileAssetCreationResult result =
                    new FileAssetCreationResult(fileAsset, statusHistory, outbox, domainEvent);

            // then
            assertThat(result.fileAssetId()).isEqualTo(fileAsset.getId());
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("fileAsset이 null이면 예외가 발생한다")
        void constructor_WithNullFileAsset_ShouldThrowException() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();
            FileProcessingRequestedEvent domainEvent = createDomainEvent(outbox, fileAsset);

            // when & then
            assertThatThrownBy(
                            () ->
                                    new FileAssetCreationResult(
                                            null, statusHistory, outbox, domainEvent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAsset은 null일 수 없습니다");
        }

        @Test
        @DisplayName("statusHistory가 null이면 예외가 발생한다")
        void constructor_WithNullStatusHistory_ShouldThrowException() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();
            FileProcessingRequestedEvent domainEvent = createDomainEvent(outbox, fileAsset);

            // when & then
            assertThatThrownBy(
                            () -> new FileAssetCreationResult(fileAsset, null, outbox, domainEvent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("StatusHistory는 null일 수 없습니다");
        }

        @Test
        @DisplayName("outbox가 null이면 예외가 발생한다")
        void constructor_WithNullOutbox_ShouldThrowException() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();
            FileProcessingRequestedEvent domainEvent = createDomainEvent(outbox, fileAsset);

            // when & then
            assertThatThrownBy(
                            () ->
                                    new FileAssetCreationResult(
                                            fileAsset, statusHistory, null, domainEvent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Outbox은 null일 수 없습니다");
        }

        @Test
        @DisplayName("domainEvent가 null이면 예외가 발생한다")
        void constructor_WithNullDomainEvent_ShouldThrowException() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aStatusHistory();
            FileProcessingOutbox outbox = FileProcessingOutboxFixture.aPendingOutbox();

            // when & then
            assertThatThrownBy(
                            () ->
                                    new FileAssetCreationResult(
                                            fileAsset, statusHistory, outbox, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DomainEvent는 null일 수 없습니다");
        }
    }

    private FileProcessingRequestedEvent createDomainEvent(
            FileProcessingOutbox outbox, FileAsset fileAsset) {
        return FileProcessingRequestedEvent.of(
                outbox.getId(),
                fileAsset.getId(),
                "PROCESS_REQUEST",
                outbox.getPayload(),
                ClockFixture.defaultClock());
    }
}
