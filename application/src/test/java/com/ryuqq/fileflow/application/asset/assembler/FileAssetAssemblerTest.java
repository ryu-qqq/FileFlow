package com.ryuqq.fileflow.application.asset.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FileAssetAssembler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetAssemblerTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private ClockHolder clockHolder;

    private FileAssetAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new FileAssetAssembler(clockHolder);
    }

    @Nested
    @DisplayName("toFileAsset")
    class ToFileAsset {

        @Test
        @DisplayName("FileUploadCompletedEvent를 FileAsset으로 변환한다")
        void toFileAsset_ShouldConvertEventToDomain() {
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

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);

            // when
            FileAsset result = assembler.toFileAsset(event);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSessionIdValue()).isEqualTo(sessionUuid.toString());
            assertThat(result.getFileNameValue()).isEqualTo("test-image.jpg");
            assertThat(result.getFileSizeValue()).isEqualTo(1024L);
            assertThat(result.getContentTypeValue()).isEqualTo("image/jpeg");
            assertThat(result.getBucketValue()).isEqualTo("test-bucket");
            assertThat(result.getS3KeyValue()).isEqualTo("uploads/test-image.jpg");
            assertThat(result.getEtagValue()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
            assertThat(result.getCategory()).isEqualTo(FileCategory.IMAGE);
        }

        @ParameterizedTest
        @DisplayName("ContentType에 따라 올바른 FileCategory를 설정한다")
        @CsvSource({
            "image/jpeg,IMAGE",
            "image/png,IMAGE",
            "video/mp4,VIDEO",
            "video/webm,VIDEO",
            "audio/mpeg,AUDIO",
            "audio/wav,AUDIO",
            "application/pdf,DOCUMENT",
            "application/msword,DOCUMENT",
            "text/plain,DOCUMENT",
            "application/zip,OTHER",
            "application/octet-stream,OTHER"
        })
        void toFileAsset_ShouldDetermineCategoryByContentType(
                String contentType, FileCategory expectedCategory) {
            // given
            UUID sessionUuid = UUID.randomUUID();
            LocalDateTime completedAt = LocalDateTime.now();
            FileUploadCompletedEvent event =
                    FileUploadCompletedEvent.of(
                            UploadSessionId.of(sessionUuid),
                            FileName.of("test-file"),
                            FileSize.of(1024L),
                            ContentType.of(contentType),
                            S3Bucket.of("test-bucket"),
                            S3Key.of("uploads/test-file"),
                            ETag.of("d41d8cd98f00b204e9800998ecf8427e"),
                            1L,
                            2L,
                            3L,
                            completedAt);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);

            // when
            FileAsset result = assembler.toFileAsset(event);

            // then
            assertThat(result.getCategory()).isEqualTo(expectedCategory);
        }
    }
}
