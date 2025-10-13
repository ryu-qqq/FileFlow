package com.ryuqq.fileflow.adapter.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.ryuqq.fileflow.application.file.MetadataExtractionException;
import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.file.MetadataType;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 이미지 파일로부터 메타데이터를 추출하는 클래스
 *
 * 사용 라이브러리:
 * - metadata-extractor: EXIF, IPTC, XMP 등 이미지 메타데이터 추출
 *
 * 추출 메타데이터:
 * - width: 이미지 폭
 * - height: 이미지 높이
 * - format: 이미지 포맷 (JPEG, PNG 등)
 * - color_space: 색 공간 (RGB, CMYK 등)
 * - has_alpha: 알파 채널 존재 여부
 * - exif_make: 카메라 제조사 (EXIF)
 * - exif_model: 카메라 모델 (EXIF)
 * - exif_datetime: 촬영 일시 (EXIF)
 * - exif_orientation: 이미지 방향 (EXIF)
 * - exif_gps_latitude: GPS 위도 (EXIF)
 * - exif_gps_longitude: GPS 경도 (EXIF)
 *
 * @author sangwon-ryu
 */
public class ImageMetadataExtractor {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/tiff",
            "image/bmp",
            "image/webp"
    );

    /**
     * Content-Type이 이미지인지 확인합니다.
     *
     * @param contentType Content-Type
     * @return 이미지 여부
     */
    public boolean supports(String contentType) {
        if (contentType == null) {
            return false;
        }
        return SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * 이미지 스트림으로부터 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param inputStream 이미지 입력 스트림
     * @param contentType Content-Type
     * @return 추출된 메타데이터 리스트
     * @throws MetadataExtractionException 추출 실패 시
     */
    public List<FileMetadata> extract(FileId fileId, InputStream inputStream, String contentType) {
        if (!supports(contentType)) {
            throw new MetadataExtractionException(
                    "Unsupported content type for image extraction: " + contentType
            );
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            return extractAllMetadata(fileId, metadata, contentType);
        } catch (ImageProcessingException | IOException e) {
            throw new MetadataExtractionException(
                    "Failed to extract image metadata for fileId: " + fileId, e
            );
        }
    }

    /**
     * 추출된 메타데이터를 FileMetadata 리스트로 변환합니다.
     *
     * @param fileId 파일 ID
     * @param metadata 추출된 메타데이터
     * @param contentType Content-Type
     * @return FileMetadata 리스트
     */
    private List<FileMetadata> extractAllMetadata(FileId fileId, Metadata metadata, String contentType) {
        List<FileMetadata> results = new ArrayList<>();

        // 기본 메타데이터: format
        results.add(createMetadata(fileId, "format", extractFormat(contentType)));

        // JPEG 전용 메타데이터
        if (isJpeg(contentType)) {
            extractJpegMetadata(fileId, metadata, results);
        }

        // PNG 전용 메타데이터
        if (isPng(contentType)) {
            extractPngMetadata(fileId, metadata, results);
        }

        // EXIF 메타데이터 (JPEG, TIFF 등)
        extractExifMetadata(fileId, metadata, results);

        // GPS 메타데이터 (EXIF GPS)
        extractGpsMetadata(fileId, metadata, results);

        return results;
    }

    /**
     * JPEG 전용 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param metadata 메타데이터
     * @param results 결과 리스트
     */
    private void extractJpegMetadata(FileId fileId, Metadata metadata, List<FileMetadata> results) {
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpegDirectory == null) {
            return;
        }

        try {
            // width, height
            Integer width = jpegDirectory.getImageWidth();
            if (width != null) {
                results.add(createMetadata(fileId, "width", width.toString(), MetadataType.NUMBER));
            }

            Integer height = jpegDirectory.getImageHeight();
            if (height != null) {
                results.add(createMetadata(fileId, "height", height.toString(), MetadataType.NUMBER));
            }

            // component_count (색상 채널 수)
            Integer componentCount = jpegDirectory.getNumberOfComponents();
            if (componentCount != null) {
                results.add(createMetadata(fileId, "component_count", componentCount.toString(), MetadataType.NUMBER));
            }
        } catch (Exception e) {
            // JPEG 메타데이터 추출 실패 시 무시 (다른 메타데이터는 계속 추출)
        }
    }

    /**
     * PNG 전용 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param metadata 메타데이터
     * @param results 결과 리스트
     */
    private void extractPngMetadata(FileId fileId, Metadata metadata, List<FileMetadata> results) {
        PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
        if (pngDirectory == null) {
            return;
        }

        // width, height
        Integer width = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
        if (width != null) {
            results.add(createMetadata(fileId, "width", width.toString(), MetadataType.NUMBER));
        }

        Integer height = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
        if (height != null) {
            results.add(createMetadata(fileId, "height", height.toString(), MetadataType.NUMBER));
        }

        // color_type
        Integer colorType = pngDirectory.getInteger(PngDirectory.TAG_COLOR_TYPE);
        if (colorType != null) {
            String colorTypeName = getPngColorTypeName(colorType);
            results.add(createMetadata(fileId, "color_type", colorTypeName));
        }

        // has_alpha (color type 4 또는 6이면 알파 채널 존재)
        if (colorType != null && (colorType == 4 || colorType == 6)) {
            results.add(createMetadata(fileId, "has_alpha", "true", MetadataType.BOOLEAN));
        }
    }

    /**
     * EXIF 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param metadata 메타데이터
     * @param results 결과 리스트
     */
    private void extractExifMetadata(FileId fileId, Metadata metadata, List<FileMetadata> results) {
        ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exifDirectory == null) {
            return;
        }

        // 카메라 제조사
        String make = exifDirectory.getString(ExifIFD0Directory.TAG_MAKE);
        if (make != null) {
            results.add(createMetadata(fileId, "exif_make", make.trim()));
        }

        // 카메라 모델
        String model = exifDirectory.getString(ExifIFD0Directory.TAG_MODEL);
        if (model != null) {
            results.add(createMetadata(fileId, "exif_model", model.trim()));
        }

        // 촬영 일시
        String datetime = exifDirectory.getString(ExifIFD0Directory.TAG_DATETIME);
        if (datetime != null) {
            results.add(createMetadata(fileId, "exif_datetime", datetime.trim()));
        }

        // 소프트웨어
        String software = exifDirectory.getString(ExifIFD0Directory.TAG_SOFTWARE);
        if (software != null) {
            results.add(createMetadata(fileId, "exif_software", software.trim()));
        }

        // 이미지 방향 (Orientation)
        Integer orientation = exifDirectory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
        if (orientation != null) {
            results.add(createMetadata(fileId, "exif_orientation", orientation.toString(), MetadataType.NUMBER));
        }
    }

    /**
     * GPS 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param metadata 메타데이터
     * @param results 결과 리스트
     */
    private void extractGpsMetadata(FileId fileId, Metadata metadata, List<FileMetadata> results) {
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory == null) {
            return;
        }

        try {
            // GPS 위도
            if (gpsDirectory.getGeoLocation() != null) {
                Double latitude = gpsDirectory.getGeoLocation().getLatitude();
                if (latitude != null) {
                    results.add(createMetadata(fileId, "exif_gps_latitude", latitude.toString(), MetadataType.NUMBER));
                }

                // GPS 경도
                Double longitude = gpsDirectory.getGeoLocation().getLongitude();
                if (longitude != null) {
                    results.add(createMetadata(fileId, "exif_gps_longitude", longitude.toString(), MetadataType.NUMBER));
                }
            }

            // GPS 고도 (선택적) - Rational 타입으로 저장될 수 있으므로 안전하게 처리
            Double altitude = gpsDirectory.getDoubleObject(GpsDirectory.TAG_ALTITUDE);
            if (altitude != null) {
                results.add(createMetadata(fileId, "exif_gps_altitude", altitude.toString(), MetadataType.NUMBER));
            }
        } catch (Exception e) {
            // GPS 메타데이터 추출 실패 시 무시 (다른 메타데이터는 계속 추출)
        }
    }

    /**
     * PNG Color Type 숫자를 이름으로 변환합니다.
     *
     * @param colorType PNG Color Type 값
     * @return Color Type 이름
     */
    private String getPngColorTypeName(int colorType) {
        switch (colorType) {
            case 0:
                return "Grayscale";
            case 2:
                return "RGB";
            case 3:
                return "Indexed";
            case 4:
                return "Grayscale with Alpha";
            case 6:
                return "RGB with Alpha";
            default:
                return "Unknown";
        }
    }

    /**
     * Content-Type으로부터 포맷 문자열을 추출합니다.
     *
     * @param contentType Content-Type
     * @return 포맷 문자열 (예: "JPEG", "PNG")
     */
    private String extractFormat(String contentType) {
        if (contentType == null) {
            return "UNKNOWN";
        }

        String normalized = contentType.toLowerCase();
        if (normalized.contains("jpeg") || normalized.contains("jpg")) {
            return "JPEG";
        } else if (normalized.contains("png")) {
            return "PNG";
        } else if (normalized.contains("gif")) {
            return "GIF";
        } else if (normalized.contains("tiff")) {
            return "TIFF";
        } else if (normalized.contains("bmp")) {
            return "BMP";
        } else if (normalized.contains("webp")) {
            return "WEBP";
        }

        return contentType.toUpperCase();
    }

    /**
     * Content-Type이 JPEG인지 확인합니다.
     *
     * @param contentType Content-Type
     * @return JPEG 여부
     */
    private boolean isJpeg(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalized = contentType.toLowerCase();
        return normalized.contains("jpeg") || normalized.contains("jpg");
    }

    /**
     * Content-Type이 PNG인지 확인합니다.
     *
     * @param contentType Content-Type
     * @return PNG 여부
     */
    private boolean isPng(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase().contains("png");
    }

    /**
     * FileMetadata 인스턴스를 생성합니다 (STRING 타입).
     *
     * @param fileId 파일 ID
     * @param key 메타데이터 키
     * @param value 메타데이터 값
     * @return FileMetadata 인스턴스
     */
    private FileMetadata createMetadata(FileId fileId, String key, String value) {
        return createMetadata(fileId, key, value, MetadataType.STRING);
    }

    /**
     * FileMetadata 인스턴스를 생성합니다.
     *
     * @param fileId 파일 ID
     * @param key 메타데이터 키
     * @param value 메타데이터 값
     * @param type 메타데이터 타입
     * @return FileMetadata 인스턴스
     */
    private FileMetadata createMetadata(FileId fileId, String key, String value, MetadataType type) {
        return FileMetadata.create(fileId, key, value, type);
    }
}
