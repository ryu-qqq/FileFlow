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
    // Domain 모듈만 의존 (Persistence 의존 X)
    api(project(":domain"))

    // Test 유틸리티
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
