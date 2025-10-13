package com.ryuqq.fileflow.adapter.persistence.entity;

import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploadSessionEntity 단위 테스트
 *
 * Entity 생성 및 재구성을 검증합니다.
 *
 * @author sangwon-ryu
 */
class UploadSessionEntityTest {

    @Test
    @DisplayName("유효한 파라미터로 UploadSessionEntity를 생성할 수 있다")
    void createUploadSession() {
        // given
        String sessionId = "test-session-id";
        String idempotencyKey = "idempotency-key-123";
        String tenantId = "b2c";
        String uploaderId = "user-123";
        String policyKey = "b2c:CONSUMER:REVIEW";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        Long fileSize = 1024L;
        String checksum = "SHA-256:abc123";
        UploadStatus status = UploadStatus.PENDING;
        String presignedUrl = "https://s3.amazonaws.com/bucket/key";
        String multipartUploadInfoJson = null;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        // when
        UploadSessionEntity entity = UploadSessionEntity.of(
                sessionId,
                idempotencyKey,
                tenantId,
                uploaderId,
                policyKey,
                fileName,
                contentType,
                fileSize,
                checksum,
                status,
                presignedUrl,
                multipartUploadInfoJson,
                expiresAt
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getSessionId()).isEqualTo(sessionId);
        assertThat(entity.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(entity.getTenantId()).isEqualTo(tenantId);
        assertThat(entity.getUploaderId()).isEqualTo(uploaderId);
        assertThat(entity.getPolicyKey()).isEqualTo(policyKey);
        assertThat(entity.getFileName()).isEqualTo(fileName);
        assertThat(entity.getContentType()).isEqualTo(contentType);
        assertThat(entity.getFileSize()).isEqualTo(fileSize);
        assertThat(entity.getChecksum()).isEqualTo(checksum);
        assertThat(entity.getStatus()).isEqualTo(status);
        assertThat(entity.getPresignedUrl()).isEqualTo(presignedUrl);
        assertThat(entity.getMultipartUploadInfoJson()).isNull();
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("idempotencyKey와 checksum이 null인 경우에도 Entity를 생성할 수 있다")
    void createUploadSessionWithNullableFields() {
        // given
        String sessionId = "test-session-id";
        String tenantId = "b2c";
        String uploaderId = "user-123";
        String policyKey = "b2c:CONSUMER:REVIEW";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        Long fileSize = 1024L;
        UploadStatus status = UploadStatus.PENDING;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        // when
        UploadSessionEntity entity = UploadSessionEntity.of(
                sessionId,
                null,  // idempotencyKey is nullable
                tenantId,
                uploaderId,
                policyKey,
                fileName,
                contentType,
                fileSize,
                null,  // checksum is nullable
                status,
                null,  // presignedUrl can be null
                null,  // multipartUploadInfoJson can be null
                expiresAt
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getIdempotencyKey()).isNull();
        assertThat(entity.getChecksum()).isNull();
        assertThat(entity.getPresignedUrl()).isNull();
        assertThat(entity.getMultipartUploadInfoJson()).isNull();
    }

    @Test
    @DisplayName("기존 ID를 유지하며 Entity를 재구성할 수 있다")
    void reconstituteWithId() {
        // given
        Long existingId = 100L;
        String sessionId = "test-session-id";
        String idempotencyKey = "idempotency-key-123";
        String tenantId = "b2c";
        String uploaderId = "user-123";
        String policyKey = "b2c:CONSUMER:REVIEW";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        Long fileSize = 1024L;
        String checksum = "SHA-256:abc123";
        UploadStatus status = UploadStatus.COMPLETED;
        String presignedUrl = "https://s3.amazonaws.com/bucket/key";
        String multipartUploadInfoJson = "{\"uploadId\":\"test\"}";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

        // when
        UploadSessionEntity entity = UploadSessionEntity.reconstituteWithId(
                existingId,
                sessionId,
                idempotencyKey,
                tenantId,
                uploaderId,
                policyKey,
                fileName,
                contentType,
                fileSize,
                checksum,
                status,
                presignedUrl,
                multipartUploadInfoJson,
                expiresAt,
                createdAt
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(existingId);
        assertThat(entity.getSessionId()).isEqualTo(sessionId);
        assertThat(entity.getStatus()).isEqualTo(status);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("multipart upload 정보를 포함하는 Entity를 생성할 수 있다")
    void createUploadSessionWithMultipartInfo() {
        // given
        String sessionId = "test-session-id";
        String uploaderId = "user-123";
        String multipartUploadInfoJson = "{\"uploadId\":\"multipart-123\",\"uploadPath\":\"tenant/uploads/file.mp4\",\"parts\":[]}";

        // when
        UploadSessionEntity entity = UploadSessionEntity.of(
                sessionId,
                null,
                "b2c",
                uploaderId,
                "b2c:CONSUMER:REVIEW",
                "large-video.mp4",
                "video/mp4",
                100_000_000L,  // 100MB
                "SHA-256:def456",
                UploadStatus.PENDING,
                null,
                multipartUploadInfoJson,
                LocalDateTime.now().plusHours(1)
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getMultipartUploadInfoJson()).isEqualTo(multipartUploadInfoJson);
        assertThat(entity.getMultipartUploadInfoJson()).contains("multipart-123");
    }

    @Test
    @DisplayName("동일한 sessionId를 가진 엔티티는 같다고 판단한다")
    void equals_sameSessionId() {
        // given
        UploadSessionEntity entity1 = createTestSession("same-session-id");
        UploadSessionEntity entity2 = createTestSession("same-session-id");

        // when & then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("다른 sessionId를 가진 엔티티는 다르다고 판단한다")
    void equals_differentSessionId() {
        // given
        UploadSessionEntity entity1 = createTestSession("session-id-1");
        UploadSessionEntity entity2 = createTestSession("session-id-2");

        // when & then
        assertThat(entity1).isNotEqualTo(entity2);
    }

    @Test
    @DisplayName("toString()은 엔티티의 주요 정보를 포함한다")
    void toStringTest() {
        // given
        UploadSessionEntity entity = createTestSession("test-session-id");

        // when
        String result = entity.toString();

        // then
        assertThat(result).contains("sessionId='test-session-id'");
        assertThat(result).contains("tenantId='b2c'");
        assertThat(result).contains("uploaderId='user-123'");
        assertThat(result).contains("status=PENDING");
    }

    private UploadSessionEntity createTestSession(String sessionId) {
        return UploadSessionEntity.of(
                sessionId,
                "idempotency-key-123",
                "b2c",
                "user-123",
                "b2c:CONSUMER:REVIEW",
                "test.jpg",
                "image/jpeg",
                1024L,
                "SHA-256:abc123",
                UploadStatus.PENDING,
                "https://s3.amazonaws.com/bucket/key",
                null,
                LocalDateTime.now().plusMinutes(15)
        );
    }
}
