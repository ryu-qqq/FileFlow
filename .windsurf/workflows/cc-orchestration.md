---
description: Orchestration Pattern 보일러플레이트를 CC에 준수하여 만든다
---

# Orchestration Pattern Workflow - External API Integration

**Version**: 1.0.0
**Framework**: Spring Boot 3.5.x + Java 21
**Pattern**: 3-Phase Lifecycle (Accept → Execute → Finalize) + Idempotency + WAL
---

## 📚 Overview

Orchestration Pattern은 **외부 API 호출의 안전한 멱등성 보장** 및 **크래시 복구**를 위한 패턴입니다.

### 핵심 개념
- **3-Phase Lifecycle**: Accept(DB 기록) → Execute(외부 API) → Finalize(결과 저장)
- **Idempotency**: IdemKey + DB Unique 제약으로 중복 실행 방지
- **Write-Ahead Log (WAL)**: 크래시 복구 메커니즘
- **Outcome Modeling**: Sealed interface (Ok/Retry/Fail)
- **Async Execution**: @Async로 트랜잭션 밖에서 외부 API 호출

---

## 🏗️ Directory Structure

```
application/
└── src/
    └── main/
        └── java/
            └── com/company/application/
                └── [domain]/           # 예: payment, fileupload, notification
                    ├── command/        # Command Record
                    │   └── PaymentCommand.java
                    │
                    ├── orchestrator/   # 핵심 오케스트레이터
                    │   └── PaymentOrchestrator.java
                    │
                    ├── scheduler/      # 복구 스케줄러
                    │   ├── PaymentFinalizer.java
                    │   └── PaymentReaper.java
                    │
                    └── outcome/        # 결과 모델링
                        └── PaymentOutcome.java

adapter-out/
└── persistence-mysql/
    └── src/
        └── main/
            └── java/
                └── com/company/adapter/out/persistence/
                    └── [domain]/
                        ├── entity/
                        │   ├── PaymentOperationEntity.java
                        │   └── PaymentWriteAheadLogEntity.java
                        │
                        └── repository/
                            ├── PaymentOperationRepository.java
                            └── PaymentWriteAheadLogRepository.java

adapter-in/
└── web/
    └── src/
        └── main/
            └── java/
                └── com/company/adapter/in/web/
                    └── [domain]/
                        └── PaymentController.java
```

---

## 🎯 Component Templates

### 1. Command Record (Lombok 금지)

```java
package com.company.application.payment.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * PaymentCommand - 결제 처리 명령
 *
 * <p>멱등성 보장을 위한 IdemKey를 포함한 Command Record입니다.</p>
 *
 * <p><strong>핵심 원칙:</strong></p>
 * <ul>
 *   <li>Record 패턴 사용 (Lombok 금지)</li>
 *   <li>IdemKey 필수 (중복 실행 방지)</li>
 *   <li>Compact Constructor로 검증</li>
 * </ul>
 *
 * @param idemKey 멱등성 키 (UUID or Business Key)
 * @param orderId 주문 ID
 * @param amount 결제 금액
 * @param paymentMethod 결제 방법
 * @param customerId 고객 ID (Long FK)
 * @param timestamp 요청 시간
 *
 * @author cc-orchestration
 * @since 1.0.0
 */
public record PaymentCommand(
        String idemKey,
        Long orderId,
        BigDecimal amount,
        String paymentMethod,
        Long customerId,
        Instant timestamp
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 활용한 불변성 보장 및 검증</p>
     */
    public PaymentCommand {
        // IdemKey 검증 (필수)
        Objects.requireNonNull(idemKey, "IdemKey must not be null");
        if (idemKey.isBlank()) {
            throw new IllegalArgumentException("IdemKey must not be blank");
        }

        // 비즈니스 필드 검증
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Objects.requireNonNull(paymentMethod, "Payment method must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");

        // 타임스탬프 기본값 설정
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    /**
     * 정적 팩토리 메서드 - 비즈니스 키 기반 IdemKey 생성
     */
    public static PaymentCommand withBusinessKey(
            Long orderId,
            BigDecimal amount,
            String paymentMethod,
            Long customerId) {
        // 비즈니스 키 조합으로 IdemKey 생성
        String idemKey = String.format("PAY-%d-%d-%s",
            customerId, orderId, paymentMethod);
        return new PaymentCommand(
            idemKey,
            orderId,
            amount,
            paymentMethod,
            customerId,
            Instant.now()
        );
    }
}
```

### 2. Orchestrator (BaseOrchestrator 상속)

```java
package com.company.application.payment.orchestrator;

import com.company.application.common.orchestration.BaseOrchestrator;
import com.company.application.common.orchestration.Outcome;
import com.company.application.payment.command.PaymentCommand;
import com.company.adapter.out.integration.payment.PaymentGatewayClient;
import com.company.adapter.out.integration.payment.PaymentRequest;
import com.company.adapter.out.integration.payment.PaymentResponse;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * PaymentOrchestrator - 결제 처리 오케스트레이터
 *
 * <p>3-Phase Lifecycle을 통해 외부 결제 API를 안전하게 호출합니다.</p>
 *
 * <p><strong>처리 단계:</strong></p>
 * <ol>
 *   <li>Accept: IdemKey 검증, Operation 저장, WAL 기록</li>
 *   <li>Execute: @Async로 외부 API 호출 (트랜잭션 밖)</li>
 *   <li>Finalize: 결과 저장, WAL 완료 처리</li>
 * </ol>
 *
 * <p><strong>Zero-Tolerance Rules:</strong></p>
 * <ul>
 *   <li>executeInternal()에 @Async 필수</li>
 *   <li>executeInternal()에 @Transactional 금지</li>
 *   <li>Outcome(Ok/Retry/Fail) 반환 필수</li>
 * </ul>
 *
 * @author cc-orchestration
 * @since 1.0.0
 */
@Component
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {

    private final PaymentGatewayClient paymentGatewayClient;

    /**
     * PaymentOrchestrator 생성자
     *
     * @param paymentGatewayClient 외부 결제 게이트웨이 클라이언트
     */
    public PaymentOrchestrator(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    /**
     * Domain 이름 반환
     */
    @Override
    protected String domain() {
        return "PAYMENT";
    }

    /**
     * EventType 반환
     */
    @Override
    protected String eventType() {
        return "PAYMENT_REQUESTED";
    }

    /**
     * 결제 처리 실행 (외부 API 호출)
     *
     * <p><strong>중요:</strong></p>
     * <ul>
     *   <li>@Async 필수 (트랜잭션 밖에서 실행)</li>
     *   <li>@Transactional 금지 (외부 API 호출 시 트랜잭션 유지 금지)</li>
     *   <li>멱등성 보장 (동일 IdemKey는 재실행되지 않음)</li>
     * </ul>
     *
     * @param command 결제 명령
     * @return Outcome (Ok: 성공, Retry: 재시도, Fail: 실패)
     */
    @Async
    @Override
    protected Outcome executeInternal(PaymentCommand command) {
        try {
            // 1. 외부 API 요청 준비
            PaymentRequest request = PaymentRequest.builder()
                .orderId(command.orderId())
                .amount(command.amount())
                .paymentMethod(command.paymentMethod())
                .customerId(command.customerId())
                .idempotencyKey(command.idemKey())  // 외부 API도 멱등성 지원 시
                .build();

            // 2. 외부 API 호출 (트랜잭션 밖)
            PaymentResponse response = paymentGatewayClient.processPayment(request);

            // 3. 응답 분석 및 Outcome 결정
            if (response.isSuccess()) {
                // 성공
                return Outcome.ok()
                    .withData("transactionId", response.getTransactionId())
                    .withData("approvalCode", response.getApprovalCode());
            } else if (response.isRetryable()) {
                // 일시적 오류 (재시도 가능)
                return Outcome.retry()
                    .withReason(response.getErrorMessage())
                    .withBackoffSeconds(calculateBackoff(command));
            } else {
                // 영구적 오류 (재시도 불가)
                return Outcome.fail()
                    .withReason(response.getErrorMessage())
                    .withErrorCode(response.getErrorCode());
            }

        } catch (Exception e) {
            // 예외 분류
            if (isTransientException(e)) {
                // 네트워크 오류 등 일시적 문제
                return Outcome.retry()
                    .withReason("Transient error: " + e.getMessage())
                    .withBackoffSeconds(calculateBackoff(command));
            } else {
                // 영구적 오류
                return Outcome.fail()
                    .withReason("Permanent error: " + e.getMessage());
            }
        }
    }

    /**
     * Backoff 시간 계산 (Exponential Backoff)
     */
    private int calculateBackoff(PaymentCommand command) {
        int attemptNumber = getAttemptNumber(command.idemKey());
        return Math.min(300, (int) Math.pow(2, attemptNumber) * 10);  // 최대 5분
    }

    /**
     * 일시적 예외 판별
     */
    private boolean isTransientException(Exception e) {
        return e instanceof java.net.SocketTimeoutException
            || e instanceof java.net.ConnectException
            || e instanceof org.springframework.web.client.ResourceAccessException;
    }
}
```

### 3. Operation Entity (JPA)

```java
package com.company.adapter.out.persistence.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentOperationEntity - 결제 작업 엔티티
 *
 * <p>멱등성 보장을 위한 Operation 상태 관리</p>
 *
 * <p><strong>핵심 원칙:</strong></p>
 * <ul>
 *   <li>IdemKey Unique 제약 필수</li>
 *   <li>Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>상태 추적 (ACCEPTED → PROCESSING → COMPLETED/FAILED)</li>
 * </ul>
 *
 * @author cc-orchestration
 * @since 1.0.0
 */
@Entity
@Table(
    name = "payment_operation",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payment_operation_idem_key",
            columnNames = {"idem_key"}
        )
    },
    indexes = {
        @Index(name = "idx_payment_operation_state", columnList = "state"),
        @Index(name = "idx_payment_operation_created_at", columnList = "created_at")
    }
)
public class PaymentOperationEntity {

    @Id
    @Column(name = "idem_key", length = 100)
    private String idemKey;

    @Column(name = "order_id", nullable = false)
    private Long orderId;  // Long FK

    @Column(name = "customer_id", nullable = false)
    private Long customerId;  // Long FK

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private OperationState state;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "outcome_data", columnDefinition = "JSON")
    private String outcomeData;  // JSON 형태로 Outcome 저장

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Operation 상태
     */
    public enum OperationState {
        ACCEPTED,      // 요청 수락
        PROCESSING,    // 처리 중
        COMPLETED,     // 완료
        FAILED,        // 실패
        TIMEOUT        // 타임아웃
    }

    // Constructors
    protected PaymentOperationEntity() {
        // JPA용 기본 생성자
    }

    /**
     * 신규 Operation 생성
     */
    public static PaymentOperationEntity create(
            String idemKey,
            Long orderId,
            Long customerId,
            BigDecimal amount,
            String paymentMethod) {
        PaymentOperationEnt