package com.ryuqq.fileflow.application.image.dto;

import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.util.Set;

/**
 * 이미지 압축 Command
 *
 * 이미지를 동일 포맷으로 압축(품질 90%)하기 위한 데이터를 전달하는 Command 객체입니다.
 *
 * 비즈니스 규칙:
 * - FileId는 필수
 * - 소스 S3 URI는 필수
 * - 소스 포맷은 필수 (JPEG, PNG, WebP만 지원)
 * - 압축 품질은 기본값 90%
 * - 메타데이터 보존 여부 선택 가능
 *
 * @param fileId 파일 식별자
 * @param sourceS3Uri 소스 이미지 S3 URI
 * @param sourceFormat 소스 이미지 포맷
 * @param quality 압축 품질 (nullable, 기본값 90%)
 * @param preserveMetadata 메타데이터 보존 여부
 * @author sangwon-ryu
 */
public record CompressImageCommand(
        FileId fileId,
        String sourceS3Uri,
        ImageFormat sourceFormat,
        CompressionQuality quality,
        boolean preserveMetadata
) {

    /**
     * 압축 지원 포맷 집합
     */
    private static final Set<ImageFormat> COMPRESSIBLE_FORMATS = Set.of(
            ImageFormat.JPEG,
            ImageFormat.PNG,
            ImageFormat.WEBP
    );

    /**
     * Compact constructor로 검증 로직 수행
     */
    public CompressImageCommand {
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
     * @return CompressImageCommand 인스턴스
     */
    public static CompressImageCommand withDefaults(
            FileId fileId,
            String sourceS3Uri,
            ImageFormat sourceFormat
    ) {
        return new CompressImageCommand(
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
     * 압축 가능한 포맷인지 확인합니다.
     *
     * @return 압축 가능 여부
     */
    public boolean isCompressible() {
        return COMPRESSIBLE_FORMATS.contains(sourceFormat);
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
        // 압축 지원 포맷 검증
        if (!COMPRESSIBLE_FORMATS.contains(sourceFormat)) {
            throw new IllegalArgumentException(
                    "Unsupported format for compression: " + sourceFormat +
                    ". Only JPEG, PNG, and WebP are supported."
            );
        }
    }
}
