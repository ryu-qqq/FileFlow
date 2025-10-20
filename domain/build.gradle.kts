// ========================================
// Domain Module (Hexagonal Architecture - Core)
// ========================================
// Purpose: 비즈니스 로직의 핵심 도메인 모델
// - Aggregates, Entities, Value Objects
// - Domain Events
// - Domain Services
// - Repository Ports (Interfaces)
//
// Policy:
// - ZERO external dependencies (외부 의존성 절대 금지)
// - Pure Java only (Lombok 금지)
// - No Spring, No JPA annotations
// - Law of Demeter 엄격 준수
// ========================================

plugins {
    java
}

dependencies {
    // ========================================
    // ZERO External Dependencies
    // ========================================
    // Domain layer는 순수 Java만 사용
    // 프레임워크, 라이브러리 의존성 금지
}
