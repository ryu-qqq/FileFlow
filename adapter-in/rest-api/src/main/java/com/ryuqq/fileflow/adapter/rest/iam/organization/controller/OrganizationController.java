package com.ryuqq.fileflow.adapter.rest.iam.organization.controller;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.OrganizationApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.OrganizationListQueryParam;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationStatusRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.mapper.OrganizationDtoMapper;
import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationCommandFacade;
import com.ryuqq.fileflow.application.iam.organization.facade.OrganizationQueryFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OrganizationController - Organization REST API Controller
 *
 * <p>Organization 관리를 위한 REST API 진입점입니다.
 * Hexagonal Architecture의 Driving Adapter (Inbound Adapter)에 해당합니다.</p>
 *
 * <p><strong>제공 API:</strong></p>
 * <ul>
 *   <li>POST /api/v1/organizations - Organization 생성 (201 Created)</li>
 *   <li>PATCH /api/v1/organizations/{organizationId} - Organization 수정 (200 OK)</li>
 *   <li>DELETE /api/v1/organizations/{organizationId} - Organization 삭제 (204 No Content)</li>
 *   <li>GET /api/v1/organizations - Organization 목록 조회 (200 OK) - TODO: 미구현</li>
 *   <li>GET /api/v1/organizations/{organizationId} - Organization 상세 조회 (200 OK) - TODO: 미구현</li>
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
 * <p><strong>개선 사항 (Phase 6-2):</strong></p>
 * <ul>
 *   <li>✅ Facade Pattern 적용 - 6개 UseCase → 2개 Facade (67% 의존성 감소)</li>
 *   <li>✅ Command/Query 분리 - OrganizationCommandFacade, OrganizationQueryFacade</li>
 *   <li>✅ Controller 단순화 - 단일 책임 원칙 준수</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>400 Bad Request: Validation 실패</li>
 *   <li>404 Not Found: Organization이 존재하지 않음</li>
 *   <li>409 Conflict: 중복된 조직 코드</li>
 *   <li>500 Internal Server Error: 서버 내부 오류</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationCommandFacade organizationCommandFacade;
    private final OrganizationQueryFacade organizationQueryFacade;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     * <p>Phase 6-2: Facade Pattern 적용으로 6개 의존성 → 2개 의존성 (67% 감소)</p>
     *
     * @param organizationCommandFacade Organization Command Facade (Create, Update, UpdateStatus, Delete)
     * @param organizationQueryFacade Organization Query Facade (GetOrganization, GetOrganizations)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationController(
        OrganizationCommandFacade organizationCommandFacade,
        OrganizationQueryFacade organizationQueryFacade
    ) {
        this.organizationCommandFacade = organizationCommandFacade;
        this.organizationQueryFacade = organizationQueryFacade;
    }

    /**
     * POST /api/v1/organizations - Organization 생성
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>201 Created: Organization 생성 성공</li>
     *   <li>400 Bad Request: Validation 실패</li>
     *   <li>409 Conflict: 중복된 조직 코드</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * POST /api/v1/organizations
     * {
     *   "tenantId": 1,
     *   "orgCode": "ORG001",
     *   "name": "Engineering Department"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 201 Created
     * {
     *   "organizationId": 1,
     *   "tenantId": 1,
     *   "orgCode": "ORG001",
     *   "name": "Engineering Department",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-22T10:30:00"
     * }
     * }</pre>
     *
     * @param request Organization 생성 요청 DTO
     * @return 201 Created + OrganizationApiResponse
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationApiResponse>> createOrganization(
        @Valid @RequestBody CreateOrganizationRequest request
    ) {
        CreateOrganizationCommand command = OrganizationDtoMapper.toCommand(request);
        OrganizationResponse response = organizationCommandFacade.createOrganization(command);
        OrganizationApiResponse apiResponse = OrganizationDtoMapper.toApiResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * PATCH /api/v1/organizations/{organizationId} - Organization 수정
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Organization 수정 성공</li>
     *   <li>400 Bad Request: Validation 실패</li>
     *   <li>404 Not Found: Organization이 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * PATCH /api/v1/organizations/1
     * {
     *   "name": "Updated Department Name"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "organizationId": 1,
     *   "tenantId": 1,
     *   "orgCode": "ORG001",
     *   "name": "Updated Department Name",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-22T11:00:00"
     * }
     * }</pre>
     *
     * @param organizationId Organization ID (Path Variable)
     * @param request Organization 수정 요청 DTO
     * @return 200 OK + OrganizationApiResponse
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @PatchMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationApiResponse>> updateOrganization(
        @PathVariable Long organizationId,
        @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        UpdateOrganizationCommand command = OrganizationDtoMapper.toCommand(organizationId, request);
        OrganizationResponse response = organizationCommandFacade.updateOrganization(command);
        OrganizationApiResponse apiResponse = OrganizationDtoMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * PATCH /api/v1/organizations/{organizationId}/status - Organization 상태 변경
     *
     * <p><strong>상태 전환 규칙:</strong></p>
     * <ul>
     *   <li>ACTIVE → INACTIVE: Organization 비활성화 (Soft Delete, 단방향)</li>
     *   <li>INACTIVE → ACTIVE: 허용되지 않음 (복원 불가)</li>
     * </ul>
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: Organization 상태 변경 성공</li>
     *   <li>400 Bad Request: Validation 실패, 잘못된 상태값, 또는 복원 시도</li>
     *   <li>404 Not Found: Organization이 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * PATCH /api/v1/organizations/1/status
     * {
     *   "status": "INACTIVE"
     * }
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "organizationId": 1,
     *   "tenantId": 1,
     *   "orgCode": "ORG001",
     *   "name": "Engineering Department",
     *   "deleted": false,
     *   "createdAt": "2025-10-22T10:30:00",
     *   "updatedAt": "2025-10-23T15:00:00"
     * }
     * }</pre>
     *
     * @param organizationId Organization ID (Path Variable)
     * @param request Organization 상태 변경 요청 DTO
     * @return 200 OK + OrganizationApiResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @PatchMapping("/{organizationId}/status")
    public ResponseEntity<ApiResponse<OrganizationApiResponse>> updateOrganizationStatus(
        @PathVariable Long organizationId,
        @Valid @RequestBody UpdateOrganizationStatusRequest request
    ) {
        UpdateOrganizationStatusCommand command = OrganizationDtoMapper.toCommand(organizationId, request);
        OrganizationResponse response = organizationCommandFacade.updateOrganizationStatus(command);
        OrganizationApiResponse apiResponse = OrganizationDtoMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * DELETE /api/v1/organizations/{organizationId} - Organization 삭제 (Soft Delete)
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>204 No Content: Organization 삭제 성공</li>
     *   <li>404 Not Found: Organization이 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * DELETE /api/v1/organizations/1
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 204 No Content
     * }</pre>
     *
     * @param organizationId Organization ID (Path Variable)
     * @return 204 No Content
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganization(
        @PathVariable Long organizationId
    ) {
        SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(organizationId);
        organizationCommandFacade.deleteOrganization(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/organizations - Organization 목록 조회 (Offset-based 또는 Cursor-based)
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
     *   <li>400 Bad Request: Validation 실패 (page < 0, size < 1 또는 size > 100, tenantId < 1)</li>
     * </ul>
     *
     * <p><strong>Request Example - Offset-based:</strong></p>
     * <pre>{@code
     * GET /api/v1/organizations?page=0&size=20&tenantId=1&nameContains=dept&deleted=false
     * }</pre>
     *
     * <p><strong>Request Example - Cursor-based:</strong></p>
     * <pre>{@code
     * GET /api/v1/organizations?cursor=encoded-cursor&size=20&tenantId=1&orgCodeContains=ORG
     * }</pre>
     *
     * <p><strong>개선 사항 (Phase 6-1):</strong></p>
     * <ul>
     *   <li>✅ Query Parameter 객체화 ({@link OrganizationListQueryParam} Record)</li>
     *   <li>✅ 메서드 파라미터 7개 → 1개로 단순화</li>
     *   <li>✅ Validation 자동 적용 (@Valid + Bean Validation)</li>
     * </ul>
     *
     * @param param Organization 목록 조회 Query Parameter (Record, @ModelAttribute 바인딩)
     * @return 200 OK + ApiResponse<PageResponse> 또는 ApiResponse<SliceResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @GetMapping
    public ResponseEntity<?> getOrganizations(
        @Valid @ModelAttribute OrganizationListQueryParam param
    ) {
        GetOrganizationsQuery query = param.toQuery();

        if (param.isOffsetBased()) {
            PageResponse<OrganizationResponse> pageResponse = organizationQueryFacade.getOrganizationsWithPage(query);
            return ResponseEntity.ok(ApiResponse.ofSuccess(pageResponse));
        } else {
            SliceResponse<OrganizationResponse> sliceResponse = organizationQueryFacade.getOrganizationsWithSlice(query);
            return ResponseEntity.ok(ApiResponse.ofSuccess(sliceResponse));
        }
    }

    /**
     * GET /api/v1/organizations/{organizationId} - Organization 상세 조회
     *
     * <p><strong>HTTP Status Codes:</strong></p>
     * <ul>
     *   <li>200 OK: 조회 성공</li>
     *   <li>404 Not Found: Organization이 존재하지 않음</li>
     * </ul>
     *
     * <p><strong>Request Example:</strong></p>
     * <pre>{@code
     * GET /api/v1/organizations/1
     * }</pre>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "organizationId": 1,
     *     "tenantId": 1,
     *     "orgCode": "ORG001",
     *     "name": "Engineering Department",
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
     * @param organizationId Organization ID (Path Variable)
     * @return 200 OK + ApiResponse<OrganizationApiResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationApiResponse>> getOrganization(
        @PathVariable Long organizationId
    ) {
        GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
        OrganizationResponse response = organizationQueryFacade.getOrganization(query);
        OrganizationApiResponse apiResponse = OrganizationDtoMapper.toApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
