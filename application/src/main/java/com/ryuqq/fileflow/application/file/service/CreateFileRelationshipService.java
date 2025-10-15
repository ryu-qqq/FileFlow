package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.port.in.CreateFileRelationshipUseCase;
import com.ryuqq.fileflow.application.file.port.out.SaveFileRelationshipPort;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 관계 생성 Service
 *
 * 책임:
 * - 파일 관계 생성 비즈니스 로직 처리
 * - 도메인 객체 생성 및 영속화
 * - 트랜잭션 관리
 *
 * @author sangwon-ryu
 */
@Service
public class CreateFileRelationshipService implements CreateFileRelationshipUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateFileRelationshipService.class);

    private final SaveFileRelationshipPort saveFileRelationshipPort;

    public CreateFileRelationshipService(SaveFileRelationshipPort saveFileRelationshipPort) {
        this.saveFileRelationshipPort = saveFileRelationshipPort;
    }

    @Override
    @Transactional
    public FileRelationship createRelationship(CreateRelationshipCommand command) {
        log.info("Creating file relationship: source={}, target={}, type={}",
                command.sourceFileId(), command.targetFileId(), command.relationshipType());

        FileRelationship fileRelationship = FileRelationship.create(
                command.sourceFileId(),
                command.targetFileId(),
                command.relationshipType(),
                command.metadata()
        );

        FileRelationship savedRelationship = saveFileRelationshipPort.save(fileRelationship);

        log.info("File relationship created successfully: id={}", savedRelationship.getId());

        return savedRelationship;
    }
}
