package com.ryuqq.fileflow.application.asset.port.in.command;

import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;

/**
 * Presigned Download URL 생성 UseCase.
 *
 * <p>CQRS Command Side - S3 Presigned Download URL 생성
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>파일 자산 존재 및 접근 권한 검증
 *   <li>S3 Presigned Download URL 생성
 *   <li>URL 만료 시간 설정
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>파일 다운로드 링크 생성
 *   <li>임시 다운로드 URL 제공
 *   <li>보안 파일 전송
 * </ul>
 */
public interface GenerateDownloadUrlUseCase {

    /**
     * Presigned Download URL 생성.
     *
     * @param command URL 생성 명령
     * @return 생성된 URL 정보
     */
    DownloadUrlResponse execute(GenerateDownloadUrlCommand command);
}
