package com.ryuqq.fileflow.adapter.out.metadata.extractor;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Apache Tika Metadata Extractor
 *
 * <p>Apache Tika 라이브러리를 사용한 범용 메타데이터 추출기입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>파일 타입 자동 감지 (Auto-detection)</li>
 *   <li>타입별 메타데이터 추출 (이미지, 비디오, 문서)</li>
 *   <li>메타데이터 정규화 (공통 포맷)</li>
 * </ul>
 *
 * <p><strong>Apache Tika 특징:</strong></p>
 * <ul>
 *   <li>범용 메타데이터 추출 (1000+ 파일 타입 지원)</li>
 *   <li>자동 포맷 감지 (Magic Bytes, Content-Type)</li>
 *   <li>표준 메타데이터 (Dublin Core, XMP, EXIF)</li>
 *   <li>빠른 파싱 (헤더만 읽음, 전체 파일 불필요)</li>
 * </ul>
 *
 * <p><strong>지원 메타데이터:</strong></p>
 * <ul>
 *   <li>이미지: EXIF (촬영 날짜, GPS, 카메라 정보)</li>
 *   <li>비디오: Duration, Resolution, Codec, Frame Rate</li>
 *   <li>문서: 작성자, 생성일, 페이지 수, 제목</li>
 * </ul>
 *
 * <p><strong>처리 예시:</strong></p>
 * <pre>
 * 이미지 (JPEG):
 *   width: 4000
 *   height: 3000
 *   takenAt: 2025-01-01T10:00:00
 *   gpsLatitude: 37.5665
 *   gpsLongitude: 126.9780
 *   cameraModel: "iPhone 15 Pro"
 *
 * 비디오 (MP4):
 *   duration: 120  // 초
 *   width: 1920
 *   height: 1080
 *   codec: "H.264"
 *   frameRate: 30.0
 *
 * 문서 (PDF):
 *   author: "John Doe"
 *   createdAt: 2025-01-01T10:00:00
 *   pageCount: 10
 *   title: "Report 2025"
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see <a href="https://tika.apache.org/">Apache Tika</a>
 */
@Component
public class TikaMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(TikaMetadataExtractor.class);

    private final Parser parser;

    /**
     * 생성자
     */
    public TikaMetadataExtractor() {
        this.parser = new AutoDetectParser();  // 자동 포맷 감지
    }

    /**
     * 메타데이터 추출
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Apache Tika로 메타데이터 파싱</li>
     *   <li>Content-Type 기반 메타데이터 정규화</li>
     *   <li>타입별 메타데이터 추출 (이미지/비디오/문서)</li>
     *   <li>Map&lt;String, Object&gt; 형태로 반환</li>
     * </ol>
     *
     * @param inputStream 파일 InputStream
     * @param contentType Content-Type (예: "image/jpeg")
     * @return 추출된 메타데이터 (없으면 빈 Map)
     */
    public Map<String, Object> extract(InputStream inputStream, String contentType) {
        log.debug("Extracting metadata: contentType={}", contentType);

        try {
            // 1. Tika로 메타데이터 파싱
            Metadata tikaMetadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1);  // 전체 텍스트 (제한 없음)
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, tikaMetadata, context);

            // 2. Content-Type 기반 메타데이터 정규화
            Map<String, Object> metadata = new HashMap<>();

            if (contentType.startsWith("image/")) {
                // 이미지 메타데이터
                extractImageMetadata(tikaMetadata, metadata);

            } else if (contentType.startsWith("video/")) {
                // 비디오 메타데이터
                extractVideoMetadata(tikaMetadata, metadata);

            } else if (contentType.contains("pdf") || contentType.contains("document")) {
                // 문서 메타데이터
                extractDocumentMetadata(tikaMetadata, metadata);

            } else {
                // 기본 메타데이터 (모든 파일 타입)
                extractCommonMetadata(tikaMetadata, metadata);
            }

            log.debug("Metadata extracted: count={}", metadata.size());

            return metadata;

        } catch (IOException | SAXException | TikaException e) {
            // 메타데이터 추출 실패 - 빈 Map 반환 (예외 없음)
            log.warn("Failed to extract metadata: contentType={}, error={}",
                contentType, e.getMessage());

            return new HashMap<>();
        }
    }

    /**
     * 이미지 메타데이터 추출 (EXIF)
     *
     * <p><strong>추출 메타데이터:</strong></p>
     * <ul>
     *   <li>width, height: 이미지 크기</li>
     *   <li>takenAt: 촬영 날짜 (EXIF DateTimeOriginal)</li>
     *   <li>gpsLatitude, gpsLongitude: GPS 좌표</li>
     *   <li>cameraModel: 카메라 모델</li>
     *   <li>iso, fNumber, exposureTime: 촬영 정보</li>
     * </ul>
     */
    private void extractImageMetadata(Metadata tikaMetadata, Map<String, Object> metadata) {
        // 이미지 크기
        String width = tikaMetadata.get("Image Width");
        String height = tikaMetadata.get("Image Height");
        if (width != null) metadata.put("width", parseInteger(width));
        if (height != null) metadata.put("height", parseInteger(height));

        // 촬영 날짜 (EXIF)
        String dateTimeOriginal = tikaMetadata.get("Date/Time Original");
        if (dateTimeOriginal != null) {
            metadata.put("takenAt", parseDateTime(dateTimeOriginal));
        }

        // GPS 좌표
        String gpsLatitude = tikaMetadata.get("GPS Latitude");
        String gpsLongitude = tikaMetadata.get("GPS Longitude");
        if (gpsLatitude != null) metadata.put("gpsLatitude", parseDouble(gpsLatitude));
        if (gpsLongitude != null) metadata.put("gpsLongitude", parseDouble(gpsLongitude));

        // 카메라 정보
        String cameraModel = tikaMetadata.get("Model");
        if (cameraModel != null) metadata.put("cameraModel", cameraModel);

        // 촬영 정보
        String iso = tikaMetadata.get("ISO Speed Ratings");
        String fNumber = tikaMetadata.get("F-Number");
        String exposureTime = tikaMetadata.get("Exposure Time");

        if (iso != null) metadata.put("iso", parseInteger(iso));
        if (fNumber != null) metadata.put("fNumber", parseDouble(fNumber));
        if (exposureTime != null) metadata.put("exposureTime", exposureTime);

        log.debug("Image metadata extracted: width={}, height={}, camera={}",
            width, height, cameraModel);
    }

    /**
     * 비디오 메타데이터 추출
     *
     * <p><strong>추출 메타데이터:</strong></p>
     * <ul>
     *   <li>duration: 재생 시간 (초)</li>
     *   <li>width, height: 해상도</li>
     *   <li>codec: 비디오 코덱</li>
     *   <li>frameRate: 프레임 레이트 (FPS)</li>
     *   <li>bitrate: 비트레이트 (bps)</li>
     * </ul>
     */
    private void extractVideoMetadata(Metadata tikaMetadata, Map<String, Object> metadata) {
        // 재생 시간
        String duration = tikaMetadata.get(XMPDM.DURATION);
        if (duration != null) {
            metadata.put("duration", parseDouble(duration) / 1000.0);  // ms → s
        }

        // 해상도
        String width = tikaMetadata.get("width");
        String height = tikaMetadata.get("height");
        if (width != null) metadata.put("width", parseInteger(width));
        if (height != null) metadata.put("height", parseInteger(height));

        // 코덱
        String codec = tikaMetadata.get("X-Parsed-By");
        if (codec != null) metadata.put("codec", codec);

        // 프레임 레이트
        String frameRate = tikaMetadata.get("videoframerate");
        if (frameRate != null) metadata.put("frameRate", parseDouble(frameRate));

        log.debug("Video metadata extracted: duration={}, resolution={}x{}",
            duration, width, height);
    }

    /**
     * 문서 메타데이터 추출
     *
     * <p><strong>추출 메타데이터:</strong></p>
     * <ul>
     *   <li>author: 작성자</li>
     *   <li>createdAt: 생성일</li>
     *   <li>pageCount: 페이지 수</li>
     *   <li>title: 제목</li>
     * </ul>
     */
    private void extractDocumentMetadata(Metadata tikaMetadata, Map<String, Object> metadata) {
        // 작성자
        String author = tikaMetadata.get(TikaCoreProperties.CREATOR);
        if (author != null) metadata.put("author", author);

        // 생성일
        String created = tikaMetadata.get(TikaCoreProperties.CREATED);
        if (created != null) metadata.put("createdAt", parseDateTime(created));

        // 페이지 수
        String pageCount = tikaMetadata.get("xmpTPg:NPages");
        if (pageCount != null) metadata.put("pageCount", parseInteger(pageCount));

        // 제목
        String title = tikaMetadata.get(TikaCoreProperties.TITLE);
        if (title != null) metadata.put("title", title);

        log.debug("Document metadata extracted: author={}, pages={}, title={}",
            author, pageCount, title);
    }

    /**
     * 공통 메타데이터 추출 (모든 파일 타입)
     *
     * <p><strong>추출 메타데이터:</strong></p>
     * <ul>
     *   <li>contentType: MIME Type</li>
     *   <li>created: 생성일</li>
     *   <li>modified: 수정일</li>
     * </ul>
     */
    private void extractCommonMetadata(Metadata tikaMetadata, Map<String, Object> metadata) {
        // Content-Type
        String contentType = tikaMetadata.get(Metadata.CONTENT_TYPE);
        if (contentType != null) metadata.put("contentType", contentType);

        // 생성일
        String created = tikaMetadata.get(TikaCoreProperties.CREATED);
        if (created != null) metadata.put("created", parseDateTime(created));

        // 수정일
        String modified = tikaMetadata.get(TikaCoreProperties.MODIFIED);
        if (modified != null) metadata.put("modified", parseDateTime(modified));

        log.debug("Common metadata extracted: contentType={}", contentType);
    }

    /**
     * 정수 파싱 (오류 시 null)
     */
    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: value={}", value);
            return null;
        }
    }

    /**
     * 실수 파싱 (오류 시 null)
     */
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double: value={}", value);
            return null;
        }
    }

    /**
     * 날짜 파싱 (오류 시 null)
     *
     * <p><strong>지원 포맷:</strong></p>
     * <ul>
     *   <li>ISO 8601: "2025-01-01T10:00:00"</li>
     *   <li>EXIF: "2025:01:01 10:00:00"</li>
     * </ul>
     */
    private LocalDateTime parseDateTime(String value) {
        try {
            // ISO 8601 포맷 시도
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);

        } catch (DateTimeParseException e1) {
            try {
                // EXIF 포맷 시도 (2025:01:01 10:00:00 → 2025-01-01T10:00:00)
                String normalized = value.substring(0, 10).replace(":", "-") + "T" + value.substring(11);
                return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_DATE_TIME);

            } catch (DateTimeParseException e2) {
                log.warn("Failed to parse datetime: value={}", value);
                return null;
            }
        }
    }
}
