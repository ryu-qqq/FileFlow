// ========================================
// Adapter-In: Admin Web (REST API)
// ========================================
// Inbound adapter for admin REST API
// Handles HTTP requests and responses
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
    // Application layer (use cases)
    api(project(":application"))
    api(project(":domain"))

    // Spring Web
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)

    // Spring Security (Optional)
    implementation(libs.spring.boot.starter.security)

    // JSON Processing
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)

    // API Documentation (Optional)
    implementation(libs.springdoc.openapi)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.rest.assured)
    testImplementation(libs.spring.boot.starter.data.jpa)

    // Testcontainers for integration tests
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(libs.testcontainers.mysql)

    // AWS SDK for S3 integration tests
    testImplementation(platform(libs.aws.bom))
    testImplementation(libs.aws.s3)
    testImplementation(libs.aws.sqs)

    // Adapter dependencies for integration tests
    testImplementation(project(":adapter:adapter-out-persistence-jpa"))
    testImplementation(project(":adapter:adapter-out-aws-s3"))
    testImplementation(project(":adapter:adapter-out-redis"))
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
