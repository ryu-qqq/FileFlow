#!/usr/bin/env python3
"""
LangFuse Prompt Registration Script

Layer별 프롬프트를 LangFuse에 등록하여 버전 관리 및 A/B/C/D 테스트 준비
"""

import os
import sys
import json
import requests
from datetime import datetime
from pathlib import Path

# .env 파일 로드
def load_env():
    """Load .env file from project root"""
    # 프로젝트 루트 경로 (.claude/commands/lib/ → 프로젝트 루트)
    env_path = Path(__file__).parent.parent.parent.parent / '.env'
    if env_path.exists():
        with open(env_path) as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    os.environ[key.strip()] = value.strip()

load_env()

def register_prompt(layer, version):
    """
    Layer별 프롬프트를 LangFuse에 등록

    Args:
        layer: domain, application, persistence, adapter-rest
        version: v1.0, v1.1 등
    """

    # 환경 변수 확인
    public_key = os.getenv("LANGFUSE_PUBLIC_KEY")
    secret_key = os.getenv("LANGFUSE_SECRET_KEY")
    host = os.getenv("LANGFUSE_HOST", "https://us.cloud.langfuse.com")

    if not public_key or not secret_key:
        print("❌ LangFuse 환경 변수가 설정되지 않았습니다.")
        print("\n설정 방법:")
        print("  export LANGFUSE_PUBLIC_KEY='pk-lf-...'")
        print("  export LANGFUSE_SECRET_KEY='sk-lf-...'")
        print("  export LANGFUSE_HOST='https://us.cloud.langfuse.com'")
        sys.exit(1)

    # 프롬프트 파일 읽기
    prompt_file = f".claude/prompts/{layer}-layer-{version}.md"
    if not os.path.exists(prompt_file):
        print(f"❌ 프롬프트 파일이 없습니다: {prompt_file}")
        print(f"\n프롬프트 파일을 먼저 생성해주세요:")
        print(f"  touch {prompt_file}")
        sys.exit(1)

    with open(prompt_file, "r", encoding="utf-8") as f:
        prompt_content = f.read()

    # Zero-Tolerance 규칙 개수 추출
    zero_tolerance_count = prompt_content.count("✅")
    template_count = prompt_content.count("```java")
    checklist_count = prompt_content.count("- [ ]")

    print(f"\n📋 프롬프트 분석:")
    print(f"   Layer: {layer}")
    print(f"   Version: {version}")
    print(f"   Zero-Tolerance 규칙: {zero_tolerance_count}개")
    print(f"   코드 템플릿: {template_count}개")
    print(f"   검증 체크리스트: {checklist_count}개")

    # LangFuse API 요청
    url = f"{host}/api/public/v2/prompts"
    headers = {
        "Content-Type": "application/json"
    }
    auth = (public_key, secret_key)

    data = {
        "name": f"{layer}-layer-prompt",
        "prompt": prompt_content,
        "config": {
            "model": "claude-sonnet-4-5",
            "temperature": 0.7,
            "max_tokens": 8000
        },
        "labels": [layer, version, "zero-tolerance"],
        "tags": [f"layer:{layer}", f"version:{version}"]
    }

    print(f"\n🔄 LangFuse 업로드 중...")
    response = requests.post(url, headers=headers, auth=auth, json=data)

    # 200 (OK) 또는 201 (Created) 모두 성공으로 처리
    if response.status_code in [200, 201]:
        response_data = response.json()
        prompt_id = response_data.get("id")
        print(f"\n✅ 프롬프트 등록 완료!")
        print(f"   Prompt ID: {prompt_id}")
        print(f"   Version: {response_data.get('version')}")
        print(f"   Created At: {response_data.get('createdAt')}")
        print(f"   LangFuse 대시보드: {host}/prompts/{prompt_id}")
        print(f"\n다음 단계:")
        print(f"   1. /abcd-test {layer} {version} - A/B/C/D 테스트 실행")
        print(f"   2. /langfuse-analyze {layer} {version} - 결과 분석")
    else:
        print(f"\n❌ 프롬프트 등록 실패: {response.status_code}")
        print(f"   Error: {response.text}")
        sys.exit(1)

def main():
    if len(sys.argv) < 3:
        print("사용법: python3 register_prompt_to_langfuse.py <layer> <version>")
        print("\n예시:")
        print("  python3 register_prompt_to_langfuse.py domain v1.0")
        print("  python3 register_prompt_to_langfuse.py application v1.1")
        print("  python3 register_prompt_to_langfuse.py all v1.0")
        sys.exit(1)

    layer = sys.argv[1]
    version = sys.argv[2]

    if layer == "all":
        # 모든 Layer 등록
        layers = ["domain", "application", "persistence", "adapter-rest"]
        print(f"🔄 모든 Layer ({len(layers)}개)를 등록합니다...\n")
        for l in layers:
            register_prompt(l, version)
            print("\n" + "="*60 + "\n")
    else:
        register_prompt(layer, version)

if __name__ == "__main__":
    main()
