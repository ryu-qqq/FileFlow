package com.ryuqq.fileflow.application.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetPersistencePort;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("DeleteFileAssetService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class DeleteFileAssetServiceTest {

    private static final String FILE_ASSET_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final long ORG_ID = 10L;
    private static final long TENANT_ID = 20L;

    @Mock private FileAssetQueryPort fileAssetQueryPort;
    @Mock private FileAssetPersistencePort fileAssetPersistencePort;

    @InjectMocks private DeleteFileAssetService deleteFileAssetService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("파일 자산을 삭제하고 응답을 반환한다")
        void execute_ShouldDeleteFileAssetAndReturnResponse() {
            // given
            DeleteFileAssetCommand command =
                    DeleteFileAssetCommand.of(FILE_ASSET_ID, TENANT_ID, ORG_ID, "사용하지 않는 파일");

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);
            LocalDateTime deletedAt = LocalDateTime.of(2025, 11, 27, 10, 30);

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            when(fileAsset.getDeletedAt()).thenReturn(deletedAt);

            // when
            DeleteFileAssetResponse response = deleteFileAssetService.execute(command);

            // then
            assertThat(response.id()).isEqualTo(FILE_ASSET_ID);
            assertThat(response.processedAt()).isEqualTo(deletedAt);

            verify(fileAssetQueryPort).findById(fileAssetId, ORG_ID, TENANT_ID);
            verify(fileAsset).delete();
            verify(fileAssetPersistencePort).persist(fileAsset);
        }

        @Test
        @DisplayName("삭제 사유가 없어도 삭제를 수행한다")
        void execute_WithoutReason_ShouldDeleteFileAsset() {
            // given
            DeleteFileAssetCommand command =
                    DeleteFileAssetCommand.of(FILE_ASSET_ID, TENANT_ID, ORG_ID, null);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);
            LocalDateTime deletedAt = LocalDateTime.now();

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            when(fileAsset.getDeletedAt()).thenReturn(deletedAt);

            // when
            DeleteFileAssetResponse response = deleteFileAssetService.execute(command);

            // then
            assertThat(response.id()).isEqualTo(FILE_ASSET_ID);
            verify(fileAsset).delete();
            verify(fileAssetPersistencePort).persist(fileAsset);
        }

        @Test
        @DisplayName("파일 자산이 없으면 FileAssetNotFoundException을 던진다")
        void execute_WhenNotFound_ShouldThrowException() {
            // given
            DeleteFileAssetCommand command =
                    DeleteFileAssetCommand.of(FILE_ASSET_ID, TENANT_ID, ORG_ID, null);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deleteFileAssetService.execute(command))
                    .isInstanceOf(FileAssetNotFoundException.class)
                    .hasMessageContaining(FILE_ASSET_ID);

            verify(fileAssetPersistencePort, never()).persist(any());
        }

        @Test
        @DisplayName("이미 삭제된 파일에 대해 삭제 시 예외가 발생한다")
        void execute_WhenAlreadyDeleted_ShouldThrowException() {
            // given
            DeleteFileAssetCommand command =
                    DeleteFileAssetCommand.of(FILE_ASSET_ID, TENANT_ID, ORG_ID, null);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            doThrow(new IllegalStateException("이미 삭제된 FileAsset입니다.")).when(fileAsset).delete();

            // when & then
            assertThatThrownBy(() -> deleteFileAssetService.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 삭제된");

            verify(fileAssetPersistencePort, never()).persist(any());
        }
    }
}
