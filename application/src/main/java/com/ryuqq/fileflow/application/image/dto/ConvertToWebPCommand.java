package com.ryuqq.fileflow.application.image.dto;

import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

/**
 * WebP 변환 Command
 *
 * 이미지를 WebP 포맷으로 변환하기 위한 데이터를 전달하는 Command 객체입니다.
 *
 * 비즈니스 규칙:
 * - FileId는 필수
 * - 소스 S3 URI는 필수
 * - 소스 포맷은 필수
 * - 압축 품질은 선택적 (기본값: 90%)
 * - 메타데이터 보존 여부 선택 가능
 *
 * @param fileId 파일 식별자
 * @param sourceS3Uri 소스 이미지 S3 URI
 * @param sourceFormat 소스 이미지 포맷
 * @param quality 압축 품질 (nullable, 기본값 90%)
 * @param preserveMetadata 메타데이터 보존 여부
 * @author sangwon-ryu
 */
public record ConvertToWebPCommand(
        FileId fileId,
        String sourceS3Uri,
        ImageFormat sourceFormat,
        CompressionQuality quality,
        boolean preserveMetadata
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ConvertToWebPCommand {
        validateFileId(fileId);
        validateSourceS3Uri(sourceS3Uri);
        validateSourceFormat(sourceFormat);
    }

    /**
     * 기본 설정(품질 90%, 메타데이터 미보존)으로 Command를 생성합니다.
     *
     * @param fileId 파일 식별자
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @return ConvertToWebPCommand 인스턴스
     */
    public static ConvertToWebPCommand withDefaults(
            FileId fileId,
            String sourceS3Uri,
            ImageFormat sourceFormat
    ) {
        return new ConvertToWebPCommand(
                fileId,
                sourceS3Uri,
                sourceFormat,
                CompressionQuality.defaultQuality(),
                false
        );
    }

    /**
     * 압축 품질을 반환합니다.
     * null인 경우 기본값(90%)을 반환합니다.
     *
     * @return 압축 품질
     */
    public CompressionQuality getQualityOrDefault() {
        return quality != null ? quality : CompressionQuality.defaultQuality();
    }

    /**
     * WebP 변환 전략을 결정합니다.
     * 소스 포맷의 특성에 따라 최적 전략을 반환합니다.
     *
     * @return 최적화 전략
     */
    public OptimizationStrategy determineStrategy() {
        // 이미 WebP면 압축만 수행
        if (sourceFormat.isWebP()) {
            return OptimizationStrategy.COMPRESS_ONLY;
        }

        // 투명도를 지원하는 포맷(PNG, GIF)은 무손실 WebP로 변환
        if (sourceFormat.supportsTransparency()) {
            return OptimizationStrategy.CONVERT_TO_WEBP_LOSSLESS;
        }

        // 그 외(JPEG)는 손실 압축 WebP로 변환
        return OptimizationStrategy.CONVERT_TO_WEBP;
    }

    /**
     * WebP로 변환 가능한지 확인합니다.
     *
     * @return WebP 변환 가능 여부
     */
    public boolean isConvertible() {
        return sourceFormat.isConvertibleToWebP();
    }

    // ========== Validation Methods ==========

    private static void validateFileId(FileId fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
    }

    private static void validateSourceS3Uri(String sourceS3Uri) {
        if (sourceS3Uri == null || sourceS3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Source S3 URI cannot be null or empty");
        }
        if (!sourceS3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Source S3 URI must start with 's3://'");
        }
    }

    private static void validateSourceFormat(ImageFormat sourceFormat) {
        if (sourceFormat == null) {
            throw new IllegalArgumentException("Source format cannot be null");
        }
    }
}
