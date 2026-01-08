package com.ryuqq.fileflow.integration.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetStatisticsResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * FileAssetApi SDK 통합 테스트.
 *
 * <p>실제 서버와 통신하여 SDK의 FileAsset 기능을 검증합니다.
 */
@DisplayName("FileAssetApi SDK 통합 테스트")
class FileAssetSdkIntegrationTest extends SdkIntegrationTest {

    private FileAssetApi fileAssetApi;

    @BeforeEach
    void setUp() {
        fileAssetApi = fileFlowClient.fileAssets();
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("파일 에셋 목록을 페이지네이션으로 조회할 수 있다")
        void shouldListFileAssets() {
            // when
            PageResponse<FileAssetResponse> response = fileAssetApi.list(0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("존재하지 않는 파일 ID로 조회하면 NotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenFileNotExists() {
            // given - 존재하지 않는 UUID
            String nonExistentId = UUID.randomUUID().toString();

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get(nonExistentId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStatistics 메서드")
    class GetStatisticsTest {

        @Test
        @DisplayName("파일 에셋 통계를 조회할 수 있다")
        void shouldGetFileAssetStatistics() {
            // when
            FileAssetStatisticsResponse response = fileAssetApi.getStatistics();

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalCount()).isGreaterThanOrEqualTo(0);
            assertThat(response.getStatusCounts()).isNotNull();
            assertThat(response.getCategoryCounts()).isNotNull();
        }
    }

    @Nested
    @DisplayName("generateDownloadUrl 메서드")
    class GenerateDownloadUrlTest {

        @Test
        @DisplayName("존재하지 않는 파일 ID로 다운로드 URL 생성 시 NotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenGeneratingDownloadUrlForNonExistentFile() {
            // given - 존재하지 않는 UUID
            String nonExistentId = UUID.randomUUID().toString();

            // when & then
            assertThatThrownBy(() -> fileAssetApi.generateDownloadUrl(nonExistentId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("존재하지 않는 파일 ID로 삭제 시 NotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistentFile() {
            // given - 존재하지 않는 UUID
            String nonExistentId = UUID.randomUUID().toString();

            // when & then
            assertThatThrownBy(() -> fileAssetApi.delete(nonExistentId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }
}
