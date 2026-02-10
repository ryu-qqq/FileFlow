package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.DownloadTaskApiResponse;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import org.springframework.stereotype.Component;

/**
 * DownloadTaskQueryApiMapper - 다운로드 작업 Query API 변환 매퍼.
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
public class DownloadTaskQueryApiMapper {

    /**
     * DownloadTaskResponse → DownloadTaskApiResponse 변환.
     *
     * @param response Application 응답
     * @return DownloadTaskApiResponse
     */
    public DownloadTaskApiResponse toResponse(DownloadTaskResponse response) {
        return new DownloadTaskApiResponse(
                response.downloadTaskId(),
                response.sourceUrl(),
                response.s3Key(),
                response.bucket(),
                response.accessType().name(),
                response.purpose(),
                response.source(),
                response.status(),
                response.retryCount(),
                response.maxRetries(),
                response.callbackUrl(),
                response.lastError(),
                DateTimeFormatUtils.formatIso8601(response.createdAt()),
                DateTimeFormatUtils.formatIso8601(response.startedAt()),
                DateTimeFormatUtils.formatIso8601(response.completedAt()));
    }
}
