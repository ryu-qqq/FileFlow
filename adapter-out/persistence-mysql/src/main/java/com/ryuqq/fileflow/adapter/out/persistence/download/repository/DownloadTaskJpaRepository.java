package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadTaskJpaRepository extends JpaRepository<DownloadTaskJpaEntity, String> {}
