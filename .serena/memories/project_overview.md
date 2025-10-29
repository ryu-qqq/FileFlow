# FileFlow Project Overview

## Project Identity
- **Name**: FileFlow
- **Purpose**: Tenant-based file upload and intelligent post-processing pipeline platform
- **Group**: com.ryuqq.fileflow
- **Version**: 1.0.0-SNAPSHOT

## What This Project Does
FileFlow is an enterprise platform that manages file upload, processing, and post-processing based on tenant-specific policies. It handles various file types (images, HTML, Excel, PDF) with intelligent processing capabilities.

### Core Features
- **Tenant Policy Management**: Configure file upload policies and constraints per tenant
- **Intelligent File Processing**: Image optimization, OCR, data standardization
- **Scalable Pipeline**: SQS-based asynchronous post-processing pipeline
- **Complete Traceability**: Full history tracking from upload to processing

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Cloud Services**: AWS (S3, SQS, Textract, CloudFront)
- **Database**: MySQL (production), H2 (testing)
- **Caching**: Redis
- **Build Tool**: Gradle 8.x with Kotlin DSL
- **NO Lombok**: Pure Java policy enforced

## Architecture Pattern
**Hexagonal Architecture** with strict dependency rules:
```
Domain ← Application ← Adapter
```
- **Domain**: Pure business logic, framework-independent
- **Application**: Use case orchestration, transaction boundaries
- **Adapter**: External system integration (REST, DB, AWS, etc.)

## Module Structure
```
fileflow/
├── domain/                          # Pure business logic
├── application/                     # Use cases (port definitions)
├── adapter/
│   ├── adapter-in-rest-api         # REST API endpoints
│   ├── adapter-out-persistence-jpa # Database persistence
│   ├── adapter-out-redis           # Caching layer
│   ├── adapter-out-aws-s3          # File storage
│   ├── adapter-out-aws-sqs         # Messaging
│   └── adapter-out-aws-textract    # OCR processing
└── bootstrap/
    └── bootstrap-web-api           # Spring Boot application

