// ========================================
// Adapter-Out Redis (Outbound Adapter)
// ========================================
// Purpose: Redis 캐시 어댑터
// - Cache Repositories
// - Session Management
// - Rate Limiting
//
// Dependencies:
// - application (Cache Port 구현)
// - domain
// - Spring Data Redis
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Spring Data Redis
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ========================================
    // Lettuce (Redis Client)
    // ========================================
    implementation(rootProject.libs.lettuce.core)

    // ========================================
    // JSON Processing
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(rootProject.libs.testcontainers.junit)
    // Redis TestContainers는 필요시 추가
}
