package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.PersistenceTestConfiguration;
import com.ryuqq.fileflow.adapter.persistence.mapper.UploadPolicyMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.UploadPolicyJpaRepository;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UploadPolicyPersistenceAdapter 통합 테스트
 *
 * Testcontainers를 사용하여 실제 PostgreSQL 환경에서 테스트합니다.
 * - 커버리지 70% 이상 달성 목표
 * - Entity, Mapper, Adapter의 전체 플로우 검증
 *
 * @author sangwon-ryu
 */
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
@org.springframework.context.annotation.Import({com.ryuqq.fileflow.adapter.persistence.PersistenceTestConfiguration.class, com.ryuqq.fileflow.adapter.persistence.mapper.UploadPolicyMapper.class, com.ryuqq.fileflow.adapter.persistence.adapter.UploadPolicyPersistenceAdapter.class, com.ryuqq.fileflow.adapter.persistence.TestApplication.class})
@org.springframework.test.context.ActiveProfiles("test")
class UploadPolicyPersistenceAdapterTest {

    @Autowired
    private UploadPolicyPersistenceAdapter adapter;

    @Autowired
    private UploadPolicyJpaRepository repository;

    private PolicyKey testPolicyKey;
    private UploadPolicy testPolicy;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testPolicyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");

        FileTypePolicies fileTypePolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null,
                null,
                null
        );

        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        testPolicy = UploadPolicy.create(
                testPolicyKey,
                fileTypePolicies,
                rateLimiting,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }

    @Test
    @DisplayName("정책을 저장하고 조회할 수 있다")
    void save_and_load_policy() {
        // when
        UploadPolicy savedPolicy = adapter.save(testPolicy);

        // then
        assertThat(savedPolicy).isNotNull();
        assertThat(savedPolicy.getPolicyKey()).isEqualTo(testPolicyKey);
        assertThat(savedPolicy.getVersion()).isEqualTo(1);

        // when
        Optional<UploadPolicy> loadedPolicy = adapter.loadByKey(testPolicyKey);

        // then
        assertThat(loadedPolicy).isPresent();
        assertThat(loadedPolicy.get().getPolicyKey()).isEqualTo(testPolicyKey);
    }

    @Test
    @DisplayName("동일한 PolicyKey로 중복 저장 시 예외가 발생한다")
    void save_duplicate_policy_throws_exception() {
        // given
        adapter.save(testPolicy);

        // when & then
        assertThatThrownBy(() -> adapter.save(testPolicy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("활성화된 정책만 조회할 수 있다")
    void load_active_policy_only() {
        // given
        UploadPolicy savedPolicy = adapter.save(testPolicy);

        // when
        Optional<UploadPolicy> activePolicy = adapter.loadActiveByKey(testPolicyKey);

        // then
        assertThat(activePolicy).isEmpty(); // 초기 생성 시 isActive = false

        // when - 활성화 후
        UploadPolicy activatedPolicy = savedPolicy.activate();
        adapter.update(activatedPolicy);

        // then
        Optional<UploadPolicy> loadedActivePolicy = adapter.loadActiveByKey(testPolicyKey);
        assertThat(loadedActivePolicy).isPresent();
        assertThat(loadedActivePolicy.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("정책을 업데이트할 수 있다")
    void update_policy() {
        // given
        UploadPolicy savedPolicy = adapter.save(testPolicy);

        // when
        FileTypePolicies newPolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                new HtmlPolicy(20, 1000, true),
                null,
                null
        );
        UploadPolicy updatedPolicy = savedPolicy.updatePolicy(newPolicies, "test-user");
        adapter.update(updatedPolicy);

        // then
        Optional<UploadPolicy> loadedPolicy = adapter.loadByKey(testPolicyKey);
        assertThat(loadedPolicy).isPresent();
        assertThat(loadedPolicy.get().getVersion()).isEqualTo(2);
        assertThat(loadedPolicy.get().getFileTypePolicies().getHtmlPolicy()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 정책을 업데이트하면 예외가 발생한다")
    void update_non_existent_policy_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.update(testPolicy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("정책을 삭제할 수 있다")
    void delete_policy() {
        // given
        adapter.save(testPolicy);

        // when
        adapter.delete(testPolicyKey);

        // then
        Optional<UploadPolicy> loadedPolicy = adapter.loadByKey(testPolicyKey);
        assertThat(loadedPolicy).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 PolicyKey로 조회 시 Optional.empty를 반환한다")
    void load_non_existent_policy_returns_empty() {
        // given
        PolicyKey nonExistentKey = PolicyKey.of("non", "EXISTENT", "KEY");

        // when
        Optional<UploadPolicy> result = adapter.loadByKey(nonExistentKey);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null PolicyKey로 조회 시 예외가 발생한다")
    void load_with_null_policy_key_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.loadByKey(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("null UploadPolicy로 저장 시 예외가 발생한다")
    void save_null_policy_throws_exception() {
        // when & then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("JSON 직렬화/역직렬화가 정상 동작한다")
    void json_serialization_works() {
        // given
        FileTypePolicies complexPolicies = FileTypePolicies.of(
                new ImagePolicy(10, 5, List.of("jpg", "png"), Dimension.of(1920, 1080)),
                new HtmlPolicy(20, 1000, true),
                new ExcelPolicy(15, 100),
                new PdfPolicy(25, 500)
        );

        UploadPolicy policyWithAllTypes = UploadPolicy.create(
                PolicyKey.of("test", "ALL", "TYPES"),
                complexPolicies,
                new RateLimiting(200, 2000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );

        // when
        UploadPolicy savedPolicy = adapter.save(policyWithAllTypes);
        Optional<UploadPolicy> loadedPolicy = adapter.loadByKey(PolicyKey.of("test", "ALL", "TYPES"));

        // then
        assertThat(loadedPolicy).isPresent();
        assertThat(loadedPolicy.get().getFileTypePolicies().getImagePolicy()).isNotNull();
        assertThat(loadedPolicy.get().getFileTypePolicies().getHtmlPolicy()).isNotNull();
        assertThat(loadedPolicy.get().getFileTypePolicies().getExcelPolicy()).isNotNull();
        assertThat(loadedPolicy.get().getFileTypePolicies().getPdfPolicy()).isNotNull();
        assertThat(loadedPolicy.get().getRateLimiting().requestsPerHour()).isEqualTo(200);
    }
}
