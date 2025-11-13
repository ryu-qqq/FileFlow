package com.ryuqq.fileflow.domain.settings;

/**
 * Setting Level Enum
 *
 * <p>EAV 설정 시스템의 3단계 우선순위 레벨을 정의합니다.</p>
 * <p>우선순위: ORG > TENANT > DEFAULT (높음 > 낮음)</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Domain 불변 객체 - 생성 후 변경 불가</li>
 *   <li>✅ 우선순위 명확성 - priority 필드로 병합 순서 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public enum SettingLevel {
    /**
     * 조직(Organization) 레벨 설정 - 최고 우선순위
     */
    ORG(1),

    /**
     * 테넌트(Tenant) 레벨 설정 - 중간 우선순위
     */
    TENANT(2),

    /**
     * 기본(Default) 레벨 설정 - 최저 우선순위
     */
    DEFAULT(3);

    private final int priority;

    /**
     * SettingLevel 생성자.
     *
     * @param priority 우선순위 (낮을수록 높은 우선순위)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    SettingLevel(int priority) {
        this.priority = priority;
    }

    /**
     * 우선순위 값을 반환합니다.
     *
     * <p>숫자가 낮을수록 높은 우선순위입니다.</p>
     * <p>예: ORG(1) > TENANT(2) > DEFAULT(3)</p>
     *
     * @return 우선순위 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 두 레벨 중 우선순위가 높은 레벨을 반환합니다.
     *
     * <p>Law of Demeter 준수: 비교 로직 캡슐화</p>
     * <p>❌ Bad: level1.getPriority() < level2.getPriority() ? level1 : level2</p>
     * <p>✅ Good: SettingLevel.higherPriority(level1, level2)</p>
     *
     * @param level1 첫 번째 레벨
     * @param level2 두 번째 레벨
     * @return 우선순위가 높은 레벨
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static SettingLevel higherPriority(SettingLevel level1, SettingLevel level2) {
        if (level1 == null) {
            return level2;
        }
        if (level2 == null) {
            return level1;
        }
        return level1.priority < level2.priority ? level1 : level2;
    }

    /**
     * 현재 레벨이 다른 레벨보다 높은 우선순위를 가지는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 비교 로직 캡슐화</p>
     *
     * @param other 비교 대상 레벨
     * @return 현재 레벨이 더 높은 우선순위를 가지면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasHigherPriorityThan(SettingLevel other) {
        if (other == null) {
            return true;
        }
        return this.priority < other.priority;
    }
}
