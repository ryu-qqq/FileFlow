package com.ryuqq.fileflow.application.session.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.session.port.out.query.MultipartUploadSessionQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.SingleUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import java.util.Optional;
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
@DisplayName("SessionReadManager 단위 테스트")
class SessionReadManagerTest {

    @InjectMocks private SessionReadManager sut;
    @Mock private SingleUploadSessionQueryPort singleQueryPort;
    @Mock private MultipartUploadSessionQueryPort multipartQueryPort;

    @Nested
    @DisplayName("getSingle 메서드")
    class GetSingleTest {

        @Test
        @DisplayName("존재하는 세션 ID로 SingleUploadSession을 반환한다")
        void getSingle_ExistingId_ReturnsSession() {
            // given
            String sessionId = "single-session-001";
            SingleUploadSession expectedSession = SingleUploadSessionFixture.aCreatedSession();

            given(singleQueryPort.findById(SingleUploadSessionId.of(sessionId)))
                    .willReturn(Optional.of(expectedSession));

            // when
            SingleUploadSession result = sut.getSingle(sessionId);

            // then
            assertThat(result).isEqualTo(expectedSession);
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 SessionNotFoundException을 던진다")
        void getSingle_NonExistingId_ThrowsSessionNotFoundException() {
            // given
            String sessionId = "non-existing-session";

            given(singleQueryPort.findById(SingleUploadSessionId.of(sessionId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getSingle(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMultipart 메서드")
    class GetMultipartTest {

        @Test
        @DisplayName("존재하는 세션 ID로 MultipartUploadSession을 반환한다")
        void getMultipart_ExistingId_ReturnsSession() {
            // given
            String sessionId = "multipart-session-001";
            MultipartUploadSession expectedSession =
                    MultipartUploadSessionFixture.anInitiatedSession();

            given(multipartQueryPort.findById(MultipartUploadSessionId.of(sessionId)))
                    .willReturn(Optional.of(expectedSession));

            // when
            MultipartUploadSession result = sut.getMultipart(sessionId);

            // then
            assertThat(result).isEqualTo(expectedSession);
        }

        @Test
        @DisplayName("존재하지 않는 세션 ID로 SessionNotFoundException을 던진다")
        void getMultipart_NonExistingId_ThrowsSessionNotFoundException() {
            // given
            String sessionId = "non-existing-session";

            given(multipartQueryPort.findById(MultipartUploadSessionId.of(sessionId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getMultipart(sessionId))
                    .isInstanceOf(SessionNotFoundException.class);
        }
    }
}
