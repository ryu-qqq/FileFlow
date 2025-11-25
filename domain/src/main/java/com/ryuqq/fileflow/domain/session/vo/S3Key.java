package com.ryuqq.fileflow.domain.session.vo;

/**
 * S3 객체 키 (경로) Value Object.
 *
 * <p><strong>AWS S3 객체 키 규칙</strong>:
 *
 * <ul>
 *   <li>최대 1024자
 *   <li>UTF-8 인코딩 지원
 *   <li>경로 순회 공격 방지 (../ 금지)
 *   <li>빈 경로 세그먼트 불가 (예: "a//b")
 * </ul>
 *
 * @param key S3 객체 키 (예: "uploads/2024/01/image.jpg")
 */
public record S3Key(String key) {

    private static final int MAX_LENGTH = 1024;
    private static final String PATH_SEPARATOR = "/";

    /** Compact Constructor (검증 로직). */
    public S3Key {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("S3 객체 키는 null이거나 빈 문자열일 수 없습니다.");
        }

        if (key.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("S3 객체 키는 최대 %d자를 초과할 수 없습니다: %d", MAX_LENGTH, key.length()));
        }

        if (key.contains("../") || key.contains("..\\")) {
            throw new IllegalArgumentException("S3 객체 키에 경로 순회 패턴(../)이 포함될 수 없습니다: " + key);
        }

        if (key.contains("//")) {
            throw new IllegalArgumentException("S3 객체 키에 빈 경로 세그먼트(//)가 포함될 수 없습니다: " + key);
        }

        if (key.startsWith(PATH_SEPARATOR)) {
            throw new IllegalArgumentException("S3 객체 키는 슬래시(/)로 시작할 수 없습니다: " + key);
        }

        if (containsControlCharacters(key)) {
            throw new IllegalArgumentException("S3 객체 키에 제어 문자가 포함될 수 없습니다: " + key);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param key S3 객체 키 (null 불가, 최대 1024자)
     * @return S3Key
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static S3Key of(String key) {
        return new S3Key(key);
    }

    /**
     * 경로 세그먼트들로부터 S3 키 생성.
     *
     * @param segments 경로 세그먼트 (예: "uploads", "2024", "01", "image.jpg")
     * @return S3Key
     * @throws IllegalArgumentException segments가 null이거나 빈 경우
     */
    public static S3Key fromSegments(String... segments) {
        if (segments == null || segments.length == 0) {
            throw new IllegalArgumentException("경로 세그먼트는 null이거나 비어있을 수 없습니다.");
        }

        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] == null || segments[i].isBlank()) {
                throw new IllegalArgumentException("경로 세그먼트는 null이거나 빈 문자열일 수 없습니다.");
            }

            keyBuilder.append(segments[i]);

            if (i < segments.length - 1) {
                keyBuilder.append(PATH_SEPARATOR);
            }
        }

        return new S3Key(keyBuilder.toString());
    }

    /**
     * 디렉토리 경로를 반환한다 (파일명 제외).
     *
     * @return 디렉토리 경로 (없으면 빈 문자열)
     */
    public String getDirectory() {
        int lastSlashIndex = key.lastIndexOf(PATH_SEPARATOR);
        if (lastSlashIndex == -1) {
            return "";
        }
        return key.substring(0, lastSlashIndex);
    }

    /**
     * 파일명을 반환한다 (디렉토리 경로 제외).
     *
     * @return 파일명
     */
    public String getFileName() {
        int lastSlashIndex = key.lastIndexOf(PATH_SEPARATOR);
        if (lastSlashIndex == -1) {
            return key;
        }
        return key.substring(lastSlashIndex + 1);
    }

    /**
     * S3 키가 안전한지 확인한다 (경로 순회 공격 없음).
     *
     * @return 안전하면 true
     */
    public boolean isSecure() {
        return !key.contains("../")
                && !key.contains("..\\")
                && !key.contains("//")
                && !containsControlCharacters(key);
    }

    /**
     * 특정 디렉토리 하위에 있는지 확인한다.
     *
     * @param directory 디렉토리 경로 (예: "uploads/2024")
     * @return 하위에 있으면 true
     */
    public boolean isUnder(String directory) {
        if (directory == null || directory.isBlank()) {
            return false;
        }

        String normalizedDir =
                directory.endsWith(PATH_SEPARATOR) ? directory : directory + PATH_SEPARATOR;

        return key.startsWith(normalizedDir);
    }

    /**
     * 제어 문자 포함 여부 확인.
     *
     * @param value 검사할 문자열
     * @return 제어 문자 포함 시 true
     */
    private static boolean containsControlCharacters(String value) {
        for (char c : value.toCharArray()) {
            if (Character.isISOControl(c)) {
                return true;
            }
        }
        return false;
    }
}
