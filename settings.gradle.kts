rootProject.name = "fileflow"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

// ========================================
// Core Modules (Hexagonal Architecture)
// ========================================
include("domain")
include("application")

// ========================================
// Adapter Modules (Ports & Adapters)
// ========================================
// Inbound Adapters (Driving)
include("adapter:adapter-in-rest-api")

// Outbound Adapters (Driven)
include("adapter:adapter-out-persistence-jpa")
include("adapter:adapter-out-redis")
include("adapter:adapter-out-aws-s3")
include("adapter:adapter-out-aws-sqs")
include("adapter:adapter-out-aws-textract")

// ========================================
// Bootstrap Modules (Runnable Applications)
// ========================================
include("bootstrap:bootstrap-web-api")
