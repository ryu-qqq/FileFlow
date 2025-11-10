#!/bin/bash
# ============================================================================
# FileFlow 로컬 실행 스크립트 (AWS RDS/Redis 연결)
# ============================================================================
# 사용법: bash run-local-with-aws.sh
# ============================================================================

set -e

echo "🚀 FileFlow 로컬 환경 시작 중 (AWS 리소스 사용)..."
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. SSH 터널 확인 및 시작
echo "🔍 Step 1: SSH 터널 확인 중..."

# 기존 SSH 터널 프로세스 확인
EXISTING_PID=$(lsof -ti:13306 2>/dev/null || echo "")

if [ -n "$EXISTING_PID" ]; then
    echo -e "${YELLOW}⚠️  기존 SSH 터널 발견 (PID: $EXISTING_PID)${NC}"
    echo "   기존 터널을 종료하고 새로 시작합니다..."
    kill $EXISTING_PID 2>/dev/null || true
    sleep 2
fi

# SSH 터널 시작 (MySQL만)
echo "🔗 SSH 터널 시작 중 (MySQL: localhost:13306 → RDS:3306)..."
ssh -f -N \
    -L 13306:prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com:3306 \
    -i /Users/sangwon-ryu/Downloads/setof-prod.pem \
    ec2-user@3.38.189.162

echo -e "${GREEN}✅ SSH 터널 시작 완료!${NC}"

# 2. 연결 대기
echo "⏳ MySQL 연결 대기 중..."
sleep 3

# 3. 연결 테스트
echo "🔍 MySQL 연결 테스트 중..."
if timeout 5 bash -c 'cat < /dev/null > /dev/tcp/127.0.0.1/13306' 2>&1; then
    echo -e "${GREEN}✅ MySQL 터널 연결 확인!${NC}"
else
    echo -e "${RED}❌ MySQL 터널 연결 실패!${NC}"
    echo "   터널을 수동으로 확인하세요: lsof -i :13306"
    exit 1
fi

echo ""
echo "============================================"
echo -e "${GREEN}✅ SSH 터널 준비 완료!${NC}"
echo "============================================"
echo ""
echo "연결 정보:"
echo "  - MySQL (터널):  localhost:13306"
echo "  - RDS 실제 호스트: prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com"
echo ""

# 4. AWS 환경 변수 설정
echo "📦 Step 2: AWS 환경 변수 설정..."

# AWS 자격 증명 확인
if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo -e "${YELLOW}⚠️  AWS 자격 증명이 환경 변수에 없습니다.${NC}"
    echo "   기본 AWS CLI 자격 증명을 사용합니다."
fi

# 5. application-local-aws.yml 생성 (임시)
echo "📄 Step 3: AWS 연결 설정 파일 생성 중..."

cat > /tmp/application-local-aws.yml << 'EOF'
# ===============================================
# Local Profile with AWS Resources
# ===============================================

spring:
  datasource:
    # SSH 터널을 통한 RDS 연결
    url: jdbc:mysql://localhost:13306/fileflow?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: fileflow-user
    password: FxGkwjtQaFbD60Pjvg5LlSeTwhbSa1tF
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      pool-name: WebAPI-AWS-HikariPool

  data:
    redis:
      # 로컬 Redis 또는 ElastiCache 엔드포인트
      host: localhost
      port: 6379
      password: ""
      timeout: 3000ms

  flyway:
    clean-disabled: true  # AWS RDS는 clean 금지
    baseline-on-migrate: true

aws:
  s3:
    region: ap-northeast-2
    bucket-name: ${S3_BUCKET_NAME:fileflow-prod}
    # AWS CLI 자격 증명 사용 (환경 변수 또는 ~/.aws/credentials)
    access-key: ${AWS_ACCESS_KEY_ID:}
    secret-key: ${AWS_SECRET_ACCESS_KEY:}

server:
  port: 8083

logging:
  level:
    root: INFO
    com.ryuqq.fileflow: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOF

echo -e "${GREEN}✅ 설정 파일 생성 완료: /tmp/application-local-aws.yml${NC}"

# 6. Gradle 빌드
echo ""
echo "📦 Step 4: Gradle 빌드 시작..."
./gradlew :bootstrap:bootstrap-web-api:clean :bootstrap:bootstrap-web-api:build -x test

echo ""
echo "🚀 Step 5: FileFlow Web API 서버 시작 (AWS 리소스 사용)..."
echo ""
echo "============================================"
echo -e "${YELLOW}🌟 서버 접속 정보${NC}"
echo "============================================"
echo "  - API Server: http://localhost:8083"
echo "  - Actuator:   http://localhost:8083/actuator"
echo "  - Health:     http://localhost:8083/actuator/health"
echo ""
echo "  - Database:   RDS via SSH Tunnel (localhost:13306)"
echo "  - S3 Bucket:  \${S3_BUCKET_NAME}"
echo "============================================"
echo ""
echo "Ctrl+C로 종료하세요"
echo ""

# 7. 서버 실행
./gradlew :bootstrap:bootstrap-web-api:bootRun \
    --args="--spring.profiles.active=local --spring.config.additional-location=file:/tmp/application-local-aws.yml"
