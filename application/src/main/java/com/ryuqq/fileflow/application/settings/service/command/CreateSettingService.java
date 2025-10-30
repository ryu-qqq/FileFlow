package com.ryuqq.fileflow.application.settings.service.command;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort;
import com.ryuqq.fileflow.application.settings.port.out.SaveSettingPort;
import com.ryuqq.fileflow.domain.settings.exception.InvalidSettingException;
import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateSettingService - 설정 생성 Command Handler
 *
 * <p>새로운 설정을 생성하는 Command Handler 구현체입니다.</p>
 * <p>중복 검증과 JSON 스키마 검증을 거쳐 설정을 생성합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS 패턴 - Command Handler (쓰기 작업)</li>
 *   <li>✅ Hexagonal Architecture - Port 사용 (LoadSettingsPort, SaveSettingPort)</li>
 *   <li>✅ Transaction 경계 - 쓰기 트랜잭션</li>
 *   <li>✅ Schema 검증 - SchemaValidator Port 활용</li>
 *   <li>✅ Domain 규칙 강제 - Setting Aggregate 메서드 사용</li>
 *   <li>✅ Assembler 사용 - Domain ↔ DTO 변환</li>
 * </ul>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ @Transactional 명시 (Application Layer에서 Transaction 관리)</li>
 *   <li>✅ 외부 API 호출 절대 금지 (DB 작업만 포함)</li>
 *   <li>✅ Command당 하나의 Transaction</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Service
@Transactional
public class CreateSettingService implements CreateSettingUseCase {

    private final LoadSettingsPort loadSettingsPort;
    private final SaveSettingPort saveSettingPort;
    private final SchemaValidator schemaValidator;
    private final SettingAssembler settingAssembler;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param loadSettingsPort Load Settings Port (Query 전용 Outbound Port)
     * @param saveSettingPort Save Setting Port (Command 전용 Outbound Port)
     * @param schemaValidator Schema Validator Port
     * @param settingAssembler Setting Assembler
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public CreateSettingService(
        LoadSettingsPort loadSettingsPort,
        SaveSettingPort saveSettingPort,
        SchemaValidator schemaValidator,
        SettingAssembler settingAssembler
    ) {
        this.loadSettingsPort = loadSettingsPort;
        this.saveSettingPort = saveSettingPort;
        this.schemaValidator = schemaValidator;
        this.settingAssembler = settingAssembler;
    }

    /**
     * 설정 생성 UseCase 실행
     *
     * <p><strong>Transaction 경계:</strong></p>
     * <ul>
     *   <li>Transaction 시작: 메서드 진입 시</li>
     *   <li>Transaction 종료: 메서드 종료 시 (정상 Commit 또는 예외 시 Rollback)</li>
     *   <li>외부 API 호출: 없음 (DB 작업만 포함)</li>
     * </ul>
     *
     * <p><strong>실행 절차:</strong></p>
     * <ol>
     *   <li>Command → Domain Value Object 변환</li>
     *   <li>중복 검증 ((key, level, contextId) 복합 유니크)</li>
     *   <li>JSON 스키마 검증 (필요 시)</li>
     *   <li>Setting 생성 (Domain Factory 메서드)</li>
     *   <li>Repository 저장</li>
     *   <li>Assembler를 통한 Response 변환</li>
     * </ol>
     *
     * @param command 설정 생성 Command
     * @return Response 생성된 설정 정보
     * @throws IllegalArgumentException level 또는 valueType이 유효하지 않은 경우
     * @throws IllegalStateException 동일한 (key, level, contextId) 조합이 이미 존재하는 경우
     * @throws InvalidSettingException 스키마 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public Response execute(Command command) {
        // 1. Command → Domain Value Object 변환
        SettingKey key = SettingKey.of(command.key());
        SettingLevel level = SettingLevel.valueOf(command.level());
        SettingType type = SettingType.valueOf(command.valueType());
        Long contextId = command.contextId();

        // 2. 중복 검증 ((key, level, contextId) 복합 유니크 제약)
        boolean exists = loadSettingsPort.findByKeyAndLevel(key, level, contextId).isPresent();
        if (exists) {
            throw new IllegalStateException(
                String.format(
                    "이미 존재하는 설정입니다. key=%s, level=%s, contextId=%s",
                    key.getValue(),
                    level.name(),
                    contextId
                )
            );
        }

        // 3. JSON 스키마 검증
        validateValue(command.value(), type);

        // 4. Domain 생성 (Setting Factory 메서드)
        // secret 여부는: 1) 명시적 요청 우선, 2) 키 패턴 자동 판단
        boolean shouldBeSecret = command.secret() || key.isSecretKey();
        SettingValue value = shouldBeSecret
            ? SettingValue.secret(command.value(), type)
            : SettingValue.of(command.value(), type);

        Setting setting = Setting.forNew(
            key,
            value,
            level,
            contextId
        );

        // 5. Repository 저장 (Command Port 사용)
        Setting savedSetting = saveSettingPort.save(setting);

        // 6. Assembler를 통한 Response 변환
        return settingAssembler.toCreateResponse(savedSetting);
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
     * @since 2025-10-26
     */
    private void validateValue(String value, SettingType type) {
        boolean isValid = schemaValidator.validate(value, type);
        if (!isValid) {
            throw new InvalidSettingException(
                "설정 값이 타입과 호환되지 않습니다. 타입: " + type + ", 값: " + value
            );
        }
    }
}
