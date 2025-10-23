package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.assembler.OrganizationAssembler;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationsUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

/**
 * OrganizationQueryService - Organization Query 통합 서비스
 *
 * <p>CQRS 패턴의 Query Service입니다.
 * 조회 작업만 담당하며, 데이터 변경은 하지 않습니다.</p>
 *
 * <p><strong>구현 UseCase:</strong></p>
 * <ul>
 *   <li>{@link GetOrganizationUseCase} - Organization 단건 조회</li>
 *   <li>{@link GetOrganizationsUseCase} - Organization 목록 조회</li>
 * </ul>
 *
 * <p><strong>Transaction 설정:</strong></p>
 * <ul>
 *   <li>✅ {@code @Transactional(readOnly = true)} 사용</li>
 *   <li>✅ 읽기 전용 트랜잭션으로 성능 최적화</li>
 *   <li>✅ Dirty Checking 비활성화</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Law of Demeter 준수</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 사용</li>
 *   <li>✅ 조회 전용 (데이터 변경 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Service
@Transactional(readOnly = true)
public class OrganizationQueryService implements GetOrganizationUseCase, GetOrganizationsUseCase {

    private final OrganizationQueryRepositoryPort organizationQueryRepositoryPort;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param organizationQueryRepositoryPort Organization Query Repository Port
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationQueryService(OrganizationQueryRepositoryPort organizationQueryRepositoryPort) {
        this.organizationQueryRepositoryPort = organizationQueryRepositoryPort;
    }

    /**
     * Organization 단건 조회 UseCase 실행
     *
     * @param query Organization 조회 Query
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException query가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public OrganizationResponse execute(GetOrganizationQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetOrganizationQuery는 필수입니다");
        }

        OrganizationId organizationId = OrganizationId.of(query.organizationId());
        Organization organization = organizationQueryRepositoryPort.findById(organizationId)
            .orElseThrow(() -> new IllegalStateException(
                "Organization을 찾을 수 없습니다: " + query.organizationId()
            ));

        return OrganizationAssembler.toResponse(organization);
    }

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * @param query Organization 목록 조회 Query
     * @return PageResponse<OrganizationResponse>
     * @throws IllegalArgumentException query가 null이거나 page가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public PageResponse<OrganizationResponse> executeWithPage(GetOrganizationsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetOrganizationsQuery는 필수입니다");
        }
        if (!query.isOffsetBased()) {
            throw new IllegalArgumentException("Offset-based Pagination을 위해서는 page가 필요합니다");
        }

        // 1. 데이터 조회
        int offset = query.page() * query.size();
        List<Organization> organizations = organizationQueryRepositoryPort.findAllWithOffset(
            query.tenantId(),
            query.orgCodeContains(),
            query.nameContains(),
            query.deleted(),
            offset,
            query.size()
        );

        // 2. 전체 개수 조회 (COUNT 쿼리)
        long totalElements = organizationQueryRepositoryPort.countAll(
            query.tenantId(),
            query.orgCodeContains(),
            query.nameContains(),
            query.deleted()
        );

        // 3. DTO 변환
        List<OrganizationResponse> content = organizations.stream()
            .map(OrganizationAssembler::toResponse)
            .toList();

        // 4. PageResponse 생성
        int totalPages = (int) Math.ceil((double) totalElements / query.size());
        boolean first = query.page() == 0;
        boolean last = query.page() >= totalPages - 1;

        return PageResponse.of(
            content,
            query.page(),
            query.size(),
            totalElements,
            totalPages,
            first,
            last
        );
    }

    /**
     * Organization 목록 조회 (Cursor-based Pagination)
     *
     * @param query Organization 목록 조회 Query
     * @return SliceResponse<OrganizationResponse>
     * @throws IllegalArgumentException query가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public SliceResponse<OrganizationResponse> executeWithSlice(GetOrganizationsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetOrganizationsQuery는 필수입니다");
        }

        // 1. 데이터 조회 (limit + 1개 조회하여 hasNext 판단)
        List<Organization> organizations = organizationQueryRepositoryPort.findAllWithCursor(
            query.tenantId(),
            query.orgCodeContains(),
            query.nameContains(),
            query.deleted(),
            query.cursor(),
            query.size() + 1  // limit + 1
        );

        // 2. hasNext 판단 및 실제 content 분리
        boolean hasNext = organizations.size() > query.size();
        List<Organization> content = hasNext
            ? organizations.subList(0, query.size())
            : organizations;

        // 3. nextCursor 생성 (마지막 항목의 ID를 Base64 인코딩)
        String nextCursor = hasNext && !content.isEmpty()
            ? encodeCursor(content.getLast().getIdValue())
            : null;

        // 4. DTO 변환
        List<OrganizationResponse> responseContent = content.stream()
            .map(OrganizationAssembler::toResponse)
            .toList();

        return SliceResponse.of(
            responseContent,
            query.size(),
            hasNext,
            nextCursor
        );
    }

    /**
     * Cursor 인코딩 (Organization ID를 Base64로 인코딩)
     *
     * @param organizationId Organization ID
     * @return Base64 인코딩된 커서
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private String encodeCursor(Long organizationId) {
        return Base64.getUrlEncoder().encodeToString(organizationId.toString().getBytes());
    }
}
