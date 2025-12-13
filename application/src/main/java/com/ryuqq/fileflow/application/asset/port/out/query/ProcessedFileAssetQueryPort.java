package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import java.util.List;

/**
 * ProcessedFileAsset Query Port.
 *
 * <p>처리된 파일 자산 조회를 위한 출력 포트이다.
 */
public interface ProcessedFileAssetQueryPort {

    /**
     * 원본 FileAsset ID로 ProcessedFileAsset 목록을 조회한다.
     *
     * <p>하나의 원본 이미지에서 생성된 모든 변형 이미지를 조회한다.
     *
     * @param originalAssetId 원본 FileAsset ID (문자열)
     * @return ProcessedFileAsset 목록
     */
    List<ProcessedFileAsset> findByOriginalAssetId(String originalAssetId);

    /**
     * 부모 ProcessedFileAsset ID로 하위 ProcessedFileAsset 목록을 조회한다.
     *
     * <p>HTML에서 추출된 이미지의 경우, 부모 ProcessedFileAsset 아래에 여러 추출 이미지가 계층적으로 존재할 수 있다.
     *
     * @param parentAssetId 부모 ProcessedFileAsset ID (문자열)
     * @return ProcessedFileAsset 목록
     */
    List<ProcessedFileAsset> findByParentAssetId(String parentAssetId);
}
