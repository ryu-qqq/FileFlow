package com.ryuqq.fileflow.domain.session.vo;

import java.util.Set;

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
    DOCUMENT("document", "문서"),

    /** HTML 문서 (웹 페이지, 상세 설명 등). */
    HTML("html", "HTML 문서");

    /** 이미지 처리가 필요한 카테고리 집합. */
    private static final Set<UploadCategory> IMAGE_PROCESSING_REQUIRED =
            Set.of(BANNER, PRODUCT_IMAGE, HTML);

    /** CDN 접근이 필요한 카테고리 집합 (uploads/ prefix 경로 사용). */
    private static final Set<UploadCategory> CDN_ACCESS_REQUIRED =
            Set.of(BANNER, PRODUCT_IMAGE, HTML);

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

    /**
     * HTML 문서 카테고리인지 확인한다.
     *
     * @return HTML이면 true
     */
    public boolean isHtml() {
        return this == HTML;
    }

    /**
     * 이미지 처리(리사이징 등)가 필요한 카테고리인지 확인한다.
     *
     * @return BANNER, PRODUCT_IMAGE, HTML이면 true
     */
    public boolean requiresImageProcessing() {
        return IMAGE_PROCESSING_REQUIRED.contains(this);
    }

    /**
     * CDN 접근이 필요한 카테고리인지 확인한다.
     *
     * <p>CDN(cdn.set-of.com)을 통해 공개 접근이 가능해야 하는 카테고리입니다. {@code uploads/} prefix 경로에 저장되어
     * CloudFront를 통해 접근 가능합니다.
     *
     * <p><strong>CDN 접근 필요</strong>: BANNER, PRODUCT_IMAGE, HTML
     *
     * <p><strong>내부 전용</strong>: EXCEL, SALES_MATERIAL, DOCUMENT (internal/ prefix)
     *
     * @return CDN 접근이 필요하면 true
     */
    public boolean requiresCdnAccess() {
        return CDN_ACCESS_REQUIRED.contains(this);
    }
}
