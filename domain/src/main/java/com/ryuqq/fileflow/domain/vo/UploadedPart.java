package com.ryuqq.fileflow.domain.vo;

/**
 * UploadedPart Value Object
 * <p>
 * S3 멀티파트 업로드에서 업로드된 개별 파트 정보를 캡슐화합니다.
 * </p>
 *
 * <p>
 * S3 Multipart Upload 파트 정보:
 * - partNumber: 1부터 시작하는 파트 번호 (최대 10,000)
 * - etag: S3가 반환한 파트의 ETag (체크섬)
 * - size: 파트의 크기 (bytes)
 * </p>
 *
 * <p>
 * S3 Complete Multipart Upload 시 필요:
 * - partNumber와 etag를 함께 전송하여 최종 파일 조립
 * </p>
 */
public record UploadedPart(int partNumber, ETag etag, long size) {

    /**
     * 최소 파트 번호
     */
    private static final int MIN_PART_NUMBER = 1;

    /**
     * 최소 파트 크기
     */
    private static final long MIN_PART_SIZE = 1L;

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public UploadedPart {
        validatePartNumber(partNumber);
        validateEtag(etag);
        validateSize(size);
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param partNumber 파트 번호 (1부터 시작)
     * @param etag       S3가 반환한 ETag
     * @param size       파트 크기 (bytes)
     * @return UploadedPart VO
     */
    public static UploadedPart of(int partNumber, ETag etag, long size) {
        return new UploadedPart(partNumber, etag, size);
    }

    /**
     * 파트 번호 검증
     */
    private static void validatePartNumber(int partNumber) {
        if (partNumber < MIN_PART_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("파트 번호는 %d 이상이어야 합니다 (현재: %d)", MIN_PART_NUMBER, partNumber)
            );
        }
    }

    /**
     * ETag 검증
     */
    private static void validateEtag(ETag etag) {
        if (etag == null) {
            throw new IllegalArgumentException("ETag는 null일 수 없습니다");
        }
    }

    /**
     * 파트 크기 검증
     */
    private static void validateSize(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException(
                    String.format("파트 크기는 0보다 커야 합니다 (현재: %d bytes)", size)
            );
        }
    }
}
