package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.session.SingleUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SingleUploadSessionJpaMapper 단위 테스트")
class SingleUploadSessionJpaMapperTest {

    private final SingleUploadSessionJpaMapper mapper = new SingleUploadSessionJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            SingleUploadSession domain = SingleUploadSessionFixture.aCreatedSession();

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getS3Key()).isEqualTo(domain.s3Key());
            assertThat(entity.getBucket()).isEqualTo(domain.bucket());
            assertThat(entity.getAccessType()).isEqualTo(domain.accessType());
            assertThat(entity.getFileName()).isEqualTo(domain.fileName());
            assertThat(entity.getContentType()).isEqualTo(domain.contentType());
            assertThat(entity.getPresignedUrl()).isEqualTo(domain.presignedUrlValue());
            assertThat(entity.getPurpose()).isEqualTo(domain.purposeValue());
            assertThat(entity.getSource()).isEqualTo(domain.sourceValue());
            assertThat(entity.getStatus()).isEqualTo(domain.status());
            assertThat(entity.getExpiresAt()).isEqualTo(domain.expiresAt());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.createdAt());
            assertThat(entity.getUpdatedAt()).isEqualTo(domain.updatedAt());
        }

        @Test
        @DisplayName("완료된 세션을 JPA 엔티티로 변환합니다")
        void toEntity_completedSession_shouldMapStatus() {
            // given
            SingleUploadSession domain = SingleUploadSessionFixture.aCompletedSession();

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getStatus()).isEqualTo(domain.status());
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            SingleUploadSessionJpaEntity entity =
                    SingleUploadSessionJpaEntityFixture.aCreatedEntity();

            // when
            SingleUploadSession domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.s3Key()).isEqualTo(entity.getS3Key());
            assertThat(domain.bucket()).isEqualTo(entity.getBucket());
            assertThat(domain.accessType()).isEqualTo(entity.getAccessType());
            assertThat(domain.fileName()).isEqualTo(entity.getFileName());
            assertThat(domain.contentType()).isEqualTo(entity.getContentType());
            assertThat(domain.presignedUrlValue()).isEqualTo(entity.getPresignedUrl());
            assertThat(domain.purposeValue()).isEqualTo(entity.getPurpose());
            assertThat(domain.sourceValue()).isEqualTo(entity.getSource());
            assertThat(domain.status()).isEqualTo(entity.getStatus());
            assertThat(domain.expiresAt()).isEqualTo(entity.getExpiresAt());
            assertThat(domain.createdAt()).isEqualTo(entity.getCreatedAt());
            assertThat(domain.updatedAt()).isEqualTo(entity.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("도메인 -> 엔티티 -> 도메인 변환 시 데이터가 보존됩니다")
        void roundTrip_shouldPreserveAllData() {
            // given
            SingleUploadSession original = SingleUploadSessionFixture.aCreatedSession();

            // when
            SingleUploadSessionJpaEntity entity = mapper.toEntity(original);
            SingleUploadSession restored = mapper.toDomain(entity);

            // then
            assertThat(restored.idValue()).isEqualTo(original.idValue());
            assertThat(restored.s3Key()).isEqualTo(original.s3Key());
            assertThat(restored.bucket()).isEqualTo(original.bucket());
            assertThat(restored.accessType()).isEqualTo(original.accessType());
            assertThat(restored.status()).isEqualTo(original.status());
            assertThat(restored.expiresAt()).isEqualTo(original.expiresAt());
        }
    }
}
