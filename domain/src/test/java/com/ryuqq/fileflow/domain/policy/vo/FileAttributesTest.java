package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileAttributes Value Object 테스트
 */
@DisplayName("FileAttributes Value Object 테스트")
class FileAttributesTest {

    @Test
    @DisplayName("정상적인 값으로 FileAttributes 생성 성공")
    void createFileAttributes_Success() {
        // given & when
        FileAttributes attrs = new FileAttributes(
                1024000,
                1,
                "jpg",
                Dimension.of(1920, 1080),
                null,
                null,
                null,
                null
        );

        // then
        assertThat(attrs.sizeBytes()).isEqualTo(1024000);
        assertThat(attrs.fileCount()).isEqualTo(1);
        assertThat(attrs.format()).isEqualTo("jpg");
        assertThat(attrs.dimension()).isEqualTo(Dimension.of(1920, 1080));
        assertThat(attrs.imageCount()).isNull();
        assertThat(attrs.sheetCount()).isNull();
        assertThat(attrs.pageCount()).isNull();
    }

    @Test
    @DisplayName("Builder로 FileAttributes 생성 성공 - 모든 필드")
    void builder_AllFields_Success() {
        // when
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(512000)
                .fileCount(3)
                .format("pdf")
                .dimension(Dimension.of(2048, 2048))
                .imageCount(5)
                .sheetCount(10)
                .pageCount(20)
                .build();

        // then
        assertThat(attrs.sizeBytes()).isEqualTo(512000);
        assertThat(attrs.fileCount()).isEqualTo(3);
        assertThat(attrs.format()).isEqualTo("pdf");
        assertThat(attrs.dimension()).isEqualTo(Dimension.of(2048, 2048));
        assertThat(attrs.imageCount()).isEqualTo(5);
        assertThat(attrs.sheetCount()).isEqualTo(10);
        assertThat(attrs.pageCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("Builder로 FileAttributes 생성 성공 - 필수 필드만")
    void builder_RequiredFieldsOnly_Success() {
        // when
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(1024)
                .build();

        // then
        assertThat(attrs.sizeBytes()).isEqualTo(1024);
        assertThat(attrs.fileCount()).isEqualTo(1); // default value
        assertThat(attrs.format()).isNull();
        assertThat(attrs.dimension()).isNull();
        assertThat(attrs.imageCount()).isNull();
        assertThat(attrs.sheetCount()).isNull();
        assertThat(attrs.pageCount()).isNull();
    }

    @Test
    @DisplayName("Builder - 이미지 파일 속성 예시")
    void builder_ImageFile_Example() {
        // when
        FileAttributes imageAttrs = FileAttributes.builder()
                .sizeBytes(1024000)
                .fileCount(1)
                .format("jpg")
                .dimension(Dimension.of(1920, 1080))
                .build();

        // then
        assertThat(imageAttrs.sizeBytes()).isEqualTo(1024000);
        assertThat(imageAttrs.format()).isEqualTo("jpg");
        assertThat(imageAttrs.dimension()).isEqualTo(Dimension.of(1920, 1080));
        assertThat(imageAttrs.imageCount()).isNull();
    }

    @Test
    @DisplayName("Builder - HTML 파일 속성 예시")
    void builder_HtmlFile_Example() {
        // when
        FileAttributes htmlAttrs = FileAttributes.builder()
                .sizeBytes(512000)
                .imageCount(5)
                .build();

        // then
        assertThat(htmlAttrs.sizeBytes()).isEqualTo(512000);
        assertThat(htmlAttrs.imageCount()).isEqualTo(5);
        assertThat(htmlAttrs.format()).isNull();
        assertThat(htmlAttrs.dimension()).isNull();
    }

    @Test
    @DisplayName("Builder - Excel 파일 속성 예시")
    void builder_ExcelFile_Example() {
        // when
        FileAttributes excelAttrs = FileAttributes.builder()
                .sizeBytes(2048000)
                .sheetCount(10)
                .build();

        // then
        assertThat(excelAttrs.sizeBytes()).isEqualTo(2048000);
        assertThat(excelAttrs.sheetCount()).isEqualTo(10);
        assertThat(excelAttrs.format()).isNull();
    }

    @Test
    @DisplayName("Builder - PDF 파일 속성 예시")
    void builder_PdfFile_Example() {
        // when
        FileAttributes pdfAttrs = FileAttributes.builder()
                .sizeBytes(3072000)
                .pageCount(20)
                .build();

        // then
        assertThat(pdfAttrs.sizeBytes()).isEqualTo(3072000);
        assertThat(pdfAttrs.pageCount()).isEqualTo(20);
        assertThat(pdfAttrs.format()).isNull();
    }

    @Test
    @DisplayName("sizeBytes가 음수일 때 예외 발생")
    void createFileAttributes_SizeBytesNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new FileAttributes(-1, 1, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sizeBytes must not be negative");
    }

    @Test
    @DisplayName("fileCount가 음수일 때 예외 발생")
    void createFileAttributes_FileCountNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new FileAttributes(1024, -1, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileCount must not be negative");
    }

    @Test
    @DisplayName("Builder - sizeBytes가 음수일 때 예외 발생")
    void builder_SizeBytesNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> FileAttributes.builder()
                .sizeBytes(-100)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sizeBytes must not be negative");
    }

    @Test
    @DisplayName("Builder - fileCount가 음수일 때 예외 발생")
    void builder_FileCountNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(-1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileCount must not be negative");
    }

    @Test
    @DisplayName("sizeBytes가 0일 때 생성 성공")
    void createFileAttributes_SizeBytesZero_Success() {
        // when
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(0)
                .build();

        // then
        assertThat(attrs.sizeBytes()).isEqualTo(0);
    }

    @Test
    @DisplayName("fileCount가 0일 때 생성 성공")
    void createFileAttributes_FileCountZero_Success() {
        // when
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(0)
                .build();

        // then
        assertThat(attrs.fileCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Builder 메서드 체이닝 동작 확인")
    void builder_MethodChaining_Success() {
        // when
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(2)
                .format("png")
                .dimension(Dimension.of(800, 600))
                .imageCount(3)
                .sheetCount(4)
                .pageCount(5)
                .build();

        // then
        assertThat(attrs.sizeBytes()).isEqualTo(1024);
        assertThat(attrs.fileCount()).isEqualTo(2);
        assertThat(attrs.format()).isEqualTo("png");
        assertThat(attrs.dimension()).isEqualTo(Dimension.of(800, 600));
        assertThat(attrs.imageCount()).isEqualTo(3);
        assertThat(attrs.sheetCount()).isEqualTo(4);
        assertThat(attrs.pageCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 FileAttributes는 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        Dimension dimension = Dimension.of(1920, 1080);
        FileAttributes attrs1 = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(1)
                .format("jpg")
                .dimension(dimension)
                .build();
        FileAttributes attrs2 = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(1)
                .format("jpg")
                .dimension(dimension)
                .build();

        // when & then
        assertThat(attrs1).isEqualTo(attrs2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 FileAttributes는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        FileAttributes attrs1 = FileAttributes.builder()
                .sizeBytes(1024)
                .build();
        FileAttributes attrs2 = FileAttributes.builder()
                .sizeBytes(2048)
                .build();

        // when & then
        assertThat(attrs1).isNotEqualTo(attrs2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 FileAttributes는 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        Dimension dimension = Dimension.of(1920, 1080);
        FileAttributes attrs1 = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(1)
                .format("jpg")
                .dimension(dimension)
                .build();
        FileAttributes attrs2 = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(1)
                .format("jpg")
                .dimension(dimension)
                .build();

        // when & then
        assertThat(attrs1.hashCode()).isEqualTo(attrs2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        FileAttributes attrs = FileAttributes.builder()
                .sizeBytes(1024)
                .fileCount(5)
                .format("pdf")
                .build();

        // when
        String result = attrs.toString();

        // then
        assertThat(result).contains("1024");
        assertThat(result).contains("5");
        assertThat(result).contains("pdf");
    }
}
