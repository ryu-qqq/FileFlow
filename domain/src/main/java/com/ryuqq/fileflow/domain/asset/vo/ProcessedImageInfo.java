package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 처리된 이미지 정보.
 *
 * <p>이미지 리사이징 결과의 도메인 표현입니다. 기술적 의존성(Port) 없이 순수 도메인 정보만 포함합니다.
 *
 * <p><strong>설계 결정:</strong>
 *
 * <ul>
 *   <li>dimension (이미지 크기): 저장하지 않음 - ImageVariant 스펙에서 결정됨
 *   <li>colorSpace (색 공간): 저장하지 않음 - 가공 시 항상 RGB로 변환됨
 *   <li>fileSize만 저장 - 압축 결과에 따라 실제 크기가 달라짐
 * </ul>
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>순수 도메인 VO (Infrastructure 의존성 없음)
 *   <li>Application Layer의 UploadedImage와 분리
 *   <li>도메인 로직에서 사용 가능
 * </ul>
 *
 * @param spec 리사이징 명세 (variant + format)
 * @param fileSize 처리된 이미지 파일 크기 (바이트) - 압축 결과에 따라 달라짐
 */
public record ProcessedImageInfo(ImageResizingSpec spec, long fileSize) {

    /** Compact Constructor (검증 로직). */
    public ProcessedImageInfo {
        if (spec == null) {
            throw new IllegalArgumentException("리사이징 명세는 null일 수 없습니다.");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다: " + fileSize);
        }
    }

    /**
     * 정적 팩토리 메서드.
     *
     * @param spec 리사이징 명세
     * @param fileSize 파일 크기
     * @return ProcessedImageInfo
     */
    public static ProcessedImageInfo of(ImageResizingSpec spec, long fileSize) {
        return new ProcessedImageInfo(spec, fileSize);
    }

    /**
     * 이미지 변형 타입을 반환합니다.
     *
     * @return ImageVariant
     */
    public ImageVariant variant() {
        return spec.variant();
    }

    /**
     * 이미지 포맷을 반환합니다.
     *
     * @return ImageFormat
     */
    public ImageFormat format() {
        return spec.format();
    }

    /**
     * 처리 결과 식별자를 반환합니다.
     *
     * @return "variant_format" 형식의 식별자
     */
    public String infoId() {
        return spec.specId();
    }

    /**
     * 파일 크기를 KB 단위로 반환합니다.
     *
     * @return 파일 크기 (KB)
     */
    public double fileSizeKb() {
        return fileSize / 1024.0;
    }
}
