package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.fixture.FileStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File Aggregate Root 테스트")
class FileTest {

    @Test
    @DisplayName("유효한 데이터로 File을 생성할 수 있어야 한다")
    void shouldCreateFileWithValidData() {
        // Given & When
        File file = FileFixture.aFile()
                .fileName("test-image.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .category("IMAGE")
                .tags("product,thumbnail")
                .build();

        // Then
        assertThat(file).isNotNull();
        assertThat(file.getFileId()).isNotBlank();
        assertThat(file.getFileName()).isEqualTo("test-image.jpg");
        assertThat(file.getFileSize()).isEqualTo(1024000L);
        assertThat(file.getMimeType()).isEqualTo("image/jpeg");
        assertThat(file.getStatus()).isEqualTo(FileStatusFixture.pending());
        assertThat(file.getS3Key()).contains("test-image.jpg");
        assertThat(file.getS3Bucket()).isEqualTo("fileflow-storage");
        assertThat(file.getCdnUrl()).contains("test-image.jpg");
        assertThat(file.getUploaderId()).isNotNull();
        assertThat(file.getCategory()).isEqualTo("IMAGE");
        assertThat(file.getTags()).isEqualTo("product,thumbnail");
        assertThat(file.getVersion()).isEqualTo(1);
        assertThat(file.getDeletedAt()).isNull();
        assertThat(file.getCreatedAt()).isNotNull();
        assertThat(file.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드가 올바르게 설정되어야 한다")
    void shouldHaveRequiredFields() {
        // Given & When - PDF 문서 Fixture 사용
        File file = FileFixture.aPdfDocument();

        // Then - 필수 필드 검증
        assertThat(file.getFileId()).isNotBlank();
        assertThat(file.getFileName()).isNotBlank();
        assertThat(file.getFileSize()).isPositive();
        assertThat(file.getMimeType()).isNotBlank();
        assertThat(file.getStatus()).isNotNull();
    }

    @Test
    @DisplayName("JPG 이미지 Fixture를 사용할 수 있어야 한다")
    void shouldUseJpgImageFixture() {
        // Given & When
        File file = FileFixture.aJpgImage();

        // Then
        assertThat(file.getFileName()).isEqualTo("test-image.jpg");
        assertThat(file.getMimeType()).isEqualTo("image/jpeg");
        assertThat(file.getCategory()).isEqualTo("IMAGE");
    }

    @Test
    @DisplayName("Excel 파일 Fixture를 사용할 수 있어야 한다")
    void shouldUseExcelFileFixture() {
        // Given & When
        File file = FileFixture.anExcelFile();

        // Then
        assertThat(file.getFileName()).isEqualTo("data.xlsx");
        assertThat(file.getMimeType()).contains("spreadsheetml");
        assertThat(file.getCategory()).isEqualTo("EXCEL");
    }
}
