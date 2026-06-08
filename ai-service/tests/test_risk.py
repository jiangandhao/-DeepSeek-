from datetime import datetime, timedelta

from app.risk.scorer import score_risk
from app.schemas import GlucancePoint


def _series(values, period="AFTER_MEAL"):
    base = datetime(2026, 6, 1, 8, 0, 0)
    return [
        GlucancePoint(value_mmol=v, period=period,
                      measured_at=(base + timedelta(hours=4 * i)).strftime("%Y-%m-%dT%H:%M:%S"))
        for i, v in enumerate(values)
    ]


def test_high_risk_profile():
    glucose = _series([11, 12, 13, 10, 14, 12, 11, 13])
    profile = {"age": 65, "height_cm": 170, "weight_kg": 90,
               "family_history": "父亲糖尿病", "chronic": "高血压"}
    r = score_risk(profile, glucose)
    assert r["level"] == "HIGH"
    assert r["score"] >= 8
    assert r["metrics"]["bmi"] > 28
    assert any("家族" in f for f in r["factors"])


def test_low_risk_profile():
    glucose = _series([5.2, 5.5, 5.0, 5.3, 5.1], period="FASTING")
    profile = {"age": 30, "height_cm": 175, "weight_kg": 65}
    r = score_risk(profile, glucose)
    assert r["level"] == "LOW"


def test_hba1c_estimate_present():
    r = score_risk({}, _series([7, 8, 7.5, 8.2]))
    assert "est_hba1c" in r["metrics"]
    assert r["metrics"]["est_hba1c"] > 0


def test_empty_glucose_handled():
    r = score_risk({}, [])
    assert r["level"] in ("LOW", "MEDIUM", "HIGH")
    assert isinstance(r["factors"], list)
