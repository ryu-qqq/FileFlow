package com.ryuqq.fileflow.application.upload.manager;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadPartFixture;

/**
 * MultipartUploadStateManager 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>save 메서드 (Command 전담)</li>
 *   <li>complete 메서드 (Domain Aggregate 기반)</li>
 *   <li>abort 메서드 (Domain Aggregate 기반)</li>
 *   <li>fail 메서드 (Domain Aggregate 기반)</li>
 *   <li>addPart 메서드</li>
 *   <li>delete 메서드 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>변경 사항:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Query 메서드 제거 (findById, findByUploadSessionId 등)</li>
 *   <li>✅ Port 분리: SaveMultipartUploadPort, DeleteMultipartUploadPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultipartUploadStateManager 단위 테스트")
class MultipartUploadStateManagerTest {

    @Mock
    private SaveMultipartUploadPort savePort;

    @Mock
    private DeleteMultipartUploadPort deletePort;

    @InjectMocks
    private MultipartUploadStateManager stateManager;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_Success_Create - 신규 생성")
        void save_Success_Create() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();
            MultipartUpload savedUpload = MultipartUploadFixture.reconstituteDefault(1L);

            when(savePort.save(upload)).thenReturn(savedUpload);

            // When
            MultipartUpload result = stateManager.save(upload);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdValue()).isEqualTo(1L);
            verify(savePort).save(upload);
        }

        @Test
        @DisplayName("save_Success_Update - 기존 데이터 업데이트")
        void save_Success_Update() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.reconstituteDefault(1L);
            MultipartUpload updatedUpload = MultipartUploadFixture.reconstituteDefault(1L);

            when(savePort.save(upload)).thenReturn(updatedUpload);

            // When
            MultipartUpload result = stateManager.save(upload);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIdValue()).isEqualTo(1L);
            verify(savePort).save(upload);
        }
    }

    @Nested
    @DisplayName("complete 메서드 테스트")
    class CompleteTests {

        @Test
        @DisplayName("complete_Success - Domain Aggregate 완료")
        void complete_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(2)
                .build();

            MultipartUpload completedUpload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(2)
                .complete()
                .build();

            when(savePort.save(any(MultipartUpload.class))).thenReturn(completedUpload);

            // When
            MultipartUpload result = stateManager.complete(upload);

            // Then
            assertThat(result).isNotNull();
            verify(savePort).save(any(MultipartUpload.class));
        }
    }

    @Nested
    @DisplayName("abort 메서드 테스트")
    class AbortTests {

        @Test
        @DisplayName("abort_Success - Domain Aggregate 중단")
        void abort_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(1)
                .build();

            MultipartUpload abortedUpload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(1)
                .abort()
                .build();

            when(savePort.save(any(MultipartUpload.class))).thenReturn(abortedUpload);

            // When
            MultipartUpload result = stateManager.abort(upload);

            // Then
            assertThat(result).isNotNull();
            verify(savePort).save(any(MultipartUpload.class));
        }
    }

    @Nested
    @DisplayName("fail 메서드 테스트")
    class FailTests {

        @Test
        @DisplayName("fail_Success - Domain Aggregate 실패 처리")
        void fail_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(1)
                .build();

            MultipartUpload failedUpload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(1)
                .fail()
                .build();

            when(savePort.save(any(MultipartUpload.class))).thenReturn(failedUpload);

            // When
            MultipartUpload result = stateManager.fail(upload);

            // Then
            assertThat(result).isNotNull();
            verify(savePort).save(any(MultipartUpload.class));
        }
    }

    @Nested
    @DisplayName("addPart 메서드 테스트")
    class AddPartTests {

        @Test
        @DisplayName("addPart_Success - 파트 추가 성공")
        void addPart_Success() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(3))
                .initiate()
                .build();

            UploadPart part = UploadPartFixture.create(1, 5242880L);
            MultipartUpload updatedUpload = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(1L))
                .totalParts(TotalParts.of(3))
                .initiate()
                .addParts(1)
                .build();

            when(savePort.save(any(MultipartUpload.class))).thenReturn(updatedUpload);

            // When
            MultipartUpload result = stateManager.addPart(upload, part);

            // Then
            assertThat(result).isNotNull();
            verify(savePort).save(any(MultipartUpload.class));
        }
    }

    @Nested
    @DisplayName("delete 메서드 테스트")
    class DeleteTests {

        @Test
        @DisplayName("delete_Success - Multipart Upload 삭제 성공")
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

