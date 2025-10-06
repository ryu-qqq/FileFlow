package com.ryuqq.fileflow.application.policy.port.out;

import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;

/**
 * 정책 검증을 위한 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * Application Layer가 정책 검증 로직을 실행하기 위한 인터페이스입니다.
 * Epic 1에서 구현된 정책 검증 로직과 통합됩니다.
 *
 * @author sangwon-ryu
 */
public interface PolicyValidationPort {

    /**
     * 파일 업로드 정책을 검증합니다.
     *
     * 검증 항목:
     * 1. 정책이 존재하는지 확인
     * 2. 정책이 활성화되어 있는지 확인
     * 3. 현재 시점이 유효 기간 내인지 확인
     * 4. 파일 타입에 대한 정책이 존재하는지 확인
     * 5. 파일 크기가 허용 범위 내인지 확인
     * 6. 파일 개수가 허용 범위 내인지 확인
     *
     * @param policyKey 정책 식별자
     * @param fileType 파일 타입
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param fileCount 파일 개수
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 위반 시
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    void validateUploadPolicy(
            PolicyKey policyKey,
            FileType fileType,
            long fileSizeBytes,
            int fileCount
    );

    /**
     * Rate Limiting 정책을 검증합니다.
     *
     * @param policyKey 정책 식별자
     * @param currentRequestCount 현재 시간당 요청 횟수
     * @param currentUploadCount 현재 일일 업로드 횟수
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException Rate Limit 초과 시
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    void validateRateLimit(
            PolicyKey policyKey,
            int currentRequestCount,
            int currentUploadCount
    );
}
