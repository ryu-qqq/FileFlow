package com.ryuqq.adapter.in.web.{domain_lower};

import com.ryuqq.application.{domain_lower}.{Domain}Orchestrator;
import com.ryuqq.application.{domain_lower}.command.{Domain}Command;
import com.ryuqq.application.common.orchestration.Outcome;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {Domain} REST API Controller
 *
 * <p>Orchestration Pattern의 비동기 처리를 위한 Controller입니다.
 * 202 Accepted 응답으로 즉시 반환하고, 백그라운드에서 처리합니다.</p>
 *
 * @author {author_name}
 * @since {version}
 */
@RestController
@RequestMapping("/api/v1/{domain_lower}")
public class {Domain}Controller {

    private final {Domain}Orchestrator orchestrator;

    public {Domain}Controller({Domain}Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * {Domain} 작업 실행 (비동기)
     *
     * <p>202 Accepted를 즉시 반환하고, 백그라운드에서 처리합니다.
     * IdemKey를 통해 멱등성이 보장됩니다.</p>
     */
    @PostMapping
    public ResponseEntity<{Domain}Response> execute(@RequestBody {Domain}Request request) {
        {Domain}Command command = new {Domain}Command(
            request.businessKey(),
            request.idempotencyKey()
            // TODO: 추가 필드 매핑
        );

        Outcome outcome = orchestrator.execute(command);

        // TODO: Outcome → Response DTO 매핑
        {Domain}Response response = mapToResponse(outcome);

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(response);
    }

    /**
     * Outcome → Response DTO 변환
     */
    private {Domain}Response mapToResponse(Outcome outcome) {
        // TODO: 구현 필요
        return switch (outcome) {
            case Outcome.Ok ok -> new {Domain}Response(
                ok.getOpId().toString(),
                "COMPLETED",
                ok.getMessage(),
                null
            );
            case Outcome.Retry retry -> new {Domain}Response(
                retry.getOpId().toString(),
                "PENDING",
                retry.getMessage(),
                retry.getNextRetryAt().toString()
            );
            case Outcome.Fail fail -> new {Domain}Response(
                fail.getOpId().toString(),
                "FAILED",
                fail.getMessage(),
                null
            );
        };
    }

    // Request/Response DTOs
    public record {Domain}Request(
        String businessKey,
        String idempotencyKey
        // TODO: 추가 필드
    ) {}

    public record {Domain}Response(
        String operationId,
        String status,
        String message,
        String nextRetryAt
    ) {}
}
