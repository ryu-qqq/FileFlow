---
name: grafana
description: Grafana 대시보드/데이터소스 관리 자동화. API 키 자동 생성/삭제, 대시보드 CRUD, 메트릭 쿼리.
context: fork
allowed-tools: Bash, Read, Write, Edit, Glob
---

# /grafana

Amazon Managed Grafana 워크스페이스를 API로 직접 관리합니다.
대시보드 import/export, 데이터소스 설정, 메트릭 쿼리를 자동화합니다.

## 사용법

```bash
# 대시보드
/grafana list                          # 전체 대시보드 목록
/grafana push <json-path>              # 로컬 JSON → Grafana 업로드
/grafana pull <uid> <output-path>      # Grafana → 로컬 다운로드
/grafana sync <json-path>              # 필수 필드 보정 + push
/grafana delete <uid>                  # 대시보드 삭제 (확인 필요)

# 데이터소스
/grafana ds list                       # 데이터소스 목록
/grafana ds namespace add <ns>         # CloudWatch 커스텀 네임스페이스 추가
/grafana ds namespace list             # 등록된 커스텀 네임스페이스 조회

# 메트릭 쿼리
/grafana query <ns> <metric> [dim=val] # CloudWatch 메트릭 쿼리

# 유틸리티
/grafana status                        # 워크스페이스 상태 확인
/grafana fix <json-path>               # 로컬 JSON 필수 필드 보정 (push 없이)
```

## 인수: $ARGUMENTS

## Grafana 워크스페이스 정보

```
Workspace ID: g-13d0f6f6be
Endpoint: https://g-13d0f6f6be.grafana-workspace.ap-northeast-2.amazonaws.com
Region: ap-northeast-2
Grafana Version: 10.4
Auth: AWS SSO (API Key for automation)
```

## 데이터소스 정보

```
CloudWatch:
  Name: CloudWatch-prod
  UID: df4r4id6rd8n4c
  Type: cloudwatch
  Default Region: ap-northeast-2
  Auth: ec2_iam_role
```

## API 키 라이프사이클

**모든 API 호출은 이 패턴을 따라야 합니다:**

```bash
# 1. 임시 API 키 생성 (TTL 5분)
KEY_NAME="claude-grafana-$(date +%s)"
API_KEY=$(aws grafana create-workspace-api-key \
  --workspace-id "g-13d0f6f6be" \
  --key-name "$KEY_NAME" \
  --key-role ADMIN \
  --seconds-to-live 300 \
  --region ap-northeast-2 \
  --query 'key' --output text 2>/dev/null)
GRAFANA_URL="https://g-13d0f6f6be.grafana-workspace.ap-northeast-2.amazonaws.com"

# 2. API 호출 수행
# ... 작업 ...

# 3. 반드시 키 삭제 (오류 시에도)
aws grafana delete-workspace-api-key \
  --workspace-id "g-13d0f6f6be" \
  --key-name "$KEY_NAME" \
  --region ap-northeast-2 2>/dev/null
```

## Grafana 10.4 CloudWatch 필수 필드

대시보드 JSON의 CloudWatch target에 다음 필드가 **반드시** 있어야 데이터가 표시됩니다:

```json
{
  "queryMode": "Metrics",
  "metricQueryType": 0,
  "metricEditorMode": 0,
  "id": "",
  "expression": "",
  "sqlExpression": "",
  "region": "ap-northeast-2",
  "refId": "A"
}
```

`sync` 또는 `fix` 명령 시 아래 Python 로직으로 자동 보정:

```python
def fix_cloudwatch_targets(dashboard):
    panels = dashboard.get('panels', [])
    for panel in panels:
        targets = panel.get('targets', [])
        letter = ord('A')
        for target in targets:
            ds = target.get('datasource', {})
            if ds.get('type') == 'cloudwatch':
                target.setdefault('queryMode', 'Metrics')
                target.setdefault('metricQueryType', 0)
                target.setdefault('metricEditorMode', 0)
                target.setdefault('id', '')
                target.setdefault('expression', '')
                target.setdefault('sqlExpression', '')
                if target.get('region', 'default') == 'default':
                    target['region'] = 'ap-northeast-2'
                target['refId'] = chr(letter)
                letter += 1
    return dashboard
```

## 명령별 실행 방법

### `list` - 대시보드 목록

```bash
curl -s -H "Authorization: Bearer $API_KEY" \
  "$GRAFANA_URL/api/search?type=dash-db" | python3 -c "
import sys, json
for d in json.load(sys.stdin):
    print(f'  {d[\"uid\"]:40s} {d[\"title\"]}')
"
```

### `push <json-path>` - 대시보드 업로드

```python
import json

with open('JSON_PATH') as f:
    dashboard = json.load(f)

fix_cloudwatch_targets(dashboard)
dashboard.pop('id', None)

payload = {"dashboard": dashboard, "overwrite": True}
with open('/tmp/grafana-push.json', 'w') as f:
    json.dump(payload, f)
```

```bash
curl -s -X POST "$GRAFANA_URL/api/dashboards/db" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d @/tmp/grafana-push.json
rm -f /tmp/grafana-push.json
```

### `pull <uid> <output-path>` - 대시보드 다운로드

```bash
curl -s -H "Authorization: Bearer $API_KEY" \
  "$GRAFANA_URL/api/dashboards/uid/DASHBOARD_UID" > /tmp/grafana-pull.json
```

```python
import json

with open('/tmp/grafana-pull.json') as f:
    r = json.load(f)
dash = r['dashboard']
dash.pop('id', None)
dash['version'] = 1
with open('OUTPUT_PATH', 'w') as f:
    json.dump(dash, f, indent=2)
    f.write('\n')
```

### `sync <json-path>` - 보정 + push

1. 로컬 JSON 읽기
2. `fix_cloudwatch_targets()` 적용
3. Grafana에 push
4. 로컬 JSON도 보정된 버전으로 저장

### `query <namespace> <metric> [dimension=value]` - 메트릭 쿼리

```bash
NOW_MS=$(python3 -c "import time; print(int(time.time()*1000))")
FROM_MS=$(python3 -c "import time; print(int((time.time()-3600)*1000))")

curl -s -X POST "$GRAFANA_URL/api/ds/query" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "queries": [{
      "datasource": {"type": "cloudwatch", "uid": "df4r4id6rd8n4c"},
      "type": "timeSeriesQuery",
      "namespace": "NAMESPACE",
      "metricName": "METRIC",
      "dimensions": {"DIM_KEY": ["DIM_VALUE"]},
      "matchExact": true,
      "statistic": "Average",
      "period": "300",
      "region": "ap-northeast-2",
      "refId": "A"
    }],
    "from": "FROM_MS",
    "to": "NOW_MS"
  }'
```

### `ds namespace add <namespace>` - 커스텀 네임스페이스 추가

1. 현재 데이터소스 조회: `GET /api/datasources/uid/df4r4id6rd8n4c`
2. `jsonData.customMetricsNamespaces`에 쉼표로 추가
3. `PUT /api/datasources/{id}`로 업데이트

### `ds namespace list` - 네임스페이스 조회

데이터소스 조회 후 `jsonData.customMetricsNamespaces` 출력

### `fix <json-path>` - 로컬 JSON만 보정

`fix_cloudwatch_targets()` 적용 후 로컬 파일만 덮어쓰기 (Grafana push 없음)

### `status` - 워크스페이스 상태

```bash
aws grafana describe-workspace --workspace-id "g-13d0f6f6be" --region ap-northeast-2
```

데이터소스 목록, 대시보드 수, API 키 목록 등을 출력

## 대시보드 JSON 파일 위치 규칙

```
tools/<service>/grafana/<dashboard-name>.json
docs/grafana/<dashboard-name>.json
```

## 안전 규칙

- API 키는 항상 5분 TTL로 생성하고 작업 후 **즉시 삭제**
- 대시보드 `delete` 시 반드시 사용자 확인
- `push`/`sync` 시 `overwrite: true` 사용
- `/tmp/grafana-*.json` 임시 파일은 작업 후 삭제
