"""生成合成血糖时间序列数据,用于预测模型实验(论文复现)。

模拟特征:
- 昼夜基线波动(早晨偏高的「黎明现象」)
- 三餐后血糖升高(餐后峰值)
- 个体趋势(缓慢上升/下降)
- 随机噪声

用法:
    python generate_synthetic.py --days 60 --out glucose_synthetic.csv
"""
import argparse
import csv
from datetime import datetime, timedelta

import numpy as np

PERIODS = [
    ("FASTING", 7),      # 早 7 点 空腹
    ("AFTER_MEAL", 9),   # 早餐后
    ("BEFORE_MEAL", 12), # 午餐前
    ("AFTER_MEAL", 14),  # 午餐后
    ("BEFORE_MEAL", 18), # 晚餐前
    ("AFTER_MEAL", 20),  # 晚餐后
    ("BEDTIME", 22),     # 睡前
]


def generate(days: int, seed: int = 42) -> list[dict]:
    rng = np.random.default_rng(seed)
    start = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0) - timedelta(days=days)
    base = 5.6                         # 基础空腹血糖
    trend = rng.uniform(-0.005, 0.012) # 每条记录的缓慢趋势
    rows = []
    idx = 0
    for d in range(days):
        day = start + timedelta(days=d)
        for period, hour in PERIODS:
            t = day + timedelta(hours=hour)
            val = base + trend * idx
            # 餐后峰值
            if period == "AFTER_MEAL":
                val += rng.uniform(2.0, 4.5)
            elif period == "FASTING":
                val += rng.uniform(-0.2, 0.6)  # 黎明现象
            elif period == "BEDTIME":
                val += rng.uniform(0.5, 1.8)
            # 噪声 + 偶发异常
            val += rng.normal(0, 0.4)
            if rng.random() < 0.03:
                val += rng.choice([-2.0, 3.5])  # 偶发低/高血糖
            val = float(np.clip(val, 3.0, 18.0))
            rows.append({
                "measured_at": t.strftime("%Y-%m-%dT%H:%M:%S"),
                "period": period,
                "value_mmol": round(val, 1),
            })
            idx += 1
    return rows


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--days", type=int, default=60)
    ap.add_argument("--out", default="glucose_synthetic.csv")
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()

    rows = generate(args.days, args.seed)
    with open(args.out, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=["measured_at", "period", "value_mmol"])
        writer.writeheader()
        writer.writerows(rows)
    print(f"已生成 {len(rows)} 条记录 -> {args.out}")


if __name__ == "__main__":
    main()
