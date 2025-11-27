package com.ryuqq.fileflow.application.asset.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetQueryAssembler 단위 테스트")
class FileAssetQueryAssemblerTest {

    private FileAssetQueryAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new FileAssetQueryAssembler();
    }

    @Nested
    @DisplayName("toCriteria")
    class ToCriteria {

        @Test
        @DisplayName("ListFileAssetsQuery를 FileAssetCriteria로 변환한다")
        void toCriteria_ShouldConvertQueryToCriteria() {
            // given
            ListFileAssetsQuery query =
                    ListFileAssetsQuery.of(
                            1L, 2L, FileAssetStatus.COMPLETED, FileCategory.IMAGE, 0, 20);

            // when
            FileAssetCriteria result = assembler.toCriteria(query);

            // then
            assertThat(result).isNotNull();
            assertThat(result.organizationId()).isEqualTo(1L);
            assertThat(result.tenantId()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(result.category()).isEqualTo(FileCategory.IMAGE);
            assertThat(result.offset()).isZero();
            assertThat(result.limit()).isEqualTo(20);
        }

        @Test
        @DisplayName("선택적 필드가 null인 경우에도 정상 변환한다")
        void toCriteria_ShouldHandleNullOptionalFields() {
            // given
            ListFileAssetsQuery query = ListFileAssetsQuery.of(1L, 2L, null, null, 0, 10);

            // when
            FileAssetCriteria result = assembler.toCriteria(query);

            // then
            assertThat(result).isNotNull();
            assertThat(result.organizationId()).isEqualTo(1L);
            assertThat(result.tenantId()).isEqualTo(2L);
            assertThat(result.status()).isNull();
            assertThat(result.category()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("FileAsset을 FileAssetResponse로 변환한다")
        void toResponse_ShouldConvertDomainToResponse() {
            // given
            FileAsset fileAsset = mock(FileAsset.class);
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime processedAt = createdAt.plusMinutes(1);

            when(fileAsset.getIdValue()).thenReturn("asset-123");
            when(fileAsset.getSessionIdValue()).thenReturn("session-456");
            when(fileAsset.getFileNameValue()).thenReturn("test-image.jpg");
            when(fileAsset.getFileSizeValue()).thenReturn(1024L);
            when(fileAsset.getContentTypeValue()).thenReturn("image/jpeg");
            when(fileAsset.getCategory()).thenReturn(FileCategory.IMAGE);
            when(fileAsset.getBucketValue()).thenReturn("test-bucket");
            when(fileAsset.getS3KeyValue()).thenReturn("uploads/test-image.jpg");
            when(fileAsset.getEtagValue()).thenReturn("etag-789");
            when(fileAsset.getStatus()).thenReturn(FileAssetStatus.COMPLETED);
            when(fileAsset.getCreatedAt()).thenReturn(createdAt);
            when(fileAsset.getProcessedAt()).thenReturn(processedAt);

            // when
            FileAssetResponse result = assembler.toResponse(fileAsset);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("asset-123");
            assertThat(result.sessionId()).isEqualTo("session-456");
            assertThat(result.fileName()).isEqualTo("test-image.jpg");
            assertThat(result.fileSize()).isEqualTo(1024L);
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.category()).isEqualTo(FileCategory.IMAGE);
            assertThat(result.bucket()).isEqualTo("test-bucket");
            assertThat(result.s3Key()).isEqualTo("uploads/test-image.jpg");
            assertThat(result.etag()).isEqualTo("etag-789");
            assertThat(result.status()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(result.createdAt()).isEqualTo(createdAt);
            assertThat(result.processedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("toResponses")
    class ToResponses {

        @Test
        @DisplayName("FileAsset 목록을 FileAssetResponse 목록으로 변환한다")
        void toResponses_ShouldConvertDomainListToResponseList() {
            // given
            FileAsset fileAsset1 = mock(FileAsset.class);
            FileAsset fileAsset2 = mock(FileAsset.class);
            LocalDateTime now = LocalDateTime.now();

            when(fileAsset1.getIdValue()).thenReturn("asset-1");
            when(fileAsset1.getSessionIdValue()).thenReturn("session-1");
            when(fileAsset1.getFileNameValue()).thenReturn("file1.jpg");
            when(fileAsset1.getFileSizeValue()).thenReturn(1024L);
            when(fileAsset1.getContentTypeValue()).thenReturn("image/jpeg");
            when(fileAsset1.getCategory()).thenReturn(FileCategory.IMAGE);
            when(fileAsset1.getBucketValue()).thenReturn("bucket");
            when(fileAsset1.getS3KeyValue()).thenReturn("key1");
            when(fileAsset1.getEtagValue()).thenReturn("etag1");
            when(fileAsset1.getStatus()).thenReturn(FileAssetStatus.COMPLETED);
            when(fileAsset1.getCreatedAt()).thenReturn(now);
            when(fileAsset1.getProcessedAt()).thenReturn(now);

            when(fileAsset2.getIdValue()).thenReturn("asset-2");
            when(fileAsset2.getSessionIdValue()).thenReturn("session-2");
            when(fileAsset2.getFileNameValue()).thenReturn("file2.pdf");
            when(fileAsset2.getFileSizeValue()).thenReturn(2048L);
            when(fileAsset2.getContentTypeValue()).thenReturn("application/pdf");
            when(fileAsset2.getCategory()).thenReturn(FileCategory.DOCUMENT);
            when(fileAsset2.getBucketValue()).thenReturn("bucket");
            when(fileAsset2.getS3KeyValue()).thenReturn("key2");
            when(fileAsset2.getEtagValue()).thenReturn("etag2");
            when(fileAsset2.getStatus()).thenReturn(FileAssetStatus.COMPLETED);
            when(fileAsset2.getCreatedAt()).thenReturn(now);
            when(fileAsset2.getProcessedAt()).thenReturn(now);

            List<FileAsset> fileAssets = List.of(fileAsset1, fileAsset2);

            // when
            List<FileAssetResponse> result = assembler.toResponses(fileAssets);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo("asset-1");
            assertThat(result.get(0).fileName()).isEqualTo("file1.jpg");
            assertThat(result.get(1).id()).isEqualTo("asset-2");
            assertThat(result.get(1).fileName()).isEqualTo("file2.pdf");
        }

        @Test
        @DisplayName("빈 목록을 입력하면 빈 목록을 반환한다")
        void toResponses_ShouldReturnEmptyListWhenInputIsEmpty() {
            // given
            List<FileAsset> fileAssets = List.of();

            // when
            List<FileAssetResponse> result = assembler.toResponses(fileAssets);

            // then
            assertThat(result).isEmpty();
        }
    }
}
