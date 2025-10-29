# Dynamic Hooks + Cache System

## Innovation Overview
This project features an **AI-driven automatic rule injection and real-time validation system** that provides:
- **90% token reduction** (from 50K to 500-1K tokens)
- **73.6% faster validation** (from 561ms to 148ms)
- **O(1) rule lookup** via JSON cache

## System Architecture
```
docs/coding_convention/ (90 markdown rules)
         ↓
build-rule-cache.py (Cache builder)
         ↓
.claude/cache/rules/ (90 JSON files + index.json)
         ↓
user-prompt-submit.sh (Keyword detection → Layer mapping)
         ↓
inject-rules.py (Layer-specific rule injection)
         ↓
Claude Code (Rule-compliant code generation)
         ↓
after-tool-use.sh (Post-generation validation)
         ↓
validation-helper.py (Cache-based real-time validation)
```

## Cache System

### Cache Location
- **Rules Cache**: `.claude/cache/rules/`
- **Index File**: `.claude/cache/rules/index.json`
- **Rule Files**: 96 JSON files (one per coding rule)

### Cache Structure
Each JSON file contains:
```json
{
  "id": "domain-layer-law-of-demeter-01_getter-chaining-prohibition",
  "layer": "domain-layer",
  "category": "law-of-demeter",
  "title": "Law of Demeter — Getter 체이닝 금지",
  "content": "...markdown content...",
  "keywords": ["getter", "chaining", "law", "demeter", "encapsulation"]
}
```

### Building Cache
```bash
# Rebuild cache from markdown files (takes ~5 seconds)
python3 .claude/hooks/scripts/build-rule-cache.py

# Verify cache
cat .claude/cache/rules/index.json
ls -l .claude/cache/rules/*.json | wc -l  # Should show 96+
```

## Coding Convention Layers

### Layer Structure (90+ rules across 8 categories)

1. **adapter-rest-api-layer** (18 rules)
   - controller-design, dto-patterns, exception-handling, mapper-patterns, testing

2. **domain-layer** (15 rules)
   - aggregate-design, law-of-demeter, package-guide, testing

3. **application-layer** (18 rules)
   - assembler-pattern, dto-patterns, transaction-management, usecase-design, testing

4. **persistence-layer** (10 rules)
   - jpa-entity-design, querydsl-optimization, repository-patterns, testing

5. **testing** (12 rules)
   - archunit-rules, integration-testing

6. **java21-patterns** (8 rules)
   - record-patterns, sealed-classes, virtual-threads

7. **enterprise-patterns** (5 rules)
   - caching, event-driven, resilience

8. **error-handling** (5 rules)
   - error-handling-strategy, domain-exception-design, global-exception-handler

## Automatic Rule Injection

### How It Works
1. User prompt is analyzed for keywords: "domain", "usecase", "controller", "entity", etc.
2. Keywords map to layers: domain → "domain-layer", controller → "adapter-rest-api-layer"
3. `inject-rules.py` loads relevant JSON rules from cache
4. Rules are injected into Claude's context (500-1K tokens vs 50K for full docs)

### Layer Mapping
- **domain**, **aggregate**, **entity**, **value object** → domain-layer
- **usecase**, **command**, **query**, **application service** → application-layer
- **controller**, **rest**, **api** → adapter-rest-api-layer
- **repository**, **jpa**, **entity** → persistence-layer

## Real-time Validation

### After Tool Use Hook
Runs automatically after Write/Edit operations:
```bash
# Location: .claude/hooks/after-tool-use.sh
# Triggers: Write, Edit, MultiEdit tool usage
# Action: Calls validation-helper.py with cache
```

### Validation Helper
```bash
# Manual validation
python3 .claude/hooks/scripts/validation-helper.py \
  --file domain/src/main/java/.../Order.java \
  --layer domain

# Cache-based validation checks:
# - Lombok usage
# - Getter chaining (Law of Demeter)
# - JPA relationship annotations
# - Transaction boundary violations
# - Javadoc presence
```

## Slash Commands Integration

### Code Generation Commands (with automatic rule injection)
- `/code-gen-domain <name>` - Injects domain-layer rules
- `/code-gen-usecase <name>` - Injects application-layer rules  
- `/code-gen-controller <name>` - Injects adapter-rest-api-layer rules

### Validation Commands (cache-powered)
- `/validate-domain <file>` - Validates against domain-layer cache
- `/validate-architecture [dir]` - Validates entire architecture

## Performance Metrics

| Metric | Before Cache | With Cache | Improvement |
|--------|--------------|------------|-------------|
| Token Usage | 50,000 | 500-1,000 | **90% reduction** |
| Validation Speed | 561ms | 148ms | **73.6% faster** |
| Doc Loading | 2-3 sec | <100ms | **95% faster** |
| Rule Lookup | O(n) scan | O(1) hash | **Constant time** |

## When to Rebuild Cache
Rebuild cache when:
1. Coding convention markdown files are modified
2. New rules are added to docs/coding_convention/
3. Rule metadata or structure changes
4. After pulling updates from git (if conventions changed)

```bash
# Quick rebuild
python3 .claude/hooks/scripts/build-rule-cache.py

# Verify rebuild succeeded
python3 .claude/hooks/scripts/validation-helper.py --verify-cache
```
