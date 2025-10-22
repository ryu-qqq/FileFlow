package com.ryuqq.fileflow.domain.iam.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tenant Aggregate Root 비즈니스 로직 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("Tenant 테스트")
class TenantTest {

    private TenantId tenantId;
    private TenantName tenantName;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId("tenant-001");
        tenantName = new TenantName("Acme Corporation");
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 Tenant를 생성할 수 있다")
        void createWithValidValues() {
            // when
            Tenant tenant = new Tenant(tenantId, tenantName);

            // then
            assertThat(tenant.getId()).isEqualTo(tenantId);
            assertThat(tenant.getName()).isEqualTo(tenantName);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isDeleted()).isFalse();
            assertThat(tenant.getCreatedAt()).isNotNull();
            assertThat(tenant.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성 직후 Tenant는 활성 상태이다")
        void initiallyActive() {
            // when
            Tenant tenant = new Tenant(tenantId, tenantName);

            // then
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("TenantId가 null이면 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> new Tenant(null, tenantName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("TenantName이 null이면 예외가 발생한다")
        void createWithNullName() {
            // when & then
            assertThatThrownBy(() -> new Tenant(tenantId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("이름 변경 테스트")
    class UpdateNameTest {

        @Test
        @DisplayName("활성 상태의 Tenant 이름을 변경할 수 있다")
        void updateName() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            TenantName newName = new TenantName("Acme Corp");

            // when
            tenant.updateName(newName);

            // then
            assertThat(tenant.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("이름 변경 시 updatedAt이 갱신된다")
        void updateNameUpdatesTimestamp() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            TenantName newName = new TenantName("Acme Corp");
            var originalUpdatedAt = tenant.getUpdatedAt();

            // when
            tenant.updateName(newName);

            // then
            assertThat(tenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("삭제된 Tenant의 이름은 변경할 수 없다")
        void cannotUpdateNameWhenDeleted() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.softDelete();
            TenantName newName = new TenantName("New Name");

            // when & then
            assertThatThrownBy(() -> tenant.updateName(newName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Tenant의 이름은 변경할 수 없습니다");
        }

        @Test
        @DisplayName("null로 이름을 변경하면 예외가 발생한다")
        void cannotUpdateWithNull() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when & then
            assertThatThrownBy(() -> tenant.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 Tenant 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("일시 정지 테스트")
    class SuspendTest {

        @Test
        @DisplayName("활성 Tenant를 일시 정지할 수 있다")
        void suspend() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when
            tenant.suspend();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }

        @Test
        @DisplayName("일시 정지 시 updatedAt이 갱신된다")
        void suspendUpdatesTimestamp() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            var originalUpdatedAt = tenant.getUpdatedAt();

            // when
            tenant.suspend();

            // then
            assertThat(tenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이미 일시 정지된 Tenant는 다시 정지할 수 없다")
        void cannotSuspendTwice() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.suspend();

            // when & then
            assertThatThrownBy(tenant::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 일시 정지된 Tenant입니다");
        }

        @Test
        @DisplayName("삭제된 Tenant는 일시 정지할 수 없다")
        void cannotSuspendWhenDeleted() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.softDelete();

            // when & then
            assertThatThrownBy(tenant::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Tenant는 일시 정지할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("활성화 테스트")
    class ActivateTest {

        @Test
        @DisplayName("일시 정지된 Tenant를 활성화할 수 있다")
        void activate() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.suspend();

            // when
            tenant.activate();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("활성화 시 updatedAt이 갱신된다")
        void activateUpdatesTimestamp() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.suspend();
            var originalUpdatedAt = tenant.getUpdatedAt();

            // when
            tenant.activate();

            // then
            assertThat(tenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이미 활성화된 Tenant는 다시 활성화할 수 없다")
        void cannotActivateTwice() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when & then
            assertThatThrownBy(tenant::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 활성 상태인 Tenant입니다");
        }

        @Test
        @DisplayName("삭제된 Tenant는 활성화할 수 없다")
        void cannotActivateWhenDeleted() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.softDelete();

            // when & then
            assertThatThrownBy(tenant::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Tenant는 활성화할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("소프트 삭제 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("Tenant를 소프트 삭제할 수 있다")
        void softDelete() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when
            tenant.softDelete();

            // then
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }

        @Test
        @DisplayName("소프트 삭제 시 updatedAt이 갱신된다")
        void softDeleteUpdatesTimestamp() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            var originalUpdatedAt = tenant.getUpdatedAt();

            // when
            tenant.softDelete();

            // then
            assertThat(tenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이미 삭제된 Tenant는 다시 삭제할 수 없다")
        void cannotDeleteTwice() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.softDelete();

            // when & then
            assertThatThrownBy(tenant::softDelete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 Tenant입니다");
        }
    }

    @Nested
    @DisplayName("isActive 테스트 (Law of Demeter)")
    class IsActiveTest {

        @Test
        @DisplayName("ACTIVE 상태이고 삭제되지 않으면 true를 반환한다")
        void activeAndNotDeleted() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when & then
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 상태이면 false를 반환한다")
        void suspended() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.suspend();

            // when & then
            assertThat(tenant.isActive()).isFalse();
        }

        @Test
        @DisplayName("삭제되었으면 false를 반환한다")
        void deleted() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);
            tenant.softDelete();

            // when & then
            assertThat(tenant.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("생명주기 통합 테스트")
    class LifecycleTest {

        @Test
        @DisplayName("생성 → 일시정지 → 활성화 시나리오")
        void createSuspendActivate() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when & then - 초기 활성 상태
            assertThat(tenant.isActive()).isTrue();

            // when & then - 일시 정지
            tenant.suspend();
            assertThat(tenant.isActive()).isFalse();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);

            // when & then - 재활성화
            tenant.activate();
            assertThat(tenant.isActive()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        }

        @Test
        @DisplayName("생성 → 이름변경 → 삭제 시나리오")
        void createUpdateDelete() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName);

            // when & then - 이름 변경
            TenantName newName = new TenantName("New Corporation");
            tenant.updateName(newName);
            assertThat(tenant.getName()).isEqualTo(newName);
            assertThat(tenant.isActive()).isTrue();

            // when & then - 소프트 삭제
            tenant.softDelete();
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("시간 제어 테스트 (Clock)")
    class ClockTest {

        private Clock fixedClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
            fixedClock = Clock.fixed(Instant.parse("2025-10-22T00:00:00Z"), ZoneId.of("UTC"));
            fixedTime = LocalDateTime.ofInstant(fixedClock.instant(), ZoneId.of("UTC"));
        }

        @Test
        @DisplayName("고정된 시간으로 Tenant를 생성할 수 있다")
        void createWithFixedClock() {
            // when
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);

            // then
            assertThat(tenant.getCreatedAt()).isEqualTo(fixedTime);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("고정된 시간으로 이름 변경 시 예측 가능한 시간이 기록된다")
        void updateNameWithFixedClock() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);
            TenantName newName = new TenantName("New Name");

            // when
            tenant.updateName(newName);

            // then
            assertThat(tenant.getName()).isEqualTo(newName);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 suspend 시 예측 가능한 시간이 기록된다")
        void suspendWithFixedClock() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);

            // when
            tenant.suspend();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 activate 시 예측 가능한 시간이 기록된다")
        void activateWithFixedClock() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);
            tenant.suspend();

            // when
            tenant.activate();

            // then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 softDelete 시 예측 가능한 시간이 기록된다")
        void softDeleteWithFixedClock() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);

            // when
            tenant.softDelete();

            // then
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("시간이 변경되어도 동일한 Clock을 사용하면 시간이 일관되게 유지된다")
        void consistentTimeWithSameClock() {
            // given
            Tenant tenant = new Tenant(tenantId, tenantName, fixedClock);
            LocalDateTime createdAt = tenant.getCreatedAt();

            // when - 여러 작업 수행
            tenant.updateName(new TenantName("Name 1"));
            tenant.updateName(new TenantName("Name 2"));
            tenant.suspend();
            tenant.activate();

            // then - 모든 시간이 고정된 시간과 동일
            assertThat(tenant.getCreatedAt()).isEqualTo(fixedTime);
            assertThat(tenant.getUpdatedAt()).isEqualTo(fixedTime);
            assertThat(createdAt).isEqualTo(fixedTime);
        }
    }
}
