package com.ryuqq.fileflow.adapter.in.rest.transform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.command.CreateTransformRequestApiRequest;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TransformRequestCommandApiMapper 단위 테스트.
 *
 * <p>API Request -> Application Command 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("TransformRequestCommandApiMapper 단위 테스트")
class TransformRequestCommandApiMapperTest {

    private TransformRequestCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransformRequestCommandApiMapper();
    }

    @Nested
    @DisplayName("toCommand(CreateTransformRequestApiRequest)")
    class ToCreateTransformRequestCommandTest {

        @Test
        @DisplayName("이미지 변환 요청 생성 요청을 Command로 변환한다")
        void toCommand_createTransformRequest_success() {
            // given
            CreateTransformRequestApiRequest request =
                    TransformRequestApiFixtures.createTransformRequestRequest();

            // when
            CreateTransformRequestCommand command = mapper.toCommand(request);

            // then
            assertThat(command.sourceAssetId()).isEqualTo(request.sourceAssetId());
            assertThat(command.transformType()).isEqualTo(request.transformType());
            assertThat(command.width()).isEqualTo(request.width());
            assertThat(command.height()).isEqualTo(request.height());
            assertThat(command.quality()).isEqualTo(request.quality());
            assertThat(command.targetFormat()).isEqualTo(request.targetFormat());
        }
    }
}
