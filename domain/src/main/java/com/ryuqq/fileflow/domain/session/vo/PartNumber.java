package com.ryuqq.fileflow.domain.session.vo;

/**
 * Multipart Upload의 Part 번호 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Part 번호는 1 이상이어야 한다.
 *   <li>Part 번호는 10,000 이하여야 한다 (S3 제약).
 * </ul>
 *
 * @param number Part 번호 (1부터 시작)
 */
public record PartNumber(int number) {

    private static final int MIN_PART_NUMBER = 1;
    private static final int MAX_PART_NUMBER = 10_000;

    /** Compact Constructor (검증 로직). */
    public PartNumber {
        if (number < MIN_PART_NUMBER || number > MAX_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format(
                            "Part 번호는 %d ~ %d 사이여야 합니다: %d",
                            MIN_PART_NUMBER, MAX_PART_NUMBER, number));
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param number Part 번호 (1 ~ 10,000)
     * @return PartNumber
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static PartNumber of(int number) {
        return new PartNumber(number);
    }

    /**
     * 첫 번째 Part인지 확인한다.
     *
     * @return 첫 번째 Part이면 true
     */
    public boolean isFirst() {
        return number == MIN_PART_NUMBER;
    }

    /**
     * 마지막 Part인지 확인한다.
     *
     * @param totalParts 전체 Part 개수
     * @return 마지막 Part이면 true
     */
    public boolean isLast(TotalParts totalParts) {
        if (totalParts == null) {
            throw new IllegalArgumentException("TotalParts는 null일 수 없습니다.");
        }
        return number == totalParts.value();
    }

    /**
     * 다음 Part 번호를 반환한다.
     *
     * @return 다음 PartNumber
     * @throws IllegalArgumentException 마지막 Part인 경우
     */
    public PartNumber next() {
        if (number >= MAX_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("마지막 Part %d 다음은 존재하지 않습니다.", MAX_PART_NUMBER));
        }
        return new PartNumber(number + 1);
    }

    /**
     * 이전 Part 번호를 반환한다.
     *
     * @return 이전 PartNumber
     * @throws IllegalArgumentException 첫 번째 Part인 경우
     */
    public PartNumber previous() {
        if (number <= MIN_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("첫 번째 Part %d 이전은 존재하지 않습니다.", MIN_PART_NUMBER));
        }
        return new PartNumber(number - 1);
    }
}
