package com.ryuqq.fileflow.application.asset.dto.command;

import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

public record RegisterAssetCommand(
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        long fileSize,
        String contentType,
        String etag,
        String extension,
        AssetOrigin origin,
        String originId,
        String purpose,
        String source) {}
