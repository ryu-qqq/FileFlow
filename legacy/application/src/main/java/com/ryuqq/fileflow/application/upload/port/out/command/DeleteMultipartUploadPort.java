package com.ryuqq.fileflow.application.upload.port.out.command;

/**
 * Multipart Upload 삭제 Port (Command)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Command Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate 삭제</li>
 *   <li>CQRS Command 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Port (Write 전담)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface DeleteMultipartUploadPort {

    /**
     * Multipart Upload 삭제
     *
     * @param id Multipart Upload ID
     */
    void delete(Long id);
}

