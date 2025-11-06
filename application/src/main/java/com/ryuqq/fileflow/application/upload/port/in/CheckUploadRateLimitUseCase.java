package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.CheckRateLimitCommand;
import com.ryuqq.fileflow.application.upload.dto.response.RateLimitResponse;

/**
 * Check Upload Rate Limit UseCase
 *
 * <p>Tenant별 동시 업로드 세션 제한을 확인하는 UseCase입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Tenant의 현재 활성 업로드 세션 개수 확인</li>
 *   <li>Rate Limit 초과 여부 검증</li>
 *   <li>Rate Limit 정보 제공</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>업로드 시작 전 사전 검증 (HTTP 429 Too Many Requests 방지)</li>
 *   <li>다수의 업로드 요청 Batch 처리 전 제한 확인</li>
 *   <li>Tenant별 리소스 모니터링 및 통계</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface CheckUploadRateLimitUseCase {

    /**
     * Rate Limit 확인
     *
     * @param command Check Rate Limit Command
     * @return Rate Limit 정보 (현재/최대/여유/허용 여부)
     */
    RateLimitResponse execute(CheckRateLimitCommand command);
}
