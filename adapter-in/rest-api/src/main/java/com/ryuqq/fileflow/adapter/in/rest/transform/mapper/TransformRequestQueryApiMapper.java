package com.ryuqq.fileflow.adapter.in.rest.transform.mapper;

import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import org.springframework.stereotype.Component;

/**
 * TransformRequestQueryApiMapper - 이미지 변환 요청 Query API 변환 매퍼.
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
public class TransformRequestQueryApiMapper {

    /**
     * TransformRequestResponse → TransformRequestApiResponse 변환.
     *
     * @param response Application 응답
     * @return TransformRequestApiResponse
     */
    public TransformRequestApiResponse toResponse(TransformRequestResponse response) {
        return new TransformRequestApiResponse(
                response.transformRequestId(),
                response.sourceAssetId(),
                response.sourceContentType(),
                response.transformType(),
                response.width(),
                response.height(),
                response.quality(),
                response.targetFormat(),
                response.status(),
                response.resultAssetId(),
                response.lastError(),
                DateTimeFormatUtils.formatIso8601(response.createdAt()),
                DateTimeFormatUtils.formatIso8601(response.completedAt()));
    }
}
