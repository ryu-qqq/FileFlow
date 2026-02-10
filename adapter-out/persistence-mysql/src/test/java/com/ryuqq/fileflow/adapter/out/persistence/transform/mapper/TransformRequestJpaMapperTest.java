package com.ryuqq.fileflow.adapter.out.persistence.transform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformRequestJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformRequestJpaMapper 단위 테스트")
class TransformRequestJpaMapperTest {

    private final TransformRequestJpaMapper mapper = new TransformRequestJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("리사이즈 요청을 JPA 엔티티로 변환합니다")
        void toEntity_resizeRequest_shouldMapAllFields() {
            // given
            TransformRequest domain = TransformRequestFixture.aResizeRequest();

            // when
            TransformRequestJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getSourceAssetId()).isEqualTo(domain.sourceAssetIdValue());
            assertThat(entity.getSourceContentType()).isEqualTo(domain.sourceContentType());
            assertThat(entity.getType()).isEqualTo(domain.type());
            assertThat(entity.getStatus()).isEqualTo(domain.status());
            assertThat(entity.getWidth()).isEqualTo(domain.params().width());
            assertThat(entity.getHeight()).isEqualTo(domain.params().height());
            assertThat(entity.isMaintainAspectRatio()).isTrue();
            assertThat(entity.getTargetFormat()).isNull();
            assertThat(entity.getQuality()).isNull();
            assertThat(entity.getResultAssetId()).isNull();
        }

        @Test
        @DisplayName("변환 요청을 JPA 엔티티로 변환합니다")
        void toEntity_convertRequest_shouldMapTargetFormat() {
            // given
            TransformRequest domain = TransformRequestFixture.aConvertRequest();

            // when
            TransformRequestJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getTargetFormat()).isEqualTo("webp");
            assertThat(entity.getWidth()).isNull();
            assertThat(entity.getHeight()).isNull();
        }

        @Test
        @DisplayName("완료된 요청의 resultAssetId가 매핑됩니다")
        void toEntity_completedRequest_shouldMapResultAssetId() {
            // given
            TransformRequest domain = TransformRequestFixture.aCompletedRequest();

            // when
            TransformRequestJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getResultAssetId()).isNotNull();
            assertThat(entity.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            TransformRequestJpaEntity entity =
                    TransformRequestJpaEntityFixture.aQueuedResizeEntity();

            // when
            TransformRequest domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.sourceAssetIdValue()).isEqualTo(entity.getSourceAssetId());
            assertThat(domain.sourceContentType()).isEqualTo(entity.getSourceContentType());
            assertThat(domain.type()).isEqualTo(entity.getType());
            assertThat(domain.status()).isEqualTo(entity.getStatus());
            assertThat(domain.params().width()).isEqualTo(entity.getWidth());
            assertThat(domain.params().height()).isEqualTo(entity.getHeight());
            assertThat(domain.params().maintainAspectRatio()).isTrue();
        }

        @Test
        @DisplayName("완료된 엔티티의 resultAssetId가 도메인에 매핑됩니다")
        void toDomain_completedEntity_shouldMapResultAssetId() {
            // given
            TransformRequestJpaEntity entity = TransformRequestJpaEntityFixture.aCompletedEntity();

            // when
            TransformRequest domain = mapper.toDomain(entity);

            // then
            assertThat(domain.resultAssetIdValue()).isEqualTo("result-001");
            assertThat(domain.completedAt()).isNotNull();
        }

        @Test
        @DisplayName("resultAssetId가 null인 엔티티를 변환합니다")
        void toDomain_nullResultAssetId_shouldMapAsNull() {
            // given
            TransformRequestJpaEntity entity =
                    TransformRequestJpaEntityFixture.aQueuedResizeEntity();

            // when
            TransformRequest domain = mapper.toDomain(entity);

            // then
            assertThat(domain.resultAssetIdValue()).isNull();
        }
    }
}
