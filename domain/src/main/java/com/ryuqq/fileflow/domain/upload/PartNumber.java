package com.ryuqq.fileflow.domain.upload;

/**
 * PartNumber Value Object
 * S3 Multipart Upload의 Part 번호를 나타내는 값 객체
 *
 * <p>Part 번호는 S3 Multipart Upload에서 각 Part를 식별하는 고유 번호입니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>Part 번호는 1부터 시작합니다</li>
 *   <li>최대 10,000개의 Part까지 허용됩니다 (S3 제한)</li>
 *   <li>Part 번호는 순차적이어야 합니다</li>
 * </ul>
 *
 * @param value Part 번호 (1-10000)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PartNumber(Integer value) {

    /**
     * S3 Multipart Upload의 최소 Part 번호
     */
    public static final int MIN_PART_NUMBER = 1;

    /**
     * S3 Multipart Upload의 최대 Part 번호 (S3 제한)
     */
    public static final int MAX_PART_NUMBER = 10_000;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException Part 번호가 null이거나 유효 범위를 벗어난 경우
     */
    public PartNumber {
        if (value == null) {
            throw new IllegalArgumentException("Part 번호는 필수입니다");
        }
        if (value < MIN_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("Part 번호는 %d 이상이어야 합니다: %d", MIN_PART_NUMBER, value)
            );
        }
        if (value > MAX_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("Part 번호는 %d 이하여야 합니다: %d", MAX_PART_NUMBER, value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value Part 번호
     * @return PartNumber 인스턴스
     * @throws IllegalArgumentException Part 번호가 유효하지 않은 경우
     */
    public static PartNumber of(Integer value) {
        return new PartNumber(value);
    }

    /**
     * 첫 번째 Part인지 확인
     *
     * @return 첫 번째 Part이면 true
     */
    public boolean isFirst() {
        return value == MIN_PART_NUMBER;
    }

    /**
     * 마지막 Part인지 확인 (총 Part 수 기준)
     *
     * @param totalParts 전체 Part 수
     * @return 마지막 Part이면 true
     */
    public boolean isLast(Integer totalParts) {
        return value.equals(totalParts);
    }

    /**
     * 다음 Part 번호 생성
     *
     * @return 다음 Part 번호
     * @throws IllegalArgumentException 이미 최대 Part 번호인 경우
     */
    public PartNumber next() {
        if (value >= MAX_PART_NUMBER) {
            throw new IllegalArgumentException("더 이상 Part를 추가할 수 없습니다 (최대 10,000개)");
        }
        return new PartNumber(value + 1);
    }

    /**
     * 이전 Part 번호 생성
     *
     * @return 이전 Part 번호
     * @throws IllegalArgumentException 이미 첫 번째 Part인 경우
     */
    public PartNumber previous() {
        if (value <= MIN_PART_NUMBER) {
            throw new IllegalArgumentException("첫 번째 Part 이전은 존재하지 않습니다");
        }
        return new PartNumber(value - 1);
    }
}
