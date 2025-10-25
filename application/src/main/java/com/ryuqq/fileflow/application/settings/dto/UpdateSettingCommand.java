package com.ryuqq.fileflow.application.settings.dto;

/**
 * Update Setting Command
 *
 * <p>설정 값을 업데이트하기 위한 Command DTO입니다.</p>
 * <p>JSON 스키마 검증을 거쳐 설정 값을 업데이트합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Command DTO 불변성 - final 필드</li>
 *   <li>✅ 검증 로직 분리 - UseCase에서 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class UpdateSettingCommand {

    private final String key;
    private final String value;
    private final String level;
    private final Long contextId;

    /**
     * UpdateSettingCommand 생성자.
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
     * @param contextId 컨텍스트 ID (ORG/TENANT의 경우 필수, DEFAULT는 null)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public UpdateSettingCommand(String key, String value, String level, Long contextId) {
        this.key = key;
        this.value = value;
        this.level = level;
        this.contextId = contextId;
    }

    /**
     * 설정 키를 반환합니다.
     *
     * @return 설정 키
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getKey() {
        return key;
    }

    /**
     * 설정 값을 반환합니다.
     *
     * @return 설정 값
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getValue() {
        return value;
    }

    /**
     * 설정 레벨을 반환합니다.
     *
     * @return 설정 레벨
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getLevel() {
        return level;
    }

    /**
     * 컨텍스트 ID를 반환합니다.
     *
     * @return 컨텍스트 ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getContextId() {
        return contextId;
    }
}
