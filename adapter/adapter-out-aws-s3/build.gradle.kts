// ========================================
// Adapter-Out: AWS S3
// ========================================
// Outbound adapter for AWS S3 file storage
// Implements file storage ports from application layer
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

    // AWS SDK v2
    implementation(platform(libs.aws.bom))
    implementation(libs.aws.s3)
    implementation(libs.aws.s3.transfer)
    implementation(libs.aws.apache.client)

    // Spring Framework
    implementation(libs.spring.context)
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Resilience & Retry
    implementation(libs.bundles.resilience)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(libs.testcontainers.junit)
}

// ========================================
// Test Coverage (70% for adapters)
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
