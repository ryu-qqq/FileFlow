# 🚀 FileFlow 로컬 실행 가이드

FileFlow를 로컬 환경에서 빠르게 실행하는 방법입니다.

---

## 📦 방법 1: 로컬 Docker 환경 (추천!)

**완전히 독립적인 로컬 환경**으로 실행합니다. AWS 연결 필요 없음!

### 필요 사항
- Docker Desktop 설치
- Java 21

### 실행 명령어

```bash
# 한 줄로 실행!
bash run-local.sh
```

### 제공되는 서비스
- **MySQL**: `localhost:3306` (fileflow / fileflow-user / fileflow-password)
- **Redis**: `localhost:6379`
- **MinIO** (S3 호환): `http://localhost:9000` (minioadmin / minioadmin)
  - MinIO Console: `http://localhost:9001`
  - **자동 버킷 생성**: `fileflow-local` (업로드 즉시 사용 가능!)
  - S3 테스트 가이드: [S3_LOCAL_TEST_GUIDE.md](S3_LOCAL_TEST_GUIDE.md)

### 서버 접속 정보
- **API Server**: `http://localhost:8083`
- **Actuator**: `http://localhost:8083/actuator`
- **Health Check**: `http://localhost:8083/actuator/health`

### 종료 방법

```bash
# 서버 종료
Ctrl+C

# Docker 서비스 종료
docker-compose -f docker-compose.local.yml down

# 데이터까지 완전히 삭제
docker-compose -f docker-compose.local.yml down -v
```

---

## ☁️ 방법 2: AWS RDS/S3 연결

**실제 AWS 리소스**(RDS, S3)를 로컬에서 테스트할 때 사용합니다.

### 필요 사항
- SSH 키: `/Users/sangwon-ryu/Downloads/setof-prod.pem`
- AWS 자격 증명 (선택사항)

### 실행 명령어

```bash
# 한 줄로 실행!
bash run-local-with-aws.sh
```

### 연결 정보
- **Database**: RDS via SSH Tunnel (localhost:13306 → prod-shared-mysql)
- **S3**: AWS S3 (자격 증명 필요)
- **Redis**: 로컬 Redis (localhost:6379)

### 환경 변수 (선택사항)

```bash
# S3 버킷 지정
export S3_BUCKET_NAME=your-bucket-name

# AWS 자격 증명 (없으면 ~/.aws/credentials 사용)
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key

# 실행
bash run-local-with-aws.sh
```

---

## 🧪 테스트 API 호출

서버가 실행되면 다음 API를 테스트할 수 있습니다:

### 기본 Health Check
```bash
# Health Check
curl http://localhost:8083/actuator/health

# Actuator Endpoints
curl http://localhost:8083/actuator
```

### S3 업로드 테스트 (MinIO)
```bash
# 1. Single Upload 시작
curl -X POST http://localhost:8083/api/v1/upload/init-single \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.txt",
    "fileSize": 1024,
    "contentType": "text/plain"
  }'

# 2. Presigned URL로 파일 업로드
# (위 응답에서 받은 presignedUrl 사용)
curl -X PUT "<presignedUrl>" \
  -H "Content-Type: text/plain" \
  --data-binary @test.txt

# 3. MinIO Console에서 확인
# http://localhost:9001 → Buckets → fileflow-local
```

**상세 가이드**: [S3_LOCAL_TEST_GUIDE.md](S3_LOCAL_TEST_GUIDE.md)

---

## 🛠️ 트러블슈팅

### 1. "포트가 이미 사용 중" 오류

```bash
# MySQL 포트 충돌 (3306)
docker ps | grep 3306
# 또는
lsof -i :3306

# 실행 중인 컨테이너 종료
docker stop <container-id>
```

### 2. Docker Compose 문제

```bash
# Docker 서비스 완전 재시작
docker-compose -f docker-compose.local.yml down -v
docker-compose -f docker-compose.local.yml up -d
```

### 3. SSH 터널 문제 (AWS 연결 시)

```bash
# 기존 터널 확인
lsof -i :13306

# 터널 종료
kill <PID>

# 수동 터널 재시작
ssh -f -N \
    -L 13306:prod-shared-mysql.cfacertspqbw.ap-northeast-2.rds.amazonaws.com:3306 \
    -i /Users/sangwon-ryu/Downloads/setof-prod.pem \
    ec2-user@3.38.189.162
```

### 4. Gradle 빌드 실패

```bash
# 캐시 정리 후 재빌드
./gradlew clean build -x test
```

---

## 📝 다음 단계

서버가 정상 실행되면:

1. **API 문서 확인**: `http://localhost:8083/swagger-ui.html` (Swagger 설정 시)
2. **데이터베이스 확인**: MySQL Workbench, DataGrip 등으로 `localhost:3306` 접속
3. **MinIO 파일 확인**: `http://localhost:9001` (MinIO Console)
4. **로그 확인**: 콘솔 출력 또는 `logs/` 디렉토리

---

## 🎯 빠른 실행 요약

### 로컬 환경 (추천)
```bash
bash run-local.sh
# → http://localhost:8083
```

### AWS 연결
```bash
bash run-local-with-aws.sh
# → http://localhost:8083 (RDS, S3 사용)
```

### 종료
```bash
Ctrl+C                                          # 서버 종료
docker-compose -f docker-compose.local.yml down # Docker 종료
```

---

**준비 완료! 🚀**
