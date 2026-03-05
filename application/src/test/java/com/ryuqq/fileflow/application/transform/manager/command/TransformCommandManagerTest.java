package com.ryuqq.fileflow.application.transform.manager.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
        @DisplayName("TransformRequest를 영속화하고 version을 업데이트한다")
        void persist_TransformRequest_DelegatesToPortAndUpdatesVersion() {
            // given
            TransformRequest transformRequest = TransformRequestFixture.aResizeRequest();
            long expectedVersion = 1L;
            given(persistencePort.persist(transformRequest)).willReturn(expectedVersion);

            // when
            sut.persist(transformRequest);

            // then
            then(persistencePort).should().persist(transformRequest);
            assertThat(transformRequest.version()).isEqualTo(expectedVersion);
        }
    }
}
