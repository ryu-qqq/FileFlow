// ========================================
// Adapter-Out AWS S3 (Outbound Adapter)
// ========================================
// Purpose: AWS S3 Storage 어댑터 (Driven Adapter)
// - S3 Multipart Upload API
// - Presigned URL 생성
// - S3 Object 관리
//
// Dependencies:
// - application (S3StoragePort 구현)
// - domain (Domain Value Objects)
// - AWS SDK for Java v2
//
// Policy:
// - AWS SDK v2 사용 (비동기 지원)
// - S3 Exception을 Domain Exception으로 변환
// - Infrastructure 레이어 (Domain과 분리)
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
    // Spring Boot
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter")

    // ========================================
    // AWS SDK for Java v2
    // ========================================
    implementation(platform("software.amazon.awssdk:bom:2.20.26"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:auth")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(testFixtures(project(":domain")))
}
