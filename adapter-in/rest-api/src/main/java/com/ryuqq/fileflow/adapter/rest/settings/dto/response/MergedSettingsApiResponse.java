package com.ryuqq.fileflow.adapter.rest.settings.dto;

import java.util.Map;

/**
 * MergedSettingsApiResponse - 병합된 설정 API 응답 DTO
 *
 * <p>REST API를 통해 3레벨 병합된 설정 정보를 클라이언트에게 전달하기 위한 불변 Response 객체입니다.</p>
 * <p>Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>REST API Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ ApiResponse 접미사 사용</li>
 *   <li>✅ Application DTO와 분리 (Mapper 변환 필수)</li>
 *   <li>✅ JSON 직렬화 친화적 구조</li>
 * </ul>
 *
 * <p><strong>병합 우선순위:</strong></p>
 * <ul>
 *   <li>1순위: ORG (조직 레벨)</li>
 *   <li>2순위: TENANT (테넌트 레벨)</li>
 *   <li>3순위: DEFAULT (기본 레벨)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * {
 *   "settings": {
 *     "MAX_UPLOAD_SIZE": "100MB",
 *     "API_TIMEOUT": "30",
 *     "ENABLE_CACHE": "true",
 *     "API_KEY": "****"
 *   }
 * }
 * }</pre>
 *
 * @param settings 병합된 설정 Map (key: 설정 키, value: 설정 값 - 비밀 키는 마스킹됨)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record MergedSettingsApiResponse(
    Map<String, String> settings
) {
}
