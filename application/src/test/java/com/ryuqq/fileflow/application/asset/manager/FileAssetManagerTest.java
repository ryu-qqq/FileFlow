package com.ryuqq.fileflow.application.asset.manager;

import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetAssembler;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetPersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FileAssetManager 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetManagerTest {

    @Mock private FileAssetAssembler fileAssetAssembler;
    @Mock private FileAssetPersistencePort fileAssetPersistencePort;

    private FileAssetManager manager;

    @BeforeEach
    void setUp() {
        manager = new FileAssetManager(fileAssetAssembler, fileAssetPersistencePort);
    }

    @Nested
    @DisplayName("createAndPersist")
    class CreateAndPersist {

        @Test
        @DisplayName("FileUploadCompletedEvent로부터 FileAsset을 생성하고 저장한다")
        void createAndPersist_ShouldCreateAndPersistFileAsset() {
            // given
            UUID sessionUuid = UUID.randomUUID();
            LocalDateTime completedAt = LocalDateTime.now();
            FileUploadCompletedEvent event =
                    FileUploadCompletedEvent.of(
                            UploadSessionId.of(sessionUuid),
                            FileName.of("test-image.jpg"),
                            FileSize.of(1024L),
                            ContentType.of("image/jpeg"),
                            S3Bucket.of("test-bucket"),
                            S3Key.of("uploads/test-image.jpg"),
                            ETag.of("d41d8cd98f00b204e9800998ecf8427e"),
                            1L,
                            2L,
                            3L,
                            completedAt);

            FileAsset fileAsset = mock(FileAsset.class);
            FileAssetId fileAssetId = FileAssetId.of(UUID.randomUUID());

            when(fileAssetAssembler.toFileAsset(event)).thenReturn(fileAsset);
            when(fileAssetPersistencePort.persist(fileAsset)).thenReturn(fileAssetId);

            // when
            manager.createAndPersist(event);

            // then
            verify(fileAssetAssembler).toFileAsset(event);
            verify(fileAssetPersistencePort).persist(fileAsset);
        }

        @Test
        @DisplayName("다양한 ContentType의 이벤트를 처리한다")
        void createAndPersist_ShouldHandleVariousContentTypes() {
            // given
            UUID sessionUuid = UUID.randomUUID();
            LocalDateTime completedAt = LocalDateTime.now();
            FileUploadCompletedEvent event =
                    FileUploadCompletedEvent.of(
                            UploadSessionId.of(sessionUuid),
                            FileName.of("document.pdf"),
                            FileSize.of(2048L),
                            ContentType.of("application/pdf"),
                            S3Bucket.of("test-bucket"),
                            S3Key.of("uploads/document.pdf"),
                            ETag.of("abcdef123456"),
                            1L,
                            2L,
                            3L,
                            completedAt);

            FileAsset fileAsset = mock(FileAsset.class);
            FileAssetId fileAssetId = FileAssetId.of(UUID.randomUUID());

            when(fileAssetAssembler.toFileAsset(event)).thenReturn(fileAsset);
            when(fileAssetPersistencePort.persist(fileAsset)).thenReturn(fileAssetId);

            // when
            manager.createAndPersist(event);

            // then
            verify(fileAssetAssembler).toFileAsset(event);
            verify(fileAssetPersistencePort).persist(fileAsset);
        }
    }
}
