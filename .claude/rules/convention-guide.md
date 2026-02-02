# Convention Guide

> ⚠️ **규칙은 하드코딩되지 않습니다. MCP를 통해 동적으로 조회하세요.**

## 규칙 조회 방법

### 1. 레이어 목록 먼저 조회

```python
list_tech_stacks()
# → layers: ["DOMAIN", "APPLICATION", "ADAPTER_OUT", "ADAPTER_IN", "BOOTSTRAP"]
```

### 2. 전체 규칙 개요

```python
validation_context(layers=[...])  # 조회된 레이어 사용
```

### 3. 레이어별 상세 규칙

```python
get_layer_detail(layer_code="DOMAIN")
```

### 4. 특정 규칙 상세

```python
get_rule(rule_code="DOM-AGG-001")
```

### 5. 클래스별 템플릿 + 규칙

```python
module_context(module_id=1, class_type="AGGREGATE")
```

## Serena 캐싱

조회된 규칙은 Serena Memory에 캐싱하여 재사용:

```python
# 캐시 키: convention-{layer}-{class_type}
serena.write_memory("convention-domain-aggregate", rules)
serena.read_memory("convention-domain-aggregate")
```

## Zero-Tolerance 빠른 참조

> 상세 규칙은 `validation_context()`로 조회

| Layer | 핵심 규칙 |
|-------|----------|
| DOMAIN | Lombok 금지, Getter 체이닝 금지 |
| APPLICATION | @Transactional 내 외부 API 금지 |
| ADAPTER_OUT | JPA 관계 어노테이션 금지 |
| ADAPTER_IN | MockMvc 금지 |
