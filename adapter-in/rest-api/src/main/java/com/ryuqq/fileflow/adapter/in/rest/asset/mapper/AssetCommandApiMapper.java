package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import org.springframework.stereotype.Component;

/**
 * AssetCommandApiMapper - Asset Command Mapper.
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
public class AssetCommandApiMapper {

    /**
     * Path Variable + Query Parameter → DeleteAssetCommand 변환.
     *
     * @param assetId Asset ID (PathVariable)
     * @param source 요청 서비스명 (RequestParam)
     * @return DeleteAssetCommand
     */
    public DeleteAssetCommand toDeleteCommand(String assetId, String source) {
        return new DeleteAssetCommand(assetId, source);
    }
}
