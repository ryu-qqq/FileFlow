package com.ryuqq.fileflow.adapter.s3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

/**
 * AWS S3 설정 클래스
 * S3Client와 S3Presigner 빈을 생성합니다.
 *
 * 보안 설정:
 * - DefaultCredentialsProvider를 사용하여 AWS 자격 증명 관리
 * - HTTPS 강제 사용
 * - Apache HTTP Client를 통한 커넥션 풀 관리
 *
 * HTTP 클라이언트 설정:
 * - maxConnections: application.yml에서 설정 가능 (기본값: 100)
 * - connectionTimeout: application.yml에서 설정 가능 (기본값: 10초)
 * - socketTimeout: application.yml에서 설정 가능 (기본값: 30초)
 */
@Configuration
public class S3Config {

    private final S3Properties s3Properties;

    /**
     * S3Config 생성자
     *
     * @param s3Properties S3 설정 프로퍼티
     * @throws IllegalArgumentException s3Properties가 null인 경우
     */
    public S3Config(S3Properties s3Properties) {
        if (s3Properties == null) {
            throw new IllegalArgumentException("S3Properties cannot be null");
        }
        this.s3Properties = s3Properties;
    }

    /**
     * S3Client 빈을 생성합니다.
     * 파일 업로드, 다운로드 등의 S3 작업에 사용됩니다.
     *
     * endpoint 설정:
     * - Production: endpoint가 null이면 기본 AWS S3 사용
     * - Test/LocalStack: endpoint가 설정되면 해당 endpoint 사용
     *
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(ApacheHttpClient.builder()
                        .maxConnections(s3Properties.getMaxConnections())
                        .connectionTimeout(Duration.ofMillis(s3Properties.getConnectionTimeoutMillis()))
                        .socketTimeout(Duration.ofMillis(s3Properties.getSocketTimeoutMillis()))
                );

        // LocalStack 또는 S3-compatible 스토리지 endpoint 설정
        if (s3Properties.getEndpoint() != null && !s3Properties.getEndpoint().isBlank()) {
            builder.endpointOverride(java.net.URI.create(s3Properties.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * S3Presigner 빈을 생성합니다.
     * Presigned URL 생성에 사용됩니다.
     *
     * endpoint 설정:
     * - Production: endpoint가 null이면 기본 AWS S3 사용
     * - Test/LocalStack: endpoint가 설정되면 해당 endpoint 사용
     *
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // LocalStack 또는 S3-compatible 스토리지 endpoint 설정
        if (s3Properties.getEndpoint() != null && !s3Properties.getEndpoint().isBlank()) {
            builder.endpointOverride(java.net.URI.create(s3Properties.getEndpoint()));
        }

        return builder.build();
    }
}
