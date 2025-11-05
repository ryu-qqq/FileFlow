// ========================================
// Adapter-In Scheduler (Event-Driven Adapter)
// ========================================
// Purpose: 스케줄링 및 이벤트 처리 (Driving Adapter)
// - Scheduled Jobs
// - Redis Event Listeners
// - Background Tasks
//
// Dependencies:
// - application (Use Case 호출)
// - Spring Boot
// - Spring Data Redis
//
// Policy:
// - Listener는 thin layer (비즈니스 로직 없음)
// - UseCase 호출로 비즈니스 로직 위임
// - 이벤트 처리는 비동기 가능
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":application"))

    // ========================================
    // Spring Boot
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ========================================
    // Logging
    // ========================================
    implementation("org.slf4j:slf4j-api")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
