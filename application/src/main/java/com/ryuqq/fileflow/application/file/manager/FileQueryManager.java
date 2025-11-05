package com.ryuqq.fileflow.application.file.manager;

import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * FileAsset Query Manager (CQRS Query)
 *
 * <p>FileAsset Domain Aggregate의 조회 전담 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>FileAsset 조회 (ID, Upload Session ID)</li>
 *   <li>조회 최적화 (Read-only Transaction)</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Query: FileQueryManager (조회만) → FileQueryPort 사용</li>
 *   <li>Command: FileCommandManager (상태 변경만) → FileCommandPort 사용</li>
 *   <li>분리 이유: 조회와 변경의 책임 분리, 성능 최적화</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>모든 메서드: readOnly=true (조회 최적화)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class FileQueryManager {

    private final FileQueryPort fileQueryPort;

    /**
     * 생성자
     *
     * @param fileQueryPort File Query Port (CQRS Query)
     */
    public FileQueryManager(FileQueryPort fileQueryPort) {
        this.fileQueryPort = fileQueryPort;
    }

    /**
     * ID로 FileAsset 조회
     *
     * @param id FileAsset ID
     * @return FileAsset (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<FileAsset> findById(Long id) {
        return fileQueryPort.findById(id);
    }

    /**
     * Upload Session ID로 FileAsset 조회 (Long 타입)
     *
     * @param uploadSessionId Upload Session ID (Long 타입)
     * @return FileAsset (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<FileAsset> findByUploadSessionId(Long uploadSessionId) {
        return fileQueryPort.findByUploadSessionId(uploadSessionId);
    }

    /**
     * Upload Session ID로 FileAsset 조회 (Value Object 지원)
     *
     * <p>UploadSessionId Value Object를 직접 받아 조회합니다.</p>
     *
     * @param uploadSessionId UploadSession의 ID (UploadSessionId Value Object)
     * @return FileAsset (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<FileAsset> findByUploadSessionId(UploadSessionId uploadSessionId) {
        return fileQueryPort.findByUploadSessionId(uploadSessionId.value());
    }
}
