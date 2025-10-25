package com.ryuqq.fileflow.application.settings.dto;

import java.util.Collections;
import java.util.Map;

/**
 * Merged Settings Response DTO
 *
 * <p>병합된 설정 맵을 반환하기 위한 Response DTO입니다.</p>
 * <p>키-값 쌍으로 구성되며, 비밀 키는 자동으로 마스킹됩니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Response DTO 불변성 - final 필드, unmodifiable map</li>
 *   <li>✅ 비밀 키 마스킹 - displayValue 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class MergedSettingsResponse {

    private final Map<String, String> settings;

    /**
     * MergedSettingsResponse 생성자.
     *
     * @param settings 병합된 설정 맵 (키 → 값)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public MergedSettingsResponse(Map<String, String> settings) {
        this.settings = settings != null
            ? Collections.unmodifiableMap(settings)
            : Collections.emptyMap();
    }

    /**
     * 병합된 설정 맵을 반환합니다.
     *
     * @return 병합된 설정 맵 (불변)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * 특정 키의 설정 값을 반환합니다.
     *
     * @param key 설정 키
     * @return 설정 값 (없으면 null)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String get(String key) {
        return settings.get(key);
    }

    /**
     * 설정이 비어있는지 확인합니다.
     *
     * @return 설정이 비어있으면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isEmpty() {
        return settings.isEmpty();
    }

    /**
     * 설정 개수를 반환합니다.
     *
     * @return 설정 개수
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public int size() {
        return settings.size();
    }
}
