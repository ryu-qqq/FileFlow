package com.ryuqq.fileflow.application.policy.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.out.CachePolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import org.springframework.stereotype.Service;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;

import java.util.Objects;
import java.util.Optional;

/**
 * 업로드 정책 검증 서비스
 *
 * Epic 1에서 구현된 정책 검증 로직을 파일 업로드 흐름에 통합합니다.
 * Hexagonal Architecture의 Application Service로서, Use Case를 구현합니다.
 *
 * 검증 흐름:
 * 1. 테넌트 식별 (tenantId, userType, serviceType)
 * 2. 정책 조회 (캐시 우선, 없으면 DB 조회)
 * 3. 파일 크기/타입 검증 (UploadPolicy.validateFile)
 * 4. Rate Limiting 검증 (선택적, UploadPolicy.validateRateLimit)
 *
 * @author sangwon-ryu
 */
@Service
public class UploadPolicyValidationService implements ValidateUploadPolicyUseCase {

    private final LoadUploadPolicyPort loadUploadPolicyPort;
    private final CachePolicyPort cachePolicyPort;

    public UploadPolicyValidationService(
            LoadUploadPolicyPort loadUploadPolicyPort,
            CachePolicyPort cachePolicyPort
    ) {
        this.loadUploadPolicyPort = Objects.requireNonNull(loadUploadPolicyPort, "loadUploadPolicyPort must not be null");
        this.cachePolicyPort = Objects.requireNonNull(cachePolicyPort, "cachePolicyPort must not be null");
    }

    /**
     * 업로드 정책을 검증합니다 (Use Case 구현).
     *
     * @param command 검증 요청 커맨드
     * @throws PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 위반 시
     */
    @Override
    public void validate(ValidateUploadPolicyCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        PolicyKey policyKey = PolicyKey.of(
                command.tenantId(),
                command.userType(),
                command.serviceType()
        );

        // 파일 업로드 정책 검증
        validateUploadPolicy(
                policyKey,
                command.fileType(),
                command.fileFormat(),
                command.fileSizeBytes(),
                command.fileCount()
        );

        // Rate Limiting 검증 (선택적)
        if (command.shouldValidateRateLimit()) {
            validateRateLimit(
                    policyKey,
                    command.currentRequestCount(),
                    command.currentUploadCount()
            );
        }
    }

    /**
     * 파일 업로드 정책을 검증합니다.
     *
     * @param policyKey 정책 식별자
     * @param fileType 파일 타입
     * @param fileFormat 파일 포맷 (Optional)
     * @param fileSizeBytes 파일 크기 (bytes)
     * @param fileCount 파일 개수
     * @throws PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 위반 시
     */
    private void validateUploadPolicy(
            PolicyKey policyKey,
            FileType fileType,
            String fileFormat,
            long fileSizeBytes,
            int fileCount
    ) {
        Objects.requireNonNull(policyKey, "policyKey must not be null");
        Objects.requireNonNull(fileType, "fileType must not be null");

        UploadPolicy policy = loadPolicyWithCache(policyKey);
        policy.validateFile(fileType, fileFormat, fileSizeBytes, fileCount);
    }

    /**
     * Rate Limiting 정책을 검증합니다.
     *
     * @param policyKey 정책 식별자
     * @param currentRequestCount 현재 시간당 요청 횟수
     * @param currentUploadCount 현재 일일 업로드 횟수
     * @throws PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException Rate Limit 초과 시
     */
    private void validateRateLimit(
            PolicyKey policyKey,
            int currentRequestCount,
            int currentUploadCount
    ) {
        Objects.requireNonNull(policyKey, "policyKey must not be null");

        UploadPolicy policy = loadPolicyWithCache(policyKey);
        policy.validateRateLimit(currentRequestCount, currentUploadCount);
    }

    /**
     * 정책을 조회합니다 (캐시 우선, 없으면 DB 조회 후 캐싱).
     *
     * 조회 전략:
     * 1. 캐시에서 조회 시도
     * 2. 캐시에 없으면 DB에서 활성화된 정책 조회
     * 3. DB에서 조회한 정책을 캐시에 저장
     * 4. 정책이 없으면 PolicyNotFoundException 발생
     *
     * @param policyKey 정책 식별자
     * @return 조회된 UploadPolicy
     * @throws PolicyNotFoundException 정책을 찾을 수 없는 경우
     */
    private UploadPolicy loadPolicyWithCache(PolicyKey policyKey) {
        // 1. 캐시에서 조회 시도
        Optional<UploadPolicy> cachedPolicy = cachePolicyPort.get(policyKey);
        if (cachedPolicy.isPresent()) {
            return cachedPolicy.get();
        }

        // 2. DB에서 활성화된 정책 조회
        UploadPolicy policy = loadUploadPolicyPort.loadActiveByKey(policyKey)
                .orElseThrow(() -> new PolicyNotFoundException(policyKey.getValue()));

        // 3. 캐시에 저장
        cachePolicyPort.put(policy);

        return policy;
    }
}
