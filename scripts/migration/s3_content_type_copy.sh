#!/bin/bash
# ============================================================
# V8: S3 Content-Type 수정 및 키 복사 스크립트
# ============================================================
# 실행 순서:
#   1. ./s3_content_type_copy.sh --dry-run     (대상 확인)
#   2. ./s3_content_type_copy.sh               (S3 복사)
#   3. V8_content_type_fix.sql 실행            (DB 업데이트)
#   4. ./s3_content_type_copy.sh --cleanup     (구 오브젝트 삭제)
#
# 환경변수:
#   S3_BUCKET, AWS_REGION, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
# ============================================================

set -euo pipefail

BUCKET="${S3_BUCKET:-fileflow-stage}"
REGION="${AWS_REGION:-ap-northeast-2}"
DRY_RUN=false
CLEANUP=false
LOG_FILE="s3_v8_migration_$(date +%Y%m%d_%H%M%S).log"
MAPPING_FILE="s3_v8_mapping.tsv"

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

run_sql_file() {
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" \
        --batch --skip-column-names < "$1"
}

# ============================================================
# Step 1: 매핑 파일 생성 (SQL 파일 사용으로 이스케이핑 문제 회피)
# ============================================================
generate_mapping() {
    log "=== S3 키 매핑 생성 ==="

    cat > /tmp/v8_generate_mapping.sql << 'EOSQL'
SELECT
    a.s3_key,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.jpeg')
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.png')
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.gif')
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.webp')
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.svg')
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.bmp')
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.tiff')
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.avif')
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.heic')
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN CONCAT(a.s3_key, '.pdf')
        ELSE a.s3_key
    END,
    CASE
        WHEN dt.source_url REGEXP '\\.(jpe?g)([/?#_/]|$)' THEN 'image/jpeg'
        WHEN dt.source_url REGEXP '\\.(png)([/?#_/]|$)' THEN 'image/png'
        WHEN dt.source_url REGEXP '\\.(gif)([/?#_/]|$)' THEN 'image/gif'
        WHEN dt.source_url REGEXP '\\.(webp)([/?#_/]|$)' THEN 'image/webp'
        WHEN dt.source_url REGEXP '\\.(svg)([/?#_/]|$)' THEN 'image/svg+xml'
        WHEN dt.source_url REGEXP '\\.(bmp)([/?#_/]|$)' THEN 'image/bmp'
        WHEN dt.source_url REGEXP '\\.(tiff?)([/?#_/]|$)' THEN 'image/tiff'
        WHEN dt.source_url REGEXP '\\.(avif)([/?#_/]|$)' THEN 'image/avif'
        WHEN dt.source_url REGEXP '\\.(heic)([/?#_/]|$)' THEN 'image/heic'
        WHEN dt.source_url REGEXP '\\.(pdf)([/?#_/]|$)' THEN 'application/pdf'
        ELSE 'application/octet-stream'
    END
FROM asset a
INNER JOIN download_task dt ON a.origin_id = dt.id
WHERE a.content_type = 'application/octet-stream'
  AND a.origin = 'EXTERNAL_DOWNLOAD'
  AND a.deleted_at IS NULL
  AND dt.source_url REGEXP '\\.(jpe?g|png|gif|webp|svg|bmp|tiff?|avif|heic|pdf)([/?#_/]|$)';
EOSQL

    run_sql_file /tmp/v8_generate_mapping.sql > "$MAPPING_FILE"
    rm -f /tmp/v8_generate_mapping.sql

    local total
    total=$(wc -l < "$MAPPING_FILE" | tr -d ' ')
    log "매핑 생성 완료: ${total}건 → ${MAPPING_FILE}"
}

# ============================================================
# Step 2: S3 복사 실행
# ============================================================
copy_objects() {
    log "=== S3 오브젝트 복사 시작 (${BUCKET}) ==="

    local count=0
    local errors=0
    local skipped=0
    local total
    total=$(wc -l < "$MAPPING_FILE" | tr -d ' ')

    while IFS=$'\t' read -r old_key new_key content_type; do
        if [[ "$old_key" == "$new_key" ]]; then
            ((skipped++))
            continue
        fi

        if $DRY_RUN; then
            log "[DRY RUN] COPY s3://${BUCKET}/${old_key} → ${new_key} (${content_type})"
        else
            if aws s3 cp "s3://${BUCKET}/${old_key}" "s3://${BUCKET}/${new_key}" \
                --content-type "$content_type" \
                --metadata-directive REPLACE \
                --region "$REGION" 2>>"$LOG_FILE"; then
                ((count++))
                if (( count % 100 == 0 )); then
                    log "진행: ${count}/${total}"
                fi
            else
                log "ERROR: COPY 실패 ${old_key}"
                ((errors++))
            fi
        fi
    done < "$MAPPING_FILE"

    log "복사 결과: 성공=${count} 실패=${errors} 스킵=${skipped} 전체=${total}"
}

# ============================================================
# Step 3: 구 오브젝트 삭제 (--cleanup)
# ============================================================
cleanup_old_objects() {
    log "=== 구 S3 오브젝트 삭제 ==="

    if [[ ! -f "$MAPPING_FILE" ]]; then
        MAPPING_FILE=$(ls -t s3_v8_mapping*.tsv 2>/dev/null | head -1)
        if [[ -z "$MAPPING_FILE" ]]; then
            log "ERROR: 매핑 파일 없음. 먼저 복사를 실행하세요."
            exit 1
        fi
    fi

    log "WARNING: DB 마이그레이션(V8_content_type_fix.sql)이 COMMIT 되었는지 확인!"
    read -p "구 오브젝트를 삭제합니까? (yes/no): " confirm
    if [[ "$confirm" != "yes" ]]; then
        log "삭제 취소"
        exit 0
    fi

    local count=0
    local errors=0

    while IFS=$'\t' read -r old_key new_key content_type; do
        [[ "$old_key" == "$new_key" ]] && continue

        # 새 오브젝트 존재 확인 후 삭제
        if aws s3api head-object --bucket "$BUCKET" --key "$new_key" \
            --region "$REGION" >/dev/null 2>&1; then
            if aws s3 rm "s3://${BUCKET}/${old_key}" --region "$REGION" 2>>"$LOG_FILE"; then
                ((count++))
            else
                log "ERROR: DELETE 실패 ${old_key}"
                ((errors++))
            fi
        else
            log "SKIP: 새 오브젝트 미존재, 보존 ${old_key}"
        fi
    done < "$MAPPING_FILE"

    log "삭제 완료: ${count}건 삭제, ${errors}건 실패"
}

# ============================================================
# Main
# ============================================================
log "========================================="
log "V8: S3 Content-Type 마이그레이션"
log "Bucket: ${BUCKET} | Region: ${REGION}"
log "DRY_RUN: ${DRY_RUN} | CLEANUP: ${CLEANUP}"
log "========================================="

if [[ -z "$DB_HOST" || -z "$DB_USER" ]]; then
    log "ERROR: DB 접속 정보 미설정"
    log "  export DB_HOST=xxx DB_USER=xxx DB_PASS=xxx"
    exit 1
fi

if $CLEANUP; then
    cleanup_old_objects
else
    generate_mapping
    copy_objects
    log "========================================="
    if $DRY_RUN; then
        log "DRY RUN 완료. 실제 복사: ./s3_content_type_copy.sh"
    else
        log "S3 복사 완료!"
        log "다음: V8_content_type_fix.sql 실행 → COMMIT → ./s3_content_type_copy.sh --cleanup"
    fi
    log "========================================="
fi
