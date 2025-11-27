package com.ryuqq.fileflow.application.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetFileAssetService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GetFileAssetServiceTest {

    private static final String FILE_ASSET_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final long ORG_ID = 10L;
    private static final long TENANT_ID = 20L;
    private static final GetFileAssetQuery QUERY =
            GetFileAssetQuery.of(FILE_ASSET_ID, ORG_ID, TENANT_ID);

    @Mock private FileAssetQueryPort fileAssetQueryPort;
    @Mock private FileAssetQueryAssembler fileAssetQueryAssembler;

    @InjectMocks private GetFileAssetService getFileAssetService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("파일 자산을 조회하여 응답을 반환한다")
        void execute_ShouldReturnFileAsset() {
            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);
            FileAssetResponse expectedResponse = mock(FileAssetResponse.class);

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            when(fileAssetQueryAssembler.toResponse(fileAsset)).thenReturn(expectedResponse);

            FileAssetResponse response = getFileAssetService.execute(QUERY);

            assertThat(response).isEqualTo(expectedResponse);
            verify(fileAssetQueryPort).findById(fileAssetId, ORG_ID, TENANT_ID);
            verify(fileAssetQueryAssembler).toResponse(fileAsset);
        }

        @Test
        @DisplayName("파일 자산이 없으면 FileAssetNotFoundException을 던진다")
        void execute_WhenNotFound_ShouldThrowException() {
            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> getFileAssetService.execute(QUERY))
                    .isInstanceOf(FileAssetNotFoundException.class)
                    .hasMessageContaining(FILE_ASSET_ID);

            verify(fileAssetQueryAssembler, never()).toResponse(any());
        }
    }
}
