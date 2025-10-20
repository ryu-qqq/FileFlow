// ========================================
// Adapter-Out Persistence JPA (Outbound Adapter)
// ========================================
// Purpose: 데이터베이스 영속성 어댑터 (Driven Adapter)
// - JPA Entities (NOT Domain Entities!)
// - JPA Repositories
// - QueryDSL for complex queries
// - Flyway Migrations
//
// Dependencies:
// - application (Repository Port 구현)
// - domain (Domain → Entity 변환)
// - Spring Data JPA
// - QueryDSL
// - MySQL Driver
//
// Policy:
// - Long FK 전략 (JPA 관계 어노테이션 금지)
// - @ManyToOne, @OneToMany, @OneToOne, @ManyToMany 절대 금지
// - Entity는 Infrastructure 레이어 (Domain과 분리)
// - QueryDSL로 복잡한 쿼리 작성
// ========================================

plugins {
    java
    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Spring Data JPA
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // ========================================
    // Database
    // ========================================
    runtimeOnly(rootProject.libs.mysql.connector)
    testImplementation(rootProject.libs.h2)

    // ========================================
    // QueryDSL
    // ========================================
    implementation(rootProject.libs.querydsl.jpa) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor(rootProject.libs.querydsl.apt) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // ========================================
    // Flyway Migration
    // ========================================
    implementation(rootProject.libs.flyway.core)
    implementation(rootProject.libs.flyway.mysql)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(rootProject.libs.testcontainers.junit)
    testImplementation(rootProject.libs.testcontainers.mysql)
}

// ========================================
// QueryDSL Configuration
// ========================================
val querydslDir = "src/main/generated"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main {
        java {
            srcDirs(querydslDir)
        }
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(file(querydslDir))
}

tasks.named("clean") {
    doLast {
        file(querydslDir).deleteRecursively()
    }
}
