// ========================================
// Adapter-Out AWS SQS (Outbound Adapter)
// ========================================
// Purpose: SQS 메시지 큐 어댑터
// - Event Publishing
// - Async Processing
// - Dead Letter Queue
//
// Dependencies:
// - application (Messaging Port 구현)
// - domain (Domain Events)
// - AWS SDK SQS
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
    // AWS SDK SQS
    // ========================================
    implementation(platform(rootProject.libs.aws.sdk.bom))
    implementation("software.amazon.awssdk:sqs")

    // ========================================
    // Spring Cloud AWS (Optional)
    // ========================================
    // Spring Cloud AWS SQS integration if needed

    // ========================================
    // JSON Processing
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
