"""血糖预测模型对比实验(论文实验章节素材)。

对合成血糖数据做「一步向前」(one-step-ahead)滚动预测,对比多种基线:
- Naive-Last:以上一观测值作为预测
- Naive-Mean:以历史均值作为预测
- MovingAvg:近 w 点滑动均值
- LinearTrend:线性趋势 + 残差校正(系统线上所用基线)

输出 MAE / RMSE / MAPE 对比表;如安装 matplotlib 则保存预测对比图。

用法:
    python glucose_forecast_experiment.py
"""
import sys
from pathlib import Path

import numpy as np

# 允许直接复用线上预测器
sys.path.append(str(Path(__file__).resolve().parents[1]))
from data.generate_synthetic import generate  # noqa: E402


def naive_last(history: np.ndarray) -> float:
    return history[-1]


def naive_mean(history: np.ndarray) -> float:
    return history.mean()


def moving_avg(history: np.ndarray, w: int = 5) -> float:
    return history[-w:].mean()


def linear_trend(history: np.ndarray) -> float:
    n = len(history)
    x = np.arange(n)
    a, b = np.polyfit(x, history, 1)
    resid = history - (a * x + b)
    return a * n + b + resid[-3:].mean()


METHODS = {
    "Naive-Last": naive_last,
    "Naive-Mean": naive_mean,
    "MovingAvg-5": moving_avg,
    "LinearTrend": linear_trend,
}


def metrics(y_true: np.ndarray, y_pred: np.ndarray) -> dict:
    err = y_pred - y_true
    mae = np.mean(np.abs(err))
    rmse = np.sqrt(np.mean(err ** 2))
    mape = np.mean(np.abs(err / y_true)) * 100
    return {"MAE": mae, "RMSE": rmse, "MAPE%": mape}


def run(days: int = 90, min_train: int = 20):
    rows = generate(days)
    series = np.array([r["value_mmol"] for r in rows])

    results = {name: {"true": [], "pred": []} for name in METHODS}
    for i in range(min_train, len(series)):
        hist = series[:i]
        truth = series[i]
        for name, fn in METHODS.items():
            results[name]["true"].append(truth)
            results[name]["pred"].append(fn(hist))

    print(f"\n样本量:{len(series)} 条,训练窗口起点:{min_train}\n")
    print(f"{'Model':<14}{'MAE':>8}{'RMSE':>8}{'MAPE%':>8}")
    print("-" * 38)
    summary = {}
    for name in METHODS:
        m = metrics(np.array(results[name]["true"]), np.array(results[name]["pred"]))
        summary[name] = m
        print(f"{name:<14}{m['MAE']:>8.3f}{m['RMSE']:>8.3f}{m['MAPE%']:>8.2f}")

    best = min(summary, key=lambda k: summary[k]["RMSE"])
    print(f"\n最优模型(按 RMSE):{best}")

    # 可选绘图
    try:
        import matplotlib.pyplot as plt
        plt.figure(figsize=(12, 4))
        plt.plot(series[min_train:], label="真实值", color="black", linewidth=1)
        for name in ["LinearTrend", "Naive-Last"]:
            plt.plot(results[name]["pred"], label=name, alpha=0.7)
        plt.legend(); plt.title("血糖一步向前预测对比"); plt.tight_layout()
        out = Path(__file__).parent / "forecast_compare.png"
        plt.savefig(out, dpi=120)
        print(f"已保存预测对比图 -> {out}")
    except ImportError:
        print("(未安装 matplotlib,跳过绘图;pip install matplotlib 可生成对比图)")


if __name__ == "__main__":
    run()
