// ========================================
// Adapter-Out ABAC CEL (Outbound Adapter)
// ========================================
// Purpose: CEL 기반 ABAC 조건 평가 엔진 어댑터 (Driven Adapter)
// - Google CEL (Common Expression Language) 라이브러리 래핑
// - ABAC 조건식 평가 (Attribute-Based Access Control)
// - 보수적 거부 (Deny by Default) 정책
// - 성능 목표: P95 < 10ms
//
// Dependencies:
// - application (AbacEvaluatorPort 구현)
// - Google CEL Java Library
//
// Policy:
// - 평가 실패 시 항상 false 반환 (보수적 거부)
// - 변수 바인딩: ctx.* (컨텍스트), res.* (리소스)
// - 성능 최적화: CEL Env 캐싱, 사전 컴파일
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
    // Google CEL (Common Expression Language)
    // ========================================
    implementation("dev.cel:cel:0.6.0")

    // ========================================
    // Spring Framework
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")

    // ========================================
    // Logging
    // ========================================
    implementation("org.slf4j:slf4j-api")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

// ========================================
// Test Configuration
// ========================================
tasks.test {
    useJUnitPlatform()

    // 성능 테스트 활성화
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
}
