package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.BatchDeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDeleteFileAssetResponse;

/**
 * FileAsset 일괄 삭제 UseCase.
 *
 * <p>CQRS Command Side - 여러 파일 자산을 일괄 Soft Delete
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>여러 파일 자산 일괄 Soft Delete 처리
 *   <li>테넌트/조직 스코프 검증
 *   <li>개별 실패 시에도 나머지 처리 계속 (Partial Success 지원)
 *   <li>S3 객체는 삭제하지 않음 (메타데이터만 삭제 처리)
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>관리자 파일 일괄 정리
 *   <li>기간 만료 파일 일괄 삭제
 * </ul>
 */
public interface BatchDeleteFileAssetUseCase {

    /**
     * FileAsset 일괄 Soft Delete.
     *
     * @param command 일괄 삭제 명령
     * @return 일괄 삭제 결과 응답
     */
    BatchDeleteFileAssetResponse execute(BatchDeleteFileAssetCommand command);
}
