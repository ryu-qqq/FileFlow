package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallbackOutboxJpaRepository
        extends JpaRepository<CallbackOutboxJpaEntity, String> {}
