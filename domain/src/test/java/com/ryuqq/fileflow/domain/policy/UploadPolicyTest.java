package com.ryuqq.fileflow.domain.policy;

import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadPolicy Aggregate Root 테스트
 */
@DisplayName("UploadPolicy Aggregate Root 테스트")
class UploadPolicyTest {

    @Test
    @DisplayName("정상적인 값으로 UploadPolicy 생성 성공")
    void createUploadPolicy_Success() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        // when
        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // then
        assertThat(policy.getPolicyKey()).isEqualTo(policyKey);
        assertThat(policy.getFileTypePolicies()).isEqualTo(policies);
        assertThat(policy.getRateLimiting()).isEqualTo(rateLimiting);
        assertThat(policy.getVersion()).isEqualTo(1);
        assertThat(policy.isActive()).isFalse();
        assertThat(policy.getEffectiveFrom()).isEqualTo(effectiveFrom);
        assertThat(policy.getEffectiveUntil()).isEqualTo(effectiveUntil);
    }

    @Test
    @DisplayName("PolicyKey가 null일 때 예외 발생")
    void createUploadPolicy_PolicyKeyNull_ThrowsException() {
        // given
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                null,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PolicyKey cannot be null");
    }

    @Test
    @DisplayName("FileTypePolicies가 null일 때 예외 발생")
    void createUploadPolicy_FileTypePoliciesNull_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                policyKey,
                null,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileTypePolicies cannot be null");
    }

    @Test
    @DisplayName("RateLimiting이 null일 때 예외 발생")
    void createUploadPolicy_RateLimitingNull_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                policyKey,
                policies,
                null,
                effectiveFrom,
                effectiveUntil
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RateLimiting cannot be null");
    }

    @Test
    @DisplayName("effectiveFrom이 null일 때 예외 발생")
    void createUploadPolicy_EffectiveFromNull_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusMonths(1);

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                null,
                effectiveUntil
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EffectiveFrom cannot be null");
    }

    @Test
    @DisplayName("effectiveUntil이 null일 때 예외 발생")
    void createUploadPolicy_EffectiveUntilNull_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EffectiveUntil cannot be null");
    }

    @Test
    @DisplayName("effectiveFrom이 effectiveUntil보다 이후일 때 예외 발생")
    void createUploadPolicy_EffectiveFromAfterEffectiveUntil_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.minusDays(1);

        // when & then
        assertThatThrownBy(() -> UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EffectiveFrom must be before EffectiveUntil");
    }

    @Test
    @DisplayName("activate: 정책 활성화 성공")
    void activate_Success() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when
        UploadPolicy activatedPolicy = policy.activate();

        // then
        assertThat(activatedPolicy.isActive()).isTrue();
        assertThat(activatedPolicy.getVersion()).isEqualTo(policy.getVersion());
        assertThat(activatedPolicy.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("activate: 이미 활성화된 정책 활성화 시 예외 발생")
    void activate_AlreadyActive_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then
        assertThatThrownBy(() -> {
            @SuppressWarnings("unused")
            UploadPolicy ignored = policy.activate();
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Policy is already active");
    }

    @Test
    @DisplayName("deactivate: 정책 비활성화 성공")
    void deactivate_Success() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when
        UploadPolicy deactivatedPolicy = policy.deactivate();

        // then
        assertThat(deactivatedPolicy.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivate: 이미 비활성화된 정책 비활성화 시 예외 발생")
    void deactivate_AlreadyInactive_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> {
            @SuppressWarnings("unused")
            UploadPolicy ignored = policy.deactivate();
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Policy is already inactive");
    }

    @Test
    @DisplayName("isEffectiveAt: 유효 기간 내 시점은 true 반환")
    void isEffectiveAt_WithinPeriod_ReturnsTrue() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime effectiveUntil = LocalDateTime.of(2025, 12, 31, 23, 59);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThat(policy.isEffectiveAt(LocalDateTime.of(2025, 6, 15, 12, 0))).isTrue();
        assertThat(policy.isEffectiveAt(effectiveFrom)).isTrue();
        assertThat(policy.isEffectiveAt(effectiveUntil)).isTrue();
    }

    @Test
    @DisplayName("isEffectiveAt: 유효 기간 외 시점은 false 반환")
    void isEffectiveAt_OutsidePeriod_ReturnsFalse() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime effectiveUntil = LocalDateTime.of(2025, 12, 31, 23, 59);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThat(policy.isEffectiveAt(LocalDateTime.of(2024, 12, 31, 23, 59))).isFalse();
        assertThat(policy.isEffectiveAt(LocalDateTime.of(2026, 1, 1, 0, 0))).isFalse();
    }

    @Test
    @DisplayName("isEffectiveAt: null 시점은 예외 발생")
    void isEffectiveAt_NullDateTime_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> policy.isEffectiveAt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DateTime cannot be null");
    }

    @Test
    @DisplayName("validateFile: 비활성 정책으로 검증 시 예외 발생")
    void validateFile_InactivePolicy_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> policy.validateFile(FileType.IMAGE, "jpg", 1024, 1))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("Policy is not active");
    }

    @Test
    @DisplayName("validateRateLimit: 정상 범위 내 요청은 통과")
    void validateRateLimit_WithinLimit_Success() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then - no exception
        policy.validateRateLimit(50, 500);
    }

    @Test
    @DisplayName("validateRateLimit: Rate Limit 초과 시 예외 발생")
    void validateRateLimit_ExceedsLimit_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then
        assertThatThrownBy(() -> policy.validateRateLimit(101, 500))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    @DisplayName("equals: 동일한 PolicyKey와 version을 가진 정책은 같음")
    void equals_SamePolicyKeyAndVersion_ReturnsTrue() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies1 = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        FileTypePolicies policies2 = FileTypePolicies.of(
                null,
                new HtmlPolicy(20, 50, true),
                null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy1 = UploadPolicy.reconstitute(
                policyKey, policies1, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );
        UploadPolicy policy2 = UploadPolicy.reconstitute(
                policyKey, policies2, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );

        // when & then
        assertThat(policy1).isEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: 다른 PolicyKey를 가진 정책은 다름")
    void equals_DifferentPolicyKey_ReturnsFalse() {
        // given
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy1 = UploadPolicy.reconstitute(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"), policies, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );
        UploadPolicy policy2 = UploadPolicy.reconstitute(
                PolicyKey.of("b2b", "SELLER", "PRODUCT"), policies, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("hashCode: 동일한 PolicyKey와 version은 같은 hashCode")
    void hashCode_SamePolicyKeyAndVersion_ReturnsSameHashCode() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy1 = UploadPolicy.reconstitute(
                policyKey, policies, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );
        UploadPolicy policy2 = UploadPolicy.reconstitute(
                policyKey, policies, rateLimiting, 1, true, effectiveFrom, effectiveUntil
        );

        // when & then
        assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("b2c");
        assertThat(result).contains("version=1");
        assertThat(result).contains("isActive=false");
    }

    // ========== 추가 테스트: isEffectiveNow, updatePolicy, validateFile ==========

    @Test
    @DisplayName("isEffectiveNow: 현재 시점이 유효 기간 내면 true를 반환한다")
    void isEffectiveNow_CurrentTimeWithinPeriod_ReturnsTrue() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThat(policy.isEffectiveNow()).isTrue();
    }

    @Test
    @DisplayName("isEffectiveNow: 현재 시점이 유효 기간 전이면 false를 반환한다")
    void isEffectiveNow_CurrentTimeBeforePeriod_ReturnsFalse() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().plusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(2);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThat(policy.isEffectiveNow()).isFalse();
    }

    @Test
    @DisplayName("isEffectiveNow: 현재 시점이 유효 기간 후면 false를 반환한다")
    void isEffectiveNow_CurrentTimeAfterPeriod_ReturnsFalse() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(2);
        LocalDateTime effectiveUntil = LocalDateTime.now().minusDays(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThat(policy.isEffectiveNow()).isFalse();
    }

    @Test
    @DisplayName("updatePolicy: 정책 업데이트 시 버전이 증가한다")
    void updatePolicy_IncrementsVersion() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies oldPolicies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        FileTypePolicies newPolicies = FileTypePolicies.of(
                null,
                new HtmlPolicy(20, 50, true),
                null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                oldPolicies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when
        UploadPolicy updatedPolicy = policy.updatePolicy(newPolicies, "admin");

        // then
        assertThat(updatedPolicy.getVersion()).isEqualTo(2);
        assertThat(updatedPolicy.getFileTypePolicies()).isEqualTo(newPolicies);
        assertThat(updatedPolicy.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("updatePolicy: changedBy가 null이면 예외가 발생한다")
    void updatePolicy_NullChangedBy_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> policy.updatePolicy(policies, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangedBy cannot be null or empty");
    }

    @Test
    @DisplayName("updatePolicy: changedBy가 빈 문자열이면 예외가 발생한다")
    void updatePolicy_EmptyChangedBy_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> policy.updatePolicy(policies, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangedBy cannot be null or empty");
    }

    @Test
    @DisplayName("validateFile: 유효 기간이 아닌 경우 예외가 발생한다")
    void validateFile_NotEffective_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().plusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(2);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then
        assertThatThrownBy(() -> policy.validateFile(FileType.IMAGE, "jpg", 1024, 1))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("Policy is not effective at current time");
    }

    @Test
    @DisplayName("validateFile: 지원하지 않는 파일 타입은 예외가 발생한다")
    void validateFile_UnsupportedFileType_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then
        assertThatThrownBy(() -> policy.validateFile(FileType.PDF, "pdf", 1024, 1))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("No policy found for file type");
    }

    @Test
    @DisplayName("validateFile: 활성화되고 유효 기간 내이며 정책이 있는 경우 정상 처리된다")
    void validateFile_ValidConditions_Success() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then - no exception
        policy.validateFile(FileType.IMAGE, "jpg", 1024, 1);
    }

    @Test
    @DisplayName("validateRateLimit: 비활성 정책으로 검증 시 예외 발생")
    void validateRateLimit_InactivePolicy_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().minusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        );

        // when & then
        assertThatThrownBy(() -> policy.validateRateLimit(50, 500))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("Policy is not active");
    }

    @Test
    @DisplayName("validateRateLimit: 유효 기간이 아닌 경우 예외 발생")
    void validateRateLimit_NotEffective_ThrowsException() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now().plusDays(1);
        LocalDateTime effectiveUntil = LocalDateTime.now().plusDays(2);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        // when & then
        assertThatThrownBy(() -> policy.validateRateLimit(50, 500))
                .isInstanceOf(PolicyViolationException.class)
                .hasMessageContaining("Policy is not effective at current time");
    }

    @Test
    @DisplayName("도메인 이벤트를 초기화할 수 있다")
    void clearDomainEvents() {
        // given
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        FileTypePolicies policies = FileTypePolicies.of(
                ImagePolicy.createDefault(),
                null, null, null
        );
        RateLimiting rateLimiting = new RateLimiting(100, 1000);
        LocalDateTime effectiveFrom = LocalDateTime.now();
        LocalDateTime effectiveUntil = effectiveFrom.plusMonths(1);

        UploadPolicy policy = UploadPolicy.create(
                policyKey,
                policies,
                rateLimiting,
                effectiveFrom,
                effectiveUntil
        ).activate();

        assertThat(policy.getDomainEvents()).isNotEmpty();

        // when
        policy.clearDomainEvents();

        // then
        assertThat(policy.getDomainEvents()).isEmpty();
    }
}
