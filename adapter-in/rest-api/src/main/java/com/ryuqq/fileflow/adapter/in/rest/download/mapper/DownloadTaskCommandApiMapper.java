package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.CreateDownloadTaskApiRequest;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import org.springframework.stereotype.Component;

/**
 * DownloadTaskCommandApiMapper - 다운로드 작업 Command Mapper.
 *
 * <p>API Request → Application Command 변환을 담당합니다.
 *
 * <p>API-MAP-001: Mapper는 @Component로 등록.
 *
 * <p>API-MAP-004: Command Mapper는 toCommand() 메서드 제공.
 *
 * <p>API-MAP-003: 순수 변환 로직만.
 */
@Component
public class DownloadTaskCommandApiMapper {

    /**
     * CreateDownloadTaskApiRequest → CreateDownloadTaskCommand 변환.
     *
     * @param request API 요청
     * @return CreateDownloadTaskCommand
     */
    public CreateDownloadTaskCommand toCommand(CreateDownloadTaskApiRequest request) {
        return new CreateDownloadTaskCommand(
                request.sourceUrl(),
                request.s3Key(),
                request.bucket(),
                request.accessType(),
                request.purpose(),
                request.source(),
                request.callbackUrl());
    }
}
