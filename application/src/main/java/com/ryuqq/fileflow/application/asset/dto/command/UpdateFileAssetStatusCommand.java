package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * FileAsset 상태 변경 Command.
 *
 * <p>N8N 워크플로우 등 외부 시스템에서 상태 변경 요청 시 사용합니다.
 *
 * @param fileAssetId 상태를 변경할 FileAsset ID
 * @param toStatus 변경할 상태 (RESIZED, N8N_PROCESSING, N8N_COMPLETED, COMPLETED, FAILED)
 * @param reason 상태 변경 사유 (nullable, 실패 시 오류 메시지 등)
 */
public record UpdateFileAssetStatusCommand(String fileAssetId, String toStatus, String reason) {}
