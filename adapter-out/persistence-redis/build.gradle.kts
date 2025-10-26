// ========================================
// Adapter-Out Persistence Redis (Outbound Adapter)
// ========================================
// Purpose: Redis 캐시 영속성 어댑터 (Driven Adapter)
// - Grants Cache (TTL 5분)
// - Settings Cache (TTL 10분)
// - RedisTemplate 기반 캐시 관리
//
// Dependencies:
// - application (Cache Port 구현)
// - domain (Domain 객체 직렬화)
// - Spring Data Redis
// - Jackson (JSON 직렬화)
//
// Policy:
// - Look-Aside 패턴 (Cache-Aside)
// - TTL 기반 자동 만료
// - Cache Fallback (실패 시에도 서비스 정상 동작)
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
    // Jackson (JSON 직렬화)
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // Java 8 Time Support

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(project(":test-fixtures"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(rootProject.libs.testcontainers.junit)

    // TestContainers Redis
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("com.redis:testcontainers-redis:2.2.2")
}
