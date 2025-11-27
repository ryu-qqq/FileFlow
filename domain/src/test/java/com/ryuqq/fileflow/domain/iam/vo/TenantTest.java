package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tenant 단위 테스트")
class TenantTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("정적 팩토리 메서드로 생성할 수 있다")
        void of_WithValidParams_ShouldCreateTenant() {
            // given
            long id = 1L;
            String name = "Test Tenant";

            // when
            Tenant tenant = Tenant.of(id, name);

            // then
            assertThat(tenant).isNotNull();
            assertThat(tenant.id()).isEqualTo(id);
            assertThat(tenant.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("레코드 생성자로 생성할 수 있다")
        void constructor_WithValidParams_ShouldCreateTenant() {
            // given
            long id = 100L;
            String name = "Another Tenant";

            // when
            Tenant tenant = new Tenant(id, name);

            // then
            assertThat(tenant.id()).isEqualTo(id);
            assertThat(tenant.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("Connectly 테넌트를 생성할 수 있다")
        void connectly_ShouldCreateConnectlyTenant() {
            // when
            Tenant tenant = Tenant.connectly();

            // then
            assertThat(tenant.id()).isEqualTo(1L);
            assertThat(tenant.name()).isEqualTo("Connectly");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("테넌트 ID가 1 미만이면 예외가 발생한다")
        void constructor_WithInvalidId_ShouldThrowException(long invalidId) {
            // when & then
            assertThatThrownBy(() -> Tenant.of(invalidId, "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트 ID는 1 이상이어야 합니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("테넌트명이 null이거나 빈 문자열이면 예외가 발생한다")
        void constructor_WithInvalidName_ShouldThrowException(String invalidName) {
            // when & then
            assertThatThrownBy(() -> Tenant.of(1L, invalidName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트명은 null이거나 빈 문자열일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("isConnectly 테스트")
    class IsConnectlyTest {

        @Test
        @DisplayName("Connectly 테넌트인지 확인할 수 있다")
        void isConnectly_WithConnectlyTenant_ShouldReturnTrue() {
            // given
            Tenant tenant = Tenant.connectly();

            // when & then
            assertThat(tenant.isConnectly()).isTrue();
        }

        @Test
        @DisplayName("id가 1이고 name이 Connectly가 아니면 false를 반환한다")
        void isConnectly_WithDifferentName_ShouldReturnFalse() {
            // given
            Tenant tenant = Tenant.of(1L, "Other");

            // when & then
            assertThat(tenant.isConnectly()).isFalse();
        }

        @Test
        @DisplayName("id가 1이 아니면 false를 반환한다")
        void isConnectly_WithDifferentId_ShouldReturnFalse() {
            // given
            Tenant tenant = Tenant.of(2L, "Connectly");

            // when & then
            assertThat(tenant.isConnectly()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 Tenant는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Tenant tenant1 = Tenant.of(1L, "Test");
            Tenant tenant2 = Tenant.of(1L, "Test");

            // when & then
            assertThat(tenant1).isEqualTo(tenant2);
            assertThat(tenant1.hashCode()).isEqualTo(tenant2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 Tenant는 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Tenant tenant1 = Tenant.of(1L, "Test");
            Tenant tenant2 = Tenant.of(2L, "Test");

            // when & then
            assertThat(tenant1).isNotEqualTo(tenant2);
        }

        @Test
        @DisplayName("다른 name을 가진 Tenant는 동등하지 않다")
        void equals_WithDifferentName_ShouldNotBeEqual() {
            // given
            Tenant tenant1 = Tenant.of(1L, "Test1");
            Tenant tenant2 = Tenant.of(1L, "Test2");

            // when & then
            assertThat(tenant1).isNotEqualTo(tenant2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드 정보를 포함한다")
        void toString_ShouldContainAllFields() {
            // given
            Tenant tenant = Tenant.of(1L, "Connectly");

            // when
            String result = tenant.toString();

            // then
            assertThat(result).contains("Tenant");
            assertThat(result).contains("id=1");
            assertThat(result).contains("Connectly");
        }
    }
}
