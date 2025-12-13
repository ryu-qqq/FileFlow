package com.ryuqq.fileflow.application.asset.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetStatusHistoryTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetStatusHistoryFixture;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateResult;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkFileAssetAsFailedService 테스트")
class MarkFileAssetAsFailedServiceTest {

    @Mock private FileAssetReadManager fileAssetReadManager;

    @Mock private FileAssetTransactionManager fileAssetTransactionManager;

    @Mock private FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager;

    @Mock private FileAssetCommandFactory commandFactory;

    private MarkFileAssetAsFailedService service;

    @BeforeEach
    void setUp() {
        service =
                new MarkFileAssetAsFailedService(
                        fileAssetReadManager,
                        fileAssetTransactionManager,
                        statusHistoryTransactionManager,
                        commandFactory);
    }

    @Nested
    @DisplayName("markAsFailed 메서드")
    class MarkAsFailedTest {

        @Test
        @DisplayName("PROCESSING 상태의 FileAsset을 FAILED로 변경하고 저장한다")
        void shouldMarkProcessingFileAssetAsFailed() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            String errorMessage = "이미지 처리 실패";

            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aFailedHistory();
            FileAssetUpdateResult updateResult =
                    new FileAssetUpdateResult(fileAsset, statusHistory);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(commandFactory.markFailed(eq(fileAsset), eq(errorMessage)))
                    .willReturn(updateResult);

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(commandFactory).markFailed(fileAsset, errorMessage);
            verify(fileAssetTransactionManager).persist(fileAsset);
            verify(statusHistoryTransactionManager).persist(statusHistory);
        }

        @Test
        @DisplayName("존재하지 않는 FileAsset ID인 경우 저장하지 않는다")
        void shouldNotSaveWhenFileAssetNotFound() {
            // given
            String fileAssetId = "00000000-0000-0000-0000-000000000001";
            String errorMessage = "Some error";

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.empty());

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(fileAssetTransactionManager, never()).persist(any());
            verify(statusHistoryTransactionManager, never()).persist(any());
        }

        @Test
        @DisplayName("이미 COMPLETED 상태인 경우 저장하지 않는다")
        void shouldNotSaveWhenAlreadyCompleted() {
            // given
            FileAsset fileAsset = FileAssetFixture.completedFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            String errorMessage = "Error after completion";

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(fileAssetTransactionManager, never()).persist(any());
            verify(statusHistoryTransactionManager, never()).persist(any());
        }

        @Test
        @DisplayName("이미 FAILED 상태인 경우 저장하지 않는다")
        void shouldNotSaveWhenAlreadyFailed() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            fileAsset.failProcessing(ClockFixture.defaultClock()); // FAILED 상태로 변경

            String fileAssetId = fileAsset.getId().getValue();
            String errorMessage = "Second failure";

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(fileAssetTransactionManager, never()).persist(any());
            verify(statusHistoryTransactionManager, never()).persist(any());
        }

        @Test
        @DisplayName("PENDING 상태에서도 FAILED로 변경할 수 있다")
        void shouldMarkPendingFileAssetAsFailed() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            String errorMessage = "Validation failed";

            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aFailedHistory();
            FileAssetUpdateResult updateResult =
                    new FileAssetUpdateResult(fileAsset, statusHistory);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(commandFactory.markFailed(eq(fileAsset), eq(errorMessage)))
                    .willReturn(updateResult);

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(commandFactory).markFailed(fileAsset, errorMessage);
            verify(fileAssetTransactionManager).persist(fileAsset);
            verify(statusHistoryTransactionManager).persist(statusHistory);
        }

        @Test
        @DisplayName("에러 메시지와 함께 실패 처리된다")
        void shouldSaveWithErrorMessage() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            String errorMessage = "DLQ: 이미지 처리 최대 재시도 횟수 초과";

            FileAssetStatusHistory statusHistory = FileAssetStatusHistoryFixture.aFailedHistory();
            FileAssetUpdateResult updateResult =
                    new FileAssetUpdateResult(fileAsset, statusHistory);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(commandFactory.markFailed(eq(fileAsset), eq(errorMessage)))
                    .willReturn(updateResult);

            // when
            service.markAsFailed(fileAssetId, errorMessage);

            // then
            verify(commandFactory).markFailed(fileAsset, errorMessage);
            verify(fileAssetTransactionManager).persist(fileAsset);
            verify(statusHistoryTransactionManager).persist(statusHistory);
        }
    }
}
