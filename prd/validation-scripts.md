# 파일 업로드 시스템 검증 스크립트

## 🔍 자동 검증 스크립트 모음

### 1. Zero-Tolerance 규칙 검증기

#### validate-zero-tolerance.sh
```bash
#!/bin/bash
# Zero-Tolerance 규칙 자동 검증 스크립트

echo "🔍 Zero-Tolerance 규칙 검증 시작..."

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0

# 1. Lombok 검사
echo -e "\n${YELLOW}1. Lombok 사용 검사${NC}"
LOMBOK_FOUND=$(find domain application -name "*.java" -exec grep -l "@Data\|@Getter\|@Setter\|@Builder\|@AllArgsConstructor\|@NoArgsConstructor" {} \; 2>/dev/null)
if [ -z "$LOMBOK_FOUND" ]; then
    echo -e "${GREEN}✅ Lombok 미사용 - PASS${NC}"
else
    echo -e "${RED}❌ Lombok 사용 발견:${NC}"
    echo "$LOMBOK_FOUND"
    ERRORS=$((ERRORS+1))
fi

# 2. Getter 체이닝 검사
echo -e "\n${YELLOW}2. Law of Demeter (Getter 체이닝) 검사${NC}"
CHAINING_FOUND=$(find domain application -name "*.java" -exec grep -n "\.get[A-Z][a-zA-Z]*()\.get" {} + 2>/dev/null)
if [ -z "$CHAINING_FOUND" ]; then
    echo -e "${GREEN}✅ Getter 체이닝 없음 - PASS${NC}"
else
    echo -e "${RED}❌ Getter 체이닝 발견:${NC}"
    echo "$CHAINING_FOUND"
    ERRORS=$((ERRORS+1))
fi

# 3. JPA 관계 어노테이션 검사
echo -e "\n${YELLOW}3. JPA 관계 어노테이션 검사${NC}"
JPA_RELATIONS=$(find adapter-out/persistence -name "*Entity.java" -exec grep -l "@ManyToOne\|@OneToMany\|@OneToOne\|@ManyToMany\|@JoinColumn" {} \; 2>/dev/null)
if [ -z "$JPA_RELATIONS" ]; then
    echo -e "${GREEN}✅ JPA 관계 어노테이션 미사용 - PASS${NC}"
else
    echo -e "${RED}❌ JPA 관계 어노테이션 발견:${NC}"
    echo "$JPA_RELATIONS"
    ERRORS=$((ERRORS+1))
fi

# 4. Transaction 경계 검사
echo -e "\n${YELLOW}4. Transaction 경계 검사${NC}"
WRONG_TX=$(find domain adapter-in adapter-out -name "*.java" -exec grep -l "@Transactional" {} \; 2>/dev/null)
if [ -z "$WRONG_TX" ]; then
    echo -e "${GREEN}✅ Transaction 경계 준수 - PASS${NC}"
else
    echo -e "${YELLOW}⚠️  Application Layer 외부에서 @Transactional 발견 (수동 확인 필요):${NC}"
    echo "$WRONG_TX"
fi

# 5. Private/Final 메서드 @Transactional 검사
echo -e "\n${YELLOW}5. Private/Final 메서드 @Transactional 검사${NC}"
PRIVATE_TX=$(find . -name "*.java" -exec grep -B1 "@Transactional" {} \; | grep -E "private|final" 2>/dev/null)
if [ -z "$PRIVATE_TX" ]; then
    echo -e "${GREEN}✅ Private/Final 메서드에 @Transactional 없음 - PASS${NC}"
else
    echo -e "${RED}❌ Private/Final 메서드에 @Transactional 발견:${NC}"
    echo "$PRIVATE_TX"
    ERRORS=$((ERRORS+1))
fi

# 결과 요약
echo -e "\n=========================================="
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ 모든 Zero-Tolerance 규칙 통과!${NC}"
    exit 0
else
    echo -e "${RED}❌ Zero-Tolerance 규칙 위반 발견: $ERRORS 건${NC}"
    exit 1
fi
```

### 2. Domain Layer 검증기

#### validate-domain-layer.py
```python
#!/usr/bin/env python3
"""
Domain Layer 검증 스크립트
- Aggregate Root 패턴 검증
- Value Object 불변성 검증
- Domain Event 구조 검증
"""

import os
import re
import sys
from pathlib import Path

class DomainValidator:
    def __init__(self, domain_path="domain/src/main/java"):
        self.domain_path = Path(domain_path)
        self.errors = []
        self.warnings = []

    def validate_aggregate_root(self, file_path):
        """Aggregate Root 패턴 검증"""
        with open(file_path, 'r') as f:
            content = f.read()

            # AbstractAggregateRoot 상속 확인
            if 'extends AbstractAggregateRoot' in content:
                # registerEvent 사용 확인
                if 'registerEvent' not in content:
                    self.warnings.append(f"{file_path}: AbstractAggregateRoot 상속하지만 이벤트 미발행")

            # Static Factory Method 확인
            if not re.search(r'public static \w+ (create|of|from)\(', content):
                self.warnings.append(f"{file_path}: Static Factory Method 미사용")

            # Lombok 확인
            if any(anno in content for anno in ['@Data', '@Getter', '@Setter', '@Builder']):
                self.errors.append(f"{file_path}: Lombok 사용 발견!")

    def validate_value_object(self, file_path):
        """Value Object 불변성 검증"""
        with open(file_path, 'r') as f:
            content = f.read()

            # final 필드 확인
            fields = re.findall(r'private (?!final)\w+ \w+;', content)
            if fields:
                self.warnings.append(f"{file_path}: non-final 필드 발견: {fields}")

            # equals/hashCode 구현 확인
            if 'public boolean equals' not in content:
                self.warnings.append(f"{file_path}: equals() 메서드 미구현")
            if 'public int hashCode' not in content:
                self.warnings.append(f"{file_path}: hashCode() 메서드 미구현")

    def validate_domain_event(self, file_path):
        """Domain Event 구조 검증"""
        with open(file_path, 'r') as f:
            content = f.read()

            # 불변성 확인
            fields = re.findall(r'private (?!final)\w+ \w+;', content)
            if fields:
                self.errors.append(f"{file_path}: Event에 mutable 필드: {fields}")

            # Static Factory 확인
            if not re.search(r'public static \w+Event (of|from)\(', content):
                self.warnings.append(f"{file_path}: Event에 Static Factory Method 미사용")

    def run(self):
        """전체 검증 실행"""
        print("🔍 Domain Layer 검증 시작...")

        for java_file in self.domain_path.rglob("*.java"):
            file_name = java_file.name

            if 'Aggregate' in file_name or 'Entity' in file_name:
                self.validate_aggregate_root(java_file)
            elif 'ValueObject' in file_name or 'VO' in file_name:
                self.validate_value_object(java_file)
            elif 'Event' in file_name:
                self.validate_domain_event(java_file)

        # 결과 출력
        if self.errors:
            print("\n❌ 오류:")
            for error in self.errors:
                print(f"  - {error}")

        if self.warnings:
            print("\n⚠️  경고:")
            for warning in self.warnings:
                print(f"  - {warning}")

        if not self.errors and not self.warnings:
            print("✅ Domain Layer 검증 통과!")

        return len(self.errors)

if __name__ == "__main__":
    validator = DomainValidator()
    exit_code = validator.run()
    sys.exit(exit_code)
```

### 3. Application Layer 검증기

#### validate-application-layer.py
```python
#!/usr/bin/env python3
"""
Application Layer 검증 스크립트
- UseCase 단일 책임 검증
- Command/Query 분리 검증
- Transaction 경계 검증
"""

import os
import re
import sys
from pathlib import Path

class ApplicationValidator:
    def __init__(self, app_path="application/src/main/java"):
        self.app_path = Path(app_path)
        self.errors = []
        self.warnings = []

    def validate_usecase(self, file_path):
        """UseCase 패턴 검증"""
        with open(file_path, 'r') as f:
            content = f.read()

            # 단일 public 메서드 확인
            public_methods = re.findall(r'public \w+ (\w+)\(', content)
            if len(public_methods) > 2:  # execute + 생성자
                self.warnings.append(
                    f"{file_path}: UseCase에 multiple public 메서드: {public_methods}"
                )

            # @Transactional 위치 확인
            if '@Transactional' in content:
                # private 메서드에 있는지 확인
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if '@Transactional' in line:
                        # 다음 줄 확인
                        if i + 1 < len(lines):
                            next_line = lines[i + 1]
                            if 'private' in next_line:
                                self.errors.append(
                                    f"{file_path}: Private 메서드에 @Transactional"
                                )

            # 외부 호출 확인
            if '@Transactional' in content:
                if any(api in content for api in ['RestTemplate', 'WebClient', 'HttpClient']):
                    self.errors.append(
                        f"{file_path}: @Transactional 내 외부 API 호출 발견!"
                    )

    def validate_command_query(self, file_path):
        """Command/Query 분리 검증"""
        file_name = file_path.name

        with open(file_path, 'r') as f:
            content = f.read()

            if 'Command' in file_name:
                # Command는 void 반환 권장
                if not re.search(r'public void \w+\(', content):
                    self.warnings.append(f"{file_path}: Command가 값을 반환함")

            elif 'Query' in file_name:
                # Query는 상태 변경 금지
                if any(word in content for word in ['save', 'update', 'delete', 'insert']):
                    self.errors.append(f"{file_path}: Query에서 상태 변경 시도")

    def run(self):
        """전체 검증 실행"""
        print("🔍 Application Layer 검증 시작...")

        for java_file in self.app_path.rglob("*.java"):
            if 'UseCase' in java_file.name:
                self.validate_usecase(java_file)
            if 'Command' in java_file.name or 'Query' in java_file.name:
                self.validate_command_query(java_file)

        # 결과 출력
        if self.errors:
            print("\n❌ 오류:")
            for error in self.errors:
                print(f"  - {error}")

        if self.warnings:
            print("\n⚠️  경고:")
            for warning in self.warnings:
                print(f"  - {warning}")

        if not self.errors and not self.warnings:
            print("✅ Application Layer 검증 통과!")

        return len(self.errors)

if __name__ == "__main__":
    validator = ApplicationValidator()
    exit_code = validator.run()
    sys.exit(exit_code)
```

### 4. 통합 검증 실행기

#### run-all-validations.sh
```bash
#!/bin/bash
# 모든 검증 스크립트 실행

echo "======================================"
echo "📋 FileFlow 파일 업로드 시스템 검증"
echo "======================================"

TOTAL_ERRORS=0

# 1. Zero-Tolerance 규칙 검증
echo -e "\n[1/4] Zero-Tolerance 규칙 검증"
bash validate-zero-tolerance.sh
TOTAL_ERRORS=$((TOTAL_ERRORS + $?))

# 2. Domain Layer 검증
echo -e "\n[2/4] Domain Layer 검증"
python3 validate-domain-layer.py
TOTAL_ERRORS=$((TOTAL_ERRORS + $?))

# 3. Application Layer 검증
echo -e "\n[3/4] Application Layer 검증"
python3 validate-application-layer.py
TOTAL_ERRORS=$((TOTAL_ERRORS + $?))

# 4. ArchUnit 테스트
echo -e "\n[4/4] ArchUnit 아키텍처 테스트"
./gradlew test --tests "*ArchitectureTest" --quiet
TOTAL_ERRORS=$((TOTAL_ERRORS + $?))

# 최종 결과
echo -e "\n======================================"
if [ $TOTAL_ERRORS -eq 0 ]; then
    echo -e "✅ 모든 검증 통과! 배포 가능합니다."
    exit 0
else
    echo -e "❌ 검증 실패: $TOTAL_ERRORS 개 문제 발견"
    echo -e "위 문제들을 수정한 후 다시 검증하세요."
    exit 1
fi
```

### 5. Git Pre-Push Hook

#### .git/hooks/pre-push
```bash
#!/bin/bash
# Pre-push hook: 푸시 전 자동 검증

echo "🔍 Pre-push 검증 실행 중..."

# Zero-Tolerance 규칙만 빠르게 검증
bash prd/validate-zero-tolerance.sh

if [ $? -ne 0 ]; then
    echo "❌ Zero-Tolerance 규칙 위반으로 push가 차단되었습니다."
    echo "위반 사항을 수정한 후 다시 시도하세요."
    exit 1
fi

echo "✅ Pre-push 검증 통과"
exit 0
```

---

## 🚀 검증 스크립트 사용법

### 1. 스크립트 설치
```bash
# 실행 권한 부여
chmod +x prd/*.sh
chmod +x prd/*.py

# Git hooks 설치
cp prd/.git/hooks/pre-push .git/hooks/
chmod +x .git/hooks/pre-push
```

### 2. 개별 검증 실행
```bash
# Zero-Tolerance 규칙만 검증
./prd/validate-zero-tolerance.sh

# Domain Layer만 검증
python3 prd/validate-domain-layer.py

# Application Layer만 검증
python3 prd/validate-application-layer.py
```

### 3. 전체 검증 실행
```bash
# 모든 검증 한 번에 실행
./prd/run-all-validations.sh
```

### 4. CI/CD 통합
```yaml
# .github/workflows/validation.yml
name: Code Convention Validation

on: [push, pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run validations
        run: |
          chmod +x prd/*.sh
          ./prd/run-all-validations.sh
```

---

## 📊 검증 결과 리포트 예시

```
====================================
📋 FileFlow 파일 업로드 시스템 검증
====================================

[1/4] Zero-Tolerance 규칙 검증
✅ Lombok 미사용 - PASS
✅ Getter 체이닝 없음 - PASS
✅ JPA 관계 어노테이션 미사용 - PASS
✅ Transaction 경계 준수 - PASS
✅ Private/Final 메서드에 @Transactional 없음 - PASS

[2/4] Domain Layer 검증
✅ Domain Layer 검증 통과!

[3/4] Application Layer 검증
⚠️  경고:
  - CreateMultipartUploadUseCase.java: UseCase에 multiple public 메서드

[4/4] ArchUnit 아키텍처 테스트
✅ 모든 아키텍처 규칙 통과

====================================
✅ 모든 검증 통과! 배포 가능합니다.
```

이 검증 스크립트들을 사용하여 개발 과정에서 지속적으로 코딩 컨벤션을 체크할 수 있습니다.