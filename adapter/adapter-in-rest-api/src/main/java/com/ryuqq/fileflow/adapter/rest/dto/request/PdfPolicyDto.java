package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * PDF Policy Request DTO
 *
 * PDF 파일 정책을 전달하는 요청 DTO
 *
 * @param maxFileSizeMB 최대 파일 크기 (MB)
 * @param maxPageCount 최대 페이지 개수
 * @author sangwon-ryu
 */
public record PdfPolicyDto(
        @NotNull(message = "maxFileSizeMB must not be null")
        @Min(value = 1, message = "maxFileSizeMB must be at least 1")
        Integer maxFileSizeMB,

        @NotNull(message = "maxPageCount must not be null")
        @Min(value = 1, message = "maxPageCount must be at least 1")
        Integer maxPageCount
) {
    /**
     * DTO를 Domain PdfPolicy로 변환합니다.
     *
     * @return PdfPolicy
     */
    public PdfPolicy toDomain() {
        return new PdfPolicy(
                maxFileSizeMB,
                maxPageCount
        );
    }
}
