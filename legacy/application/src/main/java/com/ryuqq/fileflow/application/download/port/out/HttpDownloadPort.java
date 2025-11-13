package com.ryuqq.fileflow.application.download.port.out;

import com.ryuqq.fileflow.application.download.dto.response.HttpDownloadResult;

import java.io.IOException;
import java.net.URL;

/**
 * HTTP Download Port
 * 외부 URL에서 파일을 다운로드하기 위한 Port 인터페이스
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>외부 URL에서 파일 다운로드</li>
 *   <li>HTTP 연결 관리</li>
 *   <li>Content-Length 및 Content-Type 제공</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface HttpDownloadPort {

    /**
     * 외부 URL에서 파일을 다운로드
     *
     * @param url 다운로드할 파일의 URL
     * @return HTTP 다운로드 결과
     * @throws IOException 다운로드 실패 시
     */
    HttpDownloadResult download(URL url) throws IOException;


}
