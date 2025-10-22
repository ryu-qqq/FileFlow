// ========================================
// Adapter-Out Parent Module
// ========================================
// Purpose: Outbound Adapters 그룹 (Driven Adapters)
//
// Submodules:
// - persistence-mysql: MySQL 영속성 어댑터
//
// Policy:
// - Adapter 모듈들의 공통 설정 관리
// - 각 Adapter는 독립적인 책임을 가짐
// ========================================

plugins {
    java
}

// 공통 설정이 필요한 경우 여기에 추가
