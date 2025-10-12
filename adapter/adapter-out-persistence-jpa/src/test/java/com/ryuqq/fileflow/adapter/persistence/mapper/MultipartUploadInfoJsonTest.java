package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.persistence.entity.UploadSessionEntity;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploadSessionMapper의 MultipartUploadInfo JSON 직렬화/역직렬화 테스트
 *
 * 이 테스트는 실제 애플리케이션 로직(UploadSessionMapper)에서 사용되는
 * Domain VO → DTO → JSON → DTO → Domain VO 변환 경로를 검증합니다.
 */
class MultipartUploadInfoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // Java 8 date/time module 등록

    private final UploadSessionMapper mapper = new UploadSessionMapper(objectMapper);

    @Test
    void uploadSessionMapper_MultipartUploadInfo_직렬화_및_역직렬화_테스트() {
        // Given: MultipartUploadInfo를 포함한 UploadSession 도메인 객체 생성
        List<PartUploadInfo> parts = List.of(
                PartUploadInfo.of(
                        1,
                        "https://s3.amazonaws.com/bucket/part1?signature=xxx",
                        0L,
                        5242879L, // 5MB - 1 byte
                        LocalDateTime.of(2025, 10, 13, 1, 0, 0)
                ),
                PartUploadInfo.of(
                        2,
                        "https://s3.amazonaws.com/bucket/part2?signature=yyy",
                        5242880L,
                        10485759L, // 5MB
                        LocalDateTime.of(2025, 10, 13, 1, 0, 0)
                )
        );

        MultipartUploadInfo multipartInfo = MultipartUploadInfo.of(
                "test-upload-id-123",
                "tenant-id/uploads/2025/10/13/video.mp4",
                parts
        );

        PolicyKey policyKey = PolicyKey.of("tenant-id", "SELLER", "PRODUCT");
        UploadRequest uploadRequest = UploadRequest.of(
                "test-video.mp4",
                FileType.VIDEO_MP4,
                104857600L, // 100MB
                "video/mp4",
                null,
                IdempotencyKey.of("test-idempotency-key")
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);

        UploadSession originalSession = UploadSession.reconstituteWithMultipart(
                "test-session-id",
                policyKey,
                uploadRequest,
                "uploader-123",
                "UPLOADING",
                now,
                expiresAt,
                multipartInfo
        );

        // When: Mapper를 통해 Domain → Entity 변환 (JSON 직렬화 포함)
        UploadSessionEntity entity = mapper.toEntity(originalSession, "tenant-id");

        // Then: Entity의 JSON 필드가 올바르게 생성되었는지 확인
        assertThat(entity).isNotNull();
        assertThat(entity.getMultipartUploadInfoJson()).isNotNull();
        assertThat(entity.getMultipartUploadInfoJson()).isNotEmpty();

        // Entity → Domain 변환 (JSON 역직렬화 포함)
        UploadSession deserializedSession = mapper.toDomain(entity);

        // Verify: 원본 MultipartUploadInfo와 역직렬화된 MultipartUploadInfo 비교
        assertThat(deserializedSession).isNotNull();
        assertThat(deserializedSession.getMultipartUploadInfo()).isPresent();

        MultipartUploadInfo deserializedMultipartInfo = deserializedSession.getMultipartUploadInfo().get();
        assertThat(deserializedMultipartInfo.uploadId()).isEqualTo(multipartInfo.uploadId());
        assertThat(deserializedMultipartInfo.uploadPath()).isEqualTo(multipartInfo.uploadPath());
        assertThat(deserializedMultipartInfo.totalParts()).isEqualTo(multipartInfo.totalParts());
        assertThat(deserializedMultipartInfo.parts()).hasSize(2);
        assertThat(deserializedMultipartInfo.parts().get(0).partNumber()).isEqualTo(1);
        assertThat(deserializedMultipartInfo.parts().get(1).partNumber()).isEqualTo(2);
        assertThat(deserializedMultipartInfo.parts().get(0).presignedUrl())
                .isEqualTo("https://s3.amazonaws.com/bucket/part1?signature=xxx");
    }
}
