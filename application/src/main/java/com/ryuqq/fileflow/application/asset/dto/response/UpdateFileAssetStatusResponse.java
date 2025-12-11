package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.Instant;

/**
 * FileAsset 상태 변경 응답 DTO.
 *
 * @param fileAssetId 상태가 변경된 FileAsset ID
 * @param fromStatus 이전 상태
 * @param toStatus 변경된 상태
 * @param statusChangedAt 상태 변경 시각
 */
public record UpdateFileAssetStatusResponse(
        String fileAssetId, String fromStatus, String toStatus, Instant statusChangedAt) {}
