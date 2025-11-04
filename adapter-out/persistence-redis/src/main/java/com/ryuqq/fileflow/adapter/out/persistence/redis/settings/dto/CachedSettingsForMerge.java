package com.ryuqq.fileflow.adapter.out.persistence.redis.settings.dto;

import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort.SettingsForMerge;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis 캐시용 SettingsForMerge DTO
 *
 * <p>Domain 객체를 Redis에 직접 저장하지 않고 별도 DTO로 변환하여 저장합니다.</p>
 * <p>Hexagonal Architecture 원칙: Adapter layer에서 직렬화 문제 해결</p>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public class CachedSettingsForMerge {

    private List<CachedSetting> orgSettings;
    private List<CachedSetting> tenantSettings;
    private List<CachedSetting> defaultSettings;

    /**
     * 기본 생성자 (Jackson 역직렬화용)
     */
    public CachedSettingsForMerge() {
    }

    /**
     * Domain SettingsForMerge를 CachedSettingsForMerge로 변환
     *
     * @param domain Domain SettingsForMerge
     * @return CachedSettingsForMerge
     */
    public static CachedSettingsForMerge from(SettingsForMerge domain) {
        CachedSettingsForMerge cached = new CachedSettingsForMerge();
        cached.setOrgSettings(
            domain.orgSettings().stream()
                .map(CachedSetting::from)
                .collect(Collectors.toList())
        );
        cached.setTenantSettings(
            domain.tenantSettings().stream()
                .map(CachedSetting::from)
                .collect(Collectors.toList())
        );
        cached.setDefaultSettings(
            domain.defaultSettings().stream()
                .map(CachedSetting::from)
                .collect(Collectors.toList())
        );
        return cached;
    }

    /**
     * CachedSettingsForMerge를 Domain SettingsForMerge로 변환
     *
     * @return Domain SettingsForMerge
     */
    public SettingsForMerge toDomain() {
        return new SettingsForMerge(
            orgSettings.stream()
                .map(CachedSetting::toDomain)
                .collect(Collectors.toList()),
            tenantSettings.stream()
                .map(CachedSetting::toDomain)
                .collect(Collectors.toList()),
            defaultSettings.stream()
                .map(CachedSetting::toDomain)
                .collect(Collectors.toList())
        );
    }

    public List<CachedSetting> getOrgSettings() {
        return orgSettings;
    }

    public void setOrgSettings(List<CachedSetting> orgSettings) {
        this.orgSettings = orgSettings;
    }

    public List<CachedSetting> getTenantSettings() {
        return tenantSettings;
    }

    public void setTenantSettings(List<CachedSetting> tenantSettings) {
        this.tenantSettings = tenantSettings;
    }

    public List<CachedSetting> getDefaultSettings() {
        return defaultSettings;
    }

    public void setDefaultSettings(List<CachedSetting> defaultSettings) {
        this.defaultSettings = defaultSettings;
    }
}
