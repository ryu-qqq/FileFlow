package com.ryuqq.fileflow.domain.session.vo;

/**
 * 파일 업로드 카테고리 정의.
 *
 * <p><strong>적용 대상</strong>:
 *
 * <ul>
 *   <li>ADMIN: 모든 카테고리 사용 가능
 *   <li>SELLER: 모든 카테고리 사용 가능
 *   <li>CUSTOMER: 카테고리 사용하지 않음 (경로에 포함 안 됨)
 * </ul>
 *
 * <p><strong>S3 경로 예시</strong>:
 *
 * <ul>
 *   <li>BANNER: admin/banner/2025/11/hero.jpg
 *   <li>EXCEL: seller-1/excel/2025/11/products.xlsx
 * </ul>
 */
public enum UploadCategory {
    /** 배너 이미지 (홈페이지, 프로모션 등). */
    BANNER("banner", "배너 이미지"),

    /** 엑셀 데이터 파일 (재고, 상품 목록 등). */
    EXCEL("excel", "엑셀 데이터"),

    /** 판매 자료 (카탈로그, 브로슈어 등). */
    SALES_MATERIAL("sales", "판매 자료"),

    /** 상품 이미지 (메인, 상세 등). */
    PRODUCT_IMAGE("product", "상품 이미지"),

    /** 일반 문서 (계약서, 공지사항 등). */
    DOCUMENT("document", "문서");

    private final String path;
    private final String description;

    UploadCategory(String path, String description) {
        this.path = path;
        this.description = description;
    }

    /**
     * S3 경로에 사용될 문자열을 반환한다.
     *
     * @return 경로 문자열 (예: "banner", "excel")
     */
    public String getPath() {
        return path;
    }

    /**
     * 카테고리 설명을 반환한다.
     *
     * @return 설명 (예: "배너 이미지", "엑셀 데이터")
     */
    public String getDescription() {
        return description;
    }

    /**
     * 경로 문자열로부터 UploadCategory를 찾는다.
     *
     * @param path 경로 문자열 (예: "banner", "excel")
     * @return UploadCategory
     * @throws IllegalArgumentException 일치하는 카테고리가 없는 경우
     */
    public static UploadCategory fromPath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("경로 문자열은 null이거나 빈 문자열일 수 없습니다.");
        }

        for (UploadCategory category : values()) {
            if (category.path.equalsIgnoreCase(path)) {
                return category;
            }
        }

        // 가능한 값 목록 생성
        String validValues =
                String.join(
                        ", ",
                        java.util.Arrays.stream(values())
                                .map(UploadCategory::name)
                                .toArray(String[]::new));

        throw new IllegalArgumentException(
                "유효하지 않은 업로드 카테고리입니다. 입력값: '" + path + "', 가능한 값: " + validValues);
    }

    /**
     * 배너 이미지 카테고리인지 확인한다.
     *
     * @return BANNER이면 true
     */
    public boolean isBanner() {
        return this == BANNER;
    }

    /**
     * 엑셀 파일 카테고리인지 확인한다.
     *
     * @return EXCEL이면 true
     */
    public boolean isExcel() {
        return this == EXCEL;
    }

    /**
     * 이미지 카테고리인지 확인한다 (배너, 상품 이미지).
     *
     * @return 이미지 카테고리이면 true
     */
    public boolean isImage() {
        return this == BANNER || this == PRODUCT_IMAGE;
    }
}
