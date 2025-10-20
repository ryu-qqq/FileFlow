// ========================================
// Bootstrap: Web API Application
// ========================================
// Runnable Spring Boot application
// Wires all adapters and beans together
// Main application entry point
// NO Lombok allowed
// ========================================

import java.time.Instant

plugins {
    java
    alias(libs.plugins.spring.boot)
    jacoco
}

dependencies {
    // ========================================
    // Core Modules
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Adapters
    // ========================================
    // Inbound
    implementation(project(":adapter:adapter-in-rest-api"))

    // Outbound
    implementation(project(":adapter:adapter-out-persistence-jpa"))
    implementation(project(":adapter:adapter-out-redis"))
    implementation(project(":adapter:adapter-out-aws-s3"))
    implementation(project(":adapter:adapter-out-aws-sqs"))
    implementation(project(":adapter:adapter-out-aws-textract"))
    implementation(project(":adapter:adapter-out-metadata-extraction"))
    implementation(project(":adapter:adapter-out-image-conversion"))

    // ========================================
    // Spring Boot Starters
    // ========================================
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)

    // Configuration Processing
    annotationProcessor(libs.spring.boot.configuration.processor)

    // ========================================
    // Observability
    // ========================================
    // Micrometer for metrics
    implementation(libs.micrometer.prometheus)

    // Logging
    implementation(libs.logstash.logback.encoder)

    // ========================================
    // Database
    // ========================================
    runtimeOnly(libs.mysql.connector)

    // ========================================
    // AWS SDK (from adapters)
    // ========================================
    implementation(platform(libs.aws.bom))

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.rest.assured)
}

// ========================================
// Spring Boot Configuration
// ========================================
tasks.bootJar {
    archiveFileName.set("${project.rootProject.name}-web-api.jar")

    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.rootProject.name,
                "Implementation-Version" to project.version,
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version"),
                "Build-Timestamp" to Instant.now().toString()
            )
        )
    }
}

// ========================================
// Integration Test Coverage
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            excludes = listOf(
                "*.config.*",
                "*Application"
            )
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

// ========================================
// Application Run Configuration
// ========================================
tasks.bootRun {
    jvmArgs = listOf(
        "-Xms512m",
        "-Xmx1024m",
        "-XX:+UseG1GC"
    )
}
