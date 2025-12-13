package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.UpdateFileAssetStatusCommand;
import com.ryuqq.fileflow.application.asset.dto.response.UpdateFileAssetStatusResponse;

/**
 * FileAsset 상태 변경 UseCase.
 *
 * <p>CQRS Command Side - 파일 자산 상태 전이 처리
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상태 전이 검증 (유효한 전이인지)
 *   <li>상태 변경 및 이력 저장
 *   <li>N8N 워크플로우 연동 상태 관리
 * </ul>
 *
 * <p><strong>상태 전이 규칙:</strong>
 *
 * <ul>
 *   <li>RESIZED → N8N_PROCESSING (N8N 워크플로우 시작)
 *   <li>N8N_PROCESSING → N8N_COMPLETED (N8N 처리 완료)
 *   <li>N8N_COMPLETED → COMPLETED (최종 완료)
 *   <li>* → FAILED (실패 처리)
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>N8N 워크플로우에서 상태 업데이트 요청
 *   <li>외부 시스템 연동 상태 동기화
 * </ul>
 */
public interface UpdateFileAssetStatusUseCase {

    /**
     * FileAsset 상태 변경 실행.
     *
     * @param command 상태 변경 Command
     * @return 상태 변경 결과 응답
     */
    UpdateFileAssetStatusResponse execute(UpdateFileAssetStatusCommand command);
}
