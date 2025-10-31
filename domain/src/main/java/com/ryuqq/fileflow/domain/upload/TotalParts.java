package com.ryuqq.fileflow.domain.upload;

/**
 * TotalParts Value Object
 * Multipart Upload의 전체 Part 개수를 나타내는 값 객체
 *
 * <p>전체 Part 개수는 Multipart Upload가 몇 개의 Part로 구성되는지를 나타냅니다.
 * S3는 최대 10,000개의 Part를 허용합니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>전체 Part 개수는 필수 값입니다</li>
 *   <li>최소 1개 이상이어야 합니다</li>
 *   <li>최대 10,000개까지 허용됩니다 (S3 제한)</li>
 * </ul>
 *
 * @param value 전체 Part 개수 (1-10000)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record TotalParts(Integer value) {

    /**
     * 최소 Part 개수
     */
    public static final int MIN_PARTS = 1;

    /**
     * 최대 Part 개수 (S3 제한)
     */
    public static final int MAX_PARTS = 10_000;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException Part 개수가 null이거나 유효 범위를 벗어난 경우
     */
    public TotalParts {
        if (value == null) {
            throw new IllegalArgumentException("전체 Part 개수는 필수입니다");
        }
        if (value < MIN_PARTS) {
            throw new IllegalArgumentException(
                    String.format("전체 Part 개수는 %d 이상이어야 합니다: %d", MIN_PARTS, value)
            );
        }
        if (value > MAX_PARTS) {
            throw new IllegalArgumentException(
                    String.format("전체 Part 개수는 %d 이하여야 합니다: %d", MAX_PARTS, value)
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 전체 Part 개수
     * @return TotalParts 인스턴스
     * @throws IllegalArgumentException Part 개수가 유효하지 않은 경우
     */
    public static TotalParts of(Integer value) {
        return new TotalParts(value);
    }

    /**
     * 파일 크기를 기반으로 전체 Part 개수 계산
     * Part 크기는 S3 최소 Part 크기 (5MB) 기준
     *
     * @param fileSize 파일 크기
     * @return 계산된 TotalParts 인스턴스
     */
    public static TotalParts calculateFrom(FileSize fileSize) {
        if (fileSize == null) {
            throw new IllegalArgumentException("파일 크기는 필수입니다");
        }

        long bytes = fileSize.bytes();
        long partSize = FileSize.MIN_MULTIPART_PART_SIZE;

        // 올림 계산 (마지막 Part는 5MB보다 작을 수 있음)
        int parts = (int) Math.ceil((double) bytes / partSize);

        // 최소 1개, 최대 10,000개 제한
        parts = Math.max(MIN_PARTS, Math.min(parts, MAX_PARTS));

        return new TotalParts(parts);
    }

    /**
     * 단일 Part 업로드인지 확인
     *
     * @return Part 개수가 1개면 true
     */
    public boolean isSinglePart() {
        return value == MIN_PARTS;
    }

    /**
     * Multipart Upload인지 확인
     *
     * @return Part 개수가 2개 이상이면 true
     */
    public boolean isMultipart() {
        return value > MIN_PARTS;
    }

    /**
     * 최대 Part 개수에 도달했는지 확인
     *
     * @return Part 개수가 최대치면 true
     */
    public boolean isMaxParts() {
        return value == MAX_PARTS;
    }

    /**
     * 특정 Part 번호가 마지막 Part인지 확인
     *
     * @param partNumber 확인할 Part 번호
     * @return 마지막 Part이면 true
     */
    public boolean isLastPart(PartNumber partNumber) {
        if (partNumber == null) {
            return false;
        }
        return partNumber.value().equals(value);
    }

    /**
     * 업로드 진행률 계산
     *
     * @param uploadedParts 업로드 완료된 Part 개수
     * @return 진행률 (0.0 ~ 1.0)
     */
    public double calculateProgress(int uploadedParts) {
        if (uploadedParts < 0 || uploadedParts > value) {
            throw new IllegalArgumentException(
                    String.format("업로드 완료 Part 개수가 유효하지 않습니다: %d (전체: %d)", uploadedParts, value)
            );
        }
        return (double) uploadedParts / value;
    }
}
