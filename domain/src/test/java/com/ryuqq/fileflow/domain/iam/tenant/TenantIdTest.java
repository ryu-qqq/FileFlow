package com.ryuqq.fileflow.domain.iam.tenant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantId 유효성 검증 테스트
 *
 * @author Claude
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("TenantId 테스트")
class TenantIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 TenantId를 생성할 수 있다")
        void createWithValidValue() {
            // given
            String validValue = "tenant-001";

            // when
            TenantId tenantId = new TenantId(validValue);

            // then
            assertThat(tenantId.value()).isEqualTo(validValue);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNullValue() {
            // given
            String nullValue = null;

            // when & then
            assertThatThrownBy(() -> new TenantId(nullValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 예외가 발생한다")
        void createWithEmptyValue() {
            // given
            String emptyValue = "";

            // when & then
            assertThatThrownBy(() -> new TenantId(emptyValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("공백 문자열로 생성하면 예외가 발생한다")
        void createWithBlankValue() {
            // given
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> new TenantId(blankValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 TenantId는 동등하다")
        void equalityWithSameValue() {
            // given
            TenantId id1 = new TenantId("tenant-001");
            TenantId id2 = new TenantId("tenant-001");

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 TenantId는 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            TenantId id1 = new TenantId("tenant-001");
            TenantId id2 = new TenantId("tenant-002");

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
