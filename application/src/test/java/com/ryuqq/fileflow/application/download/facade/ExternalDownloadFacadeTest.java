package com.ryuqq.fileflow.application.download.facade;

import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * ExternalDownloadFacade 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>findById() - Optional 조회</li>
 *   <li>getById() - Required 조회 (없으면 예외)</li>
 *   <li>save() - 저장</li>
 *   <li>findByUploadSessionId() - 세션 ID로 조회</li>
 *   <li>getByUploadSessionId() - Required 세션 조회</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>Port 위임 정확성</li>
 *   <li>Optional 처리</li>
 *   <li>Exception 처리 (DownloadNotFoundException)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadFacade 단위 테스트")
class ExternalDownloadFacadeTest {

    @Mock
    private ExternalDownloadCommandPort commandPort;

    @Mock
    private ExternalDownloadQueryPort queryPort;

    @InjectMocks
    private ExternalDownloadFacade facade;

    private ExternalDownload mockDownload;
    private Long downloadId;
    private Long uploadSessionId;

    @BeforeEach
    void setUp() {
        downloadId = 1L;
        uploadSessionId = 100L;
        mockDownload = createMockDownload(downloadId, uploadSessionId);
    }

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("findById_WithExistingId_ShouldReturnDownload - 기존 ID로 조회 성공")
        void findById_WithExistingId_ShouldReturnDownload() {
            // Given
            given(queryPort.findById(downloadId)).willReturn(Optional.of(mockDownload));

            // When
            Optional<ExternalDownload> result = facade.findById(downloadId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(mockDownload);
            verify(queryPort).findById(downloadId);
        }

        @Test
        @DisplayName("findById_WithNonExistentId_ShouldReturnEmpty - 존재하지 않는 ID 조회 시 Empty")
        void findById_WithNonExistentId_ShouldReturnEmpty() {
            // Given
            Long nonExistentId = 999L;
            given(queryPort.findById(nonExistentId)).willReturn(Optional.empty());

            // When
            Optional<ExternalDownload> result = facade.findById(nonExistentId);

            // Then
            assertThat(result).isEmpty();
            verify(queryPort).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("getById 메서드 테스트")
    class GetByIdTests {

        @Test
        @DisplayName("getById_WithExistingId_ShouldReturnDownload - 기존 ID로 조회 성공")
        void getById_WithExistingId_ShouldReturnDownload() {
            // Given
            given(queryPort.findById(downloadId)).willReturn(Optional.of(mockDownload));

            // When
            ExternalDownload result = facade.getById(downloadId);

            // Then
            assertThat(result).isEqualTo(mockDownload);
            verify(queryPort).findById(downloadId);
        }

        @Test
        @DisplayName("getById_WithNonExistentId_ShouldThrowException - 존재하지 않는 ID 조회 시 예외")
        void getById_WithNonExistentId_ShouldThrowException() {
            // Given
            Long nonExistentId = 999L;
            given(queryPort.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> facade.getById(nonExistentId))
                .isInstanceOf(DownloadNotFoundException.class)
                .hasMessageContaining("Download not found");

            verify(queryPort).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_WithValidDownload_ShouldReturnSavedDownload - 정상 저장")
        void save_WithValidDownload_ShouldReturnSavedDownload() {
            // Given
            ExternalDownload savedDownload = createMockDownload(2L, 200L);
            given(commandPort.save(any(ExternalDownload.class))).willReturn(savedDownload);

            // When
            ExternalDownload result = facade.save(mockDownload);

            // Then
            assertThat(result).isEqualTo(savedDownload);
            verify(commandPort).save(mockDownload);
        }
    }

    @Nested
    @DisplayName("findByUploadSessionId 메서드 테스트")
    class FindByUploadSessionIdTests {

        @Test
        @DisplayName("findByUploadSessionId_WithExistingSessionId_ShouldReturnDownload - 기존 세션 ID로 조회 성공")
        void findByUploadSessionId_WithExistingSessionId_ShouldReturnDownload() {
            // Given
            given(queryPort.findByUploadSessionId(uploadSessionId)).willReturn(Optional.of(mockDownload));

            // When
            Optional<ExternalDownload> result = facade.findByUploadSessionId(uploadSessionId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(mockDownload);
            verify(queryPort).findByUploadSessionId(uploadSessionId);
        }

        @Test
        @DisplayName("findByUploadSessionId_WithNonExistentSessionId_ShouldReturnEmpty - 존재하지 않는 세션 ID 조회 시 Empty")
        void findByUploadSessionId_WithNonExistentSessionId_ShouldReturnEmpty() {
            // Given
            Long nonExistentSessionId = 999L;
            given(queryPort.findByUploadSessionId(nonExistentSessionId)).willReturn(Optional.empty());

            // When
            Optional<ExternalDownload> result = facade.findByUploadSessionId(nonExistentSessionId);

            // Then
            assertThat(result).isEmpty();
            verify(queryPort).findByUploadSessionId(nonExistentSessionId);
        }
    }

    @Nested
    @DisplayName("getByUploadSessionId 메서드 테스트")
    class GetByUploadSessionIdTests {

        @Test
        @DisplayName("getByUploadSessionId_WithExistingSessionId_ShouldReturnDownload - 기존 세션 ID로 조회 성공")
        void getByUploadSessionId_WithExistingSessionId_ShouldReturnDownload() {
            // Given
            given(queryPort.findByUploadSessionId(uploadSessionId)).willReturn(Optional.of(mockDownload));

            // When
            ExternalDownload result = facade.getByUploadSessionId(uploadSessionId);

            // Then
            assertThat(result).isEqualTo(mockDownload);
            verify(queryPort).findByUploadSessionId(uploadSessionId);
        }

        @Test
        @DisplayName("getByUploadSessionId_WithNonExistentSessionId_ShouldThrowException - 존재하지 않는 세션 ID 조회 시 예외")
        void getByUploadSessionId_WithNonExistentSessionId_ShouldThrowException() {
            // Given
            Long nonExistentSessionId = 999L;
            given(queryPort.findByUploadSessionId(nonExistentSessionId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> facade.getByUploadSessionId(nonExistentSessionId))
                .isInstanceOf(DownloadNotFoundException.class)
                .hasMessageContaining("Download not found");

            verify(queryPort).findByUploadSessionId(nonExistentSessionId);
        }
    }

    // ===== Helper Methods =====

    /**
     * Mock ExternalDownload 생성
     *
     * @param downloadId 다운로드 ID
     * @param uploadSessionId 업로드 세션 ID
     * @return Mock ExternalDownload
     */
    private ExternalDownload createMockDownload(Long downloadId, Long uploadSessionId) {
        // Note: ExternalDownload 실제 생성 방법에 따라 수정 필요
        // 여기서는 Mockito를 사용하여 Mock 객체 생성
        ExternalDownload download = org.mockito.Mockito.mock(ExternalDownload.class);
        lenient().when(download.getIdValue()).thenReturn(downloadId);
        return download;
    }
}
