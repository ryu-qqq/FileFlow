# GitHub 이슈 → Jira 자동 동기화 설정 가이드

## 개요

GitHub에서 이슈가 생성되면 자동으로 Jira에 Task가 생성되는 워크플로우입니다.

## 동작 방식

- **GitHub 이슈 생성** → Jira Task 자동 생성
- **GitHub 이슈 수정** → Jira Task 업데이트
- **GitHub 이슈 닫기** → Jira Task 완료 처리
- **GitHub 이슈 재오픈** → Jira Task 재오픈
- **GitHub 코멘트 작성** → Jira Task에 코멘트 추가

## 사전 준비

### 1. Jira API Token 발급

1. Jira 계정으로 로그인
2. https://id.atlassian.com/manage-profile/security/api-tokens 접속
3. **Create API token** 클릭
4. 토큰 이름 입력 (예: `GitHub Integration`)
5. 생성된 토큰 복사 (한 번만 표시됨)

### 2. Jira 프로젝트 정보 확인

- **Base URL**: `https://your-domain.atlassian.net`
- **Project Key**: Jira 프로젝트의 키 (예: `FFL`, `PROJ`)
- **User Email**: Jira 계정 이메일

## GitHub Secrets 설정

GitHub 저장소 설정에서 다음 Secrets를 추가해야 합니다:

### 설정 경로
`Settings` → `Secrets and variables` → `Actions` → `New repository secret`

### 필수 Secrets

| Secret 이름 | 설명 | 예시 |
|------------|------|------|
| `JIRA_BASE_URL` | Jira 인스턴스 URL | `https://your-domain.atlassian.net` |
| `JIRA_USER_EMAIL` | Jira 계정 이메일 | `your-email@company.com` |
| `JIRA_API_TOKEN` | Jira API 토큰 | `ATATT3xFf...` |
| `JIRA_PROJECT_KEY` | Jira 프로젝트 키 | `FFL` 또는 `PROJ` |

## 빠른 시작 (5분 설정)

### 1단계: GitHub Secrets 설정

GitHub CLI 사용:
```bash
gh secret set JIRA_BASE_URL -b "https://your-domain.atlassian.net"
gh secret set JIRA_USER_EMAIL -b "your-email@company.com"
gh secret set JIRA_API_TOKEN -b "YOUR_API_TOKEN"
gh secret set JIRA_PROJECT_KEY -b "FFL"
```

또는 웹 UI:
1. 저장소 → `Settings` → `Secrets and variables` → `Actions`
2. `New repository secret` 클릭하여 4개 Secret 추가

### 2단계: 테스트

1. GitHub에서 테스트 이슈 생성
2. `Actions` 탭에서 워크플로우 실행 확인
3. Jira에서 Task가 생성되었는지 확인
4. 이슈에 Jira 링크 코멘트가 자동으로 추가되었는지 확인

## 워크플로우 커스터마이징

### Issue Type 변경

기본값은 `Task`이지만, 다른 타입으로 변경 가능:

```yaml
issuetype: Story  # Task, Story, Bug, Epic 등
```

### Jira 필드 추가

더 많은 Jira 필드를 설정하려면 `fields` 섹션을 수정:

```yaml
fields: |
  {
    "labels": ${{ toJson(github.event.issue.labels.*.name) }},
    "priority": {"name": "Medium"},
    "components": [{"name": "Backend"}]
  }
```

### 특정 라벨만 동기화

특정 라벨이 있는 이슈만 Jira로 동기화:

```yaml
- name: Check if issue has sync label
  if: contains(github.event.issue.labels.*.name, 'sync-to-jira')
  run: echo "This issue should be synced"
```

## 트러블슈팅

### 1. 인증 실패
- Jira API Token이 올바른지 확인
- User Email이 Jira 계정과 일치하는지 확인
- Base URL에 `https://`가 포함되어 있는지 확인

### 2. 프로젝트를 찾을 수 없음
- Project Key가 정확한지 확인 (대소문자 구분)
- 해당 프로젝트에 대한 권한이 있는지 확인

### 3. Issue Type 오류
- Jira 프로젝트에서 해당 Issue Type이 지원되는지 확인
- 프로젝트 설정에서 사용 가능한 Issue Type 확인

## 추가 기능

### 양방향 동기화

Jira에서 변경사항이 있을 때 GitHub로 동기화하려면 Jira Webhook 설정이 필요합니다.

### 자동 라벨 매핑

GitHub 라벨을 Jira 라벨로 자동 매핑:

```yaml
- name: Map GitHub labels to Jira
  run: |
    LABELS=$(echo '${{ toJson(github.event.issue.labels.*.name) }}' | jq -r 'map(select(. != ""))' )
    echo "JIRA_LABELS=$LABELS" >> $GITHUB_ENV
```

## 참고 자료

- [Atlassian Jira GitHub Actions](https://github.com/marketplace?type=actions&query=atlassian+jira)
- [Jira REST API Documentation](https://developer.atlassian.com/cloud/jira/platform/rest/v3/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

## 지원

문제가 발생하면 GitHub Issues에 보고해주세요.
