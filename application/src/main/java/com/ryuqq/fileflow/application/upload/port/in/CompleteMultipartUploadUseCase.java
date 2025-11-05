package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;

/**
 * Multipart 업로드 완료 UseCase
 *
 * <p>모든 파트 업로드 완료 후 S3에서 최종 파일을 조립합니다.</p>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>완료 가능 검증 (트랜잭션 내)</li>
 *   <li>S3 Complete Multipart API 호출 (트랜잭션 밖) ⭐</li>
 *   <li>Domain 상태 업데이트 (트랜잭션 내)</li>
 *   <li>이벤트 발행 (트랜잭션 커밋 시 자동)</li>
 * </ol>
 *
 * <p><strong>Transaction 경계 관리:</strong></p>
 * <ul>
 *   <li>✅ S3 API 호출은 트랜잭션 밖</li>
 *   <li>✅ Domain 업데이트는 트랜잭션 내</li>
 *   <li>✅ 이벤트는 Domain에서 자동 발행</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface CompleteMultipartUploadUseCase {

    /**
     * Multipart 업로드 완료
     *
     * @param command 완료 Command
     * @return 완료 Response
     * @throws IllegalArgumentException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException 완료 불가능한 상태인 경우
     * @throws RuntimeException S3 완료 실패
     */
    CompleteMultipartResponse execute(CompleteMultipartCommand command);
}
