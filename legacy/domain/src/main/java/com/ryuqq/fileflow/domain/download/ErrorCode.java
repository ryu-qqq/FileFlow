package com.ryuqq.fileflow.domain.download;

/**
 * ErrorCode Value Object
 * 다운로드 에러 코드를 나타내는 값 객체
 *
 * <p>에러 코드는 외부 다운로드 실패 시 에러 타입을 분류합니다.
 * HTTP 상태 코드, AWS SDK 에러 코드 등을 저장합니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>에러 코드는 선택적 값입니다 (에러 발생 시에만 존재)</li>
 *   <li>빈 문자열은 허용되지 않습니다</li>
 *   <li>대문자와 숫자, 언더스코어만 허용 (예: "HTTP_404", "S3_ACCESS_DENIED")</li>
 * </ul>
 *
 * @param value 에러 코드
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ErrorCode(String value) {

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 에러 코드가 빈 문자열이거나 형식이 유효하지 않은 경우
     */
    public ErrorCode {
        if (value != null) {
            if (value.isBlank()) {
                throw new IllegalArgumentException("에러 코드는 빈 문자열일 수 없습니다");
            }
            value = value.trim().toUpperCase();

            if (!isValidFormat(value)) {
                throw new IllegalArgumentException(
                        "에러 코드는 대문자, 숫자, 언더스코어만 허용됩니다: " + value
                );
            }
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 에러 코드
     * @return ErrorCode 인스턴스
     * @throws IllegalArgumentException 에러 코드가 유효하지 않은 경우
     */
    public static ErrorCode of(String value) {
        return new ErrorCode(value);
    }

    /**
     * 에러 코드 형식 검증
     * 대문자, 숫자, 언더스코어만 허용
     *
     * @param code 검증할 에러 코드
     * @return 유효한 형식이면 true
     */
    private static boolean isValidFormat(String code) {
        return code.matches("^[A-Z0-9_]+$");
    }

    /**
     * 에러 코드가 존재하는지 확인
     *
     * @return 에러 코드가 있으면 true
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * HTTP 에러 코드인지 확인
     *
     * @return HTTP_ 접두사로 시작하면 true
     */
    public boolean isHttpError() {
        return value != null && value.startsWith("HTTP_");
    }

    /**
     * S3 에러 코드인지 확인
     *
     * @return S3_ 접두사로 시작하면 true
     */
    public boolean isS3Error() {
        return value != null && value.startsWith("S3_");
    }

    /**
     * 특정 에러 코드와 일치하는지 확인
     *
     * @param code 비교할 에러 코드
     * @return 일치하면 true
     */
    public boolean matches(String code) {
        if (value == null || code == null || code.isBlank()) {
            return false;
        }
        return value.equals(code.trim().toUpperCase());
    }
}
