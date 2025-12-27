package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/** 외부 다운로드를 찾을 수 없을 때 발생하는 예외. */
public class ExternalDownloadNotFoundException extends DomainException {

    public ExternalDownloadNotFoundException(String externalDownloadId) {
        super(
                ExternalDownloadErrorCode.EXTERNAL_DOWNLOAD_NOT_FOUND,
                "ExternalDownload not found: " + externalDownloadId);
    }
}
