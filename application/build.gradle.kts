// ========================================
// Application Module (Hexagonal Architecture - Use Cases)
// ========================================
// Purpose: 비즈니스 유스케이스 및 애플리케이션 서비스
// - Use Cases (Command/Query)
// - Application Services
// - Inbound Ports (Interfaces)
// - Outbound Ports (Interfaces)
// - DTOs, Mappers, Assemblers
//
// Dependencies:
// - domain (의존)
// - Spring Context (for @Transactional, @Service)
//
// Policy:
// - @Transactional은 Application Layer에서만 사용
// - 외부 API 호출은 @Transactional 밖에서
// - Lombok 금지 (Domain은 특히 엄격, Application은 DTO에서만 고려)
// ========================================

plugins {
    java
    `java-test-fixtures`  // TestFixtures 플러그인
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))

    // ========================================
    // Spring Context (for @Transactional, @Service)
    // ========================================
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")

    // ========================================
    // Validation
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(project(":domain"))
    testImplementation(testFixtures(project(":domain")))
}
