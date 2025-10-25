package com.ryuqq.fileflow.application.iam.permission.dto.response;

import com.ryuqq.fileflow.domain.iam.permission.exception.DenialReason;

/**
 * Evaluate Permission Response
 *
 * <p>권한 평가 결과를 나타내는 불변 Response DTO입니다.</p>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Response - 권한 평가 결과 반환</li>
 *   <li>UseCase: EvaluatePermissionUseCase</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * // 권한 허용 (조건 없음)
 * EvaluatePermissionResponse allowed = EvaluatePermissionResponse.allowed();
 *
 * // 권한 허용 (조건 충족)
 * EvaluatePermissionResponse allowedWithCondition = EvaluatePermissionResponse.allowedWithCondition(
 *     "res.size_mb <= 20"
 * );
 *
 * // 권한 거부 (Grant 없음)
 * EvaluatePermissionResponse denied = EvaluatePermissionResponse.denied(
 *     DenialReason.NO_GRANT,
 *     "사용자에게 file.upload 권한이 없습니다"
 * );
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Java 21 Record 패턴 사용</li>
 *   <li>✅ 불변성 보장 (Immutable)</li>
 *   <li>✅ 정적 팩토리 메서드 제공</li>
 * </ul>
 *
 * @param allowed 권한 허용 여부 (true: 허용, false: 거부)
 * @param denialReason 거부 사유 (허용 시 null)
 * @param message 상세 메시지 (선택 사항)
 * @param evaluatedCondition 평가된 조건식 (ABAC 조건이 있었던 경우)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record EvaluatePermissionResponse(
    boolean allowed,
    DenialReason denialReason,
    String message,
    String evaluatedCondition
) {

    /**
     * EvaluatePermissionResponse Compact Constructor
     *
     * <p>Record 생성 시 자동으로 호출되어 유효성을 검증합니다.</p>
     *
     * @throws IllegalArgumentException 유효하지 않은 조합인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public EvaluatePermissionResponse {
        // 허용된 경우 DenialReason이 없어야 함
        if (allowed && denialReason != null) {
            throw new IllegalArgumentException("권한이 허용된 경우 DenialReason은 null이어야 합니다");
        }

        // 거부된 경우 DenialReason이 있어야 함
        if (!allowed && denialReason == null) {
            throw new IllegalArgumentException("권한이 거부된 경우 DenialReason은 필수입니다");
        }

        // 문자열 정규화
        if (message != null && message.isBlank()) {
            message = null;
        } else if (message != null) {
            message = message.trim();
        }

        if (evaluatedCondition != null && evaluatedCondition.isBlank()) {
            evaluatedCondition = null;
        } else if (evaluatedCondition != null) {
            evaluatedCondition = evaluatedCondition.trim();
        }
    }

    /**
     * 조건 없이 권한이 허용된 Response를 생성합니다
     *
     * @return 허용 Response
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluatePermissionResponse ofAllowed() {
        return new EvaluatePermissionResponse(true, null, "권한이 허용되었습니다", null);
    }

    /**
     * 조건 충족으로 권한이 허용된 Response를 생성합니다
     *
     * @param condition 평가된 조건식
     * @return 허용 Response (조건 포함)
     * @throws IllegalArgumentException condition이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluatePermissionResponse ofAllowedWithCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("조건식은 필수입니다");
        }
        return new EvaluatePermissionResponse(
            true,
            null,
            "ABAC 조건을 충족하여 권한이 허용되었습니다",
            condition
        );
    }

    /**
     * 권한이 거부된 Response를 생성합니다
     *
     * @param reason 거부 사유
     * @param message 상세 메시지
     * @return 거부 Response
     * @throws IllegalArgumentException reason이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluatePermissionResponse ofDenied(DenialReason reason, String message) {
        if (reason == null) {
            throw new IllegalArgumentException("거부 사유는 필수입니다");
        }
        return new EvaluatePermissionResponse(false, reason, message, null);
    }

    /**
     * ABAC 조건 불충족으로 권한이 거부된 Response를 생성합니다
     *
     * @param condition 평가된 조건식
     * @param message 상세 메시지
     * @return 거부 Response (조건 포함)
     * @throws IllegalArgumentException condition이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static EvaluatePermissionResponse ofDeniedByCondition(String condition, String message) {
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("조건식은 필수입니다");
        }
        return new EvaluatePermissionResponse(
            false,
            DenialReason.CONDITION_NOT_MET,
            message,
            condition
        );
    }

    /**
     * ABAC 조건이 있었는지 확인합니다
     *
     * @return 조건이 있었으면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasEvaluatedCondition() {
        return evaluatedCondition != null && !evaluatedCondition.isEmpty();
    }

    /**
     * EvaluatePermissionResponse의 문자열 표현을 반환합니다 (디버깅 및 로깅용)
     *
     * @return Response의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        if (allowed) {
            if (hasEvaluatedCondition()) {
                return String.format(
                    "EvaluatePermissionResponse[ALLOWED with condition='%s', message='%s']",
                    evaluatedCondition, message
                );
            } else {
                return String.format(
                    "EvaluatePermissionResponse[ALLOWED, message='%s']",
                    message
                );
            }
        } else {
            if (hasEvaluatedCondition()) {
                return String.format(
                    "EvaluatePermissionResponse[DENIED reason=%s, condition='%s', message='%s']",
                    denialReason, evaluatedCondition, message
                );
            } else {
                return String.format(
                    "EvaluatePermissionResponse[DENIED reason=%s, message='%s']",
                    denialReason, message
                );
            }
        }
    }
}
