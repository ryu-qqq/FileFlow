package com.ryuqq.fileflow.domain.file.thumbnail;

/**
 * Image Height Value Object
 *
 * <p>이미지 높이를 나타내는 불변 Value Object입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>높이는 1 이상이어야 함 (최소 1픽셀)</li>
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
 * @param value 이미지 높이 (픽셀)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ImageHeight(int value) {

    /**
     * Compact Constructor - 유효성 검증
     *
     * @throws IllegalArgumentException 높이가 1 미만인 경우
     */
    public ImageHeight {
        if (value < 1) {
            throw new IllegalArgumentException(
                "이미지 높이는 1 이상이어야 합니다: " + value
            );
        }
    }

    /**
     * ImageHeight 생성 (Static Factory Method)
     *
     * @param value 이미지 높이 (픽셀)
     * @return ImageHeight VO
     * @throws IllegalArgumentException 높이가 1 미만인 경우
     */
    public static ImageHeight of(int value) {
        return new ImageHeight(value);
    }
}
