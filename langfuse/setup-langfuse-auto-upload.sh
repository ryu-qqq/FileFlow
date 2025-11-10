#!/bin/bash
# LangFuse Hook 로그 자동 업로드 설정 스크립트
#
# 목적: Hook 실행 로그를 LangFuse로 자동 업로드하여 실시간 모니터링
#
# 사용법:
#   bash tools/setup-langfuse-auto-upload.sh [method]
#
# Methods:
#   cron      - Cron Job으로 매시간 자동 업로드 (추천)
#   launchd   - macOS launchd로 자동 업로드
#   git-hook  - Git post-commit hook으로 자동 업로드
#   manual    - 수동 실행 가이드만 표시

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
METHOD="${1:-cron}"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 LangFuse Hook 로그 자동 업로드 설정"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo

# 환경 변수 확인
if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo "❌ .env 파일이 없습니다."
    echo "   다음 내용으로 .env 파일을 생성하세요:"
    echo
    echo "   LANGFUSE_PUBLIC_KEY=pk-lf-..."
    echo "   LANGFUSE_SECRET_KEY=sk-lf-..."
    echo "   LANGFUSE_HOST=https://us.cloud.langfuse.com"
    exit 1
fi

# Python 확인
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3가 설치되어 있지 않습니다."
    exit 1
fi

echo "✅ 환경 확인 완료"
echo

case "$METHOD" in
    cron)
        echo "📅 Cron Job 설정 (매시간 자동 업로드)"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo

        CRON_CMD="0 * * * * cd $PROJECT_ROOT && python3 scripts/langfuse/upload-hook-logs-v2.py >> logs/langfuse-upload.log 2>&1"

        echo "다음 명령어를 실행하여 Cron Job을 추가하세요:"
        echo
        echo "  crontab -e"
        echo
        echo "그리고 다음 라인을 추가:"
        echo
        echo "  $CRON_CMD"
        echo
        echo "또는 자동으로 추가:"
        echo
        echo "  (crontab -l 2>/dev/null; echo '$CRON_CMD') | crontab -"
        echo
        echo "✅ 설정 후 로그 확인: tail -f $PROJECT_ROOT/logs/langfuse-upload.log"
        ;;

    launchd)
        echo "🍎 macOS launchd 설정 (매시간 자동 업로드)"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo

        PLIST_FILE="$HOME/Library/LaunchAgents/com.ryuqq.langfuse.upload.plist"
        LOG_DIR="$PROJECT_ROOT/logs"

        mkdir -p "$LOG_DIR"

        cat > "$PLIST_FILE" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.ryuqq.langfuse.upload</string>

    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/python3</string>
        <string>$PROJECT_ROOT/scripts/langfuse/upload-hook-logs-v2.py</string>
    </array>

    <key>WorkingDirectory</key>
    <string>$PROJECT_ROOT</string>

    <key>StartInterval</key>
    <integer>3600</integer>

    <key>StandardOutPath</key>
    <string>$LOG_DIR/langfuse-upload.log</string>

    <key>StandardErrorPath</key>
    <string>$LOG_DIR/langfuse-upload-error.log</string>

    <key>RunAtLoad</key>
    <true/>
</dict>
</plist>
EOF

        echo "✅ launchd plist 파일 생성 완료: $PLIST_FILE"
        echo
        echo "다음 명령어로 서비스 시작:"
        echo
        echo "  launchctl load $PLIST_FILE"
        echo
        echo "서비스 중지:"
        echo
        echo "  launchctl unload $PLIST_FILE"
        echo
        echo "로그 확인:"
        echo
        echo "  tail -f $LOG_DIR/langfuse-upload.log"
        ;;

    git-hook)
        echo "🔗 Git Post-Commit Hook 설정"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo

        HOOK_FILE="$PROJECT_ROOT/.git/hooks/post-commit"

        if [ -f "$HOOK_FILE" ]; then
            echo "⚠️  기존 post-commit hook 발견. 백업 생성 중..."
            cp "$HOOK_FILE" "$HOOK_FILE.backup"
        fi

        cat >> "$HOOK_FILE" <<'EOF'

# LangFuse Hook 로그 자동 업로드
python3 scripts/langfuse/upload-hook-logs-v2.py &> /dev/null &
EOF

        chmod +x "$HOOK_FILE"

        echo "✅ Git post-commit hook 설정 완료"
        echo
        echo "이제 커밋할 때마다 자동으로 Hook 로그가 LangFuse로 업로드됩니다."
        echo
        echo "⚠️  백그라운드 실행이므로 커밋 속도에 영향 없음"
        ;;

    manual)
        echo "📖 수동 실행 가이드"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo
        echo "Claude Code에서 Slash Command 실행:"
        echo
        echo "  /upload-langfuse-hooks"
        echo
        echo "또는 직접 스크립트 실행:"
        echo
        echo "  # 증분 업로드 (기본)"
        echo "  python3 scripts/langfuse/upload-hook-logs-v2.py"
        echo
        echo "  # 전체 로그 업로드"
        echo "  python3 scripts/langfuse/upload-hook-logs-v2.py --full"
        echo
        echo "  # Dry-run (전송 없이 테스트)"
        echo "  python3 scripts/langfuse/upload-hook-logs-v2.py --dry-run"
        ;;

    *)
        echo "❌ 알 수 없는 설정 방법: $METHOD"
        echo
        echo "사용 가능한 방법:"
        echo "  cron      - Cron Job (추천)"
        echo "  launchd   - macOS launchd"
        echo "  git-hook  - Git post-commit hook"
        echo "  manual    - 수동 실행 가이드"
        exit 1
        ;;
esac

echo
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ 설정 완료!"
echo
echo "📊 LangFuse Dashboard:"
echo "   https://us.cloud.langfuse.com/project/claude-spring-standards"
echo
echo "📚 자세한 가이드:"
echo "   langfuse/MEASUREMENT_STRATEGY.md"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
