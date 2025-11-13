package com.ryuqq.fileflow.adapter.rest.iam.tenant.controller;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.response.TenantApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.TenantSearchApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantStatusApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.mapper.TenantApiMapper;
import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.rest.common.dto.PageApiResponse;
import com.ryuqq.fileflow.adapter.rest.common.dto.SliceApiResponse;
import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantCommandFacade;
import com.ryuqq.fileflow.application.iam.tenant.facade.TenantQueryFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TenantController - Tenant REST API Controller
 *
 * <p>Tenant 관리를 위한 REST API 진입점입니다.
 * Hexagonal Architecture의 Driving Adapter (Inbound Adapter)에 해당합니다.</p>
 *
 * <p><strong>제공 API:</strong></p>
 * <ul>
 *   <li>POST /api/v1/tenants - Tenant 생성 (201 Created)</li>
 *   <li>PATCH /api/v1/tenants/{tenantId} - Tenant 수정 (200 OK)</li>
 *   <li>DELETE /api/v1/tenants/{tenantId} - Tenant 삭제 (204 No Content)</li>
 *   <li>GET /api/v1/tenants - Tenant 목록 조회 (200 OK)</li>
 *   <li>GET /api/v1/tenants/{tenantId} - Tenant 상세 조회 (200 OK)</li>
 *   <li>GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회 (200 OK)</li>
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
 * </ul>
 *
 * <p><strong>개선 사항 (Phase 6-2):</strong></p>
 * <ul>
 *   <li>✅ Facade Pattern 적용 - 5개 UseCase → 2개 Facade (60% 의존성 감소)</li>
 *   <li>✅ Command/Query 분리 - TenantCommandFacade, TenantQueryFacade</li>
 *   <li>✅ Controller 단순화 - 단일 책임 원칙 준수</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>400 Bad Request: Validation 실패</li>
 *   <li>404 Not Found: Tenant가 존재하지 않음</li>
 *   <li>409 Conflict: 중복된 Tenant 이름</li>
 *   <li>500 Internal Server Error: 서버 내부 오류</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.iam.tenant.base}")
public class TenantController {
    //Todo facade 리팩토링
    private final TenantCommandFacade tenantCommandFacade;
    private final TenantQueryFacade tenantQueryFacade;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     * <p>Phase 6-2: Facade Pattern 적용으로 5개 의존성 → 2개 의존성 (60% 감소)</p>
     *
     * @param tenantCommandFacade Tenant Command Facade (Create, Update, UpdateStatus)
     * @param tenantQueryFacade Tenant Query Facade (GetTenant, GetTenants)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantController(
        TenantCommandFacade tenantCommandFacade,
        TenantQueryFacade tenantQueryFacade
    ) {
        this.tenantCommandFacade = tenantCommandFacade;
        this.tenantQueryFacade = tenantQueryFacade;
    }

    /**
     * POST /api/v1/tenants - Tenant 생성
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>201 Created: Tenant 생성 성공</li>
     *   <li>400 Bad Request: Validation 실패</li>
     *   <li>409 Conflict: 중복된 Tenant 이름</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * POST /api/v1/tenants
     * {
     *   "name": "my-tenant"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 201 Created
     * {
     *   "tenantId": "tenant-id-123",
     *   "name": "my-tenant",
     *   "status": "ACTIVE",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-22T10:30:00"
     * }
     * }</pre>
     *
     * @param request Tenant 생성 요청 DTO
     * @return 201 Created + TenantApiResponse
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TenantApiResponse>> createTenant(
        @Valid @RequestBody CreateTenantApiRequest request
    ) {
        CreateTenantCommand command = TenantApiMapper.toCommand(request);
        TenantResponse response = tenantCommandFacade.createTenant(command);
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * PATCH /api/v1/tenants/{tenantId} - Tenant 수정
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Tenant 수정 성공</li>
     *   <li>400 Bad Request: Validation 실패</li>
     *   <li>404 Not Found: Tenant가 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example (Option B 변경):</strong></p>
     * <pre>{@code
     * PATCH /api/v1/tenants/123
     * {
     *   "name": "updated-tenant-name"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "tenantId": 123,
     *   "name": "updated-tenant-name",
     *   "status": "ACTIVE",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-22T11:00:00"
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Path Variable - Long)
     * @param request Tenant 수정 요청 DTO
     * @return 200 OK + TenantApiResponse
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @PatchMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<TenantApiResponse>> updateTenant(
        @PathVariable Long tenantId,
        @Valid @RequestBody UpdateTenantApiRequest request
    ) {
        UpdateTenantCommand command = TenantApiMapper.toCommand(tenantId, request);
        TenantResponse response = tenantCommandFacade.updateTenant(command);
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * PATCH /api/v1/tenants/{tenantId}/status - Tenant 상태 변경
     *
     * <p><strong>상태 전환 규칙:</strong></p>
     * <ul>
     *   <li>ACTIVE → SUSPENDED: Tenant 일시 정지</li>
     *   <li>SUSPENDED → ACTIVE: Tenant 재활성화</li>
     * </ul>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Tenant 상태 변경 성공</li>
     *   <li>400 Bad Request: Validation 실패 또는 잘못된 상태값</li>
     *   <li>404 Not Found: Tenant가 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example (Option B 변경):</strong></p>
     * <pre>{@code
     * PATCH /api/v1/tenants/123/status
     * {
     *   "status": "SUSPENDED"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "tenantId": 123,
     *   "name": "my-tenant",
     *   "status": "SUSPENDED",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-23T15:00:00"
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Path Variable - Long)
     * @param request Tenant 상태 변경 요청 DTO
     * @return 200 OK + TenantApiResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @PatchMapping("/{tenantId}/status")
    public ResponseEntity<ApiResponse<TenantApiResponse>> updateTenantStatus(
        @PathVariable Long tenantId,
        @Valid @RequestBody UpdateTenantStatusApiRequest request
    ) {
        UpdateTenantStatusCommand command = TenantApiMapper.toCommand(tenantId, request);
        TenantResponse response = tenantCommandFacade.updateTenantStatus(command);
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * GET /api/v1/tenants - Tenant 목록 조회 (Offset-based 또는 Cursor-based)
     *
     * <p><strong>Pagination 전략:</strong></p>
     * <ul>
     *   <li>Offset-based: page 파라미터 사용 → PageResponse 반환</li>
     *   <li>Cursor-based: cursor 파라미터 사용 → SliceResponse 반환</li>
     * </ul>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 조회 성공</li>
     *   <li>400 Bad Request: Validation 실패 (page < 0, size < 1 또는 size > 100)</li>
     * </ul>
     *
     * <p><strong>Request Example - Offset-based:</strong></p>
     * <pre>{@code
     * GET /api/v1/tenants?page=0&size=20&nameContains=test&deleted=false
     * }</pre>
     *
     * <p><strong>Request Example - Cursor-based:</strong></p>
     * <pre>{@code
     * GET /api/v1/tenants?cursor=encoded-cursor&size=20&nameContains=test
     * }</pre>
     *
     * <p><strong>개선 사항 (Phase 6-1):</strong></p>
     * <ul>
     *   <li>✅ Query Parameter 객체화 ({@link TenantSearchApiRequest} Record)</li>
     *   <li>✅ 메서드 파라미터 5개 → 1개로 단순화</li>
     *   <li>✅ Validation 자동 적용 (@Valid + Bean Validation)</li>
     * </ul>
     *
     * @param param Tenant 목록 조회 Query Parameter (Record, @ModelAttribute 바인딩)
     * @return 200 OK + ApiResponse<PageResponse> 또는 ApiResponse<SliceResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @GetMapping
    public ResponseEntity<?> getTenants(
        @Valid @ModelAttribute TenantSearchApiRequest param
    ) {
        GetTenantsQuery query = param.toQuery();

        if (param.isOffsetBased()) {
            PageResponse<TenantResponse> pageResponse = tenantQueryFacade.getTenantsWithPage(query);
            PageApiResponse<TenantApiResponse> pageApiResponse = PageApiResponse.from(pageResponse, TenantApiMapper::toApiResponse);
            return ResponseEntity.ok(ApiResponse.ofSuccess(pageApiResponse));
        } else {
            SliceResponse<TenantResponse> sliceResponse = tenantQueryFacade.getTenantsWithSlice(query);
            SliceApiResponse<TenantApiResponse> sliceApiResponse = SliceApiResponse.from(sliceResponse, TenantApiMapper::toApiResponse);
            return ResponseEntity.ok(ApiResponse.ofSuccess(sliceApiResponse));
        }
    }

    /**
     * GET /api/v1/tenants/{tenantId} - Tenant 상세 조회
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 조회 성공</li>
     *   <li>404 Not Found: Tenant가 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example (Option B 변경):</strong></p>
     * <pre>{@code
     * GET /api/v1/tenants/123
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "tenantId": 123,
     *     "name": "my-tenant",
     *     "status": "ACTIVE",
     *     "deleted": false,
     *     "createdAt": "2025-10-22T10:30:00",
     *     "updatedAt": "2025-10-22T10:30:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-10-23T14:30:00",
     *   "requestId": "req-uuid-123"
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Path Variable - Long)
     * @return 200 OK + ApiResponse<TenantApiResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<ApiResponse<TenantApiResponse>> getTenant(
        @PathVariable Long tenantId
    ) {
        GetTenantQuery query = GetTenantQuery.of(tenantId);
        TenantResponse response = tenantQueryFacade.getTenant(query);
        TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * GET /api/v1/tenants/{tenantId}/tree - Tenant 트리 조회 (Tenant + Organizations)
     *
     * <p>Tenant와 하위 Organization 목록을 트리 구조로 조회합니다.</p>
     * <p>Organization 개수와 목록을 포함하여 반환합니다.</p>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 조회 성공</li>
     *   <li>404 Not Found: Tenant가 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example (Option B 변경):</strong></p>
     * <pre>{@code
     * GET /api/v1/tenants/123/tree
     * GET /api/v1/tenants/123/tree?includeDeleted=true
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "tenantId": 123,
     *     "name": "my-tenant",
     *     "status": "ACTIVE",
     *     "deleted": false,
     *     "organizationCount": 2,
     *     "organizations": [
     *       {
     *         "organizationId": 1,
     *         "orgCode": "ORG-001",
     *         "name": "Sales Team",
     *         "status": "ACTIVE",
     *         "deleted": false
     *       },
     *       {
     *         "organizationId": 2,
     *         "orgCode": "ORG-002",
     *         "name": "Marketing Team",
     *         "status": "ACTIVE",
     *         "deleted": false
     *       }
     *     ],
     *     "createdAt": "2025-10-22T10:30:00",
     *     "updatedAt": "2025-10-22T10:30:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-10-23T14:30:00",
     *   "requestId": "req-uuid-123"
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Path Variable - Long)
     * @return 200 OK + ApiResponse<TenantTreeResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @GetMapping("/{tenantId}/tree")
    public ResponseEntity<ApiResponse<TenantTreeResponse>> getTenantTree(
        @PathVariable Long tenantId
    ) {
        GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
        TenantTreeResponse response = tenantQueryFacade.getTenantTree(query);
        return ResponseEntity.ok(ApiResponse.ofSuccess(response));
    }

}
