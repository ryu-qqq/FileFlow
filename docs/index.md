---
layout: default
title: Spring Standards Documentation
---

# Spring Standards - Coding Conventions

> **Spring Boot 3.5.x + Java 21 헥사고날 아키텍처 엔터프라이즈 표준**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-blue.svg)](https://alistair.cockburn.us/hexagonal-architecture/)
[![Claude Skills](https://img.shields.io/badge/Claude%20Skills-14-purple.svg)](https://github.com/ryu-qqq/claude-spring-standards)

---

## 프로젝트 소개

**14개 전문 Claude Skills**와 **146개 코딩 컨벤션 문서**가 일관된 고품질 코드 생성을 보장하는 엔터프라이즈 표준 프로젝트입니다.

### 핵심 철학

| 원칙 | 설명 |
|------|------|
| **Documentation-Driven** | 146개 코딩 컨벤션 문서가 설계를 강제 |
| **Smart Strategy** | 기존 코드 수정 → TDD, 신규 코드 생성 → Doc-Driven |
| **Zero-Tolerance** | Lombok 금지, Law of Demeter, Long FK 전략 |
| **AI-First** | Claude Code + Serena MCP + 14개 전문 Skills |

---

## 문서 통계

| 레이어 | 파일 수 | 주요 내용 |
|--------|---------|-----------|
| 00-project-setup | 5 | 멀티모듈, Gradle, GitHub Workflows, Terraform |
| 01-adapter-in-layer | 27 | REST API, Controller, DTO, Error, OpenAPI, Security |
| 02-domain-layer | 14 | Aggregate, VO, Event, Exception |
| 03-application-layer | 51 | Assembler, DTO, Event, Facade, Factory, Manager, Port, Service, Scheduler |
| 04-persistence-layer | 41 | MySQL (Adapter, Entity, Mapper, Repository), Redis (Cache, Lock) |
| 05-testing | 3 | Integration Testing, Test Fixtures |
| 06-observability | 4 | Logging, ADOT, CloudWatch |
| 07-local-development | 1 | 로컬 개발 환경 |
| **총계** | **146** | README 제외 |

---

## 레이어별 가이드

### 🏗️ [00. Project Setup](coding_convention/00-project-setup/)

프로젝트 구조 및 인프라 설정

| 문서 | 설명 |
|------|------|
| [멀티모듈 구조](coding_convention/00-project-setup/multi-module-structure.md) | 헥사고날 멀티모듈 구조 및 의존성 규칙 |
| [Gradle 설정](coding_convention/00-project-setup/gradle-configuration.md) | Gradle 빌드 설정 가이드 |
| [GitHub Workflows](coding_convention/00-project-setup/github-workflows-guide.md) | CI/CD 워크플로우 설정 |
| [버전 관리](coding_convention/00-project-setup/version-management.md) | gradle.properties 버전 관리 |
| [Terraform](coding_convention/00-project-setup/terraform-guide.md) | AWS 인프라 Wrapper Module 패턴 |

### 🌐 [01. Adapter-In Layer (REST API)](coding_convention/01-adapter-in-layer/rest-api/)

HTTP 요청/응답 처리 → UseCase 위임

**핵심 원칙**: Thin Controller, Pure Java, Bean Validation 필수, RESTful 설계

| 컴포넌트 | 가이드 | 테스트 | ArchUnit |
|----------|--------|--------|----------|
| **REST API 요약** | [가이드](coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md) | - | - |
| **Controller** | [가이드](coding_convention/01-adapter-in-layer/rest-api/controller/controller-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/controller/controller-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/controller/controller-archunit.md) |
| **DTO - Command** | [가이드](coding_convention/01-adapter-in-layer/rest-api/dto/command/command-dto-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/dto/command/command-dto-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/dto/command/command-dto-archunit.md) |
| **DTO - Query** | [가이드](coding_convention/01-adapter-in-layer/rest-api/dto/query/query-dto-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/dto/query/query-dto-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/dto/query/query-dto-archunit.md) |
| **DTO - Response** | [가이드](coding_convention/01-adapter-in-layer/rest-api/dto/response/response-dto-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/dto/response/response-dto-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/dto/response/response-dto-archunit.md) |
| **Error Handling** | [가이드](coding_convention/01-adapter-in-layer/rest-api/error/error-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/error/error-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/error/error-archunit.md) |
| **Mapper** | [가이드](coding_convention/01-adapter-in-layer/rest-api/mapper/mapper-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/mapper/mapper-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/mapper/mapper-archunit.md) |
| **OpenAPI** | [가이드](coding_convention/01-adapter-in-layer/rest-api/openapi/openapi-guide.md) | - | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/openapi/openapi-archunit.md) |
| **Security** | [가이드](coding_convention/01-adapter-in-layer/rest-api/security/security-guide.md) | [테스트](coding_convention/01-adapter-in-layer/rest-api/security/security-test-guide.md) | [ArchUnit](coding_convention/01-adapter-in-layer/rest-api/security/security-archunit.md) |

### 🎯 [02. Domain Layer](coding_convention/02-domain-layer/)

순수 비즈니스 로직 (기술 독립적)

**핵심 원칙**: Pure Java (Lombok 절대 금지), Law of Demeter 엄수, Aggregate 중심 설계, 불변성 우선

| 컴포넌트 | 가이드 | 테스트 | ArchUnit |
|----------|--------|--------|----------|
| **Domain 요약** | [가이드](coding_convention/02-domain-layer/domain-guide.md) | - | - |
| **Aggregate** | [가이드](coding_convention/02-domain-layer/aggregate/aggregate-guide.md) | [테스트](coding_convention/02-domain-layer/aggregate/aggregate-test-guide.md) | [ArchUnit](coding_convention/02-domain-layer/aggregate/aggregate-archunit.md) |
| **Value Object** | [가이드](coding_convention/02-domain-layer/vo/vo-guide.md) | [테스트](coding_convention/02-domain-layer/vo/vo-test-guide.md) | [ArchUnit](coding_convention/02-domain-layer/vo/vo-archunit.md) |
| **Query VO** | [가이드](coding_convention/02-domain-layer/vo/query-vo-guide.md) | - | - |
| **LockKey** | - | - | [ArchUnit](coding_convention/02-domain-layer/vo/lockkey-archunit.md) |
| **Domain Event** | [가이드](coding_convention/02-domain-layer/event/event-guide.md) | - | [ArchUnit](coding_convention/02-domain-layer/event/event-archunit.md) |
| **Exception** | [가이드](coding_convention/02-domain-layer/exception/exception-guide.md) | [테스트](coding_convention/02-domain-layer/exception/exception-test-guide.md) | [ArchUnit](coding_convention/02-domain-layer/exception/exception-archunit-guide.md) |

### 🔧 [03. Application Layer](coding_convention/03-application-layer/)

UseCase + Transaction 관리

**핵심 원칙**: UseCase = 단일 비즈니스 트랜잭션, Transaction 경계 엄격, CQRS 분리 고정, Port/Adapter 패턴

| 컴포넌트 | 가이드 | 테스트 | ArchUnit |
|----------|--------|--------|----------|
| **Application 요약** | [가이드](coding_convention/03-application-layer/application-guide.md) | - | - |
| **Assembler** | [가이드](coding_convention/03-application-layer/assembler/assembler-guide.md) | [테스트](coding_convention/03-application-layer/assembler/assembler-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/assembler/assembler-archunit.md) |
| **Command Facade** | [가이드](coding_convention/03-application-layer/facade/command/facade-guide.md) | [테스트](coding_convention/03-application-layer/facade/facade-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/facade/facade-archunit.md) |
| **Query Facade** | [가이드](coding_convention/03-application-layer/facade/query/query-facade-guide.md) | - | - |
| **Command Factory** | [가이드](coding_convention/03-application-layer/factory/command/command-factory-guide.md) | [테스트](coding_convention/03-application-layer/factory/command/command-factory-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/factory/command/command-factory-archunit.md) |
| **Query Factory** | [가이드](coding_convention/03-application-layer/factory/query/query-factory-guide.md) | [테스트](coding_convention/03-application-layer/factory/query/query-factory-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/factory/query/query-factory-archunit.md) |
| **Transaction Manager** | [가이드](coding_convention/03-application-layer/manager/transaction-manager-guide.md) | [테스트](coding_convention/03-application-layer/manager/transaction-manager-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/manager/transaction-manager-archunit.md) |
| **Read Manager** | [가이드](coding_convention/03-application-layer/manager/query/read-manager-guide.md) | - | - |
| **Command Service** | [가이드](coding_convention/03-application-layer/service/command/command-service-guide.md) | [테스트](coding_convention/03-application-layer/service/command/command-service-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/service/command/command-service-archunit.md) |
| **Query Service** | [가이드](coding_convention/03-application-layer/service/query/query-service-guide.md) | [테스트](coding_convention/03-application-layer/service/query/query-service-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/service/query/query-service-archunit.md) |
| **Event Listener** | [가이드](coding_convention/03-application-layer/listener/event-listener-guide.md) | [테스트](coding_convention/03-application-layer/listener/event-listener-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/listener/event-listener-archunit.md) |
| **Scheduler** | [가이드](coding_convention/03-application-layer/scheduler/scheduler-guide.md) | [테스트](coding_convention/03-application-layer/scheduler/scheduler-test-guide.md) | [ArchUnit](coding_convention/03-application-layer/scheduler/scheduler-archunit.md) |

### 💾 [04. Persistence Layer](coding_convention/04-persistence-layer/)

저장소 (Database) 연동

**핵심 원칙**: 어댑터 = 비즈니스 로직 금지, CQRS 분리 (Command=JPA, Query=QueryDSL), 엔티티 연관관계 금지 (Long FK)

#### MySQL (JPA/QueryDSL)

| 컴포넌트 | 가이드 | 테스트 | ArchUnit |
|----------|--------|--------|----------|
| **Command Adapter** | [가이드](coding_convention/04-persistence-layer/mysql/adapter/command/command-adapter-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/adapter/command/command-adapter-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/adapter/command/command-adapter-archunit.md) |
| **Query Adapter** | [가이드](coding_convention/04-persistence-layer/mysql/adapter/query/general/query-adapter-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/adapter/query/general/query-adapter-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/adapter/query/general/query-adapter-archunit.md) |
| **Lock Query Adapter** | [가이드](coding_convention/04-persistence-layer/mysql/adapter/query/lock/lock-query-adapter-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/adapter/query/lock/lock-query-adapter-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/adapter/query/lock/lock-query-adapter-archunit.md) |
| **Entity** | [가이드](coding_convention/04-persistence-layer/mysql/entity/entity-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/entity/entity-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/entity/entity-archunit.md) |
| **Mapper** | [가이드](coding_convention/04-persistence-layer/mysql/mapper/mapper-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/mapper/mapper-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/mapper/mapper-archunit.md) |
| **JPA Repository** | [가이드](coding_convention/04-persistence-layer/mysql/repository/jpa/jpa-repository-guide.md) | - | [ArchUnit](coding_convention/04-persistence-layer/mysql/repository/jpa/jpa-repository-archunit.md) |
| **QueryDSL Repository** | [가이드](coding_convention/04-persistence-layer/mysql/repository/querydsl/querydsl-repository-guide.md) | [테스트](coding_convention/04-persistence-layer/mysql/repository/querydsl/querydsl-repository-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/mysql/repository/querydsl/querydsl-repository-archunit.md) |

#### Redis (Cache & Lock)

| 컴포넌트 | 가이드 | 테스트 | ArchUnit |
|----------|--------|--------|----------|
| **Redis 요약** | [가이드](coding_convention/04-persistence-layer/redis/persistence-redis-guide.md) | - | - |
| **Cache Adapter** | [가이드](coding_convention/04-persistence-layer/redis/adapter/cache-adapter-guide.md) | [테스트](coding_convention/04-persistence-layer/redis/adapter/cache-adapter-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/redis/adapter/cache-adapter-archunit.md) |
| **Distributed Lock** | [가이드](coding_convention/04-persistence-layer/redis/lock/distributed-lock-guide.md) | - | - |
| **Lock Adapter** | [가이드](coding_convention/04-persistence-layer/redis/lock/lock-adapter-guide.md) | [테스트](coding_convention/04-persistence-layer/redis/lock/lock-adapter-test-guide.md) | [ArchUnit](coding_convention/04-persistence-layer/redis/lock/lock-adapter-archunit.md) |

### 🧪 [05. Testing](coding_convention/05-testing/)

테스트 전략 및 Test Fixtures

| 컴포넌트 | 가이드 | ArchUnit |
|----------|--------|----------|
| **통합 테스트** | [가이드](coding_convention/05-testing/integration-testing/01_integration-testing-overview.md) | - |
| **Test Fixtures** | [가이드](coding_convention/05-testing/test-fixtures/01_test-fixtures-guide.md) | [ArchUnit](coding_convention/05-testing/test-fixtures/02_test-fixtures-archunit.md) |

### 📊 [06. Observability](coding_convention/06-observability/)

모니터링, 로깅, 추적

| 컴포넌트 | 가이드 |
|----------|--------|
| **Observability 요약** | [가이드](coding_convention/06-observability/observability-guide.md) |
| **Logging 설정** | [가이드](coding_convention/06-observability/logging-configuration.md) |
| **ADOT 연동** | [가이드](coding_convention/06-observability/adot-integration.md) |
| **CloudWatch 연동** | [가이드](coding_convention/06-observability/cloudwatch-integration.md) |

### 🖥️ [07. Local Development](coding_convention/07-local-development/)

로컬 개발 환경 설정

| 컴포넌트 | 가이드 |
|----------|--------|
| **로컬 개발 환경** | [가이드](coding_convention/07-local-development/local-dev-guide.md) |

---

## Zero-Tolerance 규칙

절대 위반 불가 규칙:

| 번호 | 규칙 | 적용 레이어 | 이유 |
|------|------|-------------|------|
| 1 | **Lombok 전면 금지** | 전체 | 명시적 코드, 디버깅 용이성 |
| 2 | **Law of Demeter** | Domain | 캡슐화, 결합도 감소 |
| 3 | **Long FK 전략** | Persistence | N+1 회피, 성능 최적화 |
| 4 | **Transaction 경계** | Application | 외부 API 호출 격리 |
| 5 | **Spring Proxy 제약** | Application | @Transactional 정상 작동 보장 |
| 6 | **CQRS 분리** | Application | Command/Query 완전 분리 |

---

## Claude Skills (14개)

프로젝트에 특화된 14개 전문 Skills:

| 카테고리 | Skill | 역할 |
|----------|-------|------|
| **Planning** | `requirements-analyst` | 추상적 요구사항 → 구체적 비즈니스 규칙 |
| **Planning** | `layer-architect` | 영향도 분석, TDD vs Doc-Driven 결정 |
| **Domain** | `domain-expert` | Aggregate, VO, Event, Exception 설계 |
| **Application** | `usecase-expert` | Port-In 인터페이스, UseCase/Service 구현 |
| **Application** | `transaction-expert` | TransactionManager, ReadManager, Facade |
| **Application** | `factory-assembler-expert` | CommandFactory, QueryAssembler, Bundle |
| **Persistence** | `entity-mapper-expert` | JPA Entity, EntityMapper (Long FK) |
| **Persistence** | `repository-expert` | JpaRepository, QueryDslRepository |
| **Persistence** | `adapter-expert` | CommandAdapter, QueryAdapter, LockAdapter |
| **Persistence** | `redis-expert` | Lettuce 캐시 + Redisson 분산락 |
| **REST API** | `controller-expert` | REST Controller, Command/Query DTO |
| **Cross-Cutting** | `testing-expert` | Integration Test, TestRestTemplate |
| **Cross-Cutting** | `project-setup-expert` | Multi-module 구조, Gradle |
| **Cross-Cutting** | `devops-expert` | GitHub Actions, Terraform, Docker |

---

## 링크

- **GitHub Repository**: [ryu-qqq/claude-spring-standards](https://github.com/ryu-qqq/claude-spring-standards)
- **Main README**: [README.md](https://github.com/ryu-qqq/claude-spring-standards/blob/main/README.md)
- **전체 코딩 컨벤션**: [coding_convention/README.md](coding_convention/README.md)

---

**Last Updated**: 2025-12-05
**Version**: 3.0
