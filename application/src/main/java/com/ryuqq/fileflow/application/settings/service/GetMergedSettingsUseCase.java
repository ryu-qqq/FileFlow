package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.dto.GetMergedSettingsQuery;
import com.ryuqq.fileflow.application.settings.dto.MergedSettingsResponse;
import com.ryuqq.fileflow.domain.settings.SettingMerger;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Get Merged Settings Use Case
 *
 * <p>3단계 우선순위 병합(ORG > TENANT > DEFAULT)을 수행하는 UseCase입니다.</p>
 * <p>조직, 테넌트, 기본 레벨의 설정을 병합하여 최종 설정 맵을 반환합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ UseCase 패턴 - 하나의 비즈니스 유스케이스</li>
 *   <li>✅ Transaction 경계 - 읽기 전용 트랜잭션</li>
 *   <li>✅ Domain Service 활용 - SettingMerger 위임</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Service
@Transactional(readOnly = true)
public class GetMergedSettingsUseCase {

    private final SettingRepository settingRepository;
    private final SettingMerger settingMerger;

    /**
     * GetMergedSettingsUseCase 생성자.
     *
     * <p>생성자 주입을 통한 의존성 주입입니다.</p>
     *
     * @param settingRepository Setting Repository
     * @param settingMerger Setting Merger
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public GetMergedSettingsUseCase(
        SettingRepository settingRepository,
        SettingMerger settingMerger
    ) {
        this.settingRepository = settingRepository;
        this.settingMerger = settingMerger;
    }

    /**
     * 병합된 설정을 조회합니다.
     *
     * <p>병합 전략:</p>
     * <ol>
     *   <li>DEFAULT 레벨 설정 조회</li>
     *   <li>TENANT 레벨 설정 조회 (tenantId가 있는 경우)</li>
     *   <li>ORG 레벨 설정 조회 (orgId가 있는 경우)</li>
     *   <li>SettingMerger를 통한 3단계 병합</li>
     * </ol>
     *
     * <p>읽기 전용 트랜잭션으로 실행됩니다.</p>
     *
     * @param query 병합 조회 Query
     * @return 병합된 설정 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public MergedSettingsResponse execute(GetMergedSettingsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query는 필수입니다");
        }

        // 3레벨 설정 조회
        SettingRepository.SettingsForMerge settingsForMerge = settingRepository.findAllForMerge(
            query.getOrgId(),
            query.getTenantId()
        );

        // Domain Service를 통한 병합
        Map<String, String> mergedSettings = settingMerger.mergeToValueMap(
            settingsForMerge.getOrgSettings(),
            settingsForMerge.getTenantSettings(),
            settingsForMerge.getDefaultSettings()
        );

        return new MergedSettingsResponse(mergedSettings);
    }
}
