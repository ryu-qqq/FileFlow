package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.command.CheckRateLimitCommand;
import com.ryuqq.fileflow.application.upload.dto.response.RateLimitResponse;
import com.ryuqq.fileflow.application.upload.port.in.CheckUploadRateLimitUseCase;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Check Upload Rate Limit Service
 *
 * <p>Tenant별 동시 업로드 세션 제한을 확인하는 UseCase 구현체입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Tenant의 현재 활성 업로드 세션 개수 조회</li>
 *   <li>Rate Limit 초과 여부 검증</li>
 *   <li>Rate Limit 정보 반환 (현재/최대/여유)</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>업로드 시작 전 Rate Limit 사전 확인</li>
 *   <li>다수의 업로드 요청 Batch 처리 전 검증</li>
 *   <li>Tenant별 리소스 사용량 모니터링</li>
 * </ul>
 *
 * <p><strong>성능:</strong></p>
 * <ul>
 *   <li>DB Index 활용: idx_tenant_status (tenant_id, status)</li>
 *   <li>COUNT 쿼리만 수행 (테이블 접근 불필요)</li>
 *   <li>트랜잭션: readOnly = true (조회 최적화)</li>
 * </ul>
 *
 * <p><strong>설정:</strong></p>
 * <ul>
 *   <li>{@code upload.rate-limit.max-concurrent-per-tenant}: Tenant당 최대 동시 업로드 수</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class CheckUploadRateLimitService implements CheckUploadRateLimitUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckUploadRateLimitService.class);

    private final LoadUploadSessionPort loadUploadSessionPort;
    private final int maxConcurrentPerTenant;

    /**
     * 생성자
     *
     * @param loadUploadSessionPort Load UploadSession Port (Query)
     * @param maxConcurrentPerTenant Tenant당 최대 동시 업로드 수 (설정)
     */
    public CheckUploadRateLimitService(
        LoadUploadSessionPort loadUploadSessionPort,
        @Value("${upload.rate-limit.max-concurrent-per-tenant:10}") int maxConcurrentPerTenant
    ) {
        if (maxConcurrentPerTenant <= 0) {
            throw new IllegalArgumentException(
                "maxConcurrentPerTenant는 0보다 커야 합니다: " + maxConcurrentPerTenant
            );
        }
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.maxConcurrentPerTenant = maxConcurrentPerTenant;
    }

    /**
     * Rate Limit 확인
     *
     * <p><strong>실행 순서:</strong></p>
     * <ol>
     *   <li>Tenant의 IN_PROGRESS 상태 세션 개수 조회 (DB Index 활용)</li>
     *   <li>Rate Limit 초과 여부 판단</li>
     *   <li>응답 생성 (현재/최대/여유/허용 여부)</li>
     * </ol>
     *
     * @param command Check Rate Limit Command
     * @return Rate Limit 정보
     */
    @Override
    @Transactional(readOnly = true)
    public RateLimitResponse execute(CheckRateLimitCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CheckRateLimitCommand는 null일 수 없습니다.");
        }
        Long tenantId = command.tenantId();

        // 1. Tenant의 진행 중인 업로드 세션 개수 조회 (IN_PROGRESS 상태)
        // ⭐ Index 활용: idx_tenant_status (tenant_id, status)
        long currentCount = loadUploadSessionPort.countByTenantIdAndStatus(
            tenantId,
            SessionStatus.IN_PROGRESS
        );

        // 2. Rate Limit 초과 여부 판단
        boolean allowed = currentCount < maxConcurrentPerTenant;
        long remaining = Math.max(0, maxConcurrentPerTenant - currentCount);

        log.debug("Rate limit check for tenantId={}: current={}, max={}, remaining={}, allowed={}",
            tenantId, currentCount, maxConcurrentPerTenant, remaining, allowed);

        // 3. 응답 생성
        return RateLimitResponse.of(
            tenantId,
            currentCount,
            maxConcurrentPerTenant,
            remaining,
            allowed
        );
    }
}
