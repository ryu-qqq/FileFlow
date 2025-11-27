package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.LocalDateTime;

/**
 * FileAsset 삭제 Response.
 *
 * <p>Soft Delete 처리 결과를 반환합니다.
 *
 * @param id 삭제된 파일 자산 ID
 * @param processedAt 처리 시각
 */
public record DeleteFileAssetResponse(String id, LocalDateTime processedAt) {

    public static DeleteFileAssetResponse of(String id, LocalDateTime processedAt) {
        return new DeleteFileAssetResponse(id, processedAt);
    }
}
