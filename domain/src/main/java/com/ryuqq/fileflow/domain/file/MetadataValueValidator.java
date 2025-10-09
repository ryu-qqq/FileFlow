package com.ryuqq.fileflow.domain.file;

/**
 * 메타데이터 값 검증을 담당하는 유틸리티 클래스
 *
 * Domain 계층에서 순수 Java만 사용하여 기본적인 형식 검증을 수행합니다.
 * 더 엄격한 검증은 Infrastructure 계층에서 수행합니다.
 *
 * @author sangwon-ryu
 */
public final class MetadataValueValidator {

    private MetadataValueValidator() {
        // 유틸리티 클래스 - 인스턴스화 방지
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 문자열이 숫자인지 검증합니다.
     *
     * @param value 검증할 문자열
     * @return 숫자로 변환 가능하면 true
     */
    public static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 문자열이 불리언 값인지 검증합니다.
     *
     * @param value 검증할 문자열
     * @return "true" 또는 "false"면 true (대소문자 무시)
     */
    public static boolean isBoolean(String value) {
        String lower = value.toLowerCase();
        return "true".equals(lower) || "false".equals(lower);
    }

    /**
     * 문자열이 JSON 형식인지 기본 검증합니다.
     *
     * 검증 규칙:
     * - JSON 객체: '{' 로 시작하고 '}' 로 끝남
     * - JSON 배열: '[' 로 시작하고 ']' 로 끝남
     * - 최소 길이: 2자 이상 (빈 객체 {}, 빈 배열 [] 포함)
     * - 따옴표 쌍 일치 검증: escape되지 않은 따옴표가 짝수개여야 함
     *
     * 참고: 완전한 JSON 파싱은 infrastructure 계층에서 수행
     *       (Domain은 순수 Java만 사용, Jackson 같은 외부 의존성 불허)
     *
     * @param value 검증할 문자열
     * @return 기본 JSON 형식을 만족하면 true
     */
    public static boolean isJson(String value) {
        String trimmed = value.trim();

        // JSON 객체 또는 배열 형식 확인
        boolean isObject = trimmed.startsWith("{") && trimmed.endsWith("}");
        boolean isArray = trimmed.startsWith("[") && trimmed.endsWith("]");

        if (!isObject && !isArray) {
            return false;
        }

        // 최소 길이 검증 (빈 객체 {} 또는 빈 배열 [] 포함)
        if (trimmed.length() < 2) {
            return false;
        }

        // 따옴표 불일치 검증: escape되지 않은 따옴표가 짝수개여야 함
        int quoteCount = 0;
        boolean escaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                quoteCount++;
            }
        }

        // 따옴표가 홀수개면 잘못된 JSON
        return quoteCount % 2 == 0;
    }
}
