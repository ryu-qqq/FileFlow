package com.ryuqq.fileflow.integration.test.common.container;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

/**
 * TestContainers 설정.
 *
 * <p>MySQL, Redis, LocalStack(S3) 컨테이너를 관리합니다. 모든 E2E 테스트 Base 클래스에서 이 설정을 사용합니다.
 */
public final class TestContainerConfig {

    private TestContainerConfig() {}

    public static final String BUCKET_NAME = "fileflow-test-bucket";

    // MySQL Container
    @SuppressWarnings("resource")
    public static final MySQLContainer MYSQL =
            new MySQLContainer(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("fileflow_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withCommand(
                            "--character-set-server=utf8mb4",
                            "--collation-server=utf8mb4_unicode_ci")
                    .withReuse(true);

    // Redis Container (keyspace notification 활성화: TTL 만료 이벤트 수신용)
    @SuppressWarnings("resource")
    public static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withCommand("redis-server", "--notify-keyspace-events", "Ex")
                    .withReuse(true);

    // LocalStack Container (S3)
    @SuppressWarnings("resource")
    public static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                    .withServices("s3")
                    .withReuse(true);

    static {
        MYSQL.start();
        REDIS.start();
        LOCALSTACK.start();
        createTestBucket();

        // AWS SDK default credential chain이 LocalStack 인증정보를 사용하도록 설정
        System.setProperty("aws.accessKeyId", LOCALSTACK.getAccessKey());
        System.setProperty("aws.secretAccessKey", LOCALSTACK.getSecretKey());
        System.setProperty("aws.region", LOCALSTACK.getRegion());
    }

    /** TestContainers의 동적 프로퍼티를 Spring 환경에 주입합니다. */
    public static void registerProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);

        // Redis (Spring Data Redis)
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        // LocalStack S3
        registry.add("spring.cloud.aws.s3.endpoint", () -> LOCALSTACK.getEndpoint().toString());
        registry.add("spring.cloud.aws.s3.region", () -> LOCALSTACK.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> LOCALSTACK.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> LOCALSTACK.getSecretKey());
        registry.add("spring.cloud.aws.region.static", () -> LOCALSTACK.getRegion());

        // S3 설정 (S3ClientProperties에서 사용하는 키)
        registry.add("fileflow.s3.bucket", () -> BUCKET_NAME);
        registry.add("fileflow.s3.region", () -> LOCALSTACK.getRegion());
        registry.add("fileflow.s3.endpoint", () -> LOCALSTACK.getEndpoint().toString());

        // Redisson 설정 (RedissonConfig에서 사용하는 키)
        registry.add(
                "redisson.singleServerConfig.address",
                () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    }

    private static void createTestBucket() {
        try (S3Client s3 =
                S3Client.builder()
                        .endpointOverride(LOCALSTACK.getEndpoint())
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                LOCALSTACK.getAccessKey(),
                                                LOCALSTACK.getSecretKey())))
                        .region(Region.of(LOCALSTACK.getRegion()))
                        .forcePathStyle(true)
                        .build()) {
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        }
    }
}
