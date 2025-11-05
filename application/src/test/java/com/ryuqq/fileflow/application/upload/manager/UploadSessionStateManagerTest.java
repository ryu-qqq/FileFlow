package com.ryuqq.fileflow.application.upload.manager;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * UploadSessionStateManager 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>save 메서드 (Command 전담)</li>
 *   <li>delete 메서드 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>변경 사항:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Query 메서드 제거 (findById, findBySessionKey 등)</li>
 *   <li>✅ Port 분리: SaveUploadSessionPort, DeleteUploadSessionPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UploadSessionStateManager 단위 테스트")
class UploadSessionStateManagerTest {

    @Mock
    private SaveUploadSessionPort savePort;

    @Mock
    private DeleteUploadSessionPort deletePort;

    @InjectMocks
    private UploadSessionStateManager stateManager;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_Success - 세션 저장 성공")
        void save_Success() {
            // Given
            UploadSession session = UploadSessionFixture.createSingle();
            UploadSession savedSession = UploadSessionFixture.reconstituteDefault(1L);

            when(savePort.save(session)).thenReturn(savedSession);

            // When
            UploadSession result = stateManager.save(session);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdValue()).isEqualTo(1L);
            verify(savePort).save(session);
        }
    }

    @Nested
    @DisplayName("delete 메서드 테스트")
    class DeleteTests {

        @Test
        @DisplayName("delete_Success - 세션 삭제 성공")
        void delete_Success() {
            // Given
            Long id = 1L;

            // When
            stateManager.delete(id);

            // Then
            verify(deletePort).delete(id);
        }
    }
}

