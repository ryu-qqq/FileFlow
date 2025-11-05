package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;

/**
 * Multipart 업로드 초기화 UseCase
 *
 * <p>실행 흐름:</p>
 * <ol>
 *   <li>S3 Multipart 초기화 (트랜잭션 밖) ⭐ 중요</li>
 *   <li>Domain 객체 생성 및 저장 (트랜잭션 내)</li>
 *   <li>실패 시 S3 리소스 정리</li>
 * </ol>
 *
 * <p><strong>Transaction 경계 관리:</strong></p>
 * <ul>
 *   <li>✅ S3 API 호출은 트랜잭션 밖에서 실행</li>
 *   <li>✅ Domain 저장은 트랜잭션 내에서 실행</li>
 *   <li>✅ 실패 시 S3 리소스 정리 로직 포함</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface InitMultipartUploadUseCase {

    /**
     * Multipart 업로드 초기화 실행
     *
     * @param command 초기화 Command
     * @return 초기화 Response
     * @throws IllegalArgumentException 유효하지 않은 입력값
     * @throws RuntimeException S3 초기화 실패 또는 저장 실패
     */
    InitMultipartResponse execute(InitMultipartCommand command);
}
