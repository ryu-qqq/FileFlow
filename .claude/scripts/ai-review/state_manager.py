#!/usr/bin/env python3
"""
AI Review State Manager

상태 파일 관리 및 자동 정리 로직
- TTL: 7일 (7일 이상 된 PR 자동 삭제)
- 크기 제한: 최대 100개 PR
- 매 실행 시 자동 정리
"""

import json
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Optional


class ReviewStateManager:
    """리뷰 상태 관리 클래스"""

    def __init__(self, state_file: Optional[Path] = None):
        """
        초기화

        Args:
            state_file: 상태 파일 경로 (기본: .claude/scripts/ai-review/review-state.json)
        """
        if state_file is None:
            base_dir = Path(__file__).parent
            state_file = base_dir / "review-state.json"

        self.state_file = Path(state_file)
        self.max_prs = 100  # 최대 PR 개수
        self.ttl_days = 7   # TTL (일) - 7일로 설정

    def load_state(self) -> Dict:
        """
        상태 파일 로드 + 자동 정리

        Returns:
            정리된 상태 딕셔너리
        """
        if not self.state_file.exists():
            return {}

        try:
            with open(self.state_file, 'r', encoding='utf-8') as f:
                state = json.load(f)
        except (json.JSONDecodeError, IOError) as e:
            print(f"⚠️ 상태 파일 로드 실패: {e}")
            return {}

        # 자동 정리 실행
        cleaned_state = self._auto_cleanup(state)

        # 정리된 상태 저장 (변경 있을 때만)
        if cleaned_state != state:
            self.save_state(cleaned_state)
            removed_count = len(state) - len(cleaned_state)
            print(f"🧹 자동 정리: {removed_count}개 PR 제거됨")

        return cleaned_state

    def _auto_cleanup(self, state: Dict) -> Dict:
        """
        자동 정리 로직

        Args:
            state: 현재 상태 딕셔너리

        Returns:
            정리된 상태 딕셔너리
        """
        now = datetime.now()
        cleaned = {}
        ttl_removed = 0

        for pr_key, pr_data in state.items():
            # 1. TTL 체크 (7일)
            last_run_str = pr_data.get("last_run", "2000-01-01T00:00:00")
            try:
                last_run = datetime.fromisoformat(last_run_str)
            except ValueError:
                # 잘못된 날짜 형식은 제거
                ttl_removed += 1
                continue

            age_days = (now - last_run).days

            if age_days > self.ttl_days:
                ttl_removed += 1
                continue  # 삭제 (7일 초과)

            cleaned[pr_key] = pr_data

        if ttl_removed > 0:
            print(f"  ⏰ TTL 초과 (7일): {ttl_removed}개 PR 제거")

        # 2. 크기 제한 (최대 100개, 최근 것만 유지)
        if len(cleaned) > self.max_prs:
            sorted_prs = sorted(
                cleaned.items(),
                key=lambda x: x[1].get("last_run", ""),
                reverse=True  # 최근 순
            )
            size_removed = len(cleaned) - self.max_prs
            cleaned = dict(sorted_prs[:self.max_prs])
            print(f"  📦 크기 제한: {size_removed}개 PR 제거 (최대 {self.max_prs}개 유지)")

        return cleaned

    def save_state(self, state: Dict) -> None:
        """
        상태 파일 저장

        Args:
            state: 저장할 상태 딕셔너리
        """
        self.state_file.parent.mkdir(parents=True, exist_ok=True)

        try:
            with open(self.state_file, 'w', encoding='utf-8') as f:
                json.dump(state, f, indent=2, ensure_ascii=False)
        except IOError as e:
            print(f"⚠️ 상태 파일 저장 실패: {e}")

    def mark_as_processed(
        self,
        pr_number: int,
        comment_ids: List[str],
        bots: List[str]
    ) -> None:
        """
        댓글 처리 완료 마킹

        Args:
            pr_number: PR 번호
            comment_ids: 처리된 댓글 ID 리스트
            bots: 처리한 봇 리스트
        """
        state = self.load_state()
        pr_key = f"pr-{pr_number}"

        state[pr_key] = {
            "processed_comments": comment_ids,
            "last_run": datetime.now().isoformat(),
            "bots": bots
        }

        self.save_state(state)
        print(f"✅ {pr_key} 처리 완료 마킹: {len(comment_ids)}개 댓글")

    def get_processed_comments(self, pr_number: int) -> List[str]:
        """
        이미 처리된 댓글 ID 조회

        Args:
            pr_number: PR 번호

        Returns:
            처리된 댓글 ID 리스트
        """
        state = self.load_state()
        pr_key = f"pr-{pr_number}"
        return state.get(pr_key, {}).get("processed_comments", [])

    def filter_new_comments(
        self,
        pr_number: int,
        all_comments: List[Dict]
    ) -> List[Dict]:
        """
        새 댓글만 필터링 (이미 처리된 댓글 제외)

        Args:
            pr_number: PR 번호
            all_comments: 모든 댓글 리스트 (각 댓글은 'id' 필드 포함)

        Returns:
            새 댓글 리스트
        """
        processed_ids = set(self.get_processed_comments(pr_number))
        new_comments = [
            comment for comment in all_comments
            if str(comment.get("id", "")) not in processed_ids
        ]

        filtered_count = len(all_comments) - len(new_comments)
        if filtered_count > 0:
            print(f"🔍 필터링: {filtered_count}개 댓글 이미 처리됨 (총 {len(all_comments)}개)")

        return new_comments

    def clean_pr(self, pr_number: int) -> None:
        """
        특정 PR 상태 제거

        Args:
            pr_number: PR 번호
        """
        state = self.load_state()
        pr_key = f"pr-{pr_number}"

        if pr_key in state:
            del state[pr_key]
            self.save_state(state)
            print(f"✅ {pr_key} 상태 제거 완료")
        else:
            print(f"⚠️ {pr_key} 상태 없음 (이미 제거됨)")

    def clean_all(self) -> None:
        """모든 상태 초기화"""
        count = len(self.load_state())
        self.save_state({})
        print(f"✅ 모든 PR 상태 초기화 완료 ({count}개 제거)")

    def show_stats(self) -> None:
        """상태 통계 출력"""
        state = self.load_state()

        print("\n📊 Review State 통계")
        print("=" * 50)
        print(f"총 PR 수: {len(state)}")
        print(f"TTL: {self.ttl_days}일")
        print(f"최대 개수: {self.max_prs}개")

        if state:
            print("\n최근 처리된 PR:")
            sorted_prs = sorted(
                state.items(),
                key=lambda x: x[1].get("last_run", ""),
                reverse=True
            )
            for pr_key, pr_data in sorted_prs[:5]:  # 최근 5개만
                last_run = pr_data.get("last_run", "Unknown")
                bots = ", ".join(pr_data.get("bots", []))
                comment_count = len(pr_data.get("processed_comments", []))
                print(f"  - {pr_key}: {comment_count}개 댓글 ({bots}) - {last_run}")


if __name__ == "__main__":
    """테스트 및 수동 관리용"""
    import sys

    manager = ReviewStateManager()

    if len(sys.argv) < 2:
        manager.show_stats()
    elif sys.argv[1] == "--clean":
        manager.clean_all()
    elif sys.argv[1] == "--clean-pr" and len(sys.argv) > 2:
        pr_num = int(sys.argv[2])
        manager.clean_pr(pr_num)
    elif sys.argv[1] == "--stats":
        manager.show_stats()
    else:
        print("Usage:")
        print("  python state_manager.py              # 통계 출력")
        print("  python state_manager.py --stats      # 통계 출력")
        print("  python state_manager.py --clean      # 모든 상태 초기화")
        print("  python state_manager.py --clean-pr N # PR N 상태 제거")
