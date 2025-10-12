package com.ryuqq.fileflow.domain.policy.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PolicyActivatedEvent Domain Event 테스트
 *
 * 목표: 이벤트 커버리지 58% → 80%+ 달성
 *
 * 테스트 시나리오:
 * - 이벤트 생성 및 검증
 * - 불변성 (Record 타입)
 * - Validation 로직
 * - DomainEvent 인터페이스 구현
 *
 * @author sangwon-ryu
 */
@DisplayName("PolicyActivatedEvent Domain Event 테스트")
class PolicyActivatedEventTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 PolicyActivatedEvent를 생성할 수 있다")
        void createPolicyActivatedEvent() {
            // given
            String policyKey = "b2c:CONSUMER:REVIEW";
            int version = 1;
            String activatedBy = "admin@example.com";
            LocalDateTime activatedAt = LocalDateTime.now();

            // when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    policyKey,
                    version,
                    activatedBy,
                    activatedAt
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.policyKey()).isEqualTo(policyKey);
            assertThat(event.version()).isEqualTo(version);
            assertThat(event.activatedBy()).isEqualTo(activatedBy);
            assertThat(event.activatedAt()).isEqualTo(activatedAt);
        }

        @Test
        @DisplayName("version이 0일 때 정상 생성된다")
        void createWithZeroVersion() {
            // when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    0,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.version()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Validation 테스트")
    class ValidationTest {

        @Test
        @DisplayName("null policyKey로 생성 시 NullPointerException을 던진다")
        void throwsException_whenPolicyKeyIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyActivatedEvent(
                    null,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("policyKey must not be null");
        }

        @Test
        @DisplayName("null activatedBy로 생성 시 NullPointerException을 던진다")
        void throwsException_whenActivatedByIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    null,
                    LocalDateTime.now()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("activatedBy must not be null");
        }

        @Test
        @DisplayName("null activatedAt로 생성 시 NullPointerException을 던진다")
        void throwsException_whenActivatedAtIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("activatedAt must not be null");
        }

        @Test
        @DisplayName("음수 version으로 생성 시 IllegalArgumentException을 던진다")
        void throwsException_whenVersionIsNegative() {
            // when & then
            assertThatThrownBy(() -> new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    -1,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("version must not be negative");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100, Integer.MIN_VALUE})
        @DisplayName("다양한 음수 version에 대해 예외를 던진다")
        void throwsException_forVariousNegativeVersions(int negativeVersion) {
            // when & then
            assertThatThrownBy(() -> new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    negativeVersion,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("version must not be negative");
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스 구현 테스트")
    class DomainEventInterfaceTest {

        @Test
        @DisplayName("occurredOn()은 activatedAt을 반환한다")
        void occurredOnReturnsActivatedAt() {
            // given
            LocalDateTime activatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    activatedAt
            );

            // when
            LocalDateTime occurredOn = event.occurredOn();

            // then
            assertThat(occurredOn).isEqualTo(activatedAt);
        }

        @Test
        @DisplayName("eventType()은 'PolicyActivatedEvent'를 반환한다")
        void eventTypeReturnsPolicyActivatedEvent() {
            // given
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("PolicyActivatedEvent");
        }
    }

    @Nested
    @DisplayName("Record 불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("생성된 이벤트의 필드는 변경할 수 없다")
        void fieldsAreImmutable() {
            // given
            String policyKey = "b2c:CONSUMER:REVIEW";
            int version = 1;
            String activatedBy = "admin@example.com";
            LocalDateTime activatedAt = LocalDateTime.now();

            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    policyKey,
                    version,
                    activatedBy,
                    activatedAt
            );

            // when & then
            assertThat(event.policyKey()).isEqualTo(policyKey);
            assertThat(event.version()).isEqualTo(version);
            assertThat(event.activatedBy()).isEqualTo(activatedBy);
            assertThat(event.activatedAt()).isEqualTo(activatedAt);

            // Record는 불변이므로 setter 메서드가 없음
            assertThat(event.getClass().getMethods())
                    .noneMatch(method -> method.getName().startsWith("set"));
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("동일한 값을 가진 두 이벤트는 equals로 같다")
        void equalsWithSameValues() {
            // given
            String policyKey = "b2c:CONSUMER:REVIEW";
            int version = 1;
            String activatedBy = "admin@example.com";
            LocalDateTime activatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            PolicyActivatedEvent event1 = new PolicyActivatedEvent(
                    policyKey, version, activatedBy, activatedAt
            );
            PolicyActivatedEvent event2 = new PolicyActivatedEvent(
                    policyKey, version, activatedBy, activatedAt
            );

            // when & then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsSelf() {
            // given
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // when & then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsNull() {
            // given
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // when & then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsDifferentClass() {
            // given
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // when & then
            assertThat(event).isNotEqualTo("different class");
        }

        @Test
        @DisplayName("policyKey가 다르면 equals로 다르다")
        void notEqualsWhenPolicyKeyDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            PolicyActivatedEvent event1 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin@example.com", now
            );
            PolicyActivatedEvent event2 = new PolicyActivatedEvent(
                    "b2b:MERCHANT:PRODUCT", 1, "admin@example.com", now
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("version이 다르면 equals로 다르다")
        void notEqualsWhenVersionDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            PolicyActivatedEvent event1 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin@example.com", now
            );
            PolicyActivatedEvent event2 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 2, "admin@example.com", now
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("activatedBy가 다르면 equals로 다르다")
        void notEqualsWhenActivatedByDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            PolicyActivatedEvent event1 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin1@example.com", now
            );
            PolicyActivatedEvent event2 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin2@example.com", now
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("activatedAt이 다르면 equals로 다르다")
        void notEqualsWhenActivatedAtDiffers() {
            // given
            PolicyActivatedEvent event1 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin@example.com",
                    LocalDateTime.of(2024, 1, 15, 10, 0, 0)
            );
            PolicyActivatedEvent event2 = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, "admin@example.com",
                    LocalDateTime.of(2024, 1, 15, 11, 0, 0)
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드를 포함한다")
        void toStringContainsAllFields() {
            // given
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.of(2024, 1, 15, 10, 30, 0)
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("PolicyActivatedEvent");
            assertThat(result).contains("b2c:CONSUMER:REVIEW");
            assertThat(result).contains("version=1");
            assertThat(result).contains("admin@example.com");
            assertThat(result).contains("2024-01-15");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("정책 활성화 이벤트 생성 - B2C 시나리오")
        void b2cPolicyActivation() {
            // given & when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.policyKey()).startsWith("b2c:");
            assertThat(event.version()).isGreaterThanOrEqualTo(0);
            assertThat(event.activatedBy()).contains("@");
        }

        @Test
        @DisplayName("정책 활성화 이벤트 생성 - B2B 시나리오")
        void b2bPolicyActivation() {
            // given & when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2b:MERCHANT:PRODUCT",
                    2,
                    "system@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.policyKey()).startsWith("b2b:");
            assertThat(event.version()).isEqualTo(2);
            assertThat(event.activatedBy()).isEqualTo("system@example.com");
        }

        @Test
        @DisplayName("자동 활성화 시스템에 의한 이벤트")
        void automaticSystemActivation() {
            // given & when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:PROFILE",
                    1,
                    "system",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.activatedBy()).isEqualTo("system");
            assertThat(event.occurredOn()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("특정 시점으로 소급 활성화")
        void retroactiveActivation() {
            // given
            LocalDateTime pastDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

            // when
            PolicyActivatedEvent event = new PolicyActivatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    "admin@example.com",
                    pastDate
            );

            // then
            assertThat(event.occurredOn()).isEqualTo(pastDate);
            assertThat(event.occurredOn()).isBefore(LocalDateTime.now());
        }
    }
}
