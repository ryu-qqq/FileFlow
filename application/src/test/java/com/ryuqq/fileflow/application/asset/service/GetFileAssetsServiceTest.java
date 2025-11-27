package com.ryuqq.fileflow.application.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetFileAssetsService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GetFileAssetsServiceTest {

    private static final ListFileAssetsQuery QUERY =
            ListFileAssetsQuery.of(1L, 2L, null, null, 0, 10);

    @Mock private FileAssetQueryPort fileAssetQueryPort;
    @Mock private FileAssetQueryAssembler fileAssetQueryAssembler;

    @InjectMocks private GetFileAssetsService getFileAssetsService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("FileAsset 목록을 조회하고 PageResponse로 반환한다")
        void execute_ShouldReturnPageResponse() {
            FileAssetCriteria criteria = mock(FileAssetCriteria.class);
            FileAsset fileAsset = mock(FileAsset.class);
            FileAssetResponse responseDto = mock(FileAssetResponse.class);
            List<FileAsset> fileAssets = List.of(fileAsset);
            List<FileAssetResponse> responses = List.of(responseDto);

            when(fileAssetQueryAssembler.toCriteria(QUERY)).thenReturn(criteria);
            when(fileAssetQueryPort.findByCriteria(criteria)).thenReturn(fileAssets);
            when(fileAssetQueryPort.countByCriteria(criteria)).thenReturn(20L);
            when(fileAssetQueryAssembler.toResponses(fileAssets)).thenReturn(responses);

            PageResponse<FileAssetResponse> pageResponse = getFileAssetsService.execute(QUERY);

            assertThat(pageResponse.content()).isEqualTo(responses);
            assertThat(pageResponse.page()).isEqualTo(0);
            assertThat(pageResponse.size()).isEqualTo(10);
            assertThat(pageResponse.totalElements()).isEqualTo(20L);
            assertThat(pageResponse.totalPages()).isEqualTo(2);
            assertThat(pageResponse.first()).isTrue();
            assertThat(pageResponse.last()).isFalse();

            verify(fileAssetQueryAssembler).toCriteria(QUERY);
            verify(fileAssetQueryPort).findByCriteria(criteria);
            verify(fileAssetQueryPort).countByCriteria(criteria);
            verify(fileAssetQueryAssembler).toResponses(fileAssets);
        }
    }
}
