package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Excel Policy Request DTO
 *
 * Excel 파일 정책을 전달하는 요청 DTO
 *
 * @param maxFileSizeMB 최대 파일 크기 (MB)
 * @param maxSheetCount 최대 시트 개수
 * @author sangwon-ryu
 */
public record ExcelPolicyDto(
        @NotNull(message = "maxFileSizeMB must not be null")
        @Min(value = 1, message = "maxFileSizeMB must be at least 1")
        Integer maxFileSizeMB,

        @NotNull(message = "maxSheetCount must not be null")
        @Min(value = 1, message = "maxSheetCount must be at least 1")
        Integer maxSheetCount
) {
    /**
     * DTO를 Domain ExcelPolicy로 변환합니다.
     *
     * @return ExcelPolicy
     */
    public ExcelPolicy toDomain() {
        return new ExcelPolicy(
                maxFileSizeMB,
                maxSheetCount
        );
    }
}
