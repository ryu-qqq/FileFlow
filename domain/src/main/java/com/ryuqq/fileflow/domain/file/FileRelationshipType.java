package com.ryuqq.fileflow.domain.file;

/**
 * 파일 간 관계 유형을 정의하는 Enum
 *
 * 비즈니스 규칙:
 * - 파일 파생 관계를 명확히 분류합니다
 * - 각 관계 유형은 고유한 의미와 용도를 가집니다
 * - 새로운 관계 유형은 신중히 추가해야 합니다 (하위 호환성 고려)
 *
 * @author sangwon-ryu
 */
public enum FileRelationshipType {

    /**
     * 썸네일 관계
     * - 원본 이미지의 축소된 버전
     * - 주로 목록 표시나 미리보기 용도
     * - 예: 원본 1920x1080 → 썸네일 200x200
     */
    THUMBNAIL("thumbnail", "썸네일 관계"),

    /**
     * 최적화된 파일
     * - 품질과 크기가 최적화된 버전
     * - 웹 전송 효율성 향상 목적
     * - 예: 원본 10MB PNG → 최적화된 2MB PNG
     */
    OPTIMIZED("optimized", "최적화된 파일"),

    /**
     * 변환된 파일
     * - 다른 포맷으로 변환된 버전
     * - 브라우저 호환성이나 성능 향상 목적
     * - 예: PNG → WebP, MP4 → HLS
     */
    CONVERTED("converted", "변환된 파일"),

    /**
     * 파생 파일
     * - 원본에서 파생된 추가 가공 버전
     * - 특정 비즈니스 요구사항으로 생성
     * - 예: 워터마크 적용, 크롭 버전
     */
    DERIVATIVE("derivative", "파생 파일"),

    /**
     * 버전 관계
     * - 동일 파일의 다른 버전
     * - 파일 업데이트나 수정 이력 추적
     * - 예: v1 → v2 → v3
     */
    VERSION("version", "버전 관계");

    private final String code;
    private final String description;

    FileRelationshipType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 코드 문자열로부터 FileRelationshipType을 조회합니다.
     *
     * @param code 관계 유형 코드
     * @return FileRelationshipType
     * @throws IllegalArgumentException 유효하지 않은 코드인 경우
     */
    public static FileRelationshipType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Relationship type code cannot be null");
        }

        for (FileRelationshipType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown relationship type code: " + code);
    }

    /**
     * 썸네일 관계인지 확인합니다.
     *
     * @return 썸네일 관계이면 true
     */
    public boolean isThumbnail() {
        return this == THUMBNAIL;
    }

    /**
     * 파일 변환 관계인지 확인합니다 (OPTIMIZED, CONVERTED).
     *
     * @return 변환 관계이면 true
     */
    public boolean isTransformation() {
        return this == OPTIMIZED || this == CONVERTED;
    }

    /**
     * 파일 파생 관계인지 확인합니다.
     *
     * @return 파생 관계이면 true
     */
    public boolean isDerivative() {
        return this == DERIVATIVE;
    }

    /**
     * 버전 관계인지 확인합니다.
     *
     * @return 버전 관계이면 true
     */
    public boolean isVersion() {
        return this == VERSION;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
