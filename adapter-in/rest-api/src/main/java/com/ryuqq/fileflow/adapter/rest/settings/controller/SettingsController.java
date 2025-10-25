package com.ryuqq.fileflow.adapter.rest.settings.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.MergedSettingsApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.UpdateSettingRequest;
import com.ryuqq.fileflow.adapter.rest.settings.dto.UpdateSettingResponse;
import com.ryuqq.fileflow.adapter.rest.settings.mapper.SettingsDtoMapper;
import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SettingsController - Settings REST API Controller
 *
 * <p>EAV 설정 시스템의 REST API 진입점입니다.
 * Hexagonal Architecture의 Driving Adapter (Inbound Adapter)에 해당합니다.</p>
 *
 * <p><strong>제공 API:</strong></p>
 * <ul>
 *   <li>GET /api/v1/settings - 3레벨 병합된 설정 조회 (200 OK)</li>
 *   <li>PATCH /api/v1/settings - 특정 설정 수정 (200 OK)</li>
 * </ul>
 *
 * <p><strong>3레벨 병합 우선순위:</strong></p>
 * <ul>
 *   <li>1순위: ORG (조직 레벨) - 조직 특화 설정</li>
 *   <li>2순위: TENANT (테넌트 레벨) - 테넌트 특화 설정</li>
 *   <li>3순위: DEFAULT (기본 레벨) - 전역 기본 설정</li>
 * </ul>
 *
 * <p><strong>REST API Controller 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Thin Controller - 비즈니스 로직 없음</li>
 *   <li>✅ Use Case 호출만 담당</li>
 *   <li>✅ {@code @Valid} 검증 적용</li>
 *   <li>✅ 적절한 HTTP 상태 코드 반환</li>
 *   <li>✅ DTO Mapper를 통한 변환</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Long FK 전략 적용</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>400 Bad Request: Validation 실패, 잘못된 SettingLevel</li>
 *   <li>404 Not Found: Setting이 존재하지 않음</li>
 *   <li>500 Internal Server Error: 서버 내부 오류</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final GetMergedSettingsUseCase getMergedSettingsUseCase;
    private final UpdateSettingUseCase updateSettingUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param getMergedSettingsUseCase 3레벨 병합 설정 조회 UseCase
     * @param updateSettingUseCase 설정 수정 UseCase
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingsController(
        GetMergedSettingsUseCase getMergedSettingsUseCase,
        UpdateSettingUseCase updateSettingUseCase
    ) {
        this.getMergedSettingsUseCase = getMergedSettingsUseCase;
        this.updateSettingUseCase = updateSettingUseCase;
    }

    /**
     * GET /api/v1/settings - 3레벨 병합된 설정 조회
     *
     * <p>조직 ID와 테넌트 ID를 받아 3레벨 우선순위 병합(ORG > TENANT > DEFAULT)을 수행합니다.</p>
     *
     * <p><strong>병합 전략:</strong></p>
     * <ul>
     *   <li>orgId와 tenantId가 모두 제공: ORG → TENANT → DEFAULT 병합</li>
     *   <li>orgId만 제공: ORG → DEFAULT 병합 (TENANT 생략)</li>
     *   <li>tenantId만 제공: TENANT → DEFAULT 병합 (ORG 생략)</li>
     *   <li>모두 null: DEFAULT만 반환</li>
     * </ul>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 설정 조회 성공 (병합 완료)</li>
     *   <li>400 Bad Request: Validation 실패</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * GET /api/v1/settings?orgId=1&tenantId=100
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "settings": {
     *       "MAX_UPLOAD_SIZE": "100MB",
     *       "API_TIMEOUT": "30",
     *       "ENABLE_CACHE": "true",
     *       "API_KEY": "****"
     *     }
     *   },
     *   "error": null,
     *   "timestamp": "2025-10-25T14:30:00"
     * }
     * }</pre>
     *
     * <p><strong>비밀 키 마스킹:</strong></p>
     * <ul>
     *   <li>{@code is_secret=1} 설정은 값이 "****"로 마스킹되어 반환됩니다</li>
     *   <li>마스킹은 Application Layer에서 수행됩니다</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (Long FK, nullable)
     * @return 200 OK + ApiResponse<MergedSettingsApiResponse>
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MergedSettingsApiResponse>> getMergedSettings(
        @RequestParam(required = false) Long orgId,
        @RequestParam(required = false) Long tenantId
    ) {
        GetMergedSettingsUseCase.Query query = new GetMergedSettingsUseCase.Query(orgId, tenantId);
        GetMergedSettingsUseCase.Response response = getMergedSettingsUseCase.execute(query);
        MergedSettingsApiResponse apiResponse = SettingsDtoMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * PATCH /api/v1/settings - 특정 설정 수정
     *
     * <p>특정 Setting의 값을 수정합니다. Key, Level, ContextId로 Setting을 식별합니다.</p>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 설정 수정 성공</li>
     *   <li>400 Bad Request: Validation 실패, 잘못된 SettingLevel</li>
     *   <li>404 Not Found: Setting이 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example - ORG Level:</strong></p>
     * <pre>{@code
     * PATCH /api/v1/settings
     * {
     *   "key": "MAX_UPLOAD_SIZE",
     *   "value": "200MB",
     *   "level": "ORG",
     *   "contextId": 1
     * }
     * }</pre>
     *
     * <p><strong>Request Example - DEFAULT Level:</strong></p>
     * <pre>{@code
     * PATCH /api/v1/settings
     * {
     *   "key": "API_TIMEOUT",
     *   "value": "60",
     *   "level": "DEFAULT",
     *   "contextId": null
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "id": 1,
     *     "key": "MAX_UPLOAD_SIZE",
     *     "value": "200MB",
     *     "valueType": "STRING",
     *     "level": "ORG",
     *     "contextId": 1,
     *     "secret": false,
     *     "createdAt": "2025-10-25T14:30:00",
     *     "updatedAt": "2025-10-25T14:35:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-10-25T14:35:00"
     * }
     * }</pre>
     *
     * <p><strong>Validation 규칙:</strong></p>
     * <ul>
     *   <li>key: 필수, 빈 문자열 불가</li>
     *   <li>value: 필수, 빈 문자열 불가</li>
     *   <li>level: 필수, "ORG", "TENANT", "DEFAULT" 중 하나</li>
     *   <li>contextId: ORG/TENANT 레벨은 필수, DEFAULT는 null</li>
     * </ul>
     *
     * @param request 설정 수정 요청 DTO
     * @return 200 OK + ApiResponse<SettingResponse> (수정된 설정 반환)
     * @throws IllegalArgumentException level이 유효하지 않은 경우 (400)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<UpdateSettingResponse>> updateSetting(
        @Valid @RequestBody UpdateSettingRequest request
    ) {
        UpdateSettingUseCase.Command command = SettingsDtoMapper.toCommand(request);
        UpdateSettingUseCase.Response response = updateSettingUseCase.execute(command);
        UpdateSettingResponse apiResponse = SettingsDtoMapper.toUpdateResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
