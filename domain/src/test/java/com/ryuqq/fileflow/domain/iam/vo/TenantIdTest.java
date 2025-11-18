package com.ryuqq.fileflow.domain.iam.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantId Value Object 테스트
 */
class TenantIdTest {

    @Test
    @DisplayName("유효한 Long 값으로 TenantId를 생성해야 한다")
    void shouldCreateValidTenantId() {
        // given
        Long validTenantId = 1L;

        // when
        TenantId tenantId = TenantId.of(validTenantId);

        // then
        assertThat(tenantId).isNotNull();
        assertThat(tenantId.value()).isEqualTo(validTenantId);
    }

    @Test
    @DisplayName("null로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // when & then
        assertThatThrownBy(() -> TenantId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId는 null일 수 없습니다");
    }

    @Test
    @DisplayName("0 이하의 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsZeroOrNegative() {
        // when & then
        assertThatThrownBy(() -> TenantId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId는 0보다 커야 합니다");

        assertThatThrownBy(() -> TenantId.of(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("같은 값을 가진 TenantId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        Long value = 100L;
        TenantId tenantId1 = TenantId.of(value);
        TenantId tenantId2 = TenantId.of(value);

        // when & then
        assertThat(tenantId1).isEqualTo(tenantId2);
    }
}
