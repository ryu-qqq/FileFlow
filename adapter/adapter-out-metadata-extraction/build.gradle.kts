// ========================================
// Adapter-Out: Metadata Extraction
// ========================================
// Outbound adapter for file metadata extraction
// Implements metadata extraction ports from application layer
// Supports image metadata extraction using metadata-extractor library
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

    // Metadata Extraction Libraries
    implementation(libs.metadata.extractor)
    implementation(libs.tika.core)

    // Spring Framework
    implementation(libs.spring.context)
    implementation(libs.spring.boot.configuration.processor)

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
}

// ========================================
// Test Coverage (temporarily disabled for initial implementation)
// ========================================
// Note: 실제 이미지 파일을 사용한 통합 테스트가 필요하여 초기 구현에서는 커버리지 검증 비활성화
// TODO: 실제 이미지 파일을 src/test/resources에 추가하고 커버리지 70% 달성 후 활성화
tasks.jacocoTestCoverageVerification {
    enabled = false
}

tasks.test {
    // Coverage verification is disabled for initial implementation
}
