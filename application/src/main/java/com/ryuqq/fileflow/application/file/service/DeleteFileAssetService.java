package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.DeleteFileAssetUseCase;
import com.ryuqq.fileflow.application.file.port.out.DeleteFileRelationshipPort;
import com.ryuqq.fileflow.application.upload.port.out.DeleteFileAssetPort;
import com.ryuqq.fileflow.application.upload.port.out.LoadFileAssetPort;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * FileAsset 삭제 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * FileAsset 삭제 및 Cascade 삭제 비즈니스 로직을 처리합니다.
 *
 * Cascade 삭제 규칙:
 * - 원본 파일 삭제 시, 해당 파일과 관련된 모든 FileRelationship 삭제
 * - 썸네일, 최적화 이미지, 변환 이미지 등 파생 파일과의 관계 모두 삭제
 * - 원본이 삭제되면 대상(target) 파일로서의 관계도 삭제
 *
 * 트랜잭션 보장:
 * - FileRelationship 삭제와 FileAsset 삭제는 하나의 트랜잭션으로 처리
 * - 실패 시 모두 롤백하여 데이터 일관성 유지
 *
 * @author sangwon-ryu
 */
@Service
public class DeleteFileAssetService implements DeleteFileAssetUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteFileAssetService.class);

    private final LoadFileAssetPort loadFileAssetPort;
    private final DeleteFileAssetPort deleteFileAssetPort;
    private final DeleteFileRelationshipPort deleteFileRelationshipPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param loadFileAssetPort FileAsset 로드 Port
     * @param deleteFileAssetPort FileAsset 삭제 Port
     * @param deleteFileRelationshipPort FileRelationship 삭제 Port
     */
    public DeleteFileAssetService(
            LoadFileAssetPort loadFileAssetPort,
            DeleteFileAssetPort deleteFileAssetPort,
            DeleteFileRelationshipPort deleteFileRelationshipPort
    ) {
        this.loadFileAssetPort = Objects.requireNonNull(
                loadFileAssetPort,
                "LoadFileAssetPort must not be null"
        );
        this.deleteFileAssetPort = Objects.requireNonNull(
                deleteFileAssetPort,
                "DeleteFileAssetPort must not be null"
        );
        this.deleteFileRelationshipPort = Objects.requireNonNull(
                deleteFileRelationshipPort,
                "DeleteFileRelationshipPort must not be null"
        );
    }

    /**
     * FileAsset을 삭제하고 관련된 모든 FileRelationship을 cascade 삭제합니다.
     *
     * 삭제 순서:
     * 1. FileAsset 존재 확인 (예외 발생 시 트랜잭션 롤백)
     * 2. 관련된 모든 FileRelationship 삭제 (원본/대상 모두)
     * 3. FileAsset 삭제
     *
     * @param fileId 삭제할 파일 ID
     * @throws com.ryuqq.fileflow.domain.upload.exception.FileAssetNotFoundException 파일이 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void deleteFileAsset(FileId fileId) {
        Objects.requireNonNull(fileId, "FileId must not be null");

        log.info("Starting FileAsset deletion with cascade: {}", fileId.value());

        // 1. FileAsset 존재 확인 (없으면 FileAssetNotFoundException 발생)
        loadFileAssetPort.getById(fileId);

        // 2. 관련된 모든 FileRelationship 삭제 (cascade)
        int deletedRelationships = deleteFileRelationshipPort.deleteByFileId(fileId);
        log.debug("Deleted {} file relationships for FileId: {}", deletedRelationships, fileId.value());

        // 3. FileAsset 삭제
        boolean deleted = deleteFileAssetPort.deleteById(fileId);

        if (deleted) {
            log.info("Successfully deleted FileAsset: {} (cascade deleted {} relationships)",
                    fileId.value(), deletedRelationships);
        } else {
            log.warn("FileAsset deletion returned false for FileId: {}", fileId.value());
        }
    }

    /**
     * 여러 FileAsset을 일괄 삭제합니다.
     *
     * 각 파일에 대해 cascade 삭제를 수행하며,
     * 개별 파일 삭제 실패 시에도 다른 파일 삭제를 계속 진행합니다.
     *
     * @param fileIds 삭제할 파일 ID 목록
     * @return 성공적으로 삭제된 파일 개수
     */
    @Override
    @Transactional
    public int deleteFileAssets(Iterable<FileId> fileIds) {
        Objects.requireNonNull(fileIds, "FileIds must not be null");

        int deletedCount = 0;

        for (FileId fileId : fileIds) {
            try {
                deleteFileAsset(fileId);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete FileAsset: {}", fileId.value(), e);
                // 계속 진행 (부분 실패 허용)
            }
        }

        log.info("Batch deletion completed: {} files successfully deleted", deletedCount);
        return deletedCount;
    }
}
