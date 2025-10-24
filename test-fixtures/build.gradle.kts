plugins {
    id("java-library")
}

group = "com.ryuqq.fileflow"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Domain 모듈 의존
    api(project(":domain"))

    // Application 모듈 의존 (DTO 참조를 위해 필요)
    api(project(":application"))

    // Persistence 모듈 의존 (JpaEntity Fixtures를 위해 필요)
    api(project(":adapter-out:persistence-mysql"))

    // Test 유틸리티
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
