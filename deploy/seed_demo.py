"""一键导入演示数据(答辩用)。

注册/登录演示用户,并通过后端 API 批量录入近 14 天的血糖、饮食、运动数据。
仅依赖 Python 标准库。

用法:
    python seed_demo.py                       # 默认 http://localhost:8080
    python seed_demo.py --base http://host:8080 --user demo --pwd demo123 --days 14
"""
import argparse
import json
import random
import urllib.request
from datetime import datetime, timedelta

random.seed(7)


def call(base, path, payload=None, token=None, method="POST"):
    url = base + path
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode())


def ensure_token(base, user, pwd):
    try:
        r = call(base, "/api/auth/register", {"username": user, "password": pwd, "nickname": "演示用户"})
        print("已注册演示用户")
    except Exception:
        r = call(base, "/api/auth/login", {"username": user, "password": pwd})
        print("演示用户已存在,直接登录")
    return r["data"]["token"]


def seed(base, token, days):
    start = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0) - timedelta(days=days)
    periods = [("FASTING", 7), ("AFTER_MEAL", 9), ("AFTER_MEAL", 14), ("BEFORE_MEAL", 18),
               ("AFTER_MEAL", 20), ("BEDTIME", 22)]
    g = 0
    for d in range(days):
        day = start + timedelta(days=d)
        for period, hour in periods:
            t = day + timedelta(hours=hour)
            base_v = 5.6 + 0.01 * (d * len(periods))  # 轻微上升趋势
            if period == "AFTER_MEAL":
                base_v += random.uniform(2.0, 4.5)
            elif period == "BEDTIME":
                base_v += random.uniform(0.5, 1.5)
            v = round(min(max(base_v + random.gauss(0, 0.4), 3.2), 16.0), 1)
            call(base, "/api/glucose", {"valueMmol": v, "period": period,
                                        "measuredAt": t.strftime("%Y-%m-%dT%H:%M:%S")}, token)
            g += 1

    diets = [("BREAKFAST", "燕麦粥 + 鸡蛋 + 牛奶", 350, 45),
             ("LUNCH", "糙米饭 + 清蒸鱼 + 西兰花", 550, 60),
             ("DINNER", "杂粮饭 + 鸡胸肉 + 青菜", 480, 55),
             ("SNACK", "苹果一个", 90, 20)]
    for d in range(days):
        day = start + timedelta(days=d)
        for meal, food, cal, carbs in diets:
            hour = {"BREAKFAST": 8, "LUNCH": 12, "DINNER": 18, "SNACK": 15}[meal]
            call(base, "/api/diet", {"mealType": meal, "food": food, "calories": cal,
                                     "carbsG": carbs,
                                     "eatenAt": (day + timedelta(hours=hour)).strftime("%Y-%m-%dT%H:%M:%S")}, token)

    exs = [("快走", 40, "MEDIUM", 180), ("慢跑", 30, "HIGH", 280), ("太极", 45, "LOW", 120)]
    for d in range(days):
        if d % 2 == 0:
            typ, dur, inten, cal = random.choice(exs)
            day = start + timedelta(days=d, hours=19)
            call(base, "/api/exercise", {"type": typ, "durationMin": dur, "intensity": inten,
                                         "calories": cal,
                                         "doneAt": day.strftime("%Y-%m-%dT%H:%M:%S")}, token)
    print(f"已导入 {g} 条血糖、{days * len(diets)} 条饮食、约 {days // 2} 条运动记录")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default="http://localhost:8080")
    ap.add_argument("--user", default="demo")
    ap.add_argument("--pwd", default="demo123")
    ap.add_argument("--days", type=int, default=14)
    args = ap.parse_args()
    token = ensure_token(args.base, args.user, args.pwd)
    seed(args.base, token, args.days)
    print(f"完成。可用 {args.user}/{args.pwd} 登录 http://localhost:5173 查看。")


if __name__ == "__main__":
    main()
