package com.ryuqq.fileflow.adapter.out.image.processor;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Thumbnailator Image Processor
 *
 * <p>Thumbnailator 라이브러리를 사용한 이미지 리사이징 처리기입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>이미지 리사이징 (비율 유지 또는 강제 크기)</li>
 *   <li>이미지 압축 (JPEG 품질 조정)</li>
 *   <li>포맷 변환 (PNG → JPEG)</li>
 * </ul>
 *
 * <p><strong>Thumbnailator 특징:</strong></p>
 * <ul>
 *   <li>간단한 API (Fluent Interface)</li>
 *   <li>빠른 성능 (Java 2D 기반)</li>
 *   <li>다양한 포맷 지원 (JPEG, PNG, GIF, BMP)</li>
 *   <li>비율 유지 리사이징</li>
 *   <li>품질 조정 (0.0 ~ 1.0)</li>
 * </ul>
 *
 * <p><strong>처리 예시:</strong></p>
 * <pre>
 * 원본: 4000x3000, 5MB PNG
 *   ↓ 리사이징 (300x300, 비율 유지)
 *   ↓ 압축 (JPEG 85%)
 * 결과: 300x300, 50KB JPEG
 * </pre>
 *
 * <p><strong>품질 설정:</strong></p>
 * <ul>
 *   <li>0.85 (85%): 시각적 품질과 파일 크기 균형 (기본값)</li>
 *   <li>0.9 (90%): 높은 품질 (파일 크기 큼)</li>
 *   <li>0.7 (70%): 낮은 품질 (파일 크기 작음)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see <a href="https://github.com/coobird/thumbnailator">Thumbnailator GitHub</a>
 */
@Component
public class ThumbnailatorImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailatorImageProcessor.class);

    private static final double JPEG_QUALITY = 0.85;  // 85% 품질
    private static final String OUTPUT_FORMAT = "jpg";  // JPEG 포맷

    /**
     * 썸네일 생성 (정사각형, 비율 유지)
     *
     * <p><strong>처리 방식:</strong></p>
     * <ul>
     *   <li>원본 비율 유지하며 리사이징</li>
     *   <li>크기에 맞게 자동 크롭 (중앙 정렬)</li>
     *   <li>JPEG 포맷으로 변환</li>
     *   <li>품질 85%로 압축</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>
     * 원본: 4000x3000 (가로가 더 큼)
     *   ↓ 300x300 정사각형으로 리사이징
     *   → 가로 크롭: 300x300 (중앙 부분만)
     *
     * 원본: 3000x4000 (세로가 더 큼)
     *   ↓ 300x300 정사각형으로 리사이징
     *   → 세로 크롭: 300x300 (중앙 부분만)
     * </pre>
     *
     * @param inputStream 원본 이미지 InputStream
     * @param width       썸네일 너비 (픽셀)
     * @param height      썸네일 높이 (픽셀)
     * @return 썸네일 이미지 (JPEG 바이트 배열)
     * @throws RuntimeException 이미지 처리 실패 시
     */
    public byte[] createThumbnail(InputStream inputStream, int width, int height) {
        log.debug("Creating thumbnail: targetSize={}x{}, quality={}, format={}",
            width, height, JPEG_QUALITY, OUTPUT_FORMAT);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Thumbnailator로 리사이징 + 압축
            Thumbnails.of(inputStream)
                .size(width, height)                    // 목표 크기
                .outputFormat(OUTPUT_FORMAT)             // JPEG 포맷
                .outputQuality(JPEG_QUALITY)             // 85% 품질
                .toOutputStream(outputStream);           // ByteArrayOutputStream으로 출력

            byte[] thumbnailBytes = outputStream.toByteArray();

            log.debug("Thumbnail created: size={}KB", thumbnailBytes.length / 1024);

            return thumbnailBytes;

        } catch (IOException e) {
            log.error("Failed to create thumbnail: targetSize={}x{}", width, height, e);

            throw new RuntimeException(
                "Thumbnail creation failed: targetSize=" + width + "x" + height,
                e
            );
        }
    }

    /**
     * 썸네일 생성 (비율 유지, 크기 제한)
     *
     * <p><strong>처리 방식:</strong></p>
     * <ul>
     *   <li>원본 비율 유지</li>
     *   <li>최대 너비/높이 제한</li>
     *   <li>비율에 따라 자동 조정</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>
     * 원본: 4000x3000 (4:3 비율)
     *   ↓ maxWidth=800, maxHeight=600
     *   → 결과: 800x600 (4:3 비율 유지)
     *
     * 원본: 3000x4000 (3:4 비율)
     *   ↓ maxWidth=800, maxHeight=600
     *   → 결과: 450x600 (3:4 비율 유지, 높이 기준)
     * </pre>
     *
     * @param inputStream 원본 이미지 InputStream
     * @param maxWidth    최대 너비 (픽셀)
     * @param maxHeight   최대 높이 (픽셀)
     * @return 썸네일 이미지 (JPEG 바이트 배열)
     * @throws RuntimeException 이미지 처리 실패 시
     */
    public byte[] createThumbnailKeepAspectRatio(
        InputStream inputStream,
        int maxWidth,
        int maxHeight
    ) {
        log.debug("Creating thumbnail (keep aspect ratio): maxSize={}x{}, quality={}, format={}",
            maxWidth, maxHeight, JPEG_QUALITY, OUTPUT_FORMAT);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Thumbnailator로 리사이징 + 압축 (비율 유지)
            Thumbnails.of(inputStream)
                .size(maxWidth, maxHeight)               // 최대 크기 (비율 유지)
                .outputFormat(OUTPUT_FORMAT)             // JPEG 포맷
                .outputQuality(JPEG_QUALITY)             // 85% 품질
                .toOutputStream(outputStream);           // ByteArrayOutputStream으로 출력

            byte[] thumbnailBytes = outputStream.toByteArray();

            log.debug("Thumbnail created (keep aspect ratio): size={}KB",
                thumbnailBytes.length / 1024);

            return thumbnailBytes;

        } catch (IOException e) {
            log.error("Failed to create thumbnail (keep aspect ratio): maxSize={}x{}",
                maxWidth, maxHeight, e);

            throw new RuntimeException(
                "Thumbnail creation failed (keep aspect ratio): maxSize=" +
                maxWidth + "x" + maxHeight,
                e
            );
        }
    }

    /**
     * 이미지 리사이징 (스케일 비율)
     *
     * <p><strong>처리 방식:</strong></p>
     * <ul>
     *   <li>원본 크기의 배율로 리사이징</li>
     *   <li>예: 0.5 → 원본의 50% 크기</li>
     *   <li>비율 유지</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>
     * 원본: 4000x3000
     *   ↓ scale=0.5 (50%)
     *   → 결과: 2000x1500
     *
     * 원본: 3000x4000
     *   ↓ scale=0.25 (25%)
     *   → 결과: 750x1000
     * </pre>
     *
     * @param inputStream 원본 이미지 InputStream
     * @param scale       스케일 비율 (0.0 ~ 1.0, 예: 0.5 = 50%)
     * @return 리사이징된 이미지 (JPEG 바이트 배열)
     * @throws RuntimeException         이미지 처리 실패 시
     * @throws IllegalArgumentException scale이 0.0 ~ 1.0 범위 밖일 때
     */
    public byte[] resizeByScale(InputStream inputStream, double scale) {
        if (scale <= 0.0 || scale > 1.0) {
            throw new IllegalArgumentException(
                "Scale must be between 0.0 and 1.0: " + scale
            );
        }

        log.debug("Resizing image by scale: scale={}, quality={}, format={}",
            scale, JPEG_QUALITY, OUTPUT_FORMAT);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Thumbnailator로 스케일 리사이징
            Thumbnails.of(inputStream)
                .scale(scale)                            // 스케일 비율
                .outputFormat(OUTPUT_FORMAT)             // JPEG 포맷
                .outputQuality(JPEG_QUALITY)             // 85% 품질
                .toOutputStream(outputStream);           // ByteArrayOutputStream으로 출력

            byte[] resizedBytes = outputStream.toByteArray();

            log.debug("Image resized by scale: size={}KB", resizedBytes.length / 1024);

            return resizedBytes;

        } catch (IOException e) {
            log.error("Failed to resize image by scale: scale={}", scale, e);

            throw new RuntimeException(
                "Image resize failed: scale=" + scale,
                e
            );
        }
    }
}
