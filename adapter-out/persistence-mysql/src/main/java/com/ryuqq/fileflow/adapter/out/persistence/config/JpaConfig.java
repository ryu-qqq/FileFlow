package com.ryuqq.fileflow.adapter.out.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.ryuqq.fileflow.adapter.out.persistence")
@EntityScan(basePackages = "com.ryuqq.fileflow.adapter.out.persistence")
public class JpaConfig {}
