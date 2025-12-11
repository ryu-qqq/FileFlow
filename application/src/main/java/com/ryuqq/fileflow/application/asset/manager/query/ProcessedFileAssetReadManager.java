package com.ryuqq.fileflow.application.asset.manager.query;

import com.ryuqq.fileflow.application.asset.port.out.query.ProcessedFileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProcessedFileAsset ReadManager.
 *
 * <p>처리된 파일 자산 조회를 담당하는 ReadManager입니다. 단일 QueryPort만 의존하며, 읽기 전용 트랜잭션으로 동작합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProcessedFileAssetReadManager {

    private final ProcessedFileAssetQueryPort processedFileAssetQueryPort;

    public ProcessedFileAssetReadManager(ProcessedFileAssetQueryPort processedFileAssetQueryPort) {
        this.processedFileAssetQueryPort = processedFileAssetQueryPort;
    }

    /**
     * 원본 FileAsset ID로 ProcessedFileAsset 목록 조회.
     *
     * @param originalAssetId 원본 FileAsset ID
     * @return ProcessedFileAsset 목록
     */
    @Transactional(readOnly = true)
    public List<ProcessedFileAsset> findByOriginalAssetId(String originalAssetId) {
        return processedFileAssetQueryPort.findByOriginalAssetId(originalAssetId);
    }

    /**
     * 부모 ProcessedFileAsset ID로 하위 목록 조회.
     *
     * @param parentAssetId 부모 ProcessedFileAsset ID
     * @return ProcessedFileAsset 목록
     */
    @Transactional(readOnly = true)
    public List<ProcessedFileAsset> findByParentAssetId(String parentAssetId) {
        return processedFileAssetQueryPort.findByParentAssetId(parentAssetId);
    }
}
