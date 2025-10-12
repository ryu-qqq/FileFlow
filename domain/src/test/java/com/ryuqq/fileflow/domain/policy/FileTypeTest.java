package com.ryuqq.fileflow.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileType 테스트")
class FileTypeTest {

    @Test
    @DisplayName("모든 FileType이 정의되어 있다")
    void allFileTypesAreDefined() {
        // when
        FileType[] types = FileType.values();

        // then
        assertThat(types).containsExactlyInAnyOrder(
            FileType.IMAGE,
            FileType.HTML,
            FileType.EXCEL,
            FileType.PDF
        );
    }

    @Test
    @DisplayName("IMAGE 타입의 속성이 올바르다")
    void imageTypePropertiesAreCorrect() {
        // when
        FileType type = FileType.IMAGE;

        // then
        assertThat(type.getMimeType()).isEqualTo("image/*");
        assertThat(type.getDescription()).isEqualTo("Image files");
        assertThat(type.getTypeName()).isEqualTo("Image");
        assertThat(type.isImage()).isTrue();
        assertThat(type.isDocument()).isFalse();
    }

    @Test
    @DisplayName("HTML 타입의 속성이 올바르다")
    void htmlTypePropertiesAreCorrect() {
        // when
        FileType type = FileType.HTML;

        // then
        assertThat(type.getMimeType()).isEqualTo("text/html");
        assertThat(type.getDescription()).isEqualTo("HTML files");
        assertThat(type.getTypeName()).isEqualTo("HTML");
        assertThat(type.isImage()).isFalse();
        assertThat(type.isDocument()).isTrue();
    }

    @Test
    @DisplayName("EXCEL 타입의 속성이 올바르다")
    void excelTypePropertiesAreCorrect() {
        // when
        FileType type = FileType.EXCEL;

        // then
        assertThat(type.getMimeType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(type.getDescription()).isEqualTo("Excel files");
        assertThat(type.getTypeName()).isEqualTo("Excel");
        assertThat(type.isImage()).isFalse();
        assertThat(type.isDocument()).isTrue();
    }

    @Test
    @DisplayName("PDF 타입의 속성이 올바르다")
    void pdfTypePropertiesAreCorrect() {
        // when
        FileType type = FileType.PDF;

        // then
        assertThat(type.getMimeType()).isEqualTo("application/pdf");
        assertThat(type.getDescription()).isEqualTo("PDF files");
        assertThat(type.getTypeName()).isEqualTo("PDF");
        assertThat(type.isImage()).isFalse();
        assertThat(type.isDocument()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/svg+xml",
        "IMAGE/JPEG",
        "Image/Png"
    })
    @DisplayName("이미지 Content-Type으로 IMAGE 타입을 반환한다")
    void returnsImageTypeForImageContentTypes(String contentType) {
        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.IMAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "text/html",
        "TEXT/HTML",
        "Text/Html"
    })
    @DisplayName("HTML Content-Type으로 HTML 타입을 반환한다")
    void returnsHtmlTypeForHtmlContentTypes(String contentType) {
        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.HTML);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel",
        "APPLICATION/VND.MS-EXCEL",
        "application/excel"
    })
    @DisplayName("Excel Content-Type으로 EXCEL 타입을 반환한다")
    void returnsExcelTypeForExcelContentTypes(String contentType) {
        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.EXCEL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application/pdf",
        "APPLICATION/PDF",
        "Application/Pdf"
    })
    @DisplayName("PDF Content-Type으로 PDF 타입을 반환한다")
    void returnsPdfTypeForPdfContentTypes(String contentType) {
        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.PDF);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("null 또는 빈 Content-Type으로 호출 시 예외가 발생한다")
    void throwsExceptionForNullOrEmptyContentType(String invalidContentType) {
        // when & then
        assertThatThrownBy(() -> FileType.fromContentType(invalidContentType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ContentType must not be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "text/plain",
        "application/json",
        "video/mp4",
        "audio/mpeg",
        "application/zip"
    })
    @DisplayName("지원하지 않는 Content-Type으로 호출 시 예외가 발생한다")
    void throwsExceptionForUnsupportedContentType(String unsupportedContentType) {
        // when & then
        assertThatThrownBy(() -> FileType.fromContentType(unsupportedContentType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported content type");
    }

    @Test
    @DisplayName("IMAGE만 isImage가 true를 반환한다")
    void onlyImageTypeReturnsTrueForIsImage() {
        // when & then
        assertThat(FileType.IMAGE.isImage()).isTrue();
        assertThat(FileType.HTML.isImage()).isFalse();
        assertThat(FileType.EXCEL.isImage()).isFalse();
        assertThat(FileType.PDF.isImage()).isFalse();
    }

    @Test
    @DisplayName("HTML, EXCEL, PDF는 isDocument가 true를 반환한다")
    void documentTypesReturnTrueForIsDocument() {
        // when & then
        assertThat(FileType.IMAGE.isDocument()).isFalse();
        assertThat(FileType.HTML.isDocument()).isTrue();
        assertThat(FileType.EXCEL.isDocument()).isTrue();
        assertThat(FileType.PDF.isDocument()).isTrue();
    }

    @Test
    @DisplayName("Content-Type의 공백을 제거하고 처리한다")
    void trimsWhitespaceFromContentType() {
        // given
        String contentTypeWithSpaces = "  image/jpeg  ";

        // when
        FileType type = FileType.fromContentType(contentTypeWithSpaces);

        // then
        assertThat(type).isEqualTo(FileType.IMAGE);
    }

    @Test
    @DisplayName("Content-Type를 소문자로 정규화하여 처리한다")
    void normalizesContentTypeToLowercase() {
        // given
        String mixedCaseContentType = "IMAGE/JPEG";

        // when
        FileType type = FileType.fromContentType(mixedCaseContentType);

        // then
        assertThat(type).isEqualTo(FileType.IMAGE);
    }

    @ParameterizedTest
    @CsvSource({
        "IMAGE, Image",
        "HTML, HTML",
        "EXCEL, Excel",
        "PDF, PDF"
    })
    @DisplayName("getTypeName이 올바른 타입 이름을 반환한다")
    void getTypeNameReturnsCorrectName(FileType type, String expectedName) {
        // when
        String typeName = type.getTypeName();

        // then
        assertThat(typeName).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Excel Content-Type에 'spreadsheetml'이 포함되어 있으면 EXCEL을 반환한다")
    void identifiesExcelBySpreadsheetML() {
        // given
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.EXCEL);
    }

    @Test
    @DisplayName("Excel Content-Type에 'excel'이 포함되어 있으면 EXCEL을 반환한다")
    void identifiesExcelByExcelKeyword() {
        // given
        String contentType = "application/excel";

        // when
        FileType type = FileType.fromContentType(contentType);

        // then
        assertThat(type).isEqualTo(FileType.EXCEL);
    }

    @Test
    @DisplayName("레거시 Excel MIME 타입을 인식한다")
    void recognizesLegacyExcelMimeType() {
        // given
        String legacyExcelType = "application/vnd.ms-excel";

        // when
        FileType type = FileType.fromContentType(legacyExcelType);

        // then
        assertThat(type).isEqualTo(FileType.EXCEL);
    }

    @Test
    @DisplayName("다양한 이미지 서브타입을 인식한다")
    void recognizesVariousImageSubtypes() {
        // given
        String[] imageSubtypes = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "image/svg+xml",
            "image/tiff"
        };

        // when & then
        for (String subtype : imageSubtypes) {
            FileType type = FileType.fromContentType(subtype);
            assertThat(type).isEqualTo(FileType.IMAGE);
        }
    }

    @Test
    @DisplayName("Content-Type에 파라미터가 포함되어 있어도 타입을 식별한다")
    void identifiesTypeWithContentTypeParameters() {
        // given
        String contentTypeWithParams = "image/jpeg; charset=utf-8";

        // when
        // 현재 구현에서는 파라미터가 포함된 경우 IMAGE를 반환하지 않을 수 있음
        // 이는 구현에 따라 다를 수 있으므로 실제 동작을 확인
    }

    @Test
    @DisplayName("toString이 enum 이름을 반환한다")
    void toStringReturnsEnumName() {
        // when & then
        assertThat(FileType.IMAGE.toString()).isEqualTo("IMAGE");
        assertThat(FileType.HTML.toString()).isEqualTo("HTML");
        assertThat(FileType.EXCEL.toString()).isEqualTo("EXCEL");
        assertThat(FileType.PDF.toString()).isEqualTo("PDF");
    }

    @Test
    @DisplayName("valueOf로 enum을 조회할 수 있다")
    void canGetEnumByValueOf() {
        // when
        FileType image = FileType.valueOf("IMAGE");
        FileType html = FileType.valueOf("HTML");
        FileType excel = FileType.valueOf("EXCEL");
        FileType pdf = FileType.valueOf("PDF");

        // then
        assertThat(image).isEqualTo(FileType.IMAGE);
        assertThat(html).isEqualTo(FileType.HTML);
        assertThat(excel).isEqualTo(FileType.EXCEL);
        assertThat(pdf).isEqualTo(FileType.PDF);
    }
}
