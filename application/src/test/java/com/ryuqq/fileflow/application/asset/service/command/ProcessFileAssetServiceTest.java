package com.ryuqq.fileflow.application.asset.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.fileflow.application.asset.coordinator.ImageProcessingCoordinator;
import com.ryuqq.fileflow.application.asset.dto.command.ProcessFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.application.asset.dto.response.ProcessFileAssetResponse;
import com.ryuqq.fileflow.application.asset.facade.FileAssetProcessingFacade;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.service.assembler.FileAssetProcessingAssembler;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetStatusHistoryFixture;
import com.ryuqq.fileflow.domain.asset.fixture.ProcessedFileAssetFixture;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateResult;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateService;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessFileAssetService 테스트")
class ProcessFileAssetServiceTest {

    @Mock private FileAssetReadManager fileAssetReadManager;
    @Mock private FileAssetUpdateService fileAssetUpdateService;
    @Mock private ImageProcessingCoordinator processingCoordinator;
    @Mock private FileAssetProcessingFacade processingFacade;
    @Mock private FileAssetCommandFactory commandFactory;
    @Mock private FileAssetProcessingAssembler assembler;
    @Mock private UploadedImage uploadedImage;

    private ProcessFileAssetService service;

    @BeforeEach
    void setUp() {
        service =
                new ProcessFileAssetService(
                        fileAssetReadManager,
                        fileAssetUpdateService,
                        processingCoordinator,
                        processingFacade,
                        commandFactory,
                        assembler);
    }

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("파일 처리 성공 시 RESIZED 상태로 완료 처리한다")
        void shouldCompleteProcessingSuccessfully() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            ProcessFileAssetCommand command = new ProcessFileAssetCommand(fileAssetId);

            FileAssetStatusHistory startHistory =
                    FileAssetStatusHistoryFixture.aProcessingHistory();
            FileAssetStatusHistory resizedHistory = FileAssetStatusHistoryFixture.aResizedHistory();
            FileAssetUpdateResult startResult = new FileAssetUpdateResult(fileAsset, startHistory);
            FileAssetUpdateResult resizedResult =
                    new FileAssetUpdateResult(fileAsset, resizedHistory);

            List<UploadedImage> uploadedImages = List.of(uploadedImage);
            ProcessedFileAsset processedAsset =
                    ProcessedFileAssetFixture.defaultProcessedFileAsset();
            List<ProcessedFileAsset> processedAssets = List.of(processedAsset);

            ProcessFileAssetResponse expectedResponse =
                    new ProcessFileAssetResponse(fileAssetId, "RESIZED", List.of(), 1);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(fileAssetUpdateService.startProcessing(fileAsset)).willReturn(startResult);
            given(processingFacade.updateStatusWithHistory(any(), any()))
                    .willReturn(fileAsset.getId());
            given(processingCoordinator.process(fileAsset)).willReturn(uploadedImages);
            given(commandFactory.createProcessedFileAsset(eq(fileAsset), eq(uploadedImage)))
                    .willReturn(processedAsset);
            given(fileAssetUpdateService.markResized(fileAsset, 1)).willReturn(resizedResult);
            given(processingFacade.completeProcessingWithResults(any(), any(), any()))
                    .willReturn(fileAsset.getId());
            given(assembler.toResponseForProcess(eq(fileAsset), any()))
                    .willReturn(expectedResponse);

            // when
            ProcessFileAssetResponse result = service.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(result.status()).isEqualTo("RESIZED");

            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verify(fileAssetUpdateService).startProcessing(fileAsset);
            verify(processingCoordinator).process(fileAsset);
            verify(fileAssetUpdateService).markResized(fileAsset, 1);
        }

        @Test
        @DisplayName("존재하지 않는 FileAsset ID인 경우 FileAssetNotFoundException을 던진다")
        void shouldThrowExceptionWhenFileAssetNotFound() {
            // given
            String fileAssetId = "00000000-0000-0000-0000-000000000001";
            ProcessFileAssetCommand command = new ProcessFileAssetCommand(fileAssetId);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(FileAssetNotFoundException.class);

            verify(fileAssetReadManager).findById(FileAssetId.of(fileAssetId));
            verifyNoInteractions(fileAssetUpdateService);
            verifyNoInteractions(processingCoordinator);
        }

        @Test
        @DisplayName("이미지 처리 중 예외 발생 시 FAILED 상태로 변경하고 예외를 다시 던진다")
        void shouldMarkAsFailedWhenProcessingFails() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            ProcessFileAssetCommand command = new ProcessFileAssetCommand(fileAssetId);

            FileAssetStatusHistory startHistory =
                    FileAssetStatusHistoryFixture.aProcessingHistory();
            FileAssetStatusHistory failedHistory = FileAssetStatusHistoryFixture.aFailedHistory();
            FileAssetUpdateResult startResult = new FileAssetUpdateResult(fileAsset, startHistory);
            FileAssetUpdateResult failedResult =
                    new FileAssetUpdateResult(fileAsset, failedHistory);

            RuntimeException processingException = new RuntimeException("이미지 처리 실패");

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(fileAssetUpdateService.startProcessing(fileAsset)).willReturn(startResult);
            given(processingFacade.updateStatusWithHistory(any(), any()))
                    .willReturn(fileAsset.getId());
            given(processingCoordinator.process(fileAsset)).willThrow(processingException);
            given(fileAssetUpdateService.markFailed(eq(fileAsset), any())).willReturn(failedResult);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("파일 처리 실패");

            verify(fileAssetUpdateService).startProcessing(fileAsset);
            verify(fileAssetUpdateService).markFailed(eq(fileAsset), eq("이미지 처리 실패"));
        }

        @Test
        @DisplayName("여러 개의 이미지가 처리된 경우 모두 저장한다")
        void shouldProcessMultipleImages() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            ProcessFileAssetCommand command = new ProcessFileAssetCommand(fileAssetId);

            FileAssetStatusHistory startHistory =
                    FileAssetStatusHistoryFixture.aProcessingHistory();
            FileAssetStatusHistory resizedHistory = FileAssetStatusHistoryFixture.aResizedHistory();
            FileAssetUpdateResult startResult = new FileAssetUpdateResult(fileAsset, startHistory);
            FileAssetUpdateResult resizedResult =
                    new FileAssetUpdateResult(fileAsset, resizedHistory);

            UploadedImage uploadedImage1 = uploadedImage;
            UploadedImage uploadedImage2 = uploadedImage;
            UploadedImage uploadedImage3 = uploadedImage;
            List<UploadedImage> uploadedImages =
                    List.of(uploadedImage1, uploadedImage2, uploadedImage3);

            ProcessedFileAsset processedAsset =
                    ProcessedFileAssetFixture.defaultProcessedFileAsset();
            ProcessFileAssetResponse expectedResponse =
                    new ProcessFileAssetResponse(fileAssetId, "RESIZED", List.of(), 3);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(fileAssetUpdateService.startProcessing(fileAsset)).willReturn(startResult);
            given(processingFacade.updateStatusWithHistory(any(), any()))
                    .willReturn(fileAsset.getId());
            given(processingCoordinator.process(fileAsset)).willReturn(uploadedImages);
            given(commandFactory.createProcessedFileAsset(any(), any())).willReturn(processedAsset);
            given(fileAssetUpdateService.markResized(fileAsset, 3)).willReturn(resizedResult);
            given(processingFacade.completeProcessingWithResults(any(), any(), any()))
                    .willReturn(fileAsset.getId());
            given(assembler.toResponseForProcess(eq(fileAsset), any()))
                    .willReturn(expectedResponse);

            // when
            ProcessFileAssetResponse result = service.execute(command);

            // then
            assertThat(result.processedFileCount()).isEqualTo(3);
            verify(fileAssetUpdateService).markResized(fileAsset, 3);
        }

        @Test
        @DisplayName("빈 이미지 목록인 경우에도 정상 처리된다")
        void shouldHandleEmptyImageList() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            String fileAssetId = fileAsset.getId().getValue();
            ProcessFileAssetCommand command = new ProcessFileAssetCommand(fileAssetId);

            FileAssetStatusHistory startHistory =
                    FileAssetStatusHistoryFixture.aProcessingHistory();
            FileAssetStatusHistory resizedHistory = FileAssetStatusHistoryFixture.aResizedHistory();
            FileAssetUpdateResult startResult = new FileAssetUpdateResult(fileAsset, startHistory);
            FileAssetUpdateResult resizedResult =
                    new FileAssetUpdateResult(fileAsset, resizedHistory);

            List<UploadedImage> emptyUploadedImages = Collections.emptyList();
            ProcessFileAssetResponse expectedResponse =
                    new ProcessFileAssetResponse(fileAssetId, "RESIZED", List.of(), 0);

            given(fileAssetReadManager.findById(FileAssetId.of(fileAssetId)))
                    .willReturn(Optional.of(fileAsset));
            given(fileAssetUpdateService.startProcessing(fileAsset)).willReturn(startResult);
            given(processingFacade.updateStatusWithHistory(any(), any()))
                    .willReturn(fileAsset.getId());
            given(processingCoordinator.process(fileAsset)).willReturn(emptyUploadedImages);
            given(fileAssetUpdateService.markResized(fileAsset, 0)).willReturn(resizedResult);
            given(processingFacade.completeProcessingWithResults(any(), any(), any()))
                    .willReturn(fileAsset.getId());
            given(assembler.toResponseForProcess(eq(fileAsset), any()))
                    .willReturn(expectedResponse);

            // when
            ProcessFileAssetResponse result = service.execute(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.processedFileCount()).isEqualTo(0);

            verify(fileAssetUpdateService).markResized(fileAsset, 0);
        }
    }
}
