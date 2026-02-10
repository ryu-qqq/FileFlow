package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.session.SessionApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.AddCompletedPartApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SessionCommandApiMapper 단위 테스트.
 *
 * <p>API Request -> Application Command 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("SessionCommandApiMapper 단위 테스트")
class SessionCommandApiMapperTest {

    private SessionCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SessionCommandApiMapper();
    }

    @Nested
    @DisplayName("toCommand(CreateSingleUploadSessionApiRequest)")
    class ToCreateSingleUploadSessionCommandTest {

        @Test
        @DisplayName("단건 업로드 세션 생성 요청을 Command로 변환한다")
        void toCommand_createSingleUploadSession_success() {
            // given
            CreateSingleUploadSessionApiRequest request =
                    SessionApiFixtures.createSingleUploadSessionRequest();

            // when
            CreateSingleUploadSessionCommand command = mapper.toCommand(request);

            // then
            assertThat(command.fileName()).isEqualTo(request.fileName());
            assertThat(command.contentType()).isEqualTo(request.contentType());
            assertThat(command.accessType()).isEqualTo(request.accessType());
            assertThat(command.purpose()).isEqualTo(request.purpose());
            assertThat(command.source()).isEqualTo(request.source());
        }
    }

    @Nested
    @DisplayName("toCommand(String, CompleteSingleUploadSessionApiRequest)")
    class ToCompleteSingleUploadSessionCommandTest {

        @Test
        @DisplayName("단건 업로드 세션 완료 요청을 Command로 변환한다")
        void toCommand_completeSingleUploadSession_success() {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            CompleteSingleUploadSessionApiRequest request =
                    SessionApiFixtures.completeSingleUploadSessionRequest();

            // when
            CompleteSingleUploadSessionCommand command = mapper.toCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.fileSize()).isEqualTo(request.fileSize());
            assertThat(command.etag()).isEqualTo(request.etag());
        }
    }

    @Nested
    @DisplayName("toCommand(CreateMultipartUploadSessionApiRequest)")
    class ToCreateMultipartUploadSessionCommandTest {

        @Test
        @DisplayName("멀티파트 업로드 세션 생성 요청을 Command로 변환한다")
        void toCommand_createMultipartUploadSession_success() {
            // given
            CreateMultipartUploadSessionApiRequest request =
                    SessionApiFixtures.createMultipartUploadSessionRequest();

            // when
            CreateMultipartUploadSessionCommand command = mapper.toCommand(request);

            // then
            assertThat(command.fileName()).isEqualTo(request.fileName());
            assertThat(command.contentType()).isEqualTo(request.contentType());
            assertThat(command.accessType()).isEqualTo(request.accessType());
            assertThat(command.partSize()).isEqualTo(request.partSize());
            assertThat(command.purpose()).isEqualTo(request.purpose());
            assertThat(command.source()).isEqualTo(request.source());
        }
    }

    @Nested
    @DisplayName("toCommand(String, int) - GeneratePresignedPartUrl")
    class ToGeneratePresignedPartUrlCommandTest {

        @Test
        @DisplayName("세션 ID와 파트 번호로 Presigned URL 발급 Command를 생성한다")
        void toCommand_generatePresignedPartUrl_success() {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            int partNumber = 3;

            // when
            GeneratePresignedPartUrlCommand command = mapper.toCommand(sessionId, partNumber);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.partNumber()).isEqualTo(partNumber);
        }
    }

    @Nested
    @DisplayName("toCommand(String, AddCompletedPartApiRequest)")
    class ToAddCompletedPartCommandTest {

        @Test
        @DisplayName("파트 업로드 완료 기록 요청을 Command로 변환한다")
        void toCommand_addCompletedPart_success() {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            AddCompletedPartApiRequest request = SessionApiFixtures.addCompletedPartRequest();

            // when
            AddCompletedPartCommand command = mapper.toCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.partNumber()).isEqualTo(request.partNumber());
            assertThat(command.etag()).isEqualTo(request.etag());
            assertThat(command.size()).isEqualTo(request.size());
        }
    }

    @Nested
    @DisplayName("toCommand(String, CompleteMultipartUploadSessionApiRequest)")
    class ToCompleteMultipartUploadSessionCommandTest {

        @Test
        @DisplayName("멀티파트 업로드 세션 완료 요청을 Command로 변환한다")
        void toCommand_completeMultipartUploadSession_success() {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;
            CompleteMultipartUploadSessionApiRequest request =
                    SessionApiFixtures.completeMultipartUploadSessionRequest();

            // when
            CompleteMultipartUploadSessionCommand command = mapper.toCommand(sessionId, request);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
            assertThat(command.totalFileSize()).isEqualTo(request.totalFileSize());
            assertThat(command.etag()).isEqualTo(request.etag());
        }
    }

    @Nested
    @DisplayName("toAbortCommand(String)")
    class ToAbortCommandTest {

        @Test
        @DisplayName("세션 ID로 중단 Command를 생성한다")
        void toAbortCommand_success() {
            // given
            String sessionId = SessionApiFixtures.SESSION_ID;

            // when
            AbortMultipartUploadSessionCommand command = mapper.toAbortCommand(sessionId);

            // then
            assertThat(command.sessionId()).isEqualTo(sessionId);
        }
    }
}
