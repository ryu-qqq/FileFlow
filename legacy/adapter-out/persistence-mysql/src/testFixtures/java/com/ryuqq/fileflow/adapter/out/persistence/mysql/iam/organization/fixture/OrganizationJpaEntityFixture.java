package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;

import java.time.LocalDateTime;

/**
 * OrganizationJpaEntity Test Fixture
 *
 * <p>테스트에서 OrganizationJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성
 * OrganizationJpaEntity org = OrganizationJpaEntityFixture.create();
 *
 * // ID 포함 생성
 * OrganizationJpaEntity org = OrganizationJpaEntityFixture.createWithId(1L);
 *
 * // 커스텀 생성
 * OrganizationJpaEntity org = OrganizationJpaEntityFixture.create(1L, "ORG-001", "Test Org");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class OrganizationJpaEntityFixture {

    private static final Long DEFAULT_TENANT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private OrganizationJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_ORG_CODE = "ORG-001";
    private static final String DEFAULT_NAME = "Test Organization";
    private static final OrganizationStatus DEFAULT_STATUS = OrganizationStatus.ACTIVE;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final boolean DEFAULT_DELETED = false;

    /**
     * 기본 OrganizationJpaEntity 생성 (ID 없음)
     *
     * <p>신규 생성 시나리오 테스트에 사용합니다.</p>
     *
     * @return 새로운 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity create() {
        return OrganizationJpaEntity.create(
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 커스텀 OrganizationJpaEntity 생성 (ID 없음)
     *
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 새로운 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity create(Long tenantId, String orgCode, String name) {
        return OrganizationJpaEntity.create(
            tenantId,
            orgCode,
            name,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * ID를 포함한 OrganizationJpaEntity 생성 (재구성)
     *
     * <p>DB 조회 시나리오 테스트에 사용합니다.</p>
     *
     * @param id Organization ID
     * @return 재구성된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity createWithId(Long id) {
        return OrganizationJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 커스텀 ID를 포함한 OrganizationJpaEntity 생성 (재구성)
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 재구성된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity createWithId(Long id, Long tenantId, String orgCode, String name) {
        return OrganizationJpaEntity.reconstitute(
            id,
            tenantId,
            orgCode,
            name,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 특정 상태의 OrganizationJpaEntity 생성 (재구성)
     *
     * @param id Organization ID
     * @param status Organization 상태
     * @return 재구성된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity createWithStatus(Long id, OrganizationStatus status) {
        return OrganizationJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME,
            status,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            DEFAULT_DELETED
        );
    }

    /**
     * 삭제된 OrganizationJpaEntity 생성 (재구성)
     *
     * @param id Organization ID
     * @return 삭제된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity createDeleted(Long id) {
        return OrganizationJpaEntity.reconstitute(
            id,
            DEFAULT_TENANT_ID,
            DEFAULT_ORG_CODE,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT,
            true
        );
    }

    /**
     * 여러 개의 OrganizationJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return OrganizationJpaEntity 배열
     */
    public static OrganizationJpaEntity[] createMultiple(int count) {
        OrganizationJpaEntity[] entities = new OrganizationJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createWithId(
                (long) (i + 1),
                DEFAULT_TENANT_ID,
                DEFAULT_ORG_CODE + "-" + (i + 1),
                DEFAULT_NAME + " " + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 OrganizationJpaEntity 생성 (재구성)
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity reconstitute(
        Long id,
        Long tenantId,
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
}
