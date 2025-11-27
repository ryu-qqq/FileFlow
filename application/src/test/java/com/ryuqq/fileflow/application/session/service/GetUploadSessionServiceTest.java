package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.UploadSessionQueryAssembler;
import com.ryuqq.fileflow.application.session.dto.query.GetUploadSessionQuery;
import com.ryuqq.fileflow.application.session.dto.response.UploadSessionDetailResponse;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetUploadSessionService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GetUploadSessionServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final long TENANT_ID = 20L;
    private static final String FILE_NAME = "document.pdf";
    private static final long FILE_SIZE = 1024 * 1024L;
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String S3_KEY = "uploads/document.pdf";

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private FindCompletedPartQueryPort findCompletedPartQueryPort;
    @Mock private UploadSessionQueryAssembler uploadSessionQueryAssembler;

    @InjectMocks private GetUploadSessionService getUploadSessionService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("SingleUploadSession을 조회하여 상세 응답을 반환한다")
        void execute_SingleSession_ShouldReturnDetailResponse() {
            // given
            GetUploadSessionQuery query = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);
            UploadSessionId sessionId = UploadSessionId.of(SESSION_ID);
            SingleUploadSession singleSession = mock(SingleUploadSession.class);
            UploadSessionDetailResponse expectedResponse = createSingleDetailResponse();

            when(findUploadSessionQueryPort.findByIdAndTenantId(sessionId, TENANT_ID))
                    .thenReturn(Optional.of(singleSession));
            when(uploadSessionQueryAssembler.toDetailResponse(eq(singleSession), isNull()))
                    .thenReturn(expectedResponse);

            // when
            UploadSessionDetailResponse response = getUploadSessionService.execute(query);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            assertThat(response.uploadType()).isEqualTo("SINGLE");

            verify(findUploadSessionQueryPort).findByIdAndTenantId(sessionId, TENANT_ID);
            verify(findCompletedPartQueryPort, never()).findAllBySessionId(any());
            verify(uploadSessionQueryAssembler).toDetailResponse(singleSession, null);
        }

        @Test
        @DisplayName("MultipartUploadSession을 조회하여 Part 정보와 함께 상세 응답을 반환한다")
        void execute_MultipartSession_ShouldReturnDetailResponseWithParts() {
            // given
            GetUploadSessionQuery query = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);
            UploadSessionId sessionId = UploadSessionId.of(SESSION_ID);
            MultipartUploadSession multipartSession = mock(MultipartUploadSession.class);
            UploadSessionId multipartSessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));

            List<CompletedPart> completedParts =
                    List.of(mock(CompletedPart.class), mock(CompletedPart.class));
            UploadSessionDetailResponse expectedResponse = createMultipartDetailResponse();

            when(findUploadSessionQueryPort.findByIdAndTenantId(sessionId, TENANT_ID))
                    .thenReturn(Optional.of(multipartSession));
            when(multipartSession.getId()).thenReturn(multipartSessionId);
            when(findCompletedPartQueryPort.findAllBySessionId(multipartSessionId))
                    .thenReturn(completedParts);
            when(uploadSessionQueryAssembler.toDetailResponse(multipartSession, completedParts))
                    .thenReturn(expectedResponse);

            // when
            UploadSessionDetailResponse response = getUploadSessionService.execute(query);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
            assertThat(response.uploadedParts()).isEqualTo(2);

            verify(findUploadSessionQueryPort).findByIdAndTenantId(sessionId, TENANT_ID);
            verify(findCompletedPartQueryPort).findAllBySessionId(multipartSessionId);
            verify(uploadSessionQueryAssembler).toDetailResponse(multipartSession, completedParts);
        }

        @Test
        @DisplayName("세션이 존재하지 않으면 SessionNotFoundException을 던진다")
        void execute_NotFound_ShouldThrowException() {
            // given
            GetUploadSessionQuery query = GetUploadSessionQuery.of(SESSION_ID, TENANT_ID);
            UploadSessionId sessionId = UploadSessionId.of(SESSION_ID);

            when(findUploadSessionQueryPort.findByIdAndTenantId(sessionId, TENANT_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> getUploadSessionService.execute(query))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining(SESSION_ID);

            verify(findCompletedPartQueryPort, never()).findAllBySessionId(any());
            verify(uploadSessionQueryAssembler, never()).toDetailResponse(any(), any());
        }
    }

    private UploadSessionDetailResponse createSingleDetailResponse() {
        return UploadSessionDetailResponse.ofSingle(
                SESSION_ID,
                FILE_NAME,
                FILE_SIZE,
                CONTENT_TYPE,
                SessionStatus.COMPLETED,
                BUCKET_NAME,
                S3_KEY,
                "etag-123",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(23),
                LocalDateTime.now());
    }

    private UploadSessionDetailResponse createMultipartDetailResponse() {
        return UploadSessionDetailResponse.ofMultipart(
                SESSION_ID,
                FILE_NAME,
                FILE_SIZE,
                CONTENT_TYPE,
                SessionStatus.ACTIVE,
                BUCKET_NAME,
                S3_KEY,
                "upload-id-123",
                5,
                2,
                List.of(),
                null,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(23),
                null);
    }
}
