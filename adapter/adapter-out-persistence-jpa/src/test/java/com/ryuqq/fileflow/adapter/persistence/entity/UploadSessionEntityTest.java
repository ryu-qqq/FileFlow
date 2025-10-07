package com.ryuqq.fileflow.adapter.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploadSessionEntity 단위 테스트
 *
 * 비즈니스 로직과 상태 전환을 검증합니다.
 *
 * @author sangwon-ryu
 */
class UploadSessionEntityTest {

    @Test
    @DisplayName("유효한 파라미터로 UploadSessionEntity를 생성할 수 있다")
    void createUploadSession() {
        // given
        String sessionId = "test-session-id";
        String tenantId = "b2c";
        String policyKey = "b2c:CONSUMER:REVIEW";
        String fileName = "test.jpg";
        String contentType = "image/jpeg";
        Long fileSize = 1024L;
        UploadSessionEntity.UploadStatus status = UploadSessionEntity.UploadStatus.INITIATED;
        String presignedUrl = "https://s3.amazonaws.com/bucket/key";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        // when
        UploadSessionEntity entity = UploadSessionEntity.of(
                sessionId,
                tenantId,
                policyKey,
                fileName,
                contentType,
                fileSize,
                status,
                presignedUrl,
                expiresAt
        );

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getSessionId()).isEqualTo(sessionId);
        assertThat(entity.getTenantId()).isEqualTo(tenantId);
        assertThat(entity.getPolicyKey()).isEqualTo(policyKey);
        assertThat(entity.getFileName()).isEqualTo(fileName);
        assertThat(entity.getContentType()).isEqualTo(contentType);
        assertThat(entity.getFileSize()).isEqualTo(fileSize);
        assertThat(entity.getStatus()).isEqualTo(status);
        assertThat(entity.getPresignedUrl()).isEqualTo(presignedUrl);
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("업로드를 시작하면 상태가 IN_PROGRESS로 변경된다")
    void startUpload() {
        // given
        UploadSessionEntity entity = createInitiatedSession();
        String s3Key = "tenant/b2c/uploads/test.jpg";

        // when
        entity.startUpload(s3Key);

        // then
        assertThat(entity.getStatus()).isEqualTo(UploadSessionEntity.UploadStatus.IN_PROGRESS);
        assertThat(entity.getS3Key()).isEqualTo(s3Key);
        assertThat(entity.getUploadStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("업로드를 완료하면 상태가 COMPLETED로 변경된다")
    void completeUpload() {
        // given
        UploadSessionEntity entity = createInitiatedSession();
        entity.startUpload("tenant/b2c/uploads/test.jpg");

        // when
        entity.completeUpload();

        // then
        assertThat(entity.getStatus()).isEqualTo(UploadSessionEntity.UploadStatus.COMPLETED);
        assertThat(entity.getUploadCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("업로드가 실패하면 상태가 FAILED로 변경된다")
    void failUpload() {
        // given
        UploadSessionEntity entity = createInitiatedSession();
        entity.startUpload("tenant/b2c/uploads/test.jpg");

        // when
        entity.failUpload();

        // then
        assertThat(entity.getStatus()).isEqualTo(UploadSessionEntity.UploadStatus.FAILED);
    }

    @Test
    @DisplayName("세션이 만료되면 상태가 EXPIRED로 변경된다")
    void expireSession() {
        // given
        UploadSessionEntity entity = createInitiatedSession();

        // when
        entity.expireSession();

        // then
        assertThat(entity.getStatus()).isEqualTo(UploadSessionEntity.UploadStatus.EXPIRED);
    }

    @Test
    @DisplayName("만료 시각이 지난 세션은 만료된 것으로 판단한다")
    void isExpired_expiredSession() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        UploadSessionEntity entity = UploadSessionEntity.of(
                "session-id",
                "b2c",
                "b2c:CONSUMER:REVIEW",
                "test.jpg",
                "image/jpeg",
                1024L,
                UploadSessionEntity.UploadStatus.INITIATED,
                "https://s3.amazonaws.com/bucket/key",
                pastTime
        );

        // when
        boolean expired = entity.isExpired();

        // then
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("만료 시각이 지나지 않은 세션은 만료되지 않은 것으로 판단한다")
    void isExpired_notExpiredSession() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(15);
        UploadSessionEntity entity = UploadSessionEntity.of(
                "session-id",
                "b2c",
                "b2c:CONSUMER:REVIEW",
                "test.jpg",
                "image/jpeg",
                1024L,
                UploadSessionEntity.UploadStatus.INITIATED,
                "https://s3.amazonaws.com/bucket/key",
                futureTime
        );

        // when
        boolean expired = entity.isExpired();

        // then
        assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태의 세션은 완료된 것으로 판단한다")
    void isCompleted_completedSession() {
        // given
        UploadSessionEntity entity = createInitiatedSession();
        entity.startUpload("tenant/b2c/uploads/test.jpg");
        entity.completeUpload();

        // when
        boolean completed = entity.isCompleted();

        // then
        assertThat(completed).isTrue();
    }

    @Test
    @DisplayName("COMPLETED가 아닌 상태의 세션은 완료되지 않은 것으로 판단한다")
    void isCompleted_notCompletedSession() {
        // given
        UploadSessionEntity entity = createInitiatedSession();

        // when
        boolean completed = entity.isCompleted();

        // then
        assertThat(completed).isFalse();
    }

    @Test
    @DisplayName("동일한 ID와 sessionId를 가진 엔티티는 같다고 판단한다")
    void equals_sameIdAndSessionId() {
        // given
        UploadSessionEntity entity1 = createInitiatedSession();
        UploadSessionEntity entity2 = createInitiatedSession();

        // when & then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("toString()은 엔티티의 주요 정보를 포함한다")
    void toStringTest() {
        // given
        UploadSessionEntity entity = createInitiatedSession();

        // when
        String result = entity.toString();

        // then
        assertThat(result).contains("sessionId='test-session-id'");
        assertThat(result).contains("tenantId='b2c'");
        assertThat(result).contains("status=INITIATED");
    }

    private UploadSessionEntity createInitiatedSession() {
        return UploadSessionEntity.of(
                "test-session-id",
                "b2c",
                "b2c:CONSUMER:REVIEW",
                "test.jpg",
                "image/jpeg",
                1024L,
                UploadSessionEntity.UploadStatus.INITIATED,
                "https://s3.amazonaws.com/bucket/key",
                LocalDateTime.now().plusMinutes(15)
        );
    }
}
