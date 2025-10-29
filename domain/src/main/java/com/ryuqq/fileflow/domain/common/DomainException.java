package com.ryuqq.fileflow.domain.common;

import java.util.Map;

/**
 * DomainException - 도메인 예외 클래스
 *
 * <p>모든 도메인 예외는 이 클래스를 상속하여 일관된 예외 처리를 보장합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ ErrorCode 기반 예외 생성 지원</li>
 *   <li>✅ 기본 메시지 또는 커스텀 메시지 지원</li>
 *   <li>✅ RuntimeException 계열 (Unchecked Exception)</li>
 *   <li>✅ 메시지 템플릿 파라미터 지원 (args)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 */
public class DomainException extends RuntimeException {

    private final String code;              // ex) TENANT-001, PERMISSION-001
    private final Map<String, Object> args; // 메시지 템플릿 파라미터 등 (선택)

    /**
     * ErrorCode 기본 메시지 사용 생성자
     *
     * <p>ErrorCode의 getMessage()를 예외 메시지로 사용합니다.</p>
     *
     * @param errorCode 에러 코드
     * @author ryu-qqq
     * @since 2025-10-29
     */
    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.args = Map.of();
    }

    /**
     * ErrorCode 커스텀 메시지 사용 생성자
     *
     * <p>ErrorCode의 기본 메시지 대신 커스텀 메시지를 사용합니다.</p>
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-29
     */
    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.args = Map.of();
    }

    /**
     * ErrorCode Cause 포함 생성자
     *
     * <p>원인 예외를 포함한 DomainException 생성</p>
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-10-29
     */
    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.args = Map.of();
    }

    /**
     * String code 기본 생성자 (기존 호환성)
     *
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-29
     */
    public DomainException(String code, String message) {
        super(message);
        this.code = code;
        this.args = Map.of();
    }

    /**
     * String code args 포함 생성자 (기존 호환성)
     *
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @param args 메시지 템플릿 파라미터
     * @author ryu-qqq
     * @since 2025-10-29
     */
    public DomainException(String code, String message, Map<String, Object> args) {
        super(message);
        this.code = code;
        this.args = args == null ? Map.of() : Map.copyOf(args);
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 문자열
     * @author ryu-qqq
     * @since 2025-10-29
     */
    public String code() {
        return code;
    }

    /**
     * 메시지 템플릿 파라미터 반환
     *
     * @return 파라미터 맵
     * @author ryu-qqq
     * @since 2025-10-29
     */
    public Map<String, Object> args() {
        return args;
    }
}