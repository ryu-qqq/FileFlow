package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("AssetJpaMapper 단위 테스트")
class AssetJpaMapperTest {

    private final AssetJpaMapper mapper = new AssetJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            Asset domain = AssetFixture.anAsset();

            // when
            AssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getBucket()).isEqualTo(domain.bucket());
            assertThat(entity.getS3Key()).isEqualTo(domain.s3Key());
            assertThat(entity.getAccessType()).isEqualTo(domain.accessType());
            assertThat(entity.getFileName()).isEqualTo(domain.fileName());
            assertThat(entity.getFileSize()).isEqualTo(domain.fileSize());
            assertThat(entity.getContentType()).isEqualTo(domain.contentType());
            assertThat(entity.getEtag()).isEqualTo(domain.etag());
            assertThat(entity.getExtension()).isEqualTo(domain.extension());
            assertThat(entity.getOrigin()).isEqualTo(domain.origin());
            assertThat(entity.getOriginId()).isEqualTo(domain.originId());
            assertThat(entity.getPurpose()).isEqualTo(domain.purpose());
            assertThat(entity.getSource()).isEqualTo(domain.source());
            assertThat(entity.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("삭제된 도메인 객체의 deletedAt이 매핑됩니다")
        void toEntity_deletedAsset_shouldMapDeletedAt() {
            // given
            Asset domain = AssetFixture.aDeletedAsset();

            // when
            AssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            AssetJpaEntity entity = AssetJpaEntityFixture.anAssetEntity();

            // when
            Asset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.bucket()).isEqualTo(entity.getBucket());
            assertThat(domain.s3Key()).isEqualTo(entity.getS3Key());
            assertThat(domain.accessType()).isEqualTo(entity.getAccessType());
            assertThat(domain.fileName()).isEqualTo(entity.getFileName());
            assertThat(domain.fileSize()).isEqualTo(entity.getFileSize());
            assertThat(domain.contentType()).isEqualTo(entity.getContentType());
            assertThat(domain.etag()).isEqualTo(entity.getEtag());
            assertThat(domain.extension()).isEqualTo(entity.getExtension());
            assertThat(domain.origin()).isEqualTo(entity.getOrigin());
            assertThat(domain.originId()).isEqualTo(entity.getOriginId());
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존됩니다")
        void roundTrip_shouldPreserveAllData() {
            // given
            Asset original = AssetFixture.aReconstitutedAsset();

            // when
            AssetJpaEntity entity = mapper.toEntity(original);
            Asset restored = mapper.toDomain(entity);

            // then
            assertThat(restored.idValue()).isEqualTo(original.idValue());
            assertThat(restored.bucket()).isEqualTo(original.bucket());
            assertThat(restored.s3Key()).isEqualTo(original.s3Key());
            assertThat(restored.accessType()).isEqualTo(original.accessType());
            assertThat(restored.fileSize()).isEqualTo(original.fileSize());
            assertThat(restored.origin()).isEqualTo(original.origin());
        }
    }
}
