package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformRequestJpaRepository
        extends JpaRepository<TransformRequestJpaEntity, String> {}
