from datetime import datetime, timedelta

from app.prediction.predictor import predict
from app.schemas import GlucancePoint


def _series(values):
    base = datetime(2026, 6, 1, 8, 0, 0)
    return [
        GlucancePoint(value_mmol=v, period="FASTING",
                      measured_at=(base + timedelta(hours=4 * i)).strftime("%Y-%m-%dT%H:%M:%S"))
        for i, v in enumerate(values)
    ]


def test_predict_returns_requested_horizon(glucose_series):
    res = predict(glucose_series, horizon=5)
    assert len(res.predictions) == 5
    assert res.model == "linear-trend+ma"
    assert res.metrics is not None
    assert {"mae", "rmse", "slope_per_h"} <= set(res.metrics)


def test_predict_naive_when_insufficient_data():
    res = predict(_series([6.0, 6.5]), horizon=3)
    assert res.model == "naive-last"
    assert len(res.predictions) == 3


def test_predict_values_within_plausible_range():
    res = predict(_series([5, 6, 7, 8, 9, 10]), horizon=4)
    for p in res.predictions:
        assert 2.5 <= p.value_mmol <= 25.0


def test_predict_captures_upward_trend():
    res = predict(_series([5.0, 5.5, 6.0, 6.5, 7.0, 7.5]), horizon=3)
    # 明显上升趋势,预测值应不低于历史末值附近
    assert res.metrics["slope_per_h"] > 0
