"""疾病风险评分(规则模型)。

综合长期血糖指标 + 用户画像,输出低/中/高风险分级、量化评分与可解释因子。
评分规则面向可解释性设计(便于论文论述),阈值参考临床常识。

eAG↔HbA1c 换算(ADAG 公式):eAG(mmol/L) = 1.59 × HbA1c(%) − 2.59
反推:HbA1c ≈ (eAG + 2.59) / 1.59
"""
from statistics import mean, pstdev

from app.schemas import GlucancePoint

FASTING_PERIODS = {"FASTING", "BEFORE_MEAL", "BEDTIME"}


def _estimate_hba1c(mean_glucose: float) -> float:
    return round((mean_glucose + 2.59) / 1.59, 1)


def score_risk(profile: dict, glucose: list[GlucancePoint]) -> dict:
    factors: list[str] = []
    score = 0
    metrics: dict = {}

    if glucose:
        values = [g.value_mmol for g in glucose]
        avg = mean(values)
        std = pstdev(values) if len(values) > 1 else 0.0
        hba1c = _estimate_hba1c(avg)
        high_cnt = sum(
            1 for g in glucose
            if (g.value_mmol >= 7.0 and g.period in FASTING_PERIODS)
            or (g.value_mmol >= 11.1 and g.period not in FASTING_PERIODS))
        low_cnt = sum(1 for g in glucose if g.value_mmol < 3.9)
        high_ratio = high_cnt / len(glucose)
        metrics = {"mean": round(avg, 1), "std": round(std, 2),
                   "est_hba1c": hba1c, "high_ratio": round(high_ratio, 2),
                   "low_count": low_cnt, "n": len(glucose)}

        # 平均血糖
        if avg >= 10:
            score += 3; factors.append(f"平均血糖很高({avg:.1f} mmol/L)")
        elif avg >= 7.8:
            score += 2; factors.append(f"平均血糖偏高({avg:.1f} mmol/L)")
        elif avg >= 6.1:
            score += 1; factors.append(f"平均血糖略高({avg:.1f} mmol/L)")

        # 估算 HbA1c
        if hba1c >= 9:
            score += 3; factors.append(f"估算糖化血红蛋白很高(~{hba1c}%)")
        elif hba1c >= 7.5:
            score += 2; factors.append(f"估算糖化血红蛋白偏高(~{hba1c}%)")
        elif hba1c >= 6.5:
            score += 1; factors.append(f"估算糖化血红蛋白达糖尿病诊断线(~{hba1c}%)")

        # 波动性
        if std >= 3:
            score += 2; factors.append(f"血糖波动大(标准差 {std:.1f})")
        elif std >= 2:
            score += 1; factors.append(f"血糖波动偏大(标准差 {std:.1f})")

        # 高血糖占比
        if high_ratio >= 0.5:
            score += 2; factors.append(f"超过半数记录偏高({high_ratio*100:.0f}%)")
        elif high_ratio >= 0.25:
            score += 1; factors.append(f"较多记录偏高({high_ratio*100:.0f}%)")

        # 低血糖事件
        if low_cnt > 0:
            score += 1; factors.append(f"存在 {low_cnt} 次低血糖事件")
    else:
        factors.append("缺少血糖记录,建议尽快开始监测")

    # 画像因子
    bmi = None
    h = profile.get("height_cm") or profile.get("身高")
    w = profile.get("weight_kg") or profile.get("体重")
    try:
        if h and w:
            bmi = round(float(w) / ((float(h) / 100) ** 2), 1)
            metrics["bmi"] = bmi
            if bmi >= 28:
                score += 2; factors.append(f"肥胖(BMI {bmi})")
            elif bmi >= 24:
                score += 1; factors.append(f"超重(BMI {bmi})")
    except (ValueError, ZeroDivisionError):
        pass

    age = profile.get("age") or profile.get("年龄")
    try:
        if age and int(age) >= 60:
            score += 1; factors.append("年龄 ≥ 60 岁")
    except (ValueError, TypeError):
        pass

    family = profile.get("family_history") or profile.get("家族病史")
    if family and str(family).strip() and str(family) not in ("无", "None"):
        score += 1; factors.append("有家族病史")

    chronic = profile.get("chronic") or profile.get("基础疾病")
    if chronic and str(chronic).strip() and str(chronic) not in ("无", "None"):
        score += 1; factors.append("合并基础/慢性疾病")

    level = "HIGH" if score >= 8 else "MEDIUM" if score >= 4 else "LOW"
    level_cn = {"HIGH": "高", "MEDIUM": "中", "LOW": "低"}[level]
    message = f"综合风险评分 {score},风险等级:{level_cn}风险。" + (
        "主要因素:" + "、".join(factors[:4]) if factors else "")

    return {"level": level, "score": score, "factors": factors,
            "metrics": metrics, "message": message}
