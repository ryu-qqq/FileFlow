package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.RetryFailedFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.RetryFailedFileAssetResponse;

/**
 * 실패한 FileAsset 재처리 UseCase.
 *
 * <p>CQRS Command Side - FAILED 상태인 파일 자산을 재처리 대기 상태로 변경
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>FAILED 상태 파일 → PENDING 상태 변경
 *   <li>FileProcessingOutbox 재처리 메시지 생성
 *   <li>테넌트/조직 스코프 검증
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>일시적 오류로 실패한 파일 재처리
 *   <li>관리자 수동 재처리 트리거
 * </ul>
 */
public interface RetryFailedFileAssetUseCase {

    /**
     * 실패한 FileAsset 재처리 요청.
     *
     * @param command 재처리 명령
     * @return 재처리 결과 응답
     */
    RetryFailedFileAssetResponse execute(RetryFailedFileAssetCommand command);
}
