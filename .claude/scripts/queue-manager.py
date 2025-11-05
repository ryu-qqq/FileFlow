#!/usr/bin/env python3

"""
Queue Manager Script
Purpose: 작업 큐 시스템 관리 (추가, 시작, 완료, 목록)
Usage: python3 queue-manager.py [add|start|complete|list|status] [args...]
"""

import json
import sys
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional

# 색상 정의
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color

def log_info(msg: str):
    print(f"{Colors.BLUE}ℹ️  {Colors.NC}{msg}")

def log_success(msg: str):
    print(f"{Colors.GREEN}✅ {Colors.NC}{msg}")

def log_warning(msg: str):
    print(f"{Colors.YELLOW}⚠️  {Colors.NC}{msg}")

def log_error(msg: str):
    print(f"{Colors.RED}❌ {Colors.NC}{msg}")

# 큐 파일 경로
QUEUE_FILE = Path(".claude/work-queue.json")

def load_queue() -> Dict:
    """큐 파일 로드"""
    if not QUEUE_FILE.exists():
        return {
            "queue": [],
            "completed": [],
            "metadata": {
                "version": "1.0",
                "created_at": datetime.now().isoformat(),
                "last_updated": datetime.now().isoformat()
            }
        }
    
    with open(QUEUE_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_queue(data: Dict):
    """큐 파일 저장"""
    data["metadata"]["last_updated"] = datetime.now().isoformat()
    with open(QUEUE_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def add_task(feature: str, work_order: Optional[str] = None, priority: str = "normal", estimated_time: Optional[str] = None):
    """작업 추가"""
    data = load_queue()

    # 중복 확인
    for task in data["queue"]:
        if task["feature"] == feature:
            log_warning(f"작업이 이미 큐에 존재: {feature}")
            return

    # 새 작업 추가
    task = {
        "id": len(data["queue"]) + 1,
        "feature": feature,
        "work_order": work_order,
        "priority": priority,
        "status": "pending",
        "created_at": datetime.now().isoformat(),
        "started_at": None,
        "completed_at": None,
        "estimated_time": estimated_time,  # Claude 예상 시간
        "actual_time": None,
        "accuracy": None,
        "code_lines": 0,
        "files_created": 0,
        "interruptions": 0
    }

    data["queue"].append(task)
    save_queue(data)

    log_success(f"작업 추가됨: {feature}")
    print(f"  ID: {task['id']}")
    print(f"  작업지시서: {work_order or 'None'}")
    print(f"  우선순위: {priority}")
    if estimated_time:
        print(f"  예상 시간: {estimated_time}")

def start_task(feature: str):
    """작업 시작"""
    data = load_queue()
    
    for task in data["queue"]:
        if task["feature"] == feature:
            if task["status"] == "in_progress":
                log_warning(f"작업이 이미 진행 중: {feature}")
                return
            
            task["status"] = "in_progress"
            task["started_at"] = datetime.now().isoformat()
            save_queue(data)
            
            log_success(f"작업 시작됨: {feature}")
            print(f"\n📝 다음 단계:")
            print(f"  1. bash .claude/scripts/worktree-manager.sh create {feature} {task.get('work_order', '')}")
            print(f"  2. Cursor AI로 Boilerplate 생성")
            print(f"  3. Git Commit")
            print(f"  4. python3 .claude/scripts/queue-manager.py complete {feature}")
            return
    
    log_error(f"작업을 찾을 수 없음: {feature}")

def complete_task(feature: str, code_lines: int = 0, files_created: int = 0, interruptions: int = 0):
    """작업 완료"""
    data = load_queue()

    for i, task in enumerate(data["queue"]):
        if task["feature"] == feature:
            if task["status"] != "in_progress":
                log_warning(f"작업이 진행 중이 아님: {feature}")
                return

            task["status"] = "completed"
            task["completed_at"] = datetime.now().isoformat()

            # 실제 소요 시간 계산
            actual_time = _calculate_duration(task['started_at'], task['completed_at'])
            task["actual_time"] = actual_time

            # 메트릭 저장
            task["code_lines"] = code_lines
            task["files_created"] = files_created
            task["interruptions"] = interruptions

            # 정확도 계산 (예상 시간이 있을 경우)
            if task.get("estimated_time"):
                accuracy = _calculate_accuracy(task["estimated_time"], actual_time)
                task["accuracy"] = accuracy

            # completed 목록으로 이동
            data["completed"].append(task)
            data["queue"].pop(i)
            save_queue(data)

            log_success(f"작업 완료됨: {feature}")
            print(f"\n📊 통계:")
            print(f"  실제 소요 시간: {actual_time}")
            if task.get("estimated_time"):
                print(f"  예상 시간: {task['estimated_time']}")
                print(f"  정확도: {task.get('accuracy', 'N/A')}")
            if code_lines > 0:
                print(f"  생성 코드: {code_lines} 줄")
            if files_created > 0:
                print(f"  생성 파일: {files_created}개")
            if interruptions > 0:
                print(f"  중단 횟수: {interruptions}회")
            print(f"  남은 작업: {len(data['queue'])}개")
            print(f"  완료된 작업: {len(data['completed'])}개")
            return

    log_error(f"작업을 찾을 수 없음: {feature}")

def list_tasks():
    """작업 목록"""
    data = load_queue()
    
    if not data["queue"]:
        log_info("큐에 작업이 없습니다")
        return
    
    print(f"\n{Colors.CYAN}📋 작업 큐{Colors.NC}\n")
    
    for task in data["queue"]:
        status_icon = "⏳" if task["status"] == "pending" else "🔄"
        priority_icon = "🔥" if task["priority"] == "high" else "📌"
        
        print(f"{status_icon} {priority_icon} {Colors.PURPLE}{task['feature']}{Colors.NC}")
        print(f"   ID: {task['id']} | 상태: {task['status']}")
        if task.get('work_order'):
            print(f"   작업지시서: {task['work_order']}")
        if task['started_at']:
            print(f"   시작: {task['started_at']}")
        print()

def status_queue():
    """큐 상태"""
    data = load_queue()
    
    pending = [t for t in data["queue"] if t["status"] == "pending"]
    in_progress = [t for t in data["queue"] if t["status"] == "in_progress"]
    completed = data["completed"]
    
    print(f"\n{Colors.CYAN}📊 큐 상태{Colors.NC}\n")
    print(f"  ⏳ 대기 중: {len(pending)}개")
    print(f"  🔄 진행 중: {len(in_progress)}개")
    print(f"  ✅ 완료됨: {len(completed)}개")
    print(f"  📝 총 작업: {len(pending) + len(in_progress)}개")
    
    if in_progress:
        print(f"\n{Colors.YELLOW}현재 진행 중:{Colors.NC}")
        for task in in_progress:
            print(f"  🔄 {task['feature']}")
            if task['started_at']:
                duration = _calculate_duration_from_now(task['started_at'])
                print(f"     진행 시간: {duration}")

def _calculate_duration(start: str, end: str) -> str:
    """소요 시간 계산"""
    start_dt = datetime.fromisoformat(start)
    end_dt = datetime.fromisoformat(end)
    delta = end_dt - start_dt
    
    minutes = int(delta.total_seconds() / 60)
    if minutes < 60:
        return f"{minutes}분"
    else:
        hours = minutes // 60
        mins = minutes % 60
        return f"{hours}시간 {mins}분"

def _calculate_duration_from_now(start: str) -> str:
    """현재까지 소요 시간"""
    return _calculate_duration(start, datetime.now().isoformat())

def _parse_time_to_minutes(time_str: str) -> int:
    """시간 문자열을 분으로 변환 (예: '30분', '1시간 30분')"""
    if not time_str:
        return 0

    time_str = time_str.lower().replace(" ", "")
    total_minutes = 0

    # 시간 파싱
    if "시간" in time_str:
        parts = time_str.split("시간")
        try:
            hours = int(parts[0])
            total_minutes += hours * 60
            if len(parts) > 1 and "분" in parts[1]:
                mins = int(parts[1].replace("분", ""))
                total_minutes += mins
        except ValueError:
            return 0
    elif "분" in time_str:
        try:
            mins = int(time_str.replace("분", ""))
            total_minutes = mins
        except ValueError:
            return 0

    return total_minutes

def _calculate_accuracy(estimated: str, actual: str) -> str:
    """예상 시간 vs 실제 시간 정확도 계산"""
    est_minutes = _parse_time_to_minutes(estimated)
    act_minutes = _parse_time_to_minutes(actual)

    if est_minutes == 0 or act_minutes == 0:
        return "N/A"

    # 정확도 계산: 100% - |차이| / 예상 * 100
    diff = abs(est_minutes - act_minutes)
    accuracy = max(0, 100 - (diff / est_minutes * 100))

    return f"{accuracy:.1f}%"

def usage():
    """사용법"""
    print("""
📋 Queue Manager

Usage:
  queue-manager.py add <feature> [work-order] [--priority high|normal] [--estimate <time>]
  queue-manager.py start <feature>
  queue-manager.py complete <feature> [--lines <num>] [--files <num>] [--interruptions <num>]
  queue-manager.py list
  queue-manager.py status

Examples:
  # 작업 추가 (예상 시간 포함)
  queue-manager.py add order order-aggregate.md --estimate "30분"

  # 높은 우선순위로 작업 추가
  queue-manager.py add payment --priority high --estimate "1시간 30분"

  # 작업 시작
  queue-manager.py start order

  # 작업 완료 (메트릭 포함)
  queue-manager.py complete order --lines 450 --files 12 --interruptions 2

  # 큐 목록
  queue-manager.py list

  # 큐 상태
  queue-manager.py status
""")

def main():
    if len(sys.argv) < 2:
        usage()
        sys.exit(1)
    
    command = sys.argv[1]

    if command == "add":
        if len(sys.argv) < 3:
            log_error("Feature name required")
            usage()
            sys.exit(1)

        feature = sys.argv[2]
        work_order = sys.argv[3] if len(sys.argv) > 3 and not sys.argv[3].startswith('--') else None
        priority = "normal"
        estimated_time = None

        if "--priority" in sys.argv:
            idx = sys.argv.index("--priority")
            if len(sys.argv) > idx + 1:
                priority = sys.argv[idx + 1]

        if "--estimate" in sys.argv:
            idx = sys.argv.index("--estimate")
            if len(sys.argv) > idx + 1:
                estimated_time = sys.argv[idx + 1]

        add_task(feature, work_order, priority, estimated_time)
    
    elif command == "start":
        if len(sys.argv) < 3:
            log_error("Feature name required")
            sys.exit(1)
        start_task(sys.argv[2])
    
    elif command == "complete":
        if len(sys.argv) < 3:
            log_error("Feature name required")
            sys.exit(1)

        feature = sys.argv[2]
        code_lines = 0
        files_created = 0
        interruptions = 0

        if "--lines" in sys.argv:
            idx = sys.argv.index("--lines")
            if len(sys.argv) > idx + 1:
                code_lines = int(sys.argv[idx + 1])

        if "--files" in sys.argv:
            idx = sys.argv.index("--files")
            if len(sys.argv) > idx + 1:
                files_created = int(sys.argv[idx + 1])

        if "--interruptions" in sys.argv:
            idx = sys.argv.index("--interruptions")
            if len(sys.argv) > idx + 1:
                interruptions = int(sys.argv[idx + 1])

        complete_task(feature, code_lines, files_created, interruptions)
    
    elif command == "list":
        list_tasks()
    
    elif command == "status":
        status_queue()
    
    else:
        log_error(f"Unknown command: {command}")
        usage()
        sys.exit(1)

if __name__ == "__main__":
    main()
