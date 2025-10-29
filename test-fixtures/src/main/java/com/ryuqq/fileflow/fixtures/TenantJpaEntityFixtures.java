package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TenantJpaEntity Object Mother Pattern
 *
 * <p>TenantJpaEntity (Persistence Layer)의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 Persistence 레이어 테스트에서 필요한 다양한 JPA Entity 상태를 제공합니다.</p>
 *
 * <h3>Domain Fixtures와의 차이점</h3>
 * <ul>
 *   <li>{@code TenantFixtures}: Domain 객체 생성 (비즈니스 로직 테스트)</li>
 *   <li>{@code TenantJpaEntityFixtures}: JPA Entity 생성 (Persistence 레이어 테스트)</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 활성 Tenant Entity
 * TenantJpaEntity entity = TenantJpaEntityFixtures.activeTenantEntity();
 *
 * // 특정 ID를 가진 Tenant Entity
 * TenantJpaEntity entity = TenantJpaEntityFixtures.tenantEntityWithId("tenant-123");
 *
 * // 일시 정지된 Tenant Entity
 * TenantJpaEntity entity = TenantJpaEntityFixtures.suspendedTenantEntity();
 *
 * // 삭제된 Tenant Entity
 * TenantJpaEntity entity = TenantJpaEntityFixtures.deletedTenantEntity();
 *
 * // 테스트용 Entity 리스트 (Pagination 테스트)
 * List<TenantJpaEntity> entities = TenantJpaEntityFixtures.tenantEntityList(10);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class TenantJpaEntityFixtures {

    private TenantJpaEntityFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 활성 Tenant Entity를 생성합니다.
     *
     * <p>"Test Company"를 이름으로 사용합니다.</p>
     * <p>신규 Entity로 생성되므로 JPA의 {@code create()} 메서드를 사용합니다 (ID는 DB에서 자동 생성).</p>
     *
     * @return ACTIVE 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity activeTenantEntity() {
        return TenantJpaEntity.create(
            "Test Company",
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 특정 ID를 가진 활성 Tenant Entity를 생성합니다.
     *
     * <p>테스트에서 특정 ID가 필요한 경우 사용합니다.</p>
     *
     * @param id Tenant ID 값 (Long AUTO_INCREMENT)
     * @return ACTIVE 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity tenantEntityWithId(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            "Test Company",
            TenantStatus.ACTIVE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID와 이름을 가진 활성 Tenant Entity를 생성합니다.
     *
     * @param id Tenant ID 값 (Long AUTO_INCREMENT)
     * @param name Tenant 이름
     * @return ACTIVE 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity activeTenantEntityWithIdAndName(Long id, String name) {
        return TenantJpaEntity.reconstitute(
            id,
            name,
            TenantStatus.ACTIVE,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 일시 정지된 Tenant Entity를 생성합니다.
     *
     * <p>DB에서 조회한 상태를 시뮬레이션하므로 {@code reconstitute()} 메서드를 사용합니다.</p>
     *
     * @return SUSPENDED 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity suspendedTenantEntity() {
        return TenantJpaEntity.reconstitute(
            1L,
            "Suspended Company",
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 특정 ID를 가진 일시 정지된 Tenant Entity를 생성합니다.
     *
     * @param id Tenant ID 값
     * @return SUSPENDED 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity suspendedTenantEntityWithId(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            "Suspended Company",
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1),
            false
        );
    }

    /**
     * 삭제된 Tenant Entity를 생성합니다 (Soft Delete).
     *
     * <p>deleted = true, status = SUSPENDED 상태입니다.</p>
     *
     * @return 삭제된 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity deletedTenantEntity() {
        return TenantJpaEntity.reconstitute(
            1L,
            "Deleted Company",
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 특정 ID를 가진 삭제된 Tenant Entity를 생성합니다.
     *
     * @param id Tenant ID 값
     * @return 삭제된 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity deletedTenantEntityWithId(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            "Deleted Company",
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 특정 이름을 가진 삭제된 Tenant Entity를 생성합니다.
     *
     * @param name Tenant 이름
     * @return 삭제된 상태의 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity deletedTenantEntityWithName(String name) {
        return TenantJpaEntity.reconstitute(
            1L,
            name,
            TenantStatus.SUSPENDED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30),
            true  // deleted
        );
    }

    /**
     * 완전히 커스터마이징된 Tenant Entity를 생성합니다.
     *
     * <p>모든 필드를 직접 지정할 수 있는 팩토리 메서드입니다.</p>
     *
     * @param id Tenant ID 값
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 생성된 TenantJpaEntity
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantJpaEntity customTenantEntity(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return TenantJpaEntity.reconstitute(id, name, status, createdAt, updatedAt, deleted);
    }

    /**
     * 활성 Tenant Entity 리스트를 생성합니다 (Pagination 테스트용).
     *
     * <p>각 Entity는 순차적인 이름을 가지며, 모두 ACTIVE 상태입니다.</p>
     * <p>생성 시간은 1일 전부터 count개의 시간 간격으로 역순 정렬됩니다.</p>
     *
     * @param count 생성할 Entity 개수
     * @return ACTIVE 상태의 TenantJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantJpaEntity> tenantEntityList(int count) {
        List<TenantJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);

        for (int i = 0; i < count; i++) {
            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                1L,
                "Test Company " + (i + 1),
                TenantStatus.ACTIVE,
                baseTime.minusHours(count - i),
                baseTime.minusHours(count - i),
                false
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 일시 정지된 Tenant Entity 리스트를 생성합니다.
     *
     * <p>각 Entity는 순차적인 이름을 가지며, 모두 SUSPENDED 상태입니다.</p>
     *
     * @param count 생성할 Entity 개수
     * @return SUSPENDED 상태의 TenantJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantJpaEntity> suspendedTenantEntityList(int count) {
        List<TenantJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(30);

        for (int i = 0; i < count; i++) {
            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                1L,
                "Suspended Company " + (i + 1),
                TenantStatus.SUSPENDED,
                baseTime.minusDays(count - i),
                baseTime.minusHours(count - i),
                false
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 삭제된 Tenant Entity 리스트를 생성합니다.
     *
     * <p>각 Entity는 순차적인 이름을 가지며, 모두 deleted = true 상태입니다.</p>
     *
     * @param count 생성할 Entity 개수
     * @return 삭제된 상태의 TenantJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantJpaEntity> deletedTenantEntityList(int count) {
        List<TenantJpaEntity> entities = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.now().minusDays(60);

        for (int i = 0; i < count; i++) {
            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                1L,
                "Deleted Company " + (i + 1),
                TenantStatus.SUSPENDED,
                baseTime.minusDays(count - i),
                baseTime.minusDays(count - i / 2),
                true  // deleted
            );
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 혼합 상태 Tenant Entity 리스트를 생성합니다 (통합 테스트용).
     *
     * <p>ACTIVE, SUSPENDED, DELETED 상태를 골고루 포함합니다.</p>
     * <p>count가 3의 배수가 아니면 ACTIVE 상태가 더 많이 생성됩니다.</p>
     *
     * @param count 생성할 Entity 개수 (최소 3)
     * @return 혼합 상태의 TenantJpaEntity 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<TenantJpaEntity> mixedStatusTenantEntityList(int count) {
        List<TenantJpaEntity> entities = new ArrayList<>(count);
        int activeCount = (count + 2) / 3;  // 반올림
        int suspendedCount = count / 3;
        int deletedCount = count - activeCount - suspendedCount;

        entities.addAll(tenantEntityList(activeCount));
        entities.addAll(suspendedTenantEntityList(suspendedCount));
        entities.addAll(deletedTenantEntityList(deletedCount));

        return entities;
    }
}
