plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":application"))
    api(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
