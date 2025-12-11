package com.ryuqq.fileflow.application.asset.dto.response;

import java.util.List;

/**
 * FileAsset 처리 응답 DTO.
 *
 * <p>이미지 리사이징 및 포맷 변환 처리 결과를 반환합니다.
 *
 * @param fileAssetId 원본 FileAsset ID
 * @param status 처리 후 상태
 * @param processedFiles 처리된 파일 목록
 * @param processedFileCount 처리된 파일 수
 */
public record ProcessFileAssetResponse(
        String fileAssetId,
        String status,
        List<ProcessedFileInfoResponse> processedFiles,
        int processedFileCount) {
    /** Compact Constructor: 불변 리스트 변환. */
    public ProcessFileAssetResponse {
        processedFiles = (processedFiles != null) ? List.copyOf(processedFiles) : List.of();
    }
}
