package com.ryuqq.fileflow.application.asset.manager.query;

import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset ReadManager.
 *
 * <p>FileAsset 조회를 담당하는 ReadManager입니다. 단일 QueryPort만 의존하며, 읽기 전용 트랜잭션으로 동작합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileAssetReadManager {

    private final FileAssetQueryPort fileAssetQueryPort;

    public FileAssetReadManager(FileAssetQueryPort fileAssetQueryPort) {
        this.fileAssetQueryPort = fileAssetQueryPort;
    }

    /**
     * ID로 FileAsset 조회.
     *
     * @param id 파일 자산 ID
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return FileAsset (없으면 empty)
     */
    @Transactional(readOnly = true)
    public Optional<FileAsset> findById(FileAssetId id, String organizationId, String tenantId) {
        return fileAssetQueryPort.findById(id, organizationId, tenantId);
    }

    /**
     * ID만으로 FileAsset 조회 (DLQ 처리용).
     *
     * @param id 파일 자산 ID
     * @return FileAsset (없으면 empty)
     */
    @Transactional(readOnly = true)
    public Optional<FileAsset> findById(FileAssetId id) {
        return fileAssetQueryPort.findById(id);
    }

    /**
     * 검색 조건에 맞는 FileAsset 목록 조회.
     *
     * @param criteria 검색 조건
     * @return FileAsset 목록
     */
    @Transactional(readOnly = true)
    public List<FileAsset> findByCriteria(FileAssetCriteria criteria) {
        return fileAssetQueryPort.findByCriteria(criteria);
    }

    /**
     * 검색 조건에 맞는 FileAsset 개수 조회.
     *
     * @param criteria 검색 조건
     * @return 전체 개수
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FileAssetCriteria criteria) {
        return fileAssetQueryPort.countByCriteria(criteria);
    }

    /**
     * 상태별 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return 상태별 개수
     */
    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus(String organizationId, String tenantId) {
        return fileAssetQueryPort.countByStatus(organizationId, tenantId);
    }

    /**
     * 카테고리별 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return 카테고리별 개수
     */
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategory(String organizationId, String tenantId) {
        return fileAssetQueryPort.countByCategory(organizationId, tenantId);
    }

    /**
     * 전체 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return 전체 개수
     */
    @Transactional(readOnly = true)
    public long countTotal(String organizationId, String tenantId) {
        return fileAssetQueryPort.countTotal(organizationId, tenantId);
    }
}
