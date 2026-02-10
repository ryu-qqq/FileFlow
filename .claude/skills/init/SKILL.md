---
name: init
description: .claude/ 디렉토리 초기화. Spring Standards MCP 템플릿 기반 설정 파일 생성.
context: fork
agent: project-initializer
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# /init

`.claude/` 디렉토리를 Spring Standards MCP 템플릿 기반으로 초기화합니다.

## 사용법

```bash
/init                              # 기본 초기화 (tech_stack=1, arch=1)
/init --tech-stack 2               # 다른 tech stack 사용
/init --architecture 3             # 다른 architecture 사용
/init --no-backup                  # 기존 .claude/ 백업 안함
/init --dry-run                    # 미리보기만 (파일 생성 안함)
/init --config-only                # CLAUDE.md + settings만 생성
```

## 수행 작업

1. 기존 `.claude/` 백업 (cp -r)
2. `list_tech_stacks()` → tech stack, architecture, layers 조회
3. `get_config_files()` → config 템플릿 조회
4. 변수 치환 → 파일 생성
5. 검증
