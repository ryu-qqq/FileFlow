package com.ryuqq.fileflow.application.iam.organization.facade;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrganizationQueryFacade - Organization Query 통합 Facade
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
 *   <li>{@link GetOrganizationUseCase} - Organization 단건 조회</li>
 *   <li>{@link GetOrganizationsUseCase} - Organization 목록 조회 (Offset/Cursor Pagination)</li>
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
public class OrganizationQueryFacade {

    private final GetOrganizationUseCase getOrganizationUseCase;
    private final GetOrganizationsUseCase getOrganizationsUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param getOrganizationUseCase Organization 단건 조회 UseCase
     * @param getOrganizationsUseCase Organization 목록 조회 UseCase
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationQueryFacade(
        GetOrganizationUseCase getOrganizationUseCase,
        GetOrganizationsUseCase getOrganizationsUseCase
    ) {
        this.getOrganizationUseCase = getOrganizationUseCase;
        this.getOrganizationsUseCase = getOrganizationsUseCase;
    }

    /**
     * Organization 단건 조회
     *
     * <p>{@link GetOrganizationUseCase}로 위임합니다.</p>
     *
     * @param query Organization 단건 조회 Query
     * @return OrganizationResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationResponse getOrganization(GetOrganizationQuery query) {
        return getOrganizationUseCase.execute(query);
    }

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * <p>{@link GetOrganizationsUseCase#executeWithPage(GetOrganizationsQuery)}로 위임합니다.</p>
     * <p>전체 개수(totalElements)를 포함한 페이지 정보를 반환합니다.</p>
     *
     * @param query Organization 목록 조회 Query (page 필수)
     * @return PageResponse<OrganizationResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public PageResponse<OrganizationResponse> getOrganizationsWithPage(GetOrganizationsQuery query) {
        return getOrganizationsUseCase.executeWithPage(query);
    }

    /**
     * Organization 목록 조회 (Cursor-based Pagination)
     *
     * <p>{@link GetOrganizationsUseCase#executeWithSlice(GetOrganizationsQuery)}로 위임합니다.</p>
     * <p>다음 페이지 존재 여부(hasNext)와 커서(nextCursor)를 반환합니다.</p>
     *
     * @param query Organization 목록 조회 Query (cursor 선택적)
     * @return SliceResponse<OrganizationResponse>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public SliceResponse<OrganizationResponse> getOrganizationsWithSlice(GetOrganizationsQuery query) {
        return getOrganizationsUseCase.executeWithSlice(query);
    }
}
