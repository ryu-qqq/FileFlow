package com.ryuqq.fileflow.application.session.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.port.out.command.CompletedPartPersistencePort;
import com.ryuqq.fileflow.application.session.port.out.command.MultipartUploadSessionPersistencePort;
import com.ryuqq.fileflow.application.session.port.out.command.SingleUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UploadSessionManager 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UploadSessionManagerTest {

    @Mock private SingleUploadSessionPersistencePort singleUploadSessionPersistencePort;
    @Mock private MultipartUploadSessionPersistencePort multipartUploadSessionPersistencePort;
    @Mock private CompletedPartPersistencePort completedPartPersistencePort;

    private UploadSessionManager manager;

    @BeforeEach
    void setUp() {
        manager =
                new UploadSessionManager(
                        singleUploadSessionPersistencePort,
                        multipartUploadSessionPersistencePort,
                        completedPartPersistencePort);
    }

    @Nested
    @DisplayName("save(SingleUploadSession)")
    class SaveSingleUploadSession {

        @Test
        @DisplayName("SingleUploadSession을 저장하고 반환한다")
        void save_ShouldPersistAndReturnSingleUploadSession() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession savedSession = mock(SingleUploadSession.class);

            when(singleUploadSessionPersistencePort.persist(session)).thenReturn(savedSession);

            // when
            SingleUploadSession result = manager.save(session);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(singleUploadSessionPersistencePort).persist(session);
        }
    }

    @Nested
    @DisplayName("save(MultipartUploadSession)")
    class SaveMultipartUploadSession {

        @Test
        @DisplayName("MultipartUploadSession을 저장하고 반환한다")
        void save_ShouldPersistAndReturnMultipartUploadSession() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            MultipartUploadSession savedSession = mock(MultipartUploadSession.class);

            when(multipartUploadSessionPersistencePort.persist(session)).thenReturn(savedSession);

            // when
            MultipartUploadSession result = manager.save(session);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(multipartUploadSessionPersistencePort).persist(session);
        }
    }

    @Nested
    @DisplayName("saveCompletedPart")
    class SaveCompletedPart {

        @Test
        @DisplayName("CompletedPart를 저장하고 반환한다")
        void saveCompletedPart_ShouldPersistAndReturnPart() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            CompletedPart completedPart = mock(CompletedPart.class);
            CompletedPart savedPart = mock(CompletedPart.class);

            when(completedPartPersistencePort.persist(sessionId, completedPart))
                    .thenReturn(savedPart);

            // when
            CompletedPart result = manager.saveCompletedPart(sessionId, completedPart);

            // then
            assertThat(result).isEqualTo(savedPart);
            verify(completedPartPersistencePort).persist(sessionId, completedPart);
        }
    }

    @Nested
    @DisplayName("saveAllCompletedParts")
    class SaveAllCompletedParts {

        @Test
        @DisplayName("여러 CompletedPart를 순차적으로 저장한다")
        void saveAllCompletedParts_ShouldPersistAllPartsSequentially() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            CompletedPart part1 = mock(CompletedPart.class);
            CompletedPart part2 = mock(CompletedPart.class);
            CompletedPart part3 = mock(CompletedPart.class);
            List<CompletedPart> parts = List.of(part1, part2, part3);

            // when
            manager.saveAllCompletedParts(sessionId, parts);

            // then
            verify(completedPartPersistencePort).persist(sessionId, part1);
            verify(completedPartPersistencePort).persist(sessionId, part2);
            verify(completedPartPersistencePort).persist(sessionId, part3);
            verify(completedPartPersistencePort, times(3)).persist(eq(sessionId), any());
        }

        @Test
        @DisplayName("빈 리스트인 경우 저장하지 않는다")
        void saveAllCompletedParts_ShouldNotPersistWhenEmptyList() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            List<CompletedPart> parts = List.of();

            // when
            manager.saveAllCompletedParts(sessionId, parts);

            // then
            verify(completedPartPersistencePort, never()).persist(any(), any());
        }
    }
}
