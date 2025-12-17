package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tenant 단위 테스트")
class TenantTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 Tenant를 생성할 수 있다")
        void validValues_ShouldCreateTenant() {
            // given
            TenantId tenantId = TenantId.generate();

            // when
            Tenant tenant = new Tenant(tenantId, "TestTenant");

            // then
            assertThat(tenant.id()).isEqualTo(tenantId);
            assertThat(tenant.name()).isEqualTo("TestTenant");
        }

        @Test
        @DisplayName("of 메서드로 Tenant를 생성할 수 있다")
        void of_ShouldCreateTenant() {
            // given
            TenantId tenantId = TenantId.generate();

            // when
            Tenant tenant = Tenant.of(tenantId, "AnotherTenant");

            // then
            assertThat(tenant.id()).isEqualTo(tenantId);
            assertThat(tenant.name()).isEqualTo("AnotherTenant");
        }

        @Test
        @DisplayName("connectly 팩토리 메서드로 기본 테넌트를 생성할 수 있다")
        void connectly_ShouldCreateDefaultTenant() {
            // when
            Tenant tenant = Tenant.connectly();

            // then
            assertThat(tenant.id()).isNotNull();
            assertThat(tenant.name()).isEqualTo("connectly");
        }

        @Test
        @DisplayName("create 팩토리 메서드로 새로운 테넌트를 생성할 수 있다")
        void create_ShouldCreateNewTenant() {
            // when
            Tenant tenant = Tenant.create("NewTenant");

            // then
            assertThat(tenant.id()).isNotNull();
            assertThat(tenant.name()).isEqualTo("NewTenant");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void nullId_ShouldThrowException() {
            assertThatThrownBy(() -> new Tenant(null, "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트 ID는 null일 수 없습니다");
        }

        @Test
        @DisplayName("이름이 null이면 예외가 발생한다")
        void nullName_ShouldThrowException() {
            // given
            TenantId tenantId = TenantId.generate();

            // when & then
            assertThatThrownBy(() -> new Tenant(tenantId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 예외가 발생한다")
        void emptyName_ShouldThrowException() {
            // given
            TenantId tenantId = TenantId.generate();

            // when & then
            assertThatThrownBy(() -> new Tenant(tenantId, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("이름이 공백 문자열이면 예외가 발생한다")
        void blankName_ShouldThrowException() {
            // given
            TenantId tenantId = TenantId.generate();

            // when & then
            assertThatThrownBy(() -> new Tenant(tenantId, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트명은 null이거나 빈 문자열일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("isConnectly 테스트")
    class IsConnectlyTest {

        @Test
        @DisplayName("Connectly 테넌트는 true를 반환한다")
        void connectlyTenant_ShouldReturnTrue() {
            // given
            Tenant tenant = Tenant.connectly();

            // then
            assertThat(tenant.isConnectly()).isTrue();
        }

        @Test
        @DisplayName("다른 테넌트는 false를 반환한다")
        void otherTenant_ShouldReturnFalse() {
            // given
            Tenant tenant = Tenant.create("OtherTenant");

            // then
            assertThat(tenant.isConnectly()).isFalse();
        }

        @Test
        @DisplayName("이름이 다르면 false를 반환한다")
        void differentName_ShouldReturnFalse() {
            // given
            TenantId tenantId = TenantId.generate();
            Tenant tenant = new Tenant(tenantId, "NotConnectly");

            // then
            assertThat(tenant.isConnectly()).isFalse();
        }
    }

    @Nested
    @DisplayName("레코드 동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 Tenant는 동등하다")
        void sameValues_ShouldBeEqual() {
            // given
            TenantId tenantId = TenantId.generate();
            Tenant tenant1 = new Tenant(tenantId, "Test");
            Tenant tenant2 = new Tenant(tenantId, "Test");

            // then
            assertThat(tenant1).isEqualTo(tenant2);
            assertThat(tenant1.hashCode()).isEqualTo(tenant2.hashCode());
        }

        @Test
        @DisplayName("다른 이름을 가진 Tenant는 동등하지 않다")
        void differentName_ShouldNotBeEqual() {
            // given
            TenantId tenantId = TenantId.generate();
            Tenant tenant1 = new Tenant(tenantId, "Test1");
            Tenant tenant2 = new Tenant(tenantId, "Test2");

            // then
            assertThat(tenant1).isNotEqualTo(tenant2);
        }

        @Test
        @DisplayName("다른 ID를 가진 Tenant는 동등하지 않다")
        void differentId_ShouldNotBeEqual() {
            // given
            Tenant tenant1 = new Tenant(TenantId.generate(), "Test");
            Tenant tenant2 = new Tenant(TenantId.generate(), "Test");

            // then
            assertThat(tenant1).isNotEqualTo(tenant2);
        }
    }
}
