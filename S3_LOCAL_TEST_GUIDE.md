# 🪣 S3 로컬 테스트 가이드 (MinIO)

FileFlow에서 S3를 로컬에서 테스트하는 방법입니다.

---

## 📋 현재 설정 요약

### ✅ 이미 구성된 항목

1. **Docker Compose (docker-compose.local.yml)**
   - MinIO Server: S3 호환 로컬 스토리지
   - MinIO Client: 버킷 자동 생성

2. **Spring Boot (application-local.yml)**
   - S3 Client: MinIO endpoint 자동 연결
   - Presigned URL: MinIO 기반 URL 생성

3. **Java Code (S3ClientConfiguration.java)**
   - Endpoint Override 지원 (Line 70-72, 98-100)
   - AWS SDK v2 기반 (MinIO 완벽 호환)

---

## 🚀 빠른 시작

### 1. MinIO 시작 (버킷 자동 생성)

```bash
# Docker Compose로 MinIO 시작
docker-compose -f docker-compose.local.yml up -d

# 로그 확인 (버킷 생성 확인)
docker logs fileflow-minio-init

# 출력 예시:
# Added `myminio` successfully.
# Bucket created successfully `myminio/fileflow-local`.
# MinIO bucket fileflow-local created successfully
```

### 2. MinIO Console 접속

**URL**: http://localhost:9001

**로그인 정보**:
- Username: `minioadmin`
- Password: `minioadmin`

### 3. 버킷 확인

MinIO Console → Buckets → `fileflow-local` 확인

---

## 🧪 S3 연결 테스트

### 방법 1: FileFlow API로 테스트

```bash
# 1. FileFlow 서버 시작
bash run-local.sh

# 2. 파일 업로드 테스트 (Presigned URL 방식)
curl -X POST http://localhost:8083/api/v1/upload/init-single \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.txt",
    "fileSize": 1024,
    "contentType": "text/plain"
  }'

# 3. 응답 예시:
# {
#   "uploadId": "...",
#   "presignedUrl": "http://localhost:9000/fileflow-local/test.txt?X-Amz-..."
# }

# 4. Presigned URL로 파일 업로드
curl -X PUT "<presignedUrl>" \
  -H "Content-Type: text/plain" \
  --data-binary @test.txt
```

### 방법 2: AWS CLI로 테스트

```bash
# AWS CLI 설치 (없는 경우)
brew install awscli

# MinIO 프로필 설정
aws configure --profile minio
# AWS Access Key ID: minioadmin
# AWS Secret Access Key: minioadmin
# Default region name: ap-northeast-2
# Default output format: json

# 버킷 목록 확인
aws s3 ls --endpoint-url http://localhost:9000 --profile minio

# 파일 업로드
echo "Hello MinIO" > test.txt
aws s3 cp test.txt s3://fileflow-local/ --endpoint-url http://localhost:9000 --profile minio

# 파일 목록 확인
aws s3 ls s3://fileflow-local/ --endpoint-url http://localhost:9000 --profile minio

# 파일 다운로드
aws s3 cp s3://fileflow-local/test.txt ./downloaded.txt --endpoint-url http://localhost:9000 --profile minio
```

### 방법 3: MinIO Client (mc)로 테스트

```bash
# MinIO Client 설치
brew install minio/stable/mc

# MinIO 서버 별칭 설정
mc alias set local http://localhost:9000 minioadmin minioadmin

# 버킷 목록 확인
mc ls local

# 파일 업로드
mc cp test.txt local/fileflow-local/

# 파일 목록 확인
mc ls local/fileflow-local

# 파일 다운로드
mc cp local/fileflow-local/test.txt ./downloaded.txt
```

---

## 🔧 설정 상세

### Docker Compose (docker-compose.local.yml)

```yaml
# MinIO Server
minio:
  image: minio/minio:latest
  ports:
    - "9000:9000"  # S3 API
    - "9001:9001"  # Web Console
  environment:
    MINIO_ROOT_USER: minioadmin
    MINIO_ROOT_PASSWORD: minioadmin
  volumes:
    - minio-data:/data

# MinIO Client (버킷 자동 생성)
minio-init:
  image: minio/mc:latest
  depends_on:
    minio:
      condition: service_healthy
  entrypoint: >
    /bin/sh -c "
    /usr/bin/mc alias set myminio http://minio:9000 minioadmin minioadmin;
    /usr/bin/mc mb myminio/fileflow-local --ignore-existing;
    /usr/bin/mc anonymous set download myminio/fileflow-local;
    echo 'MinIO bucket fileflow-local created successfully';
    exit 0;
    "
```

**주요 기능**:
- `mc mb`: 버킷 생성 (Make Bucket)
- `--ignore-existing`: 이미 존재하면 무시
- `mc anonymous set download`: 공개 다운로드 허용

### Spring Boot (application-local.yml)

```yaml
aws:
  s3:
    region: ap-northeast-2
    bucket-name: fileflow-local
    access-key: ${AWS_ACCESS_KEY_ID:minioadmin}
    secret-key: ${AWS_SECRET_ACCESS_KEY:minioadmin}
    endpoint: ${AWS_S3_ENDPOINT:http://localhost:9000}  # MinIO
```

**주요 설정**:
- `endpoint`: MinIO 서버 URL (AWS S3는 설정 안 함)
- `bucket-name`: 자동 생성된 버킷 이름
- `access-key/secret-key`: MinIO 자격증명

### Java Code (S3ClientConfiguration.java)

```java
@Bean
public S3Client s3Client() {
    var builder = S3Client.builder()
        .region(Region.of(properties.getRegion()))
        .credentialsProvider(...);

    // MinIO endpoint override (로컬 전용)
    if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
        builder.endpointOverride(java.net.URI.create(properties.getEndpoint()));
    }

    return builder.build();
}
```

**동작 원리**:
- `endpoint`가 있으면 → MinIO 연결
- `endpoint`가 없으면 → AWS S3 연결

---

## 📊 MinIO vs AWS S3 비교

| 항목 | MinIO (로컬) | AWS S3 (운영) |
|------|--------------|---------------|
| **URL** | http://localhost:9000 | https://s3.ap-northeast-2.amazonaws.com |
| **자격증명** | minioadmin / minioadmin | AWS IAM Credentials |
| **버킷** | 로컬 Docker Volume | AWS S3 Bucket |
| **비용** | 무료 | 사용량 기반 과금 |
| **성능** | 로컬 디스크 속도 | 네트워크 속도 |
| **용도** | 개발 및 테스트 | 운영 환경 |

---

## 🛠️ 트러블슈팅

### 1. "Bucket does not exist" 오류

**원인**: `fileflow-local` 버킷이 자동 생성되지 않음

**해결**:
```bash
# MinIO 초기화 컨테이너 로그 확인
docker logs fileflow-minio-init

# 수동으로 버킷 생성
docker exec -it fileflow-minio-local mc mb /data/fileflow-local

# 또는 MinIO Console에서 수동 생성
# http://localhost:9001 → Buckets → Create Bucket
```

### 2. "Access Denied" 오류

**원인**: MinIO 자격증명 불일치

**확인**:
```bash
# application-local.yml 확인
cat bootstrap/bootstrap-web-api/src/main/resources/application-local.yml | grep -A 5 "aws:"

# 출력 예시:
# aws:
#   s3:
#     access-key: minioadmin
#     secret-key: minioadmin
```

### 3. "Connection refused" 오류

**원인**: MinIO 서버가 시작되지 않음

**해결**:
```bash
# MinIO 컨테이너 상태 확인
docker ps | grep minio

# MinIO 재시작
docker-compose -f docker-compose.local.yml restart minio

# 로그 확인
docker logs fileflow-minio-local
```

### 4. Presigned URL이 localhost로 생성되는 문제

**원인**: MinIO는 기본적으로 localhost URL 생성

**확인**:
```bash
# Presigned URL 예시
http://localhost:9000/fileflow-local/test.txt?X-Amz-Algorithm=...
```

**해결** (외부 접속 필요 시):
```yaml
# docker-compose.local.yml
minio:
  environment:
    MINIO_SERVER_URL: http://your-ip:9000  # 외부 IP 설정
```

---

## 📝 다음 단계

### 1. Multipart Upload 테스트

```bash
# 큰 파일 생성 (100MB)
dd if=/dev/zero of=large.bin bs=1m count=100

# Multipart Upload 시작
curl -X POST http://localhost:8083/api/v1/upload/init-multipart \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "large.bin",
    "fileSize": 104857600,
    "contentType": "application/octet-stream",
    "partCount": 5
  }'
```

### 2. Presigned URL 다운로드 테스트

```bash
# 다운로드 URL 생성
curl -X POST http://localhost:8083/api/v1/files/download-url \
  -H "Content-Type: application/json" \
  -d '{
    "fileId": 123,
    "expirationMinutes": 10
  }'

# 응답받은 URL로 다운로드
curl "<presignedUrl>" -o downloaded-file.bin
```

### 3. MinIO 모니터링

**MinIO Console**: http://localhost:9001
- Buckets → fileflow-local → Objects (업로드된 파일 확인)
- Monitoring → Metrics (성능 지표)
- Access → Service Accounts (권한 관리)

---

**✅ 이제 로컬에서 S3를 완벽하게 테스트할 수 있습니다!**
