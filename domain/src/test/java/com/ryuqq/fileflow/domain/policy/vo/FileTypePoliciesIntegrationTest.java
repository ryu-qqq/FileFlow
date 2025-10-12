package com.ryuqq.fileflow.domain.policy.vo;

import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileTypePolicies 통합 테스트 - 커버리지 향상을 위한 추가 테스트
 */
@DisplayName("FileTypePolicies 통합 테스트")
class FileTypePoliciesIntegrationTest {

    @Test
    @DisplayName("HTML 정책 검증 - 정상 케이스")
    void validateHtmlPolicy() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("html")
                .sizeBytes(5L * 1024 * 1024)
                .imageCount(50)
                .build();

        // when & then
        policies.validate(FileType.HTML, attributes);
    }

    @Test
    @DisplayName("HTML 정책 검증 - imageCount null 처리")
    void validateHtmlPolicyWithNullImageCount() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("html")
                .sizeBytes(5L * 1024 * 1024)
                .build();

        // when & then
        policies.validate(FileType.HTML, attributes);
    }

    @Test
    @DisplayName("HTML 정책이 없는데 검증하면 예외 발생")
    void validateHtmlPolicyNotConfigured() {
        // given
        ImagePolicy imagePolicy = new ImagePolicy(10, 5,
                Arrays.asList("jpg"), Dimension.of(1920, 1080));
        FileTypePolicies policies = FileTypePolicies.of(imagePolicy, null, null, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("html")
                .sizeBytes(5L * 1024 * 1024)
                .build();

        // when & then
        assertThatThrownBy(() -> policies.validate(FileType.HTML, attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No HTML policy configured");
    }

    @Test
    @DisplayName("EXCEL 정책 검증 - 정상 케이스")
    void validateExcelPolicy() {
        // given
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        FileTypePolicies policies = FileTypePolicies.of(null, null, excelPolicy, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("xlsx")
                .sizeBytes(10L * 1024 * 1024)
                .sheetCount(5)
                .build();

        // when & then
        policies.validate(FileType.EXCEL, attributes);
    }

    @Test
    @DisplayName("EXCEL 정책 검증 - sheetCount null 처리")
    void validateExcelPolicyWithNullSheetCount() {
        // given
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        FileTypePolicies policies = FileTypePolicies.of(null, null, excelPolicy, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("xlsx")
                .sizeBytes(10L * 1024 * 1024)
                .build();

        // when & then
        policies.validate(FileType.EXCEL, attributes);
    }

    @Test
    @DisplayName("EXCEL 정책이 없는데 검증하면 예외 발생")
    void validateExcelPolicyNotConfigured() {
        // given
        ImagePolicy imagePolicy = new ImagePolicy(10, 5,
                Arrays.asList("jpg"), Dimension.of(1920, 1080));
        FileTypePolicies policies = FileTypePolicies.of(imagePolicy, null, null, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("xlsx")
                .sizeBytes(10L * 1024 * 1024)
                .build();

        // when & then
        assertThatThrownBy(() -> policies.validate(FileType.EXCEL, attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No EXCEL policy configured");
    }

    @Test
    @DisplayName("PDF 정책 검증 - 정상 케이스")
    void validatePdfPolicy() {
        // given
        PdfPolicy pdfPolicy = new PdfPolicy(15, 200);
        FileTypePolicies policies = FileTypePolicies.of(null, null, null, pdfPolicy);
        FileAttributes attributes = FileAttributes.builder()
                .format("pdf")
                .sizeBytes(8L * 1024 * 1024)
                .pageCount(100)
                .build();

        // when & then
        policies.validate(FileType.PDF, attributes);
    }

    @Test
    @DisplayName("PDF 정책 검증 - pageCount null 처리")
    void validatePdfPolicyWithNullPageCount() {
        // given
        PdfPolicy pdfPolicy = new PdfPolicy(15, 200);
        FileTypePolicies policies = FileTypePolicies.of(null, null, null, pdfPolicy);
        FileAttributes attributes = FileAttributes.builder()
                .format("pdf")
                .sizeBytes(8L * 1024 * 1024)
                .build();

        // when & then
        policies.validate(FileType.PDF, attributes);
    }

    @Test
    @DisplayName("PDF 정책이 없는데 검증하면 예외 발생")
    void validatePdfPolicyNotConfigured() {
        // given
        ImagePolicy imagePolicy = new ImagePolicy(10, 5,
                Arrays.asList("jpg"), Dimension.of(1920, 1080));
        FileTypePolicies policies = FileTypePolicies.of(imagePolicy, null, null, null);
        FileAttributes attributes = FileAttributes.builder()
                .format("pdf")
                .sizeBytes(8L * 1024 * 1024)
                .build();

        // when & then
        assertThatThrownBy(() -> policies.validate(FileType.PDF, attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No PDF policy configured");
    }

    @Test
    @DisplayName("getPolicyFor - HTML 정책 조회")
    void getPolicyForHtml() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when
        Object policy = policies.getPolicyFor(FileType.HTML);

        // then
        assertThat(policy).isEqualTo(htmlPolicy);
    }

    @Test
    @DisplayName("getPolicyFor - EXCEL 정책 조회")
    void getPolicyForExcel() {
        // given
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        FileTypePolicies policies = FileTypePolicies.of(null, null, excelPolicy, null);

        // when
        Object policy = policies.getPolicyFor(FileType.EXCEL);

        // then
        assertThat(policy).isEqualTo(excelPolicy);
    }

    @Test
    @DisplayName("Getter - getHtmlPolicy")
    void getHtmlPolicy() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when & then
        assertThat(policies.getHtmlPolicy()).isEqualTo(htmlPolicy);
    }

    @Test
    @DisplayName("Getter - getExcelPolicy")
    void getExcelPolicy() {
        // given
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        FileTypePolicies policies = FileTypePolicies.of(null, null, excelPolicy, null);

        // when & then
        assertThat(policies.getExcelPolicy()).isEqualTo(excelPolicy);
    }

    @Test
    @DisplayName("Getter - getPdfPolicy")
    void getPdfPolicy() {
        // given
        PdfPolicy pdfPolicy = new PdfPolicy(15, 200);
        FileTypePolicies policies = FileTypePolicies.of(null, null, null, pdfPolicy);

        // when & then
        assertThat(policies.getPdfPolicy()).isEqualTo(pdfPolicy);
    }

    @Test
    @DisplayName("Getter - getImagePolicy")
    void getImagePolicy() {
        // given
        ImagePolicy imagePolicy = new ImagePolicy(10, 5,
                Arrays.asList("jpg"), Dimension.of(1920, 1080));
        FileTypePolicies policies = FileTypePolicies.of(imagePolicy, null, null, null);

        // when & then
        assertThat(policies.getImagePolicy()).isEqualTo(imagePolicy);
    }

    @Test
    @DisplayName("size - 여러 정책의 개수 계산")
    void sizeWithMultiplePolicies() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        PdfPolicy pdfPolicy = new PdfPolicy(15, 200);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, excelPolicy, pdfPolicy);

        // when & then
        assertThat(policies.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("equals - 동일한 정책으로 생성된 객체는 같다")
    void equalsWithSamePolicies() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies1 = FileTypePolicies.of(null, htmlPolicy, null, null);
        FileTypePolicies policies2 = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when & then
        assertThat(policies1).isEqualTo(policies2);
        assertThat(policies1.hashCode()).isEqualTo(policies2.hashCode());
    }

    @Test
    @DisplayName("equals - 다른 정책으로 생성된 객체는 다르다")
    void equalsWithDifferentPolicies() {
        // given
        HtmlPolicy htmlPolicy1 = new HtmlPolicy(10, 100, false);
        HtmlPolicy htmlPolicy2 = new HtmlPolicy(20, 200, false);
        FileTypePolicies policies1 = FileTypePolicies.of(null, htmlPolicy1, null, null);
        FileTypePolicies policies2 = FileTypePolicies.of(null, htmlPolicy2, null, null);

        // when & then
        assertThat(policies1).isNotEqualTo(policies2);
    }

    @Test
    @DisplayName("equals - null과 비교하면 다르다")
    void equalsWithNull() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when & then
        assertThat(policies).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals - 다른 클래스 객체와 비교하면 다르다")
    void equalsWithDifferentClass() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when & then
        assertThat(policies).isNotEqualTo("different class");
    }

    @Test
    @DisplayName("toString - 모든 정책 정보를 포함한다")
    void toStringContainsAllPolicies() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        ExcelPolicy excelPolicy = new ExcelPolicy(20, 10);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, excelPolicy, null);

        // when
        String result = policies.toString();

        // then
        assertThat(result).contains("FileTypePolicies");
        assertThat(result).contains("imagePolicy=null");
        assertThat(result).contains("htmlPolicy=present");
        assertThat(result).contains("excelPolicy=present");
        assertThat(result).contains("pdfPolicy=null");
    }

    @Test
    @DisplayName("hashCode는 일관된 값을 반환한다")
    void hashCodeIsConsistent() {
        // given
        HtmlPolicy htmlPolicy = new HtmlPolicy(10, 100, false);
        FileTypePolicies policies = FileTypePolicies.of(null, htmlPolicy, null, null);

        // when
        int hashCode1 = policies.hashCode();
        int hashCode2 = policies.hashCode();

        // then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }
}
