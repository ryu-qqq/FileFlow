package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto.GrantReadModel;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper.GrantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.GrantQueryRepository;
import com.ryuqq.fileflow.application.iam.permission.port.out.GrantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Grant Persistence Adapter - GrantRepositoryPort 구현
 *
 * <p><strong>역할</strong>: Hexagonal Architecture의 Driven Adapter (Outbound Adapter)입니다.</p>
 * <p>Application Layer에서 정의한 {@link GrantRepositoryPort}를 구현하여
 * Persistence Layer (GrantQueryRepository)와 연결합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Hexagonal Architecture - Driven Adapter</li>
 *   <li>✅ 의존성 역전 원칙 (DIP) - Application이 정의한 Port 구현</li>
 *   <li>✅ {@code @Component} 사용 - Spring Bean 자동 등록</li>
 *   <li>✅ GrantQueryRepository 위임 + DTO → Domain 변환</li>
 * </ul>
 *
 * <h3>처리 흐름</h3>
 * <ol>
 *   <li>Application Layer → GrantRepositoryPort.findEffectiveGrants() 호출</li>
 *   <li>GrantPersistenceAdapter → GrantQueryRepository.findEffectiveGrants() 위임</li>
 *   <li>GrantQueryRepository → 4-table JOIN 쿼리 실행 (MySQL)</li>
 *   <li>GrantReadModel 리스트 반환</li>
 *   <li>GrantEntityMapper → Grant domain 리스트로 변환</li>
 *   <li>Application Layer로 Grant 리스트 반환</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Component
public class GrantPersistenceAdapter implements GrantRepositoryPort {

    private final GrantQueryRepository grantQueryRepository;

    /**
     * Constructor - 의존성 주입
     *
     * @param grantQueryRepository Grant Query Repository
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public GrantPersistenceAdapter(GrantQueryRepository grantQueryRepository) {
        this.grantQueryRepository = grantQueryRepository;
    }

    /**
     * 사용자의 유효 Grants 조회
     *
     * <p>GrantQueryRepository에 위임하여 4-table JOIN 쿼리를 실행하고,
     * 결과를 Domain Model (Grant)로 변환하여 반환합니다.</p>
     *
     * <p><strong>성능 특징</strong>:</p>
     * <ul>
     *   <li>✅ 단일 쿼리로 모든 Grant 조회 (N+1 문제 해결)</li>
     *   <li>✅ DTO Projection으로 필요한 컬럼만 조회</li>
     *   <li>✅ INNER JOIN으로 삭제된 Role/Permission 자동 필터링</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @return Grant 리스트 (빈 List 가능, null 불가)
     * @throws IllegalArgumentException userId, tenantId, organizationId가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-27
     */
    @Override
    public List<Grant> findEffectiveGrants(Long userId, Long tenantId, Long organizationId) {
        // 1. GrantQueryRepository에 위임하여 4-table JOIN 쿼리 실행
        List<GrantReadModel> readModels = grantQueryRepository.findEffectiveGrants(
            userId,
            tenantId,
            organizationId
        );

        // 2. GrantReadModel → Grant domain 변환
        return readModels.stream()
            .map(GrantEntityMapper::toDomain)
            .toList();
    }
}
