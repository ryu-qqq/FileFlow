package com.ryuqq.fileflow.domain.iam.permission.exception;

/**
 * Denial Reason Enum
 *
 * <p>권한 거부 사유를 나타내는 Enum입니다.</p>
 *
 * <p><strong>사용 목적:</strong></p>
 * <ul>
 *   <li>권한 거부 시 구체적인 사유 제공</li>
 *   <li>디버깅 및 로깅 용이성</li>
 *   <li>클라이언트에게 의미 있는 에러 메시지 전달</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Enum 타입으로 타입 안전성 보장</li>
 *   <li>✅ 불변성 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public enum DenialReason {

    /**
     * Grant가 존재하지 않음
     *
     * <p>사용자에게 요청한 권한에 대한 Grant가 없습니다.</p>
     */
    NO_GRANT("NO_GRANT", "권한이 부여되지 않았습니다"),

    /**
     * Scope 불일치
     *
     * <p>Grant의 Scope가 요청된 Scope를 포함하지 않습니다.</p>
     * <p>예: SELF 권한으로 ORGANIZATION 범위 작업 시도</p>
     */
    SCOPE_MISMATCH("SCOPE_MISMATCH", "권한 범위가 일치하지 않습니다"),

    /**
     * ABAC 조건 불충족
     *
     * <p>CEL 조건식 평가 결과가 false입니다.</p>
     * <p>예: "res.size_mb <= 20" 조건에서 파일 크기가 25MB인 경우</p>
     */
    CONDITION_NOT_MET("CONDITION_NOT_MET", "권한 조건을 충족하지 않습니다"),

    /**
     * ABAC 조건 평가 실패
     *
     * <p>CEL 조건식 평가 중 에러가 발생했습니다.</p>
     * <p>예: 표현식 오류, 변수 누락 등</p>
     */
    CONDITION_EVALUATION_FAILED("CONDITION_EVALUATION_FAILED", "권한 조건 평가에 실패했습니다"),

    /**
     * 권한 평가 시스템 에러
     *
     * <p>예상치 못한 시스템 에러가 발생했습니다.</p>
     */
    SYSTEM_ERROR("SYSTEM_ERROR", "시스템 오류로 권한 평가에 실패했습니다");

    private final String code;
    private final String description;

    /**
     * DenialReason을 생성합니다
     *
     * @param code 거부 사유 코드
     * @param description 거부 사유 설명
     */
    DenialReason(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 거부 사유 코드를 반환합니다
     *
     * @return 거부 사유 코드
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getCode() {
        return code;
    }

    /**
     * 거부 사유 설명을 반환합니다
     *
     * @return 거부 사유 설명
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public String getDescription() {
        return description;
    }

    /**
     * 코드로부터 DenialReason을 찾아 반환합니다
     *
     * @param code 거부 사유 코드
     * @return 해당하는 DenialReason
     * @throws IllegalArgumentException 일치하는 DenialReason이 없는 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static DenialReason fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("DenialReason 코드는 필수입니다");
        }

        for (DenialReason reason : values()) {
            if (reason.code.equalsIgnoreCase(code.trim())) {
                return reason;
            }
        }

        throw new IllegalArgumentException("알 수 없는 DenialReason 코드: " + code);
    }

    /**
     * DenialReason의 문자열 표현을 반환합니다
     *
     * @return DenialReason의 읽기 쉬운 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String toString() {
        return String.format("DenialReason[%s: %s]", code, description);
    }
}
