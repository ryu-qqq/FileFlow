package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 색 공간(Color Space) enum.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>이미지 처리 시 색 공간 정보를 추출하여 저장
 *   <li>추출 실패 시 UNKNOWN 사용
 *   <li>SVG 등 벡터 이미지는 UNKNOWN 처리
 * </ul>
 *
 * <p><strong>지원 색 공간</strong>:
 *
 * <ul>
 *   <li>RGB: 표준 RGB (sRGB, Display P3 등)
 *   <li>RGBA: 알파 채널 포함 RGB
 *   <li>CMYK: 인쇄용 색 공간
 *   <li>GRAYSCALE: 흑백 이미지
 *   <li>INDEXED: 팔레트 기반 (GIF 등)
 *   <li>UNKNOWN: 추출 실패 또는 미지원 포맷
 * </ul>
 */
public enum ColorSpace {

    /** 표준 RGB 색 공간 (sRGB, Display P3 등). */
    RGB("RGB", "표준 RGB 색 공간"),

    /** 알파 채널 포함 RGB. */
    RGBA("RGBA", "알파 채널 포함 RGB"),

    /** 인쇄용 CMYK 색 공간. */
    CMYK("CMYK", "인쇄용 CMYK 색 공간"),

    /** 흑백 이미지. */
    GRAYSCALE("Grayscale", "흑백 이미지"),

    /** 팔레트 기반 색 공간 (GIF 등). */
    INDEXED("Indexed", "팔레트 기반 색 공간"),

    /** 추출 실패 또는 미지원 포맷. */
    UNKNOWN("Unknown", "색 공간 정보 없음");

    private final String displayName;
    private final String description;

    ColorSpace(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 표시용 이름을 반환합니다.
     *
     * @return 표시용 이름
     */
    public String displayName() {
        return displayName;
    }

    /**
     * 설명을 반환합니다.
     *
     * @return 설명
     */
    public String description() {
        return description;
    }

    /**
     * 알파 채널을 지원하는 색 공간인지 확인합니다.
     *
     * @return RGBA 또는 INDEXED면 true
     */
    public boolean supportsAlpha() {
        return this == RGBA || this == INDEXED;
    }

    /**
     * 웹 표시에 적합한 색 공간인지 확인합니다.
     *
     * @return RGB, RGBA, GRAYSCALE, INDEXED면 true
     */
    public boolean isWebCompatible() {
        return this == RGB || this == RGBA || this == GRAYSCALE || this == INDEXED;
    }

    /**
     * 인쇄용 색 공간인지 확인합니다.
     *
     * @return CMYK면 true
     */
    public boolean isPrintReady() {
        return this == CMYK;
    }

    /**
     * 문자열에서 ColorSpace를 파싱합니다.
     *
     * <p>대소문자 무시, 공백 제거 후 매칭합니다. 매칭 실패 시 UNKNOWN 반환.
     *
     * @param value 색 공간 문자열 (예: "sRGB", "RGBA", "Grayscale")
     * @return 매칭된 ColorSpace 또는 UNKNOWN
     */
    public static ColorSpace fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        String normalized = value.trim().toUpperCase();

        // sRGB, AdobeRGB 등 RGB 계열 처리
        if (normalized.contains("RGB") && !normalized.contains("RGBA")) {
            return RGB;
        }
        if (normalized.contains("RGBA")) {
            return RGBA;
        }
        if (normalized.contains("CMYK")) {
            return CMYK;
        }
        if (normalized.contains("GRAY") || normalized.contains("GREY")) {
            return GRAYSCALE;
        }
        if (normalized.contains("INDEX") || normalized.contains("PALETTE")) {
            return INDEXED;
        }

        // 정확한 enum 이름 매칭 시도
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
