package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.iam.vo.FileId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;

/**
 * S3 Object Key Value Object
 * <p>
 * UploaderType별 S3 경로 생성을 담당합니다.
 * </p>
 *
 * <p>
 * UploaderType별 경로 패턴:
 * - ADMIN: uploads/{tenantId}/admin/{uploaderSlug}/{category}/{fileId}_{fileName}
 * - SELLER: uploads/{tenantId}/seller/{uploaderSlug}/{category}/{fileId}_{fileName}
 * - CUSTOMER: uploads/{tenantId}/customer/default/{fileId}_{fileName}
 * </p>
 *
 * <p>
 * 예시:
 * - Admin: uploads/1/admin/connectly/banner/01JD8001-1234-5678-9abc-def012345678_메인배너.jpg
 * - Seller: uploads/1/seller/samsung-electronics/product/01JD8010-5678-1234-abcd-ef0123456789_갤럭시.jpg
 * - Customer: uploads/1/customer/default/01JD8100-9abc-def0-1234-567890abcdef_리뷰.jpg
 * </p>
 */
public record S3Key(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * S3 Key는 null이거나 빈 값일 수 없습니다.
     * </p>
     */
    public S3Key {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("S3 Key는 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * UploaderType별 S3 Object Key 생성
     *
     * @param tenantId 테넌트 ID
     * @param uploaderType 업로더 타입 (ADMIN, SELLER, CUSTOMER)
     * @param uploaderSlug 업로더 슬러그 (예: "connectly", "samsung-electronics")
     * @param category 파일 카테고리
     * @param fileId 파일 ID (UUID v7)
     * @param fileName 파일명
     * @return S3Key VO
     */
    public static S3Key generate(
            TenantId tenantId,
            UploaderType uploaderType,
            String uploaderSlug,
            FileCategory category,
            FileId fileId,
            FileName fileName
    ) {
        String key;

        if (uploaderType == UploaderType.ADMIN || uploaderType == UploaderType.SELLER) {
            // Admin, Seller: uploads/{tenantId}/{uploaderType}/{uploaderSlug}/{category}/{fileId}_{fileName}
            key = String.format(
                    "uploads/%d/%s/%s/%s/%s_%s",
                    tenantId.value(),
                    uploaderType.name().toLowerCase(),
                    uploaderSlug,
                    category.getValue(),
                    fileId.getValue(),
                    fileName.getValue()
            );
        } else {
            // Customer: uploads/{tenantId}/customer/default/{fileId}_{fileName}
            key = String.format(
                    "uploads/%d/customer/default/%s_%s",
                    tenantId.value(),
                    fileId.getValue(),
                    fileName.getValue()
            );
        }

        return new S3Key(key);
    }

    /**
     * S3 Key 원시 값 조회
     *
     * @return S3 Key 문자열
     */
    public String getValue() {
        return value;
    }
}
