package com.ryuqq.fileflow.application.download.dto.command;

import com.ryuqq.fileflow.domain.common.vo.AccessType;

public record CreateDownloadTaskCommand(
        String sourceUrl,
        String s3Key,
        String bucket,
        AccessType accessType,
        String purpose,
        String source,
        String callbackUrl) {}
