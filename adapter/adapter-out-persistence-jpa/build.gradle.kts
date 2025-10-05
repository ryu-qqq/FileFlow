// ========================================
// Adapter-Out: Persistence (JPA + QueryDSL)
// ========================================
// Outbound adapter for database operations
// Implements repository ports from application layer
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

    // Spring Data JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // QueryDSL
    implementation(libs.querydsl.jpa) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor(libs.querydsl.apt) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor(libs.jakarta.persistence.api)

    // Database Drivers (Runtime)
    runtimeOnly(libs.mysql.connector)
    runtimeOnly(libs.h2) // For testing

    // Connection Pooling
    implementation(libs.hikaricp)

    // Flyway Migration
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.mysql)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.junit)
}

// ========================================
// QueryDSL Configuration
// ========================================
val generatedSourcesDir = file("build/generated/sources/annotationProcessor/java/main")

sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
        }
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(generatedSourcesDir)
}

// ========================================
// Test Coverage (70% for adapters)
// ========================================
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/entity/**",
                    "**/Q*.class"
                )
            }
        })
    )

    // 마이그레이션 테스트는 인프라 검증이므로 커버리지에서 제외
    executionData.setFrom(
        files(executionData.files.filter {
            !it.path.contains("FlywayMigrationTest")
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/entity/**",
                    "**/Q*.class"
                )
            }
        })
    )

    // 마이그레이션 테스트는 인프라 검증이므로 커버리지에서 제외
    executionData.setFrom(
        files(executionData.files.filter {
            !it.path.contains("FlywayMigrationTest")
        })
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            limit {
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

// ========================================
// Clean Generated Sources
// ========================================
tasks.clean {
    delete(generatedSourcesDir)
}
