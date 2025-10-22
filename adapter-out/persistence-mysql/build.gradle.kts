// ========================================
// Adapter-Out Persistence MySQL (Outbound Adapter)
// ========================================
// Purpose: MySQL 데이터베이스 영속성 어댑터 (Driven Adapter)
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
    // QueryDSL (Jakarta)
    // ========================================
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // ========================================
    // Flyway Migration
    // ========================================
    implementation(rootProject.libs.flyway.core)
    implementation(rootProject.libs.flyway.mysql)

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation(project(":test-fixtures"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(rootProject.libs.testcontainers.junit)
    testImplementation(rootProject.libs.testcontainers.mysql)
}

// ========================================
// QueryDSL Configuration (Gradle 8.x)
// ========================================
val querydslDir = "src/main/generated"

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
        delete(file(querydslDir))
    }
}
