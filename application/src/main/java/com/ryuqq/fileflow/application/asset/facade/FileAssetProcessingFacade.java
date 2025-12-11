package com.ryuqq.fileflow.application.asset.facade;

import com.ryuqq.fileflow.application.asset.manager.command.FileAssetStatusHistoryTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileAssetTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.FileProcessingOutboxTransactionManager;
import com.ryuqq.fileflow.application.asset.manager.command.ProcessedFileAssetTransactionManager;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 처리 Facade.
 *
 * <p>FileAsset 저장, 상태 이력, Outbox, ProcessedFileAsset을 조율한다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>FileAsset + StatusHistory + Outbox 저장 조합
 *   <li>상태 변경 시 이력과 함께 저장
 *   <li>처리 완료 시 ProcessedFileAsset 일괄 저장
 * </ul>
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>모든 저장 작업은 하나의 트랜잭션에서 처리
 *   <li>부분 실패 시 전체 롤백
 * </ul>
 */
@Component
public class FileAssetProcessingFacade {

    private static final Logger log = LoggerFactory.getLogger(FileAssetProcessingFacade.class);

    private final FileAssetTransactionManager fileAssetTransactionManager;
    private final FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager;
    private final FileProcessingOutboxTransactionManager outboxTransactionManager;
    private final ProcessedFileAssetTransactionManager processedFileAssetTransactionManager;

    public FileAssetProcessingFacade(
            FileAssetTransactionManager fileAssetTransactionManager,
            FileAssetStatusHistoryTransactionManager statusHistoryTransactionManager,
            FileProcessingOutboxTransactionManager outboxTransactionManager,
            ProcessedFileAssetTransactionManager processedFileAssetTransactionManager) {
        this.fileAssetTransactionManager = fileAssetTransactionManager;
        this.statusHistoryTransactionManager = statusHistoryTransactionManager;
        this.outboxTransactionManager = outboxTransactionManager;
        this.processedFileAssetTransactionManager = processedFileAssetTransactionManager;
    }

    /**
     * 파일 처리 요청을 저장하고 Outbox 이벤트를 등록한다.
     *
     * <p>저장 순서:
     *
     * <ol>
     *   <li>FileAsset 저장 (상태 변경 반영)
     *   <li>StatusHistory 저장 (상태 변경 이력)
     *   <li>Outbox 저장 (비동기 처리 이벤트)
     * </ol>
     *
     * @param fileAsset 저장할 FileAsset
     * @param history 상태 변경 이력
     * @param outbox Outbox 이벤트
     * @return 저장된 FileAsset ID
     */
    @Transactional
    public FileAssetId requestProcessingWithOutbox(
            FileAsset fileAsset, FileAssetStatusHistory history, FileProcessingOutbox outbox) {

        log.info("파일 처리 요청 저장 시작: fileAssetId={}", fileAsset.getIdValue());

        // 1. FileAsset 저장
        FileAssetId savedId = fileAssetTransactionManager.persist(fileAsset);
        log.debug("FileAsset 저장 완료: fileAssetId={}", savedId.getValue());

        // 2. StatusHistory 저장
        statusHistoryTransactionManager.persist(history);
        log.debug("StatusHistory 저장 완료: fileAssetId={}", savedId.getValue());

        // 3. Outbox 저장
        outboxTransactionManager.persist(outbox);
        log.debug("Outbox 저장 완료: fileAssetId={}", savedId.getValue());

        log.info("파일 처리 요청 저장 완료: fileAssetId={}", savedId.getValue());
        return savedId;
    }

    /**
     * FileAsset 상태를 변경하고 이력을 저장한다.
     *
     * <p>저장 순서:
     *
     * <ol>
     *   <li>FileAsset 저장 (상태 변경 반영)
     *   <li>StatusHistory 저장 (상태 변경 이력)
     * </ol>
     *
     * @param fileAsset 저장할 FileAsset (상태 변경된)
     * @param history 상태 변경 이력
     * @return 저장된 FileAsset ID
     */
    @Transactional
    public FileAssetId updateStatusWithHistory(
            FileAsset fileAsset, FileAssetStatusHistory history) {

        log.info(
                "파일 상태 변경 저장 시작: fileAssetId={}, toStatus={}",
                fileAsset.getIdValue(),
                history.getToStatus());

        // 1. FileAsset 저장
        FileAssetId savedId = fileAssetTransactionManager.persist(fileAsset);
        log.debug("FileAsset 저장 완료: fileAssetId={}", savedId.getValue());

        // 2. StatusHistory 저장
        statusHistoryTransactionManager.persist(history);
        log.debug("StatusHistory 저장 완료: fileAssetId={}", savedId.getValue());

        log.info(
                "파일 상태 변경 저장 완료: fileAssetId={}, toStatus={}",
                savedId.getValue(),
                history.getToStatus());
        return savedId;
    }

    /**
     * 처리 완료 상태와 ProcessedFileAsset을 함께 저장한다.
     *
     * <p>저장 순서:
     *
     * <ol>
     *   <li>ProcessedFileAsset 일괄 저장
     *   <li>FileAsset 저장 (상태 변경 반영)
     *   <li>StatusHistory 저장 (상태 변경 이력)
     * </ol>
     *
     * @param fileAsset 저장할 FileAsset (상태 변경된)
     * @param history 상태 변경 이력
     * @param processedAssets 처리된 이미지 목록
     * @return 저장된 FileAsset ID
     */
    @Transactional
    public FileAssetId completeProcessingWithResults(
            FileAsset fileAsset,
            FileAssetStatusHistory history,
            List<ProcessedFileAsset> processedAssets) {

        log.info(
                "파일 처리 완료 저장 시작: fileAssetId={}, processedCount={}",
                fileAsset.getIdValue(),
                processedAssets.size());

        // 1. ProcessedFileAsset 일괄 저장
        processedFileAssetTransactionManager.persistAll(processedAssets);
        log.debug("ProcessedFileAsset 저장 완료: count={}", processedAssets.size());

        // 2. FileAsset 저장
        FileAssetId savedId = fileAssetTransactionManager.persist(fileAsset);
        log.debug("FileAsset 저장 완료: fileAssetId={}", savedId.getValue());

        // 3. StatusHistory 저장
        statusHistoryTransactionManager.persist(history);
        log.debug("StatusHistory 저장 완료: fileAssetId={}", savedId.getValue());

        log.info(
                "파일 처리 완료 저장 완료: fileAssetId={}, toStatus={}",
                savedId.getValue(),
                history.getToStatus());
        return savedId;
    }
}
