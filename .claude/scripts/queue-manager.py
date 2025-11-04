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

def add_task(feature: str, work_order: Optional[str] = None, priority: str = "normal"):
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
        "completed_at": None
    }
    
    data["queue"].append(task)
    save_queue(data)
    
    log_success(f"작업 추가됨: {feature}")
    print(f"  ID: {task['id']}")
    print(f"  작업지시서: {work_order or 'None'}")
    print(f"  우선순위: {priority}")

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

def complete_task(feature: str):
    """작업 완료"""
    data = load_queue()
    
    for i, task in enumerate(data["queue"]):
        if task["feature"] == feature:
            if task["status"] != "in_progress":
                log_warning(f"작업이 진행 중이 아님: {feature}")
                return
            
            task["status"] = "completed"
            task["completed_at"] = datetime.now().isoformat()
            
            # completed 목록으로 이동
            data["completed"].append(task)
            data["queue"].pop(i)
            save_queue(data)
            
            log_success(f"작업 완료됨: {feature}")
            print(f"\n📊 통계:")
            print(f"  소요 시간: {_calculate_duration(task['started_at'], task['completed_at'])}")
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

def usage():
    """사용법"""
    print("""
📋 Queue Manager

Usage:
  queue-manager.py add <feature> [work-order] [--priority high|normal]
  queue-manager.py start <feature>
  queue-manager.py complete <feature>
  queue-manager.py list
  queue-manager.py status

Examples:
  # 작업 추가
  queue-manager.py add order order-aggregate.md

  # 높은 우선순위로 작업 추가
  queue-manager.py add payment --priority high

  # 작업 시작
  queue-manager.py start order

  # 작업 완료
  queue-manager.py complete order

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
        
        if "--priority" in sys.argv:
            idx = sys.argv.index("--priority")
            if len(sys.argv) > idx + 1:
                priority = sys.argv[idx + 1]
        
        add_task(feature, work_order, priority)
    
    elif command == "start":
        if len(sys.argv) < 3:
            log_error("Feature name required")
            sys.exit(1)
        start_task(sys.argv[2])
    
    elif command == "complete":
        if len(sys.argv) < 3:
            log_error("Feature name required")
            sys.exit(1)
        complete_task(sys.argv[2])
    
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
