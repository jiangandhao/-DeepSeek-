from datetime import datetime

from app.anomaly.detector import detect
from app.schemas import GlucancePoint


def _p(v, period="FASTING"):
    return GlucancePoint(value_mmol=v, period=period,
                         measured_at=datetime(2026, 6, 1, 8, 0, 0).strftime("%Y-%m-%dT%H:%M:%S"))


def test_detect_severe_low():
    items = detect([_p(2.5)])
    assert any(a.category == "GLUCOSE_LOW" and a.level == "HIGH" for a in items)


def test_detect_low():
    items = detect([_p(3.5)])
    assert any(a.category == "GLUCOSE_LOW" and a.level == "MEDIUM" for a in items)


def test_detect_high_severe():
    items = detect([_p(14.5)])
    assert any(a.category == "GLUCOSE_HIGH" and a.level == "HIGH" for a in items)


def test_fasting_high_threshold():
    # 空腹 7.5 应判偏高,餐后 7.5 不应
    assert any(a.category == "GLUCOSE_HIGH" for a in detect([_p(7.5, "FASTING")]))
    assert not detect([_p(7.5, "AFTER_MEAL")])


def test_normal_no_anomaly():
    assert detect([_p(5.5, "FASTING")]) == []
