package com.ryuqq.fileflow.domain.transform.aggregate;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.transform.exception.TransformErrorCode;
import com.ryuqq.fileflow.domain.transform.exception.TransformException;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 이미지 변환 요청 Aggregate Root.
 *
 * <p>이미지 파일에 대한 변환(리사이즈, 포맷 변환, 압축, 썸네일)을 요청합니다.
 *
 * <p>라이프사이클: QUEUED → PROCESSING → COMPLETED | FAILED
 *
 * <p><strong>비즈니스 룰:</strong> 이미지 파일(image/*)만 변환 요청 가능합니다.
 */
public class TransformRequest {

    private final TransformRequestId id;
    private final AssetId sourceAssetId;
    private final String sourceContentType;
    private final TransformType type;
    private final TransformParams params;
    private TransformStatus status;
    private AssetId resultAssetId;
    private String lastError;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private final List<DomainEvent> events = new ArrayList<>();

    private TransformRequest(
            TransformRequestId id,
            AssetId sourceAssetId,
            String sourceContentType,
            TransformType type,
            TransformParams params,
            TransformStatus status,
            AssetId resultAssetId,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        this.id = id;
        this.sourceAssetId = sourceAssetId;
        this.sourceContentType = sourceContentType;
        this.type = type;
        this.params = params;
        this.status = status;
        this.resultAssetId = resultAssetId;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    /**
     * 새 변환 요청 생성.
     *
     * @throws TransformException sourceContentType이 이미지가 아닌 경우
     * @throws TransformException TransformType에 필요한 파라미터가 누락된 경우
     */
    public static TransformRequest forNew(
            TransformRequestId id,
            AssetId sourceAssetId,
            String sourceContentType,
            TransformType type,
            TransformParams params,
            Instant now) {
        validateImageContentType(sourceContentType);
        validateParamsForType(type, params);

        return new TransformRequest(
                id,
                sourceAssetId,
                sourceContentType,
                type,
                params,
                TransformStatus.QUEUED,
                null,
                null,
                now,
                now,
                null);
    }

    public static TransformRequest reconstitute(
            TransformRequestId id,
            AssetId sourceAssetId,
            String sourceContentType,
            TransformType type,
            TransformParams params,
            TransformStatus status,
            AssetId resultAssetId,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        return new TransformRequest(
                id,
                sourceAssetId,
                sourceContentType,
                type,
                params,
                status,
                resultAssetId,
                lastError,
                createdAt,
                updatedAt,
                completedAt);
    }

    /** 변환 처리 시작. */
    public void start(Instant now) {
        if (this.status != TransformStatus.QUEUED) {
            throw new TransformException(
                    TransformErrorCode.INVALID_TRANSFORM_STATUS,
                    "Cannot start transform in status: " + this.status);
        }
        this.status = TransformStatus.PROCESSING;
        this.updatedAt = now;
    }

    /** 변환 완료 처리. 결과 Asset ID와 결과 이미지 해상도를 기록합니다. */
    public void complete(AssetId resultAssetId, int resultWidth, int resultHeight, Instant now) {
        if (this.status != TransformStatus.PROCESSING) {
            throw new TransformException(
                    TransformErrorCode.INVALID_TRANSFORM_STATUS,
                    "Cannot complete transform in status: " + this.status);
        }
        Objects.requireNonNull(resultAssetId, "resultAssetId must not be null");

        this.status = TransformStatus.COMPLETED;
        this.resultAssetId = resultAssetId;
        this.completedAt = now;
        this.updatedAt = now;
        this.lastError = null;
    }

    /** 변환 실패 처리. */
    public void fail(String errorMessage, Instant now) {
        this.status = TransformStatus.FAILED;
        this.lastError = errorMessage;
        this.completedAt = now;
        this.updatedAt = now;
    }

    // -- query methods --

    public TransformRequestId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public AssetId sourceAssetId() {
        return sourceAssetId;
    }

    public String sourceAssetIdValue() {
        return sourceAssetId.value();
    }

    public String sourceContentType() {
        return sourceContentType;
    }

    public TransformType type() {
        return type;
    }

    public TransformParams params() {
        return params;
    }

    public TransformStatus status() {
        return status;
    }

    public AssetId resultAssetId() {
        return resultAssetId;
    }

    public String resultAssetIdValue() {
        return resultAssetId != null ? resultAssetId.value() : null;
    }

    public String lastError() {
        return lastError;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    // -- event management --

    protected void registerEvent(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> pollEvents() {
        List<DomainEvent> snapshot = Collections.unmodifiableList(new ArrayList<>(events));
        events.clear();
        return snapshot;
    }

    // -- invariant validation --

    private static void validateImageContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new TransformException(
                    TransformErrorCode.NOT_IMAGE_FILE,
                    "Transform is only supported for image files, got: " + contentType);
        }
    }

    private static void validateParamsForType(TransformType type, TransformParams params) {
        Objects.requireNonNull(params, "params must not be null");

        switch (type) {
            case RESIZE -> {
                if (params.width() == null && params.height() == null) {
                    throw new TransformException(
                            TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                            "RESIZE requires at least width or height");
                }
            }
            case CONVERT -> {
                if (params.targetFormat() == null || params.targetFormat().isBlank()) {
                    throw new TransformException(
                            TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                            "CONVERT requires targetFormat");
                }
            }
            case COMPRESS -> {
                if (params.quality() == null) {
                    throw new TransformException(
                            TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                            "COMPRESS requires quality (1-100)");
                }
            }
            case THUMBNAIL -> {
                if (params.width() == null || params.height() == null) {
                    throw new TransformException(
                            TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                            "THUMBNAIL requires both width and height");
                }
            }
        }
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformRequest that = (TransformRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
