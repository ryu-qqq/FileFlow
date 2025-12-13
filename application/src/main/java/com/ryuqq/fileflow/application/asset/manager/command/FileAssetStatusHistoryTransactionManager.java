package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetStatusHistoryPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAssetStatusHistory 영속화 TransactionManager.
 *
 * <p>FileAssetStatusHistory Aggregate의 영속화를 담당합니다.
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
public class FileAssetStatusHistoryTransactionManager {

    private final FileAssetStatusHistoryPersistencePort persistencePort;

    public FileAssetStatusHistoryTransactionManager(
            FileAssetStatusHistoryPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * FileAssetStatusHistory를 저장합니다.
     *
     * @param history 저장할 FileAssetStatusHistory
     * @return 저장된 FileAssetStatusHistory ID
     */
    public FileAssetStatusHistoryId persist(FileAssetStatusHistory history) {
        return persistencePort.persist(history);
    }
}
