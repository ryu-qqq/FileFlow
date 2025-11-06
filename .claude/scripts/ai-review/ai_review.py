#!/usr/bin/env python3
"""
AI Review Integration Script

모든 모듈을 통합하여 AI 리뷰 자동화 실행
- 상태 관리 (중복 방지)
- 봇 댓글 수집
- 중복 제거
- 우선순위 계산
- TodoList 생성
"""

import argparse
import json
import sys
from pathlib import Path

# 모듈 임포트
from state_manager import ReviewStateManager
from fetch_reviews import ReviewFetcher
from deduplicator import Deduplicator
from prioritizer import Prioritizer
from todo_generator import TodoGenerator


class AIReviewIntegration:
    """AI 리뷰 통합 실행 클래스"""

    def __init__(self, args):
        """
        초기화

        Args:
            args: 명령줄 인자
        """
        self.args = args
        self.state_manager = ReviewStateManager()
        self.fetcher = ReviewFetcher()

    def run(self) -> None:
        """메인 실행"""
        print("🤖 AI Review 자동화 시작\n")
        print("=" * 60)

        # 1. PR 번호 확인
        pr_number = self.args.pr_number
        if pr_number is None:
            pr_number = self._get_current_pr()

        print(f"📌 PR 번호: {pr_number}")

        # 2. 댓글 수집
        print(f"\n{'='*60}")
        bots = self.args.bots or ["gemini", "coderabbit", "codex"]
        all_comments = self.fetcher.fetch_pr_comments(pr_number, bots)

        if not all_comments:
            print("⚠️ 수집된 댓글이 없습니다. 종료합니다.")
            sys.exit(0)

        # 3. 기존 처리된 댓글 필터링
        print(f"\n{'='*60}")
        comments_data = [
            {
                "id": c.id,
                "bot_name": c.bot_name,
                "file": c.file,
                "line": c.line,
                "body": c.body,
                "category": c.category,
                "created_at": c.created_at
            }
            for c in all_comments
        ]

        if not self.args.force:
            new_comments = self.state_manager.filter_new_comments(pr_number, comments_data)
        else:
            print("🔄 --force 플래그: 모든 댓글 재처리")
            new_comments = comments_data

        if not new_comments:
            print("✅ 새로운 댓글이 없습니다. 모두 처리되었습니다.")
            sys.exit(0)

        # 4. 중복 제거
        print(f"\n{'='*60}")
        deduplicator = Deduplicator(new_comments)
        merged_issues = deduplicator.deduplicate()

        if self.args.preview:
            deduplicator.show_deduplication_report()

        # 5. 우선순위 계산
        print(f"\n{'='*60}")
        merged_issues_data = [
            {
                "id": issue.id,
                "file": issue.file,
                "line": issue.line,
                "category": issue.category,
                "description": issue.description,
                "bots": issue.bots,
                "vote_count": issue.vote_count
            }
            for issue in merged_issues
        ]

        prioritizer = Prioritizer(merged_issues_data)
        prioritized_issues = prioritizer.prioritize()

        # 6. TodoList 생성
        print(f"\n{'='*60}")
        prioritized_data = prioritizer.export_prioritized_issues()

        generator = TodoGenerator(prioritized_data)
        todo_markdown = generator.generate()

        # 7. 결과 저장
        print(f"\n{'='*60}")
        if self.args.output:
            generator.save_to_file(self.args.output, todo_markdown)
        else:
            # 기본 위치에 저장
            output_dir = Path.cwd() / "claudedocs"
            output_dir.mkdir(exist_ok=True)
            output_file = output_dir / f"ai-review-pr{pr_number}.md"
            generator.save_to_file(str(output_file), todo_markdown)

        # 8. 상태 업데이트 (처리 완료 마킹)
        if not self.args.analyze_only:
            processed_ids = [c["id"] for c in new_comments]
            self.state_manager.mark_as_processed(pr_number, processed_ids, bots)

        # 9. 최종 요약
        print(f"\n{'='*60}")
        print("✅ AI Review 자동화 완료!\n")
        print("📊 최종 요약:")
        print(f"  - 수집된 댓글: {len(all_comments)}개")
        print(f"  - 새 댓글: {len(new_comments)}개")
        print(f"  - 병합 후 이슈: {len(merged_issues)}개")
        print(f"  - TodoList 항목: {len(prioritized_issues)}개")

        # Preview 모드가 아니면 TodoList 출력
        if not self.args.preview and not self.args.analyze_only:
            print(f"\n{'='*60}")
            print("📝 생성된 TodoList:\n")
            print(todo_markdown)

    def _get_current_pr(self) -> int:
        """현재 브랜치의 PR 번호 추출"""
        import subprocess

        try:
            result = subprocess.run(
                ["gh", "pr", "view", "--json", "number", "-q", ".number"],
                capture_output=True,
                text=True,
                check=True
            )
            return int(result.stdout.strip())
        except (subprocess.CalledProcessError, ValueError) as e:
            print(f"❌ 현재 브랜치의 PR을 찾을 수 없습니다: {e}")
            print("   PR 번호를 명시적으로 지정해주세요: /ai-review 42")
            sys.exit(1)


def main():
    """CLI 진입점"""
    parser = argparse.ArgumentParser(
        description="AI Review 자동화 - 여러 AI 봇 리뷰 통합 및 TodoList 생성"
    )

    # PR 번호
    parser.add_argument(
        "pr_number",
        type=int,
        nargs="?",
        help="PR 번호 (없으면 현재 브랜치 PR 사용)"
    )

    # 봇 선택
    parser.add_argument(
        "--bots",
        nargs="+",
        choices=["gemini", "coderabbit", "codex"],
        help="분석할 봇 (기본: 모든 봇)"
    )

    # 전략 (미래 확장용)
    parser.add_argument(
        "--strategy",
        choices=["merge", "vote", "sequential"],
        default="merge",
        help="통합 전략 (기본: merge)"
    )

    # 분석만 수행
    parser.add_argument(
        "--analyze-only",
        action="store_true",
        help="분석만 수행 (상태 업데이트 없음)"
    )

    # 미리보기
    parser.add_argument(
        "--preview",
        action="store_true",
        help="미리보기 모드 (중복 제거 리포트 출력)"
    )

    # 강제 재처리
    parser.add_argument(
        "--force",
        action="store_true",
        help="이미 처리된 댓글도 재처리"
    )

    # 출력 파일
    parser.add_argument(
        "--output",
        help="TodoList 출력 파일 (기본: claudedocs/ai-review-prN.md)"
    )

    # 상태 관리
    parser.add_argument(
        "--clean",
        action="store_true",
        help="모든 상태 초기화"
    )

    parser.add_argument(
        "--clean-pr",
        type=int,
        metavar="N",
        help="특정 PR 상태 제거"
    )

    parser.add_argument(
        "--stats",
        action="store_true",
        help="상태 통계 출력"
    )

    args = parser.parse_args()

    # 상태 관리 명령어 처리
    if args.clean:
        ReviewStateManager().clean_all()
        sys.exit(0)

    if args.clean_pr:
        ReviewStateManager().clean_pr(args.clean_pr)
        sys.exit(0)

    if args.stats:
        ReviewStateManager().show_stats()
        sys.exit(0)

    # 메인 실행
    integration = AIReviewIntegration(args)
    integration.run()


if __name__ == "__main__":
    main()
