package com.ryuqq.fileflow.application.asset.manager.command;

import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 영속화 TransactionManager.
 *
 * <p>Transaction 경계를 담당합니다.
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
public class FileAssetTransactionManager {

    private final FileAssetPersistencePort fileAssetPersistencePort;

    public FileAssetTransactionManager(FileAssetPersistencePort fileAssetPersistencePort) {
        this.fileAssetPersistencePort = fileAssetPersistencePort;
    }

    /**
     * FileAsset을 저장합니다.
     *
     * <p>신규 저장 및 상태 업데이트를 처리합니다.
     *
     * @param fileAsset 저장할 FileAsset
     * @return 저장된 FileAssetId
     */
    public FileAssetId persist(FileAsset fileAsset) {
        return fileAssetPersistencePort.persist(fileAsset);
    }
}
