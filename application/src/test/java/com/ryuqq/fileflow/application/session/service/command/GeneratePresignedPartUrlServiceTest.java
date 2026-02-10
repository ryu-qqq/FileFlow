package com.ryuqq.fileflow.application.session.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpec;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpecFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratePresignedPartUrlService 단위 테스트")
class GeneratePresignedPartUrlServiceTest {

    @InjectMocks private GeneratePresignedPartUrlService sut;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private MultipartUploadManager multipartUploadManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("세션을 조회하고 파트 Presigned URL을 생성하여 응답을 반환한다")
        void execute_ValidCommand_ReturnsPresignedPartUrlResponse() {
            // given
            String sessionId = "multipart-session-001";
            int partNumber = 1;
            GeneratePresignedPartUrlCommand command =
                    new GeneratePresignedPartUrlCommand(sessionId, partNumber);

            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            PartPresignedUrlSpec spec = PartPresignedUrlSpecFixture.aPartPresignedUrlSpec();
            String expectedUrl = "https://s3.presigned-part-url.com/test";

            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);
            given(multipartSessionCommandFactory.createPartPresignedUrlSpec(session, partNumber))
                    .willReturn(spec);
            given(multipartUploadManager.generatePresignedPartUrl(spec)).willReturn(expectedUrl);

            // when
            PresignedPartUrlResponse result = sut.execute(command);

            // then
            assertThat(result.presignedUrl()).isEqualTo(expectedUrl);
            assertThat(result.partNumber()).isEqualTo(spec.partNumber());
            assertThat(result.expiresInSeconds()).isEqualTo(spec.ttlSeconds());
            then(sessionReadManager).should().getMultipart(sessionId);
            then(multipartSessionCommandFactory)
                    .should()
                    .createPartPresignedUrlSpec(session, partNumber);
            then(multipartUploadManager).should().generatePresignedPartUrl(spec);
        }
    }
}
