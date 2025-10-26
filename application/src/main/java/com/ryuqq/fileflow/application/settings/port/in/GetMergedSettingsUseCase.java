package com.ryuqq.fileflow.application.settings.port.in;

import java.util.Map;

/**
 * Get Merged Settings UseCase
 *
 * <p>3단계 우선순위 병합(ORG > TENANT > DEFAULT)을 수행하는 Query UseCase입니다.</p>
 *
 * <p><strong>CQRS 패턴 준수:</strong></p>
 * <ul>
 *   <li>✅ Query/Response - 내부 Record로 정의</li>
 *   <li>✅ 조회만 수행 (읽기 작업)</li>
 *   <li>✅ 부작용 없음</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface GetMergedSettingsUseCase {

    /**
     * 병합된 설정을 조회합니다.
     *
     * @param query 병합 조회 Query
     * @return 병합된 설정 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Response execute(Query query);

    /**
     * Get Merged Settings Query - 내부 Record
     *
     * <p>설정 병합 조회 조건을 담는 Query 객체입니다.</p>
     *
     * <p><strong>병합 전략:</strong></p>
     * <ul>
     *   <li>orgId와 tenantId가 모두 제공: ORG → TENANT → DEFAULT 병합</li>
     *   <li>orgId만 제공: ORG → DEFAULT 병합 (TENANT 생략)</li>
     *   <li>tenantId만 제공: TENANT → DEFAULT 병합 (ORG 생략)</li>
     *   <li>모두 null: DEFAULT만 반환</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    record Query(
        Long orgId,
        Long tenantId
    ) {
        /**
         * Compact Constructor - 검증 로직 없음 (모두 nullable)
         */
        public Query {
            // orgId와 tenantId는 모두 nullable (유효성 검증 불필요)
        }
    }

    /**
     * Merged Settings Response - 내부 Record
     *
     * <p>병합된 설정 정보를 담는 Response 객체입니다.</p>
     *
     * <p><strong>settings Map 구조:</strong></p>
     * <ul>
     *   <li>Key: 설정 키 (예: "MAX_UPLOAD_SIZE")</li>
     *   <li>Value: 설정 값 (예: "100MB")</li>
     *   <li>비밀 설정은 "****"로 마스킹되어 반환됨</li>
     * </ul>
     *
     * @param settings 병합된 설정 맵 (key → value)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    record Response(
        Map<String, String> settings
    ) {
        /**
         * Compact Constructor - 불변 Map으로 변환
         */
        public Response {
            if (settings == null) {
                throw new IllegalArgumentException("Settings map은 필수입니다");
            }
            settings = Map.copyOf(settings);
        }
    }
}
