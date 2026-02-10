package com.ryuqq.fileflow.application.transform.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.port.out.command.TransformRequestPersistencePort;
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
@DisplayName("TransformCommandManager 단위 테스트")
class TransformCommandManagerTest {

    @InjectMocks private TransformCommandManager sut;
    @Mock private TransformRequestPersistencePort persistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("TransformRequest를 영속화 포트에 위임한다")
        void persist_TransformRequest_DelegatesToPort() {
            // given
            TransformRequest transformRequest = TransformRequestFixture.aResizeRequest();

            // when
            sut.persist(transformRequest);

            // then
            then(persistencePort).should().persist(transformRequest);
        }
    }
}
