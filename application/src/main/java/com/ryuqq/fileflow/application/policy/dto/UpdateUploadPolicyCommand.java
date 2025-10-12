package com.ryuqq.fileflow.application.policy.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import com.ryuqq.fileflow.domain.policy.vo.VideoPolicy;

/**
 * UploadPolicy 업데이트 Command
 *
 * UploadPolicy를 업데이트하기 위한 데이터를 전달하는 Command 객체입니다.
 *
 * @param policyKeyDto 정책 식별자
 * @param imagePolicy 새로운 이미지 정책 (nullable)
 * @param videoPolicy 새로운 비디오 정책 (nullable)
 * @param htmlPolicy 새로운 HTML 정책 (nullable)
 * @param excelPolicy 새로운 Excel 정책 (nullable)
 * @param pdfPolicy 새로운 PDF 정책 (nullable)
 * @param changedBy 변경자
 * @author sangwon-ryu
 */
public record UpdateUploadPolicyCommand(
        PolicyKeyDto policyKeyDto,
        ImagePolicy imagePolicy,
        VideoPolicy videoPolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy,
        String changedBy
) {
    public PolicyKey getPolicyKey() {
        return policyKeyDto.toDomain();
    }

    public FileTypePolicies getFileTypePolicies() {
        return FileTypePolicies.of(imagePolicy, videoPolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }
}
