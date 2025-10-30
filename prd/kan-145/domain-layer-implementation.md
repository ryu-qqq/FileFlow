# Domain Layer 구현 가이드 (KAN-217, KAN-218)

## 📋 개요

Pipeline 처리 엔진의 핵심인 Domain Layer를 구현합니다. 이 문서는 주니어 개발자도 쉽게 따라할 수 있도록 상세한 코드 예제와 설명을 포함합니다.

---

## 🎯 구현 목표

1. **PipelineDefinition Aggregate**: 파이프라인 실행 정의 및 상태 관리
2. **ProcessingPolicy Aggregate**: 테넌트별 파일 처리 정책 관리
3. **Value Objects**: 불변 값 객체들로 도메인 개념 표현
4. **Domain Events**: 파이프라인 생명주기 이벤트 발행

---

## 📁 패키지 구조

```
domain/src/main/java/com/ryuqq/fileflow/domain/pipeline/
├── aggregate/
│   ├── PipelineDefinition.java      # Pipeline Aggregate Root
│   └── ProcessingPolicy.java        # Policy Aggregate Root
├── vo/
│   ├── PipelineId.java             # Pipeline 식별자
│   ├── ProcessingPolicyId.java     # Policy 식별자
│   ├── PipelineStep.java           # Pipeline 실행 단계
│   ├── PipelineResult.java         # Pipeline 실행 결과
│   ├── ImageProcessingSettings.java # 이미지 처리 설정
│   ├── HtmlProcessingSettings.java  # HTML 처리 설정
│   ├── PdfProcessingSettings.java   # PDF 처리 설정
│   └── ExcelProcessingSettings.java # Excel 처리 설정
├── enums/
│   ├── PipelineType.java           # 파이프라인 타입
│   ├── PipelineStatus.java         # 파이프라인 상태
│   ├── Priority.java               # 우선순위
│   ├── StepType.java               # 단계 타입
│   └── FileType.java               # 파일 타입
├── event/
│   ├── PipelineStartedEvent.java
│   ├── PipelineCompletedEvent.java
│   ├── PipelineFailedEvent.java
│   ├── ProcessingPolicyCreatedEvent.java
│   └── ProcessingPolicyEnabledEvent.java
├── exception/
│   ├── InvalidPipelineStateException.java
│   ├── PolicyNotFoundException.java
│   └── PipelineExecutionException.java
└── repository/
    ├── PipelineRepository.java      # Repository Port
    └── ProcessingPolicyRepository.java
```

---

## 🔨 Step 1: Enum 정의부터 시작

### 1.1 PipelineType.java

```java
package com.ryuqq.fileflow.domain.pipeline.enums;

/**
 * 파이프라인 처리 타입
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum PipelineType {
    IMAGE("이미지 처리 파이프라인"),
    HTML("HTML 처리 파이프라인"),
    PDF("PDF 처리 파이프라인"),
    EXCEL("Excel 처리 파이프라인");

    private final String description;

    PipelineType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 파일 확장자로부터 파이프라인 타입 결정
     */
    public static PipelineType fromFileExtension(String extension) {
        String ext = extension.toLowerCase();

        if (ext.matches("jpg|jpeg|png|gif|bmp|webp")) {
            return IMAGE;
        } else if (ext.matches("html|htm")) {
            return HTML;
        } else if (ext.equals("pdf")) {
            return PDF;
        } else if (ext.matches("xls|xlsx")) {
            return EXCEL;
        }

        throw new IllegalArgumentException(
            "Unsupported file extension: " + extension
        );
    }
}
```

### 1.2 PipelineStatus.java

```java
package com.ryuqq.fileflow.domain.pipeline.enums;

/**
 * 파이프라인 실행 상태
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum PipelineStatus {
    PENDING("대기 중"),
    RUNNING("실행 중"),
    PAUSED("일시 중지"),
    COMPLETED("완료"),
    FAILED("실패"),
    CANCELLED("취소됨");

    private final String description;

    PipelineStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 현재 상태에서 전환 가능한 상태인지 확인
     */
    public boolean canTransitionTo(PipelineStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == RUNNING || newStatus == CANCELLED;
            case RUNNING:
                return newStatus == PAUSED || newStatus == COMPLETED
                    || newStatus == FAILED || newStatus == CANCELLED;
            case PAUSED:
                return newStatus == RUNNING || newStatus == CANCELLED;
            case COMPLETED:
            case FAILED:
            case CANCELLED:
                return false;  // 최종 상태는 변경 불가
            default:
                return false;
        }
    }
}
```

### 1.3 Priority.java

```java
package com.ryuqq.fileflow.domain.pipeline.enums;

/**
 * 파이프라인 처리 우선순위
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum Priority {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}
```

---

## 🔨 Step 2: Value Objects 구현

### 2.1 PipelineId.java (식별자 VO)

```java
package com.ryuqq.fileflow.domain.pipeline.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Pipeline 식별자 Value Object
 * 불변 객체로 구현
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class PipelineId {
    private final String value;

    // Private 생성자
    private PipelineId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("PipelineId cannot be null or empty");
        }
        this.value = value;
    }

    /**
     * 새로운 PipelineId 생성
     */
    public static PipelineId generate() {
        return new PipelineId("PIP-" + UUID.randomUUID().toString());
    }

    /**
     * 기존 ID로부터 PipelineId 생성
     */
    public static PipelineId of(String value) {
        return new PipelineId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipelineId)) return false;
        PipelineId that = (PipelineId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
```

### 2.2 PipelineStep.java (실행 단계 VO)

```java
package com.ryuqq.fileflow.domain.pipeline.vo;

import java.util.Map;
import java.util.Objects;

/**
 * 파이프라인 실행 단계 Value Object
 * 각 단계는 순서대로 실행됨
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class PipelineStep {
    private final String stepId;
    private final String name;
    private final StepType type;
    private final int order;
    private final Map<String, Object> parameters;
    private final long estimatedDurationMs;

    private PipelineStep(
        String stepId,
        String name,
        StepType type,
        int order,
        Map<String, Object> parameters,
        long estimatedDurationMs
    ) {
        this.stepId = Objects.requireNonNull(stepId);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.order = order;
        this.parameters = Map.copyOf(parameters); // 불변 복사
        this.estimatedDurationMs = estimatedDurationMs;
    }

    /**
     * Static Factory Method - Builder 패턴
     */
    public static class Builder {
        private String stepId;
        private String name;
        private StepType type;
        private int order;
        private Map<String, Object> parameters = Map.of();
        private long estimatedDurationMs = 0;

        public Builder stepId(String stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(StepType type) {
            this.type = type;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder estimatedDurationMs(long estimatedDurationMs) {
            this.estimatedDurationMs = estimatedDurationMs;
            return this;
        }

        public PipelineStep build() {
            return new PipelineStep(
                stepId, name, type, order, parameters, estimatedDurationMs
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters only (no setters - immutable)
    public String getStepId() { return stepId; }
    public String getName() { return name; }
    public StepType getType() { return type; }
    public int getOrder() { return order; }
    public Map<String, Object> getParameters() {
        return Map.copyOf(parameters); // 방어적 복사
    }
    public long getEstimatedDurationMs() { return estimatedDurationMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipelineStep)) return false;
        PipelineStep that = (PipelineStep) o;
        return Objects.equals(stepId, that.stepId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepId);
    }
}
```

### 2.3 ImageProcessingSettings.java (이미지 처리 설정 VO)

```java
package com.ryuqq.fileflow.domain.pipeline.vo;

import java.util.List;
import java.util.Objects;

/**
 * 이미지 처리 설정 Value Object
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class ImageProcessingSettings {
    private final boolean convertToWebP;
    private final boolean generateThumbnails;
    private final List<ThumbnailSize> thumbnailSizes;
    private final int compressionQuality;  // 0-100
    private final Integer maxWidth;
    private final Integer maxHeight;
    private final boolean preserveExif;

    private ImageProcessingSettings(
        boolean convertToWebP,
        boolean generateThumbnails,
        List<ThumbnailSize> thumbnailSizes,
        int compressionQuality,
        Integer maxWidth,
        Integer maxHeight,
        boolean preserveExif
    ) {
        validateCompressionQuality(compressionQuality);

        this.convertToWebP = convertToWebP;
        this.generateThumbnails = generateThumbnails;
        this.thumbnailSizes = List.copyOf(thumbnailSizes);
        this.compressionQuality = compressionQuality;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.preserveExif = preserveExif;
    }

    private void validateCompressionQuality(int quality) {
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException(
                "Compression quality must be between 0 and 100"
            );
        }
    }

    /**
     * 기본 설정으로 생성
     */
    public static ImageProcessingSettings createDefault() {
        return new ImageProcessingSettings(
            true,  // WebP 변환
            true,  // 썸네일 생성
            List.of(
                ThumbnailSize.of(128, 128),
                ThumbnailSize.of(256, 256),
                ThumbnailSize.of(512, 512),
                ThumbnailSize.of(1024, 1024)
            ),
            85,    // 압축 품질
            2048,  // 최대 너비
            2048,  // 최대 높이
            false  // EXIF 제거
        );
    }

    /**
     * 커스텀 설정으로 생성
     */
    public static ImageProcessingSettings create(
        boolean convertToWebP,
        List<ThumbnailSize> thumbnailSizes,
        int compressionQuality
    ) {
        return new ImageProcessingSettings(
            convertToWebP,
            !thumbnailSizes.isEmpty(),
            thumbnailSizes,
            compressionQuality,
            null,
            null,
            false
        );
    }

    // Getters only
    public boolean isConvertToWebP() { return convertToWebP; }
    public boolean isGenerateThumbnails() { return generateThumbnails; }
    public List<ThumbnailSize> getThumbnailSizes() {
        return List.copyOf(thumbnailSizes);
    }
    public int getCompressionQuality() { return compressionQuality; }
    public Integer getMaxWidth() { return maxWidth; }
    public Integer getMaxHeight() { return maxHeight; }
    public boolean isPreserveExif() { return preserveExif; }

    /**
     * 썸네일 크기 Value Object (내부 클래스)
     */
    public static final class ThumbnailSize {
        private final int width;
        private final int height;

        private ThumbnailSize(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                    "Thumbnail size must be positive"
                );
            }
            this.width = width;
            this.height = height;
        }

        public static ThumbnailSize of(int width, int height) {
            return new ThumbnailSize(width, height);
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }

        @Override
        public String toString() {
            return width + "x" + height;
        }
    }
}
```

---

## 🔨 Step 3: Aggregate Root 구현

### 3.1 PipelineDefinition.java (핵심 Aggregate)

```java
package com.ryuqq.fileflow.domain.pipeline.aggregate;

import com.ryuqq.fileflow.domain.pipeline.enums.*;
import com.ryuqq.fileflow.domain.pipeline.event.*;
import com.ryuqq.fileflow.domain.pipeline.exception.*;
import com.ryuqq.fileflow.domain.pipeline.vo.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 파이프라인 정의 Aggregate Root
 *
 * 파이프라인의 생명주기를 관리하고 비즈니스 규칙을 강제합니다.
 * DDD의 Aggregate Root 패턴을 따릅니다.
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineDefinition {

    // 식별자
    private final PipelineId pipelineId;

    // 기본 속성
    private final String name;
    private final PipelineType pipelineType;
    private final Long fileAssetId;  // Long FK 전략 (JPA 관계 사용 안함)
    private final Long tenantId;     // 테넌트 구분

    // 실행 단계
    private final List<PipelineStep> steps;

    // 상태 관리
    private PipelineStatus status;
    private final Priority priority;

    // 재시도 관리
    private int currentRetryCount;
    private final int maxRetryCount;

    // 시간 추적
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 실행 결과
    private PipelineResult result;
    private String errorMessage;

    // 도메인 이벤트 저장
    private final transient List<Object> domainEvents = new ArrayList<>();

    /**
     * Private 생성자 - Static Factory Method 사용 강제
     */
    private PipelineDefinition(
        PipelineId pipelineId,
        String name,
        PipelineType pipelineType,
        Long fileAssetId,
        Long tenantId,
        List<PipelineStep> steps,
        Priority priority,
        int maxRetryCount
    ) {
        // 검증
        validateName(name);
        validateFileAssetId(fileAssetId);
        validateTenantId(tenantId);
        validateSteps(steps);
        validateMaxRetryCount(maxRetryCount);

        // 초기화
        this.pipelineId = Objects.requireNonNull(pipelineId);
        this.name = name;
        this.pipelineType = Objects.requireNonNull(pipelineType);
        this.fileAssetId = fileAssetId;
        this.tenantId = tenantId;
        this.steps = new ArrayList<>(steps);  // 방어적 복사
        this.priority = Objects.requireNonNull(priority);
        this.maxRetryCount = maxRetryCount;

        // 초기 상태
        this.status = PipelineStatus.PENDING;
        this.currentRetryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Static Factory Method - 새로운 파이프라인 생성
     */
    public static PipelineDefinition create(
        String name,
        PipelineType pipelineType,
        Long fileAssetId,
        Long tenantId,
        List<PipelineStep> steps,
        Priority priority
    ) {
        PipelineId newId = PipelineId.generate();

        PipelineDefinition pipeline = new PipelineDefinition(
            newId,
            name,
            pipelineType,
            fileAssetId,
            tenantId,
            steps,
            priority,
            3  // 기본 재시도 횟수
        );

        // 도메인 이벤트 발행
        pipeline.addDomainEvent(
            new PipelineCreatedEvent(
                newId,
                pipelineType,
                fileAssetId,
                tenantId
            )
        );

        return pipeline;
    }

    /**
     * 파이프라인 시작
     * 비즈니스 규칙: PENDING 상태에서만 시작 가능
     */
    public void start() {
        if (!canStart()) {
            throw new InvalidPipelineStateException(
                String.format(
                    "Cannot start pipeline in %s status. Pipeline ID: %s",
                    status, pipelineId
                )
            );
        }

        this.status = PipelineStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 이벤트 발행
        addDomainEvent(
            new PipelineStartedEvent(
                pipelineId,
                pipelineType,
                priority
            )
        );
    }

    /**
     * 파이프라인 완료
     */
    public void complete(PipelineResult result) {
        if (!isRunning()) {
            throw new InvalidPipelineStateException(
                "Cannot complete pipeline that is not running"
            );
        }

        this.status = PipelineStatus.COMPLETED;
        this.result = Objects.requireNonNull(result);
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 이벤트 발행
        addDomainEvent(
            new PipelineCompletedEvent(
                pipelineId,
                result,
                calculateDuration()
            )
        );
    }

    /**
     * 파이프라인 실패 처리
     */
    public void fail(String errorMessage) {
        if (!isRunning()) {
            throw new InvalidPipelineStateException(
                "Cannot fail pipeline that is not running"
            );
        }

        this.errorMessage = errorMessage;

        if (canRetry()) {
            // 재시도 가능한 경우
            this.currentRetryCount++;
            this.status = PipelineStatus.PENDING;  // 대기 상태로 변경

            addDomainEvent(
                new PipelineRetryScheduledEvent(
                    pipelineId,
                    currentRetryCount,
                    maxRetryCount
                )
            );
        } else {
            // 최종 실패
            this.status = PipelineStatus.FAILED;
            this.completedAt = LocalDateTime.now();

            addDomainEvent(
                new PipelineFailedEvent(
                    pipelineId,
                    errorMessage,
                    currentRetryCount
                )
            );
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 파이프라인 취소
     */
    public void cancel(String reason) {
        if (!canCancel()) {
            throw new InvalidPipelineStateException(
                String.format(
                    "Cannot cancel pipeline in %s status",
                    status
                )
            );
        }

        this.status = PipelineStatus.CANCELLED;
        this.errorMessage = "Cancelled: " + reason;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(
            new PipelineCancelledEvent(
                pipelineId,
                reason
            )
        );
    }

    /**
     * 일시 중지
     */
    public void pause() {
        if (!isRunning()) {
            throw new InvalidPipelineStateException(
                "Can only pause running pipeline"
            );
        }

        this.status = PipelineStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재개
     */
    public void resume() {
        if (status != PipelineStatus.PAUSED) {
            throw new InvalidPipelineStateException(
                "Can only resume paused pipeline"
            );
        }

        this.status = PipelineStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    // ========== 비즈니스 규칙 메서드 (Tell, Don't Ask) ==========

    /**
     * Law of Demeter 준수 - 직접 질의 메서드 제공
     * 외부에서 getter 체이닝 금지
     */
    public boolean isHighPriority() {
        return priority == Priority.HIGH || priority == Priority.URGENT;
    }

    public boolean isLowPriority() {
        return priority == Priority.LOW;
    }

    public boolean isCompleted() {
        return status == PipelineStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PipelineStatus.FAILED;
    }

    public boolean isRunning() {
        return status == PipelineStatus.RUNNING;
    }

    public boolean isPending() {
        return status == PipelineStatus.PENDING;
    }

    public boolean canStart() {
        return status == PipelineStatus.PENDING;
    }

    public boolean canCancel() {
        return status == PipelineStatus.PENDING
            || status == PipelineStatus.RUNNING
            || status == PipelineStatus.PAUSED;
    }

    public boolean canRetry() {
        return currentRetryCount < maxRetryCount;
    }

    /**
     * 실행 시간 계산
     */
    public long calculateDuration() {
        if (startedAt == null || completedAt == null) {
            return 0;
        }
        return java.time.Duration.between(startedAt, completedAt).toMillis();
    }

    /**
     * 다음 실행할 Step 반환
     */
    public PipelineStep getNextStep(String currentStepId) {
        if (steps.isEmpty()) {
            return null;
        }

        if (currentStepId == null) {
            // 첫 번째 Step 반환
            return steps.get(0);
        }

        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).getStepId().equals(currentStepId)) {
                return steps.get(i + 1);
            }
        }

        return null;  // 마지막 Step이거나 찾을 수 없음
    }

    // ========== 검증 메서드 ==========

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pipeline name cannot be empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException(
                "Pipeline name cannot exceed 255 characters"
            );
        }
    }

    private void validateFileAssetId(Long fileAssetId) {
        if (fileAssetId == null || fileAssetId <= 0) {
            throw new IllegalArgumentException("Invalid file asset ID");
        }
    }

    private void validateTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
    }

    private void validateSteps(List<PipelineStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException(
                "Pipeline must have at least one step"
            );
        }

        // Step 순서 검증
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getOrder() != i) {
                throw new IllegalArgumentException(
                    "Pipeline steps must be ordered sequentially"
                );
            }
        }
    }

    private void validateMaxRetryCount(int maxRetryCount) {
        if (maxRetryCount < 0 || maxRetryCount > 10) {
            throw new IllegalArgumentException(
                "Max retry count must be between 0 and 10"
            );
        }
    }

    // ========== Domain Event 관리 ==========

    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }

    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== Getters (Manual - No Lombok!) ==========

    public PipelineId getPipelineId() {
        return pipelineId;
    }

    public String getName() {
        return name;
    }

    public PipelineType getPipelineType() {
        return pipelineType;
    }

    public Long getFileAssetId() {
        return fileAssetId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public List<PipelineStep> getSteps() {
        return new ArrayList<>(steps);  // 방어적 복사
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public PipelineResult getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
```

### 3.2 ProcessingPolicy.java

```java
package com.ryuqq.fileflow.domain.pipeline.aggregate;

import com.ryuqq.fileflow.domain.pipeline.enums.*;
import com.ryuqq.fileflow.domain.pipeline.event.*;
import com.ryuqq.fileflow.domain.pipeline.vo.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 처리 정책 Aggregate Root
 *
 * 테넌트별로 파일 타입에 대한 처리 정책을 정의합니다.
 * 비즈니스 규칙: 테넌트당 파일 타입별로 하나의 정책만 존재
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ProcessingPolicy {

    private final ProcessingPolicyId policyId;
    private final Long tenantId;  // Long FK 전략
    private final FileType fileType;

    private boolean enabled;
    private boolean autoExecute;

    // Polymorphic Value Object - 파일 타입별로 다른 설정
    private final ProcessingSettings settings;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final transient List<Object> domainEvents = new ArrayList<>();

    /**
     * Private 생성자
     */
    private ProcessingPolicy(
        ProcessingPolicyId policyId,
        Long tenantId,
        FileType fileType,
        ProcessingSettings settings,
        boolean autoExecute
    ) {
        validateTenantId(tenantId);

        this.policyId = Objects.requireNonNull(policyId);
        this.tenantId = tenantId;
        this.fileType = Objects.requireNonNull(fileType);
        this.settings = Objects.requireNonNull(settings);
        this.autoExecute = autoExecute;

        this.enabled = true;  // 생성 시 기본 활성화
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Static Factory Method - 테넌트를 위한 정책 생성
     */
    public static ProcessingPolicy createForTenant(
        Long tenantId,
        FileType fileType,
        ProcessingSettings settings,
        boolean autoExecute
    ) {
        ProcessingPolicyId newId = ProcessingPolicyId.generate();

        ProcessingPolicy policy = new ProcessingPolicy(
            newId,
            tenantId,
            fileType,
            settings,
            autoExecute
        );

        policy.addDomainEvent(
            new ProcessingPolicyCreatedEvent(
                newId,
                tenantId,
                fileType
            )
        );

        return policy;
    }

    /**
     * 기본 이미지 처리 정책 생성
     */
    public static ProcessingPolicy createDefaultImagePolicy(Long tenantId) {
        return createForTenant(
            tenantId,
            FileType.IMAGE,
            ImageProcessingSettings.createDefault(),
            true  // 자동 실행
        );
    }

    /**
     * 정책 활성화
     */
    public void enable() {
        if (this.enabled) {
            return;  // 이미 활성화됨
        }

        this.enabled = true;
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(
            new ProcessingPolicyEnabledEvent(
                policyId,
                tenantId,
                fileType
            )
        );
    }

    /**
     * 정책 비활성화
     */
    public void disable() {
        if (!this.enabled) {
            return;  // 이미 비활성화됨
        }

        this.enabled = false;
        this.autoExecute = false;  // 비활성화 시 자동 실행도 중지
        this.updatedAt = LocalDateTime.now();

        addDomainEvent(
            new ProcessingPolicyDisabledEvent(
                policyId,
                tenantId,
                fileType
            )
        );
    }

    /**
     * 자동 실행 설정 변경
     */
    public void setAutoExecute(boolean autoExecute) {
        if (!this.enabled && autoExecute) {
            throw new IllegalStateException(
                "Cannot enable auto-execute for disabled policy"
            );
        }

        this.autoExecute = autoExecute;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 파일이 이 정책에 의해 자동 처리되어야 하는지 확인
     * Law of Demeter 준수 - 직접 질의 메서드 제공
     */
    public boolean shouldAutoProcess() {
        return enabled && autoExecute;
    }

    /**
     * 파일이 이 정책으로 처리 가능한지 확인
     */
    public boolean canProcess() {
        return enabled;
    }

    /**
     * 특정 파일 확장자가 이 정책에 해당하는지 확인
     */
    public boolean supportsFileExtension(String extension) {
        return fileType.supportsExtension(extension);
    }

    // ========== 검증 메서드 ==========

    private void validateTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID");
        }
    }

    // ========== Domain Event 관리 ==========

    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }

    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== Getters ==========

    public ProcessingPolicyId getPolicyId() {
        return policyId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public FileType getFileType() {
        return fileType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAutoExecute() {
        return autoExecute;
    }

    public ProcessingSettings getSettings() {
        return settings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
```

---

## 🔨 Step 4: Domain Events 구현

```java
package com.ryuqq.fileflow.domain.pipeline.event;

import com.ryuqq.fileflow.domain.pipeline.vo.PipelineId;
import com.ryuqq.fileflow.domain.pipeline.enums.PipelineType;

import java.time.LocalDateTime;

/**
 * 파이프라인 시작 이벤트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineStartedEvent {
    private final PipelineId pipelineId;
    private final PipelineType pipelineType;
    private final Priority priority;
    private final LocalDateTime occurredAt;

    public PipelineStartedEvent(
        PipelineId pipelineId,
        PipelineType pipelineType,
        Priority priority
    ) {
        this.pipelineId = pipelineId;
        this.pipelineType = pipelineType;
        this.priority = priority;
        this.occurredAt = LocalDateTime.now();
    }

    // Getters
    public PipelineId getPipelineId() { return pipelineId; }
    public PipelineType getPipelineType() { return pipelineType; }
    public Priority getPriority() { return priority; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
```

---

## ✅ 개발 체크리스트

### 필수 확인 사항
- [ ] **NO Lombok**: 모든 클래스에서 Lombok 사용 안함
- [ ] **Manual Getters**: 모든 getter 직접 작성
- [ ] **No Setters**: Aggregate는 setter 없이 비즈니스 메서드로만 변경
- [ ] **Law of Demeter**: getter 체이닝 대신 직접 질의 메서드 제공
- [ ] **Long FK**: JPA 관계 어노테이션 사용 안함
- [ ] **Static Factory**: public 생성자 대신 static factory method
- [ ] **Immutable VO**: 모든 Value Object는 불변
- [ ] **Javadoc**: 모든 public 클래스/메서드에 작성

### 테스트 작성
- [ ] PipelineDefinition 단위 테스트
- [ ] ProcessingPolicy 단위 테스트
- [ ] Value Object 동등성 테스트
- [ ] Domain Event 발행 테스트
- [ ] 비즈니스 규칙 검증 테스트

---

## 📝 주니어 개발자를 위한 팁

1. **순서대로 구현하기**
   - Enum → Value Object → Aggregate 순서로 구현
   - 의존성이 없는 것부터 시작

2. **테스트 먼저 작성**
   - TDD 방식으로 테스트 먼저 작성
   - 비즈니스 규칙을 테스트로 검증

3. **코드 리뷰 체크포인트**
   - Lombok 사용 여부
   - Getter 체이닝 여부
   - 불변성 보장 여부
   - Javadoc 작성 여부

4. **디버깅 팁**
   - Domain Event 발행 로그 추가
   - 상태 전환 시 로그 추가
   - 검증 실패 시 명확한 에러 메시지

---

**다음 단계**: Application Layer 구현 가이드로 이동