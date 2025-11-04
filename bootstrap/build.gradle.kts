// ========================================
// Bootstrap: Architecture Tests Module
// ========================================
// This module contains cross-cutting architecture tests
// that validate the entire project structure
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // Core Modules (for ArchUnit testing)
    // ========================================
    testImplementation(project(":domain"))
    testImplementation(project(":application"))
    testImplementation(project(":adapter-in:rest-api"))
    testImplementation(project(":adapter-out:persistence-mysql"))

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.archunit.junit5)

    // ========================================
    // JPA (for JpaEntityConventionTest)
    // ========================================
    testImplementation(libs.spring.boot.starter.data.jpa)
    // Jakarta Persistence API (명시적 추가 - IDE 인식용)
    testImplementation(libs.jakarta.persistence.api)

    // ========================================
    // TestFixtures
    // ========================================
    testImplementation(testFixtures(project(":domain")))
    testImplementation(testFixtures(project(":application")))
    testImplementation(testFixtures(project(":adapter-in:rest-api")))
    testImplementation(testFixtures(project(":adapter-out:persistence-mysql")))
}

