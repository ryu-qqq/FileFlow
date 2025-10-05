package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;

import java.time.LocalDateTime;

/**
 * Policy Response DTO
 *
 * 정책 조회/수정/활성화 결과를 전달하는 응답 DTO
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
public record PolicyResponse(
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
     * Application Response를 REST Response로 변환합니다.
     *
     * @param uploadPolicyResponse Application 계층의 응답
     * @return PolicyResponse
     */
    public static PolicyResponse from(UploadPolicyResponse uploadPolicyResponse) {
        return new PolicyResponse(
                uploadPolicyResponse.policyKey(),
                uploadPolicyResponse.imagePolicy(),
                uploadPolicyResponse.htmlPolicy(),
                uploadPolicyResponse.excelPolicy(),
                uploadPolicyResponse.pdfPolicy(),
                uploadPolicyResponse.requestsPerHour(),
                uploadPolicyResponse.uploadsPerDay(),
                uploadPolicyResponse.version(),
                uploadPolicyResponse.isActive(),
                uploadPolicyResponse.effectiveFrom(),
                uploadPolicyResponse.effectiveUntil()
        );
    }
}
