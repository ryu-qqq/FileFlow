package com.ryuqq.fileflow.application.policy.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;

import java.time.LocalDateTime;

/**
 * UploadPolicy 생성 Command
 *
 * UploadPolicy를 생성하기 위한 데이터를 전달하는 Command 객체입니다.
 *
 * @param policyKeyDto 정책 식별자
 * @param imagePolicy 이미지 정책 (nullable)
 * @param htmlPolicy HTML 정책 (nullable)
 * @param excelPolicy Excel 정책 (nullable)
 * @param pdfPolicy PDF 정책 (nullable)
 * @param requestsPerHour 시간당 요청 제한
 * @param uploadsPerDay 일일 업로드 제한
 * @param effectiveFrom 유효 시작 일시
 * @param effectiveUntil 유효 종료 일시
 * @author sangwon-ryu
 */
public record CreateUploadPolicyCommand(
        PolicyKeyDto policyKeyDto,
        ImagePolicy imagePolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy,
        int requestsPerHour,
        int uploadsPerDay,
        LocalDateTime effectiveFrom,
        LocalDateTime effectiveUntil
) {
    /**
     * Command를 검증하고 도메인 객체 생성에 필요한 값들을 반환합니다.
     */
    public PolicyKey getPolicyKey() {
        return policyKeyDto.toDomain();
    }

    public FileTypePolicies getFileTypePolicies() {
        return FileTypePolicies.of(imagePolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }

    public RateLimiting getRateLimiting() {
        return new RateLimiting(requestsPerHour, uploadsPerDay);
    }
}
