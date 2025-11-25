package com.ryuqq.fileflow.domain.session.vo;

/**
 * Multipart Upload의 전체 Part 개수 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Part 개수는 1개 이상이어야 한다.
 *   <li>Part 개수는 10,000개를 초과할 수 없다 (S3 제약).
 * </ul>
 *
 * @param value Part 개수 (1 ~ 10,000)
 */
public record TotalParts(int value) {

    private static final int MIN_PARTS = 1;
    private static final int MAX_PARTS = 10_000;

    /** Compact Constructor (검증 로직). */
    public TotalParts {
        if (value < MIN_PARTS || value > MAX_PARTS) {
            throw new IllegalArgumentException(
                    String.format("Part 개수는 %d ~ %d 사이여야 합니다: %d", MIN_PARTS, MAX_PARTS, value));
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value Part 개수 (1 ~ 10,000)
     * @return TotalParts
     * @throws IllegalArgumentException value가 1 ~ 10,000 범위를 벗어난 경우
     */
    public static TotalParts of(int value) {
        return new TotalParts(value);
    }

    /**
     * 파일 크기와 Part 크기로부터 전체 Part 개수를 계산한다.
     *
     * @param fileSize 전체 파일 크기 (bytes)
     * @param partSize Part 크기 (bytes)
     * @return TotalParts (올림 계산)
     * @throws IllegalArgumentException 계산된 Part 개수가 1 ~ 10,000 범위를 벗어난 경우
     */
    public static TotalParts calculate(long fileSize, long partSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 양수여야 합니다: " + fileSize);
        }
        if (partSize <= 0) {
            throw new IllegalArgumentException("Part 크기는 양수여야 합니다: " + partSize);
        }

        // 올림 계산: (fileSize + partSize - 1) / partSize
        int totalParts = (int) ((fileSize + partSize - 1) / partSize);
        return TotalParts.of(totalParts);
    }

    /**
     * 특정 Part 번호가 유효한지 확인한다.
     *
     * @param partNumber Part 번호
     * @return 유효하면 true (1 ~ totalParts 범위 내)
     */
    public boolean isValidPartNumber(int partNumber) {
        return partNumber >= MIN_PARTS && partNumber <= this.value;
    }
}
