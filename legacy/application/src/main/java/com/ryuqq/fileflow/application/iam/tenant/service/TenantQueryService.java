package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.assembler.TenantAssembler;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantsUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

/**
 * TenantQueryService - Tenant Query 통합 서비스
 *
 * <p>CQRS 패턴의 Query Service입니다.
 * 조회 작업만 담당하며, 데이터 변경은 하지 않습니다.</p>
 *
 * <p><strong>구현 UseCase:</strong></p>
 * <ul>
 *   <li>{@link GetTenantUseCase} - Tenant 단건 조회</li>
 *   <li>{@link GetTenantsUseCase} - Tenant 목록 조회</li>
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
 *   <li>✅ 조회 전용 (데이터 변경 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Service
@Transactional(readOnly = true)
public class TenantQueryService implements GetTenantUseCase, GetTenantsUseCase {

    private final TenantQueryRepositoryPort tenantQueryRepositoryPort;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param tenantQueryRepositoryPort Tenant Query Repository Port
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantQueryService(TenantQueryRepositoryPort tenantQueryRepositoryPort) {
        this.tenantQueryRepositoryPort = tenantQueryRepositoryPort;
    }

    /**
     * Tenant 단건 조회 UseCase 실행
     *
     * @param query Tenant 조회 Query
     * @return TenantResponse DTO
     * @throws IllegalArgumentException query가 null인 경우
     * @throws IllegalStateException Tenant를 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public TenantResponse execute(GetTenantQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetTenantQuery는 필수입니다");
        }

        TenantId tenantId = TenantId.of(query.tenantId());
        Tenant tenant = tenantQueryRepositoryPort.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException(
                "Tenant를 찾을 수 없습니다: " + query.tenantId()
            ));

        return TenantAssembler.toResponse(tenant);
    }

    /**
     * Tenant 목록 조회 (Offset-based Pagination)
     *
     * @param query Tenant 목록 조회 Query
     * @return PageResponse<TenantResponse>
     * @throws IllegalArgumentException query가 null이거나 page가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public PageResponse<TenantResponse> executeWithPage(GetTenantsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetTenantsQuery는 필수입니다");
        }
        if (!query.isOffsetBased()) {
            throw new IllegalArgumentException("Offset-based Pagination을 위해서는 page가 필요합니다");
        }

        // 1. 데이터 조회
        int offset = query.page() * query.size();
        List<Tenant> tenants = tenantQueryRepositoryPort.findAllWithOffset(
            query.nameContains(),
            query.deleted(),
            offset,
            query.size()
        );

        // 2. 전체 개수 조회 (COUNT 쿼리)
        long totalElements = tenantQueryRepositoryPort.countAll(
            query.nameContains(),
            query.deleted()
        );

        // 3. DTO 변환
        List<TenantResponse> content = tenants.stream()
            .map(TenantAssembler::toResponse)
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
     * Tenant 목록 조회 (Cursor-based Pagination)
     *
     * @param query Tenant 목록 조회 Query
     * @return SliceResponse<TenantResponse>
     * @throws IllegalArgumentException query가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public SliceResponse<TenantResponse> executeWithSlice(GetTenantsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("GetTenantsQuery는 필수입니다");
        }

        // 1. 데이터 조회 (limit + 1개 조회하여 hasNext 판단)
        List<Tenant> tenants = tenantQueryRepositoryPort.findAllWithCursor(
            query.nameContains(),
            query.deleted(),
            query.cursor(),
            query.size() + 1  // limit + 1
        );

        // 2. hasNext 판단 및 실제 content 분리
        boolean hasNext = tenants.size() > query.size();
        List<Tenant> content = hasNext
            ? tenants.subList(0, query.size())
            : tenants;

        // 3. nextCursor 생성 (마지막 항목의 ID를 Base64 인코딩)
        String nextCursor = hasNext && !content.isEmpty()
            ? encodeCursor(content.get(content.size() - 1).getIdValue())
            : null;

        // 4. DTO 변환
        List<TenantResponse> responseContent = content.stream()
            .map(TenantAssembler::toResponse)
            .toList();

        return SliceResponse.of(
            responseContent,
            query.size(),
            hasNext,
            nextCursor
        );
    }

    /**
     * Cursor 인코딩 (Tenant ID를 Base64로 인코딩)
     *
     * @param tenantId Tenant ID (Long - Tenant PK 타입과 일치)
     * @return Base64 인코딩된 커서
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private String encodeCursor(Long tenantId) {
        return Base64.getUrlEncoder().encodeToString(String.valueOf(tenantId).getBytes());
    }
}
