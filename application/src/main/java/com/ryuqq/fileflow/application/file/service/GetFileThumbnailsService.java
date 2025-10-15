package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.GetFileThumbnailsUseCase;
import com.ryuqq.fileflow.application.file.port.out.LoadFileRelationshipPort;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.ThumbnailMetadata;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 파일 썸네일 조회 Service
 *
 * 책임:
 * - 파일의 모든 썸네일 관계 조회
 * - 썸네일 메타데이터 변환 및 응답 생성
 *
 * @author sangwon-ryu
 */
@Service
public class GetFileThumbnailsService implements GetFileThumbnailsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetFileThumbnailsService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LoadFileRelationshipPort loadFileRelationshipPort;

    public GetFileThumbnailsService(LoadFileRelationshipPort loadFileRelationshipPort) {
        this.loadFileRelationshipPort = loadFileRelationshipPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThumbnailResponse> getThumbnails(FileId fileId) {
        log.info("Getting thumbnails for file: {}", fileId);

        List<FileRelationship> relationships = loadFileRelationshipPort.findBySourceFileId(fileId);

        List<ThumbnailResponse> thumbnails = relationships.stream()
                .filter(FileRelationship::isThumbnail)
                .map(this::toThumbnailResponse)
                .toList();

        log.info("Found {} thumbnails for file: {}", thumbnails.size(), fileId);

        return thumbnails;
    }

    private ThumbnailResponse toThumbnailResponse(FileRelationship relationship) {
        ThumbnailMetadata metadata = ThumbnailMetadata.fromMap(
                relationship.getRelationshipMetadata()
        );

        return new ThumbnailResponse(
                relationship.getId(),
                relationship.getTargetFileId(),
                relationship.getRelationshipType().getCode(),
                metadata.getWidth(),
                metadata.getHeight(),
                metadata.getAlgorithm(),
                metadata.getCreatedAt().format(DATE_FORMATTER)
        );
    }
}
