package com.ryuqq.fileflow.application.policy.dto;

import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;

import java.time.LocalDateTime;

/**
 * UploadPolicy 응답 DTO
 *
 * UploadPolicy 조회 결과를 전달하는 응답 객체입니다.
 *
 * @param policyKey 정책 식별자
 * @param imagePolicy 이미지 정책
 * @param htmlPolicy HTML 정책
 * @param excelPolicy Excel 정책
 * @param pdfPolicy PDF 정책
 * @param requestsPerHour 시간당 요청 제한
 * @param uploadsPerDay 일일 업로드 제한
 * @param version 버전
 * @param isActive 활성 상태
 * @param effectiveFrom 유효 시작 일시
 * @param effectiveUntil 유효 종료 일시
 * @author sangwon-ryu
 */
public record UploadPolicyResponse(
        String policyKey,
        ImagePolicy imagePolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy,
        int requestsPerHour,
        int uploadsPerDay,
        int version,
        boolean isActive,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveUntil
) {
    /**
     * Domain의 UploadPolicy로부터 Response를 생성합니다.
     *
     * @param uploadPolicy 도메인 UploadPolicy
     * @return UploadPolicyResponse
     */
    public static UploadPolicyResponse from(UploadPolicy uploadPolicy) {
        FileTypePolicies fileTypePolicies = uploadPolicy.getFileTypePolicies();
        RateLimiting rateLimiting = uploadPolicy.getRateLimiting();

        return new UploadPolicyResponse(
                uploadPolicy.getPolicyKey().getValue(),
                fileTypePolicies.getImagePolicy(),
                fileTypePolicies.getHtmlPolicy(),
                fileTypePolicies.getExcelPolicy(),
                fileTypePolicies.getPdfPolicy(),
                rateLimiting.requestsPerHour(),
                rateLimiting.uploadsPerDay(),
                uploadPolicy.getVersion(),
                uploadPolicy.isActive(),
                uploadPolicy.getEffectiveFrom(),
                uploadPolicy.getEffectiveUntil()
        );
    }
}
