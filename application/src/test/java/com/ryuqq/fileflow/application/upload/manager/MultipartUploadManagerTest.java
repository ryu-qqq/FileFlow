package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.MultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadPartFixture;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MultipartUploadManager 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultipartUploadManager 단위 테스트")
class MultipartUploadManagerTest {

    @Mock
    private MultipartUploadPort multipartUploadPort;

    @InjectMocks
    private MultipartUploadManager manager;

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTests {

        @Test
        @DisplayName("save_Success_Create - 신규 생성")
        void save_Success_Create() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.createNew();
            MultipartUpload savedUpload = MultipartUploadFixture.reconstituteDefault(1L);

            when(multipartUploadPort.save(upload)).thenReturn(savedUpload);

            // When
            MultipartUpload result = manager.save(upload);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(multipartUploadPort).save(upload);
        }

        @Test
        @DisplayName("save_Success_Update - 기존 데이터 업데이트")
        void save_Success_Update() {
            // Given
            MultipartUpload upload = MultipartUploadFixture.reconstituteDefault(1L);
            MultipartUpload updatedUpload = MultipartUploadFixture.reconstituteDefault(1L);

            when(multipartUploadPort.save(upload)).thenReturn(updatedUpload);

            // When
            MultipartUpload result = manager.save(upload);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(multipartUploadPort).save(upload);
        }
    }

    @Nested
    @DisplayName("complete 메서드 테스트")
    class CompleteTests {

        @Test
        @DisplayName("complete_Success_ById - ID로 조회 후 완료")
        void complete_Success_ById() {
            // Given
            Long id = 1L;
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

            when(multipartUploadPort.findById(id)).thenReturn(Optional.of(upload));
            when(multipartUploadPort.save(any(MultipartUpload.class))).thenReturn(completedUpload);

            // When
            MultipartUpload result = manager.complete(id);

            // Then
            assertThat(result).isNotNull();
            verify(multipartUploadPort).findById(id);
            verify(multipartUploadPort).save(any(MultipartUpload.class));
        }

        @Test
        @DisplayName("complete_Success_ByDomain - Domain Aggregate 완료")
        void complete_Success_ByDomain() {
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

            when(multipartUploadPort.save(any(MultipartUpload.class))).thenReturn(completedUpload);

            // When
            MultipartUpload result = manager.complete(upload);

            // Then
            assertThat(result).isNotNull();
            verify(multipartUploadPort).save(any(MultipartUpload.class));
        }

        @Test
        @DisplayName("complete_ThrowsException_WhenNotFound - 조회 실패")
        void complete_ThrowsException_WhenNotFound() {
            // Given
            Long id = 999L;
            when(multipartUploadPort.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.complete(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multipart Upload not found");

            verify(multipartUploadPort, never()).save(any());
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

            when(multipartUploadPort.save(any(MultipartUpload.class))).thenReturn(updatedUpload);

            // When
            MultipartUpload result = manager.addPart(upload, part);

            // Then
            assertThat(result).isNotNull();
            verify(multipartUploadPort).save(any(MultipartUpload.class));
        }
    }
}

