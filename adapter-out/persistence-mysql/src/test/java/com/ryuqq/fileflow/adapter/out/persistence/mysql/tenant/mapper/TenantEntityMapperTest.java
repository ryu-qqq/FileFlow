package com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantEntityMapperTest - TenantEntityMapper 단위 테스트
 *
 * <p>TenantEntityMapper의 Domain ↔ JPA Entity 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code toDomain()}: JPA Entity → Domain 변환 검증</li>
 *   <li>✅ {@code toEntity()}: Domain → JPA Entity 변환 검증</li>
 *   <li>✅ null 파라미터 예외 처리 검증</li>
 *   <li>✅ Value Object 변환 검증 (TenantId, TenantName)</li>
 *   <li>✅ Status Enum 변환 검증</li>
 *   <li>✅ 양방향 변환 일관성 검증 (Domain → Entity → Domain)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("TenantEntityMapper 테스트")
class TenantEntityMapperTest {

    @Nested
    @DisplayName("toDomain() - JPA Entity → Domain 변환")
    class ToDomainTests {

        /**
         * 정상: ACTIVE 상태 Tenant Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ACTIVE 상태 Tenant Entity를 Domain으로 변환한다")
        void toDomain_ActiveTenantEntity_ReturnsDomain() {
            // Given - ACTIVE 상태 JPA Entity
            String id = "tenant-uuid-123";
            String name = "Test Company";
            TenantStatus status = TenantStatus.ACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
            LocalDateTime updatedAt = LocalDateTime.now();

            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                id, name, status, createdAt, updatedAt, false
            );

            // When - Domain 변환
            Tenant domain = TenantEntityMapper.toDomain(entity);

            // Then - 모든 필드 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.getIdValue()).isEqualTo(id);
            assertThat(domain.getNameValue()).isEqualTo(name);
            assertThat(domain.getStatus()).isEqualTo(status);
            assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
            assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(domain.isDeleted()).isFalse();
        }

        /**
         * 정상: SUSPENDED 상태 Tenant Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("SUSPENDED 상태 Tenant Entity를 Domain으로 변환한다")
        void toDomain_SuspendedTenantEntity_ReturnsDomain() {
            // Given - SUSPENDED 상태 JPA Entity
            String id = "tenant-uuid-456";
            String name = "Suspended Company";
            TenantStatus status = TenantStatus.SUSPENDED;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(60);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                id, name, status, createdAt, updatedAt, false
            );

            // When - Domain 변환
            Tenant domain = TenantEntityMapper.toDomain(entity);

            // Then - SUSPENDED 상태 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(domain.getNameValue()).isEqualTo(name);
            assertThat(domain.isDeleted()).isFalse();
        }

        /**
         * 정상: 삭제된 Tenant Entity를 Domain으로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 Tenant Entity를 Domain으로 변환한다")
        void toDomain_DeletedTenantEntity_ReturnsDomain() {
            // Given - 삭제된 JPA Entity
            String id = "tenant-uuid-789";
            String name = "Deleted Company";
            TenantStatus status = TenantStatus.SUSPENDED;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(90);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(30);

            TenantJpaEntity entity = TenantJpaEntity.reconstitute(
                id, name, status, createdAt, updatedAt, true  // deleted = true
            );

            // When - Domain 변환
            Tenant domain = TenantEntityMapper.toDomain(entity);

            // Then - deleted 플래그 정확히 매핑됨
            assertThat(domain).isNotNull();
            assertThat(domain.isDeleted()).isTrue();
            assertThat(domain.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
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
            assertThatThrownBy(() -> TenantEntityMapper.toDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantJpaEntity must not be null");
        }
    }

    @Nested
    @DisplayName("toEntity() - Domain → JPA Entity 변환")
    class ToEntityTests {

        /**
         * 정상: 기존 Tenant Domain을 Entity로 변환 (ID 존재)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ID가 있는 Tenant Domain을 Entity로 변환한다 (reconstitute)")
        void toEntity_ExistingTenant_ReturnsEntity() {
            // Given - ID가 있는 Domain (reconstitute)
            Tenant domain = TenantFixtures.activeTenantWithId("tenant-uuid-abc");

            // When - Entity 변환
            TenantJpaEntity entity = TenantEntityMapper.toEntity(domain);

            // Then - 모든 필드 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getIdValue());
            assertThat(entity.getName()).isEqualTo(domain.getNameValue());
            assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(domain.getUpdatedAt());
            assertThat(entity.isDeleted()).isEqualTo(domain.isDeleted());
        }

        /**
         * 정상: 신규 Tenant Domain을 Entity로 변환 (ID 없음)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ID가 없는 Tenant Domain을 Entity로 변환한다 (create)")
        void toEntity_NewTenant_ReturnsEntity() {
            // Given - ID가 없는 Domain (create)
            Tenant domain = TenantFixtures.activeTenant();

            // When - Entity 변환
            TenantJpaEntity entity = TenantEntityMapper.toEntity(domain);

            // Then - ID 제외한 필드 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();  // 신규 생성 시 ID는 null
            assertThat(entity.getName()).isEqualTo(domain.getNameValue());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        }

        /**
         * 정상: SUSPENDED 상태 Domain을 Entity로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("SUSPENDED 상태 Tenant Domain을 Entity로 변환한다")
        void toEntity_SuspendedTenant_ReturnsEntity() {
            // Given - SUSPENDED 상태 Domain
            Tenant domain = TenantFixtures.suspendedTenantWithId("tenant-uuid-def");

            // When - Entity 변환
            TenantJpaEntity entity = TenantEntityMapper.toEntity(domain);

            // Then - SUSPENDED 상태 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(entity.isDeleted()).isFalse();
        }

        /**
         * 정상: 삭제된 Domain을 Entity로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 Tenant Domain을 Entity로 변환한다")
        void toEntity_DeletedTenant_ReturnsEntity() {
            // Given - deleted=true인 Domain
            Tenant domain = TenantFixtures.deletedTenantWithId("tenant-uuid-ghi");

            // When - Entity 변환
            TenantJpaEntity entity = TenantEntityMapper.toEntity(domain);

            // Then - deleted 플래그 정확히 매핑됨
            assertThat(entity).isNotNull();
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
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
            assertThatThrownBy(() -> TenantEntityMapper.toEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant must not be null");
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
            Tenant originalDomain = TenantFixtures.activeTenantWithId("tenant-uuid-original");

            // When - Domain → Entity → Domain 변환
            TenantJpaEntity entity = TenantEntityMapper.toEntity(originalDomain);
            Tenant convertedDomain = TenantEntityMapper.toDomain(entity);

            // Then - 모든 필드가 일관되게 유지됨
            assertThat(convertedDomain.getIdValue()).isEqualTo(originalDomain.getIdValue());
            assertThat(convertedDomain.getNameValue()).isEqualTo(originalDomain.getNameValue());
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
            String id = "tenant-uuid-round";
            String name = "Round Trip Test Company";
            TenantStatus status = TenantStatus.ACTIVE;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(15);
            LocalDateTime updatedAt = LocalDateTime.now();

            TenantJpaEntity originalEntity = TenantJpaEntity.reconstitute(
                id, name, status, createdAt, updatedAt, false
            );

            // When - Entity → Domain → Entity 변환
            Tenant domain = TenantEntityMapper.toDomain(originalEntity);
            TenantJpaEntity convertedEntity = TenantEntityMapper.toEntity(domain);

            // Then - 모든 필드가 일관되게 유지됨
            assertThat(convertedEntity.getId()).isEqualTo(originalEntity.getId());
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
        @DisplayName("TenantEntityMapper는 인스턴스화할 수 없다")
        void utilityClass_CannotBeInstantiated() {
            // When & Then - Reflection으로 인스턴스 생성 시도 시 AssertionError 발생
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<TenantEntityMapper> constructor =
                    TenantEntityMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
            .hasCauseInstanceOf(AssertionError.class)
            .hasRootCauseMessage("Utility class should not be instantiated");
        }
    }
}
