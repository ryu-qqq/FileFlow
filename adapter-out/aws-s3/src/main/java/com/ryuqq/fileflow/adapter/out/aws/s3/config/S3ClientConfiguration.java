package com.ryuqq.fileflow.adapter.out.aws.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 Client Configuration
 *
 * <p>AWS SDK v2 S3 Client 설정을 담당하는 Configuration 클래스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3Client Bean 생성</li>
 *   <li>S3Presigner Bean 생성</li>
 *   <li>AWS Credentials 및 Region 설정</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ AWS SDK v2 사용</li>
 *   <li>✅ Credentials는 환경변수/프로퍼티로 주입</li>
 *   <li>✅ 재사용 가능한 Client Bean</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties(S3ClientConfiguration.S3Properties.class)
public class S3ClientConfiguration {

    private final S3Properties properties;

    /**
     * 생성자
     *
     * @param properties S3 Properties
     */
    public S3ClientConfiguration(S3Properties properties) {
        this.properties = properties;
    }

    /**
     * S3 Client Bean
     *
     * <p>S3 API 호출을 위한 클라이언트입니다.</p>
     *
     * @return S3Client
     */
    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                    )
                )
            );

        // LocalStack endpoint override (for testing)
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(java.net.URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * S3 Presigner Bean
     *
     * <p>Presigned URL 생성을 위한 클라이언트입니다.</p>
     *
     * @return S3Presigner
     */
    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                    )
                )
            );

        // LocalStack endpoint override (for testing)
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(java.net.URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * S3 Bucket Name Bean
     *
     * <p>S3 버킷 이름을 제공하는 Bean입니다.</p>
     *
     * @return S3 Bucket Name
     */
    @Bean("s3BucketName")
    public String s3BucketName() {
        return properties.getBucket();
    }

    /**
     * S3 Download URL Generator Adapter Bean
     *
     * <p>다운로드 URL 생성을 위한 Adapter Bean입니다.</p>
     *
     * @return S3DownloadUrlGeneratorAdapter
     */
    @Bean
    public com.ryuqq.fileflow.adapter.out.aws.s3.adapter.S3DownloadUrlGeneratorAdapter s3DownloadUrlGeneratorAdapter() {
        return new com.ryuqq.fileflow.adapter.out.aws.s3.adapter.S3DownloadUrlGeneratorAdapter(
            s3Presigner(),
            properties.getBucket()
        );
    }

    /**
     * S3 Properties
     *
     * <p>S3 관련 설정 프로퍼티를 담는 클래스입니다.</p>
     */
    @ConfigurationProperties(prefix = "aws.s3")
    public static class S3Properties {

        private String region;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String endpoint;  // Optional: for LocalStack testing

        /**
         * Region Getter
         *
         * @return AWS Region
         */
        public String getRegion() {
            return region;
        }

        /**
         * Region Setter
         *
         * @param region AWS Region
         */
        public void setRegion(String region) {
            this.region = region;
        }

        /**
         * Access Key Getter
         *
         * @return AWS Access Key
         */
        public String getAccessKey() {
            return accessKey;
        }

        /**
         * Access Key Setter
         *
         * @param accessKey AWS Access Key
         */
        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        /**
         * Secret Key Getter
         *
         * @return AWS Secret Key
         */
        public String getSecretKey() {
            return secretKey;
        }

        /**
         * Secret Key Setter
         *
         * @param secretKey AWS Secret Key
         */
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        /**
         * Bucket Getter
         *
         * @return S3 Bucket Name
         */
        public String getBucket() {
            return bucket;
        }

        /**
         * Bucket Setter
         *
         * @param bucket S3 Bucket Name
         */
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        /**
         * Endpoint Getter
         *
         * @return S3 Endpoint (Optional, for LocalStack)
         */
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Endpoint Setter
         *
         * @param endpoint S3 Endpoint (Optional, for LocalStack)
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
