// ========================================
// Adapter-In REST API (Inbound Adapter)
// ========================================
// Purpose: REST API 진입점 (Driving Adapter)
// - REST Controllers
// - Request/Response DTOs
// - Exception Handlers
// - API Documentation (OpenAPI/Swagger)
//
// Dependencies:
// - application (Use Case 호출)
// - domain (Domain 모델 참조)
// - Spring Web
//
// Policy:
// - Controller는 thin layer (비즈니스 로직 없음)
// - DTO ↔ Domain 변환은 Mapper로 위임
// - Exception Handling은 @ControllerAdvice
// ========================================

plugins {
    java
    `java-test-fixtures`  // TestFixtures 플러그인
    id("org.springframework.boot")
    id("org.asciidoctor.jvm.convert") version "3.3.2"  // AsciiDoc 플러그인
}

dependencies {
    // ========================================
    // Core Dependencies
    // ========================================
    implementation(project(":domain"))
    implementation(project(":application"))

    // ========================================
    // Spring Web
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ========================================
    // API Documentation
    // ========================================
    implementation(rootProject.libs.springdoc.openapi.starter.webmvc.ui)

    // ========================================
    // JSON Processing
    // ========================================
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // ========================================
    // Test Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")  // For JdbcTemplate in Integration Tests
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")  // For JPA in Integration Tests
    testImplementation("com.mysql:mysql-connector-j")  // MySQL Driver for Testcontainers
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // Bootstrap Module for Integration Tests (FileflowApplication)
    testImplementation(project(":bootstrap:bootstrap-web-api"))

    // Adapter-Out Modules for Integration Tests
    testImplementation(project(":adapter-out:persistence-mysql"))
    testImplementation(project(":adapter-out:persistence-redis"))
    testImplementation(project(":adapter-out:aws-s3"))
    testImplementation(project(":adapter-out:abac-cel"))
    testImplementation(project(":adapter-out:http-client"))

    // ========================================
    // Testcontainers (Integration Test)
    // ========================================
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:localstack")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // Redis Testcontainer for Cache Tests
    testImplementation("com.redis:testcontainers-redis:2.2.2")

    // ========================================
    // AWS SDK (for Integration Tests with LocalStack)
    // ========================================
    testImplementation(platform(rootProject.libs.aws.sdk.bom.get().toString()))
    testImplementation(rootProject.libs.aws.s3)
}

// ========================================
// Spring REST Docs Configuration
// ========================================
val snippetsDir = file("build/generated-snippets")

tasks.test {
    outputs.dir(snippetsDir)
    ignoreFailures = true  // 테스트 실패해도 snippets 생성
}

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)

    attributes(
        mapOf(
            "snippets" to snippetsDir,
            "source-highlighter" to "highlightjs",
            "toc" to "left",
            "toclevels" to "3"
        )
    )

    // 생성된 HTML을 static 리소스로 복사
    doLast {
        copy {
            from("${outputDir}/html5")
            into("build/resources/main/static/docs")
        }
    }
}

// bootJar에 문서 포함
tasks.bootJar {
    dependsOn(tasks.asciidoctor)
    from("${tasks.asciidoctor.get().outputDir}/html5") {
        into("BOOT-INF/classes/static/docs")
    }
}
