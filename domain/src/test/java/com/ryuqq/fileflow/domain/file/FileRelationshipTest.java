package com.ryuqq.fileflow.domain.file;

import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileRelationship Domain Entity 테스트")
class FileRelationshipTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 FileRelationship을 생성할 수 있다")
        void createFileRelationship() {
            // given
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            FileRelationshipType relationshipType = FileRelationshipType.THUMBNAIL;
            Map<String, Object> metadata = Map.of("width", 200, "height", 200);

            // when
            FileRelationship relationship = FileRelationship.create(
                    sourceFileId, targetFileId, relationshipType, metadata
            );

            // then
            assertThat(relationship.getId()).isNull(); // 생성 시점에는 null
            assertThat(relationship.getSourceFileId()).isEqualTo(sourceFileId);
            assertThat(relationship.getTargetFileId()).isEqualTo(targetFileId);
            assertThat(relationship.getRelationshipType()).isEqualTo(relationshipType);
            assertThat(relationship.getRelationshipMetadata()).containsEntry("width", 200);
            assertThat(relationship.getRelationshipMetadata()).containsEntry("height", 200);
            assertThat(relationship.getCreatedAt()).isNotNull();
            assertThat(relationship.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("메타데이터 없이 FileRelationship을 생성할 수 있다")
        void createFileRelationshipWithoutMetadata() {
            // given
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            Map<String, Object> emptyMetadata = Map.of();

            // when
            FileRelationship relationship = FileRelationship.create(
                    sourceFileId, targetFileId, FileRelationshipType.VERSION, emptyMetadata
            );

            // then
            assertThat(relationship.getRelationshipMetadata()).isEmpty();
            assertThat(relationship.hasMetadata()).isFalse();
        }

        @Test
        @DisplayName("기존 FileRelationship을 재구성할 수 있다")
        void reconstituteFileRelationship() {
            // given
            Long id = 1L;
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            FileRelationshipType relationshipType = FileRelationshipType.CONVERTED;
            Map<String, Object> metadata = Map.of("format", "webp", "quality", 85);
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

            // when
            FileRelationship relationship = FileRelationship.reconstitute(
                    id, sourceFileId, targetFileId, relationshipType, metadata, createdAt
            );

            // then
            assertThat(relationship.getId()).isEqualTo(id);
            assertThat(relationship.getSourceFileId()).isEqualTo(sourceFileId);
            assertThat(relationship.getTargetFileId()).isEqualTo(targetFileId);
            assertThat(relationship.getRelationshipType()).isEqualTo(relationshipType);
            assertThat(relationship.getRelationshipMetadata()).containsEntry("format", "webp");
            assertThat(relationship.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationTest {

        @Test
        @DisplayName("sourceFileId가 null이면 예외가 발생한다")
        void createWithNullSourceFileId() {
            assertThatThrownBy(() ->
                    FileRelationship.create(
                            null,
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            Map.of()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Source file ID cannot be null");
        }

        @Test
        @DisplayName("targetFileId가 null이면 예외가 발생한다")
        void createWithNullTargetFileId() {
            assertThatThrownBy(() ->
                    FileRelationship.create(
                            FileId.generate(),
                            null,
                            FileRelationshipType.THUMBNAIL,
                            Map.of()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Target file ID cannot be null");
        }

        @Test
        @DisplayName("sourceFileId와 targetFileId가 같으면 예외가 발생한다")
        void createWithSameSourceAndTarget() {
            FileId fileId = FileId.generate();

            assertThatThrownBy(() ->
                    FileRelationship.create(
                            fileId,
                            fileId,
                            FileRelationshipType.THUMBNAIL,
                            Map.of()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Source and target file IDs must be different");
        }

        @Test
        @DisplayName("relationshipType이 null이면 예외가 발생한다")
        void createWithNullRelationshipType() {
            assertThatThrownBy(() ->
                    FileRelationship.create(
                            FileId.generate(),
                            FileId.generate(),
                            null,
                            Map.of()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Relationship type cannot be null");
        }

        @Test
        @DisplayName("relationshipMetadata가 null이면 예외가 발생한다")
        void createWithNullMetadata() {
            assertThatThrownBy(() ->
                    FileRelationship.create(
                            FileId.generate(),
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            null
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Relationship metadata cannot be null");
        }

        @Test
        @DisplayName("재구성 시 id가 null이면 예외가 발생한다")
        void reconstituteWithNullId() {
            assertThatThrownBy(() ->
                    FileRelationship.reconstitute(
                            null,
                            FileId.generate(),
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            Map.of(),
                            LocalDateTime.now()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Id must be a positive number");
        }

        @Test
        @DisplayName("재구성 시 id가 0 이하면 예외가 발생한다")
        void reconstituteWithNonPositiveId() {
            assertThatThrownBy(() ->
                    FileRelationship.reconstitute(
                            0L,
                            FileId.generate(),
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            Map.of(),
                            LocalDateTime.now()
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Id must be a positive number");
        }

        @Test
        @DisplayName("재구성 시 createdAt이 null이면 예외가 발생한다")
        void reconstituteWithNullCreatedAt() {
            assertThatThrownBy(() ->
                    FileRelationship.reconstitute(
                            1L,
                            FileId.generate(),
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            Map.of(),
                            null
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CreatedAt cannot be null");
        }

        @Test
        @DisplayName("재구성 시 createdAt이 미래 시간이면 예외가 발생한다")
        void reconstituteWithFutureCreatedAt() {
            LocalDateTime future = LocalDateTime.now().plusHours(1);

            assertThatThrownBy(() ->
                    FileRelationship.reconstitute(
                            1L,
                            FileId.generate(),
                            FileId.generate(),
                            FileRelationshipType.THUMBNAIL,
                            Map.of(),
                            future
                    )
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CreatedAt cannot be in the future");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("썸네일 관계인지 확인할 수 있다")
        void isThumbnail() {
            // given
            FileRelationship thumbnail = createRelationship(FileRelationshipType.THUMBNAIL);
            FileRelationship optimized = createRelationship(FileRelationshipType.OPTIMIZED);

            // when & then
            assertThat(thumbnail.isThumbnail()).isTrue();
            assertThat(optimized.isThumbnail()).isFalse();
        }

        @Test
        @DisplayName("변환 관계인지 확인할 수 있다")
        void isTransformation() {
            // given
            FileRelationship optimized = createRelationship(FileRelationshipType.OPTIMIZED);
            FileRelationship converted = createRelationship(FileRelationshipType.CONVERTED);
            FileRelationship thumbnail = createRelationship(FileRelationshipType.THUMBNAIL);

            // when & then
            assertThat(optimized.isTransformation()).isTrue();
            assertThat(converted.isTransformation()).isTrue();
            assertThat(thumbnail.isTransformation()).isFalse();
        }

        @Test
        @DisplayName("파생 관계인지 확인할 수 있다")
        void isDerivative() {
            // given
            FileRelationship derivative = createRelationship(FileRelationshipType.DERIVATIVE);
            FileRelationship version = createRelationship(FileRelationshipType.VERSION);

            // when & then
            assertThat(derivative.isDerivative()).isTrue();
            assertThat(version.isDerivative()).isFalse();
        }

        @Test
        @DisplayName("버전 관계인지 확인할 수 있다")
        void isVersion() {
            // given
            FileRelationship version = createRelationship(FileRelationshipType.VERSION);
            FileRelationship derivative = createRelationship(FileRelationshipType.DERIVATIVE);

            // when & then
            assertThat(version.isVersion()).isTrue();
            assertThat(derivative.isVersion()).isFalse();
        }

        @Test
        @DisplayName("특정 파일 ID가 관계에 포함되는지 확인할 수 있다")
        void involves() {
            // given
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            FileId otherFileId = FileId.generate();
            FileRelationship relationship = createRelationship(sourceFileId, targetFileId);

            // when & then
            assertThat(relationship.involves(sourceFileId)).isTrue();
            assertThat(relationship.involves(targetFileId)).isTrue();
            assertThat(relationship.involves(otherFileId)).isFalse();
        }

        @Test
        @DisplayName("원본 파일인지 확인할 수 있다")
        void isSourceFile() {
            // given
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            FileRelationship relationship = createRelationship(sourceFileId, targetFileId);

            // when & then
            assertThat(relationship.isSourceFile(sourceFileId)).isTrue();
            assertThat(relationship.isSourceFile(targetFileId)).isFalse();
        }

        @Test
        @DisplayName("대상 파일인지 확인할 수 있다")
        void isTargetFile() {
            // given
            FileId sourceFileId = FileId.generate();
            FileId targetFileId = FileId.generate();
            FileRelationship relationship = createRelationship(sourceFileId, targetFileId);

            // when & then
            assertThat(relationship.isTargetFile(targetFileId)).isTrue();
            assertThat(relationship.isTargetFile(sourceFileId)).isFalse();
        }

        @Test
        @DisplayName("메타데이터 값을 가져올 수 있다")
        void getMetadataValue() {
            // given
            Map<String, Object> metadata = Map.of("width", 200, "height", 200);
            FileRelationship relationship = createRelationshipWithMetadata(metadata);

            // when & then
            assertThat(relationship.getMetadataValue("width")).isEqualTo(200);
            assertThat(relationship.getMetadataValue("height")).isEqualTo(200);
            assertThat(relationship.getMetadataValue("nonexistent")).isNull();
        }

        @Test
        @DisplayName("메타데이터 존재 여부를 확인할 수 있다")
        void hasMetadata() {
            // given
            FileRelationship withMetadata = createRelationshipWithMetadata(Map.of("key", "value"));
            FileRelationship withoutMetadata = createRelationshipWithMetadata(Map.of());

            // when & then
            assertThat(withMetadata.hasMetadata()).isTrue();
            assertThat(withoutMetadata.hasMetadata()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 id를 가진 FileRelationship은 동일하다")
        void equalsBySameId() {
            // given
            Long id = 1L;
            FileRelationship relationship1 = FileRelationship.reconstitute(
                    id,
                    FileId.generate(),
                    FileId.generate(),
                    FileRelationshipType.THUMBNAIL,
                    Map.of("width", 100),
                    LocalDateTime.now()
            );
            FileRelationship relationship2 = FileRelationship.reconstitute(
                    id,
                    FileId.generate(),
                    FileId.generate(),
                    FileRelationshipType.CONVERTED,
                    Map.of("format", "webp"),
                    LocalDateTime.now()
            );

            // when & then
            assertThat(relationship1).isEqualTo(relationship2);
            assertThat(relationship1.hashCode()).isEqualTo(relationship2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 FileRelationship은 다르다")
        void notEqualsByDifferentId() {
            // given
            FileRelationship relationship1 = FileRelationship.reconstitute(
                    1L,
                    FileId.generate(),
                    FileId.generate(),
                    FileRelationshipType.THUMBNAIL,
                    Map.of(),
                    LocalDateTime.now()
            );
            FileRelationship relationship2 = FileRelationship.reconstitute(
                    2L,
                    FileId.generate(),
                    FileId.generate(),
                    FileRelationshipType.THUMBNAIL,
                    Map.of(),
                    LocalDateTime.now()
            );

            // when & then
            assertThat(relationship1).isNotEqualTo(relationship2);
        }

        @Test
        @DisplayName("id가 null인 새로 생성된 FileRelationship은 다르다")
        void notEqualsByNullId() {
            // given
            FileRelationship relationship1 = createRelationship(FileRelationshipType.THUMBNAIL);
            FileRelationship relationship2 = createRelationship(FileRelationshipType.THUMBNAIL);

            // when & then
            assertThat(relationship1).isNotEqualTo(relationship2);
        }
    }

    // ========== Test Helper Methods ==========

    private FileRelationship createRelationship(FileRelationshipType type) {
        return FileRelationship.create(
                FileId.generate(),
                FileId.generate(),
                type,
                new HashMap<>()
        );
    }

    private FileRelationship createRelationship(FileId sourceFileId, FileId targetFileId) {
        return FileRelationship.create(
                sourceFileId,
                targetFileId,
                FileRelationshipType.THUMBNAIL,
                new HashMap<>()
        );
    }

    private FileRelationship createRelationshipWithMetadata(Map<String, Object> metadata) {
        return FileRelationship.create(
                FileId.generate(),
                FileId.generate(),
                FileRelationshipType.THUMBNAIL,
                metadata
        );
    }
}
