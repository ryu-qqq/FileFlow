package com.ryuqq.fileflow.domain.file.thumbnail;

/**
 * Image Width Value Object
 *
 * <p>이미지 너비를 나타내는 불변 Value Object입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>너비는 1 이상이어야 함 (최소 1픽셀)</li>
 *   <li>음수 및 0은 허용하지 않음</li>
 * </ul>
 *
 * <p><strong>불변성:</strong></p>
 * <ul>
 *   <li>Record 패턴 사용 (Java 21)</li>
 *   <li>모든 필드는 final</li>
 *   <li>Compact Constructor로 유효성 검증</li>
 * </ul>
 *
 * @param value 이미지 너비 (픽셀)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ImageWidth(int value) {

    /**
     * Compact Constructor - 유효성 검증
     *
     * @throws IllegalArgumentException 너비가 1 미만인 경우
     */
    public ImageWidth {
        if (value < 1) {
            throw new IllegalArgumentException(
                "이미지 너비는 1 이상이어야 합니다: " + value
            );
        }
    }

    /**
     * ImageWidth 생성 (Static Factory Method)
     *
     * @param value 이미지 너비 (픽셀)
     * @return ImageWidth VO
     * @throws IllegalArgumentException 너비가 1 미만인 경우
     */
    public static ImageWidth of(int value) {
        return new ImageWidth(value);
    }
}
