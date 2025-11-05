# .claude/ - Claude Code 설정

Claude Code의 설정 파일 및 자동화 시스템

---

## 📁 디렉토리 구조

```
.claude/
├── CLAUDE.md                   # 중앙 설정 파일 (프로젝트 컨텍스트)
├── settings.local.json         # Claude Code 로컬 설정
├── install-template.sh         # 템플릿 설치 스크립트
│
├── cache/                      # 규칙 Cache 시스템
│   └── rules/
│       ├── index.json          # 규칙 인덱스 (98개)
│       └── *.json              # 개별 규칙 JSON
│
├── hooks/                      # Dynamic Hooks
│   ├── user-prompt-submit.sh  # 규칙 자동 주입
│   ├── after-tool-use.sh      # 실시간 검증
│   ├── logs/
│   │   └── hook-execution.jsonl  # 구조화된 로그
│   └── scripts/
│       ├── build-rule-cache.py
│       ├── validation-helper.py
│       ├── setup-serena-conventions.sh
│       └── log-to-langfuse.py
│
├── commands/                   # Slash Commands
│   ├── cc/load.md             # /cc:load
│   ├── code-gen-*.md          # 코드 생성
│   ├── validate-*.md          # 검증
│   ├── ai-review.md           # AI 리뷰
│   └── lib/inject-rules.py
│
└── agents/                     # 전문 에이전트
```

---

## 🎯 핵심 시스템

### 1. Dynamic Hooks
- **자동 규칙 주입**: 키워드 감지 → Layer 매핑 → 규칙 주입
- **실시간 검증**: 코드 생성 후 즉시 검증
- **Hook 로그**: `.claude/hooks/logs/hook-execution.jsonl`

### 2. Cache 시스템
- **98개 규칙**: `docs/coding_convention/` → JSON
- **O(1) 검색**: `index.json` 인덱스
- **90% 토큰 절감**: 50,000 → 500-1,000 토큰

### 3. Serena Memory
- **세션 컨텍스트 유지**: 코딩 컨벤션 메모리
- **78% 위반 감소**: 23회 → 5회
- **명령어**: `/cc:load` (세션 시작 시 실행)

### 4. Slash Commands
- **코드 생성**: `/code-gen-domain`, `/code-gen-usecase`, `/code-gen-orchestrator`
- **검증**: `/validate-domain`, `/validate-architecture`
- **AI 리뷰**: `/ai-review` (Gemini + CodeRabbit + Codex)

---

## 🚀 빠른 시작

### 1. Cache 빌드 (최초 1회)
```bash
python3 .claude/hooks/scripts/build-rule-cache.py
```

### 2. Serena 메모리 초기화 (최초 1회)
```bash
bash .claude/hooks/scripts/setup-serena-conventions.sh
```

### 3. 세션 시작 시
```bash
/cc:load  # 코딩 컨벤션 로드
```

### 4. 코드 생성
```bash
/code-gen-domain Order
/code-gen-usecase PlaceOrder
/code-gen-orchestrator Order PlacementConfirmed
```

---

## 📖 상세 문서

- **중앙 설정**: [CLAUDE.md](./CLAUDE.md)
- **Cache 시스템**: [cache/rules/README.md](./cache/rules/README.md)
- **Hook 시스템**: [hooks/README.md](./hooks/README.md)
- **Commands**: [commands/README.md](./commands/README.md)
- **Hook 로깅**: [hooks/HOOK_LOGGING_GUIDE.md](./hooks/HOOK_LOGGING_GUIDE.md)

---

## 🔧 주요 파일

| 파일 | 역할 |
|------|------|
| `CLAUDE.md` | 중앙 설정 파일 (프로젝트 컨텍스트) |
| `cache/rules/index.json` | 규칙 인덱스 (98개) |
| `hooks/user-prompt-submit.sh` | 자동 규칙 주입 |
| `hooks/after-tool-use.sh` | 실시간 검증 |
| `hooks/logs/hook-execution.jsonl` | 구조화된 로그 |
| `commands/lib/inject-rules.py` | 규칙 주입 엔진 |
| `commands/cc/load.md` | /cc:load 명령어 |

---

## 📊 성능 메트릭

| 메트릭 | 개선율 |
|--------|--------|
| 토큰 사용량 | 90% 절감 |
| 검증 속도 | 73.6% 향상 |
| 컨벤션 위반 | 78% 감소 |
| 세션 시간 | 47% 단축 |
