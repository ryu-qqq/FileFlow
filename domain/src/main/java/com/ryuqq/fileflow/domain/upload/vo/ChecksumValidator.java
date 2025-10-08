package com.ryuqq.fileflow.domain.upload.vo;

/**
 * 체크섬 검증 비즈니스 규칙
 *
 * 상태가 없는 유틸리티 클래스로 모든 메서드는 static입니다.
 *
 * 책임:
 * - 업로드된 파일의 체크섬과 기대 체크섬의 일치 여부 검증
 * - 파일 무결성 보장
 *
 * 비즈니스 규칙:
 * - 체크섬은 대소문자 구분 없이 비교
 * - 알고리즘이 동일한 체크섬만 비교 가능
 * - 검증 실패 시 명확한 오류 메시지 제공
 */
public final class ChecksumValidator {

    /**
     * 유틸리티 클래스는 인스턴스화를 방지합니다.
     */
    private ChecksumValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 예상 체크섬과 실제 체크섬이 일치하는지 검증합니다.
     *
     * @param expected 예상 체크섬
     * @param actual 실제 체크섬
     * @return 일치하면 true
     * @throws IllegalArgumentException 체크섬이 null인 경우
     */
    public static boolean validate(CheckSum expected, CheckSum actual) {
        validateNotNull(expected, "Expected checksum");
        validateNotNull(actual, "Actual checksum");

        return expected.matches(actual);
    }

    /**
     * 예상 체크섬과 실제 체크섬이 일치하는지 검증하고, 불일치 시 예외를 발생시킵니다.
     *
     * @param expected 예상 체크섬
     * @param actual 실제 체크섬
     * @throws ChecksumMismatchException 체크섬이 일치하지 않는 경우
     * @throws IllegalArgumentException 체크섬이 null인 경우
     */
    public static void validateOrThrow(CheckSum expected, CheckSum actual) {
        validateNotNull(expected, "Expected checksum");
        validateNotNull(actual, "Actual checksum");

        if (!expected.matches(actual)) {
            throw new ChecksumMismatchException(
                    String.format(
                            "Checksum mismatch. Expected: %s (%s), but got: %s (%s)",
                            expected.value(),
                            expected.algorithm(),
                            actual.value(),
                            actual.algorithm()
                    )
            );
        }
    }

    /**
     * 여러 체크섬 중 하나라도 일치하는지 검증합니다.
     *
     * @param expected 예상 체크섬 배열
     * @param actual 실제 체크섬
     * @return 하나라도 일치하면 true
     * @throws IllegalArgumentException 체크섬이 null이거나 배열이 비어있는 경우
     */
    public static boolean validateAny(CheckSum[] expected, CheckSum actual) {
        validateNotNull(actual, "Actual checksum");

        if (expected == null || expected.length == 0) {
            throw new IllegalArgumentException("Expected checksums cannot be null or empty");
        }

        for (CheckSum exp : expected) {
            if (exp != null && exp.matches(actual)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 두 체크섬의 알고리즘이 동일한지 확인합니다.
     *
     * @param checksum1 첫 번째 체크섬
     * @param checksum2 두 번째 체크섬
     * @return 알고리즘이 동일하면 true
     * @throws IllegalArgumentException 체크섬이 null인 경우
     */
    public static boolean hasSameAlgorithm(CheckSum checksum1, CheckSum checksum2) {
        validateNotNull(checksum1, "First checksum");
        validateNotNull(checksum2, "Second checksum");

        return checksum1.algorithm().equals(checksum2.algorithm());
    }

    /**
     * 체크섬의 알고리즘이 권장 알고리즘인지 확인합니다.
     *
     * 권장 알고리즘: SHA-256, SHA-512
     * 비권장 알고리즘: MD5 (보안 취약점)
     *
     * @param checksum 확인할 체크섬
     * @return 권장 알고리즘이면 true
     * @throws IllegalArgumentException 체크섬이 null인 경우
     */
    public static boolean isRecommendedAlgorithm(CheckSum checksum) {
        validateNotNull(checksum, "Checksum");

        String algorithm = checksum.algorithm();
        return algorithm.equals("SHA-256") || algorithm.equals("SHA-512");
    }

    // ========== Private Helper Methods ==========

    private static void validateNotNull(CheckSum checksum, String paramName) {
        if (checksum == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
    }

    // ========== Custom Exception ==========

    /**
     * 체크섬 불일치 예외
     */
    public static class ChecksumMismatchException extends RuntimeException {
        public ChecksumMismatchException(String message) {
            super(message);
        }
    }
}
