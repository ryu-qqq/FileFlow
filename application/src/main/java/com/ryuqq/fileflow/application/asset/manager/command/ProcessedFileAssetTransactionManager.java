package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.ProcessedFileAssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProcessedFileAsset 영속화 TransactionManager.
 *
 * <p>ProcessedFileAsset Aggregate의 영속화를 담당합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 PersistencePort 의존성
 *   <li>persist* 메서드만 허용
 *   <li>@Component + @Transactional 필수
 * </ul>
 */
@Component
@Transactional
public class ProcessedFileAssetTransactionManager {

    private final ProcessedFileAssetPersistencePort persistencePort;

    public ProcessedFileAssetTransactionManager(ProcessedFileAssetPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * ProcessedFileAsset을 저장합니다.
     *
     * @param processedFileAsset 저장할 ProcessedFileAsset
     * @return 저장된 ProcessedFileAsset ID
     */
    public ProcessedFileAssetId persist(ProcessedFileAsset processedFileAsset) {
        return persistencePort.persist(processedFileAsset);
    }

    /**
     * 여러 ProcessedFileAsset을 일괄 저장합니다.
     *
     * @param processedFileAssets 저장할 ProcessedFileAsset 목록
     * @return 저장된 ProcessedFileAsset ID 목록
     */
    public List<ProcessedFileAssetId> persistAll(List<ProcessedFileAsset> processedFileAssets) {
        return persistencePort.persistAll(processedFileAssets);
    }
}
