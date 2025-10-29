package com.ryuqq.fileflow.adapter.rest.iam.permission.dto;

/**
 * Permission Evaluation API Response DTO
 *
 * <p>Permission 평가 결과 API 응답 DTO입니다.</p>
 *
 * <p><strong>Endpoint</strong>: GET /api/v1/permissions/evaluate</p>
 *
 * <p><strong>Response Format</strong>:</p>
 * <pre>
 * {
 *   "allowed": true,
 *   "denialReason": null,
 *   "message": "권한이 허용되었습니다",
 *   "evaluatedCondition": null
 * }
 * </pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ 불변성 보장 (모든 필드 final)</li>
 *   <li>✅ Null-safe getter 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
public class PermissionEvaluationApiResponse {

    /**
     * 권한 허용 여부 (true: 허용, false: 거부)
     */
    private final boolean allowed;

    /**
     * 거부 사유 (허용 시 null)
     *
     * <p>가능한 값:</p>
     * <ul>
     *   <li>NO_GRANT - Grant가 없음</li>
     *   <li>SCOPE_MISMATCH - Scope 불일치</li>
     *   <li>CONDITION_NOT_MET - ABAC 조건 불충족</li>
     *   <li>CONDITION_EVALUATION_FAILED - ABAC 평가 실패</li>
     *   <li>SYSTEM_ERROR - 시스템 오류</li>
     * </ul>
     */
    private final String denialReason;

    /**
     * 상세 메시지 (선택)
     */
    private final String message;

    /**
     * 평가된 ABAC 조건식 (조건이 있었던 경우에만)
     */
    private final String evaluatedCondition;

    /**
     * Constructor - DtoMapper에서 사용
     *
     * @param allowed 권한 허용 여부
     * @param denialReason 거부 사유
     * @param message 상세 메시지
     * @param evaluatedCondition 평가된 조건식
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionEvaluationApiResponse(
        boolean allowed,
        String denialReason,
        String message,
        String evaluatedCondition
    ) {
        this.allowed = allowed;
        this.denialReason = denialReason;
        this.message = message;
        this.evaluatedCondition = evaluatedCondition;
    }

    /**
     * 권한 허용 여부를 반환합니다
     *
     * @return true면 허용, false면 거부
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * 거부 사유를 반환합니다
     *
     * @return 거부 사유 (허용 시 null)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getDenialReason() {
        return denialReason;
    }

    /**
     * 상세 메시지를 반환합니다
     *
     * @return 상세 메시지
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getMessage() {
        return message;
    }

    /**
     * 평가된 조건식을 반환합니다
     *
     * @return 평가된 조건식 (조건이 없었으면 null)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public String getEvaluatedCondition() {
        return evaluatedCondition;
    }

    /**
     * PermissionEvaluationApiResponse의 문자열 표현을 반환합니다 (디버깅용)
     *
     * @return Response의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-27
     */
    @Override
    public String toString() {
        return String.format(
            "PermissionEvaluationApiResponse[allowed=%b, denialReason=%s, message='%s', condition='%s']",
            allowed, denialReason, message, evaluatedCondition
        );
    }
}
