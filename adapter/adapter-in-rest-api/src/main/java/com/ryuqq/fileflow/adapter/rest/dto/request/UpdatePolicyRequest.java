package com.ryuqq.fileflow.adapter.rest.dto.request;

import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UpdateUploadPolicyCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Update Policy Request DTO
 *
 * 정책 업데이트를 위한 요청 DTO
 *
 * @param imagePolicy 이미지 정책 (nullable)
 * @param htmlPolicy HTML 정책 (nullable)
 * @param excelPolicy Excel 정책 (nullable)
 * @param pdfPolicy PDF 정책 (nullable)
 * @param changedBy 변경자
 * @author sangwon-ryu
 */
public record UpdatePolicyRequest(
        @Valid
        ImagePolicyDto imagePolicy,

        @Valid
        HtmlPolicyDto htmlPolicy,

        @Valid
        ExcelPolicyDto excelPolicy,

        @Valid
        PdfPolicyDto pdfPolicy,

        @NotBlank(message = "changedBy must not be blank")
        String changedBy
) {
    /**
     * DTO를 Application Command로 변환합니다.
     *
     * @param policyKeyDto 정책 키 DTO
     * @return UpdateUploadPolicyCommand
     */
    public UpdateUploadPolicyCommand toCommand(PolicyKeyDto policyKeyDto) {
        return new UpdateUploadPolicyCommand(
                policyKeyDto,
                imagePolicy != null ? imagePolicy.toDomain() : null,
                htmlPolicy != null ? htmlPolicy.toDomain() : null,
                excelPolicy != null ? excelPolicy.toDomain() : null,
                pdfPolicy != null ? pdfPolicy.toDomain() : null,
                changedBy
        );
    }
}
