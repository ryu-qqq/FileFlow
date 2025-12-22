package com.ryuqq.fileflow.adapter.out.http.adapter;

import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.port.out.client.HttpDownloadPort;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * HTTP 다운로드 어댑터.
 *
 * <p>외부 URL에서 파일을 다운로드하는 Outbound Adapter.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>WebClient로 HTTP GET 요청
 *   <li>응답 헤더에서 Content-Type, Content-Length 추출
 *   <li>응답 본문을 byte[]로 수신
 *   <li>DownloadResult 반환
 * </ol>
 *
 * <p><strong>에러 처리</strong>:
 *
 * <ul>
 *   <li>4xx/5xx 응답: IllegalStateException
 *   <li>타임아웃: WebClientException
 * </ul>
 *
 * <p><strong>메트릭</strong>: {@code downstream.external.api.latency{service=http-download}} 메트릭을
 * 수집합니다.
 */
@Component
public class HttpDownloadAdapter implements HttpDownloadPort {

    private static final Logger log = LoggerFactory.getLogger(HttpDownloadAdapter.class);
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final WebClient downloadWebClient;

    public HttpDownloadAdapter(WebClient downloadWebClient) {
        this.downloadWebClient = downloadWebClient;
    }

    @Override
    @DownstreamMetric(target = "external-api", operation = "download", service = "http-download")
    public DownloadResult download(SourceUrl sourceUrl) {
        String url = sourceUrl.value();
        log.info("HTTP 다운로드 시작: url={}", url);

        try {
            return downloadWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(byte[].class)
                    .map(
                            response -> {
                                HttpHeaders headers = response.getHeaders();
                                byte[] content = response.getBody();

                                if (content == null || content.length == 0) {
                                    throw new IllegalStateException("다운로드 응답 본문이 비어있습니다: " + url);
                                }

                                String contentType = extractContentType(headers);
                                long contentLength = extractContentLength(headers, content);

                                log.info(
                                        "HTTP 다운로드 완료: url={}, contentType={}, contentLength={}",
                                        url,
                                        contentType,
                                        contentLength);

                                return new DownloadResult(content, contentType, contentLength);
                            })
                    .block();

        } catch (WebClientResponseException e) {
            log.error(
                    "HTTP 다운로드 실패: url={}, status={}, message={}",
                    url,
                    e.getStatusCode(),
                    e.getMessage());
            throw new IllegalStateException(
                    "HTTP 다운로드 실패: " + url + ", status=" + e.getStatusCode(), e);

        } catch (Exception e) {
            log.error("HTTP 다운로드 예외: url={}, error={}", url, e.getMessage(), e);
            throw new IllegalStateException("HTTP 다운로드 예외: " + url, e);
        }
    }

    /**
     * Content-Type 추출.
     *
     * @param headers HTTP 응답 헤더
     * @return Content-Type (기본값: application/octet-stream)
     */
    private String extractContentType(HttpHeaders headers) {
        MediaType mediaType = headers.getContentType();
        if (mediaType != null) {
            return mediaType.toString();
        }
        return DEFAULT_CONTENT_TYPE;
    }

    /**
     * Content-Length 추출.
     *
     * <p>헤더에 없으면 실제 바이트 배열 길이 사용.
     *
     * @param headers HTTP 응답 헤더
     * @param content 응답 본문
     * @return Content-Length
     */
    private long extractContentLength(HttpHeaders headers, byte[] content) {
        long headerLength = headers.getContentLength();
        if (headerLength > 0) {
            return headerLength;
        }
        return content.length;
    }
}
