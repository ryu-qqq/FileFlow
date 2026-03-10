---
allowed-tools: Bash(mysql:*), Bash(kill:*), Bash(lsof:*), Bash(bash:*), Bash(python3:*), Read
argument-hint: <stage|prod> [SQL 또는 작업 설명]
description: AWS SSM 포트포워딩 + DB 접속 자동화. Stage/Prod RDS에 연결하여 SQL 실행.
---

# DB Access Skill

## 목적
AWS SSM 포트포워딩을 자동으로 설정하고 Stage/Prod RDS에 접속하여 SQL을 실행합니다.
매번 비밀번호와 포트를 알려줄 필요 없이 `/db stage` 또는 `/db prod`로 바로 사용합니다.

## 인수 파싱
첫 번째 인수: 환경 (필수)
- `stage` → Stage RDS (localhost:13308)
- `prod` → Prod RDS (localhost:13307)

나머지 인수: SQL 쿼리 또는 작업 설명 (선택)
- SQL이면 바로 실행
- 작업 설명이면 적절한 SQL을 생성하여 실행
- 없으면 대화형 모드로 진입

## DB 접속 정보

### Stage
```
Host: 127.0.0.1
Port: 13308
User: admin
Password: 7N}ZQ)cIixn:[FtTWZ0>VZ8Zja]2+NyD
스키마: market (기본), setof, crawler
포트포워딩 스크립트: local-dev/scripts/aws-port-forward-stage.sh
```

### Prod
```
Host: 127.0.0.1
Port: 13307
User: admin
Password: E[&mUlOgA+ucv31nRmSDlbOr398VyGep
스키마: market (기본), setof, crawler
포트포워딩 스크립트: local-dev/scripts/aws-port-forward.sh
```

## 실행 순서

### Step 1: 포트포워딩 확인
해당 포트가 열려있는지 확인:
```bash
lsof -i :13308  # stage
lsof -i :13307  # prod
```

### Step 2: 포트포워딩이 안 되어 있으면
백그라운드로 포트포워딩 스크립트를 실행:
```bash
# Stage
bash local-dev/scripts/aws-port-forward-stage.sh &

# Prod
bash local-dev/scripts/aws-port-forward.sh &
```
15초 대기 후 접속 테스트.

### Step 3: DB 접속 및 SQL 실행
```bash
# Stage
mysql -h 127.0.0.1 -P 13308 -u admin -p'7N}ZQ)cIixn:[FtTWZ0>VZ8Zja]2+NyD' market -e "SQL"

# Prod
mysql -h 127.0.0.1 -P 13307 -u admin -p'E[&mUlOgA+ucv31nRmSDlbOr398VyGep' market -e "SQL"
```

## MySQL 명령 패턴
```bash
# 환경별 변수 (내부적으로 사용)
STAGE_MYSQL="mysql -h 127.0.0.1 -P 13308 -u admin -p'7N}ZQ)cIixn:[FtTWZ0>VZ8Zja]2+NyD'"
PROD_MYSQL="mysql -h 127.0.0.1 -P 13307 -u admin -p'E[&mUlOgA+ucv31nRmSDlbOr398VyGep'"
```

## 스키마 참고
- `market`: 메인 서비스 스키마 (sellers, brand, category, inbound_products 등)
- `setof`: SET_OF 레거시 스키마 (brands, categories 등)
- `crawler`: 크롤러 스키마 (crawled_product 등)

스키마 전환: `USE setof;` 또는 `SELECT * FROM setof.brands LIMIT 5;`

## Collation 주의
crawler와 market 스키마의 collation이 다를 수 있음:
- market: `utf8mb4_unicode_ci`
- crawler: `utf8mb4_0900_ai_ci`
JOIN 시 반드시 `COLLATE utf8mb4_unicode_ci` 추가

## 사용 예시
```
/db stage SELECT COUNT(*) FROM sellers
/db prod SHOW TABLES FROM market
/db stage 셀러별 상품 수 조회해줘
/db stage  (→ 대화형: 무엇을 조회할까요?)
```

## 안전 규칙
- **Prod에서 INSERT/UPDATE/DELETE 실행 전에 반드시 사용자 확인**
- Stage는 자유롭게 수정 가능
- 항상 `2>/dev/null`로 경고 숨김 (password on command line 경고 방지)
