#!/bin/bash
# ============================================================
# S3 Key 마이그레이션 스크립트 (Stage 환경)
# ============================================================
# 목적: 기존 S3 오브젝트를 새 경로 패턴으로 복사
# 실행 순서: 이 스크립트 → V7_s3key_migration.sql → 검증 → 구 오브젝트 삭제
#
# 사용법:
#   1. DRY RUN (복사 안함, 목록만 출력):
#      ./s3_copy.sh --dry-run
#
#   2. 실행:
#      ./s3_copy.sh
#
#   3. 구 오브젝트 삭제 (DB 마이그레이션 완료 후):
#      ./s3_copy.sh --cleanup
# ============================================================

set -euo pipefail

BUCKET="${S3_BUCKET:-fileflow-stage}"
REGION="${AWS_REGION:-ap-northeast-2}"
DRY_RUN=false
CLEANUP=false
LOG_FILE="s3_migration_$(date +%Y%m%d_%H%M%S).log"

# DB 접속 정보 (환경변수에서 가져옴)
DB_HOST="${DB_HOST:-}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-fileflow}"
DB_USER="${DB_USER:-}"
DB_PASS="${DB_PASS:-}"

for arg in "$@"; do
    case $arg in
        --dry-run) DRY_RUN=true ;;
        --cleanup) CLEANUP=true ;;
    esac
done

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

run_query() {
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" \
        --batch --skip-column-names -e "$1"
}

# ============================================================
# Step 1: download_task의 기존 s3_key → 새 s3_key 매핑 생성
# ============================================================
generate_download_mappings() {
    log "=== download_task S3 키 매핑 생성 ==="

    local query="
        SELECT id, s3_key, access_type,
               YEAR(created_at), LPAD(MONTH(created_at), 2, '0'),
               SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(source_url, '?', 1), '#', 1), '.', -1) AS ext
        FROM download_task
        WHERE s3_key NOT LIKE 'public/%'
          AND s3_key NOT LIKE 'internal/%'
    "

    local count=0
    local errors=0

    while IFS=$'\t' read -r id old_key access_type year month ext; do
        # 확장자 유효성 검사 (알파벳+숫자만, 10자 이내)
        if [[ "$ext" =~ ^[a-zA-Z0-9]{1,10}$ ]]; then
            new_key="$(echo "$access_type" | tr '[:upper:]' '[:lower:]')/${year}/${month}/${id}.$(echo "$ext" | tr '[:upper:]' '[:lower:]')"
        else
            new_key="$(echo "$access_type" | tr '[:upper:]' '[:lower:]')/${year}/${month}/${id}"
        fi

        if $DRY_RUN; then
            log "[DRY RUN] COPY s3://${BUCKET}/${old_key} → s3://${BUCKET}/${new_key}"
        else
            if aws s3 cp "s3://${BUCKET}/${old_key}" "s3://${BUCKET}/${new_key}" \
                --region "$REGION" 2>>"$LOG_FILE"; then
                log "OK: ${old_key} → ${new_key}"
                ((count++))
            else
                log "ERROR: Failed to copy ${old_key}"
                ((errors++))
            fi
        fi
    done < <(run_query "$query")

    log "download_task 복사 완료: ${count}건 성공, ${errors}건 실패"
}

# ============================================================
# Step 2: transform 결과 asset의 기존 s3_key → 새 s3_key 매핑
# ============================================================
generate_transform_mappings() {
    log "=== transform asset S3 키 매핑 생성 ==="

    local query="
        SELECT a.id, a.s3_key, a.access_type, a.extension,
               YEAR(a.created_at), LPAD(MONTH(a.created_at), 2, '0')
        FROM asset a
        WHERE a.s3_key LIKE 'transformed/%'
          AND a.deleted_at IS NULL
    "

    local count=0
    local errors=0

    while IFS=$'\t' read -r id old_key access_type ext year month; do
        if [[ -n "$ext" && "$ext" != "NULL" ]]; then
            new_key="$(echo "$access_type" | tr '[:upper:]' '[:lower:]')/${year}/${month}/${id}.$(echo "$ext" | tr '[:upper:]' '[:lower:]')"
        else
            new_key="$(echo "$access_type" | tr '[:upper:]' '[:lower:]')/${year}/${month}/${id}"
        fi

        if $DRY_RUN; then
            log "[DRY RUN] COPY s3://${BUCKET}/${old_key} → s3://${BUCKET}/${new_key}"
        else
            if aws s3 cp "s3://${BUCKET}/${old_key}" "s3://${BUCKET}/${new_key}" \
                --region "$REGION" 2>>"$LOG_FILE"; then
                log "OK: ${old_key} → ${new_key}"
                ((count++))
            else
                log "ERROR: Failed to copy ${old_key}"
                ((errors++))
            fi
        fi
    done < <(run_query "$query")

    log "transform asset 복사 완료: ${count}건 성공, ${errors}건 실패"
}

# ============================================================
# Step 3: 구 오브젝트 삭제 (--cleanup 옵션)
# ============================================================
cleanup_old_objects() {
    log "=== 구 S3 오브젝트 삭제 ==="
    log "WARNING: DB 마이그레이션이 완료되었는지 반드시 확인하세요!"
    read -p "정말 삭제하시겠습니까? (yes/no): " confirm
    if [[ "$confirm" != "yes" ]]; then
        log "삭제 취소됨"
        exit 0
    fi

    # download_task에서 더 이상 참조하지 않는 옛 키 삭제
    local old_download_keys
    old_download_keys=$(aws s3 ls "s3://${BUCKET}/product-images/" --recursive \
        --region "$REGION" 2>/dev/null | awk '{print $4}')

    local count=0
    for key in $old_download_keys; do
        if aws s3 rm "s3://${BUCKET}/${key}" --region "$REGION" 2>>"$LOG_FILE"; then
            log "DELETED: ${key}"
            ((count++))
        fi
    done

    # transformed/ 접두어의 옛 오브젝트 삭제
    local old_transform_keys
    old_transform_keys=$(aws s3 ls "s3://${BUCKET}/transformed/" --recursive \
        --region "$REGION" 2>/dev/null | awk '{print $4}')

    for key in $old_transform_keys; do
        if aws s3 rm "s3://${BUCKET}/${key}" --region "$REGION" 2>>"$LOG_FILE"; then
            log "DELETED: ${key}"
            ((count++))
        fi
    done

    log "구 오브젝트 삭제 완료: ${count}건"
}

# ============================================================
# Main
# ============================================================
log "========================================="
log "S3 Key 마이그레이션 시작"
log "Bucket: ${BUCKET}"
log "Region: ${REGION}"
log "DRY_RUN: ${DRY_RUN}"
log "CLEANUP: ${CLEANUP}"
log "========================================="

if [[ -z "$DB_HOST" || -z "$DB_USER" ]]; then
    log "ERROR: DB 접속 정보가 설정되지 않았습니다."
    log "  export DB_HOST=xxx DB_USER=xxx DB_PASS=xxx"
    exit 1
fi

if $CLEANUP; then
    cleanup_old_objects
else
    generate_download_mappings
    generate_transform_mappings
    log "========================================="
    log "S3 복사 완료!"
    if $DRY_RUN; then
        log "DRY RUN 모드였습니다. 실제 복사하려면 --dry-run 옵션을 제거하세요."
    else
        log "다음 단계: V7_s3key_migration.sql 실행하여 DB 업데이트"
    fi
    log "========================================="
fi
