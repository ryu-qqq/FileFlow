package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;

/**
 * HTTP 다운로드 포트.
 *
 * <p>외부 URL에서 파일을 다운로드하는 기능 제공
 */
public interface HttpDownloadPort {

    /**
     * 외부 URL에서 파일을 다운로드합니다.
     *
     * @param sourceUrl 다운로드할 외부 URL
     * @return 다운로드 결과
     */
    DownloadResult download(SourceUrl sourceUrl);
}
