package com.ryuqq.fileflow.application.session.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("MultiPartUploadAssembler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MultiPartUploadAssemblerTest {

    private static final String FILE_NAME = "large-file.zip";
    private static final long FILE_SIZE = 100_000_000L; // 100MB
    private static final String CONTENT_TYPE = "application/zip";
    private static final long PART_SIZE = 5_242_880L; // 5MB
    private static final String BUCKET = "test-bucket";
    private static final String S3_KEY = "uploads/2025/01/01/large-file.zip";
    private static final String S3_UPLOAD_ID = "upload-id-123";
    private static final String ETAG = "d41d8cd98f00b204e9800998ecf8427e";

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private ClockHolder clockHolder;
    @Mock private Supplier<UserContext> userContextSupplier;
    @Mock private UserContext userContext;

    private MultiPartUploadAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new MultiPartUploadAssembler(clockHolder, userContextSupplier);
    }

    @Nested
    @DisplayName("toS3Metadata")
    class ToS3Metadata {

        @Test
        @DisplayName("Command에서 S3 메타데이터를 추출한다")
        void toS3Metadata_ShouldExtractS3MetadataFromCommand() {
            // given
            InitMultipartUploadCommand command =
                    InitMultipartUploadCommand.of(
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            PART_SIZE,
                            1L,
                            2L,
                            3L,
                            "user@test.com");

            when(userContextSupplier.get()).thenReturn(userContext);
            when(userContext.getS3Bucket()).thenReturn(S3Bucket.of(BUCKET));
            when(userContext.generateS3KeyToday(null, FILE_NAME)).thenReturn(S3Key.of(S3_KEY));

            // when
            S3UploadMetadata result = assembler.toS3Metadata(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.bucket().bucketName()).isEqualTo(BUCKET);
            assertThat(result.s3Key().key()).isEqualTo(S3_KEY);
            assertThat(result.contentType().type()).isEqualTo(CONTENT_TYPE);
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("Command와 S3UploadId를 MultipartUploadSession으로 변환한다")
        void toDomain_ShouldConvertCommandToDomain() {
            // given
            InitMultipartUploadCommand command =
                    InitMultipartUploadCommand.of(
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            PART_SIZE,
                            1L,
                            2L,
                            3L,
                            "user@test.com");
            S3UploadId s3UploadId = S3UploadId.of(S3_UPLOAD_ID);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            when(userContextSupplier.get()).thenReturn(userContext);
            when(userContext.getS3Bucket()).thenReturn(S3Bucket.of(BUCKET));
            when(userContext.generateS3KeyToday(null, FILE_NAME)).thenReturn(S3Key.of(S3_KEY));

            // when
            MultipartUploadSession result = assembler.toDomain(command, s3UploadId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFileNameValue()).isEqualTo(FILE_NAME);
            assertThat(result.getFileSizeValue()).isEqualTo(FILE_SIZE);
            assertThat(result.getContentTypeValue()).isEqualTo(CONTENT_TYPE);
            assertThat(result.getBucketValue()).isEqualTo(BUCKET);
            assertThat(result.getS3KeyValue()).isEqualTo(S3_KEY);
            assertThat(result.getS3UploadIdValue()).isEqualTo(S3_UPLOAD_ID);
            assertThat(result.getStatus()).isEqualTo(SessionStatus.PREPARING);
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("MultipartUploadSession과 Part 목록을 InitMultipartUploadResponse로 변환한다")
        void toResponse_ShouldConvertSessionAndPartsToResponse() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            UUID sessionUuid = UUID.randomUUID();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            when(session.getId()).thenReturn(UploadSessionId.of(sessionUuid));
            when(session.getS3UploadIdValue()).thenReturn(S3_UPLOAD_ID);
            when(session.getTotalPartsValue()).thenReturn(2);
            when(session.getPartSizeValue()).thenReturn(PART_SIZE);
            when(session.getExpiresAt()).thenReturn(expiresAt);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);

            CompletedPart part1 = mock(CompletedPart.class);
            CompletedPart part2 = mock(CompletedPart.class);
            when(part1.getPartNumberValue()).thenReturn(1);
            when(part1.getPresignedUrlValue()).thenReturn("https://presigned/part1");
            when(part2.getPartNumberValue()).thenReturn(2);
            when(part2.getPresignedUrlValue()).thenReturn("https://presigned/part2");

            List<CompletedPart> completedParts = List.of(part1, part2);

            // when
            InitMultipartUploadResponse result = assembler.toResponse(session, completedParts);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo(sessionUuid.toString());
            assertThat(result.uploadId()).isEqualTo(S3_UPLOAD_ID);
            assertThat(result.totalParts()).isEqualTo(2);
            assertThat(result.partSize()).isEqualTo(PART_SIZE);
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
            assertThat(result.parts()).hasSize(2);
            assertThat(result.parts().get(0).partNumber()).isEqualTo(1);
            assertThat(result.parts().get(1).partNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toCompleteResponse")
    class ToCompleteResponse {

        @Test
        @DisplayName("완료된 MultipartUploadSession을 CompleteMultipartUploadResponse로 변환한다")
        void toCompleteResponse_ShouldConvertCompletedSessionToResponse() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            UUID sessionUuid = UUID.randomUUID();
            LocalDateTime completedAt = LocalDateTime.now();

            when(session.getId()).thenReturn(UploadSessionId.of(sessionUuid));
            when(session.getStatus()).thenReturn(SessionStatus.COMPLETED);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);
            when(session.getS3UploadIdValue()).thenReturn(S3_UPLOAD_ID);
            when(session.getTotalPartsValue()).thenReturn(2);
            when(session.getCompletedAt()).thenReturn(completedAt);

            CompletedPart part1 = mock(CompletedPart.class);
            when(part1.getPartNumberValue()).thenReturn(1);
            when(part1.getETagValue()).thenReturn(ETAG);
            when(part1.getSize()).thenReturn(PART_SIZE);
            when(part1.getUploadedAt()).thenReturn(completedAt.minusMinutes(5));

            List<CompletedPart> completedParts = List.of(part1);

            // when
            CompleteMultipartUploadResponse result =
                    assembler.toCompleteResponse(session, completedParts);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo(sessionUuid.toString());
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
            assertThat(result.uploadId()).isEqualTo(S3_UPLOAD_ID);
            assertThat(result.totalParts()).isEqualTo(2);
            assertThat(result.completedParts()).hasSize(1);
            assertThat(result.completedAt()).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("toCompleteMarkPartResponse")
    class ToCompleteMarkPartResponse {

        @Test
        @DisplayName("CompletedPart를 MarkPartUploadedResponse로 변환한다")
        void toCompleteMarkPartResponse_ShouldConvertPartToResponse() {
            // given
            CompletedPart completedPart = mock(CompletedPart.class);
            LocalDateTime uploadedAt = LocalDateTime.now();

            when(completedPart.getSessionIdValue()).thenReturn("session-123");
            when(completedPart.getPartNumberValue()).thenReturn(1);
            when(completedPart.getETagValue()).thenReturn(ETAG);
            when(completedPart.getUploadedAt()).thenReturn(uploadedAt);

            // when
            MarkPartUploadedResponse result = assembler.toCompleteMarkPartResponse(completedPart);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo("session-123");
            assertThat(result.partNumber()).isEqualTo(1);
            assertThat(result.etag()).isEqualTo(ETAG);
            assertThat(result.uploadedAt()).isEqualTo(uploadedAt);
        }
    }

    @Nested
    @DisplayName("toInitialCompletedParts")
    class ToInitialCompletedParts {

        @Test
        @DisplayName("Part별 Presigned URL을 생성하고 초기 CompletedPart 목록을 반환한다")
        void toInitialCompletedParts_ShouldCreatePartsWithPresignedUrls() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            UUID sessionUuid = UUID.randomUUID();

            when(session.getTotalPartsValue()).thenReturn(3);
            when(session.getId()).thenReturn(UploadSessionId.of(sessionUuid));
            when(session.getBucket()).thenReturn(S3Bucket.of(BUCKET));
            when(session.getS3Key()).thenReturn(S3Key.of(S3_KEY));
            when(session.getS3UploadIdValue()).thenReturn(S3_UPLOAD_ID);
            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);

            MultiPartUploadAssembler.PartPresignedUrlGenerator generator =
                    (bucket, s3Key, uploadId, partNumber) -> "https://presigned/part" + partNumber;

            // when
            List<CompletedPart> result = assembler.toInitialCompletedParts(session, generator);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getPartNumberValue()).isEqualTo(1);
            assertThat(result.get(0).getPresignedUrlValue()).isEqualTo("https://presigned/part1");
            assertThat(result.get(1).getPartNumberValue()).isEqualTo(2);
            assertThat(result.get(1).getPresignedUrlValue()).isEqualTo("https://presigned/part2");
            assertThat(result.get(2).getPartNumberValue()).isEqualTo(3);
            assertThat(result.get(2).getPresignedUrlValue()).isEqualTo("https://presigned/part3");
        }
    }
}
