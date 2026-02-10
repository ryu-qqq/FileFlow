package com.ryuqq.fileflow.application.transform.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.port.out.client.ImageTransformClient;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
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
@DisplayName("ImageProcessingManager 단위 테스트")
class ImageProcessingManagerTest {

    @InjectMocks private ImageProcessingManager sut;
    @Mock private ImageTransformClient imageTransformClient;

    @Nested
    @DisplayName("process 메서드")
    class ProcessTest {

        @Test
        @DisplayName("성공: 이미지를 처리하고 결과를 반환한다")
        void process_Success_ReturnsResult() {
            // given
            byte[] sourceBytes = "fake-image-data".getBytes();
            TransformType type = TransformType.RESIZE;
            TransformParams params = TransformParams.forResize(800, 600, true);

            byte[] processedBytes = "processed-image-data".getBytes();
            ImageProcessingResult expected =
                    new ImageProcessingResult(processedBytes, 800, 600, "image/jpeg", "jpg");

            given(imageTransformClient.process(sourceBytes, type, params)).willReturn(expected);

            // when
            ImageProcessingResult result = sut.process(sourceBytes, type, params);

            // then
            assertThat(result).isEqualTo(expected);
            then(imageTransformClient).should().process(sourceBytes, type, params);
        }

        @Test
        @DisplayName("실패: 클라이언트 예외 시 그대로 전파한다")
        void process_ClientThrows_PropagatesException() {
            // given
            byte[] sourceBytes = "fake-image-data".getBytes();
            TransformType type = TransformType.RESIZE;
            TransformParams params = TransformParams.forResize(800, 600, true);

            given(imageTransformClient.process(sourceBytes, type, params))
                    .willThrow(new RuntimeException("Image processing failed"));

            // when & then
            assertThatThrownBy(() -> sut.process(sourceBytes, type, params))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Image processing failed");
        }
    }
}
