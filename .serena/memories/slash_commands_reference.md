# FileFlow Slash Commands Reference

## Code Generation Commands

### Domain Layer
```bash
/code-gen-domain <AggregateName>
```
**Purpose**: Generate Domain Aggregate with automatic rule injection  
**Auto-injects**: domain-layer rules (Law of Demeter, No Lombok, etc.)  
**Generates**:
- Aggregate root class
- Value objects
- Domain events
- Unit tests

**Example**:
```bash
/code-gen-domain Order
# Creates: Order.java, OrderId.java, OrderCreated.java, OrderTest.java
```

### Application Layer
```bash
/code-gen-usecase <UseCaseName>
```
**Purpose**: Generate Application Use Case with transaction boundaries  
**Auto-injects**: application-layer rules (Transaction management, DTO patterns)  
**Generates**:
- UseCase interface (port)
- UseCase implementation
- Command/Query DTOs
- Unit tests

**Example**:
```bash
/code-gen-usecase CreateOrder
# Creates: CreateOrderUseCase.java, CreateOrderCommand.java, tests
```

### Adapter Layer (REST)
```bash
/code-gen-controller <ResourceName>
```
**Purpose**: Generate REST Controller with proper API design  
**Auto-injects**: adapter-rest-api-layer rules (RESTful design, exception handling)  
**Generates**:
- Controller class
- Request/Response DTOs
- Mapper classes
- Integration tests

**Example**:
```bash
/code-gen-controller Order
# Creates: OrderController.java, OrderRequest.java, OrderResponse.java
```

## Validation Commands

### Domain Validation
```bash
/validate-domain <file-path>
```
**Purpose**: Validate domain layer file against coding standards  
**Checks**:
- No Lombok usage
- Law of Demeter compliance (no getter chaining)
- No JPA relationship annotations
- Javadoc presence
- Pure domain (no framework dependencies)

**Example**:
```bash
/validate-domain domain/src/main/java/com/ryuqq/fileflow/domain/Order.java
```

### Architecture Validation
```bash
/validate-architecture [directory]
```
**Purpose**: Validate architecture rules across modules  
**Checks**:
- Layer dependency rules (Domain ← Application ← Adapter)
- Package naming conventions
- No cyclic dependencies
- ArchUnit test results

**Examples**:
```bash
# Validate entire project
/validate-architecture

# Validate specific module
/validate-architecture domain
/validate-architecture application
```

## Layer-Specific Commands

### REST API Layer
```bash
/rest
```
**Purpose**: Activate REST API layer development context  
**Context**: REST controller design, DTO patterns, exception handling  
**Use when**: Working on REST endpoints, API design

### Domain Layer
```bash
/domain
```
**Purpose**: Activate Domain layer development context  
**Context**: DDD patterns, aggregate design, Law of Demeter  
**Use when**: Implementing business logic, domain models

### Application Layer
```bash
/application
```
**Purpose**: Activate Application layer development context  
**Context**: Use case patterns, transaction management, DTO handling  
**Use when**: Implementing use cases, orchestration logic

### Persistence Layer
```bash
/persistence
```
**Purpose**: Activate Persistence layer development context  
**Context**: JPA entities (Long FK only), QueryDSL, repository patterns  
**Use when**: Database access, query optimization

### Testing
```bash
/test
```
**Purpose**: Activate testing context  
**Context**: Unit tests, integration tests, ArchUnit, test fixtures  
**Use when**: Writing tests, test-driven development

## Analysis Commands

### Gemini Code Review
```bash
/gemini-review [pr-number]
```
**Purpose**: Analyze Gemini Code Assist PR review comments  
**Generates**: TodoList from Gemini's feedback  
**Options**:
- `--analyze-only`: Parse without creating todos
- `--preview`: Show analysis without execution
- `--priority high|medium|low`: Filter by priority

**Example**:
```bash
/gemini-review 123
/gemini-review --analyze-only --preview
```

### Jira Task Analysis
```bash
/jira-task
```
**Purpose**: Analyze Jira task and create development plan  
**Generates**:
- TodoList from task requirements
- Feature branch suggestion
- Implementation checklist

## AI Review Commands

### Unified AI Review
```bash
/ai-review [pr-number] [options]
```
**Purpose**: Integrate multiple AI bot reviews into single TodoList  
**Options**:
- `--bots gemini,coderabbit,codex`: Select specific bots
- `--strategy merge|vote|sequential`: Review integration strategy
- `--analyze-only`: Parse without execution
- `--preview`: Preview analysis

**Example**:
```bash
/ai-review 123 --bots gemini,coderabbit
/ai-review --strategy vote --preview
```

## Command Usage Patterns

### Starting New Feature
```bash
# 1. Generate domain model
/code-gen-domain Product

# 2. Generate use case
/code-gen-usecase CreateProduct

# 3. Generate REST controller
/code-gen-controller Product

# 4. Validate everything
/validate-architecture
```

### Code Review Workflow
```bash
# 1. Get AI reviews
/gemini-review 123

# 2. Implement fixes
# ... make changes ...

# 3. Validate domain changes
/validate-domain domain/src/main/java/.../Product.java

# 4. Full validation
/validate-architecture
```

### Layer-Specific Development
```bash
# Working on domain logic
/domain
# ... Claude activates domain-layer context ...

# Working on REST API
/rest  
# ... Claude activates REST API context ...
```

## Command Workflow Best Practices

1. **Always validate after generation**: Use `/validate-architecture` after code generation
2. **Layer context helps**: Activate layer commands (`/domain`, `/rest`, etc.) for context-aware assistance
3. **Incremental validation**: Use `/validate-domain` for quick checks during development
4. **Review integration**: Use `/gemini-review` or `/ai-review` to process PR feedback systematically

## Getting Help

For detailed command documentation:
```bash
# Check commands directory
ls .claude/commands/

# Read specific command docs
cat .claude/commands/code-gen-domain.md
cat .claude/commands/validate-architecture.md
```
