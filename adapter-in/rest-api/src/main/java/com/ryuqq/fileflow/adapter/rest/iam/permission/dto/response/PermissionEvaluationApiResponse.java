package com.ryuqq.fileflow.adapter.rest.iam.permission.dto.response;

/**
 * Permission Evaluation API Response DTO (Record)
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
 *   <li>✅ Java Record 사용 - 불변성 보장</li>
 *   <li>✅ 간결한 코드</li>
 *   <li>✅ Null-safe</li>
 * </ul>
 *
 * <p><strong>거부 사유 (denialReason) 가능한 값:</strong></p>
 * <ul>
 *   <li>NO_GRANT - Grant가 없음</li>
 *   <li>SCOPE_MISMATCH - Scope 불일치</li>
 *   <li>CONDITION_NOT_MET - ABAC 조건 불충족</li>
 *   <li>CONDITION_EVALUATION_FAILED - ABAC 평가 실패</li>
 *   <li>SYSTEM_ERROR - 시스템 오류</li>
 * </ul>
 *
 * @param allowed 권한 허용 여부 (true: 허용, false: 거부)
 * @param denialReason 거부 사유 (허용 시 null)
 * @param message 상세 메시지 (선택)
 * @param evaluatedCondition 평가된 ABAC 조건식 (조건이 있었던 경우에만)
 * @author ryu-qqq
 * @since 2025-10-29
 */
public record PermissionEvaluationApiResponse(

    boolean allowed,
    String denialReason,
    String message,
    String evaluatedCondition
) {
}
