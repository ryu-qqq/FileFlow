package com.ryuqq.fileflow.adapter.out.persistence.mysql.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration (Persistence Layer)
 *
 * <p><strong>역할</strong>: JPA Entity 스캔 및 Repository 활성화</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/config/</p>
 *
 * <h3>설정 내용</h3>
 * <ul>
 *   <li>✅ Entity 스캔 경로 지정: {@code com.ryuqq.fileflow.adapter.out.persistence.mysql}</li>
 *   <li>✅ JPA Repository 활성화</li>
 *   <li>✅ Transaction Management 활성화</li>
 * </ul>
 *
 * <p><strong>주의사항</strong>:
 * <ul>
 *   <li>Entity 패키지는 {@code adapter.out.persistence.mysql.*.entity} 구조</li>
 *   <li>Repository 패키지는 {@code adapter.out.persistence.mysql.*.repository} 구조</li>
 *   <li>{@code @Transactional}은 Application Layer에서 사용 (UseCase)</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ryuqq.fileflow.adapter.out.persistence.mysql")
@EntityScan(basePackages = "com.ryuqq.fileflow.adapter.out.persistence.mysql")
@EnableTransactionManagement
public class JpaConfig {
    // JPA 설정은 application.yml에서 관리
    // 필요 시 EntityManagerFactory, DataSource 커스터마이징 가능
}
