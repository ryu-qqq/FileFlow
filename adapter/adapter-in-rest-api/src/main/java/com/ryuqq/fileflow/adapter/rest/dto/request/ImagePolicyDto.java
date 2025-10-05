package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Image Policy Request DTO
 *
 * 이미지 파일 정책을 전달하는 요청 DTO
 *
 * @param maxFileSizeMB 최대 파일 크기 (MB)
 * @param maxFileCount 최대 파일 개수
 * @param allowedFormats 허용된 포맷 목록
 * @param maxWidth 최대 너비 (픽셀)
 * @param maxHeight 최대 높이 (픽셀)
 * @author sangwon-ryu
 */
public record ImagePolicyDto(
        @NotNull(message = "maxFileSizeMB must not be null")
        @Min(value = 1, message = "maxFileSizeMB must be at least 1")
        Integer maxFileSizeMB,

        @NotNull(message = "maxFileCount must not be null")
        @Min(value = 1, message = "maxFileCount must be at least 1")
        Integer maxFileCount,

        @NotEmpty(message = "allowedFormats must not be empty")
        List<String> allowedFormats,

        @NotNull(message = "maxWidth must not be null")
        @Min(value = 1, message = "maxWidth must be at least 1")
        Integer maxWidth,

        @NotNull(message = "maxHeight must not be null")
        @Min(value = 1, message = "maxHeight must be at least 1")
        Integer maxHeight
) {
    /**
     * DTO를 Domain ImagePolicy로 변환합니다.
     *
     * @return ImagePolicy
     */
    public ImagePolicy toDomain() {
        return new ImagePolicy(
                maxFileSizeMB,
                maxFileCount,
                allowedFormats,
                Dimension.of(maxWidth, maxHeight)
        );
    }
}
