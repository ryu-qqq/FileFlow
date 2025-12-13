package com.ryuqq.fileflow.application.asset.factory.query;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import org.springframework.stereotype.Component;

/**
 * FileAsset Query Factory.
 *
 * <p>Query DTO를 Domain Criteria VO로 변환합니다. String → Domain Enum 변환은 이 Factory에서 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Query → Criteria 변환
 *   <li>String → Domain Enum 변환
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>@Component 어노테이션 (Service 아님)
 *   <li>비즈니스 로직 금지 (순수 변환)
 *   <li>Port 호출 금지 (조회 없음)
 *   <li>@Transactional 금지
 * </ul>
 */
@Component
public class FileAssetQueryFactory {

    /**
     * ListFileAssetsQuery를 FileAssetCriteria Domain VO로 변환.
     *
     * @param query ListFileAssetsQuery
     * @return FileAssetCriteria
     */
    public FileAssetCriteria createCriteria(ListFileAssetsQuery query) {
        FileAssetStatus status =
                query.status() != null ? FileAssetStatus.valueOf(query.status()) : null;
        FileCategory category =
                query.category() != null ? FileCategory.valueOf(query.category()) : null;

        return FileAssetCriteria.of(
                query.organizationId(),
                query.tenantId(),
                status,
                category,
                query.offset(),
                query.size());
    }
}
