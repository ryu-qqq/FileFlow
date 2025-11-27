package com.ryuqq.fileflow.application.session.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.dto.query.ListUploadSessionsQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionResponse;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionQueryAssembler 단위 테스트")
class UploadSessionQueryAssemblerTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final long TENANT_ID = 20L;
    private static final long ORGANIZATION_ID = 10L;
    private static final String FILE_NAME = "document.pdf";
    private static final long FILE_SIZE = 1024 * 1024L;
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String S3_KEY = "uploads/document.pdf";

    private final UploadSessionQueryAssembler assembler = new UploadSessionQueryAssembler();

    @Nested
    @DisplayName("toCriteria")
    class ToCriteria {

        @Test
        @DisplayName("ListUploadSessionsQuery를 검색 조건으로 변환한다")
        void toCriteria_ShouldConvertQueryToCriteria() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(
                            TENANT_ID, ORGANIZATION_ID, SessionStatus.COMPLETED, "SINGLE", 1, 20);

            // when
            UploadSessionSearchCriteria criteria = assembler.toCriteria(query);

            // then
            assertThat(criteria.tenantId()).isEqualTo(TENANT_ID);
            assertThat(criteria.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(criteria.status()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(criteria.uploadType()).isEqualTo("SINGLE");
            assertThat(criteria.offset()).isEqualTo(20L);
            assertThat(criteria.limit()).isEqualTo(20);
        }

        @Test
        @DisplayName("nullable 필드가 null인 경우에도 변환한다")
        void toCriteria_WithNullFields_ShouldConvertQueryToCriteria() {
            // given
            ListUploadSessionsQuery query =
                    ListUploadSessionsQuery.of(TENANT_ID, ORGANIZATION_ID, null, null, 0, 10);

            // when
            UploadSessionSearchCriteria criteria = assembler.toCriteria(query);

            // then
            assertThat(criteria.status()).isNull();
            assertThat(criteria.uploadType()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("SingleUploadSession을 UploadSessionResponse로 변환한다")
        void toResponse_SingleSession_ShouldConvert() {
            // given
            SingleUploadSession session = createMockSingleSession();

            // when
            UploadSessionResponse response = assembler.toResponse(session);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.fileName()).isEqualTo(FILE_NAME);
            assertThat(response.fileSize()).isEqualTo(FILE_SIZE);
            assertThat(response.contentType()).isEqualTo(CONTENT_TYPE);
            assertThat(response.uploadType()).isEqualTo("SINGLE");
            assertThat(response.status()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(response.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(response.key()).isEqualTo(S3_KEY);
        }

        @Test
        @DisplayName("MultipartUploadSession을 UploadSessionResponse로 변환한다")
        void toResponse_MultipartSession_ShouldConvert() {
            // given
            MultipartUploadSession session = createMockMultipartSession();

            // when
            UploadSessionResponse response = assembler.toResponse(session);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
        }

        @Test
        @DisplayName("알 수 없는 세션 타입이면 예외를 던진다")
        void toResponse_UnknownSessionType_ShouldThrowException() {
            // given
            UploadSession unknownSession = mock(UploadSession.class);

            // when & then
            assertThatThrownBy(() -> assembler.toResponse(unknownSession))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown UploadSession type");
        }
    }

    @Nested
    @DisplayName("toResponses")
    class ToResponses {

        @Test
        @DisplayName("세션 목록을 응답 목록으로 변환한다")
        void toResponses_ShouldConvertList() {
            // given
            SingleUploadSession singleSession = createMockSingleSession();
            MultipartUploadSession multipartSession = createMockMultipartSession();
            List<UploadSession> sessions = List.of(singleSession, multipartSession);

            // when
            List<UploadSessionResponse> responses = assembler.toResponses(sessions);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).uploadType()).isEqualTo("SINGLE");
            assertThat(responses.get(1).uploadType()).isEqualTo("MULTIPART");
        }

        @Test
        @DisplayName("빈 목록을 변환하면 빈 응답 목록을 반환한다")
        void toResponses_EmptyList_ShouldReturnEmptyList() {
            // given
            List<UploadSession> sessions = List.of();

            // when
            List<UploadSessionResponse> responses = assembler.toResponses(sessions);

            // then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDetailResponse")
    class ToDetailResponse {

        @Test
        @DisplayName("SingleUploadSession을 상세 응답으로 변환한다")
        void toDetailResponse_SingleSession_ShouldConvert() {
            // given
            SingleUploadSession session = createMockSingleSession();
            when(session.getETagValue()).thenReturn("etag-123");
            when(session.getCompletedAt()).thenReturn(LocalDateTime.now());

            // when
            UploadSessionDetailResponse response = assembler.toDetailResponse(session, null);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.uploadType()).isEqualTo("SINGLE");
            assertThat(response.etag()).isEqualTo("etag-123");
            assertThat(response.uploadId()).isNull();
            assertThat(response.totalParts()).isNull();
        }

        @Test
        @DisplayName("MultipartUploadSession을 Part 정보와 함께 상세 응답으로 변환한다")
        void toDetailResponse_MultipartSession_ShouldConvertWithParts() {
            // given
            MultipartUploadSession session = createMockMultipartSession();
            when(session.getS3UploadIdValue()).thenReturn("upload-id-123");
            when(session.getTotalPartsValue()).thenReturn(5);
            ETag mergedETag = new ETag("merged-etag-123");
            when(session.getMergedETag()).thenReturn(mergedETag);
            when(session.getCompletedAt()).thenReturn(LocalDateTime.now());

            CompletedPart part1 = createMockCompletedPart(1, "part-etag-1", 1024L);
            CompletedPart part2 = createMockCompletedPart(2, "part-etag-2", 2048L);
            List<CompletedPart> completedParts = List.of(part1, part2);

            // when
            UploadSessionDetailResponse response =
                    assembler.toDetailResponse(session, completedParts);

            // then
            assertThat(response.sessionId()).isEqualTo(SESSION_ID);
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
            assertThat(response.uploadId()).isEqualTo("upload-id-123");
            assertThat(response.totalParts()).isEqualTo(5);
            assertThat(response.uploadedParts()).isEqualTo(2);
            assertThat(response.parts()).hasSize(2);
            assertThat(response.etag()).isEqualTo("merged-etag-123");
        }

        @Test
        @DisplayName("MultipartUploadSession에서 Part 정보가 null이면 빈 목록을 반환한다")
        void toDetailResponse_MultipartSession_NullParts_ShouldReturnEmptyParts() {
            // given
            MultipartUploadSession session = createMockMultipartSession();
            when(session.getS3UploadIdValue()).thenReturn("upload-id-123");
            when(session.getTotalPartsValue()).thenReturn(5);
            when(session.getMergedETag()).thenReturn(null);
            when(session.getCompletedAt()).thenReturn(null);

            // when
            UploadSessionDetailResponse response = assembler.toDetailResponse(session, null);

            // then
            assertThat(response.parts()).isEmpty();
            assertThat(response.etag()).isNull();
        }

        @Test
        @DisplayName("알 수 없는 세션 타입이면 예외를 던진다")
        void toDetailResponse_UnknownSessionType_ShouldThrowException() {
            // given
            UploadSession unknownSession = mock(UploadSession.class);

            // when & then
            assertThatThrownBy(() -> assembler.toDetailResponse(unknownSession, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown UploadSession type");
        }
    }

    private SingleUploadSession createMockSingleSession() {
        SingleUploadSession session = mock(SingleUploadSession.class);
        when(session.getIdValue()).thenReturn(SESSION_ID);
        when(session.getFileNameValue()).thenReturn(FILE_NAME);
        when(session.getFileSizeValue()).thenReturn(FILE_SIZE);
        when(session.getContentTypeValue()).thenReturn(CONTENT_TYPE);
        when(session.getStatus()).thenReturn(SessionStatus.COMPLETED);
        when(session.getBucketValue()).thenReturn(BUCKET_NAME);
        when(session.getS3KeyValue()).thenReturn(S3_KEY);
        when(session.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(1));
        when(session.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(23));
        return session;
    }

    private MultipartUploadSession createMockMultipartSession() {
        MultipartUploadSession session = mock(MultipartUploadSession.class);
        UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
        when(session.getId()).thenReturn(sessionId);
        when(session.getFileNameValue()).thenReturn(FILE_NAME);
        when(session.getFileSizeValue()).thenReturn(FILE_SIZE);
        when(session.getContentTypeValue()).thenReturn(CONTENT_TYPE);
        when(session.getStatus()).thenReturn(SessionStatus.ACTIVE);
        when(session.getBucketValue()).thenReturn(BUCKET_NAME);
        when(session.getS3KeyValue()).thenReturn(S3_KEY);
        when(session.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(1));
        when(session.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(23));
        return session;
    }

    private CompletedPart createMockCompletedPart(int partNumber, String etagValue, long size) {
        CompletedPart part = mock(CompletedPart.class);
        when(part.getPartNumberValue()).thenReturn(partNumber);
        when(part.getETagValue()).thenReturn(etagValue);
        when(part.getSize()).thenReturn(size);
        when(part.getUploadedAt()).thenReturn(LocalDateTime.now());
        return part;
    }
}
