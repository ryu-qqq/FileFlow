package com.ryuqq.fileflow.domain.upload.vo;

import java.util.List;

/**
 * 멀티파트 업로드 정보를 나타내는 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 멀티파트 업로드 시작(initiate) 시 생성되는 정보
 *
 * AWS S3 멀티파트 업로드:
 * - 대용량 파일(100MB 이상)을 여러 파트로 분할하여 업로드
 * - 각 파트는 독립적으로 업로드 가능 (병렬 처리)
 * - 모든 파트 업로드 후 complete 호출로 최종 병합
 */
public record MultipartUploadInfo(
        String uploadId,
        String uploadPath,
        List<PartUploadInfo> parts
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public MultipartUploadInfo {
        validateUploadId(uploadId);
        validateUploadPath(uploadPath);
        validateParts(parts);

        // 방어적 복사: 외부에서 리스트 변경 불가
        parts = List.copyOf(parts);
    }

    /**
     * MultipartUploadInfo를 생성합니다.
     *
     * @param uploadId AWS S3 멀티파트 업로드 ID
     * @param uploadPath S3 업로드 경로 (bucket key)
     * @param parts 파트 업로드 정보 리스트
     * @return MultipartUploadInfo 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static MultipartUploadInfo of(
            String uploadId,
            String uploadPath,
            List<PartUploadInfo> parts
    ) {
        return new MultipartUploadInfo(uploadId, uploadPath, parts);
    }

    /**
     * 총 파트 개수를 반환합니다.
     *
     * @return 파트 개수
     */
    public int totalParts() {
        return parts.size();
    }

    /**
     * 특정 파트 번호의 PartUploadInfo를 반환합니다.
     *
     * @param partNumber 파트 번호 (1-based)
     * @return PartUploadInfo
     * @throws IllegalArgumentException 유효하지 않은 파트 번호인 경우
     */
    public PartUploadInfo getPart(int partNumber) {
        if (partNumber < 1 || partNumber > parts.size()) {
            throw new IllegalArgumentException(
                    "Invalid part number: " + partNumber +
                    ". Valid range: 1 to " + parts.size()
            );
        }
        return parts.get(partNumber - 1);
    }

    // ========== Validation Methods ==========

    private static void validateUploadId(String uploadId) {
        if (uploadId == null || uploadId.trim().isEmpty()) {
            throw new IllegalArgumentException("UploadId cannot be null or empty");
        }
    }

    private static void validateUploadPath(String uploadPath) {
        if (uploadPath == null || uploadPath.trim().isEmpty()) {
            throw new IllegalArgumentException("UploadPath cannot be null or empty");
        }
    }

    private static void validateParts(List<PartUploadInfo> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("Parts cannot be null or empty");
        }

        // AWS S3 제약: 최대 10,000개 파트
        if (parts.size() > 10_000) {
            throw new IllegalArgumentException(
                    "Parts cannot exceed 10,000 (AWS S3 limitation). Got: " + parts.size()
            );
        }

        // 파트 번호 연속성 검증 (1, 2, 3, ...)
        for (int i = 0; i < parts.size(); i++) {
            int expectedPartNumber = i + 1;
            int actualPartNumber = parts.get(i).partNumber();

            if (actualPartNumber != expectedPartNumber) {
                throw new IllegalArgumentException(
                        "Part numbers must be sequential starting from 1. " +
                        "Expected: " + expectedPartNumber + ", but got: " + actualPartNumber
                );
            }
        }
    }
}
