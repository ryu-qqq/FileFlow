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
 */
@Configuration
public class S3Config {

    private static final int MAX_CONNECTIONS = 100;
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SOCKET_TIMEOUT = Duration.ofSeconds(30);

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
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(ApacheHttpClient.builder()
                        .maxConnections(MAX_CONNECTIONS)
                        .connectionTimeout(CONNECTION_TIMEOUT)
                        .socketTimeout(SOCKET_TIMEOUT)
                )
                .build();
    }

    /**
     * S3Presigner 빈을 생성합니다.
     * Presigned URL 생성에 사용됩니다.
     *
     * @return S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
