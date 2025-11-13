package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.GeneratePartUrlCommand;
import com.ryuqq.fileflow.application.upload.dto.response.PartPresignedUrlResponse;

/**
 * 파트 업로드 URL 생성 UseCase
 *
 * <p>클라이언트가 직접 S3에 파트를 업로드할 수 있도록 Presigned URL을 제공합니다.</p>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>업로드 세션 조회 (트랜잭션 내)</li>
 *   <li>Multipart 정보 검증</li>
 *   <li>Presigned URL 생성 (트랜잭션 밖)</li>
 * </ol>
 *
 * <p><strong>Transaction 경계 관리:</strong></p>
 * <ul>
 *   <li>✅ 세션 조회는 Read-only 트랜잭션</li>
 *   <li>✅ S3 API 호출은 트랜잭션 밖</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface GeneratePartPresignedUrlUseCase {

    /**
     * Presigned URL 생성
     *
     * @param command URL 생성 Command
     * @return Presigned URL Response
     * @throws IllegalArgumentException 세션을 찾을 수 없거나 파트 번호가 유효하지 않은 경우
     * @throws IllegalStateException Multipart 업로드가 진행 중이 아닌 경우
     */
    PartPresignedUrlResponse execute(GeneratePartUrlCommand command);
}
