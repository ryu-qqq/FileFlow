package com.ryuqq.fileflow.domain.upload;

/**
 * MimeType Value Object
 * 파일의 MIME 타입을 나타내는 값 객체
 *
 * <p>MIME 타입은 파일의 형식을 식별하는 표준 방식입니다.
 * 예: "image/jpeg", "application/pdf", "video/mp4"</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>MIME 타입은 필수 값입니다</li>
 *   <li>"type/subtype" 형식이어야 합니다</li>
 *   <li>허용된 MIME 타입 패턴을 따라야 합니다</li>
 * </ul>
 *
 * @param value MIME 타입 (예: "image/jpeg")
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record MimeType(String value) {

    /**
     * 기본 MIME 타입 (알 수 없는 형식)
     */
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException MIME 타입이 null이거나 유효하지 않은 형식인 경우
     */
    public MimeType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MIME 타입은 필수입니다");
        }
        value = value.trim().toLowerCase();

        if (!isValidFormat(value)) {
            throw new IllegalArgumentException(
                    String.format("유효하지 않은 MIME 타입 형식입니다: %s", value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value MIME 타입
     * @return MimeType 인스턴스
     * @throws IllegalArgumentException MIME 타입이 유효하지 않은 경우
     */
    public static MimeType of(String value) {
        return new MimeType(value);
    }

    /**
     * 기본 MIME 타입으로 생성
     * 파일 형식을 알 수 없을 때 사용
     *
     * @return 기본 MimeType 인스턴스
     */
    public static MimeType defaultType() {
        return new MimeType(DEFAULT_MIME_TYPE);
    }

    /**
     * MIME 타입 형식 검증
     * "type/subtype" 패턴 확인
     *
     * @param mimeType 검증할 MIME 타입
     * @return 유효한 형식이면 true
     */
    private static boolean isValidFormat(String mimeType) {
        // MIME 타입: type/subtype 형식
        // 예: image/jpeg, application/pdf, text/plain
        return mimeType.matches("^[a-z]+/[a-z0-9.+-]+$");
    }

    /**
     * 이미지 파일인지 확인
     *
     * @return 이미지 MIME 타입이면 true
     */
    public boolean isImage() {
        return value.startsWith("image/");
    }

    /**
     * 동영상 파일인지 확인
     *
     * @return 동영상 MIME 타입이면 true
     */
    public boolean isVideo() {
        return value.startsWith("video/");
    }

    /**
     * 오디오 파일인지 확인
     *
     * @return 오디오 MIME 타입이면 true
     */
    public boolean isAudio() {
        return value.startsWith("audio/");
    }

    /**
     * 텍스트 파일인지 확인
     *
     * @return 텍스트 MIME 타입이면 true
     */
    public boolean isText() {
        return value.startsWith("text/");
    }

    /**
     * 애플리케이션 파일인지 확인
     * (PDF, ZIP, JSON 등)
     *
     * @return 애플리케이션 MIME 타입이면 true
     */
    public boolean isApplication() {
        return value.startsWith("application/");
    }

    /**
     * 특정 MIME 타입인지 확인
     *
     * @param mimeTypeValue 비교할 MIME 타입 문자열
     * @return 일치하면 true
     */
    public boolean matches(String mimeTypeValue) {
        if (mimeTypeValue == null) {
            return false;
        }
        return this.value.equals(mimeTypeValue.trim().toLowerCase());
    }

    /**
     * MIME 타입의 주 타입 반환
     * 예: "image/jpeg" → "image"
     *
     * @return 주 타입
     */
    public String getType() {
        int slashIndex = value.indexOf('/');
        return slashIndex > 0 ? value.substring(0, slashIndex) : value;
    }

    /**
     * MIME 타입의 하위 타입 반환
     * 예: "image/jpeg" → "jpeg"
     *
     * @return 하위 타입
     */
    public String getSubtype() {
        int slashIndex = value.indexOf('/');
        return slashIndex > 0 && slashIndex < value.length() - 1
                ? value.substring(slashIndex + 1)
                : "";
    }
}
