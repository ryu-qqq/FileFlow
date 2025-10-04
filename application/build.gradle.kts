// ========================================
// Application Module
// ========================================
// Use cases and application services
// Orchestrates domain logic
// NO direct adapter dependencies
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
    // Domain layer (required)
    api(project(":domain"))

    // Spring Context (Dependency Injection only)
    implementation(libs.spring.context)
    implementation(libs.spring.tx)

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":domain"))
}

// ========================================
// Application-Specific Test Coverage
// ========================================
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% coverage required
            }
        }

        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
            excludes = listOf(
                "*.config.*",
                "*.dto.*"
            )
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

// ========================================
// Architecture Validation
// ========================================
tasks.register("verifyApplicationBoundaries") {
    group = "verification"
    description = "Verify application module respects architectural boundaries"

    doLast {
        // Check no adapter dependencies
        val forbiddenDependencies = listOf(
            "adapter-in",
            "adapter-out"
        )

        project.configurations.runtimeClasspath.get().dependencies.forEach { dep ->
            forbiddenDependencies.forEach { forbidden ->
                if (dep.name.contains(forbidden)) {
                    throw GradleException(
                        """
                        ❌ APPLICATION BOUNDARY VIOLATION DETECTED

                        Application layer cannot depend on adapters:
                        - Dependency: ${dep.name}

                        Application should only depend on:
                        - domain module
                        - Spring Context (DI)

                        See: application/build.gradle.kts
                        """.trimIndent()
                    )
                }
            }
        }
        println("✅ Application boundary verification passed")
    }
}

tasks.build {
    dependsOn("verifyApplicationBoundaries")
}
