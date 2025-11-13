package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;

import java.time.LocalDateTime;

/**
 * TenantJpaEntity Test Fixture
 *
 * <p>테스트에서 TenantJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * TenantJpaEntity tenant = TenantJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * TenantJpaEntity tenant = TenantJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * TenantJpaEntity tenant = TenantJpaEntityFixture.create("Custom Tenant");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class TenantJpaEntityFixture {

    private static final String DEFAULT_NAME = "Test Tenant";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private TenantJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final TenantStatus DEFAULT_STATUS = TenantStatus.ACTIVE;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final boolean DEFAULT_DELETED = false;

    /**
     * 기본 TenantJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 TenantJpaEntity
     */
    public static TenantJpaEntity create() {
        return TenantJpaEntity.create(
            DEFAULT_NAME,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 커스텀 TenantJpaEntity 생성 (ID 없음)
     *
     * @param name Tenant 이름
     * @return 새로운 TenantJpaEntity
     */
    public static TenantJpaEntity create(String name) {
        return TenantJpaEntity.create(
            name,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * ID를 포함한 TenantJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id Tenant ID
     * @return 재구성된 TenantJpaEntity
     */
    public static TenantJpaEntity createWithId(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 커스텀 ID를 포함한 TenantJpaEntity 생성 (재구성)
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @return 재구성된 TenantJpaEntity
     */
    public static TenantJpaEntity createWithId(Long id, String name) {
        return TenantJpaEntity.reconstitute(
            id,
            name,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 특정 상태의 TenantJpaEntity 생성 (재구성)
     *
     * @param id Tenant ID
     * @param status Tenant 상태
     * @return 재구성된 TenantJpaEntity
     */
    public static TenantJpaEntity createWithStatus(Long id, TenantStatus status) {
        return TenantJpaEntity.reconstitute(
            id,
            DEFAULT_NAME,
            status,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * SUSPENDED 상태의 TenantJpaEntity 생성 (재구성)
     *
     * @param id Tenant ID
     * @return SUSPENDED 상태의 TenantJpaEntity
     */
    public static TenantJpaEntity createSuspended(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            DEFAULT_NAME,
            TenantStatus.SUSPENDED,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 삭제된 TenantJpaEntity 생성 (재구성)
     *
     * @param id Tenant ID
     * @return 삭제된 TenantJpaEntity
     */
    public static TenantJpaEntity createDeleted(Long id) {
        return TenantJpaEntity.reconstitute(
            id,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            true
        );
    }

    /**
     * 여러 개의 TenantJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return TenantJpaEntity 배열
     */
    public static TenantJpaEntity[] createMultiple(int count) {
        TenantJpaEntity[] entities = new TenantJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_NAME + " " + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 TenantJpaEntity 생성 (재구성)
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 TenantJpaEntity
     */
    public static TenantJpaEntity reconstitute(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return TenantJpaEntity.reconstitute(
            id,
            name,
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }
}
