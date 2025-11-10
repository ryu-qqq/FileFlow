#!/usr/bin/env python3
"""LangFuse 연결 테스트 및 간단한 Trace 생성"""

import os
from pathlib import Path
from dotenv import load_dotenv
from langfuse import Langfuse

# 프로젝트 루트 경로
PROJECT_ROOT = Path(__file__).parent.parent.parent

# .env 파일 로드
load_dotenv(PROJECT_ROOT / ".env")

# LangFuse 클라이언트 초기화
public_key = os.getenv('LANGFUSE_PUBLIC_KEY')
secret_key = os.getenv('LANGFUSE_SECRET_KEY')
host = os.getenv('LANGFUSE_HOST', 'https://us.cloud.langfuse.com')

print(f"🔧 LangFuse 설정:")
print(f"   - Host: {host}")
print(f"   - Public Key: {public_key[:10]}..." if public_key else "   - Public Key: ❌ 없음")
print(f"   - Secret Key: {secret_key[:10]}..." if secret_key else "   - Secret Key: ❌ 없음")

if not public_key or not secret_key:
    print("\n❌ API 키가 설정되지 않았습니다")
    exit(1)

langfuse = Langfuse(
    public_key=public_key,
    secret_key=secret_key,
    host=host
)

print(f"\n✅ LangFuse 클라이언트 초기화 완료")

# 테스트 Trace 생성
print("\n📤 테스트 Trace 생성 중...")

try:
    trace = langfuse.trace(
        name="test-hook-execution",
        input={"test": "hook system test"},
        output={"success": True},
        metadata={"version": "v1.0"}
    )
    print(f"✅ Trace 생성 완료: {trace.id}")

    # Span 추가
    span = trace.span(
        name="cache-injection-test",
        input={"layer": "domain"},
        output={"rules_loaded": 15}
    )
    print(f"✅ Span 추가 완료: {span.id}")

    # Flush
    langfuse.flush()
    print(f"\n✅ 데이터 전송 완료!")
    print(f"   → LangFuse Dashboard: {host}/traces/{trace.id}")

except Exception as e:
    print(f"\n❌ 오류 발생: {e}")
    import traceback
    traceback.print_exc()
