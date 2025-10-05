package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadPolicyEntity;
import com.ryuqq.fileflow.adapter.persistence.mapper.UploadPolicyMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.UploadPolicyJpaRepository;
import com.ryuqq.fileflow.application.policy.port.out.DeleteUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.LoadUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.SaveUploadPolicyPort;
import com.ryuqq.fileflow.application.policy.port.out.UpdateUploadPolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UploadPolicy Persistence Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * 4개의 Port 인터페이스를 구현하여 데이터베이스 영속성을 제공합니다.
 *
 * 구현 Port:
 * - LoadUploadPolicyPort: 정책 조회
 * - SaveUploadPolicyPort: 정책 저장
 * - UpdateUploadPolicyPort: 정책 업데이트
 * - DeleteUploadPolicyPort: 정책 삭제
 *
 * 트랜잭션 관리:
 * - 트랜잭션은 Application Layer의 UseCase에서 관리됩니다
 * - Adapter는 순수한 데이터 접근 계층으로 동작합니다
 *
 * @author sangwon-ryu
 */
@Component
public class UploadPolicyPersistenceAdapter implements
        LoadUploadPolicyPort,
        SaveUploadPolicyPort,
        UpdateUploadPolicyPort,
        DeleteUploadPolicyPort {

    private final UploadPolicyJpaRepository repository;
    private final UploadPolicyMapper mapper;

    public UploadPolicyPersistenceAdapter(
            UploadPolicyJpaRepository repository,
            UploadPolicyMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ========== LoadUploadPolicyPort Implementation ==========

    @Override
    public Optional<UploadPolicy> loadByKey(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String policyKeyString = policyKey.getValue();

        return repository.findById(policyKeyString)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UploadPolicy> loadActiveByKey(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String policyKeyString = policyKey.getValue();

        return repository.findByPolicyKeyAndIsActiveTrue(policyKeyString)
                .map(mapper::toDomain);
    }

    // ========== SaveUploadPolicyPort Implementation ==========

    @Override
    public UploadPolicy save(UploadPolicy uploadPolicy) {
        if (uploadPolicy == null) {
            throw new IllegalArgumentException("UploadPolicy cannot be null");
        }

        String policyKeyString = uploadPolicy.getPolicyKey().getValue();

        if (repository.existsByPolicyKey(policyKeyString)) {
            throw new IllegalStateException(
                    "UploadPolicy with PolicyKey already exists: " + policyKeyString
            );
        }

        UploadPolicyEntity entity = mapper.toEntity(uploadPolicy);
        UploadPolicyEntity savedEntity = repository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    // ========== UpdateUploadPolicyPort Implementation ==========

    @Override
    public UploadPolicy update(UploadPolicy uploadPolicy) {
        if (uploadPolicy == null) {
            throw new IllegalArgumentException("UploadPolicy cannot be null");
        }

        String policyKeyString = uploadPolicy.getPolicyKey().getValue();

        UploadPolicyEntity entity = repository.findById(policyKeyString)
                .orElseThrow(() -> new IllegalStateException(
                        "UploadPolicy with PolicyKey does not exist: " + policyKeyString
                ));

        // 기존 엔티티의 필드를 업데이트 (JPA가 자동으로 version 증가)
        entity.update(
                uploadPolicy.getFileTypePolicies(),
                uploadPolicy.getRateLimiting(),
                uploadPolicy.isActive(),
                uploadPolicy.getEffectiveFrom(),
                uploadPolicy.getEffectiveUntil()
        );

        // save()와 flush()를 명시적으로 호출하여 @Version 증가 보장
        UploadPolicyEntity updatedEntity = repository.saveAndFlush(entity);

        return mapper.toDomain(updatedEntity);
    }

    // ========== DeleteUploadPolicyPort Implementation ==========

    @Override
    public void delete(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String policyKeyString = policyKey.getValue();

        repository.deleteById(policyKeyString);
    }
}
