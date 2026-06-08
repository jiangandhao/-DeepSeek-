"""血糖趋势预测。

首期采用轻量「线性趋势 + 滑动均值」基线模型(仅依赖 numpy,无需 GPU)。
notebooks/ 中提供 Prophet / LSTM 对比实验,接口可平滑替换为更强模型。
"""
from datetime import datetime, timedelta
from statistics import median

import numpy as np

from app.schemas import GlucancePoint, PredictPoint, PredictResponse

PLAUSIBLE_MIN = 2.5
PLAUSIBLE_MAX = 25.0
ISO_FMT = "%Y-%m-%dT%H:%M:%S"


def _parse(ts: str) -> datetime:
    return datetime.fromisoformat(ts)


def predict(history: list[GlucancePoint], horizon: int = 6) -> PredictResponse:
    if len(history) < 3:
        # 数据不足:用最后一个值(或默认)平推
        last_val = history[-1].value_mmol if history else 6.0
        last_t = _parse(history[-1].measured_at) if history else datetime.now()
        preds = [
            PredictPoint(
                value_mmol=round(last_val, 1),
                measured_at=(last_t + timedelta(hours=4 * (i + 1))).strftime(ISO_FMT),
            )
            for i in range(horizon)
        ]
        return PredictResponse(predictions=preds, model="naive-last", metrics=None)

    times = [_parse(p.measured_at) for p in history]
    t0 = times[0]
    x = np.array([(t - t0).total_seconds() / 3600.0 for t in times])  # 小时
    y = np.array([p.value_mmol for p in history])

    # 一次线性拟合 value = a*x + b
    a, b = np.polyfit(x, y, 1)
    y_fit = a * x + b
    # 残差用滑动均值校正:取最近残差均值
    resid = y - y_fit
    recent_bias = float(np.mean(resid[-3:]))

    # 预测间隔:历史中位间隔
    deltas = [(times[i + 1] - times[i]).total_seconds() / 3600.0 for i in range(len(times) - 1)]
    step_h = median(deltas) if deltas else 4.0
    step_h = max(0.5, min(step_h, 24.0))

    last_x = x[-1]
    last_t = times[-1]
    preds = []
    for i in range(1, horizon + 1):
        xi = last_x + step_h * i
        val = a * xi + b + recent_bias
        val = float(np.clip(val, PLAUSIBLE_MIN, PLAUSIBLE_MAX))
        preds.append(PredictPoint(
            value_mmol=round(val, 1),
            measured_at=(last_t + timedelta(hours=step_h * i)).strftime(ISO_FMT),
        ))

    # 样本内误差指标(供论文/展示)
    mae = float(np.mean(np.abs(resid)))
    rmse = float(np.sqrt(np.mean(resid ** 2)))
    metrics = {"mae": round(mae, 3), "rmse": round(rmse, 3), "slope_per_h": round(float(a), 4)}

    return PredictResponse(predictions=preds, model="linear-trend+ma", metrics=metrics)
