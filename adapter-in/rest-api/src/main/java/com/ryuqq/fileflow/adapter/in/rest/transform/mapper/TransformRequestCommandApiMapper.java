package com.ryuqq.fileflow.adapter.in.rest.transform.mapper;

import com.ryuqq.fileflow.adapter.in.rest.transform.dto.command.CreateTransformRequestApiRequest;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import org.springframework.stereotype.Component;

/**
 * TransformRequestCommandApiMapper - 이미지 변환 요청 Command Mapper.
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
public class TransformRequestCommandApiMapper {

    /**
     * CreateTransformRequestApiRequest → CreateTransformRequestCommand 변환.
     *
     * @param request API 요청
     * @return CreateTransformRequestCommand
     */
    public CreateTransformRequestCommand toCommand(CreateTransformRequestApiRequest request) {
        return new CreateTransformRequestCommand(
                request.sourceAssetId(),
                request.transformType(),
                request.width(),
                request.height(),
                request.quality(),
                request.targetFormat());
    }
}
