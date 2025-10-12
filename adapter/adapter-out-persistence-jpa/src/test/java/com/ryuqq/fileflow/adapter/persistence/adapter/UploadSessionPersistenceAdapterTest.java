package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.mapper.UploadSessionMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.ContentType;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
 * UploadSessionPersistenceAdapter 통합 테스트
 *
 * Testcontainers를 사용하여 실제 MySQL 환경에서 테스트합니다.
 * - 커버리지 70% 이상 달성 목표
 * - Entity, Mapper, Adapter의 전체 플로우 검증
 *
 * @author sangwon-ryu
 */
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
@org.springframework.context.annotation.Import({UploadSessionMapper.class, UploadSessionPersistenceAdapter.class, com.ryuqq.fileflow.adapter.persistence.TestApplication.class})
@org.springframework.test.context.ActiveProfiles("test")
class UploadSessionPersistenceAdapterTest {

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
    private UploadSessionPersistenceAdapter adapter;

    @Autowired
    private UploadSessionJpaRepository repository;

    private PolicyKey testPolicyKey;
    private UploadRequest testUploadRequest;
    private UploadSession testSession;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testPolicyKey = PolicyKey.of("tenant1", "CONSUMER", "PRODUCT");

        testUploadRequest = UploadRequest.of(
                "test-image.jpg",
                FileType.IMAGE,
                1024000L,
                "image/jpeg",
                CheckSum.sha256("abc123def456789012345678901234567890123456789012345678901234"),
                IdempotencyKey.generate()
        );

        testSession = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60  // 60분 만료
        );
    }

    @Test
    @DisplayName("세션을 저장하고 조회할 수 있다")
    void save_and_find_session() {
        // when
        UploadSession savedSession = adapter.save(testSession);

        // then
        assertThat(savedSession).isNotNull();
        assertThat(savedSession.getSessionId()).isNotNull();

        // when
        Optional<UploadSession> foundSession = adapter.findById(savedSession.getSessionId());

        // then
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getSessionId()).isEqualTo(savedSession.getSessionId());
        assertThat(foundSession.get().getUploaderId()).isEqualTo("test-uploader");
    }

    @Test
    @DisplayName("IdempotencyKey로 세션을 조회할 수 있다")
    void find_by_idempotency_key() {
        // given
        UploadSession savedSession = adapter.save(testSession);
        IdempotencyKey idempotencyKey = testSession.getUploadRequest().idempotencyKey();

        // when
        Optional<UploadSession> foundSession = adapter.findByIdempotencyKey(idempotencyKey);

        // then
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getSessionId()).isEqualTo(savedSession.getSessionId());
    }

    @Test
    @DisplayName("세션 존재 여부를 확인할 수 있다")
    void exists_by_id() {
        // given
        UploadSession savedSession = adapter.save(testSession);

        // when
        boolean exists = adapter.existsById(savedSession.getSessionId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("세션을 삭제할 수 있다")
    void delete_by_id() {
        // given
        UploadSession savedSession = adapter.save(testSession);

        // when
        adapter.deleteById(savedSession.getSessionId());

        // then
        boolean exists = adapter.existsById(savedSession.getSessionId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("만료된 세션을 조회할 수 있다")
    void find_expired_sessions() {
        // given
        UploadSession expiredSession = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60
        );
        // 만료 시간을 과거로 설정
        UploadSession sessionWithPastExpiry = UploadSession.reconstitute(
                expiredSession.getSessionId(),
                expiredSession.getPolicyKey(),
                expiredSession.getUploadRequest(),
                expiredSession.getUploaderId(),
                expiredSession.getStatus(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1)  // 1시간 전에 만료
        );
        adapter.save(sessionWithPastExpiry);

        // when
        List<UploadSession> expiredSessions = adapter.findExpiredSessions();

        // then
        assertThat(expiredSessions).isNotEmpty();
        assertThat(expiredSessions.get(0).getSessionId()).isEqualTo(sessionWithPastExpiry.getSessionId());
    }

    @Test
    @DisplayName("null 세션으로 저장 시 예외가 발생한다")
    void save_null_session_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("null sessionId로 조회 시 예외가 발생한다")
    void find_with_null_session_id_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("null IdempotencyKey로 조회 시 예외가 발생한다")
    void find_with_null_idempotency_key_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.findByIdempotencyKey(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("존재하지 않는 세션 조회 시 Optional.empty를 반환한다")
    void find_non_existent_session_returns_empty() {
        // when
        Optional<UploadSession> result = adapter.findById("non-existent-session-id");

        // then
        assertThat(result).isEmpty();
    }
}
