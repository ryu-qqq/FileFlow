package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Policy Violation Error Response DTO
 *
 * 정책 위반 시 상세한 에러 정보를 제공하는 DTO
 *
 * @param timestamp 에러 발생 시각
 * @param status HTTP 상태 코드
 * @param error 에러 타입 (POLICY_VIOLATION)
 * @param message 사용자 친화적 에러 메시지
 * @param details 상세 정보 (위반된 정책 값들)
 * @param path 요청 경로
 * @author sangwon-ryu
 */
public record PolicyViolationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, Object> details,
        String path
) {
    /**
     * PolicyViolationException으로부터 응답 생성
     *
     * @param ex PolicyViolationException
     * @param path 요청 경로
     * @return PolicyViolationErrorResponse
     */
    public static PolicyViolationErrorResponse from(PolicyViolationException ex, String path) {
        return new PolicyViolationErrorResponse(
                LocalDateTime.now(),
                400,
                ex.getViolationType().name(),
                convertToUserFriendlyMessage(ex.getViolationType(), ex.getDetails()),
                parseDetails(ex.getDetails()),
                path
        );
    }

    /**
     * ViolationType에 따라 사용자 친화적 메시지 생성
     *
     * @param violationType 위반 타입
     * @param details 상세 정보
     * @return 사용자 친화적 메시지
     */
    private static String convertToUserFriendlyMessage(
            PolicyViolationException.ViolationType violationType,
            String details
    ) {
        return switch (violationType) {
            case FILE_SIZE_EXCEEDED -> "파일 크기가 정책 허용 범위를 초과했습니다";
            case FILE_COUNT_EXCEEDED -> "파일 개수가 정책 허용 범위를 초과했습니다";
            case INVALID_FORMAT -> "허용되지 않은 파일 형식입니다";
            case DIMENSION_EXCEEDED -> "이미지 크기가 정책 허용 범위를 초과했습니다";
            case RATE_LIMIT_EXCEEDED -> "요청 제한을 초과했습니다";
        };
    }

    /**
     * details 문자열을 Map으로 파싱
     *
     * 현재는 details를 그대로 반환하지만,
     * 향후 구조화된 정보로 확장 가능
     *
     * @param details 상세 정보 문자열
     * @return 상세 정보 Map
     */
    private static Map<String, Object> parseDetails(String details) {
        // 현재는 단순히 details 문자열을 반환
        // 향후 PolicyViolationException에서 구조화된 정보를 제공하면 파싱 로직 추가
        return Map.of("rawDetails", details);
    }
}
