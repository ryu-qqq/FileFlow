package com.ryuqq.fileflow.domain.file.variant;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FileVariant Aggregate Root
 *
 * <p>원본 파일의 변형(Variant)을 관리하는 Aggregate입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>파일 변형 메타데이터 관리 (썸네일, 압축 등)</li>
 *   <li>변형 파일의 S3 저장 위치 관리</li>
 *   <li>원본 파일과의 관계 유지 (parentFileAssetId)</li>
 *   <li>변형 생성 이벤트 발행</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>Variant는 항상 원본 FileAsset을 참조함 (parentFileAssetId)</li>
 *   <li>Variant는 원본보다 작거나 같은 크기여야 함</li>
 *   <li>동일 (parentFileAssetId, variantType) 조합은 유일해야 함</li>
 * </ul>
 *
 * <p><strong>Long FK 전략:</strong></p>
 * <ul>
 *   <li>✅ FileAssetId VO로 원본 파일 참조</li>
 *   <li>✅ getParentFileAssetIdValue()로 Long 값 조회 가능</li>
 *   <li>❌ @ManyToOne FileAsset 금지</li>
 * </ul>
 *
 * <p><strong>Zero External Dependencies:</strong></p>
 * <ul>
 *   <li>Domain Event를 List로 직접 관리 (Spring Data 의존성 제거)</li>
 *   <li>Pure Java 구현</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileVariant {

    private final List<Object> domainEvents = new ArrayList<>();

    private final FileVariantId id;
    private final FileAssetId parentFileAssetId; // ⭐ FileAssetId VO
    private final VariantType variantType;
    private final StorageKey storageKey;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final LocalDateTime createdAt;

    /**
     * Private Constructor (Static Factory Method 사용)
     */
    private FileVariant(
        FileVariantId id,
        FileAssetId parentFileAssetId,
        VariantType variantType,
        StorageKey storageKey,
        FileSize fileSize,
        MimeType mimeType,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.parentFileAssetId = parentFileAssetId;
        this.variantType = variantType;
        this.storageKey = storageKey;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.createdAt = createdAt;
    }

    /**
     * FileVariant 생성 (Static Factory Method)
     *
     * @param parentFileAssetId 원본 FileAsset ID
     * @param variantType 변형 종류
     * @param storageKey S3 저장 키
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @return FileVariant Aggregate
     */
    public static FileVariant create(
        FileAssetId parentFileAssetId,
        VariantType variantType,
        StorageKey storageKey,
        FileSize fileSize,
        MimeType mimeType
    ) {
        if (parentFileAssetId == null || parentFileAssetId.value() == null) {
            throw new IllegalArgumentException("Parent FileAsset ID는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize는 필수입니다");
        }
        if (mimeType == null) {
            throw new IllegalArgumentException("MimeType은 필수입니다");
        }

        FileVariant variant = new FileVariant(
            null, // ID는 Persistence Layer에서 생성
            parentFileAssetId, // ⭐ FileAssetId VO 사용
            variantType,
            storageKey,
            fileSize,
            mimeType,
            LocalDateTime.now()
        );

        // Domain Event 발행
        variant.registerEvent(new FileVariantCreatedEvent(
            null, // ID는 아직 할당되지 않음
            parentFileAssetId,
            variantType
        ));

        return variant;
    }

    /**
     * DB에서 조회한 데이터로 FileVariant 재구성 (Static Factory Method)
     *
     * @param id FileVariant ID
     * @param parentFileAssetId 원본 FileAsset ID (Long)
     * @param variantType 변형 종류
     * @param storageKey S3 저장 키
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param createdAt 생성 시간
     * @return FileVariant Aggregate
     */
    public static FileVariant reconstitute(
        FileVariantId id,
        Long parentFileAssetId,
        VariantType variantType,
        StorageKey storageKey,
        FileSize fileSize,
        MimeType mimeType,
        LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("FileVariantId는 필수입니다");
        }
        if (parentFileAssetId == null) {
            throw new IllegalArgumentException("Parent FileAsset ID는 필수입니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt은 필수입니다");
        }

        return new FileVariant(
            id,
            FileAssetId.of(parentFileAssetId), // Long → FileAssetId 변환
            variantType,
            storageKey,
            fileSize,
            mimeType,
            createdAt
        );
    }

    // Getters (Law of Demeter 준수)

    public FileVariantId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public FileAssetId getParentFileAssetId() {
        return parentFileAssetId;
    }

    public Long getParentFileAssetIdValue() {
        return parentFileAssetId != null ? parentFileAssetId.value() : null;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public StorageKey getStorageKey() {
        return storageKey;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    

    /**
     * Domain Event 등록 (내부 사용)
     *
     * @param event 등록할 도메인 이벤트
     */
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    /**
     * 도메인 이벤트 조회 (Persistence Layer 전용)
     *
     * @return 등록된 도메인 이벤트 목록 (읽기 전용)
     */
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트 초기화 (Persistence Layer 전용)
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
