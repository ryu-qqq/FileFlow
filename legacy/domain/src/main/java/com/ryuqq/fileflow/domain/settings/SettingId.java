package com.ryuqq.fileflow.domain.settings;

/**
 * Setting 식별자
 *
 * <p>Setting의 고유 식별자를 나타내는 Value Object입니다.
 * Java 21 Record를 사용하여 불변성을 보장합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Value Object 패턴 적용</li>
 *   <li>✅ 불변성 보장 (Java Record)</li>
 *   <li>✅ null 허용 (신규 엔티티 생성 시)</li>
 *   <li>✅ 양수 검증 (기존 엔티티)</li>
 * </ul>
 *
 * @param value Setting ID 값 (Long - AUTO_INCREMENT)
 * @author ryu-qqq
 * @since 2025-10-29
 */
public record SettingId(Long value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>null 허용: 새로운 엔티티 생성 시 (AUTO_INCREMENT 전에)</li>
     *   <li>양수만 허용: 기존 엔티티 (save 후)</li>
     * </ul>
     *
     * @throws IllegalArgumentException value가 0 이하인 경우 (null은 허용)
     */
    public SettingId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Setting ID는 양수여야 합니다");
        }
        // null은 허용: 새로운 엔티티를 의미 (save 전)
    }

    /**
     * SettingId 생성 - Static Factory Method
     *
     * @param value Setting ID 값 (Long - AUTO_INCREMENT)
     * @return SettingId 인스턴스
     * @throws IllegalArgumentException value가 0 이하인 경우 (null은 허용)
     * @author ryu-qqq
     * @since 2025-10-29
     */
    public static SettingId of(Long value) {
        return new SettingId(value);
    }
}
