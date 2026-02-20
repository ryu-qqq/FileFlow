package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.assembler.TransformAssembler;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.validator.SourceAssetValidator;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
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
@DisplayName("CreateTransformRequestService 단위 테스트")
class CreateTransformRequestServiceTest {

    @InjectMocks private CreateTransformRequestService sut;
    @Mock private SourceAssetValidator sourceAssetValidator;
    @Mock private TransformCommandFactory transformCommandFactory;
    @Mock private TransformCommandManager transformCommandManager;
    @Mock private TransformQueueOutboxCommandManager transformQueueOutboxCommandManager;
    @Mock private TransformAssembler transformAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("유효한 커맨드로 변환 요청을 생성하고 아웃박스에 기록한 후 응답을 반환한다")
        void execute_ValidCommand_CreatesRequestAndOutboxAndReturnsResponse() {
            // given
            CreateTransformRequestCommand command =
                    new CreateTransformRequestCommand("asset-001", "RESIZE", 800, 600, null, null);
            String sourceContentType = "image/jpeg";
            TransformRequest transformRequest = TransformRequestFixture.aResizeRequest();
            TransformQueueOutbox outbox =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"),
                            transformRequest.idValue(),
                            Instant.now());
            TransformRequestResponse expectedResponse =
                    new TransformRequestResponse(
                            transformRequest.idValue(),
                            transformRequest.sourceAssetIdValue(),
                            transformRequest.sourceContentType(),
                            transformRequest.type().name(),
                            transformRequest.params().width(),
                            transformRequest.params().height(),
                            transformRequest.params().quality(),
                            transformRequest.params().targetFormat(),
                            transformRequest.status().name(),
                            transformRequest.resultAssetIdValue(),
                            transformRequest.lastError(),
                            transformRequest.createdAt(),
                            transformRequest.completedAt());

            given(sourceAssetValidator.validateAndGetContentType("asset-001"))
                    .willReturn(sourceContentType);
            given(transformCommandFactory.createTransformRequest(command, sourceContentType))
                    .willReturn(transformRequest);
            given(transformCommandFactory.createQueueOutbox(transformRequest.idValue()))
                    .willReturn(outbox);
            given(transformAssembler.toResponse(transformRequest)).willReturn(expectedResponse);

            // when
            TransformRequestResponse result = sut.execute(command);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(sourceAssetValidator).should().validateAndGetContentType("asset-001");
            then(transformCommandFactory)
                    .should()
                    .createTransformRequest(command, sourceContentType);
            then(transformCommandManager).should().persist(transformRequest);
            then(transformCommandFactory).should().createQueueOutbox(transformRequest.idValue());
            then(transformQueueOutboxCommandManager)
                    .should()
                    .persist(any(TransformQueueOutbox.class));
            then(transformAssembler).should().toResponse(transformRequest);
        }
    }
}
