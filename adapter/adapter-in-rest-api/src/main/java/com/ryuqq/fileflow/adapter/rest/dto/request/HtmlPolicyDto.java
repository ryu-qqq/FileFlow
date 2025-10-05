package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * HTML Policy Request DTO
 *
 * HTML 파일 정책을 전달하는 요청 DTO
 *
 * @param maxFileSizeMB 최대 파일 크기 (MB)
 * @param maxImageCount 최대 이미지 개수
 * @param downloadExternalImages 외부 이미지 다운로드 허용 여부
 * @author sangwon-ryu
 */
public record HtmlPolicyDto(
        @NotNull(message = "maxFileSizeMB must not be null")
        @Min(value = 1, message = "maxFileSizeMB must be at least 1")
        Integer maxFileSizeMB,

        @NotNull(message = "maxImageCount must not be null")
        @Min(value = 1, message = "maxImageCount must be at least 1")
        Integer maxImageCount,

        @NotNull(message = "downloadExternalImages must not be null")
        Boolean downloadExternalImages
) {
    /**
     * DTO를 Domain HtmlPolicy로 변환합니다.
     *
     * @return HtmlPolicy
     */
    public HtmlPolicy toDomain() {
        return new HtmlPolicy(
                maxFileSizeMB,
                maxImageCount,
                downloadExternalImages
        );
    }
}
