#!/bin/bash
# ============================================================================
# FileFlow 로컬 실행 스크립트
# ============================================================================
# 사용법: bash run-local.sh
# ============================================================================

set -e

echo "🚀 FileFlow 로컬 환경 시작 중..."
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Docker Compose로 의존성 시작
echo "📦 Step 1: Docker Compose 시작 중 (MySQL, Redis, MinIO)..."
docker-compose -f docker-compose.local.yml up -d

echo ""
echo "⏳ 서비스가 준비될 때까지 대기 중..."
sleep 5

# 2. MySQL이 준비될 때까지 대기
echo "🔍 MySQL 연결 대기 중..."
until docker exec fileflow-mysql-local mysqladmin ping -h localhost --silent; do
    echo "   MySQL이 아직 준비되지 않았습니다. 5초 후 재시도..."
    sleep 5
done
echo -e "${GREEN}✅ MySQL 준비 완료!${NC}"

# 3. Redis 확인
echo "🔍 Redis 연결 확인 중..."
docker exec fileflow-redis-local redis-cli ping > /dev/null
echo -e "${GREEN}✅ Redis 준비 완료!${NC}"

# 4. MinIO 확인
echo "🔍 MinIO 연결 확인 중..."
sleep 3
echo -e "${GREEN}✅ MinIO 준비 완료!${NC}"

echo ""
echo "============================================"
echo -e "${GREEN}✅ 모든 의존성 서비스 준비 완료!${NC}"
echo "============================================"
echo ""
echo "서비스 정보:"
echo "  - MySQL:  localhost:3306 (fileflow / fileflow-user / fileflow-password)"
echo "  - Redis:  localhost:6379"
echo "  - MinIO:  http://localhost:9000 (minioadmin / minioadmin)"
echo "  - MinIO Console: http://localhost:9001"
echo ""

# 5. Gradle 빌드 및 실행
echo "📦 Step 2: Gradle 빌드 시작..."
./gradlew :bootstrap:bootstrap-web-api:clean :bootstrap:bootstrap-web-api:build -x test

echo ""
echo "🚀 Step 3: FileFlow Web API 서버 시작..."
echo ""
echo "============================================"
echo -e "${YELLOW}🌟 서버 접속 정보${NC}"
echo "============================================"
echo "  - API Server: http://localhost:8083"
echo "  - Actuator:   http://localhost:8083/actuator"
echo "  - Health:     http://localhost:8083/actuator/health"
echo "============================================"
echo ""
echo "Ctrl+C로 종료하세요"
echo ""

# 6. 서버 실행 (Foreground)
./gradlew :bootstrap:bootstrap-web-api:bootRun --args='--spring.profiles.active=local'
