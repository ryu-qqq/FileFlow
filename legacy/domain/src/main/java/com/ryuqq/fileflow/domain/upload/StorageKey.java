package com.ryuqq.fileflow.domain.upload;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Storage Key Value Object
 * S3 스토리지 키를 표현하는 불변 객체
 *
 * <p>패턴: uploads/{tenantId}/{orgId}/{date}/{uuid}_{fileName}</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Immutable Value Object</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class StorageKey {

    private final String value;

    /**
     * Private 생성자
     *
     * @param value Storage Key 값
     * @throws IllegalArgumentException 값이 null이거나 비어있는 경우
     */
    private StorageKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Storage Key cannot be empty");
        }
        this.value = value;
    }

    /**
     * Static Factory Method (기존 호환성 유지)
     *
     * @param value Storage Key 값
     * @return StorageKey 인스턴스
     * @throws IllegalArgumentException 값이 유효하지 않은 경우
     */
    public static StorageKey of(String value) {
        return new StorageKey(value);
    }

    /**
     * StorageContext 기반 Storage Key 생성 (Static Factory Method)
     *
     * <p>IAM 컨텍스트(Tenant, Organization, UserContext)를 기반으로
     * S3 Storage Key를 생성합니다.</p>
     *
     * <p><strong>생성 규칙:</strong></p>
     * <ul>
     *   <li>패턴: uploads/{tenantId}/{orgId}/{date}/{uuid}_{fileName}</li>
     *   <li>Organization 없음: default 사용</li>
     *   <li>날짜 형식: yyyy/MM/dd</li>
     * </ul>
     *
     * @param storageContext Storage Context (IAM 기반 스토리지 정책)
     * @param fileName 파일명
     * @return StorageKey 인스턴스
     * @throws IllegalArgumentException storageContext 또는 fileName이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static StorageKey generate(
        StorageContext storageContext,
        FileName fileName
    ) {
        if (storageContext == null) {
            throw new IllegalArgumentException("StorageContext는 필수입니다");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("FileName은 필수입니다");
        }

        // StorageContext에게 Storage Key 생성을 위임 (Tell, Don't Ask)
        String keyValue = storageContext.generateStorageKey(
            fileName,
            UUID.randomUUID(),
            LocalDate.now()
        );

        return new StorageKey(keyValue);
    }

    /**
     * Storage Key 값을 반환합니다.
     *
     * @return Storage Key 값
     */
    public String value() {
        return value;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StorageKey that = (StorageKey) o;
        return Objects.equals(value, that.value);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return Storage Key 값
     */
    @Override
    public String toString() {
        return value;
    }
}
