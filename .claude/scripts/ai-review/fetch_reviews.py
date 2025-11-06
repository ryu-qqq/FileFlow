#!/usr/bin/env python3
"""
AI Review Fetcher

GitHub API를 통해 AI 봇(Gemini, CodeRabbit, Codex) 댓글 수집
"""

import json
import subprocess
import sys
from typing import Dict, List, Optional
from dataclasses import dataclass, asdict


@dataclass
class BotComment:
    """봇 댓글 데이터 클래스"""
    id: str
    bot_name: str  # gemini, coderabbit, codex
    file: Optional[str]
    line: Optional[int]
    body: str
    category: str  # security, performance, style, etc.
    created_at: str


class ReviewFetcher:
    """GitHub PR 리뷰 댓글 수집기"""

    # 봇 사용자 이름 매핑
    BOT_USERS = {
        "gemini-code-assist[bot]": "gemini",
        "coderabbitai[bot]": "coderabbit",
        "chatgpt-codex-connector[bot]": "codex"
    }

    def __init__(self, repo: Optional[str] = None):
        """
        초기화

        Args:
            repo: GitHub 저장소 (owner/repo 형식, 없으면 현재 디렉토리에서 추출)
        """
        self.repo = repo or self._get_current_repo()

    def _get_current_repo(self) -> str:
        """현재 디렉토리의 GitHub 저장소 추출"""
        try:
            result = subprocess.run(
                ["gh", "repo", "view", "--json", "nameWithOwner", "-q", ".nameWithOwner"],
                capture_output=True,
                text=True,
                check=True
            )
            return result.stdout.strip()
        except subprocess.CalledProcessError as e:
            raise RuntimeError(f"현재 디렉토리가 GitHub 저장소가 아닙니다: {e}")

    def fetch_pr_comments(
        self,
        pr_number: int,
        bots: Optional[List[str]] = None
    ) -> List[BotComment]:
        """
        PR의 모든 봇 댓글 수집

        Args:
            pr_number: PR 번호
            bots: 수집할 봇 리스트 (None이면 모든 봇)

        Returns:
            봇 댓글 리스트
        """
        if bots is None:
            bots = ["gemini", "coderabbit", "codex"]

        print(f"\n🔍 PR #{pr_number} 리뷰 수집 중...")
        print(f"  저장소: {self.repo}")
        print(f"  대상 봇: {', '.join(bots)}")

        all_comments = []

        # 1. PR 댓글 (일반 댓글)
        pr_comments = self._fetch_pr_issue_comments(pr_number)
        all_comments.extend(pr_comments)

        # 2. 리뷰 댓글 (코드 라인별 댓글)
        review_comments = self._fetch_pr_review_comments(pr_number)
        all_comments.extend(review_comments)

        # 봇 필터링
        bot_users = [user for user, name in self.BOT_USERS.items() if name in bots]
        filtered_comments = [
            comment for comment in all_comments
            if comment.bot_name in bots
        ]

        print(f"✅ 수집 완료: {len(filtered_comments)}개 댓글")
        for bot in bots:
            count = sum(1 for c in filtered_comments if c.bot_name == bot)
            if count > 0:
                print(f"  - {bot}: {count}개")

        return filtered_comments

    def _fetch_pr_issue_comments(self, pr_number: int) -> List[BotComment]:
        """PR issue 댓글 수집 (일반 댓글)"""
        try:
            result = subprocess.run(
                [
                    "gh", "api",
                    f"repos/{self.repo}/issues/{pr_number}/comments",
                    "--paginate"
                ],
                capture_output=True,
                text=True,
                check=True
            )
            comments_data = json.loads(result.stdout)
        except (subprocess.CalledProcessError, json.JSONDecodeError) as e:
            print(f"⚠️ PR 댓글 수집 실패: {e}", file=sys.stderr)
            return []

        bot_comments = []
        for comment in comments_data:
            user = comment.get("user", {}).get("login", "")
            if user in self.BOT_USERS:
                bot_comments.append(BotComment(
                    id=str(comment["id"]),
                    bot_name=self.BOT_USERS[user],
                    file=None,  # 일반 댓글은 파일 정보 없음
                    line=None,
                    body=comment.get("body", ""),
                    category=self._categorize_comment(comment.get("body", "")),
                    created_at=comment.get("created_at", "")
                ))

        return bot_comments

    def _fetch_pr_review_comments(self, pr_number: int) -> List[BotComment]:
        """PR 리뷰 댓글 수집 (코드 라인별 댓글)"""
        try:
            result = subprocess.run(
                [
                    "gh", "api",
                    f"repos/{self.repo}/pulls/{pr_number}/comments",
                    "--paginate"
                ],
                capture_output=True,
                text=True,
                check=True
            )
            comments_data = json.loads(result.stdout)
        except (subprocess.CalledProcessError, json.JSONDecodeError) as e:
            print(f"⚠️ 리뷰 댓글 수집 실패: {e}", file=sys.stderr)
            return []

        bot_comments = []
        for comment in comments_data:
            user = comment.get("user", {}).get("login", "")
            if user in self.BOT_USERS:
                bot_comments.append(BotComment(
                    id=str(comment["id"]),
                    bot_name=self.BOT_USERS[user],
                    file=comment.get("path"),
                    line=comment.get("line") or comment.get("original_line"),
                    body=comment.get("body", ""),
                    category=self._categorize_comment(comment.get("body", "")),
                    created_at=comment.get("created_at", "")
                ))

        return bot_comments

    def _categorize_comment(self, body: str) -> str:
        """
        댓글 내용으로 카테고리 분류

        Args:
            body: 댓글 본문

        Returns:
            카테고리 (security, performance, style, etc.)
        """
        body_lower = body.lower()

        # 키워드 기반 분류
        if any(kw in body_lower for kw in ["security", "vulnerability", "injection", "xss"]):
            return "security"
        elif any(kw in body_lower for kw in ["performance", "slow", "optimize", "efficient"]):
            return "performance"
        elif any(kw in body_lower for kw in ["error", "exception", "handling", "validation"]):
            return "error-handling"
        elif any(kw in body_lower for kw in ["test", "coverage", "unit test"]):
            return "testing"
        elif any(kw in body_lower for kw in ["style", "format", "naming", "convention"]):
            return "style"
        elif any(kw in body_lower for kw in ["refactor", "complexity", "maintainability"]):
            return "refactoring"
        else:
            return "general"

    def export_to_json(self, comments: List[BotComment], output_file: str) -> None:
        """
        댓글을 JSON 파일로 내보내기

        Args:
            comments: 댓글 리스트
            output_file: 출력 파일 경로
        """
        data = {
            "repo": self.repo,
            "total_comments": len(comments),
            "comments": [asdict(comment) for comment in comments]
        }

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)

        print(f"💾 JSON 저장: {output_file}")


if __name__ == "__main__":
    """CLI 테스트용"""
    import argparse

    parser = argparse.ArgumentParser(description="AI 리뷰 댓글 수집")
    parser.add_argument("pr_number", type=int, help="PR 번호")
    parser.add_argument(
        "--bots",
        nargs="+",
        choices=["gemini", "coderabbit", "codex"],
        help="수집할 봇 (기본: 모든 봇)"
    )
    parser.add_argument(
        "--output",
        help="JSON 출력 파일 (선택)"
    )

    args = parser.parse_args()

    fetcher = ReviewFetcher()
    comments = fetcher.fetch_pr_comments(args.pr_number, args.bots)

    if args.output:
        fetcher.export_to_json(comments, args.output)
    else:
        # 콘솔 출력
        for comment in comments:
            print(f"\n{'='*60}")
            print(f"Bot: {comment.bot_name}")
            print(f"Category: {comment.category}")
            if comment.file:
                print(f"Location: {comment.file}:{comment.line}")
            print(f"Body:\n{comment.body[:200]}...")
