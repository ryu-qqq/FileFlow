# /implementer Skill

코드 구현 스킬. MCP + Serena Lazy Caching 기반.

## 사용법

```bash
/implementer "Order Aggregate 생성"
/implementer --layer domain --type aggregate
```

## 실행 흐름

1. **캐시 확인**: `serena.read_memory("convention-{layer}-{type}")`
2. **캐시 미스**: `module_context()` 호출 → Serena 저장
3. **코드 생성**: 템플릿 + 규칙 기반
4. **검증**: `validation_context()` 호출

## Lazy Caching 로직

```python
cache_key = f"convention-{layer}-{class_type}"

# 1. Serena 캐시 확인
cached = serena.read_memory(cache_key)

if cached:
    # 캐시 히트 → API 호출 스킵
    rules = cached
else:
    # 캐시 미스 → MCP 호출
    rules = module_context(module_id, class_type)
    # Serena에 저장
    serena.write_memory(cache_key, rules)

# 규칙 기반 코드 생성
generate_code(rules)
```
