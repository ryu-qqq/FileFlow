package com.ryuqq.fileflow.integration.config;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

/**
 * TestContainers 설정 클래스.
 *
 * <p>컨테이너들은 static block에서 한 번만 시작되고 모든 테스트에서 재사용됩니다. withReuse(true)를 사용하여 테스트 실행 간에도 컨테이너를 재사용할 수
 * 있습니다.
 */
@TestConfiguration
public class TestContainersConfig {

    // ========================================
    // MySQL Container
    // ========================================
    public static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER =
                new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                        .withDatabaseName("fileflow_test")
                        .withUsername("test")
                        .withPassword("test")
                        .withReuse(true);
        MYSQL_CONTAINER.start();
    }

    // ========================================
    // Redis Container
    // ========================================
    public static final GenericContainer<?> REDIS_CONTAINER;

    static {
        REDIS_CONTAINER =
                new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                        .withExposedPorts(6379)
                        .withReuse(true);
        REDIS_CONTAINER.start();
    }

    // ========================================
    // LocalStack Container (S3 + SQS)
    // ========================================
    public static final LocalStackContainer LOCALSTACK_CONTAINER;
    // Organization.getS3BucketName()에서 하드코딩된 값과 일치해야 함
    public static final String TEST_BUCKET_NAME = "fileflow-uploads-prod";

    static {
        LOCALSTACK_CONTAINER =
                new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
                        .withServices(S3, SQS)
                        .withReuse(true);
        LOCALSTACK_CONTAINER.start();

        // Create S3 bucket
        createTestBucket();
    }

    private static void createTestBucket() {
        try (S3Client s3Client =
                S3Client.builder()
                        .endpointOverride(LOCALSTACK_CONTAINER.getEndpoint())
                        .region(Region.of(LOCALSTACK_CONTAINER.getRegion()))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                LOCALSTACK_CONTAINER.getAccessKey(),
                                                LOCALSTACK_CONTAINER.getSecretKey())))
                        .forcePathStyle(true)
                        .build()) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET_NAME).build());
        }
    }

    // ========================================
    // Container Property Getters
    // ========================================

    public static String getMySqlJdbcUrl() {
        return MYSQL_CONTAINER.getJdbcUrl();
    }

    public static String getMySqlUsername() {
        return MYSQL_CONTAINER.getUsername();
    }

    public static String getMySqlPassword() {
        return MYSQL_CONTAINER.getPassword();
    }

    public static String getRedisHost() {
        return REDIS_CONTAINER.getHost();
    }

    public static Integer getRedisPort() {
        return REDIS_CONTAINER.getMappedPort(6379);
    }

    public static String getLocalStackEndpoint() {
        return LOCALSTACK_CONTAINER.getEndpoint().toString();
    }

    public static String getLocalStackRegion() {
        return LOCALSTACK_CONTAINER.getRegion();
    }

    public static String getLocalStackAccessKey() {
        return LOCALSTACK_CONTAINER.getAccessKey();
    }

    public static String getLocalStackSecretKey() {
        return LOCALSTACK_CONTAINER.getSecretKey();
    }

    public static String getTestBucketName() {
        return TEST_BUCKET_NAME;
    }

    // ========================================
    // Spring Beans for Test Context
    // ========================================

    @Bean
    public MySQLContainer<?> mysqlContainer() {
        return MYSQL_CONTAINER;
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return REDIS_CONTAINER;
    }

    @Bean
    public LocalStackContainer localStackContainer() {
        return LOCALSTACK_CONTAINER;
    }
}
