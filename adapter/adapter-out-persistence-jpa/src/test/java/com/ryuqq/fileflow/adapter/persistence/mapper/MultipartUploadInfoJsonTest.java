package com.ryuqq.fileflow.adapter.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MultipartUploadInfo JSON 직렬화/역직렬화 테스트
 */
class MultipartUploadInfoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // Java 8 date/time module 등록

    @Test
    void multipartUploadInfo_JSON_직렬화_및_역직렬화_테스트() throws Exception {
        // Given: MultipartUploadInfo 생성
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

        MultipartUploadInfo original = MultipartUploadInfo.of(
                "test-upload-id-123",
                "tenant-id/uploads/2025/10/13/video.mp4",
                parts
        );

        System.out.println("Original: " + original);
        System.out.println("Parts count: " + original.totalParts());

        // When: JSON으로 직렬화
        String json = objectMapper.writeValueAsString(original);
        System.out.println("\nSerialized JSON:");
        System.out.println(json);

        // Then: JSON에서 역직렬화
        MultipartUploadInfo deserialized = objectMapper.readValue(json, MultipartUploadInfo.class);
        System.out.println("\nDeserialized: " + deserialized);
        System.out.println("Parts count: " + deserialized.totalParts());

        // Verify
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.uploadId()).isEqualTo(original.uploadId());
        assertThat(deserialized.uploadPath()).isEqualTo(original.uploadPath());
        assertThat(deserialized.totalParts()).isEqualTo(original.totalParts());
        assertThat(deserialized.parts().get(0).partNumber()).isEqualTo(1);
        assertThat(deserialized.parts().get(1).partNumber()).isEqualTo(2);

        System.out.println("\n✅ JSON 직렬화/역직렬화 성공!");
    }
}
