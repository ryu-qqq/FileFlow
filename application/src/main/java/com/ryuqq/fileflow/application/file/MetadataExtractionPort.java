package com.ryuqq.fileflow.application.file;

import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.io.InputStream;
import java.util.List;

/**
 * 파일로부터 메타데이터를 추출하는 Outbound Port 인터페이스
 *
 * 목적:
 * - Application Layer에서 파일 메타데이터 추출 기능을 요청하기 위한 추상 인터페이스
 * - 구현체는 Adapter Layer에 존재 (Hexagonal Architecture)
 *
 * 지원 파일 타입:
 * - 이미지: JPEG, PNG, GIF, TIFF 등
 * - 비디오: MP4, AVI, MOV 등 (추후 확장)
 * - 문서: PDF, DOCX, PPTX 등 (추후 확장)
 *
 * 추출 메타데이터 예시:
 * - 이미지: width, height, format, color_space, has_alpha, exif_make, exif_model
 * - 비디오: duration, width, height, codec, bitrate (추후 확장)
 * - 문서: page_count, author, title, created_date (추후 확장)
 *
 * @author sangwon-ryu
 */
public interface MetadataExtractionPort {

    /**
     * 파일 스트림으로부터 메타데이터를 추출합니다.
     *
     * @param fileId 파일 ID
     * @param inputStream 파일 입력 스트림
     * @param contentType 파일의 Content-Type (예: "image/jpeg", "video/mp4")
     * @return 추출된 메타데이터 리스트
     * @throws MetadataExtractionException 메타데이터 추출 중 오류 발생 시
     */
    List<FileMetadata> extractMetadata(FileId fileId, InputStream inputStream, String contentType);

    /**
     * 특정 Content-Type의 메타데이터 추출을 지원하는지 확인합니다.
     *
     * @param contentType 파일의 Content-Type
     * @return 지원 여부
     */
    boolean supports(String contentType);
}
