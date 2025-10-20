# 📚 FileFlow Database Migration Guide

## 🗂️ Migration Files Overview

### Core Tables (V1-V2)
- **V1__create_tenant_organization_tables.sql**
  - `tenants`: 멀티테넌시 핵심 테이블
  - `organizations`: 판매자/회사 조직 관리
  - `tenant_settings`: 테넌트별 설정
  - `organization_settings`: 조직별 설정

- **V2__create_user_permission_tables.sql**
  - `user_contexts`: 사용자 컨텍스트 (외부 인증 서버 연동)
  - `roles`: 역할 정의
  - `permissions`: 권한 정의
  - `user_role_mappings`: 사용자-역할 매핑
  - `role_permissions`: 역할-권한 매핑

### File Management (V3-V4)
- **V3__create_file_management_tables.sql**
  - `file_assets`: 파일 자산 정보
  - `file_variants`: 썸네일, 리사이즈 등 변종
  - `file_metadata`: 파일 메타데이터
  - `file_relationships`: 파일 간 관계
  - `file_tags`: 파일 태그
  - `file_shares`: 파일 공유
  - `file_versions`: 버전 관리

- **V4__create_upload_management_tables.sql**
  - `upload_policies`: 업로드 정책
  - `upload_sessions`: 업로드 세션 관리
  - `upload_parts`: 멀티파트 업로드
  - `upload_chunks`: 청크 업로드
  - `external_downloads`: 외부 URL 다운로드
  - `batch_uploads`: 배치 업로드

### Processing Pipeline (V5-V6)
- **V5__create_pipeline_processing_tables.sql**
  - `pipeline_definitions`: 파이프라인 정의
  - `pipeline_stages`: 파이프라인 단계
  - `pipeline_executions`: 실행 추적
  - `pipeline_stage_logs`: 단계별 로그
  - `pipeline_templates`: 템플릿
  - `pipeline_schedules`: 스케줄

- **V6__create_data_extraction_tables.sql**
  - `extracted_data`: OCR/AI 추출 데이터
  - `canonical_formats`: 표준 포맷 정의
  - `data_mappings`: 데이터 매핑
  - `mapping_rules`: 매핑 규칙
  - `ai_training_data`: AI 학습 데이터
  - `extracted_entities`: 추출된 엔티티
  - `ocr_regions`: OCR 영역

### Monitoring & Logging (V7-V9)
- **V7__create_audit_logging_tables.sql**
  - `audit_logs`: 감사 로그 (7년 보관)
  - `access_logs`: 접근 로그 (90일 보관)
  - `processing_errors`: 처리 오류
  - `security_events`: 보안 이벤트
  - `performance_metrics`: 성능 메트릭
  - `api_usage_logs`: API 사용 로그
  - `compliance_logs`: 규정 준수 로그

- **V8__create_supplementary_tables.sql**
  - `file_categories`: 파일 카테고리
  - `file_thumbnails_queue`: 썸네일 생성 큐
  - `virus_scan_results`: 바이러스 스캔 결과
  - `file_events`: Event Sourcing
  - `schema_migrations_history`: 마이그레이션 히스토리
  - `notification_templates`: 알림 템플릿
  - `notification_queue`: 알림 큐
  - `system_configs`: 시스템 설정

- **V9__create_indexes_and_optimizations.sql**
  - 복합 인덱스 생성
  - Full-text 검색 인덱스
  - 파티션 관리 프로시저
  - 스케줄된 이벤트
  - 성능 최적화 뷰

## 🚀 Migration Execution

### Prerequisites
```yaml
MySQL: 8.0+
Character Set: utf8mb4
Collation: utf8mb4_unicode_ci
Storage Engine: InnoDB
```

### Flyway Configuration
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    table: flyway_schema_history
    baseline-version: 0
    baseline-description: Initial
```

### Manual Execution
```sql
-- Run migrations in order
source V1__create_tenant_organization_tables.sql;
source V2__create_user_permission_tables.sql;
source V3__create_file_management_tables.sql;
source V4__create_upload_management_tables.sql;
source V5__create_pipeline_processing_tables.sql;
source V6__create_data_extraction_tables.sql;
source V7__create_audit_logging_tables.sql;
source V8__create_supplementary_tables.sql;
source V9__create_indexes_and_optimizations.sql;
```

## 🔑 Key Design Decisions

### 1. No Foreign Key Constraints
- **이유**: 분산 환경 확장성, 샤딩 준비, Lock contention 감소
- **대안**: 애플리케이션 레벨 참조 무결성 검증

### 2. User Contexts Instead of Users
- **이유**: 외부 인증 서버(JWT) 사용
- **역할 분리**: 
  - Auth Server: 인증(Authentication)
  - FileFlow: 권한(Authorization)

### 3. Hybrid Logging Strategy
```
MySQL (단기) → S3 (장기 아카이브) 
  ↓              ↓
CloudWatch    Athena
(실시간 알림)  (쿼리)
```

### 4. Partitioning Strategy
| Table | Partition Type | Retention |
|-------|---------------|-----------|
| audit_logs | Monthly | 7 years |
| access_logs | Daily | 7 days |
| file_assets | Monthly | Permanent |
| performance_metrics | Daily | 7 days |

## 📊 Data Volume Estimates

### Monthly Projections
- Files: ~1M uploads
- Audit Logs: ~10M records
- Access Logs: ~100M records
- Pipeline Executions: ~5M runs

### Storage Requirements
- MySQL: ~500GB (active data)
- S3 Archive: ~20TB (compressed)
- Growth Rate: ~2TB/year

## 🔧 Maintenance

### Daily Tasks
```sql
-- Check partition status
SELECT TABLE_NAME, PARTITION_NAME, PARTITION_DESCRIPTION 
FROM INFORMATION_SCHEMA.PARTITIONS 
WHERE TABLE_SCHEMA = 'fileflow';

-- Analyze tables for optimizer
ANALYZE TABLE file_assets, upload_sessions;
```

### Weekly Tasks
```sql
-- Clean expired sessions
DELETE FROM upload_sessions 
WHERE expires_at < NOW() 
AND status IN ('EXPIRED', 'CANCELLED');

-- Archive old logs (handled by events)
CALL drop_old_partitions('access_logs', 7);
```

### Monthly Tasks
```sql
-- Create new partitions (automated)
CALL create_audit_log_partitions();

-- Update statistics
ANALYZE TABLE audit_logs, processing_errors;
```

## ⚠️ Important Notes

1. **Event Scheduler**: Must be enabled for automatic partition management
   ```sql
   SET GLOBAL event_scheduler = ON;
   ```

2. **Buffer Pool**: Adjust based on available RAM
   ```sql
   SET GLOBAL innodb_buffer_pool_size = 4294967296; -- 4GB
   ```

3. **Max Connections**: Set based on application needs
   ```sql
   SET GLOBAL max_connections = 500;
   ```

4. **Slow Query Log**: Enable for performance monitoring
   ```sql
   SET GLOBAL slow_query_log = 1;
   SET GLOBAL long_query_time = 2;
   ```

## 🔄 Rollback Strategy

Each migration can be rolled back independently:

```sql
-- Example rollback for V8
DROP TABLE IF EXISTS system_configs;
DROP TABLE IF EXISTS notification_queue;
DROP TABLE IF EXISTS notification_templates;
-- ... etc

-- Update migration history
DELETE FROM schema_migrations_history WHERE version = 'V8';
```

## 📝 Version History

| Version | Date | Description |
|---------|------|-------------|
| V1-V9 | 2025-01-20 | Initial schema creation |

## 📞 Support

For issues or questions:
- Check logs: `/var/log/mysql/error.log`
- Monitor metrics: Grafana dashboards
- Contact: FileFlow Team

---

**Note**: This schema is optimized for:
- High-volume file processing
- Multi-tenant isolation
- Horizontal scalability
- Compliance requirements (GDPR, CCPA)
- Performance monitoring
