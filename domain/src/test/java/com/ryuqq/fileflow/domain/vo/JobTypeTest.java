package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.fixture.JobTypeFixture;
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
                JobTypeFixture.thumbnailGeneration(),
                JobTypeFixture.imageResize(),
                JobTypeFixture.imageFormatConversion(),
                JobTypeFixture.imageCompression()
        );
    }

    @Test
    @DisplayName("HTML 가공 타입을 포함해야 한다")
    void shouldContainHtmlProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobTypeFixture.htmlParsing(),
                JobTypeFixture.htmlToPdf(),
                JobTypeFixture.htmlScreenshot()
        );
    }

    @Test
    @DisplayName("문서 가공 타입을 포함해야 한다")
    void shouldContainDocumentProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobTypeFixture.pdfTextExtraction(),
                JobTypeFixture.documentConversion()
        );
    }

    @Test
    @DisplayName("엑셀 가공 타입을 포함해야 한다")
    void shouldContainExcelProcessingTypes() {
        // Given & When
        JobType[] types = JobType.values();

        // Then
        assertThat(types).contains(
                JobTypeFixture.excelToCsv(),
                JobTypeFixture.excelDataExtraction()
        );
    }
}
