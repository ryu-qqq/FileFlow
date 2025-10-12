package com.ryuqq.fileflow.domain.policy.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PolicyUpdatedEvent Domain Event 테스트
 *
 * 목표: 이벤트 커버리지 58% → 80%+ 달성
 *
 * 테스트 시나리오:
 * - 이벤트 생성 및 검증
 * - 버전 검증 로직 (oldVersion < newVersion)
 * - 불변성 (Record 타입)
 * - DomainEvent 인터페이스 구현
 *
 * @author sangwon-ryu
 */
@DisplayName("PolicyUpdatedEvent Domain Event 테스트")
class PolicyUpdatedEventTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 PolicyUpdatedEvent를 생성할 수 있다")
        void createPolicyUpdatedEvent() {
            // given
            String policyKey = "b2c:CONSUMER:REVIEW";
            int oldVersion = 1;
            int newVersion = 2;
            String changedBy = "admin@example.com";
            LocalDateTime changedAt = LocalDateTime.now();

            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    policyKey,
                    oldVersion,
                    newVersion,
                    changedBy,
                    changedAt
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.policyKey()).isEqualTo(policyKey);
            assertThat(event.oldVersion()).isEqualTo(oldVersion);
            assertThat(event.newVersion()).isEqualTo(newVersion);
            assertThat(event.changedBy()).isEqualTo(changedBy);
            assertThat(event.changedAt()).isEqualTo(changedAt);
        }

        @Test
        @DisplayName("oldVersion이 0이고 newVersion이 1일 때 정상 생성된다")
        void createWithZeroOldVersion() {
            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    0,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.oldVersion()).isEqualTo(0);
            assertThat(event.newVersion()).isEqualTo(1);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 1",
                "1, 2",
                "5, 10",
                "0, 100"
        })
        @DisplayName("다양한 유효한 버전 조합으로 생성할 수 있다")
        void createWithVariousValidVersions(int oldVersion, int newVersion) {
            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    oldVersion,
                    newVersion,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.newVersion()).isGreaterThan(event.oldVersion());
        }
    }

    @Nested
    @DisplayName("Validation 테스트")
    class ValidationTest {

        @Test
        @DisplayName("null policyKey로 생성 시 NullPointerException을 던진다")
        void throwsException_whenPolicyKeyIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    null,
                    1,
                    2,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("policyKey must not be null");
        }

        @Test
        @DisplayName("null changedBy로 생성 시 NullPointerException을 던진다")
        void throwsException_whenChangedByIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    null,
                    LocalDateTime.now()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("changedBy must not be null");
        }

        @Test
        @DisplayName("null changedAt로 생성 시 NullPointerException을 던진다")
        void throwsException_whenChangedAtIsNull() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("changedAt must not be null");
        }

        @Test
        @DisplayName("음수 oldVersion으로 생성 시 IllegalArgumentException을 던진다")
        void throwsException_whenOldVersionIsNegative() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    -1,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("oldVersion must not be negative");
        }

        @Test
        @DisplayName("newVersion이 oldVersion과 같을 때 IllegalArgumentException을 던진다")
        void throwsException_whenNewVersionEqualsOldVersion() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("newVersion")
                    .hasMessageContaining("must be greater than")
                    .hasMessageContaining("oldVersion");
        }

        @Test
        @DisplayName("newVersion이 oldVersion보다 작을 때 IllegalArgumentException을 던진다")
        void throwsException_whenNewVersionIsLessThanOldVersion() {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    2,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("newVersion")
                    .hasMessageContaining("must be greater than")
                    .hasMessageContaining("oldVersion");
        }

        @ParameterizedTest
        @CsvSource({
                "1, 1",
                "5, 5",
                "10, 10",
                "2, 1",
                "10, 5",
                "100, 99"
        })
        @DisplayName("newVersion <= oldVersion인 경우 모두 예외를 던진다")
        void throwsException_whenNewVersionNotGreaterThanOldVersion(int oldVersion, int newVersion) {
            // when & then
            assertThatThrownBy(() -> new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    oldVersion,
                    newVersion,
                    "admin@example.com",
                    LocalDateTime.now()
            ))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스 구현 테스트")
    class DomainEventInterfaceTest {

        @Test
        @DisplayName("occurredOn()은 changedAt을 반환한다")
        void occurredOnReturnsChangedAt() {
            // given
            LocalDateTime changedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    changedAt
            );

            // when
            LocalDateTime occurredOn = event.occurredOn();

            // then
            assertThat(occurredOn).isEqualTo(changedAt);
        }

        @Test
        @DisplayName("eventType()은 'PolicyUpdatedEvent'를 반환한다")
        void eventTypeReturnsPolicyUpdatedEvent() {
            // given
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("PolicyUpdatedEvent");
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
            int oldVersion = 1;
            int newVersion = 2;
            String changedBy = "admin@example.com";
            LocalDateTime changedAt = LocalDateTime.now();

            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    policyKey,
                    oldVersion,
                    newVersion,
                    changedBy,
                    changedAt
            );

            // when & then
            assertThat(event.policyKey()).isEqualTo(policyKey);
            assertThat(event.oldVersion()).isEqualTo(oldVersion);
            assertThat(event.newVersion()).isEqualTo(newVersion);
            assertThat(event.changedBy()).isEqualTo(changedBy);
            assertThat(event.changedAt()).isEqualTo(changedAt);

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
            int oldVersion = 1;
            int newVersion = 2;
            String changedBy = "admin@example.com";
            LocalDateTime changedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            PolicyUpdatedEvent event1 = new PolicyUpdatedEvent(
                    policyKey, oldVersion, newVersion, changedBy, changedAt
            );
            PolicyUpdatedEvent event2 = new PolicyUpdatedEvent(
                    policyKey, oldVersion, newVersion, changedBy, changedAt
            );

            // when & then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsSelf() {
            // given
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
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
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
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
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
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
            PolicyUpdatedEvent event1 = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, 2, "admin@example.com", now
            );
            PolicyUpdatedEvent event2 = new PolicyUpdatedEvent(
                    "b2b:MERCHANT:PRODUCT", 1, 2, "admin@example.com", now
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("oldVersion이 다르면 equals로 다르다")
        void notEqualsWhenOldVersionDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            PolicyUpdatedEvent event1 = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, 3, "admin@example.com", now
            );
            PolicyUpdatedEvent event2 = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW", 2, 3, "admin@example.com", now
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("newVersion이 다르면 equals로 다르다")
        void notEqualsWhenNewVersionDiffers() {
            // given
            LocalDateTime now = LocalDateTime.now();
            PolicyUpdatedEvent event1 = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, 2, "admin@example.com", now
            );
            PolicyUpdatedEvent event2 = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW", 1, 3, "admin@example.com", now
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
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    LocalDateTime.of(2024, 1, 15, 10, 30, 0)
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("PolicyUpdatedEvent");
            assertThat(result).contains("b2c:CONSUMER:REVIEW");
            assertThat(result).contains("oldVersion=1");
            assertThat(result).contains("newVersion=2");
            assertThat(result).contains("admin@example.com");
            assertThat(result).contains("2024-01-15");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("정책 업데이트 이벤트 생성 - 버전 1에서 2로")
        void policyUpdateFromV1ToV2() {
            // given & when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.oldVersion()).isEqualTo(1);
            assertThat(event.newVersion()).isEqualTo(2);
            assertThat(event.newVersion() - event.oldVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("정책 업데이트 이벤트 생성 - 큰 버전 점프")
        void policyUpdateWithLargeVersionJump() {
            // given & when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    10,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.oldVersion()).isEqualTo(1);
            assertThat(event.newVersion()).isEqualTo(10);
            assertThat(event.newVersion() - event.oldVersion()).isEqualTo(9);
        }

        @Test
        @DisplayName("시스템에 의한 자동 정책 업데이트")
        void automaticSystemUpdate() {
            // given & when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:PROFILE",
                    5,
                    6,
                    "system",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.changedBy()).isEqualTo("system");
            assertThat(event.occurredOn()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("특정 시점으로 소급 업데이트")
        void retroactiveUpdate() {
            // given
            LocalDateTime pastDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    0,
                    1,
                    "admin@example.com",
                    pastDate
            );

            // then
            assertThat(event.occurredOn()).isEqualTo(pastDate);
            assertThat(event.occurredOn()).isBefore(LocalDateTime.now());
        }

        @Test
        @DisplayName("B2B 정책 업데이트 시나리오")
        void b2bPolicyUpdate() {
            // given & when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2b:MERCHANT:PRODUCT",
                    3,
                    4,
                    "policy-manager@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.policyKey()).startsWith("b2b:");
            assertThat(event.oldVersion()).isEqualTo(3);
            assertThat(event.newVersion()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("버전 관리 로직 테스트")
    class VersionManagementTest {

        @Test
        @DisplayName("순차적 버전 증가를 검증한다")
        void sequentialVersionIncrement() {
            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    1,
                    2,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.newVersion()).isEqualTo(event.oldVersion() + 1);
        }

        @Test
        @DisplayName("초기 버전(0)에서 시작할 수 있다")
        void startFromZeroVersion() {
            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    0,
                    1,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.oldVersion()).isEqualTo(0);
            assertThat(event.newVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("큰 버전 번호도 처리할 수 있다")
        void handleLargeVersionNumbers() {
            // when
            PolicyUpdatedEvent event = new PolicyUpdatedEvent(
                    "b2c:CONSUMER:REVIEW",
                    999,
                    1000,
                    "admin@example.com",
                    LocalDateTime.now()
            );

            // then
            assertThat(event.oldVersion()).isEqualTo(999);
            assertThat(event.newVersion()).isEqualTo(1000);
        }
    }
}
