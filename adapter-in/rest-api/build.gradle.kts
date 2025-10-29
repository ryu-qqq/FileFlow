// ========================================
// Adapter-In REST API (Inbound Adapter)
// ========================================
// Purpose: REST API 진입점 (Driving Adapter)
// - REST Controllers
// - Request/Response DTOs
// - Exception Handlers
// - API Documentation (OpenAPI/Swagger)
//
// Dependencies:
// - application (Use Case 호출)
// - domain (Domain 모델 참조)
// - Spring Web
//
// Policy:
// - Controller는 thin layer (비즈니스 로직 없음)
// - DTO ↔ Domain 변환은 Mapper로 위임
// - Exception Handling은 @ControllerAdvice
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
    implementation(project(":application"))

    // ========================================
    // Spring Web
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ========================================
    // API Documentation
    // ========================================
    implementation(rootProject.libs.springdoc.openapi.starter.webmvc.ui)

    // ========================================
    // JSON Processing
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}
