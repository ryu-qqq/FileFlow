package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JobType Value Object 테스트")
class JobTypeTest {

    @Test
    @DisplayName("이미지 가공 타입을 포함해야 한다")
    void shouldContainImageProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobType.THUMBNAIL_GENERATION,
                JobType.IMAGE_RESIZE,
                JobType.IMAGE_FORMAT_CONVERSION,
                JobType.IMAGE_COMPRESSION
        );
    }

    @Test
    @DisplayName("HTML 가공 타입을 포함해야 한다")
    void shouldContainHtmlProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobType.HTML_PARSING,
                JobType.HTML_TO_PDF,
                JobType.HTML_SCREENSHOT
        );
    }

    @Test
    @DisplayName("문서 가공 타입을 포함해야 한다")
    void shouldContainDocumentProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobType.PDF_TEXT_EXTRACTION,
                JobType.DOCUMENT_CONVERSION
        );
    }

    @Test
    @DisplayName("엑셀 가공 타입을 포함해야 한다")
    void shouldContainExcelProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobType.EXCEL_TO_CSV,
                JobType.EXCEL_DATA_EXTRACTION
        );
    }
}
