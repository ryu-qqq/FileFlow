package com.ryuqq.fileflow.application.settings.dto;

import java.time.LocalDateTime;

/**
 * Setting Response DTO
 *
 * <p>설정 정보를 외부로 반환하기 위한 Response DTO입니다.</p>
 * <p>비밀 키는 자동으로 마스킹되어 반환됩니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Response DTO 불변성 - final 필드</li>
 *   <li>✅ 비밀 키 마스킹 - displayValue 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class SettingResponse {

    private final Long id;
    private final String key;
    private final String value;
    private final String type;
    private final String level;
    private final Long contextId;
    private final boolean isSecret;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * SettingResponse 생성자.
     *
     * @param id Setting ID
     * @param key 설정 키
     * @param value 설정 값 (비밀 키는 마스킹됨)
     * @param type 설정 타입
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @param isSecret 비밀 키 여부
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingResponse(
        Long id,
        String key,
        String value,
        String type,
        String level,
        Long contextId,
        boolean isSecret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.type = type;
        this.level = level;
        this.contextId = contextId;
        this.isSecret = isSecret;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Setting ID를 반환합니다.
     *
     * @return Setting ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getId() {
        return id;
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
     * @return 설정 값 (비밀 키는 마스킹됨)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getValue() {
        return value;
    }

    /**
     * 설정 타입을 반환합니다.
     *
     * @return 설정 타입
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getType() {
        return type;
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

    /**
     * 비밀 키 여부를 반환합니다.
     *
     * @return 비밀 키이면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean isSecret() {
        return isSecret;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각을 반환합니다.
     *
     * @return 수정 시각
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
