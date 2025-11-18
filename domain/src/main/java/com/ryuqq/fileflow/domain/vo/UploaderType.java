package com.ryuqq.fileflow.domain.vo;

/**
 * UploaderType Enum
 * <p>
 * 업로더 타입을 정의합니다.
 * </p>
 *
 * <p>
 * 업로더 타입 종류:
 * - ADMIN: 커넥틀리 관리자 (자사 상품)
 * - SELLER: 입점 셀러 (회사별)
 * - CUSTOMER: 일반 고객 (리뷰)
 * </p>
 *
 * <p>
 * 사용 예시:
 * - S3 경로 생성 시 UploaderType별 경로 분리
 * - FileCategory 검증 시 UploaderType별 허용 카테고리 확인
 * </p>
 */
public enum UploaderType {

    /**
     * 관리자 (Admin)
     * <p>
     * 커넥틀리 자사 상품 업로드 담당
     * - 허용 카테고리: banner, event, excel, notice, default
     * - S3 경로: uploads/{tenantId}/admin/{uploaderSlug}/{category}/{fileId}_{fileName}
     * </p>
     */
    ADMIN,

    /**
     * 셀러 (Seller)
     * <p>
     * 입점 셀러 상품 업로드 담당
     * - 허용 카테고리: product, review, promotion, default
     * - S3 경로: uploads/{tenantId}/seller/{uploaderSlug}/{category}/{fileId}_{fileName}
     * </p>
     */
    SELLER,

    /**
     * 고객 (Customer)
     * <p>
     * 일반 고객 리뷰 이미지 업로드 담당
     * - 허용 카테고리: default만 허용
     * - S3 경로: uploads/{tenantId}/customer/default/{fileId}_{fileName}
     * </p>
     */
    CUSTOMER
}
