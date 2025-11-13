package com.ryuqq.fileflow.application.download.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * External Download Worker Configuration Properties
 * Worker 관련 설정을 외부화
 *
 * <p>application.yml에서 설정값을 주입받습니다:</p>
 * <pre>
 * fileflow:
 *   download:
 *     worker:
 *       max-file-size-bytes: 524288000  # 500MB
 *       download-timeout-seconds: 300   # 5 minutes
 *       connection-timeout-millis: 10000 # 10 seconds
 *       read-timeout-millis: 30000      # 30 seconds
 *       max-redirects: 5
 *       user-agent: "FileFlow-Downloader/1.0"
 *       buffer-size: 8192
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "fileflow.download.worker")
public class ExternalDownloadWorkerProperties {

    /**
     * 최대 다운로드 파일 크기 (bytes)
     * 기본값: 500MB
     */
    private long maxFileSizeBytes = 500 * 1024 * 1024;

    /**
     * 다운로드 전체 타임아웃 (초)
     * 기본값: 300초 (5분)
     */
    private int downloadTimeoutSeconds = 300;

    /**
     * HTTP 연결 타임아웃 (밀리초)
     * 기본값: 10000ms (10초)
     */
    private int connectionTimeoutMillis = 10_000;

    /**
     * HTTP 읽기 타임아웃 (밀리초)
     * 기본값: 30000ms (30초)
     */
    private int readTimeoutMillis = 30_000;

    /**
     * 최대 리다이렉트 횟수
     * 기본값: 5회
     */
    private int maxRedirects = 5;

    /**
     * HTTP User-Agent 헤더
     */
    private String userAgent = "FileFlow-Downloader/1.0";

    /**
     * 다운로드 버퍼 크기 (bytes)
     * 기본값: 8KB
     */
    private int bufferSize = 8192;

    /**
     * 최대 재시도 횟수
     * 기본값: 3회
     */
    private int maxRetryCount = 3;

    // Getters and Setters
    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getDownloadTimeoutSeconds() {
        return downloadTimeoutSeconds;
    }

    public void setDownloadTimeoutSeconds(int downloadTimeoutSeconds) {
        this.downloadTimeoutSeconds = downloadTimeoutSeconds;
    }

    public int getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }
}