package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.mapper.OrganizationEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.repository.OrganizationQueryDslRepository;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * OrganizationQueryRepositoryAdapter - Organization Query 전용 Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * QueryDSL을 사용하여 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>QueryDSL을 통한 동적 쿼리 생성</li>
 *   <li>Pagination 지원 (Offset-based, Cursor-based)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code OrganizationQueryRepositoryPort} 구현</li>
 *   <li>✅ QueryDSL Repository 사용</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Component
public class OrganizationQueryRepositoryAdapter implements OrganizationQueryRepositoryPort {

    private final OrganizationQueryDslRepository repository;

    /**
     * Constructor - 의존성 주입
     *
     * @param repository OrganizationQueryDslRepository
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationQueryRepositoryAdapter(OrganizationQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * Organization ID로 단건 조회
     *
     * @param organizationId Organization ID
     * @return Optional<Organization>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public Optional<Organization> findById(OrganizationId organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("OrganizationId는 필수입니다");
        }

        return repository.findById(organizationId.value())
            .map(OrganizationEntityMapper::toDomain);
    }

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return Organization 목록
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public List<Organization> findAllWithOffset(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    ) {
        List<OrganizationJpaEntity> entities = repository.findAllWithOffset(
            tenantId,
            orgCodeContains,
            nameContains,
            deleted,
            offset,
            limit
        );

        return entities.stream()
            .map(OrganizationEntityMapper::toDomain)
            .toList();
    }

    /**
     * Organization 목록 총 개수 조회
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public long countAll(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted
    ) {
        return repository.countAll(
            tenantId,
            orgCodeContains,
            nameContains,
            deleted
        );
    }

    /**
     * Organization 목록 조회 (Cursor-based Pagination)
     *
     * <p>Cursor는 Base64로 인코딩된 "createdAt|id" 복합 키입니다.
     * Organization ID는 Long이므로 순차적이지만, 일관성을 위해 createdAt + id 복합 정렬을 사용합니다.</p>
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return Organization 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public List<Organization> findAllWithCursor(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    ) {
        List<OrganizationJpaEntity> entities = repository.findAllWithCursor(
            tenantId,
            orgCodeContains,
            nameContains,
            deleted,
            cursor,
            limit
        );

        return entities.stream()
            .map(OrganizationEntityMapper::toDomain)
            .toList();
    }
}
