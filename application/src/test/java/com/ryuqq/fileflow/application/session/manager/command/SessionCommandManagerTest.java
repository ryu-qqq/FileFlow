package com.ryuqq.fileflow.application.session.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.port.out.command.MultipartUploadSessionPersistencePort;
import com.ryuqq.fileflow.application.session.port.out.command.SingleUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
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
@DisplayName("SessionCommandManager 단위 테스트")
class SessionCommandManagerTest {

    @InjectMocks private SessionCommandManager sut;
    @Mock private SingleUploadSessionPersistencePort singlePersistencePort;
    @Mock private MultipartUploadSessionPersistencePort multipartPersistencePort;

    @Nested
    @DisplayName("persist(SingleUploadSession) 메서드")
    class PersistSingleTest {

        @Test
        @DisplayName("SingleUploadSession을 영속화한다")
        void persist_SingleSession_DelegatesToPort() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();

            // when
            sut.persist(session);

            // then
            then(singlePersistencePort).should().persist(session);
        }
    }

    @Nested
    @DisplayName("persist(MultipartUploadSession) 메서드")
    class PersistMultipartTest {

        @Test
        @DisplayName("MultipartUploadSession을 영속화한다")
        void persist_MultipartSession_DelegatesToPort() {
            // given
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            // when
            sut.persist(session);

            // then
            then(multipartPersistencePort).should().persist(session);
        }
    }
}
