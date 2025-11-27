package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;

/**
 * Presigned Download URL 일괄 생성 UseCase.
 *
 * <p>CQRS Command Side - 여러 파일에 대한 S3 Presigned Download URL 일괄 생성
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>파일 자산 목록 검증 (최대 100개 제한)
 *   <li>각 파일에 대한 접근 권한 검증
 *   <li>S3 Presigned Download URL 일괄 생성
 *   <li>부분 실패 처리 (성공/실패 분리 응답)
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>갤러리 뷰에서 다중 파일 다운로드
 *   <li>일괄 내보내기 기능
 *   <li>대량 파일 전송
 * </ul>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>최대 100개까지 일괄 처리 가능
 *   <li>일부 실패 시에도 성공한 URL은 반환
 *   <li>실패 건에 대해서는 에러 정보 제공
 * </ul>
 */
public interface BatchGenerateDownloadUrlUseCase {

    /**
     * Presigned Download URL 일괄 생성.
     *
     * @param command 일괄 URL 생성 명령
     * @return 생성된 URL 목록 및 실패 정보
     */
    BatchDownloadUrlResponse execute(BatchGenerateDownloadUrlCommand command);
}
