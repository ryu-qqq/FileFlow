package com.ryuqq.fileflow.application.settings.service;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.dto.SettingResponse;
import com.ryuqq.fileflow.application.settings.dto.UpdateSettingCommand;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.settings.InvalidSettingException;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingNotFoundException;
import com.ryuqq.fileflow.domain.settings.SettingRepository;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Update Setting Use Case
 *
 * <p>설정 값을 업데이트하는 UseCase입니다.</p>
 * <p>JSON 스키마 검증을 거쳐 설정 값을 업데이트합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ UseCase 패턴 - 하나의 비즈니스 유스케이스</li>
 *   <li>✅ Transaction 경계 - 쓰기 트랜잭션</li>
 *   <li>✅ Schema 검증 - SchemaValidator Port 활용</li>
 *   <li>✅ Domain 규칙 강제 - Setting Aggregate 메서드 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
@Service
@Transactional
public class UpdateSettingUseCase {

    private final SettingRepository settingRepository;
    private final SchemaValidator schemaValidator;
    private final SettingAssembler settingAssembler;

    /**
     * UpdateSettingUseCase 생성자.
     *
     * <p>생성자 주입을 통한 의존성 주입입니다.</p>
     *
     * @param settingRepository Setting Repository
     * @param schemaValidator Schema Validator Port
     * @param settingAssembler Setting Assembler
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public UpdateSettingUseCase(
        SettingRepository settingRepository,
        SchemaValidator schemaValidator,
        SettingAssembler settingAssembler
    ) {
        this.settingRepository = settingRepository;
        this.schemaValidator = schemaValidator;
        this.settingAssembler = settingAssembler;
    }

    /**
     * 설정을 업데이트합니다.
     *
     * <p>업데이트 절차:</p>
     * <ol>
     *   <li>Command 검증</li>
     *   <li>기존 Setting 조회</li>
     *   <li>JSON 스키마 검증 (필요 시)</li>
     *   <li>Setting 업데이트 (Domain 메서드)</li>
     *   <li>Repository 저장</li>
     *   <li>Response 반환</li>
     * </ol>
     *
     * <p>쓰기 트랜잭션으로 실행됩니다.</p>
     *
     * @param command 업데이트 Command
     * @return 업데이트된 Setting Response
     * @throws IllegalArgumentException Command가 유효하지 않은 경우
     * @throws SettingNotFoundException Setting을 찾을 수 없는 경우
     * @throws InvalidSettingException 스키마 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public SettingResponse execute(UpdateSettingCommand command) {
        validateCommand(command);

        // Command → Domain Value Object 변환
        SettingKey key = SettingKey.of(command.getKey());
        SettingLevel level = SettingLevel.valueOf(command.getLevel().toUpperCase());
        Long contextId = command.getContextId();

        // 기존 Setting 조회
        Setting setting = settingRepository.findByKeyAndLevel(key, level, contextId)
            .orElseThrow(() -> SettingNotFoundException.withKeyAndLevel(key, level, contextId));

        // 새로운 값 생성 및 검증
        SettingType type = setting.getValue().getType();
        validateValue(command.getValue(), type);

        SettingValue newValue = setting.getValue().isSecret()
            ? SettingValue.secret(command.getValue(), type)
            : SettingValue.of(command.getValue(), type);

        // Domain 메서드를 통한 업데이트 (비즈니스 규칙 강제)
        setting.updateValue(newValue);

        // Repository 저장
        Setting savedSetting = settingRepository.save(setting);

        // Assembler를 통한 DTO 변환
        return settingAssembler.toResponse(savedSetting);
    }

    /**
     * Command의 유효성을 검증합니다.
     *
     * @param command 업데이트 Command
     * @throws IllegalArgumentException Command가 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private void validateCommand(UpdateSettingCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command는 필수입니다");
        }
        if (command.getKey() == null || command.getKey().isBlank()) {
            throw new IllegalArgumentException("설정 키는 필수입니다");
        }
        if (command.getValue() == null) {
            throw new IllegalArgumentException("설정 값은 필수입니다");
        }
        if (command.getLevel() == null || command.getLevel().isBlank()) {
            throw new IllegalArgumentException("설정 레벨은 필수입니다");
        }
    }

    /**
     * 설정 값을 검증합니다.
     *
     * <p>JSON 타입의 경우 SchemaValidator를 통해 검증합니다.</p>
     *
     * @param value 설정 값
     * @param type 설정 타입
     * @throws InvalidSettingException 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private void validateValue(String value, SettingType type) {
        boolean isValid = schemaValidator.validate(value, type);
        if (!isValid) {
            throw new IllegalArgumentException(
                "설정 값이 타입과 호환되지 않습니다. 타입: " + type + ", 값: " + value
            );
        }
    }
}
