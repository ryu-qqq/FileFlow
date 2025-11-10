#!/usr/bin/env python3
"""
Hook 로직 자동 최적화 스크립트

목적: Hook 실행 로그를 분석하여 키워드 매핑 및 Layer 감지 로직 개선

주요 기능:
1. 키워드 감지 정확도 분석
2. Layer 매핑 효과 측정
3. False positive/negative 케이스 식별
4. 개선된 키워드 매핑 제안
5. user-prompt-submit.sh 업데이트 스크립트 생성

사용법:
    # 기본 분석 (최근 100개 세션)
    python3 scripts/langfuse/optimize-hook-logic.py

    # 상세 분석 (모든 세션)
    python3 scripts/langfuse/optimize-hook-logic.py --all --verbose

    # 개선 제안 파일 생성
    python3 scripts/langfuse/optimize-hook-logic.py --output optimization-report.md
"""

import os
import sys
import json
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional, Tuple
from collections import Counter, defaultdict

# 프로젝트 루트
PROJECT_ROOT = Path(__file__).parent.parent.parent
HOOK_LOG_FILE = PROJECT_ROOT / ".claude" / "hooks" / "logs" / "hook-execution.jsonl"


class HookLogicOptimizer:
    """Hook 로직 최적화 분석기"""

    def __init__(self):
        """초기화"""
        self.sessions = {}
        self.keyword_stats = defaultdict(lambda: {"detected": 0, "injected": 0, "violations": 0})
        self.layer_stats = defaultdict(lambda: {"detected": 0, "injected": 0, "violations": 0})
        print("✅ Hook 로직 최적화 분석기 초기화")

    def load_hook_logs(self, max_sessions: Optional[int] = None) -> Dict:
        """
        Hook 로그 로드

        Args:
            max_sessions: 최대 세션 수 (None이면 전체)

        Returns:
            세션별 이벤트 딕셔너리
        """
        if not HOOK_LOG_FILE.exists():
            print(f"⚠️ Hook 로그 파일이 없습니다: {HOOK_LOG_FILE}")
            return {}

        sessions = defaultdict(list)
        session_count = 0

        with open(HOOK_LOG_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                try:
                    event = json.loads(line.strip())
                    sid = event.get('session_id', 'unknown')

                    # 새 세션 감지
                    if sid not in sessions:
                        session_count += 1
                        if max_sessions and session_count > max_sessions:
                            break

                    sessions[sid].append(event)

                except json.JSONDecodeError:
                    continue

        print(f"📊 {len(sessions)}개 세션 로드 완료 (총 {sum(len(events) for events in sessions.values())}개 이벤트)")
        return sessions

    def analyze_keyword_accuracy(self, sessions: Dict) -> Dict:
        """
        키워드 감지 정확도 분석

        Args:
            sessions: 세션별 이벤트

        Returns:
            키워드 통계 딕셔너리
        """
        print("🔍 키워드 감지 정확도 분석 중...")

        keyword_usage = Counter()
        keyword_to_layer = defaultdict(Counter)
        false_positives = []
        false_negatives = []

        for sid, events in sessions.items():
            # 이벤트 타입별 분류
            event_by_type = defaultdict(list)
            for e in events:
                event_type = e.get('event', 'unknown')
                event_by_type[event_type].append(e)

            # 키워드 분석
            keyword_analysis = event_by_type.get('keyword_analysis', [{}])[0]
            detected_keywords = keyword_analysis.get('detected_keywords', [])
            detected_layers = keyword_analysis.get('detected_layers', [])
            context_score = keyword_analysis.get('context_score', 0)

            # Cache 주입
            cache_injections = event_by_type.get('cache_injection', [])

            # 검증 결과
            validation_results = event_by_type.get('validation_result', [])
            violations = sum(len(vr.get('violations', [])) for vr in validation_results)

            # 키워드 사용 추적
            for keyword in detected_keywords:
                keyword_usage[keyword] += 1

                # 키워드 → Layer 매핑 추적
                for layer in detected_layers:
                    keyword_to_layer[keyword][layer] += 1

            # False Positive: 키워드 감지했지만 Cache 주입 실패
            if detected_keywords and not cache_injections:
                false_positives.append({
                    "session_id": sid,
                    "keywords": detected_keywords,
                    "context_score": context_score
                })

            # False Negative: Cache 주입 없었지만 위반 발생 (감지 실패)
            if not cache_injections and violations > 0:
                false_negatives.append({
                    "session_id": sid,
                    "violations": violations
                })

        # 통계 계산
        total_sessions = len(sessions)
        total_detections = sum(keyword_usage.values())
        avg_keywords_per_session = total_detections / total_sessions if total_sessions > 0 else 0

        analysis = {
            "total_sessions": total_sessions,
            "total_keyword_detections": total_detections,
            "avg_keywords_per_session": avg_keywords_per_session,
            "keyword_frequency": dict(keyword_usage.most_common(20)),
            "keyword_to_layer_mapping": {k: dict(v) for k, v in keyword_to_layer.items()},
            "false_positives": len(false_positives),
            "false_negatives": len(false_negatives),
            "false_positive_rate": len(false_positives) / total_sessions * 100 if total_sessions > 0 else 0,
            "false_negative_rate": len(false_negatives) / total_sessions * 100 if total_sessions > 0 else 0,
            "false_positive_examples": false_positives[:5],
            "false_negative_examples": false_negatives[:5]
        }

        return analysis

    def analyze_layer_detection(self, sessions: Dict) -> Dict:
        """
        Layer 감지 효과 분석

        Args:
            sessions: 세션별 이벤트

        Returns:
            Layer 통계 딕셔너리
        """
        print("🎯 Layer 감지 효과 분석 중...")

        layer_detections = Counter()
        layer_violations = Counter()
        layer_injection_success = Counter()
        layer_injection_attempts = Counter()

        for sid, events in sessions.items():
            event_by_type = defaultdict(list)
            for e in events:
                event_type = e.get('event', 'unknown')
                event_by_type[event_type].append(e)

            # Layer 감지
            keyword_analysis = event_by_type.get('keyword_analysis', [{}])[0]
            detected_layers = keyword_analysis.get('detected_layers', [])

            # Cache 주입
            cache_injections = event_by_type.get('cache_injection', [])

            # 검증 결과
            validation_results = event_by_type.get('validation_result', [])

            # Layer별 통계
            for layer in detected_layers:
                layer_detections[layer] += 1

                # 주입 시도
                layer_injection_attempts[layer] += 1

                # 주입 성공 여부
                layer_injected = any(ci.get('layer') == layer for ci in cache_injections)
                if layer_injected:
                    layer_injection_success[layer] += 1

            # Layer별 위반
            for vr in validation_results:
                layer = vr.get('layer', 'unknown')
                violations_count = len(vr.get('violations', []))
                if violations_count > 0:
                    layer_violations[layer] += violations_count

        # 통계 계산
        analysis = {
            "layer_detection_frequency": dict(layer_detections.most_common()),
            "layer_injection_success_rate": {
                layer: (layer_injection_success[layer] / layer_injection_attempts[layer] * 100)
                if layer_injection_attempts[layer] > 0 else 0
                for layer in layer_detections.keys()
            },
            "layer_violation_frequency": dict(layer_violations.most_common()),
            "layer_effectiveness_score": {
                layer: (
                    100 - (layer_violations[layer] / layer_detections[layer] * 100)
                    if layer_detections[layer] > 0 else 0
                )
                for layer in layer_detections.keys()
            }
        }

        return analysis

    def generate_keyword_suggestions(self, keyword_analysis: Dict, layer_analysis: Dict) -> Dict:
        """
        개선된 키워드 매핑 제안

        Args:
            keyword_analysis: 키워드 분석 결과
            layer_analysis: Layer 분석 결과

        Returns:
            키워드 개선 제안 딕셔너리
        """
        print("💡 키워드 매핑 개선 제안 생성 중...")

        suggestions = {
            "high_value_keywords": [],
            "low_value_keywords": [],
            "new_keyword_candidates": [],
            "layer_mapping_improvements": {}
        }

        # 1. 고가치 키워드 (감지율 높고 위반율 낮음)
        keyword_freq = keyword_analysis.get('keyword_frequency', {})
        for keyword, count in keyword_freq.items():
            if count >= 5:  # 최소 5번 이상 감지
                suggestions["high_value_keywords"].append({
                    "keyword": keyword,
                    "frequency": count,
                    "recommendation": "유지 또는 점수 증가"
                })

        # 2. 저가치 키워드 (감지율 낮거나 False Positive 많음)
        false_positive_rate = keyword_analysis.get('false_positive_rate', 0)
        if false_positive_rate > 10:
            suggestions["low_value_keywords"].append({
                "pattern": "generic_keywords",
                "false_positive_rate": f"{false_positive_rate:.1f}%",
                "recommendation": "더 구체적인 키워드로 대체"
            })

        # 3. Layer 매핑 개선
        keyword_to_layer = keyword_analysis.get('keyword_to_layer_mapping', {})
        layer_effectiveness = layer_analysis.get('layer_effectiveness_score', {})

        for keyword, layers in keyword_to_layer.items():
            most_common_layer = max(layers, key=layers.get) if layers else None
            if most_common_layer:
                effectiveness = layer_effectiveness.get(most_common_layer, 0)

                if effectiveness < 80:
                    suggestions["layer_mapping_improvements"][keyword] = {
                        "current_layer": most_common_layer,
                        "effectiveness": f"{effectiveness:.1f}%",
                        "recommendation": "Layer 매핑 재검토 필요"
                    }

        # 4. 새로운 키워드 후보 (False Negative 분석 기반)
        false_negative_rate = keyword_analysis.get('false_negative_rate', 0)
        if false_negative_rate > 5:
            suggestions["new_keyword_candidates"].append({
                "observation": f"위반 사례 {false_negative_rate:.1f}%에서 키워드 감지 실패",
                "recommendation": "위반 세션의 사용자 입력 패턴 재분석 필요"
            })

        return suggestions

    def generate_optimization_report(
        self,
        keyword_analysis: Dict,
        layer_analysis: Dict,
        suggestions: Dict,
        output_file: Optional[str] = None
    ):
        """
        최적화 보고서 생성

        Args:
            keyword_analysis: 키워드 분석 결과
            layer_analysis: Layer 분석 결과
            suggestions: 개선 제안
            output_file: 출력 파일 경로
        """
        print("📝 최적화 보고서 생성 중...")

        report_lines = [
            "# Hook 로직 자동 최적화 보고서",
            "",
            f"**생성 일시**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "---",
            "",
            "## 📊 키워드 감지 분석",
            "",
            f"- **총 세션 수**: {keyword_analysis['total_sessions']}",
            f"- **총 키워드 감지**: {keyword_analysis['total_keyword_detections']}회",
            f"- **평균 키워드/세션**: {keyword_analysis['avg_keywords_per_session']:.1f}개",
            f"- **False Positive 비율**: {keyword_analysis['false_positive_rate']:.1f}%",
            f"- **False Negative 비율**: {keyword_analysis['false_negative_rate']:.1f}%",
            "",
            "### 키워드 빈도 (상위 10개)",
            ""
        ]

        # 키워드 빈도
        for keyword, count in list(keyword_analysis['keyword_frequency'].items())[:10]:
            report_lines.append(f"- **{keyword}**: {count}회")

        report_lines.extend([
            "",
            "---",
            "",
            "## 🎯 Layer 감지 분석",
            "",
        ])

        # Layer 감지 빈도
        layer_freq = layer_analysis.get('layer_detection_frequency', {})
        if layer_freq:
            report_lines.append("### Layer 감지 빈도")
            report_lines.append("")
            for layer, count in layer_freq.items():
                report_lines.append(f"- **{layer}**: {count}회")
            report_lines.append("")

        # Layer 주입 성공률
        injection_rate = layer_analysis.get('layer_injection_success_rate', {})
        if injection_rate:
            report_lines.append("### Layer Cache 주입 성공률")
            report_lines.append("")
            for layer, rate in injection_rate.items():
                report_lines.append(f"- **{layer}**: {rate:.1f}%")
            report_lines.append("")

        # Layer 효과성 점수
        effectiveness = layer_analysis.get('layer_effectiveness_score', {})
        if effectiveness:
            report_lines.append("### Layer 효과성 점수 (위반 감소율)")
            report_lines.append("")
            for layer, score in effectiveness.items():
                status = "✅" if score >= 80 else "⚠️" if score >= 60 else "❌"
                report_lines.append(f"- {status} **{layer}**: {score:.1f}%")
            report_lines.append("")

        report_lines.extend([
            "---",
            "",
            "## 💡 개선 제안",
            "",
        ])

        # 고가치 키워드
        high_value = suggestions.get('high_value_keywords', [])
        if high_value:
            report_lines.append("### 🎯 고가치 키워드 (유지 권장)")
            report_lines.append("")
            for kw in high_value[:5]:
                report_lines.append(f"- **{kw['keyword']}** ({kw['frequency']}회): {kw['recommendation']}")
            report_lines.append("")

        # 저가치 키워드
        low_value = suggestions.get('low_value_keywords', [])
        if low_value:
            report_lines.append("### ⚠️ 저가치 키워드 (검토 필요)")
            report_lines.append("")
            for kw in low_value:
                report_lines.append(f"- **{kw['pattern']}** (FP: {kw['false_positive_rate']}): {kw['recommendation']}")
            report_lines.append("")

        # Layer 매핑 개선
        mapping_improvements = suggestions.get('layer_mapping_improvements', {})
        if mapping_improvements:
            report_lines.append("### 🔄 Layer 매핑 개선")
            report_lines.append("")
            for keyword, info in list(mapping_improvements.items())[:5]:
                report_lines.append(
                    f"- **{keyword}** → {info['current_layer']} (효과성: {info['effectiveness']}): "
                    f"{info['recommendation']}"
                )
            report_lines.append("")

        # 새 키워드 후보
        new_keywords = suggestions.get('new_keyword_candidates', [])
        if new_keywords:
            report_lines.append("### 🆕 새 키워드 후보")
            report_lines.append("")
            for candidate in new_keywords:
                report_lines.append(f"- **관찰**: {candidate['observation']}")
                report_lines.append(f"  - **권장**: {candidate['recommendation']}")
            report_lines.append("")

        report_lines.extend([
            "---",
            "",
            "## 🚀 다음 단계",
            "",
            "1. **고가치 키워드 강화**: 점수를 30 → 40으로 증가",
            "2. **저가치 키워드 제거**: False Positive 많은 키워드 삭제",
            "3. **Layer 매핑 재검토**: 효과성 80% 미만인 매핑 개선",
            "4. **새 키워드 추가**: False Negative 사례에서 패턴 발견",
            "5. **A/B 테스트**: 개선된 Hook 로직 효과 검증",
            "",
            "---",
            ""
        ])

        report_text = "\n".join(report_lines)

        # 출력
        if output_file:
            output_path = Path(output_file)
            output_path.parent.mkdir(parents=True, exist_ok=True)
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write(report_text)
            print(f"✅ 보고서 저장: {output_path}")
        else:
            print("\n" + report_text)


def main():
    """메인 함수"""
    import argparse

    parser = argparse.ArgumentParser(description="Hook 로직 자동 최적화")
    parser.add_argument('--all', action='store_true', help="모든 세션 분석 (기본: 최근 100개)")
    parser.add_argument('--sessions', type=int, default=100, help="분석할 세션 수")
    parser.add_argument('--verbose', action='store_true', help="상세 출력")
    parser.add_argument('--output', type=str, help="보고서 출력 파일 (Markdown)")

    args = parser.parse_args()

    try:
        optimizer = HookLogicOptimizer()

        # 로그 로드
        max_sessions = None if args.all else args.sessions
        sessions = optimizer.load_hook_logs(max_sessions=max_sessions)

        if not sessions:
            print("⚠️ 분석할 세션이 없습니다.")
            return

        # 분석 실행
        keyword_analysis = optimizer.analyze_keyword_accuracy(sessions)
        layer_analysis = optimizer.analyze_layer_detection(sessions)
        suggestions = optimizer.generate_keyword_suggestions(keyword_analysis, layer_analysis)

        # 보고서 생성
        optimizer.generate_optimization_report(
            keyword_analysis,
            layer_analysis,
            suggestions,
            output_file=args.output
        )

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
