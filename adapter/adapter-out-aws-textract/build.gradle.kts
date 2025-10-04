plugins {
    `java-library`
    jacoco
}

dependencies {
    api(project(":application"))
    api(project(":domain"))

    implementation(platform("software.amazon.awssdk:bom:${property("awsSdkVersion")}"))
    implementation("software.amazon.awssdk:textract")

    testImplementation(libs.spring.boot.starter.test)
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
