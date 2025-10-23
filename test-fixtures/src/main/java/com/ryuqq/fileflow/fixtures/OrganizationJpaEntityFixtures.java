package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationJpaEntity Object Mother Pattern
 *
 * <p>OrganizationJpaEntity (Persistence Layer)의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 Persistence 레이어 테스트에서 필요한 다양한 JPA Entity 상태를 제공합니다.</p>
 *
 * <h3>Domain Fixtures와의 차이점</h3>
 * <ul>
 *   <li>{@code OrganizationFixtures}: Domain 객체 생성 (비즈니스 로직 테스트)</li>
 *   <li>{@code OrganizationJpaEntityFixtures}: JPA Entity 생성 (Persistence 레이어 테스트)</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 Sales 조직 Entity
 * OrganizationJpaEntity entity = OrganizationJpaEntityFixtures.salesOrgEntity("tenant-123");
 *
 * // 특정 ID를 가진 조직 Entity
 * OrganizationJpaEntity entity = OrganizationJpaEntityFixtures.orgEntityWithId(1L, "tenant-123");
 *
 * // 비활성화된 조직 Entity
 * OrganizationJpaEntity entity = OrganizationJpaEntityFixtures.inactiveOrgEntity("tenant-123");
 *
 * // 삭제된 조직 Entity
 * OrganizationJpaEntity entity = OrganizationJpaEntityFixtures.deletedOrgEntity("tenant-123");
 *
 * // 테스트용 Entity 리스트 (Pagination 테스트)
 * List<OrganizationJpaEntity> entities = OrganizationJpaEntityFixtures.orgEntityList("tenant-123", 10);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class OrganizationJpaEntityFixtures {

    private OrganizationJpaEntityFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * Sales 조직 Entity를 생성합니다 (신규, ID는 JPA가 자동 생성).
     *
     * <p>JPA의 {@code create()} 메서드를 사용하여 신규 Entity를 생성합니다.</p>
     * <p>ID는 null이며, DB 저장 시 Auto Increment로 생성됩니다.</p>
     *
     * @param tenantId 소속 Tenant ID (String - Tenant PK 타입과 일치)
     * @return ACTIVE 상태의 Sales OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity salesOrgEntity(String tenantId) {
        return OrganizationJpaEntity.create(
            tenantId,
            "SALES",
            "Sales Department",
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * HR 조직 Entity를 생성합니다 (신규, ID는 JPA가 자동 생성).
     *
     * @param tenantId 소속 Tenant ID
     * @return ACTIVE 상태의 HR OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity hrOrgEntity(String tenantId) {
        return OrganizationJpaEntity.create(
            tenantId,
            "HR",
            "Human Resources",
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * IT 조직 Entity를 생성합니다 (신규, ID는 JPA가 자동 생성).
     *
     * @param tenantId 소속 Tenant ID
     * @return ACTIVE 상태의 IT OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity itOrgEntity(String tenantId) {
        return OrganizationJpaEntity.create(
            tenantId,
            "IT",
            "IT Department",
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 특정 조직 코드와 이름을 가진 조직 Entity를 생성합니다 (신규).
     *
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return ACTIVE 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity orgEntityWithCode(String tenantId, String orgCode, String name) {
        return OrganizationJpaEntity.create(
            tenantId,
            orgCode,
            name,
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 특정 ID를 가진 조직 Entity를 생성합니다 (DB 저장 후 상태).
     *
     * <p>DB에서 조회한 상태를 시뮬레이션하므로 {@code reconstitute()} 메서드를 사용합니다.</p>
     *
     * @param id Organization ID (Long, Auto Increment)
     * @param tenantId 소속 Tenant ID
     * @return ACTIVE 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity orgEntityWithId(Long id, String tenantId) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            "ORG-DEFAULT",
            "Default Organization",
            OrganizationStatus.ACTIVE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID와 조직 코드를 가진 조직 Entity를 생성합니다.
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return ACTIVE 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity orgEntityWithIdAndCode(
        Long id,
        String tenantId,
        String orgCode,
        String name
    ) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            orgCode,
            name,
            OrganizationStatus.ACTIVE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 비활성화된 조직 Entity를 생성합니다.
     *
     * @param tenantId 소속 Tenant ID
     * @return INACTIVE 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity inactiveOrgEntity(String tenantId) {
        return OrganizationJpaEntity.reconstitute(
            999L,
            tenantId,
            "INACTIVE-ORG",
            "Inactive Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID를 가진 비활성화된 조직 Entity를 생성합니다.
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @return INACTIVE 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity inactiveOrgEntityWithId(Long id, String tenantId) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            "INACTIVE-ORG",
            "Inactive Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 삭제된 조직 Entity를 생성합니다 (Soft Delete).
     *
     * <p>deleted = true, status = INACTIVE 상태입니다.</p>
     *
     * @param tenantId 소속 Tenant ID
     * @return 삭제된 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity deletedOrgEntity(String tenantId) {
        return OrganizationJpaEntity.reconstitute(
            999L,
            tenantId,
            "DELETED-ORG",
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 특정 ID를 가진 삭제된 조직 Entity를 생성합니다.
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @return 삭제된 상태의 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity deletedOrgEntityWithId(Long id, String tenantId) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            "DELETED-ORG",
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 완전히 커스터마이징된 조직 Entity를 생성합니다.
     *
     * <p>모든 필드를 직접 지정할 수 있는 팩토리 메서드입니다.</p>
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 생성된 OrganizationJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationJpaEntity customOrgEntity(
        Long id,
        String tenantId,
        String orgCode,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            orgCode,
            name,
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * 활성 조직 Entity 리스트를 생성합니다 (Pagination 테스트용).
     *
     * <p>각 Entity는 순차적인 ID와 이름을 가지며, 모두 ACTIVE 상태입니다.</p>
     *
     * @param tenantId 소속 Tenant ID (모든 조직이 동일한 Tenant 소속)
     * @param count 생성할 Entity 개수
     * @return ACTIVE 상태의 OrganizationJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationJpaEntity> orgEntityList(String tenantId, int count) {
        List<OrganizationJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);

        for (int i = 0; i < count; i++) {
            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                (long) (i + 1),
                tenantId,
                "ORG-" + String.format("%03d", i + 1),
                "Organization " + (i + 1),
                OrganizationStatus.ACTIVE,
                baseTime.minusHours(count - i),
                baseTime.minusHours(count - i),
                false
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 비활성화된 조직 Entity 리스트를 생성합니다.
     *
     * <p>각 Entity는 순차적인 ID와 이름을 가지며, 모두 INACTIVE 상태입니다.</p>
     *
     * @param tenantId 소속 Tenant ID
     * @param count 생성할 Entity 개수
     * @return INACTIVE 상태의 OrganizationJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationJpaEntity> inactiveOrgEntityList(String tenantId, int count) {
        List<OrganizationJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(30);

        for (int i = 0; i < count; i++) {
            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                (long) (i + 1),
                tenantId,
                "INACTIVE-" + String.format("%03d", i + 1),
                "Inactive Organization " + (i + 1),
                OrganizationStatus.INACTIVE,
                baseTime.minusDays(count - i),
                baseTime.minusHours(count - i),
                false
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 삭제된 조직 Entity 리스트를 생성합니다.
     *
     * <p>각 Entity는 순차적인 ID와 이름을 가지며, 모두 deleted = true 상태입니다.</p>
     *
     * @param tenantId 소속 Tenant ID
     * @param count 생성할 Entity 개수
     * @return 삭제된 상태의 OrganizationJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationJpaEntity> deletedOrgEntityList(String tenantId, int count) {
        List<OrganizationJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(60);

        for (int i = 0; i < count; i++) {
            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                (long) (i + 1),
                tenantId,
                "DELETED-" + String.format("%03d", i + 1),
                "Deleted Organization " + (i + 1),
                OrganizationStatus.INACTIVE,
                baseTime.minusDays(count - i),
                baseTime.minusDays(count - i / 2),
                true  // deleted
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 혼합 상태 조직 Entity 리스트를 생성합니다 (통합 테스트용).
     *
     * <p>ACTIVE, INACTIVE, DELETED 상태를 골고루 포함합니다.</p>
     * <p>count가 3의 배수가 아니면 ACTIVE 상태가 더 많이 생성됩니다.</p>
     *
     * @param tenantId 소속 Tenant ID
     * @param count 생성할 Entity 개수 (최소 3)
     * @return 혼합 상태의 OrganizationJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationJpaEntity> mixedStatusOrgEntityList(String tenantId, int count) {
        List<OrganizationJpaEntity> entities = new ArrayList<>(count);
        int activeCount = (count + 2) / 3;  // 반올림
        int inactiveCount = count / 3;
        int deletedCount = count - activeCount - inactiveCount;

        // ID 중복 방지를 위해 오프셋 적용
        entities.addAll(orgEntityList(tenantId, activeCount));

        List<OrganizationJpaEntity> inactive = inactiveOrgEntityList(tenantId, inactiveCount);
        for (int i = 0; i < inactive.size(); i++) {
            OrganizationJpaEntity entity = inactive.get(i);
            entities.add(OrganizationJpaEntity.reconstitute(
                (long) (activeCount + i + 1),
                entity.getTenantId(),
                entity.getOrgCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted()
            ));
        }

        List<OrganizationJpaEntity> deleted = deletedOrgEntityList(tenantId, deletedCount);
        for (int i = 0; i < deleted.size(); i++) {
            OrganizationJpaEntity entity = deleted.get(i);
            entities.add(OrganizationJpaEntity.reconstitute(
                (long) (activeCount + inactiveCount + i + 1),
                entity.getTenantId(),
                entity.getOrgCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted()
            ));
        }

        return entities;
    }
}
