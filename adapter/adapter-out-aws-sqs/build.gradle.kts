// ========================================
// Adapter-Out: AWS SQS
// ========================================
// Outbound adapter for AWS SQS messaging
// Implements messaging ports from application layer
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
    implementation(libs.aws.sqs)

    // Spring Context & Messaging
    implementation(libs.spring.context)
    implementation(libs.spring.messaging)

    // JSON Processing
    implementation(libs.jackson.databind)

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
