package com.ryuqq.fileflow.application.asset.dto.processor;

import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageResizingSpec;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedImageInfo;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;

/**
 * 업로드된 이미지 결과.
 *
 * <p>리사이징 및 S3 업로드 결과를 담는 Application Layer DTO입니다.
 *
 * <p><strong>설계 결정:</strong>
 *
 * <ul>
 *   <li>dimension (이미지 크기): 저장하지 않음 - ImageVariant 스펙에서 결정됨
 *   <li>colorSpace (색 공간): 저장하지 않음 - 가공 시 항상 RGB로 변환됨
 *   <li>fileSize만 저장 - 압축 결과에 따라 실제 크기가 달라짐
 * </ul>
 *
 * <p><strong>구조</strong>:
 *
 * <ul>
 *   <li>ProcessedImageInfo - 도메인 정보 (spec, 파일 사이즈)
 *   <li>S3 저장 정보 - Infrastructure 정보 (bucket, key, etag)
 *   <li>ImageProcessingResultResponse - 원시 데이터 (옵션, 필요 시만)
 * </ul>
 *
 * @param imageInfo 처리된 이미지 도메인 정보
 * @param bucket S3 버킷
 * @param s3Key S3 키
 * @param etag 업로드된 객체의 ETag
 * @param result 이미지 처리 결과 (데이터 포함, 선택적)
 */
public record UploadedImage(
        ProcessedImageInfo imageInfo,
        S3Bucket bucket,
        S3Key s3Key,
        ETag etag,
        ImageProcessingResultResponse result) {

    /** Compact Constructor (검증 로직). */
    public UploadedImage {
        if (imageInfo == null) {
            throw new IllegalArgumentException("ProcessedImageInfo는 null일 수 없습니다.");
        }
        if (bucket == null) {
            throw new IllegalArgumentException("S3Bucket은 null일 수 없습니다.");
        }
        if (s3Key == null) {
            throw new IllegalArgumentException("S3Key는 null일 수 없습니다.");
        }
        if (etag == null) {
            throw new IllegalArgumentException("ETag는 null일 수 없습니다.");
        }
        // result는 null 허용 (데이터가 필요 없는 경우)
    }

    /**
     * 정적 팩토리 메서드 (ProcessedImageInfo 기반).
     *
     * @param imageInfo 처리된 이미지 도메인 정보
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param etag ETag
     * @param result 이미지 처리 결과 (선택적)
     * @return UploadedImage
     */
    public static UploadedImage of(
            ProcessedImageInfo imageInfo,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            ImageProcessingResultResponse result) {
        return new UploadedImage(imageInfo, bucket, s3Key, etag, result);
    }

    /**
     * 정적 팩토리 메서드 (ImageProcessingResultResponse로부터 생성).
     *
     * <p>ImageProcessingResultResponse에서 파일 크기를 추출하여 ProcessedImageInfo를 생성합니다. dimension과
     * colorSpace는 저장하지 않습니다 (스펙에서 결정됨).
     *
     * @param spec 리사이징 명세
     * @param result 이미지 처리 결과
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param etag ETag
     * @return UploadedImage
     */
    public static UploadedImage fromResult(
            ImageResizingSpec spec,
            ImageProcessingResultResponse result,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag) {

        ProcessedImageInfo imageInfo = ProcessedImageInfo.of(spec, result.size());

        return new UploadedImage(imageInfo, bucket, s3Key, etag, result);
    }

    /**
     * 이미지 변형 타입을 반환합니다.
     *
     * @return ImageVariant
     */
    public ImageVariant variant() {
        return imageInfo.variant();
    }

    /**
     * 이미지 포맷을 반환합니다.
     *
     * @return ImageFormat
     */
    public ImageFormat format() {
        return imageInfo.format();
    }

    /**
     * 리사이징 명세를 반환합니다.
     *
     * @return ImageResizingSpec
     */
    public ImageResizingSpec spec() {
        return imageInfo.spec();
    }

    /**
     * 결과 식별자를 반환합니다.
     *
     * @return "variant_format" 형식의 식별자
     */
    public String resultId() {
        return imageInfo.infoId();
    }

    /**
     * 파일 크기를 반환합니다.
     *
     * @return 파일 크기 (바이트)
     */
    public long fileSize() {
        return imageInfo.fileSize();
    }
}
