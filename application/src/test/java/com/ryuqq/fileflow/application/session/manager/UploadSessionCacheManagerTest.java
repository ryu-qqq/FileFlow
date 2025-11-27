package com.ryuqq.fileflow.application.session.manager;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.session.port.out.command.UploadSessionCachePersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UploadSessionCacheManager 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UploadSessionCacheManagerTest {

    private static final Duration SINGLE_UPLOAD_TTL = Duration.ofMinutes(15);
    private static final Duration MULTIPART_UPLOAD_TTL = Duration.ofHours(24);

    @Mock private UploadSessionCachePersistencePort uploadSessionCachePersistencePort;

    private UploadSessionCacheManager manager;

    @BeforeEach
    void setUp() {
        manager = new UploadSessionCacheManager(uploadSessionCachePersistencePort);
    }

    @Nested
    @DisplayName("cacheSingleUpload")
    class CacheSingleUpload {

        @Test
        @DisplayName("SingleUploadSession을 캐시에 저장한다")
        void cacheSingleUpload_ShouldPersistToCache() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            // debug log에서만 사용되므로 lenient로 설정
            lenient().when(session.getIdValue()).thenReturn("session-123");

            // when
            manager.cacheSingleUpload(session, SINGLE_UPLOAD_TTL);

            // then
            verify(uploadSessionCachePersistencePort).persist(session, SINGLE_UPLOAD_TTL);
        }

        @Test
        @DisplayName("캐시 저장 실패 시 예외를 던지지 않고 로그만 남긴다")
        void cacheSingleUpload_ShouldNotThrowExceptionOnCacheFailure() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            when(session.getIdValue()).thenReturn("session-123");
            when(session.getIdempotencyKey()).thenReturn(IdempotencyKey.of(UUID.randomUUID()));

            doThrow(new RuntimeException("Redis connection failed"))
                    .when(uploadSessionCachePersistencePort)
                    .persist(session, SINGLE_UPLOAD_TTL);

            // when & then (no exception thrown)
            manager.cacheSingleUpload(session, SINGLE_UPLOAD_TTL);

            verify(uploadSessionCachePersistencePort).persist(session, SINGLE_UPLOAD_TTL);
        }
    }

    @Nested
    @DisplayName("cacheMultipartUpload")
    class CacheMultipartUpload {

        @Test
        @DisplayName("MultipartUploadSession을 캐시에 저장한다")
        void cacheMultipartUpload_ShouldPersistToCache() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            when(session.getId()).thenReturn(sessionId);

            // when
            manager.cacheMultipartUpload(session, MULTIPART_UPLOAD_TTL);

            // then
            verify(uploadSessionCachePersistencePort).persist(session, MULTIPART_UPLOAD_TTL);
        }

        @Test
        @DisplayName("캐시 저장 실패 시 예외를 던지지 않고 로그만 남긴다")
        void cacheMultipartUpload_ShouldNotThrowExceptionOnCacheFailure() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            when(session.getId()).thenReturn(sessionId);

            doThrow(new RuntimeException("Redis connection failed"))
                    .when(uploadSessionCachePersistencePort)
                    .persist(session, MULTIPART_UPLOAD_TTL);

            // when & then (no exception thrown)
            manager.cacheMultipartUpload(session, MULTIPART_UPLOAD_TTL);

            verify(uploadSessionCachePersistencePort).persist(session, MULTIPART_UPLOAD_TTL);
        }
    }
}
