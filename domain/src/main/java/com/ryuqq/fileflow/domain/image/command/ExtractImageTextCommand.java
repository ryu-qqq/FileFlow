package com.ryuqq.fileflow.domain.image.command;

import com.ryuqq.fileflow.domain.image.vo.ImageFormat;

import java.util.Set;

/**
 * 이미지 텍스트 추출 명령을 나타내는 Command Object
 * AWS Textract를 사용하여 이미지에서 텍스트를 추출합니다.
 *
 * CQRS 패턴:
 * - OCR 요청의 의도를 명시적으로 표현
 * - 불변 객체로 안전한 전달 보장
 *
 * 비즈니스 규칙:
 * - 상품 정보 파싱 (재질, 원산지, 세탁방법, 사이즈)
 * - 특정 정보 타입만 추출 가능
 */
public record ExtractImageTextCommand(
        String imageId,
        String sourceS3Uri,
        ImageFormat sourceFormat,
        Set<TextExtractionType> extractionTypes,
        String languageCode
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ExtractImageTextCommand {
        CommandValidators.validateImageId(imageId);
        CommandValidators.validateSourceS3Uri(sourceS3Uri);
        validateSourceFormat(sourceFormat);
        validateExtractionTypes(extractionTypes);
        validateLanguageCode(languageCode);
    }

    /**
     * ExtractImageTextCommand를 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param extractionTypes 추출할 정보 타입
     * @param languageCode 언어 코드 (예: "ko", "en")
     * @return ExtractImageTextCommand 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ExtractImageTextCommand of(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            Set<TextExtractionType> extractionTypes,
            String languageCode
    ) {
        return new ExtractImageTextCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                extractionTypes,
                languageCode
        );
    }

    /**
     * 모든 정보를 추출하는 명령을 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param languageCode 언어 코드
     * @return ExtractImageTextCommand 인스턴스
     */
    public static ExtractImageTextCommand extractAll(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            String languageCode
    ) {
        return new ExtractImageTextCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                Set.of(TextExtractionType.values()),
                languageCode
        );
    }

    /**
     * 한국어로 모든 정보를 추출하는 명령을 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @return ExtractImageTextCommand 인스턴스
     */
    public static ExtractImageTextCommand extractAllKorean(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat
    ) {
        return extractAll(imageId, sourceS3Uri, sourceFormat, "ko");
    }

    /**
     * 특정 타입의 정보만 추출하는지 확인합니다.
     *
     * @param type 확인할 추출 타입
     * @return 해당 타입을 추출하면 true
     */
    public boolean shouldExtract(TextExtractionType type) {
        return extractionTypes.contains(type);
    }

    // ========== Validation Methods ==========

    private static void validateSourceFormat(ImageFormat sourceFormat) {
        if (sourceFormat == null) {
            throw new IllegalArgumentException("Source format cannot be null");
        }
    }

    private static void validateExtractionTypes(Set<TextExtractionType> extractionTypes) {
        if (extractionTypes == null || extractionTypes.isEmpty()) {
            throw new IllegalArgumentException("Extraction types cannot be null or empty");
        }
    }

    private static void validateLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language code cannot be null or empty");
        }
        if (languageCode.length() < 2 || languageCode.length() > 5) {
            throw new IllegalArgumentException("Language code must be between 2 and 5 characters (e.g., 'ko', 'zh-CN')");
        }
    }

    /**
     * 추출할 정보 타입을 정의하는 Enum
     */
    public enum TextExtractionType {
        /**
         * 재질 정보
         */
        MATERIAL("Material information"),

        /**
         * 원산지 정보
         */
        COUNTRY_OF_ORIGIN("Country of origin"),

        /**
         * 세탁 방법
         */
        CARE_INSTRUCTIONS("Care instructions"),

        /**
         * 사이즈 정보
         */
        SIZE("Size information"),

        /**
         * 기타 텍스트
         */
        OTHER("Other text");

        private final String description;

        TextExtractionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
