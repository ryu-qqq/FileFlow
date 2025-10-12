package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.domain.policy.vo.VideoPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Video Policy Request DTO
 *
 * 비디오 파일 정책을 전달하는 요청 DTO
 *
 * @param maxFileSizeMB 최대 파일 크기 (MB)
 * @param maxFileCount 최대 파일 개수
 * @param allowedFormats 허용된 포맷 목록
 * @param maxDurationSeconds 최대 재생 시간 (초)
 * @author sangwon-ryu
 */
public record VideoPolicyDto(
        @NotNull(message = "maxFileSizeMB must not be null")
        @Min(value = 1, message = "maxFileSizeMB must be at least 1")
        Integer maxFileSizeMB,

        @NotNull(message = "maxFileCount must not be null")
        @Min(value = 1, message = "maxFileCount must be at least 1")
        Integer maxFileCount,

        @NotEmpty(message = "allowedFormats must not be empty")
        List<String> allowedFormats,

        @NotNull(message = "maxDurationSeconds must not be null")
        @Min(value = 1, message = "maxDurationSeconds must be at least 1")
        Integer maxDurationSeconds
) {
    /**
     * DTO를 Domain VideoPolicy로 변환합니다.
     *
     * @return VideoPolicy
     */
    public VideoPolicy toDomain() {
        return new VideoPolicy(
                maxFileSizeMB,
                maxFileCount,
                allowedFormats,
                maxDurationSeconds
        );
    }
}
