package com.ryuqq.fileflow.application.session.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SingleUploadAssembler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SingleUploadAssemblerTest {

    private static final String IDEMPOTENCY_KEY = "11111111-1111-1111-1111-111111111111";
    private static final String FILE_NAME = "test-file.jpg";
    private static final long FILE_SIZE = 1_024L;
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final String BUCKET = "test-bucket";
    private static final String S3_KEY = "uploads/2025/01/01/test-file.jpg";
    private static final String PRESIGNED_URL = "https://s3.amazonaws.com/presigned";
    private static final String ETAG = "d41d8cd98f00b204e9800998ecf8427e";

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("UTC"));

    @Mock private ClockHolder clockHolder;
    @Mock private Supplier<UserContext> userContextSupplier;
    @Mock private UserContext userContext;

    private SingleUploadAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SingleUploadAssembler(clockHolder, userContextSupplier);
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("Command를 SingleUploadSession 도메인으로 변환한다")
        void toDomain_ShouldConvertCommandToDomain() {
            // given
            InitSingleUploadCommand command =
                    InitSingleUploadCommand.of(
                            IDEMPOTENCY_KEY,
                            FILE_NAME,
                            FILE_SIZE,
                            CONTENT_TYPE,
                            1L,
                            2L,
                            3L,
                            "user@test.com");

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            when(userContextSupplier.get()).thenReturn(userContext);
            when(userContext.getS3Bucket()).thenReturn(S3Bucket.of(BUCKET));
            when(userContext.generateS3KeyToday(null, FILE_NAME)).thenReturn(S3Key.of(S3_KEY));

            // when
            SingleUploadSession result = assembler.toDomain(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFileNameValue()).isEqualTo(FILE_NAME);
            assertThat(result.getFileSizeValue()).isEqualTo(FILE_SIZE);
            assertThat(result.getContentTypeValue()).isEqualTo(CONTENT_TYPE);
            assertThat(result.getBucketValue()).isEqualTo(BUCKET);
            assertThat(result.getS3KeyValue()).isEqualTo(S3_KEY);
            assertThat(result.getStatus()).isEqualTo(SessionStatus.PREPARING);
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("SingleUploadSession을 InitSingleUploadResponse로 변환한다")
        void toResponse_ShouldConvertDomainToResponse() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            when(session.getIdValue()).thenReturn("session-123");
            when(session.getPresignedUrlValue()).thenReturn(PRESIGNED_URL);
            when(session.getExpiresAt()).thenReturn(expiresAt);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);

            // when
            InitSingleUploadResponse result = assembler.toResponse(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo("session-123");
            assertThat(result.presignedUrl()).isEqualTo(PRESIGNED_URL);
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
        }
    }

    @Nested
    @DisplayName("toCompleteResponse")
    class ToCompleteResponse {

        @Test
        @DisplayName("완료된 SingleUploadSession을 CompleteSingleUploadResponse로 변환한다")
        void toCompleteResponse_ShouldConvertCompletedSessionToResponse() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            LocalDateTime completedAt = LocalDateTime.now();

            when(session.getIdValue()).thenReturn("session-123");
            when(session.getStatus()).thenReturn(SessionStatus.COMPLETED);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);
            when(session.getETagValue()).thenReturn(ETAG);
            when(session.getCompletedAt()).thenReturn(completedAt);

            // when
            CompleteSingleUploadResponse result = assembler.toCompleteResponse(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo("session-123");
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
            assertThat(result.etag()).isEqualTo(ETAG);
            assertThat(result.completedAt()).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("toExpireResponse")
    class ToExpireResponse {

        @Test
        @DisplayName("만료된 SingleUploadSession을 ExpireUploadSessionResponse로 변환한다")
        void toExpireResponse_ShouldConvertExpiredSessionToResponse() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            LocalDateTime expiresAt = LocalDateTime.now().minusMinutes(1);

            when(session.getIdValue()).thenReturn("session-123");
            when(session.getStatus()).thenReturn(SessionStatus.EXPIRED);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);
            when(session.getExpiresAt()).thenReturn(expiresAt);

            // when
            ExpireUploadSessionResponse result = assembler.toExpireResponse(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo("session-123");
            assertThat(result.status()).isEqualTo("EXPIRED");
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    @DisplayName("toCancelResponse")
    class ToCancelResponse {

        @Test
        @DisplayName("취소된 SingleUploadSession을 CancelUploadSessionResponse로 변환한다")
        void toCancelResponse_ShouldConvertCancelledSessionToResponse() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);

            when(session.getIdValue()).thenReturn("session-123");
            when(session.getStatus()).thenReturn(SessionStatus.FAILED);
            when(session.getBucketValue()).thenReturn(BUCKET);
            when(session.getS3KeyValue()).thenReturn(S3_KEY);

            // when
            CancelUploadSessionResponse result = assembler.toCancelResponse(session);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sessionId()).isEqualTo("session-123");
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.bucket()).isEqualTo(BUCKET);
            assertThat(result.key()).isEqualTo(S3_KEY);
        }
    }
}
