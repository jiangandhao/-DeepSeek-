"""pytest 配置:把项目根加入 sys.path,便于 import app.*。"""
import sys
from datetime import datetime, timedelta
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.schemas import GlucancePoint  # noqa: E402


@pytest.fixture
def glucose_series():
    """构造一段升序血糖序列,含高/低/正常值。"""
    base = datetime(2026, 6, 1, 8, 0, 0)
    vals = [(5.5, "FASTING"), (9.8, "AFTER_MEAL"), (6.2, "FASTING"),
            (11.5, "AFTER_MEAL"), (3.5, "FASTING"), (14.2, "AFTER_MEAL"),
            (6.0, "BEDTIME"), (7.1, "FASTING")]
    return [
        GlucancePoint(value_mmol=v, period=p,
                      measured_at=(base + timedelta(hours=4 * i)).strftime("%Y-%m-%dT%H:%M:%S"))
        for i, (v, p) in enumerate(vals)
    ]
