package com.ryuqq.fileflow.adapter.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * PartUploadInfo JSON 직렬화/역직렬화용 DTO
 *
 * Domain VO와 분리하여 Jackson 의존성을 Adapter 레이어에만 격리
 *
 * @author sangwon-ryu
 */
public record PartUploadInfoDto(
        @JsonProperty("partNumber") int partNumber,
        @JsonProperty("presignedUrl") String presignedUrl,
        @JsonProperty("startByte") long startByte,
        @JsonProperty("endByte") long endByte,
        @JsonProperty("expiresAt") LocalDateTime expiresAt
) {
    @JsonCreator
    public PartUploadInfoDto {
        // Compact constructor for Jackson
    }
}
