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
include("adapter-in:rest-api")
include("adapter-in:scheduler")

// Outbound Adapters (Driven)
// New Hexagonal Architecture Adapters
include("adapter-out:persistence-mysql")
include("adapter-out:persistence-redis")
include("adapter-out:abac-cel")
include("adapter-out:aws-s3")
include("adapter-out:http-client")
include("adapter-out:image-processor")
include("adapter-out:metadata-extractor")
// ========================================
// Bootstrap Modules (Runnable Applications)
// ========================================
include("bootstrap")
include("bootstrap:bootstrap-web-api")
include("bootstrap:bootstrap-scheduler-download")
include("bootstrap:bootstrap-scheduler-pipeline")
include("bootstrap:bootstrap-scheduler-upload")

