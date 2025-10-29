package com.ryuqq.fileflow.domain.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Setting Merger Utility
 *
 * <p>3단계 우선순위 병합 전략(ORG > TENANT > DEFAULT)을 담당하는 유틸리티 클래스입니다.</p>
 * <p>여러 레벨의 설정을 병합하여 최종 설정 맵을 생성합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Static Utility - 완전히 stateless, 의존성 없음</li>
 *   <li>✅ Pure Functions - 부작용 없이 결과만 반환</li>
 *   <li>✅ Law of Demeter - 캡슐화된 병합 로직</li>
 *   <li>✅ 불변성 - 원본 Setting 객체 변경 없이 결과 반환</li>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * Map<SettingKey, Setting> merged = SettingMerger.merge(
 *     orgSettings,
 *     tenantSettings,
 *     defaultSettings
 * );
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public final class SettingMerger {

    /**
     * Private 생성자 - 인스턴스화 방지.
     *
     * <p>유틸리티 클래스는 인스턴스화할 수 없습니다.</p>
     *
     * @throws UnsupportedOperationException 항상 예외 발생
     * @author ryu-qqq
     * @since 2025-10-29
     */
    private SettingMerger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 3단계 우선순위 병합을 수행합니다.
     *
     * <p>병합 전략:</p>
     * <ol>
     *   <li>DEFAULT 레벨 설정을 기본값으로 사용</li>
     *   <li>TENANT 레벨 설정으로 덮어쓰기</li>
     *   <li>ORG 레벨 설정으로 최종 덮어쓰기 (최고 우선순위)</li>
     * </ol>
     *
     * <p>Law of Demeter 준수: 병합 로직 캡슐화</p>
     *
     * @param orgSettings 조직 레벨 설정 목록
     * @param tenantSettings 테넌트 레벨 설정 목록
     * @param defaultSettings 기본 레벨 설정 목록
     * @return 병합된 설정 맵 (키 → Setting)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Map<SettingKey, Setting> merge(
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        if (orgSettings == null) {
            orgSettings = Collections.emptyList();
        }
        if (tenantSettings == null) {
            tenantSettings = Collections.emptyList();
        }
        if (defaultSettings == null) {
            defaultSettings = Collections.emptyList();
        }

        Map<SettingKey, Setting> mergedSettings = new LinkedHashMap<>();

        // 1단계: DEFAULT 설정을 기본값으로 추가
        addSettingsToMap(mergedSettings, defaultSettings);

        // 2단계: TENANT 설정으로 덮어쓰기
        addSettingsToMap(mergedSettings, tenantSettings);

        // 3단계: ORG 설정으로 최종 덮어쓰기 (최고 우선순위)
        addSettingsToMap(mergedSettings, orgSettings);

        return Collections.unmodifiableMap(mergedSettings);
    }

    /**
     * 설정 목록을 맵에 추가합니다.
     *
     * <p>동일한 키가 이미 존재하면 덮어씁니다 (우선순위 반영).</p>
     *
     * @param targetMap 대상 맵
     * @param settings 추가할 설정 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static void addSettingsToMap(
        Map<SettingKey, Setting> targetMap,
        List<Setting> settings
    ) {
        if (settings == null || settings.isEmpty()) {
            return;
        }

        for (Setting setting : settings) {
            if (setting != null) {
                targetMap.put(setting.getKey(), setting);
            }
        }
    }

    /**
     * 특정 키의 설정을 병합합니다.
     *
     * <p>3단계 우선순위 중 가장 높은 레벨의 설정을 반환합니다.</p>
     * <p>모든 레벨에 설정이 없으면 Optional.empty()를 반환합니다.</p>
     *
     * @param key 설정 키
     * @param orgSettings 조직 레벨 설정 목록
     * @param tenantSettings 테넌트 레벨 설정 목록
     * @param defaultSettings 기본 레벨 설정 목록
     * @return 병합된 설정 (Optional)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Optional<Setting> mergeByKey(
        SettingKey key,
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        if (key == null) {
            return Optional.empty();
        }

        // 우선순위: ORG > TENANT > DEFAULT
        Optional<Setting> orgSetting = findByKey(key, orgSettings);
        if (orgSetting.isPresent()) {
            return orgSetting;
        }

        Optional<Setting> tenantSetting = findByKey(key, tenantSettings);
        if (tenantSetting.isPresent()) {
            return tenantSetting;
        }

        return findByKey(key, defaultSettings);
    }

    /**
     * 설정 목록에서 특정 키를 가진 설정을 찾습니다.
     *
     * @param key 설정 키
     * @param settings 설정 목록
     * @return 찾은 설정 (Optional)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static Optional<Setting> findByKey(SettingKey key, List<Setting> settings) {
        if (settings == null || settings.isEmpty()) {
            return Optional.empty();
        }

        return settings.stream()
            .filter(setting -> setting.hasKey(key))
            .findFirst();
    }

    /**
     * 병합된 설정 목록을 반환합니다 (List 형태).
     *
     * <p>병합된 맵을 List로 변환하여 반환합니다.</p>
     *
     * @param orgSettings 조직 레벨 설정 목록
     * @param tenantSettings 테넌트 레벨 설정 목록
     * @param defaultSettings 기본 레벨 설정 목록
     * @return 병합된 설정 List
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static List<Setting> mergeToList(
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        Map<SettingKey, Setting> mergedMap = merge(orgSettings, tenantSettings, defaultSettings);
        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 병합 결과를 키-값 맵으로 반환합니다.
     *
     * <p>SettingKey → String(표시값) 형태의 맵을 반환합니다.</p>
     * <p>비밀 키는 자동으로 마스킹됩니다.</p>
     *
     * @param orgSettings 조직 레벨 설정 목록
     * @param tenantSettings 테넌트 레벨 설정 목록
     * @param defaultSettings 기본 레벨 설정 목록
     * @return 병합된 키-값 맵
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Map<String, String> mergeToValueMap(
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        Map<SettingKey, Setting> mergedMap = merge(orgSettings, tenantSettings, defaultSettings);

        return mergedMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getValue(),
                entry -> entry.getValue().getDisplayValue(),
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));
    }

    /**
     * 병합 결과를 원시 값 맵으로 반환합니다 (마스킹 없음).
     *
     * <p>SettingKey → String(원본값) 형태의 맵을 반환합니다.</p>
     * <p>주의: 비밀 키도 원본 값이 노출됩니다. 내부 처리 용도로만 사용하세요.</p>
     *
     * @param orgSettings 조직 레벨 설정 목록
     * @param tenantSettings 테넌트 레벨 설정 목록
     * @param defaultSettings 기본 레벨 설정 목록
     * @return 병합된 원시 값 맵
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static Map<String, String> mergeToRawValueMap(
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        Map<SettingKey, Setting> mergedMap = merge(orgSettings, tenantSettings, defaultSettings);

        return mergedMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getValue(),
                entry -> entry.getValue().getRawValue(),
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));
    }
}
