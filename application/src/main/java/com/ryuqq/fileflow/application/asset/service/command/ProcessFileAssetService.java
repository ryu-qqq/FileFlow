package com.ryuqq.fileflow.application.asset.service.command;

import com.ryuqq.fileflow.application.asset.coordinator.ImageProcessingCoordinator;
import com.ryuqq.fileflow.application.asset.dto.command.ProcessFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.application.asset.dto.response.ProcessFileAssetResponse;
import com.ryuqq.fileflow.application.asset.facade.FileAssetProcessingFacade;
import com.ryuqq.fileflow.application.asset.factory.command.FileAssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.command.ProcessFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.application.asset.service.assembler.FileAssetProcessingAssembler;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateResult;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateService;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * FileAsset 처리 Service.
 *
 * <p>ProcessFileAssetUseCase 구현체입니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>FileAsset 조회
 *   <li>상태 변경 (PENDING → PROCESSING)
 *   <li>이미지 처리 (다운로드 + 메타데이터 + 리사이징) - ImageProcessingCoordinator
 *   <li>결과 저장 및 상태 변경 (PROCESSING → RESIZED) - FileAssetProcessingFacade
 * </ol>
 *
 * <p><strong>의존성 구조</strong>:
 *
 * <ul>
 *   <li>FileAssetQueryPort - 조회
 *   <li>FileAssetUpdateService - 도메인 상태 변경
 *   <li>ImageProcessingCoordinator - 이미지 처리 조율
 *   <li>FileAssetProcessingFacade - DB 저장 조율
 *   <li>FileAssetCommandFactory - Domain 객체 생성
 *   <li>FileAssetProcessingAssembler - Response DTO 변환
 * </ul>
 */
@Service
@ConditionalOnBean(ImageProcessingPort.class)
public class ProcessFileAssetService implements ProcessFileAssetUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessFileAssetService.class);

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetUpdateService fileAssetUpdateService;
    private final ImageProcessingCoordinator processingCoordinator;
    private final FileAssetProcessingFacade processingFacade;
    private final FileAssetCommandFactory commandFactory;
    private final FileAssetProcessingAssembler assembler;

    public ProcessFileAssetService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetUpdateService fileAssetUpdateService,
            ImageProcessingCoordinator processingCoordinator,
            FileAssetProcessingFacade processingFacade,
            FileAssetCommandFactory commandFactory,
            FileAssetProcessingAssembler assembler) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetUpdateService = fileAssetUpdateService;
        this.processingCoordinator = processingCoordinator;
        this.processingFacade = processingFacade;
        this.commandFactory = commandFactory;
        this.assembler = assembler;
    }

    @Override
    public ProcessFileAssetResponse execute(ProcessFileAssetCommand command) {
        log.info("파일 처리 시작: fileAssetId={}", command.fileAssetId());

        // 1. FileAsset 조회
        FileAsset fileAsset = findFileAsset(command.fileAssetId());

        // 2. 상태 변경 (PENDING → PROCESSING)
        startProcessing(fileAsset);

        try {
            // 3. 이미지 처리 (다운로드 + 메타데이터 + 리사이징)
            List<UploadedImage> uploadedImages = processingCoordinator.process(fileAsset);

            // 4. ProcessedFileAsset 변환
            List<ProcessedFileAsset> processedAssets =
                    convertToProcessedAssets(fileAsset, uploadedImages);

            // 5. 결과 저장 및 완료 처리
            completeProcessing(fileAsset, processedAssets);

            log.info("파일 처리 완료: fileAssetId={}, status=RESIZED", command.fileAssetId());
            return assembler.toResponseForProcess(fileAsset, processedAssets);

        } catch (Exception e) {
            handleFailure(fileAsset, e);
            throw new RuntimeException("파일 처리 실패: " + e.getMessage(), e);
        }
    }

    private FileAsset findFileAsset(String fileAssetId) {
        FileAssetId id = FileAssetId.of(fileAssetId);
        return fileAssetReadManager
                .findById(id)
                .orElseThrow(() -> new FileAssetNotFoundException(fileAssetId));
    }

    private void startProcessing(FileAsset fileAsset) {
        FileAssetUpdateResult startResult = fileAssetUpdateService.startProcessing(fileAsset);
        processingFacade.updateStatusWithHistory(
                startResult.fileAsset(), startResult.statusHistory());
        log.info("상태 변경 완료: PENDING → PROCESSING, fileAssetId={}", fileAsset.getIdValue());
    }

    private List<ProcessedFileAsset> convertToProcessedAssets(
            FileAsset fileAsset, List<UploadedImage> uploadedImages) {
        return uploadedImages.stream()
                .map(uploaded -> commandFactory.createProcessedFileAsset(fileAsset, uploaded))
                .toList();
    }

    private void completeProcessing(FileAsset fileAsset, List<ProcessedFileAsset> processedAssets) {
        FileAssetUpdateResult resizedResult =
                fileAssetUpdateService.markResized(fileAsset, processedAssets.size());
        processingFacade.completeProcessingWithResults(
                resizedResult.fileAsset(), resizedResult.statusHistory(), processedAssets);
    }

    private void handleFailure(FileAsset fileAsset, Exception e) {
        log.error("파일 처리 실패: fileAssetId={}, error={}", fileAsset.getIdValue(), e.getMessage(), e);
        FileAssetUpdateResult failedResult =
                fileAssetUpdateService.markFailed(fileAsset, e.getMessage());
        processingFacade.updateStatusWithHistory(
                failedResult.fileAsset(), failedResult.statusHistory());
    }
}
