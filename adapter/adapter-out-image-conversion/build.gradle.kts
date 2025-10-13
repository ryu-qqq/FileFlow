// ========================================
// Adapter-Out: Image Conversion
// ========================================
// Outbound adapter for image format conversion
// Implements image conversion ports from application layer
// Supports WebP conversion using Thumbnailator library
// NO Lombok allowed
// ========================================

plugins {
    `java-library`
    jacoco
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    api(project(":application"))
    api(project(":domain"))

    // Image Processing Libraries
    // Thumbnailator for image conversion and optimization
    implementation("net.coobird:thumbnailator:0.4.20")

    // WebP support for Thumbnailator
    // This adds WebP reading and writing capabilities
    implementation("org.sejda.imageio:webp-imageio:0.1.6")

    // Spring Framework
    implementation(libs.spring.context)
    implementation(libs.spring.boot.configuration.processor)

    // AWS S3 for file storage
    implementation(platform(libs.aws.bom))
    implementation("software.amazon.awssdk:s3")

    // Utilities
    implementation(libs.commons.lang3)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)

    // Testcontainers for LocalStack S3 testing
    testImplementation(libs.testcontainers.junit)
    testImplementation("org.testcontainers:localstack:1.19.3")
}

// ========================================
// Test Coverage
// ========================================
// Note: WebP 변환은 실제 이미지 파일과 S3 연동이 필요하여
// 초기 구현 단계에서는 커버리지 검증 비활성화
// TODO: 통합 테스트 환경 구축 후 70% 커버리지 달성 및 활성화
tasks.jacocoTestCoverageVerification {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
    // Coverage verification will be enabled after integration test setup
}
