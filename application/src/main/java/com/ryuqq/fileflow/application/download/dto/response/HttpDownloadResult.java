package com.ryuqq.fileflow.application.download.dto.response;

import java.io.InputStream;

/**
 * HTTP 다운로드 결과
 * InputStream, Content-Length, Content-Type을 포함
 */
public record HttpDownloadResult(
    InputStream inputStream,
    long contentLength,
    String contentType
) {
    /**
     * Content-Length가 유효한지 확인
     *
     * @return Content-Length > 0이면 true
     */
    public boolean hasContentLength() {
        return contentLength > 0;
    }

}
