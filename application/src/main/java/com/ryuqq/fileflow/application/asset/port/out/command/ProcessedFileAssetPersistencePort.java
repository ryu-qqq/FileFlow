package com.ryuqq.fileflow.application.asset.port.out.command;

import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import java.util.List;

/**
 * ProcessedFileAsset 영속화 포트.
 *
 * <p>처리된 파일 자산(리사이징 이미지, 포맷 변환 파일 등)의 영속화를 담당한다.
 *
 * <p>persist()는 신규/수정을 JPA merge로 통합 처리한다.
 */
public interface ProcessedFileAssetPersistencePort {

    /**
     * ProcessedFileAsset을 저장한다.
     *
     * <p>신규 저장 및 업데이트를 통합 처리한다 (JPA merge 활용).
     *
     * @param processedFileAsset 저장할 ProcessedFileAsset
     * @return 저장된 ProcessedFileAsset ID
     */
    ProcessedFileAssetId persist(ProcessedFileAsset processedFileAsset);

    /**
     * 여러 ProcessedFileAsset을 일괄 저장한다.
     *
     * <p>이미지 처리 시 여러 변형(LARGE, MEDIUM, THUMBNAIL 등)을 한 번에 저장한다.
     *
     * @param processedFileAssets 저장할 ProcessedFileAsset 목록
     * @return 저장된 ProcessedFileAsset ID 목록
     */
    List<ProcessedFileAssetId> persistAll(List<ProcessedFileAsset> processedFileAssets);
}
