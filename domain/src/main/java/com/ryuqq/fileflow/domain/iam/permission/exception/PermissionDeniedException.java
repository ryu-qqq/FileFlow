package com.ryuqq.fileflow.domain.iam.permission.exception;

import com.ryuqq.fileflow.domain.common.DomainException;

/**
 * PermissionDeniedException - 권한 거부 예외
 *
 * <p>사용자가 요청한 작업을 수행할 권한이 없을 때 발생하는 예외입니다.</p>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 403 FORBIDDEN</li>
 *   <li>Error Code: PERMISSION-001</li>
 *   <li>Message: "Permission denied: {detailMessage}"</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Grant가 없는 경우
 * throw new PermissionDeniedException(
 *     DenialReason.NO_GRANT,
 *     "file.upload",
 *     "사용자에게 file.upload 권한이 없습니다"
 * );
 *
 * // Scope 불일치
 * throw new PermissionDeniedException(
 *     DenialReason.SCOPE_MISMATCH,
 *     "file.delete",
 *     "SELF 권한으로 ORGANIZATION 범위 작업을 수행할 수 없습니다"
 * );
 *
 * // ABAC 조건 불충족
 * throw new PermissionDeniedException(
 *     DenialReason.CONDITION_NOT_MET,
 *     "file.upload",
 *     "파일 크기 20MB 제한을 초과했습니다 (res.size_mb <= 20)"
 * );
 * }</pre>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ DomainException 상속</li>
 *   <li>✅ DenialReason 포함 (거부 사유)</li>
 *   <li>✅ 상세 메시지 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class PermissionDeniedException extends DomainException {

    private final DenialReason denialReason;
    private final String permissionCode;

    /**
     * Constructor - DenialReason과 상세 메시지 포함
     *
     * <p>권한 거부 시 구체적인 사유와 메시지를 포함하여 예외를 생성합니다.</p>
     *
     * @param denialReason 거부 사유 (Not null)
     * @param permissionCode 거부된 권한 코드 (Not null)
     * @param detailMessage 상세 메시지 (Not null)
     * @throws IllegalArgumentException denialReason, permissionCode, detailMessage가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public PermissionDeniedException(
        DenialReason denialReason,
        String permissionCode,
        String detailMessage
    ) {
        this(denialReason, permissionCode, detailMessage, null);
    }

    /**
     * Constructor - 원인 예외 포함
     *
     * <p>ABAC 조건 평가 실패 등 원인 예외를 포함하는 경우 사용합니다.</p>
     *
     * @param denialReason 거부 사유 (Not null)
     * @param permissionCode 거부된 권한 코드 (Not null)
     * @param detailMessage 상세 메시지 (Not null)
     * @param cause 원인 예외
     * @throws IllegalArgumentException denialReason, permissionCode, detailMessage가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public PermissionDeniedException(
        DenialReason denialReason,
        String permissionCode,
        String detailMessage,
        Throwable cause
    ) {
        super(
            PermissionErrorCode.PERMISSION_DENIED,
            buildMessage(denialReason, permissionCode, detailMessage),
            cause
        );

        if (denialReason == null) {
            throw new IllegalArgumentException("DenialReason은 필수입니다");
        }
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new IllegalArgumentException("Permission 코드는 필수입니다");
        }
        if (detailMessage == null || detailMessage.isBlank()) {
            throw new IllegalArgumentException("상세 메시지는 필수입니다");
        }

        this.denialReason = denialReason;
        this.permissionCode = permissionCode.trim();
    }

    /**
     * 에러 메시지 생성 (Private Helper)
     *
     * <p>DenialReason, permissionCode, detailMessage를 조합하여 최종 메시지를 생성합니다.</p>
     *
     * <p><strong>메시지 포맷:</strong></p>
     * <pre>
     * "Permission denied: [{DenialReason.code}] {permissionCode} - {detailMessage}"
     * 예: "Permission denied: [NO_GRANT] file.upload - 사용자에게 file.upload 권한이 없습니다"
     * </pre>
     *
     * @param denialReason 거부 사유
     * @param permissionCode 권한 코드
     * @param detailMessage 상세 메시지
     * @return 최종 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private static String buildMessage(
        DenialReason denialReason,
        String permissionCode,
        String detailMessage
    ) {
        if (denialReason == null || permissionCode == null || detailMessage == null) {
            return "Permission denied";
        }

        return String.format(
            "Permission denied: [%s] %s - %s",
            denialReason.getCode(),
            permissionCode.trim(),
            detailMessage.trim()
        );
    }

    /**
     * 거부 사유 반환
     *
     * @return DenialReason
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public DenialReason getDenialReason() {
        return denialReason;
    }

    /**
     * 거부된 권한 코드 반환
     *
     * @return Permission 코드
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * 정적 팩토리 메서드 - Grant 없음
     *
     * @param permissionCode 권한 코드
     * @return PermissionDeniedException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static PermissionDeniedException noGrant(String permissionCode) {
        return new PermissionDeniedException(
            DenialReason.NO_GRANT,
            permissionCode,
            String.format("사용자에게 %s 권한이 부여되지 않았습니다", permissionCode)
        );
    }

    /**
     * 정적 팩토리 메서드 - Scope 불일치
     *
     * @param permissionCode 권한 코드
     * @param grantScope Grant의 Scope
     * @param requestedScope 요청된 Scope
     * @return PermissionDeniedException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static PermissionDeniedException scopeMismatch(
        String permissionCode,
        String grantScope,
        String requestedScope
    ) {
        return new PermissionDeniedException(
            DenialReason.SCOPE_MISMATCH,
            permissionCode,
            String.format(
                "%s 권한 범위(%s)로 %s 범위 작업을 수행할 수 없습니다",
                permissionCode, grantScope, requestedScope
            )
        );
    }

    /**
     * 정적 팩토리 메서드 - ABAC 조건 불충족
     *
     * @param permissionCode 권한 코드
     * @param condition 평가된 조건식
     * @param detailMessage 상세 메시지
     * @return PermissionDeniedException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static PermissionDeniedException conditionNotMet(
        String permissionCode,
        String condition,
        String detailMessage
    ) {
        return new PermissionDeniedException(
            DenialReason.CONDITION_NOT_MET,
            permissionCode,
            String.format(
                "%s - 조건: %s",
                detailMessage, condition
            )
        );
    }

    /**
     * 정적 팩토리 메서드 - ABAC 조건 평가 실패
     *
     * @param permissionCode 권한 코드
     * @param condition 평가된 조건식
     * @param cause 원인 예외
     * @return PermissionDeniedException
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static PermissionDeniedException conditionEvaluationFailed(
        String permissionCode,
        String condition,
        Throwable cause
    ) {
        return new PermissionDeniedException(
            DenialReason.CONDITION_EVALUATION_FAILED,
            permissionCode,
            String.format(
                "ABAC 조건 평가 중 에러가 발생했습니다 - 조건: %s",
                condition
            ),
            cause
        );
    }
}
