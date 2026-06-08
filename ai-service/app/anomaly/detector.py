"""血糖异常检测:基于临床阈值 + 统计波动。

阈值参考(mmol/L):
- 低血糖:< 3.9 偏低;< 3.0 严重低血糖
- 空腹/餐前:>= 7.0 偏高
- 餐后/随机:>= 11.1 偏高
- 任意:>= 13.9 显著升高(高风险)
- 波动:相邻点变化或整体标准差过大
"""
from statistics import mean, pstdev

from app.schemas import AnomalyItem, GlucancePoint

LOW = 3.9
LOW_SEVERE = 3.0
HIGH_FASTING = 7.0
HIGH_POSTMEAL = 11.1
HIGH_SEVERE = 13.9
FLUCTUATION_STD = 3.0  # 整体标准差阈值

FASTING_PERIODS = {"FASTING", "BEFORE_MEAL", "BEDTIME"}


def _check_point(p: GlucancePoint) -> AnomalyItem | None:
    v = p.value_mmol
    if v < LOW_SEVERE:
        return AnomalyItem(measured_at=p.measured_at, value_mmol=v,
                           category="GLUCOSE_LOW", level="HIGH",
                           message=f"严重低血糖({v} mmol/L),请立即补充糖分并就医")
    if v < LOW:
        return AnomalyItem(measured_at=p.measured_at, value_mmol=v,
                           category="GLUCOSE_LOW", level="MEDIUM",
                           message=f"血糖偏低({v} mmol/L),注意补充碳水")
    if v >= HIGH_SEVERE:
        return AnomalyItem(measured_at=p.measured_at, value_mmol=v,
                           category="GLUCOSE_HIGH", level="HIGH",
                           message=f"血糖显著升高({v} mmol/L),建议尽快就医")
    high_threshold = HIGH_FASTING if p.period in FASTING_PERIODS else HIGH_POSTMEAL
    if v >= high_threshold:
        label = "空腹/餐前" if p.period in FASTING_PERIODS else "餐后/随机"
        return AnomalyItem(measured_at=p.measured_at, value_mmol=v,
                           category="GLUCOSE_HIGH", level="MEDIUM",
                           message=f"{label}血糖偏高({v} mmol/L,阈值 {high_threshold})")
    return None


def detect(history: list[GlucancePoint]) -> list[AnomalyItem]:
    anomalies: list[AnomalyItem] = []
    for p in history:
        item = _check_point(p)
        if item:
            anomalies.append(item)

    # 整体波动检测
    if len(history) >= 5:
        values = [p.value_mmol for p in history]
        std = pstdev(values)
        if std >= FLUCTUATION_STD:
            last = history[-1]
            anomalies.append(AnomalyItem(
                measured_at=last.measured_at, value_mmol=last.value_mmol,
                category="FLUCTUATION", level="MEDIUM",
                message=f"近期血糖波动较大(标准差 {std:.1f},均值 {mean(values):.1f}),建议规律监测"))
    return anomalies
