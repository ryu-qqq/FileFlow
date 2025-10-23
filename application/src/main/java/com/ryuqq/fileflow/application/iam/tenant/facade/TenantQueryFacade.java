package com.ryuqq.fileflow.application.iam.tenant.facade;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantTreeUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TenantQueryFacade - Tenant Query 통합 Facade
 *
 * <p>여러 Query UseCase를 하나의 Facade로 통합하여 Controller 의존성을 줄입니다.</p>
 *
 * <p><strong>Facade Pattern 적용:</strong></p>
 * <ul>
 *   <li>✅ Controller 의존성 감소: 2개 UseCase → 1개 Facade</li>
 *   <li>✅ 단일 진입점 제공: 모든 Query 작업을 하나의 Facade로 처리</li>
 *   <li>✅ 인터페이스 없이 구현체만 제공 (YAGNI 원칙)</li>
 *   <li>✅ {@code @Transactional(readOnly = true)} 적용 (조회 최적화)</li>
 * </ul>
 *
 * <p><strong>담당 Query UseCase:</strong></p>
 * <ul>
 *   <li>{@link GetTenantUseCase} - Tenant 단건 조회</li>
 *   <li>{@link GetTenantsUseCase} - Tenant 목록 조회 (Offset/Cursor Pagination)</li>
 *   <li>{@link GetTenantTreeUseCase} - Tenant 트리 조회 (Tenant + Organizations)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ 단순 위임 패턴 (추가 로직 없음)</li>
 *   <li>✅ {@code @Service} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 읽기 전용 트랜잭션 (성능 최적화)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Service
@Transactional(readOnly = true)
public class TenantQueryFacade {

    private final GetTenantUseCase getTenantUseCase;
    private final GetTenantsUseCase getTenantsUseCase;
    private final GetTenantTreeUseCase getTenantTreeUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param getTenantUseCase Tenant 단건 조회 UseCase
     * @param getTenantsUseCase Tenant 목록 조회 UseCase
     * @param getTenantTreeUseCase Tenant 트리 조회 UseCase
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantQueryFacade(
        GetTenantUseCase getTenantUseCase,
        GetTenantsUseCase getTenantsUseCase,
        GetTenantTreeUseCase getTenantTreeUseCase
    ) {
        this.getTenantUseCase = getTenantUseCase;
        this.getTenantsUseCase = getTenantsUseCase;
        this.getTenantTreeUseCase = getTenantTreeUseCase;
    }

    /**
     * Tenant 단건 조회
     *
     * <p>{@link GetTenantUseCase}로 위임합니다.</p>
     *
     * @param query Tenant 단건 조회 Query
     * @return TenantResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantResponse getTenant(GetTenantQuery query) {
        return getTenantUseCase.execute(query);
    }

    /**
     * Tenant 목록 조회 (Offset-based Pagination)
     *
     * <p>{@link GetTenantsUseCase#executeWithPage(GetTenantsQuery)}로 위임합니다.</p>
     * <p>전체 개수(totalElements)를 포함한 페이지 정보를 반환합니다.</p>
     *
     * @param query Tenant 목록 조회 Query (page 필수)
     * @return PageResponse<TenantResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public PageResponse<TenantResponse> getTenantsWithPage(GetTenantsQuery query) {
        return getTenantsUseCase.executeWithPage(query);
    }

    /**
     * Tenant 목록 조회 (Cursor-based Pagination)
     *
     * <p>{@link GetTenantsUseCase#executeWithSlice(GetTenantsQuery)}로 위임합니다.</p>
     * <p>다음 페이지 존재 여부(hasNext)와 커서(nextCursor)를 반환합니다.</p>
     *
     * @param query Tenant 목록 조회 Query (cursor 선택적)
     * @return SliceResponse<TenantResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public SliceResponse<TenantResponse> getTenantsWithSlice(GetTenantsQuery query) {
        return getTenantsUseCase.executeWithSlice(query);
    }

    /**
     * Tenant 트리 조회 (Tenant + Organization 목록)
     *
     * <p>{@link GetTenantTreeUseCase}로 위임합니다.</p>
     * <p>Tenant 정보와 하위 Organization 목록을 트리 구조로 반환합니다.</p>
     *
     * @param query Tenant 트리 조회 Query
     * @return TenantTreeResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantTreeResponse getTenantTree(GetTenantTreeQuery query) {
        return getTenantTreeUseCase.execute(query);
    }
}
