package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;

/**
 * FileAsset 삭제 UseCase.
 *
 * <p>CQRS Command Side - 파일 자산 Soft Delete
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>파일 자산 Soft Delete 처리
 *   <li>테넌트/조직 스코프 검증
 *   <li>S3 객체는 삭제하지 않음 (메타데이터만 삭제 처리)
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>잘못 업로드된 파일 삭제
 *   <li>관리자 파일 정리
 * </ul>
 */
public interface DeleteFileAssetUseCase {

    /**
     * FileAsset Soft Delete.
     *
     * @param command 삭제 명령
     * @return 삭제 결과 응답
     */
    DeleteFileAssetResponse execute(DeleteFileAssetCommand command);
}
