package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.mapper.FileAssetMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.FileAssetJpaRepository;
import com.ryuqq.fileflow.domain.upload.vo.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileAssetPersistenceAdapter 통합 테스트
 *
 * Testcontainers를 사용하여 실제 MySQL 환경에서 테스트합니다.
 * - 커버리지 70% 이상 달성 목표
 * - Entity, Mapper, Adapter의 전체 플로우 검증
 *
 * @author sangwon-ryu
 */
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
@org.springframework.context.annotation.Import({FileAssetMapper.class, FileAssetPersistenceAdapter.class, com.ryuqq.fileflow.adapter.persistence.TestApplication.class})
@org.springframework.test.context.ActiveProfiles("test")
class FileAssetPersistenceAdapterTest {

    private static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }

    @Autowired
    private FileAssetPersistenceAdapter adapter;

    @Autowired
    private FileAssetJpaRepository repository;

    private FileAsset testFileAsset;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testFileAsset = FileAsset.create(
                "test-session-id",
                TenantId.of("tenant1"),
                S3Location.of("test-bucket", "uploads/test-file.jpg"),
                CheckSum.sha256("abc123def456789012345678901234567890123456789012345678901234"),
                FileSize.ofBytes(1024000L),
                ContentType.of("image/jpeg")
        );
    }

    @Test
    @DisplayName("FileAsset을 저장하고 조회할 수 있다")
    void save_file_asset() {
        // when
        FileAsset savedAsset = adapter.save(testFileAsset);

        // then
        assertThat(savedAsset).isNotNull();
        assertThat(savedAsset.getFileId()).isNotNull();
        assertThat(savedAsset.getSessionId()).isEqualTo("test-session-id");
        assertThat(savedAsset.getTenantId().value()).isEqualTo("tenant1");
        assertThat(savedAsset.getS3Location().bucket()).isEqualTo("test-bucket");
        assertThat(savedAsset.getS3Location().key()).isEqualTo("uploads/test-file.jpg");
        assertThat(savedAsset.getChecksum().value()).isEqualTo("abc123def456");
        assertThat(savedAsset.getFileSize().bytes()).isEqualTo(1024000L);
        assertThat(savedAsset.getContentType().value()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("null FileAsset으로 저장 시 예외가 발생한다")
    void save_null_file_asset_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("FileAsset 저장 후 조회가 정상적으로 동작한다")
    void save_and_verify_all_fields() {
        // given
        FileAsset savedAsset = adapter.save(testFileAsset);

        // when - Repository를 통한 직접 조회로 검증
        var foundEntity = repository.findByFileId(savedAsset.getFileId().value());

        // then
        assertThat(foundEntity).isPresent();
        assertThat(foundEntity.get().getFileId()).isEqualTo(savedAsset.getFileId().value());
        assertThat(foundEntity.get().getSessionId()).isEqualTo("test-session-id");
        assertThat(foundEntity.get().getTenantId()).isEqualTo("tenant1");
        assertThat(foundEntity.get().getS3Bucket()).isEqualTo("test-bucket");
        assertThat(foundEntity.get().getS3Key()).isEqualTo("uploads/test-file.jpg");
        assertThat(foundEntity.get().getChecksum()).isEqualTo("abc123def456");
        assertThat(foundEntity.get().getFileSize()).isEqualTo(1024000L);
        assertThat(foundEntity.get().getContentType()).isEqualTo("image/jpeg");
        assertThat(foundEntity.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("여러 FileAsset을 저장할 수 있다")
    void save_multiple_file_assets() {
        // given
        FileAsset asset1 = FileAsset.create(
                "session-1",
                TenantId.of("tenant1"),
                S3Location.of("bucket1", "file1.jpg"),
                CheckSum.sha256("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"),
                FileSize.ofBytes(1000L),
                ContentType.of("image/jpeg")
        );

        FileAsset asset2 = FileAsset.create(
                "session-2",
                TenantId.of("tenant2"),
                S3Location.of("bucket2", "file2.png"),
                CheckSum.sha256("fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321"),
                FileSize.ofBytes(2000L),
                ContentType.of("image/png")
        );

        // when
        FileAsset saved1 = adapter.save(asset1);
        FileAsset saved2 = adapter.save(asset2);

        // then
        assertThat(saved1.getFileId()).isNotNull();
        assertThat(saved2.getFileId()).isNotNull();
        assertThat(saved1.getFileId()).isNotEqualTo(saved2.getFileId());

        // 전체 개수 확인
        long count = repository.count();
        assertThat(count).isEqualTo(2);
    }
}
