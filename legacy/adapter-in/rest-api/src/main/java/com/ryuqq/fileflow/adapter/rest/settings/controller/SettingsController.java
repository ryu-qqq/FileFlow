package com.ryuqq.fileflow.adapter.rest.settings.controller;

import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.request.CreateSettingApiRequest;
import com.ryuqq.fileflow.adapter.rest.settings.dto.response.CreateSettingApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.response.MergedSettingsApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.request.UpdateSettingApiRequest;
import com.ryuqq.fileflow.adapter.rest.settings.dto.response.UpdateSettingApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.mapper.SettingsApiMapper;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase;
import com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
 *   <li>POST /api/v1/settings - 설정 생성 (201 Created)</li>
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
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.settings.base}")
public class SettingsController {

    private final CreateSettingUseCase createSettingUseCase;
    private final GetMergedSettingsUseCase getMergedSettingsUseCase;
    private final UpdateSettingUseCase updateSettingUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param createSettingUseCase 설정 생성 UseCase
     * @param getMergedSettingsUseCase 3레벨 병합 설정 조회 UseCase
     * @param updateSettingUseCase 설정 수정 UseCase
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public SettingsController(
        CreateSettingUseCase createSettingUseCase,
        GetMergedSettingsUseCase getMergedSettingsUseCase,
        UpdateSettingUseCase updateSettingUseCase
    ) {
        this.createSettingUseCase = createSettingUseCase;
        this.getMergedSettingsUseCase = getMergedSettingsUseCase;
        this.updateSettingUseCase = updateSettingUseCase;
    }

    /**
     * POST /api/v1/settings - 설정 생성
     *
     * <p>새로운 Setting을 생성합니다.</p>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>201 Created: 설정 생성 성공</li>
     *   <li>400 Bad Request: Validation 실패, 잘못된 SettingLevel 또는 ValueType</li>
     *   <li>409 Conflict: 동일한 (key, level, contextId) 조합이 이미 존재</li>
     * </ul>
     *
     * <p><strong>Request Example - ORG Level:</strong></p>
     * <pre>{@code
     * POST /api/v1/settings
     * {
     *   "key": "MAX_UPLOAD_SIZE",
     *   "value": "100MB",
     *   "level": "ORG",
     *   "contextId": 1,
     *   "valueType": "STRING",
     *   "secret": false
     * }
     * }</pre>
     *
     * <p><strong>Request Example - DEFAULT Level with Secret:</strong></p>
     * <pre>{@code
     * POST /api/v1/settings
     * {
     *   "key": "API_KEY",
     *   "value": "sk_live_abcdefg123456",
     *   "level": "DEFAULT",
     *   "contextId": null,
     *   "valueType": "STRING",
     *   "secret": true
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 201 Created
     * {
     *   "success": true,
     *   "data": {
     *     "id": 1,
     *     "key": "MAX_UPLOAD_SIZE",
     *     "value": "100MB",
     *     "valueType": "STRING",
     *     "level": "ORG",
     *     "contextId": 1,
     *     "secret": false,
     *     "createdAt": "2025-10-26T10:30:00",
     *     "updatedAt": "2025-10-26T10:30:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-10-26T10:30:00"
     * }
     * }</pre>
     *
     * <p><strong>Validation 규칙:</strong></p>
     * <ul>
     *   <li>key: 필수, 빈 문자열 불가</li>
     *   <li>value: 필수, 빈 문자열 불가</li>
     *   <li>level: 필수, "ORG", "TENANT", "DEFAULT" 중 하나</li>
     *   <li>contextId: ORG/TENANT 레벨은 필수, DEFAULT는 null</li>
     *   <li>valueType: 선택, null이면 기본값 "STRING"</li>
     *   <li>secret: 선택, null이면 기본값 false</li>
     * </ul>
     *
     * @param request 설정 생성 요청 DTO
     * @return 201 Created + ApiResponse<CreateSettingResponse> (생성된 설정 반환)
     * @throws IllegalArgumentException level 또는 valueType이 유효하지 않은 경우 (400)
     * @throws IllegalStateException 동일한 (key, level, contextId) 조합이 이미 존재하는 경우 (409)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSettingApiResponse>> createSetting(
        @Valid @RequestBody CreateSettingApiRequest request
    ) {
        CreateSettingUseCase.Command command = SettingsApiMapper.toCommand(request);
        CreateSettingUseCase.Response response = createSettingUseCase.execute(command);
        CreateSettingApiResponse apiResponse = SettingsApiMapper.toCreateResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ofSuccess(apiResponse));
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
        MergedSettingsApiResponse apiResponse = SettingsApiMapper.toApiResponse(response);
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
    public ResponseEntity<ApiResponse<UpdateSettingApiResponse>> updateSetting(
        @Valid @RequestBody UpdateSettingApiRequest request
    ) {
        UpdateSettingUseCase.Command command = SettingsApiMapper.toCommand(request);
        UpdateSettingUseCase.Response response = updateSettingUseCase.execute(command);
        UpdateSettingApiResponse apiResponse = SettingsApiMapper.toUpdateResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
