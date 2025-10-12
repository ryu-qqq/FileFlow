package com.ryuqq.fileflow.adapter.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * MultipartUploadInfo JSON 직렬화/역직렬화용 DTO
 *
 * Domain VO와 분리하여 Jackson 의존성을 Adapter 레이어에만 격리
 *
 * @author sangwon-ryu
 */
public record MultipartUploadInfoDto(
        @JsonProperty("uploadId") String uploadId,
        @JsonProperty("uploadPath") String uploadPath,
        @JsonProperty("parts") List<PartUploadInfoDto> parts
) {
    @JsonCreator
    public MultipartUploadInfoDto {
        // Compact constructor for Jackson
    }
}
