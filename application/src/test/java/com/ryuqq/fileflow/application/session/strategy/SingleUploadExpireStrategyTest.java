package com.ryuqq.fileflow.application.session.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.fixture.SingleUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SingleUploadExpireStrategy 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SingleUploadExpireStrategyTest {

    @Mock private UploadSessionManager uploadSessionManager;

    @InjectMocks private SingleUploadExpireStrategy singleUploadExpireStrategy;

    @Nested
    @DisplayName("expire")
    class Expire {

        @Test
        @DisplayName("첫 번째 ACTIVE 상태의 세션을 만료 처리하고 저장한다")
        void expire_WhenFirstActiveSession_ShouldExpireAndSave() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            singleUploadExpireStrategy.expire(session);

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("ACTIVE 상태의 세션을 만료 처리하고 저장한다")
        void expire_WhenActiveSession_ShouldExpireAndSave() {
            // given
            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            singleUploadExpireStrategy.expire(session);

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("만료 처리 시 도메인의 expire 메서드를 호출한다")
        void expire_ShouldCallDomainExpireMethod() {
            // given
            SingleUploadSession session =
                    spy(SingleUploadSessionFixture.activeSingleUploadSession());
            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            singleUploadExpireStrategy.expire(session);

            // then
            verify(session).expire();
            verify(uploadSessionManager).save(session);
        }
    }
}
