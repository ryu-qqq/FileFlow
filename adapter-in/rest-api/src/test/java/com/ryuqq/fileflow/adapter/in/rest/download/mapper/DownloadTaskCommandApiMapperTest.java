package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.download.DownloadTaskApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.CreateDownloadTaskApiRequest;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * DownloadTaskCommandApiMapper 단위 테스트.
 *
 * <p>API Request -> Application Command 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("DownloadTaskCommandApiMapper 단위 테스트")
class DownloadTaskCommandApiMapperTest {

    private DownloadTaskCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DownloadTaskCommandApiMapper();
    }

    @Nested
    @DisplayName("toCommand(CreateDownloadTaskApiRequest)")
    class ToCreateDownloadTaskCommandTest {

        @Test
        @DisplayName("다운로드 작업 생성 요청을 Command로 변환한다")
        void toCommand_createDownloadTask_success() {
            // given
            CreateDownloadTaskApiRequest request =
                    DownloadTaskApiFixtures.createDownloadTaskRequest();

            // when
            CreateDownloadTaskCommand command = mapper.toCommand(request);

            // then
            assertThat(command.sourceUrl()).isEqualTo(request.sourceUrl());
            assertThat(command.s3Key()).isEqualTo(request.s3Key());
            assertThat(command.bucket()).isEqualTo(request.bucket());
            assertThat(command.accessType()).isEqualTo(request.accessType());
            assertThat(command.purpose()).isEqualTo(request.purpose());
            assertThat(command.source()).isEqualTo(request.source());
            assertThat(command.callbackUrl()).isEqualTo(request.callbackUrl());
        }
    }
}
