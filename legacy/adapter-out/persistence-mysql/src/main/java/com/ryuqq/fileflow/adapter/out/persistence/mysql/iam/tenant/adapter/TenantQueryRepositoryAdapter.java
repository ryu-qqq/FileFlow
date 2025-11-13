package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.mapper.TenantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.repository.TenantQueryDslRepository;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * TenantQueryRepositoryAdapter - Tenant Query 전용 Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * QueryDSL을 사용하여 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Tenant 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>QueryDSL을 통한 동적 쿼리 생성</li>
 *   <li>Pagination 지원 (Offset-based, Cursor-based)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code TenantQueryRepositoryPort} 구현</li>
 *   <li>✅ TenantQueryDslRepository 위임 (JPAQueryFactory 직접 사용 금지)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Component
public class TenantQueryRepositoryAdapter implements TenantQueryRepositoryPort {

    private final TenantQueryDslRepository repository;

    /**
     * Constructor - 의존성 주입
     *
     * @param repository TenantQueryDslRepository
     * @author ryu-qqq
     * @since 2025-11-11
     */
    public TenantQueryRepositoryAdapter(TenantQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * Tenant ID로 단건 조회
     *
     * @param tenantId Tenant ID
     * @return Optional<Tenant>
     * @author ryu-qqq
     * @since 2025-11-11
     */
    @Override
    public Optional<Tenant> findById(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }

        return repository.findById(tenantId.value())
            .map(TenantEntityMapper::toDomain);
    }

    /**
     * Tenant 목록 조회 (Offset-based Pagination)
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return Tenant 목록
     * @author ryu-qqq
     * @since 2025-11-11
     */
    @Override
    public List<Tenant> findAllWithOffset(
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    ) {
        return repository.findAllWithOffset(nameContains, deleted, offset, limit)
            .stream()
            .map(TenantEntityMapper::toDomain)
            .toList();
    }

    /**
     * Tenant 목록 총 개수 조회
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-11-11
     */
    @Override
    public long countAll(String nameContains, Boolean deleted) {
        return repository.countAll(nameContains, deleted);
    }

    /**
     * Tenant 목록 조회 (Cursor-based Pagination)
     *
     * <p>Cursor는 Base64로 인코딩된 "createdAt|id" 복합 키입니다.
     * UUID는 순차적이지 않으므로 createdAt + id 복합 정렬을 사용하여 일관된 순서를 보장합니다.</p>
     *
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return Tenant 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-11-11
     */
    @Override
    public List<Tenant> findAllWithCursor(
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    ) {
        return repository.findAllWithCursor(nameContains, deleted, cursor, limit)
            .stream()
            .map(TenantEntityMapper::toDomain)
            .toList();
    }
}
