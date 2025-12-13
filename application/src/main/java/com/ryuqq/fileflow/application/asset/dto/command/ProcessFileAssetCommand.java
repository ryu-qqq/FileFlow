package com.ryuqq.fileflow.application.asset.dto.command;

/**
 * FileAsset 처리 요청 Command.
 *
 * <p>이미지 리사이징 및 포맷 변환 처리를 요청합니다.
 *
 * @param fileAssetId 처리할 FileAsset ID
 */
public record ProcessFileAssetCommand(String fileAssetId) {}
