package com.ryuqq.fileflow.adapter.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileAssetEntity 단위 테스트
 *
 * Entity 생성, soft delete, 동등성 비교 등을 검증합니다.
 *
 * @author sangwon-ryu
 */
class FileAssetEntityTest {

    @Test
    @DisplayName("유효한 파라미터로 FileAssetEntity를 생성할 수 있다")
    void createFileAsset() {
        // given
        String fileId = "test-file-id";
        String sessionId = "test-session-id";
        String tenantId = "b2c";
        String originalFileName = "test-image.jpg";
        String storedFileName = "20240101-abc123.jpg";
        String s3Bucket = "fileflow-assets";
        String s3Key = "b2c/uploads/2024/01/20240101-abc123.jpg";
        String s3Region = "ap-northeast-2";
        String cdnUrl = "https://cdn.example.com/20240101-abc123.jpg";
        Long fileSize = 2048L;
        String contentType = "image/jpeg";
        String fileExtension = "jpg";
        String checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        Boolean isPublic = false;

        // when
        FileAssetEntity entity = FileAssetEntity.of(
                fileId,
                sessionId,
                tenantId,
                originalFileName,
                storedFileName,
                s3Bucket,
                s3Key,
                s3Region,
                cdnUrl,
                fileSize,
                contentType,
                fileExtension,
                checksum,
                isPublic
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getFileId()).isEqualTo(fileId);
        assertThat(entity.getSessionId()).isEqualTo(sessionId);
        assertThat(entity.getTenantId()).isEqualTo(tenantId);
        assertThat(entity.getOriginalFileName()).isEqualTo(originalFileName);
        assertThat(entity.getStoredFileName()).isEqualTo(storedFileName);
        assertThat(entity.getS3Bucket()).isEqualTo(s3Bucket);
        assertThat(entity.getS3Key()).isEqualTo(s3Key);
        assertThat(entity.getS3Region()).isEqualTo(s3Region);
        assertThat(entity.getCdnUrl()).isEqualTo(cdnUrl);
        assertThat(entity.getFileSize()).isEqualTo(fileSize);
        assertThat(entity.getContentType()).isEqualTo(contentType);
        assertThat(entity.getFileExtension()).isEqualTo(fileExtension);
        assertThat(entity.getChecksum()).isEqualTo(checksum);
        assertThat(entity.getIsPublic()).isEqualTo(isPublic);
        assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("파일을 논리적으로 삭제할 수 있다 (soft delete)")
    void deleteFile() {
        // given
        FileAssetEntity entity = createTestFileAsset();
        assertThat(entity.isDeleted()).isFalse();

        // when
        entity.delete();

        // then
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 파일을 다시 삭제해도 deletedAt은 변경되지 않는다")
    void deleteAlreadyDeletedFile() {
        // given
        FileAssetEntity entity = createTestFileAsset();
        entity.delete();
        var firstDeletedAt = entity.getDeletedAt();

        // when
        entity.delete();

        // then
        assertThat(entity.getDeletedAt()).isEqualTo(firstDeletedAt);
    }

    @Test
    @DisplayName("삭제된 파일을 복원할 수 있다")
    void restoreDeletedFile() {
        // given
        FileAssetEntity entity = createTestFileAsset();
        entity.delete();
        assertThat(entity.isDeleted()).isTrue();

        // when
        entity.restore();

        // then
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("삭제되지 않은 파일은 isDeleted()가 false를 반환한다")
    void isDeleted_notDeletedFile() {
        // given
        FileAssetEntity entity = createTestFileAsset();

        // when
        boolean deleted = entity.isDeleted();

        // then
        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("삭제된 파일은 isDeleted()가 true를 반환한다")
    void isDeleted_deletedFile() {
        // given
        FileAssetEntity entity = createTestFileAsset();
        entity.delete();

        // when
        boolean deleted = entity.isDeleted();

        // then
        assertThat(deleted).isTrue();
    }

    @Test
    @DisplayName("동일한 fileId를 가진 엔티티는 같다고 판단한다")
    void equals_sameFileId() {
        // given
        FileAssetEntity entity1 = createTestFileAsset();
        FileAssetEntity entity2 = createTestFileAsset();

        // when & then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("다른 fileId를 가진 엔티티는 다르다고 판단한다")
    void equals_differentFileId() {
        // given
        FileAssetEntity entity1 = createTestFileAsset();
        FileAssetEntity entity2 = FileAssetEntity.of(
                "different-file-id",
                "test-session-id",
                "b2c",
                "test-image.jpg",
                "20240101-abc123.jpg",
                "fileflow-assets",
                "b2c/uploads/2024/01/20240101-abc123.jpg",
                "ap-northeast-2",
                "https://cdn.example.com/20240101-abc123.jpg",
                2048L,
                "image/jpeg",
                "jpg",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                false
        );

        // when & then
        assertThat(entity1).isNotEqualTo(entity2);
        assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("toString()은 엔티티의 주요 정보를 포함한다")
    void toStringTest() {
        // given
        FileAssetEntity entity = createTestFileAsset();

        // when
        String result = entity.toString();

        // then
        assertThat(result).contains("fileId='test-file-id'");
        assertThat(result).contains("sessionId='test-session-id'");
        assertThat(result).contains("tenantId='b2c'");
        assertThat(result).contains("originalFileName='test-image.jpg'");
        assertThat(result).contains("s3Key='b2c/uploads/2024/01/20240101-abc123.jpg'");
    }

    @Test
    @DisplayName("CDN URL이 null이어도 엔티티를 생성할 수 있다")
    void createFileAssetWithoutCdnUrl() {
        // given & when
        FileAssetEntity entity = FileAssetEntity.of(
                "test-file-id",
                "test-session-id",
                "b2c",
                "test-image.jpg",
                "20240101-abc123.jpg",
                "fileflow-assets",
                "b2c/uploads/2024/01/20240101-abc123.jpg",
                "ap-northeast-2",
                null, // CDN URL null
                2048L,
                "image/jpeg",
                "jpg",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                false
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getCdnUrl()).isNull();
    }

    @Test
    @DisplayName("체크섬이 null이어도 엔티티를 생성할 수 있다")
    void createFileAssetWithoutChecksum() {
        // given & when
        FileAssetEntity entity = FileAssetEntity.of(
                "test-file-id",
                "test-session-id",
                "b2c",
                "test-image.jpg",
                "20240101-abc123.jpg",
                "fileflow-assets",
                "b2c/uploads/2024/01/20240101-abc123.jpg",
                "ap-northeast-2",
                "https://cdn.example.com/20240101-abc123.jpg",
                2048L,
                "image/jpeg",
                "jpg",
                null, // 체크섬 null
                false
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getChecksum()).isNull();
    }

    @Test
    @DisplayName("공개 파일로 설정할 수 있다")
    void createPublicFileAsset() {
        // given & when
        FileAssetEntity entity = FileAssetEntity.of(
                "test-file-id",
                "test-session-id",
                "b2c",
                "test-image.jpg",
                "20240101-abc123.jpg",
                "fileflow-assets",
                "b2c/uploads/2024/01/20240101-abc123.jpg",
                "ap-northeast-2",
                "https://cdn.example.com/20240101-abc123.jpg",
                2048L,
                "image/jpeg",
                "jpg",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                true // 공개 파일
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getIsPublic()).isTrue();
    }

    private FileAssetEntity createTestFileAsset() {
        return FileAssetEntity.of(
                "test-file-id",
                "test-session-id",
                "b2c",
                "test-image.jpg",
                "20240101-abc123.jpg",
                "fileflow-assets",
                "b2c/uploads/2024/01/20240101-abc123.jpg",
                "ap-northeast-2",
                "https://cdn.example.com/20240101-abc123.jpg",
                2048L,
                "image/jpeg",
                "jpg",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                false
        );
    }
}
