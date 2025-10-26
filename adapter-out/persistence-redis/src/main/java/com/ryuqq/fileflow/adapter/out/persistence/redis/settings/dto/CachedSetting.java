package com.ryuqq.fileflow.adapter.out.persistence.redis.settings.dto;

import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import java.time.LocalDateTime;

/**
 * Redis 캐시용 Setting DTO
 *
 * <p>Domain Setting 객체를 Redis에 직접 저장하지 않고 별도 DTO로 변환하여 저장합니다.</p>
 * <p>Jackson이 자유롭게 직렬화/역직렬화할 수 있도록 모든 필드를 public으로 노출합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public class CachedSetting {

    private Long id;
    private String keyValue;
    private String settingValue;
    private String settingType;
    private boolean isSecret;
    private String level;
    private Long contextId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 기본 생성자 (Jackson 역직렬화용)
     */
    public CachedSetting() {
    }

    /**
     * Domain Setting을 CachedSetting으로 변환
     *
     * @param domain Domain Setting
     * @return CachedSetting
     */
    public static CachedSetting from(Setting domain) {
        CachedSetting cached = new CachedSetting();
        cached.setId(domain.getId());
        cached.setKeyValue(domain.getKeyValue());
        cached.setSettingValue(domain.getRawValue());
        cached.setSettingType(domain.getValueType().name());
        cached.setIsSecret(domain.isSecret());
        cached.setLevel(domain.getLevel().name());
        cached.setContextId(domain.getContextId());
        cached.setCreatedAt(domain.getCreatedAt());
        cached.setUpdatedAt(domain.getUpdatedAt());
        return cached;
    }

    /**
     * CachedSetting을 Domain Setting으로 변환
     *
     * @return Domain Setting
     */
    public Setting toDomain() {
        SettingKey key = SettingKey.of(keyValue);
        SettingValue value = getIsSecret()
            ? SettingValue.secret(settingValue, SettingType.valueOf(settingType))
            : SettingValue.of(settingValue, SettingType.valueOf(settingType));
        SettingLevel settingLevel = SettingLevel.valueOf(level);

        return Setting.reconstitute(
            id,
            key,
            value,
            settingLevel,
            contextId,
            createdAt,
            updatedAt
        );
    }

    // Getters and Setters for Jackson

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingType() {
        return settingType;
    }

    public void setSettingType(String settingType) {
        this.settingType = settingType;
    }

    public boolean getIsSecret() {
        return isSecret;
    }

    public void setIsSecret(boolean isSecret) {
        this.isSecret = isSecret;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
