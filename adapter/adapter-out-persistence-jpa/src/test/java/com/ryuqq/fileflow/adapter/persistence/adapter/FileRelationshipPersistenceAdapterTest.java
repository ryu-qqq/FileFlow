package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.entity.FileRelationshipEntity;
import com.ryuqq.fileflow.adapter.persistence.mapper.FileRelationshipMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.FileRelationshipJpaRepository;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.FileRelationshipType;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * FileRelationshipPersistenceAdapter 단위 테스트
 *
 * 테스트 시나리오:
 * 1. 단일 FileRelationship 저장
 * 2. 배치 FileRelationship 저장
 * 3. Source FileId로 조회
 * 4. Target FileId로 조회
 * 5. FileId로 조회 (Source 또는 Target)
 * 6. FileId로 삭제
 * 7. Source FileId로 삭제
 * 8. Target FileId로 삭제
 * 9. 예외 처리 검증
 *
 * @author sangwon-ryu (sangwon@company.com)
 * @since 2025-10-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileRelationshipPersistenceAdapter 단위 테스트")
class FileRelationshipPersistenceAdapterTest {

    @Mock
    private FileRelationshipJpaRepository fileRelationshipRepository;

    @Mock
    private FileRelationshipMapper fileRelationshipMapper;

    private FileRelationshipPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FileRelationshipPersistenceAdapter(
                fileRelationshipRepository,
                fileRelationshipMapper
        );
    }

    @Test
    @DisplayName("단일 FileRelationship를 정상적으로 저장한다")
    void shouldSaveSingleFileRelationship() {
        // Given
        FileId sourceFileId = FileId.generate();
        FileId targetFileId = FileId.generate();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("thumbnail_size", "SMALL");
        metadata.put("width", 300);

        FileRelationship relationship = FileRelationship.create(
                sourceFileId,
                targetFileId,
                FileRelationshipType.THUMBNAIL_SMALL,
                metadata
        );

        FileRelationshipEntity entity = FileRelationshipEntity.of(
                sourceFileId.value(),
                targetFileId.value(),
                FileRelationshipEntity.FileRelationshipTypeEntity.THUMBNAIL_SMALL,
                metadata
        );

        FileRelationshipEntity savedEntity = FileRelationshipEntity.reconstitute(
                1L,
                sourceFileId.value(),
                targetFileId.value(),
                FileRelationshipEntity.FileRelationshipTypeEntity.THUMBNAIL_SMALL,
                metadata,
                LocalDateTime.now()
        );

        FileRelationship savedRelationship = FileRelationship.reconstitute(
                1L,
                sourceFileId,
                targetFileId,
                FileRelationshipType.THUMBNAIL_SMALL,
                metadata,
                LocalDateTime.now()
        );

        when(fileRelationshipMapper.toEntity(relationship)).thenReturn(entity);
        when(fileRelationshipRepository.save(entity)).thenReturn(savedEntity);
        when(fileRelationshipMapper.toDomain(savedEntity)).thenReturn(savedRelationship);

        // When
        FileRelationship result = adapter.save(relationship);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceFileId()).isEqualTo(sourceFileId);
        assertThat(result.getTargetFileId()).isEqualTo(targetFileId);
        assertThat(result.getRelationshipType()).isEqualTo(FileRelationshipType.THUMBNAIL_SMALL);

        verify(fileRelationshipMapper).toEntity(relationship);
        verify(fileRelationshipRepository).save(entity);
        verify(fileRelationshipMapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("null FileRelationship 저장 시 예외가 발생한다")
    void shouldThrowExceptionWhenSavingNullFileRelationship() {
        // When & Then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("FileRelationship must not be null");

        verify(fileRelationshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("여러 FileRelationship를 배치로 저장한다")
    void shouldSaveBatchFileRelationships() {
        // Given
        FileId sourceFileId = FileId.generate();
        FileId targetFileId1 = FileId.generate();
        FileId targetFileId2 = FileId.generate();

        FileRelationship relationship1 = FileRelationship.create(
                sourceFileId,
                targetFileId1,
                FileRelationshipType.THUMBNAIL_SMALL,
                Map.of("size", "SMALL")
        );

        FileRelationship relationship2 = FileRelationship.create(
                sourceFileId,
                targetFileId2,
                FileRelationshipType.THUMBNAIL_MEDIUM,
                Map.of("size", "MEDIUM")
        );

        List<FileRelationship> relationships = List.of(relationship1, relationship2);

        FileRelationshipEntity entity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity2 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity1, entity2);

        FileRelationshipEntity savedEntity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity savedEntity2 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> savedEntities = List.of(savedEntity1, savedEntity2);

        when(fileRelationshipMapper.toEntity(relationship1)).thenReturn(entity1);
        when(fileRelationshipMapper.toEntity(relationship2)).thenReturn(entity2);
        when(fileRelationshipRepository.saveAll(anyList())).thenReturn(savedEntities);
        when(fileRelationshipMapper.toDomain(savedEntity1)).thenReturn(relationship1);
        when(fileRelationshipMapper.toDomain(savedEntity2)).thenReturn(relationship2);

        // When
        List<FileRelationship> result = adapter.saveAll(relationships);

        // Then
        assertThat(result).hasSize(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FileRelationshipEntity>> captor = 
                (ArgumentCaptor<List<FileRelationshipEntity>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(fileRelationshipRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);

        verify(fileRelationshipMapper, times(2)).toEntity(any(FileRelationship.class));
        verify(fileRelationshipMapper, times(2)).toDomain(any(FileRelationshipEntity.class));
    }

    @Test
    @DisplayName("빈 리스트로 배치 저장 시 예외가 발생한다")
    void shouldThrowExceptionWhenSavingEmptyList() {
        // When & Then
        assertThatThrownBy(() -> adapter.saveAll(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileRelationships list cannot be null or empty");

        verify(fileRelationshipRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Source FileId로 FileRelationship 목록을 조회한다")
    void shouldFindBySourceFileId() {
        // Given
        FileId sourceFileId = FileId.generate();
        String sourceFileIdValue = sourceFileId.value();

        FileRelationshipEntity entity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity2 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity1, entity2);

        FileRelationship relationship1 = mock(FileRelationship.class);
        FileRelationship relationship2 = mock(FileRelationship.class);

        when(fileRelationshipRepository.findBySourceFileId(sourceFileIdValue))
                .thenReturn(entities);
        when(fileRelationshipMapper.toDomain(entity1)).thenReturn(relationship1);
        when(fileRelationshipMapper.toDomain(entity2)).thenReturn(relationship2);

        // When
        List<FileRelationship> result = adapter.findBySourceFileId(sourceFileId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(relationship1, relationship2);

        verify(fileRelationshipRepository).findBySourceFileId(sourceFileIdValue);
        verify(fileRelationshipMapper, times(2)).toDomain(any(FileRelationshipEntity.class));
    }

    @Test
    @DisplayName("Target FileId로 FileRelationship 목록을 조회한다")
    void shouldFindByTargetFileId() {
        // Given
        FileId targetFileId = FileId.generate();
        String targetFileIdValue = targetFileId.value();

        FileRelationshipEntity entity = mock(FileRelationshipEntity.class);
        FileRelationship relationship = mock(FileRelationship.class);

        when(fileRelationshipRepository.findByTargetFileId(targetFileIdValue))
                .thenReturn(List.of(entity));
        when(fileRelationshipMapper.toDomain(entity)).thenReturn(relationship);

        // When
        List<FileRelationship> result = adapter.findByTargetFileId(targetFileId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(relationship);

        verify(fileRelationshipRepository).findByTargetFileId(targetFileIdValue);
        verify(fileRelationshipMapper).toDomain(entity);
    }

    @Test
    @DisplayName("FileId로 모든 관련 FileRelationship를 조회한다")
    void shouldFindByFileId() {
        // Given
        FileId fileId = FileId.generate();
        String fileIdValue = fileId.value();

        FileRelationshipEntity entity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity2 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity3 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity1, entity2, entity3);

        FileRelationship relationship1 = mock(FileRelationship.class);
        FileRelationship relationship2 = mock(FileRelationship.class);
        FileRelationship relationship3 = mock(FileRelationship.class);

        when(fileRelationshipRepository.findAllByFileId(fileIdValue))
                .thenReturn(entities);
        when(fileRelationshipMapper.toDomain(entity1)).thenReturn(relationship1);
        when(fileRelationshipMapper.toDomain(entity2)).thenReturn(relationship2);
        when(fileRelationshipMapper.toDomain(entity3)).thenReturn(relationship3);

        // When
        List<FileRelationship> result = adapter.findByFileId(fileId);

        // Then
        assertThat(result).hasSize(3);

        verify(fileRelationshipRepository).findAllByFileId(fileIdValue);
        verify(fileRelationshipMapper, times(3)).toDomain(any(FileRelationshipEntity.class));
    }

    @Test
    @DisplayName("FileId로 관련된 모든 FileRelationship를 삭제한다")
    void shouldDeleteByFileId() {
        // Given
        FileId fileId = FileId.generate();
        String fileIdValue = fileId.value();

        FileRelationshipEntity entity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity2 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity1, entity2);

        when(fileRelationshipRepository.findAllByFileId(fileIdValue))
                .thenReturn(entities);

        // When
        int deletedCount = adapter.deleteByFileId(fileId);

        // Then
        assertThat(deletedCount).isEqualTo(2);

        verify(fileRelationshipRepository).findAllByFileId(fileIdValue);
        verify(fileRelationshipRepository).deleteAll(entities);
    }

    @Test
    @DisplayName("삭제할 FileRelationship가 없으면 0을 반환한다")
    void shouldReturnZeroWhenNoFileRelationshipsToDelete() {
        // Given
        FileId fileId = FileId.generate();

        when(fileRelationshipRepository.findAllByFileId(fileId.value()))
                .thenReturn(List.of());

        // When
        int deletedCount = adapter.deleteByFileId(fileId);

        // Then
        assertThat(deletedCount).isEqualTo(0);

        verify(fileRelationshipRepository).findAllByFileId(fileId.value());
        verify(fileRelationshipRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("Source FileId로 FileRelationship를 삭제한다")
    void shouldDeleteBySourceFileId() {
        // Given
        FileId sourceFileId = FileId.generate();
        String sourceFileIdValue = sourceFileId.value();

        FileRelationshipEntity entity = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity);

        when(fileRelationshipRepository.findBySourceFileId(sourceFileIdValue))
                .thenReturn(entities);

        // When
        int deletedCount = adapter.deleteBySourceFileId(sourceFileId);

        // Then
        assertThat(deletedCount).isEqualTo(1);

        verify(fileRelationshipRepository).findBySourceFileId(sourceFileIdValue);
        verify(fileRelationshipRepository).deleteAll(entities);
    }

    @Test
    @DisplayName("Target FileId로 FileRelationship를 삭제한다")
    void shouldDeleteByTargetFileId() {
        // Given
        FileId targetFileId = FileId.generate();
        String targetFileIdValue = targetFileId.value();

        FileRelationshipEntity entity1 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity2 = mock(FileRelationshipEntity.class);
        FileRelationshipEntity entity3 = mock(FileRelationshipEntity.class);
        List<FileRelationshipEntity> entities = List.of(entity1, entity2, entity3);

        when(fileRelationshipRepository.findByTargetFileId(targetFileIdValue))
                .thenReturn(entities);

        // When
        int deletedCount = adapter.deleteByTargetFileId(targetFileId);

        // Then
        assertThat(deletedCount).isEqualTo(3);

        verify(fileRelationshipRepository).findByTargetFileId(targetFileIdValue);
        verify(fileRelationshipRepository).deleteAll(entities);
    }

    @Test
    @DisplayName("null Source FileId로 조회 시 예외가 발생한다")
    void shouldThrowExceptionWhenFindingByNullSourceFileId() {
        // When & Then
        assertThatThrownBy(() -> adapter.findBySourceFileId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("SourceFileId must not be null");

        verify(fileRelationshipRepository, never()).findBySourceFileId(anyString());
    }

    @Test
    @DisplayName("null Target FileId로 조회 시 예외가 발생한다")
    void shouldThrowExceptionWhenFindingByNullTargetFileId() {
        // When & Then
        assertThatThrownBy(() -> adapter.findByTargetFileId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TargetFileId must not be null");

        verify(fileRelationshipRepository, never()).findByTargetFileId(anyString());
    }

    @Test
    @DisplayName("null FileId로 삭제 시 예외가 발생한다")
    void shouldThrowExceptionWhenDeletingByNullFileId() {
        // When & Then
        assertThatThrownBy(() -> adapter.deleteByFileId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("FileId must not be null");

        verify(fileRelationshipRepository, never()).findAllByFileId(anyString());
    }
}
