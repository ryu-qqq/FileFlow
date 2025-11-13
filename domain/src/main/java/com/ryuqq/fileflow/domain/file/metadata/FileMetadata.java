package com.ryuqq.fileflow.domain.file.metadata;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * File Metadata Value Object
 *
 * <p>파일의 메타데이터를 나타내는 불변 Value Object입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>파일 타입별 메타데이터 관리</li>
 *   <li>EXIF 데이터 (이미지)</li>
 *   <li>비디오 정보 (Duration, Resolution, Codec)</li>
 *   <li>문서 정보 (작성자, 생성일, 페이지 수)</li>
 * </ul>
 *
 * <p><strong>지원 메타데이터:</strong></p>
 * <pre>
 * 이미지 (EXIF):
 *   - width: 4000 (픽셀)
 *   - height: 3000 (픽셀)
 *   - takenAt: "2025-01-01T10:00:00" (촬영 날짜)
 *   - gpsLatitude: 37.5665 (위도)
 *   - gpsLongitude: 126.9780 (경도)
 *   - cameraModel: "iPhone 15 Pro" (카메라 모델)
 *   - iso: 100 (ISO)
 *   - fNumber: 1.8 (조리개)
 *   - exposureTime: "1/1000" (노출 시간)
 *
 * 비디오:
 *   - duration: 120 (초)
 *   - width: 1920 (픽셀)
 *   - height: 1080 (픽셀)
 *   - codec: "H.264" (코덱)
 *   - frameRate: 30.0 (FPS)
 *   - bitrate: 5000000 (비트레이트)
 *
 * 문서 (PDF, DOCX):
 *   - author: "John Doe" (작성자)
 *   - createdAt: "2025-01-01T10:00:00" (생성일)
 *   - pageCount: 10 (페이지 수)
 *   - title: "Report 2025" (제목)
 * </pre>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>
 * FileMetadata metadata = new FileMetadata(
 *     fileId,
 *     Map.of(
 *         "width", 4000,
 *         "height", 3000,
 *         "takenAt", "2025-01-01T10:00:00",
 *         "cameraModel", "iPhone 15 Pro"
 *     )
 * );
 *
 * Optional&lt;Integer&gt; width = metadata.getInteger("width");
 * Optional&lt;String&gt; camera = metadata.getString("cameraModel");
 * </pre>
 *
 * <p><strong>불변성:</strong></p>
 * <ul>
 *   <li>Record 패턴 사용 (Java 21)</li>
 *   <li>모든 필드는 final</li>
 *   <li>metadata Map은 불변 (Collections.unmodifiableMap)</li>
 * </ul>
 *
 * @param fileId   파일 ID (com.ryuqq.fileflow.domain.file.asset.FileAssetId)
 * @param metadata 메타데이터 Map (Key: 메타데이터 이름, Value: 메타데이터 값)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileMetadata(
    FileAssetId fileId,
    Map<String, Object> metadata
) {

    /**
     * Compact Constructor - 유효성 검증 및 불변성 보장
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>fileId는 null 불가</li>
     *   <li>metadata는 null 불가</li>
     *   <li>metadata는 불변 Map으로 변환</li>
     * </ul>
     *
     * @throws IllegalArgumentException 검증 실패 시
     */
    public FileMetadata {
        if (fileId == null) {
            throw new IllegalArgumentException("FileAssetId는 null일 수 없습니다");
        }

        if (metadata == null) {
            throw new IllegalArgumentException("Metadata는 null일 수 없습니다");
        }

        // 불변 Map으로 변환
        metadata = Collections.unmodifiableMap(metadata);
    }

    /**
     * 빈 메타데이터 생성 (Factory Method)
     *
     * @param fileId 파일 ID
     * @return 빈 메타데이터
     */
    public static FileMetadata empty(FileAssetId fileId) {
        return new FileMetadata(fileId, Map.of());
    }

    /**
     * 메타데이터가 비어있는지 확인
     *
     * @return 비어있으면 true
     */
    public boolean isEmpty() {
        return metadata.isEmpty();
    }

    /**
     * 메타데이터 개수
     *
     * @return 메타데이터 개수
     */
    public int size() {
        return metadata.size();
    }

    /**
     * 특정 Key의 메타데이터 존재 여부
     *
     * @param key 메타데이터 Key
     * @return 존재하면 true
     */
    public boolean contains(String key) {
        return metadata.containsKey(key);
    }

    /**
     * 문자열 메타데이터 조회
     *
     * @param key 메타데이터 Key
     * @return 메타데이터 값 (없으면 Optional.empty())
     */
    public Optional<String> getString(String key) {
        Object value = metadata.get(key);
        if (value instanceof String) {
            return Optional.of((String) value);
        }
        return Optional.empty();
    }

    /**
     * 정수 메타데이터 조회
     *
     * @param key 메타데이터 Key
     * @return 메타데이터 값 (없으면 Optional.empty())
     */
    public Optional<Integer> getInteger(String key) {
        Object value = metadata.get(key);
        if (value instanceof Integer) {
            return Optional.of((Integer) value);
        }
        return Optional.empty();
    }

    /**
     * Long 메타데이터 조회
     *
     * @param key 메타데이터 Key
     * @return 메타데이터 값 (없으면 Optional.empty())
     */
    public Optional<Long> getLong(String key) {
        Object value = metadata.get(key);
        if (value instanceof Long) {
            return Optional.of((Long) value);
        }
        return Optional.empty();
    }

    /**
     * Double 메타데이터 조회
     *
     * @param key 메타데이터 Key
     * @return 메타데이터 값 (없으면 Optional.empty())
     */
    public Optional<Double> getDouble(String key) {
        Object value = metadata.get(key);
        if (value instanceof Double) {
            return Optional.of((Double) value);
        }
        return Optional.empty();
    }

    /**
     * LocalDateTime 메타데이터 조회
     *
     * @param key 메타데이터 Key
     * @return 메타데이터 값 (없으면 Optional.empty())
     */
    public Optional<LocalDateTime> getDateTime(String key) {
        Object value = metadata.get(key);
        if (value instanceof LocalDateTime) {
            return Optional.of((LocalDateTime) value);
        }
        return Optional.empty();
    }

    /**
     * 이미지 너비 조회 (편의 메서드)
     *
     * @return 너비 (픽셀)
     */
    public Optional<Integer> getWidth() {
        return getInteger("width");
    }

    /**
     * 이미지 높이 조회 (편의 메서드)
     *
     * @return 높이 (픽셀)
     */
    public Optional<Integer> getHeight() {
        return getInteger("height");
    }

    /**
     * 촬영 날짜 조회 (편의 메서드)
     *
     * @return 촬영 날짜
     */
    public Optional<LocalDateTime> getTakenAt() {
        return getDateTime("takenAt");
    }

    /**
     * 카메라 모델 조회 (편의 메서드)
     *
     * @return 카메라 모델
     */
    public Optional<String> getCameraModel() {
        return getString("cameraModel");
    }

    /**
     * GPS 위도 조회 (편의 메서드)
     *
     * @return 위도
     */
    public Optional<Double> getGpsLatitude() {
        return getDouble("gpsLatitude");
    }

    /**
     * GPS 경도 조회 (편의 메서드)
     *
     * @return 경도
     */
    public Optional<Double> getGpsLongitude() {
        return getDouble("gpsLongitude");
    }

    /**
     * FileAssetId 값 조회
     *
     * @return FileAssetId 값
     */
    public Long getFileAssetIdValue() {
        return fileId.value();
    }
}
