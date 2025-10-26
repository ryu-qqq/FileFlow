package com.ryuqq.fileflow.application.settings.service.query;

import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.domain.settings.SettingMerger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Get Merged Settings Service - Query Handler
 *
 * <p>3단계 우선순위 병합(ORG > TENANT > DEFAULT)을 수행하는 Query Handler 구현체입니다.</p>
 * <p>조직, 테넌트, 기본 레벨의 설정을 병합하여 최종 설정 맵을 반환합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ CQRS 패턴 - Query Handler (읽기 작업)</li>
 *   <li>✅ Hexagonal Architecture - LoadSettingsPort 사용</li>
 *   <li>✅ Transaction 경계 - 읽기 전용 트랜잭션</li>
 *   <li>✅ Domain Service 활용 - SettingMerger 위임</li>
 *   <li>✅ 부작용 없음 - 순수 조회만 수행</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Service
@Transactional(readOnly = true)
public class GetMergedSettingsService implements GetMergedSettingsUseCase {

    private final LoadSettingsPort loadSettingsPort;
    private final SettingMerger settingMerger;

    /**
     * GetMergedSettingsService 생성자.
     *
     * <p>생성자 주입을 통한 의존성 주입입니다.</p>
     *
     * @param loadSettingsPort Load Settings Port (Query 전용 Outbound Port)
     * @param settingMerger Setting Merger (Domain Service)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public GetMergedSettingsService(
        LoadSettingsPort loadSettingsPort,
        SettingMerger settingMerger
    ) {
        this.loadSettingsPort = loadSettingsPort;
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
     *   <li>SettingMerger를 통한 3단계 병합 (ORG > TENANT > DEFAULT 우선순위)</li>
     * </ol>
     *
     * <p>읽기 전용 트랜잭션으로 실행됩니다.</p>
     *
     * @param query 병합 조회 Query
     * @return 병합된 설정 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public Response execute(Query query) {
        // 1. 3레벨 설정 조회 (Query Port 사용)
        LoadSettingsPort.SettingsForMerge settingsForMerge = loadSettingsPort.findAllForMerge(
            query.orgId(),
            query.tenantId()
        );

        // 2. Domain Service를 통한 병합 (ORG > TENANT > DEFAULT 우선순위)
        Map<String, String> mergedSettings = settingMerger.mergeToValueMap(
            settingsForMerge.orgSettings(),
            settingsForMerge.tenantSettings(),
            settingsForMerge.defaultSettings()
        );

        // 3. Response 반환
        return new Response(mergedSettings);
    }
}
