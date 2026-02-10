package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetMetadataApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import org.springframework.stereotype.Component;

/**
 * AssetQueryApiMapper - Asset Query API 변환 매퍼.
 *
 * <p>Application Response → API Response 변환을 담당합니다.
 *
 * <p>API-MAP-001: Mapper는 @Component로 등록.
 *
 * <p>API-MAP-003: 순수 변환 로직만.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수.
 */
@Component
public class AssetQueryApiMapper {

    /**
     * AssetResponse → AssetApiResponse 변환.
     *
     * @param response Application 응답
     * @return AssetApiResponse
     */
    public AssetApiResponse toResponse(AssetResponse response) {
        return new AssetApiResponse(
                response.assetId(),
                response.s3Key(),
                response.bucket(),
                response.accessType().name(),
                response.fileName(),
                response.fileSize(),
                response.contentType(),
                response.etag(),
                response.extension(),
                response.origin().name(),
                response.originId(),
                response.purpose(),
                response.source(),
                DateTimeFormatUtils.formatIso8601(response.createdAt()));
    }

    /**
     * AssetMetadataResponse → AssetMetadataApiResponse 변환.
     *
     * @param response Application 응답
     * @return AssetMetadataApiResponse
     */
    public AssetMetadataApiResponse toResponse(AssetMetadataResponse response) {
        return new AssetMetadataApiResponse(
                response.metadataId(),
                response.assetId(),
                response.width(),
                response.height(),
                response.transformType(),
                DateTimeFormatUtils.formatIso8601(response.createdAt()));
    }
}
