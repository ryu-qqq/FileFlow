package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DownloadTaskJpaMapper 단위 테스트")
class DownloadTaskJpaMapperTest {

    private final DownloadTaskJpaMapper mapper = new DownloadTaskJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            DownloadTask domain = DownloadTaskFixture.aQueuedTask();

            // when
            DownloadTaskJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getSourceUrl()).isEqualTo(domain.sourceUrlValue());
            assertThat(entity.getBucket()).isEqualTo(domain.bucket());
            assertThat(entity.getS3Key()).isEqualTo(domain.s3Key());
            assertThat(entity.getAccessType()).isEqualTo(domain.accessType());
            assertThat(entity.getPurpose()).isEqualTo(domain.purpose());
            assertThat(entity.getSource()).isEqualTo(domain.source());
            assertThat(entity.getStatus()).isEqualTo(domain.status());
            assertThat(entity.getRetryCount()).isEqualTo(domain.retryCount());
            assertThat(entity.getMaxRetries()).isEqualTo(domain.maxRetries());
            assertThat(entity.getCallbackUrl()).isEqualTo(domain.callbackUrl());
        }

        @Test
        @DisplayName("콜백 URL이 없는 태스크를 변환합니다")
        void toEntity_withoutCallback_shouldMapNullCallbackUrl() {
            // given
            DownloadTask domain = DownloadTaskFixture.aTaskWithoutCallback();

            // when
            DownloadTaskJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getCallbackUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            DownloadTaskJpaEntity entity = DownloadTaskJpaEntityFixture.aQueuedEntity();

            // when
            DownloadTask domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.sourceUrlValue()).isEqualTo(entity.getSourceUrl());
            assertThat(domain.bucket()).isEqualTo(entity.getBucket());
            assertThat(domain.s3Key()).isEqualTo(entity.getS3Key());
            assertThat(domain.accessType()).isEqualTo(entity.getAccessType());
            assertThat(domain.status()).isEqualTo(entity.getStatus());
            assertThat(domain.retryCount()).isEqualTo(entity.getRetryCount());
            assertThat(domain.maxRetries()).isEqualTo(entity.getMaxRetries());
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존됩니다")
        void roundTrip_shouldPreserveAllData() {
            // given
            DownloadTask original = DownloadTaskFixture.aReconstitutedTask();

            // when
            DownloadTaskJpaEntity entity = mapper.toEntity(original);
            DownloadTask restored = mapper.toDomain(entity);

            // then
            assertThat(restored.idValue()).isEqualTo(original.idValue());
            assertThat(restored.sourceUrlValue()).isEqualTo(original.sourceUrlValue());
            assertThat(restored.status()).isEqualTo(original.status());
            assertThat(restored.retryCount()).isEqualTo(original.retryCount());
        }
    }
}
