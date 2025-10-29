package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.mapper.OrganizationEntityMapper;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrganizationEntityMapperTest - OrganizationEntityMapper 단위 테스트
 *
 * <p>OrganizationEntityMapper의 Domain ↔ JPA Entity 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code toDomain()}: JPA Entity → Domain 변환 검증</li>
 *   <li>✅ {@code toEntity()}: Domain → JPA Entity 변환 검증</li>
 *   <li>✅ null 파라미터 예외 처리 검증</li>
 *   <li>✅ Value Object 변환 검증 (OrganizationId, OrgCode)</li>
 *   <li>✅ Status Enum 변환 검증</li>
 *   <li>✅ String FK 전략 검증 (tenantId는 String 타입)</li>
 *   <li>✅ 양방향 변환 일관성 검증 (Domain → Entity → Domain)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("OrganizationEntityMapper 테스트")
class OrganizationEntityMapperTest {

    private static final String DEFAULT_TENANT_ID = "tenant-uuid-123";

    @Nested
    @DisplayName("toDomain() - JPA Entity → Domain 변환")
    class ToDomainTests {

        /**
         * 정상: ACTIVE 상태 Organization Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ACTIVE 상태 Organization Entity를 Domain으로 변환한다")
        void toDomain_ActiveOrganizationEntity_ReturnsDomain() {
            // Given - ACTIVE 상태 JPA Entity
            Long id = 1L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "SALES";
            String name = "Sales Department";
            OrganizationStatus status = OrganizationStatus.ACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
            LocalDateTime updatedAt = LocalDateTime.now();

            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                id, tenantId, orgCode, name, status, createdAt, updatedAt, false
            );

            // When - Domain 변환
            Organization domain = OrganizationEntityMapper.toDomain(entity);

            // Then - 모든 필드 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.getIdValue()).isEqualTo(id);
            assertThat(domain.getTenantId()).isEqualTo(tenantId);  // String FK
            assertThat(domain.getOrgCodeValue()).isEqualTo(orgCode);
            assertThat(domain.getName()).isEqualTo(name);
            assertThat(domain.getStatus()).isEqualTo(status);
            assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
            assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(domain.isDeleted()).isFalse();
        }

        /**
         * 정상: INACTIVE 상태 Organization Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("INACTIVE 상태 Organization Entity를 Domain으로 변환한다")
        void toDomain_InactiveOrganizationEntity_ReturnsDomain() {
            // Given - INACTIVE 상태 JPA Entity
            Long id = 2L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "HR";
            String name = "HR Department";
            OrganizationStatus status = OrganizationStatus.INACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(60);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                id, tenantId, orgCode, name, status, createdAt, updatedAt, false
            );

            // When - Domain 변환
            Organization domain = OrganizationEntityMapper.toDomain(entity);

            // Then - INACTIVE 상태 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(domain.getName()).isEqualTo(name);
            assertThat(domain.isDeleted()).isFalse();
        }

        /**
         * 정상: 삭제된 Organization Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 Organization Entity를 Domain으로 변환한다")
        void toDomain_DeletedOrganizationEntity_ReturnsDomain() {
            // Given - 삭제된 JPA Entity
            Long id = 3L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "IT";
            String name = "IT Department";
            OrganizationStatus status = OrganizationStatus.INACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(90);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(30);

            OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
                id, tenantId, orgCode, name, status, createdAt, updatedAt, true  // deleted = true
            );

            // When - Domain 변환
            Organization domain = OrganizationEntityMapper.toDomain(entity);

            // Then - deleted 플래그 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.isDeleted()).isTrue();
            assertThat(domain.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
        }

        /**
         * 예외: null Entity 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null Entity 전달 시 IllegalArgumentException이 발생한다")
        void toDomain_NullEntity_ThrowsException() {
            // When & Then - null 체크
            assertThatThrownBy(() -> OrganizationEntityMapper.toDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrganizationJpaEntity must not be null");
        }
    }

    @Nested
    @DisplayName("toEntity() - Domain → JPA Entity 변환")
    class ToEntityTests {

        /**
         * 정상: 기존 Organization Domain을 Entity로 변환 (ID 존재)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ID가 있는 Organization Domain을 Entity로 변환한다 (reconstitute)")
        void toEntity_ExistingOrganization_ReturnsEntity() {
            // Given - ID가 있는 Domain (reconstitute)
            Organization domain = OrganizationFixtures.salesOrganizationWithId(10L, DEFAULT_TENANT_ID);

            // When - Entity 변환
            OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(domain);

            // Then - 모든 필드 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getIdValue());
            assertThat(entity.getTenantId()).isEqualTo(domain.getTenantId());  // String FK
            assertThat(entity.getOrgCode()).isEqualTo(domain.getOrgCodeValue());
            assertThat(entity.getName()).isEqualTo(domain.getName());
            assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());
            assertThat(entity.isDeleted()).isEqualTo(domain.isDeleted());
        }

        /**
         * 정상: 신규 Organization Domain을 Entity로 변환 (ID 없음)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ID가 없는 Organization Domain을 Entity로 변환한다 (create)")
        void toEntity_NewOrganization_ReturnsEntity() {
            // Given - ID가 없는 Domain (create)
            Organization domain = OrganizationFixtures.salesOrganization(DEFAULT_TENANT_ID);

            // When - Entity 변환
            OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(domain);

            // Then - ID 제외한 필드 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();  // 신규 생성 시 ID는 null
            assertThat(entity.getTenantId()).isEqualTo(domain.getTenantId());  // String FK
            assertThat(entity.getOrgCode()).isEqualTo(domain.getOrgCodeValue());
            assertThat(entity.getName()).isEqualTo(domain.getName());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        }

        /**
         * 정상: INACTIVE 상태 Domain을 Entity로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("INACTIVE 상태 Organization Domain을 Entity로 변환한다")
        void toEntity_InactiveOrganization_ReturnsEntity() {
            // Given - INACTIVE 상태 Domain
            Organization domain = OrganizationFixtures.inactiveOrganizationWithId(20L, DEFAULT_TENANT_ID);

            // When - Entity 변환
            OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(domain);

            // Then - INACTIVE 상태 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(entity.isDeleted()).isFalse();
        }

        /**
         * 정상: 삭제된 Domain을 Entity로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 Organization Domain을 Entity로 변환한다")
        void toEntity_DeletedOrganization_ReturnsEntity() {
            // Given - deleted=true인 Domain
            Organization domain = OrganizationFixtures.deletedOrganizationWithId(30L, DEFAULT_TENANT_ID);

            // When - Entity 변환
            OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(domain);

            // Then - deleted 플래그 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
        }

        /**
         * 예외: null Domain 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null Domain 전달 시 IllegalArgumentException이 발생한다")
        void toEntity_NullDomain_ThrowsException() {
            // When & Then - null 체크
            assertThatThrownBy(() -> OrganizationEntityMapper.toEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization must not be null");
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성 검증")
    class RoundTripTests {

        /**
         * 양방향 변환: Domain → Entity → Domain 일관성 검증
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 일관되게 유지된다")
        void roundTrip_DomainToEntityToDomain_MaintainsConsistency() {
            // Given - 원본 Domain
            Organization originalDomain = OrganizationFixtures.salesOrganizationWithId(100L, DEFAULT_TENANT_ID);

            // When - Domain → Entity → Domain 변환
            OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(originalDomain);
            Organization convertedDomain = OrganizationEntityMapper.toDomain(entity);

            // Then - 모든 필드가 일관되게 유지됨
            assertThat(convertedDomain.getIdValue()).isEqualTo(originalDomain.getIdValue());
            assertThat(convertedDomain.getTenantId()).isEqualTo(originalDomain.getTenantId());  // String FK
            assertThat(convertedDomain.getOrgCodeValue()).isEqualTo(originalDomain.getOrgCodeValue());
            assertThat(convertedDomain.getName()).isEqualTo(originalDomain.getName());
            assertThat(convertedDomain.getStatus()).isEqualTo(originalDomain.getStatus());
            assertThat(convertedDomain.getCreatedAt()).isEqualTo(originalDomain.getCreatedAt());
            assertThat(convertedDomain.getUpdatedAt()).isEqualTo(originalDomain.getUpdatedAt());
            assertThat(convertedDomain.isDeleted()).isEqualTo(originalDomain.isDeleted());
        }

        /**
         * 양방향 변환: Entity → Domain → Entity 일관성 검증
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("Entity → Domain → Entity 변환 시 데이터가 일관되게 유지된다")
        void roundTrip_EntityToDomainToEntity_MaintainsConsistency() {
            // Given - 원본 Entity
            Long id = 200L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "FINANCE";
            String name = "Finance Department";
            OrganizationStatus status = OrganizationStatus.ACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(15);
            LocalDateTime updatedAt = LocalDateTime.now();

            OrganizationJpaEntity originalEntity = OrganizationJpaEntity.reconstitute(
                id, tenantId, orgCode, name, status, createdAt, updatedAt, false
            );

            // When - Entity → Domain → Entity 변환
            Organization domain = OrganizationEntityMapper.toDomain(originalEntity);
            OrganizationJpaEntity convertedEntity = OrganizationEntityMapper.toEntity(domain);

            // Then - 모든 필드가 일관되게 유지됨
            assertThat(convertedEntity.getId()).isEqualTo(originalEntity.getId());
            assertThat(convertedEntity.getTenantId()).isEqualTo(originalEntity.getTenantId());  // String FK
            assertThat(convertedEntity.getOrgCode()).isEqualTo(originalEntity.getOrgCode());
            assertThat(convertedEntity.getName()).isEqualTo(originalEntity.getName());
            assertThat(convertedEntity.getStatus()).isEqualTo(originalEntity.getStatus());
            assertThat(convertedEntity.getCreatedAt()).isEqualTo(originalEntity.getCreatedAt());
            assertThat(convertedEntity.getUpdatedAt()).isEqualTo(originalEntity.getUpdatedAt());
            assertThat(convertedEntity.isDeleted()).isEqualTo(originalEntity.isDeleted());
        }
    }

    @Nested
    @DisplayName("Utility 클래스 검증")
    class UtilityClassTests {

        /**
         * Utility 클래스: 인스턴스 생성 불가 검증
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("OrganizationEntityMapper는 인스턴스화할 수 없다")
        void utilityClass_CannotBeInstantiated() {
            // When & Then - Reflection으로 인스턴스 생성 시도 시 AssertionError 발생
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<OrganizationEntityMapper> constructor =
                    OrganizationEntityMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
            .hasCauseInstanceOf(AssertionError.class)
            .hasRootCauseMessage("Utility class should not be instantiated");
        }
    }
}
