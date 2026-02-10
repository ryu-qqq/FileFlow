package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;

public class DownloadException extends DomainException {

    public DownloadException(DownloadErrorCode errorCode) {
        super(errorCode);
    }

    public DownloadException(DownloadErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public DownloadException(DownloadErrorCode errorCode, String detail, Map<String, Object> args) {
        super(errorCode, detail, args);
    }
}
