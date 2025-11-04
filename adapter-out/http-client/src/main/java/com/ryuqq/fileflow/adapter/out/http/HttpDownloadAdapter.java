package com.ryuqq.fileflow.adapter.out.http;

import com.ryuqq.fileflow.application.download.config.ExternalDownloadWorkerProperties;
import com.ryuqq.fileflow.application.download.dto.response.HttpDownloadResult;
import com.ryuqq.fileflow.application.download.port.out.HttpDownloadPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP Download Adapter
 * HttpURLConnection을 사용하여 외부 URL에서 파일을 다운로드하는 Adapter
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>HTTP 연결 설정 및 관리</li>
 *   <li>InputStream 제공</li>
 *   <li>HTTP 응답 코드 검증</li>
 *   <li>Content-Length 및 Content-Type 추출</li>
 * </ul>
 *
 * <p><strong>설정:</strong></p>
 * <ul>
 *   <li>Connect Timeout: ExternalDownloadWorkerProperties에서 관리</li>
 *   <li>Read Timeout: ExternalDownloadWorkerProperties에서 관리</li>
 *   <li>Follow Redirects: 자동</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class HttpDownloadAdapter implements HttpDownloadPort {

    private static final Logger log = LoggerFactory.getLogger(HttpDownloadAdapter.class);

    private final ExternalDownloadWorkerProperties properties;

    public HttpDownloadAdapter(ExternalDownloadWorkerProperties properties) {
        this.properties = properties;
    }

    /**
     * 외부 URL에서 파일을 다운로드
     *
     * <p>⚠️ 주의: 반환된 InputStream은 반드시 호출자가 close() 해야 합니다.</p>
     *
     * @param url 다운로드할 파일의 URL
     * @return HTTP 다운로드 결과 (InputStream, Content-Length, Content-Type)
     * @throws IOException 다운로드 실패 시
     */
    @Override
    public HttpDownloadResult download(URL url) throws IOException {
        log.info("Starting HTTP download: url={}", url);

        HttpURLConnection connection = null;

        try {
            // HTTP 연결 설정
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(properties.getConnectionTimeoutMillis());
            connection.setReadTimeout(properties.getReadTimeoutMillis());
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            // HTTP 응답 코드 검증
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMessage = String.format(
                    "HTTP error: %d - %s",
                    responseCode,
                    connection.getResponseMessage()
                );
                throw new IOException(errorMessage);
            }

            // Content-Length 및 Content-Type 추출
            long contentLength = connection.getContentLengthLong();
            String contentType = connection.getContentType();

            log.info("HTTP download initiated: url={}, contentLength={}, contentType={}",
                url, contentLength, contentType);

            // InputStream 반환 (호출자가 close() 책임)
            InputStream inputStream = connection.getInputStream();

            return new HttpDownloadResult(
                inputStream,
                contentLength,
                contentType
            );

        } catch (IOException e) {
            // 에러 발생 시 연결 정리
            if (connection != null) {
                connection.disconnect();
            }
            log.error("HTTP download failed: url={}", url, e);
            throw e;
        }
    }
}
