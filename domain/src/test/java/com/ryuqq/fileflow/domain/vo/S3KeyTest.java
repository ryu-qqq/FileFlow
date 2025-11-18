package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.iam.vo.FileId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S3Key Value Object 테스트
 * <p>
 * UploaderType별 S3 Object Key 생성 검증
 * </p>
 */
@DisplayName("S3Key Value Object 테스트")
class S3KeyTest {

    @Test
    @DisplayName("Admin 업로더의 S3 경로를 생성해야 한다")
    void shouldGenerateAdminS3Key() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.ADMIN;
        String uploaderSlug = "connectly";
        FileCategory category = FileCategory.of("banner", UploaderType.ADMIN);
        FileId fileId = FileId.of("01JD8001-1234-5678-9abc-def012345678");
        FileName fileName = FileName.of("메인배너.jpg");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);

        // then
        assertThat(s3Key).isNotNull();
        assertThat(s3Key.getValue()).isEqualTo(
                "uploads/1/admin/connectly/banner/01JD8001-1234-5678-9abc-def012345678_메인배너.jpg"
        );
    }

    @Test
    @DisplayName("Seller 업로더의 S3 경로를 생성해야 한다")
    void shouldGenerateSellerS3Key() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.SELLER;
        String uploaderSlug = "samsung-electronics";
        FileCategory category = FileCategory.of("product", UploaderType.SELLER);
        FileId fileId = FileId.of("01JD8010-5678-1234-abcd-ef0123456789");
        FileName fileName = FileName.of("갤럭시.jpg");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);

        // then
        assertThat(s3Key).isNotNull();
        assertThat(s3Key.getValue()).isEqualTo(
                "uploads/1/seller/samsung-electronics/product/01JD8010-5678-1234-abcd-ef0123456789_갤럭시.jpg"
        );
    }

    @Test
    @DisplayName("Customer 업로더의 S3 경로를 생성해야 한다 (uploaderSlug 무시)")
    void shouldGenerateCustomerS3Key() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.CUSTOMER;
        String uploaderSlug = "ignored-slug";  // Customer는 uploaderSlug 무시
        FileCategory category = FileCategory.of("default", UploaderType.CUSTOMER);
        FileId fileId = FileId.of("01JD8100-9abc-def0-1234-567890abcdef");
        FileName fileName = FileName.of("리뷰.jpg");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);

        // then
        assertThat(s3Key).isNotNull();
        assertThat(s3Key.getValue()).isEqualTo(
                "uploads/1/customer/default/01JD8100-9abc-def0-1234-567890abcdef_리뷰.jpg"
        );
    }

    @Test
    @DisplayName("Admin 업로더는 다양한 카테고리 경로를 생성해야 한다")
    void shouldGenerateAdminS3KeyWithDifferentCategories() {
        // given
        TenantId tenantId = TenantId.of(2L);
        UploaderType uploaderType = UploaderType.ADMIN;
        String uploaderSlug = "connectly";
        FileCategory eventCategory = FileCategory.of("event", UploaderType.ADMIN);
        FileId fileId = FileId.of("01JD8002-1234-5678-9abc-def012345678");
        FileName fileName = FileName.of("이벤트배너.png");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, eventCategory, fileId, fileName);

        // then
        assertThat(s3Key.getValue()).isEqualTo(
                "uploads/2/admin/connectly/event/01JD8002-1234-5678-9abc-def012345678_이벤트배너.png"
        );
    }

    @Test
    @DisplayName("Seller 업로더는 다양한 카테고리 경로를 생성해야 한다")
    void shouldGenerateSellerS3KeyWithDifferentCategories() {
        // given
        TenantId tenantId = TenantId.of(3L);
        UploaderType uploaderType = UploaderType.SELLER;
        String uploaderSlug = "lg-electronics";
        FileCategory reviewCategory = FileCategory.of("review", UploaderType.SELLER);
        FileId fileId = FileId.of("01JD8020-5678-1234-abcd-ef0123456789");
        FileName fileName = FileName.of("사용자리뷰.jpg");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, reviewCategory, fileId, fileName);

        // then
        assertThat(s3Key.getValue()).isEqualTo(
                "uploads/3/seller/lg-electronics/review/01JD8020-5678-1234-abcd-ef0123456789_사용자리뷰.jpg"
        );
    }

    @Test
    @DisplayName("getValue()는 생성된 S3 경로를 반환해야 한다")
    void shouldReturnS3KeyValue() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.ADMIN;
        String uploaderSlug = "connectly";
        FileCategory category = FileCategory.of("banner", UploaderType.ADMIN);
        FileId fileId = FileId.of("01JD8001-1234-5678-9abc-def012345678");
        FileName fileName = FileName.of("test.jpg");

        // when
        S3Key s3Key = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);
        String value = s3Key.getValue();

        // then
        assertThat(value).isNotBlank();
        assertThat(value).startsWith("uploads/");
        assertThat(value).contains("test.jpg");
    }

    @Test
    @DisplayName("같은 파라미터로 생성된 S3Key는 동등해야 한다")
    void shouldBeEqualWhenGeneratedWithSameParameters() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.ADMIN;
        String uploaderSlug = "connectly";
        FileCategory category = FileCategory.of("banner", UploaderType.ADMIN);
        FileId fileId = FileId.of("01JD8001-1234-5678-9abc-def012345678");
        FileName fileName = FileName.of("test.jpg");

        // when
        S3Key s3Key1 = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);
        S3Key s3Key2 = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);

        // then
        assertThat(s3Key1).isEqualTo(s3Key2);
    }

    @Test
    @DisplayName("같은 파라미터로 생성된 S3Key는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenGeneratedWithSameParameters() {
        // given
        TenantId tenantId = TenantId.of(1L);
        UploaderType uploaderType = UploaderType.SELLER;
        String uploaderSlug = "samsung";
        FileCategory category = FileCategory.of("product", UploaderType.SELLER);
        FileId fileId = FileId.of("01JD8010-5678-1234-abcd-ef0123456789");
        FileName fileName = FileName.of("product.jpg");

        // when
        S3Key s3Key1 = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);
        S3Key s3Key2 = S3Key.generate(tenantId, uploaderType, uploaderSlug, category, fileId, fileName);

        // then
        assertThat(s3Key1.hashCode()).isEqualTo(s3Key2.hashCode());
    }
}
