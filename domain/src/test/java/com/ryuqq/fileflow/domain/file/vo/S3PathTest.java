package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.session.vo.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("S3Path VO Tests")
class S3PathTest {

    @Test
    @DisplayName("ADMIN Role로 S3Path를 생성할 수 있어야 한다")
    void shouldCreateAdminPath() {
        S3Path s3Path = S3Path.from(
            UserRole.ADMIN,
            123L,
            null,
            "uploads",
            "file-123",
            "image/jpeg"
        );

        assertThat(s3Path.tenantId()).isEqualTo(123L);
        assertThat(s3Path.namespace()).isEqualTo("connectly");
        assertThat(s3Path.sellerName()).isEqualTo("default");
        assertThat(s3Path.customPath()).isEqualTo("uploads");
        assertThat(s3Path.fileId()).isEqualTo("file-123");
        assertThat(s3Path.extension()).isEqualTo(".jpg");
    }

    @Test
    @DisplayName("SELLER Role로 S3Path를 생성할 수 있어야 한다")
    void shouldCreateSellerPath() {
        S3Path s3Path = S3Path.from(
            UserRole.SELLER,
            456L,
            "seller1",
            "products",
            "file-456",
            "image/png"
        );

        assertThat(s3Path.tenantId()).isEqualTo(456L);
        assertThat(s3Path.namespace()).isEqualTo("setof");
        assertThat(s3Path.sellerName()).isEqualTo("seller1");
        assertThat(s3Path.customPath()).isEqualTo("products");
        assertThat(s3Path.fileId()).isEqualTo("file-456");
        assertThat(s3Path.extension()).isEqualTo(".png");
    }

    @Test
    @DisplayName("DEFAULT Role로 S3Path를 생성할 수 있어야 한다")
    void shouldCreateDefaultPath() {
        S3Path s3Path = S3Path.from(
            UserRole.DEFAULT,
            789L,
            "defaultSeller",
            "files",
            "file-789",
            "text/html"
        );

        assertThat(s3Path.tenantId()).isEqualTo(789L);
        assertThat(s3Path.namespace()).isEqualTo("setof");
        assertThat(s3Path.sellerName()).isEqualTo("defaultSeller");
        assertThat(s3Path.customPath()).isEqualTo("files");
        assertThat(s3Path.fileId()).isEqualTo("file-789");
        assertThat(s3Path.extension()).isEqualTo(".html");
    }

    @Test
    @DisplayName("MIME 타입에서 확장자를 올바르게 추출해야 한다")
    void shouldExtractExtensionFromMimeType() {
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "image/jpeg").extension()).isEqualTo(".jpg");
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "image/png").extension()).isEqualTo(".png");
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "image/gif").extension()).isEqualTo(".gif");
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "image/webp").extension()).isEqualTo(".webp");
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "text/html").extension()).isEqualTo(".html");
        assertThat(S3Path.from(UserRole.ADMIN, 1L, null, "path", "file", "unknown/type").extension()).isEqualTo("");
    }

    @Test
    @DisplayName("전체 S3 경로를 올바르게 생성해야 한다")
    void shouldGenerateFullPath() {
        S3Path adminPath = S3Path.from(UserRole.ADMIN, 123L, null, "uploads", "file-123", "image/jpeg");
        assertThat(adminPath.getFullPath()).isEqualTo("123/connectly/uploads/file-123.jpg");

        S3Path sellerPath = S3Path.from(UserRole.SELLER, 456L, "seller1", "products", "file-456", "image/png");
        assertThat(sellerPath.getFullPath()).isEqualTo("456/setof/seller1/products/file-456.png");
    }

    @Test
    @DisplayName("tenantId가 null이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenTenantIdIsNull() {
        assertThatThrownBy(() -> 
            new S3Path(null, "namespace", "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("tenantId는 필수입니다");
    }

    @Test
    @DisplayName("tenantId가 0 이하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenTenantIdIsZeroOrNegative() {
        assertThatThrownBy(() -> 
            new S3Path(0L, "namespace", "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("tenantId는 필수입니다");

        assertThatThrownBy(() -> 
            new S3Path(-1L, "namespace", "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("tenantId는 필수입니다");
    }

    @Test
    @DisplayName("namespace가 null이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenNamespaceIsNull() {
        assertThatThrownBy(() -> 
            new S3Path(1L, null, "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("namespace는 필수입니다");
    }

    @Test
    @DisplayName("namespace가 빈 문자열이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenNamespaceIsBlank() {
        assertThatThrownBy(() -> 
            new S3Path(1L, "", "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("namespace는 필수입니다");

        assertThatThrownBy(() -> 
            new S3Path(1L, "   ", "seller", "path", "file", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("namespace는 필수입니다");
    }

    @Test
    @DisplayName("fileId가 null이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenFileIdIsNull() {
        assertThatThrownBy(() -> 
            new S3Path(1L, "namespace", "seller", "path", null, ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("fileId는 필수입니다");
    }

    @Test
    @DisplayName("fileId가 빈 문자열이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenFileIdIsBlank() {
        assertThatThrownBy(() -> 
            new S3Path(1L, "namespace", "seller", "path", "", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("fileId는 필수입니다");

        assertThatThrownBy(() -> 
            new S3Path(1L, "namespace", "seller", "path", "   ", ".ext")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("fileId는 필수입니다");
    }
}

