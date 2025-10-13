package com.ryuqq.fileflow.application.image.dto;

import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.time.Duration;

/**
 * 이미지 변환 결과 DTO
 *
 * 이미지 변환 작업의 결과를 전달하는 DTO입니다.
 *
 * @param fileId 파일 식별자
 * @param resultS3Uri 결과 이미지 S3 URI
 * @param originalFormat 원본 이미지 포맷
 * @param resultFormat 결과 이미지 포맷
 * @param appliedStrategy 적용된 최적화 전략
 * @param originalSizeBytes 원본 파일 크기 (bytes)
 * @param convertedSizeBytes 변환 후 파일 크기 (bytes)
 * @param processingTime 처리 시간
 * @param successful 변환 성공 여부
 * @author sangwon-ryu
 */
public record ImageConversionResult(
        FileId fileId,
        String resultS3Uri,
        ImageFormat originalFormat,
        ImageFormat resultFormat,
        OptimizationStrategy appliedStrategy,
        long originalSizeBytes,
        long convertedSizeBytes,
        Duration processingTime,
        boolean successful
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ImageConversionResult {
        validateFileId(fileId);
        validateResultS3Uri(resultS3Uri);
        validateImageFormat("Original format", originalFormat);
        validateImageFormat("Result format", resultFormat);
        validateAppliedStrategy(appliedStrategy);
        validateSize("Original size", originalSizeBytes);
        validateSize("Converted size", convertedSizeBytes);
        validateProcessingTime(processingTime);
    }

    /**
     * 성공적인 변환 결과를 생성합니다.
     *
     * @param fileId 파일 식별자
     * @param resultS3Uri 결과 이미지 S3 URI
     * @param originalFormat 원본 이미지 포맷
     * @param resultFormat 결과 이미지 포맷
     * @param appliedStrategy 적용된 최적화 전략
     * @param originalSizeBytes 원본 파일 크기
     * @param convertedSizeBytes 변환 후 파일 크기
     * @param processingTime 처리 시간
     * @return ImageConversionResult 인스턴스
     */
    public static ImageConversionResult success(
            FileId fileId,
            String resultS3Uri,
            ImageFormat originalFormat,
            ImageFormat resultFormat,
            OptimizationStrategy appliedStrategy,
            long originalSizeBytes,
            long convertedSizeBytes,
            Duration processingTime
    ) {
        return new ImageConversionResult(
                fileId,
                resultS3Uri,
                originalFormat,
                resultFormat,
                appliedStrategy,
                originalSizeBytes,
                convertedSizeBytes,
                processingTime,
                true
        );
    }

    /**
     * 실패한 변환 결과를 생성합니다.
     *
     * @param fileId 파일 식별자
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param originalFormat 원본 이미지 포맷
     * @param appliedStrategy 시도한 최적화 전략
     * @param originalSizeBytes 원본 파일 크기
     * @param processingTime 처리 시간
     * @return ImageConversionResult 인스턴스
     */
    public static ImageConversionResult failure(
            FileId fileId,
            String sourceS3Uri,
            ImageFormat originalFormat,
            OptimizationStrategy appliedStrategy,
            long originalSizeBytes,
            Duration processingTime
    ) {
        return new ImageConversionResult(
                fileId,
                sourceS3Uri,
                originalFormat,
                originalFormat, // 실패 시 포맷 변경 없음
                appliedStrategy,
                originalSizeBytes,
                originalSizeBytes, // 실패 시 크기 변경 없음
                processingTime,
                false
        );
    }

    /**
     * 파일 크기 감소율을 계산합니다.
     *
     * @return 감소율 (0.0 ~ 1.0)
     */
    public double getReductionRate() {
        if (originalSizeBytes == 0 || !successful) {
            return 0.0;
        }
        long reduced = originalSizeBytes - convertedSizeBytes;
        return (double) reduced / originalSizeBytes;
    }

    /**
     * 파일 크기 감소율을 퍼센트로 반환합니다.
     *
     * @return 감소율 (0 ~ 100)
     */
    public double getReductionPercentage() {
        return getReductionRate() * 100;
    }

    /**
     * 감소된 파일 크기를 반환합니다.
     *
     * @return 감소된 바이트 수
     */
    public long getReducedBytes() {
        return successful ? originalSizeBytes - convertedSizeBytes : 0;
    }

    /**
     * WebP로 변환되었는지 확인합니다.
     *
     * @return 결과 포맷이 WebP이면 true
     */
    public boolean convertedToWebP() {
        return successful && resultFormat.isWebP();
    }

    /**
     * 사람이 읽기 쉬운 변환 요약을 반환합니다.
     *
     * @return 변환 요약 문자열
     */
    public String getSummary() {
        if (!successful) {
            return String.format(
                    "Conversion failed: %s (Processing time: %dms)",
                    originalFormat,
                    processingTime.toMillis()
            );
        }

        return String.format(
                "Conversion successful: %s → %s (%.1f%% reduction, %d bytes saved), Processing time: %dms",
                originalFormat,
                resultFormat,
                getReductionPercentage(),
                getReducedBytes(),
                processingTime.toMillis()
        );
    }

    // ========== Validation Methods ==========

    private static void validateFileId(FileId fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
    }

    private static void validateResultS3Uri(String resultS3Uri) {
        if (resultS3Uri == null || resultS3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Result S3 URI cannot be null or empty");
        }
        if (!resultS3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Result S3 URI must start with 's3://'");
        }
    }

    private static void validateImageFormat(String name, ImageFormat format) {
        if (format == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    private static void validateAppliedStrategy(OptimizationStrategy appliedStrategy) {
        if (appliedStrategy == null) {
            throw new IllegalArgumentException("Applied strategy cannot be null");
        }
    }

    private static void validateSize(String name, long size) {
        if (size < 0) {
            throw new IllegalArgumentException(name + " cannot be negative, but was: " + size);
        }
    }

    private static void validateProcessingTime(Duration processingTime) {
        if (processingTime == null) {
            throw new IllegalArgumentException("Processing time cannot be null");
        }
        if (processingTime.isNegative()) {
            throw new IllegalArgumentException("Processing time cannot be negative");
        }
    }
}
