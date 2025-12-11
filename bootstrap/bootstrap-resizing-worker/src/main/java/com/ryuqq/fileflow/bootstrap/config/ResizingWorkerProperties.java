package com.ryuqq.fileflow.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 리사이징 워커 설정 프로퍼티.
 *
 * <p>CPU 바운드 이미지 리사이징 작업을 위한 ForkJoinPool 설정입니다.
 *
 * <p><strong>주의</strong>:
 *
 * <ul>
 *   <li>CPU 바운드 작업이므로 Platform Thread 사용
 *   <li>Virtual Thread 사용 금지
 *   <li>기본값: CPU 코어 수 기반
 * </ul>
 *
 * <p><strong>설정 예시</strong> (application.yml):
 *
 * <pre>{@code
 * fileflow:
 *   resizing:
 *     worker:
 *       pool-size: 8
 * }</pre>
 */
@ConfigurationProperties(prefix = "fileflow.resizing.worker")
public class ResizingWorkerProperties {

    /**
     * ForkJoinPool 병렬 처리 수준.
     *
     * <p>기본값: Runtime.getRuntime().availableProcessors()
     */
    private int poolSize = Runtime.getRuntime().availableProcessors();

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("Pool size must be at least 1");
        }
        this.poolSize = poolSize;
    }

    @Override
    public String toString() {
        return "ResizingWorkerProperties{poolSize=" + poolSize + '}';
    }
}
