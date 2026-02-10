package com.ryuqq.fileflow.application.transform;

import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;

/**
 * Transform Application Command 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class TransformCommandFixtures {

    private TransformCommandFixtures() {}

    // ===== CreateTransformRequestCommand Fixtures =====

    public static CreateTransformRequestCommand createResizeCommand() {
        return new CreateTransformRequestCommand("asset-001", "RESIZE", 800, 600, null, null);
    }

    public static CreateTransformRequestCommand createConvertCommand() {
        return new CreateTransformRequestCommand("asset-001", "CONVERT", null, null, null, "webp");
    }

    public static CreateTransformRequestCommand createCompressCommand() {
        return new CreateTransformRequestCommand("asset-001", "COMPRESS", null, null, 80, null);
    }

    public static CreateTransformRequestCommand createThumbnailCommand() {
        return new CreateTransformRequestCommand("asset-001", "THUMBNAIL", 150, 150, null, null);
    }

    public static CreateTransformRequestCommand createResizeCommand(String sourceAssetId) {
        return new CreateTransformRequestCommand(sourceAssetId, "RESIZE", 800, 600, null, null);
    }

    // ===== RecoverZombieTransformRequestCommand Fixtures =====

    public static RecoverZombieTransformRequestCommand recoverZombieCommand() {
        return RecoverZombieTransformRequestCommand.of(100, 300);
    }

    public static RecoverZombieTransformRequestCommand recoverZombieCommand(
            int batchSize, long timeoutSeconds) {
        return RecoverZombieTransformRequestCommand.of(batchSize, timeoutSeconds);
    }
}
