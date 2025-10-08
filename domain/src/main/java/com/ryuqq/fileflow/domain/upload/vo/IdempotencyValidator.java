package com.ryuqq.fileflow.domain.upload.vo;

import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 멱등성 키 검증 비즈니스 규칙
 *
 * 책임:
 * - 중복 요청 방지를 위한 멱등성 키 검증
 * - 요청 처리 이력 추적
 *
 * 비즈니스 규칙:
 * - 동일한 멱등성 키로 중복 요청 불가
 * - 멱등성 키는 요청 단위로 고유해야 함
 * - 처리된 키는 일정 시간 동안 재사용 불가
 *
 * 참고:
 * - 실제 프로덕션 환경에서는 Redis 등 외부 저장소 사용 권장
 * - 현재 구현은 도메인 모델 수준의 검증 규칙만 제공
 */
public class IdempotencyValidator {

    // 프로덕션에서는 Redis 등 외부 저장소 사용
    // 현재는 도메인 규칙 검증을 위한 인메모리 저장소
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    /**
     * 멱등성 키가 유효한지 검증합니다 (아직 사용되지 않았는지 확인).
     *
     * @param key 검증할 멱등성 키
     * @return 사용 가능하면 true, 이미 사용되었으면 false
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public boolean isValid(IdempotencyKey key) {
        validateNotNull(key);
        return !processedKeys.contains(key.value());
    }

    /**
     * 멱등성 키를 검증하고, 중복인 경우 예외를 발생시킵니다.
     *
     * @param key 검증할 멱등성 키
     * @throws DuplicateRequestException 이미 처리된 키인 경우
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public void validateOrThrow(IdempotencyKey key) {
        validateNotNull(key);

        if (processedKeys.contains(key.value())) {
            throw new DuplicateRequestException(
                    "Duplicate request detected. IdempotencyKey: " + key.value() +
                    " has already been processed."
            );
        }
    }

    /**
     * 멱등성 키를 처리 완료로 표시합니다.
     *
     * @param key 처리 완료할 멱등성 키
     * @return 성공적으로 등록되었으면 true, 이미 등록되어 있었으면 false
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public boolean markAsProcessed(IdempotencyKey key) {
        validateNotNull(key);
        return processedKeys.add(key.value());
    }

    /**
     * 멱등성 키가 이미 처리되었는지 확인합니다.
     *
     * @param key 확인할 멱등성 키
     * @return 이미 처리되었으면 true
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public boolean isProcessed(IdempotencyKey key) {
        validateNotNull(key);
        return processedKeys.contains(key.value());
    }

    /**
     * 멱등성 키를 검증하고 처리 완료로 표시합니다 (원자적 연산).
     *
     * @param key 검증 및 처리할 멱등성 키
     * @throws DuplicateRequestException 이미 처리된 키인 경우
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public void validateAndMarkAsProcessed(IdempotencyKey key) {
        validateNotNull(key);

        // ConcurrentHashMap.add는 원자적 연산
        boolean added = processedKeys.add(key.value());

        if (!added) {
            throw new DuplicateRequestException(
                    "Duplicate request detected. IdempotencyKey: " + key.value() +
                    " has already been processed."
            );
        }
    }

    /**
     * 멱등성 키를 처리 이력에서 제거합니다.
     *
     * 주의: 일반적으로 사용하지 않음. 테스트 또는 명시적인 재시도 정책이 있는 경우에만 사용.
     *
     * @param key 제거할 멱등성 키
     * @return 성공적으로 제거되었으면 true
     * @throws IllegalArgumentException 키가 null인 경우
     */
    public boolean remove(IdempotencyKey key) {
        validateNotNull(key);
        return processedKeys.remove(key.value());
    }

    /**
     * 모든 처리 이력을 초기화합니다.
     *
     * 주의: 일반적으로 사용하지 않음. 테스트 목적으로만 사용.
     */
    public void clear() {
        processedKeys.clear();
    }

    /**
     * 현재 처리된 키의 개수를 반환합니다.
     *
     * @return 처리된 키의 개수
     */
    public int size() {
        return processedKeys.size();
    }

    // ========== Private Helper Methods ==========

    private void validateNotNull(IdempotencyKey key) {
        if (key == null) {
            throw new IllegalArgumentException("IdempotencyKey cannot be null");
        }
    }

    // ========== Custom Exception ==========

    /**
     * 중복 요청 예외
     */
    public static class DuplicateRequestException extends RuntimeException {
        public DuplicateRequestException(String message) {
            super(message);
        }
    }
}
