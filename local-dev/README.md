# FileFlow 로컬 개발 환경

로컬에서 FileFlow를 개발하고 테스트하기 위한 Docker Compose 환경 모음입니다.

## 📦 구성

```
local-dev/
├── README.md                          # 이 파일
├── docker-compose.local.yml          # 완전 독립 로컬 환경
├── docker-compose.aws.yml            # AWS 리소스 연결 환경
├── .env.local.example                # 로컬 환경 변수 예시
├── .env.aws.example                  # AWS 환경 변수 예시
├── scripts/
│   ├── local-start.sh                # 로컬 환경 시작
│   ├── local-stop.sh                 # 로컬 환경 종료
│   ├── aws-start.sh                  # AWS 연결 환경 시작
│   ├── aws-stop.sh                   # AWS 연결 환경 종료
│   └── aws-port-forward.sh           # AWS SSM 포트 포워딩
└── docs/
    ├── LOCAL_SETUP.md                # 로컬 환경 상세 가이드
    └── AWS_SETUP.md                  # AWS 연결 환경 상세 가이드
```

## 🚀 빠른 시작

### 방법 1: 완전 독립 로컬 환경 (권장 - 일반 개발용)

로컬 MySQL, Redis를 Docker로 실행하여 완전히 독립된 환경에서 개발합니다.

```bash
cd local-dev

# 환경 변수 설정 (선택사항)
cp .env.local .env.local

# 시작
./scripts/local-start.sh

# 종료
./scripts/local-stop.sh
```

**특징:**
- ✅ 인터넷 연결 불필요 (빌드 후)
- ✅ AWS 계정 불필요
- ✅ 빠른 시작/종료
- ✅ 데이터 격리 (로컬 볼륨)
- ❌ AWS 서비스(S3, SQS) 비활성화

### 방법 2: AWS 리소스 연결 환경 (프로덕션 데이터 테스트용)

실제 AWS RDS, ElastiCache, S3, SQS에 연결하여 프로덕션과 동일한 환경에서 테스트합니다.

```bash
cd local-dev

# 1. 환경 변수 설정 (필수)
cp .env.aws .env.aws
vim .env.aws  # AWS 자격 증명 입력

# 2. AWS 포트 포워딩 시작 (터미널 1)
./scripts/aws-port-forward.sh

# 3. Docker Compose 시작 (터미널 2)
./scripts/aws-start.sh

# 4. 종료
./scripts/aws-stop.sh
# 포트 포워딩 터미널에서 Ctrl+C
```

**특징:**
- ✅ 실제 프로덕션 데이터 접근
- ✅ AWS 서비스(S3, SQS) 활성화
- ✅ 프로덕션 환경 디버깅
- ❌ AWS 계정 및 권한 필요
- ❌ 인터넷 연결 필수
- ⚠️  프로덕션 데이터 주의

## 📊 환경 비교

| 항목 | 로컬 환경 | AWS 연결 환경 |
|------|----------|--------------|
| **MySQL** | 로컬 Docker 컨테이너 | AWS RDS (SSM 포워딩) |
| **Redis** | 로컬 Docker 컨테이너 | AWS ElastiCache (SSM 포워딩) |
| **S3** | 비활성화/Mock | 실제 AWS S3 |
| **SQS** | 비활성화 | 실제 AWS SQS |
| **데이터** | 로컬 테스트 데이터 | 프로덕션 데이터 |
| **AWS 계정** | 불필요 | 필요 |
| **인터넷** | 불필요 | 필요 |
| **시작 속도** | 빠름 (~30초) | 느림 (~2분) |
| **용도** | 일반 개발, 단위 테스트 | 통합 테스트, 디버깅 |

## 🔧 서비스 포트

| 서비스 | 로컬 환경 | AWS 환경 |
|--------|----------|----------|
| Web API | http://localhost:8080 | http://localhost:8080 |
| Scheduler | http://localhost:8081 | http://localhost:8081 |
| Download Worker | http://localhost:8082 | http://localhost:8082 |
| MySQL | localhost:13306 | localhost:13307 (포워딩) |
| Redis | localhost:16379 | localhost:16380 (포워딩) |

## 📝 상세 가이드

- [로컬 환경 상세 가이드](docs/LOCAL_SETUP.md)
- [AWS 연결 환경 상세 가이드](docs/AWS_SETUP.md)

## 🛠️ 트러블슈팅

### 포트 충돌

```bash
# 포트 사용 확인
lsof -i :8080
lsof -i :3306

# 프로세스 종료
kill -9 <PID>
```

### Docker 빌드 실패

```bash
# 캐시 없이 재빌드
cd local-dev
docker-compose -f docker-compose.local.yml build --no-cache
```

### AWS 연결 실패

```bash
# AWS 자격 증명 확인
aws sts get-caller-identity

# 포트 포워딩 재시작
./scripts/aws-port-forward.sh
```

## 🔒 보안 주의사항

### 로컬 환경
- `.env.local` 파일은 Git에 커밋하지 마세요
- 로컬 MySQL 비밀번호는 간단하게 설정 가능 (개발용)

### AWS 환경
- `.env.aws` 파일은 **절대** Git에 커밋하지 마세요
- AWS 자격 증명은 최소 권한 원칙 적용
- 프로덕션 데이터 수정 시 각별히 주의
- AWS SSO 사용 권장 (임시 자격 증명)

## 📚 추가 리소스

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [AWS SSM Session Manager](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager.html)
- [FileFlow 아키텍처 문서](../docs/ARCHITECTURE.md)
