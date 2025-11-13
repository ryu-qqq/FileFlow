// ========================================
// Bootstrap: Download Scheduler Application
// ========================================
// Runnable Spring Boot application for External Download Outbox Scheduler
// Separate ECS Task for background processing
// NO REST API, NO Web Server
// NO Lombok allowed
// ========================================

import java.time.Instant

plugins {
    java
    alias(libs.plugins.spring.boot)
}

dependencies {
    // ========================================
    // Core Modules
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Adapters (필요한 것만)
    // ========================================
    // Outbound
    implementation(project(":adapter-out:persistence-mysql"))
    implementation(project(":adapter-out:persistence-redis"))
    implementation(project(":adapter-out:abac-cel"))
    implementation(project(":adapter-out:aws-s3"))
    implementation(project(":adapter-out:http-client"))
    implementation(project(":adapter-out:image-processor"))
    implementation(project(":adapter-out:metadata-extractor"))

    // ========================================
    // Spring Boot Starters
    // ========================================
    // NO Web (Scheduler만 실행)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Configuration Processing
    annotationProcessor(libs.spring.boot.configuration.processor)

    // ========================================
    // Jackson (for ObjectMapper)
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

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
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit)
}

// ========================================
// Spring Boot Configuration
// ========================================
tasks.bootJar {
    archiveFileName.set("${project.rootProject.name}-scheduler-download.jar")

    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to "fileflow-scheduler-download",
                "Implementation-Version" to project.version,
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version"),
                "Build-Timestamp" to Instant.now().toString()
            )
        )
    }
}

// ========================================
// Application Run Configuration
// ========================================
tasks.bootRun {
    jvmArgs = listOf(
        "-Xms256m",
        "-Xmx512m",
        "-XX:+UseG1GC"
    )
}
