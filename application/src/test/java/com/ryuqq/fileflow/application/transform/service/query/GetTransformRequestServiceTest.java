package com.ryuqq.fileflow.application.transform.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.assembler.TransformAssembler;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
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
@DisplayName("GetTransformRequestService 단위 테스트")
class GetTransformRequestServiceTest {

    @InjectMocks private GetTransformRequestService sut;
    @Mock private TransformReadManager transformReadManager;
    @Mock private TransformAssembler transformAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("변환 요청 ID로 조회하고 응답으로 변환하여 반환한다")
        void execute_ValidId_ReturnsResponse() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest transformRequest = TransformRequestFixture.aResizeRequest();
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

            given(transformReadManager.getTransformRequest(transformRequestId))
                    .willReturn(transformRequest);
            given(transformAssembler.toResponse(transformRequest)).willReturn(expectedResponse);

            // when
            TransformRequestResponse result = sut.execute(transformRequestId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(transformReadManager).should().getTransformRequest(transformRequestId);
            then(transformAssembler).should().toResponse(transformRequest);
        }
    }
}
