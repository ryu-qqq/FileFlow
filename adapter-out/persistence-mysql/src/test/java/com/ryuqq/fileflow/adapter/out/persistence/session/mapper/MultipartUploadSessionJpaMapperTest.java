package com.ryuqq.fileflow.adapter.out.persistence.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.session.CompletedPartJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.MultipartUploadSessionJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("MultipartUploadSessionJpaMapper 단위 테스트")
class MultipartUploadSessionJpaMapperTest {

    private final MultipartUploadSessionJpaMapper mapper = new MultipartUploadSessionJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            MultipartUploadSession domain = MultipartUploadSessionFixture.anInitiatedSession();

            // when
            MultipartUploadSessionJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getS3Key()).isEqualTo(domain.s3Key());
            assertThat(entity.getBucket()).isEqualTo(domain.bucket());
            assertThat(entity.getAccessType()).isEqualTo(domain.accessType());
            assertThat(entity.getFileName()).isEqualTo(domain.fileName());
            assertThat(entity.getContentType()).isEqualTo(domain.contentType());
            assertThat(entity.getUploadId()).isEqualTo(domain.uploadId());
            assertThat(entity.getPartSize()).isEqualTo(domain.partSize());
            assertThat(entity.getPurpose()).isEqualTo(domain.purposeValue());
            assertThat(entity.getSource()).isEqualTo(domain.sourceValue());
            assertThat(entity.getStatus()).isEqualTo(domain.status());
            assertThat(entity.getExpiresAt()).isEqualTo(domain.expiresAt());
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티와 파트 목록을 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            List<CompletedPartJpaEntity> parts = List.of();

            // when
            MultipartUploadSession domain = mapper.toDomain(entity, parts);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.uploadId()).isEqualTo(entity.getUploadId());
            assertThat(domain.partSize()).isEqualTo(entity.getPartSize());
            assertThat(domain.status()).isEqualTo(entity.getStatus());
            assertThat(domain.completedParts()).isEmpty();
        }

        @Test
        @DisplayName("완료된 파트가 있는 경우 함께 변환합니다")
        void toDomain_withParts_shouldIncludeCompletedParts() {
            // given
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntityFixture.anInitiatedEntity();
            List<CompletedPartJpaEntity> parts =
                    List.of(
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(entity.getId(), 1),
                            CompletedPartJpaEntityFixture.aCompletedPartEntity(entity.getId(), 2));

            // when
            MultipartUploadSession domain = mapper.toDomain(entity, parts);

            // then
            assertThat(domain.completedParts()).hasSize(2);
            assertThat(domain.completedParts().get(0).partNumber()).isEqualTo(1);
            assertThat(domain.completedParts().get(1).partNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toPartEntities 메서드 테스트")
    class ToPartEntitiesTest {

        @Test
        @DisplayName("도메인 파트 목록을 JPA 엔티티 목록으로 변환합니다")
        void toPartEntities_shouldMapAllParts() {
            // given
            String sessionId = "multipart-session-001";
            List<CompletedPart> parts =
                    List.of(
                            CompletedPart.of(
                                    1,
                                    "etag-1",
                                    5_242_880L,
                                    MultipartUploadSessionJpaEntityFixture.defaultNow()),
                            CompletedPart.of(
                                    2,
                                    "etag-2",
                                    5_242_880L,
                                    MultipartUploadSessionJpaEntityFixture.defaultNow()));

            // when
            List<CompletedPartJpaEntity> entities = mapper.toPartEntities(sessionId, parts);

            // then
            assertThat(entities).hasSize(2);
            assertThat(entities.get(0).getSessionId()).isEqualTo(sessionId);
            assertThat(entities.get(0).getPartNumber()).isEqualTo(1);
            assertThat(entities.get(0).getEtag()).isEqualTo("etag-1");
            assertThat(entities.get(1).getPartNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("빈 파트 목록일 때 빈 엔티티 목록을 반환합니다")
        void toPartEntities_emptyParts_shouldReturnEmptyList() {
            // given
            String sessionId = "multipart-session-001";

            // when
            List<CompletedPartJpaEntity> entities = mapper.toPartEntities(sessionId, List.of());

            // then
            assertThat(entities).isEmpty();
        }
    }
}
