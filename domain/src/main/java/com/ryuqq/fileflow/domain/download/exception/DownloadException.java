package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

public class DownloadException extends DomainException {

    public DownloadException(DownloadErrorCode errorCode) {
        super(errorCode);
    }

    public DownloadException(DownloadErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
