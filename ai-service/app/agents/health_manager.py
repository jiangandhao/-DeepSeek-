"""AI 数智健管师编排。

在血糖智能体基础上,综合用户画像/家族史 + 风险评分,生成:
风险评估解读 + 个性化饮食处方 + 运动计划调整。
复用 glucose_agent 的数据摘要与 RAG 检索。
"""
from typing import AsyncIterator

from app.agents.glucose_agent import _summarize
from app.deepseek.client import deepseek_client
from app.rag.retriever import retriever
from app.risk.scorer import score_risk
from app.schemas import AdviceRequest

SYSTEM_PROMPT = (
    "你是一名资深的 AI 数智健管师,擅长慢病(尤其糖尿病)风险评估与健康管理。"
    "请综合用户的画像/家族史、血糖/饮食/运动数据以及【风险评估结果】和【医学知识】,"
    "输出一份专业、个性化、可执行的健康管理方案,包含:\n"
    "1. **风险解读**:解释当前风险等级与主要风险因素;\n"
    "2. **饮食处方**:具体的营养搭配建议(主食/蛋白/蔬菜/进餐顺序);\n"
    "3. **运动计划**:类型、强度、频率、时机的个性化推荐;\n"
    "4. **监测与随访建议**。\n"
    "要求 Markdown 分节、通俗易懂;不开具处方药剂量,涉及用药请提示遵医嘱;语气专业而温和。"
)


def _build_messages(req: AdviceRequest) -> tuple[list[dict], str, dict]:
    risk = score_risk(req.profile, req.glucose)
    summary = _summarize(req)

    query = req.question or "糖尿病 风险评估 个性化 饮食处方 运动计划 并发症预防"
    retrieved = retriever.retrieve(query, k=3)
    knowledge = "\n\n".join(f"【{r['source']}/{r['title']}】\n{r['text']}" for r in retrieved)

    risk_text = (f"风险等级:{risk['level']},评分:{risk['score']}。"
                 f"风险因素:{'、'.join(risk['factors']) or '无明显因素'}。"
                 f"关键指标:{risk['metrics']}")

    task = req.question or "请基于以上信息,给出我的疾病风险解读与个性化健康管理方案。"
    user_content = (
        f"## 我的健康数据\n{summary}\n\n"
        f"## 风险评估结果\n{risk_text}\n\n"
        f"## 可参考的医学知识\n{knowledge or '(无)'}\n\n"
        f"## 我的需求\n{task}"
    )
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]
    context = f"风险:{risk_text}\n数据摘要:\n{summary}\n检索:" + "; ".join(
        f"{r['source']}/{r['title']}" for r in retrieved)
    return messages, context, risk


async def generate_health_plan(req: AdviceRequest) -> dict:
    messages, context, risk = _build_messages(req)
    content = await deepseek_client.chat_content(messages)
    return {"content": content, "context": context, "risk": risk}


async def stream_health_plan(req: AdviceRequest) -> AsyncIterator[str]:
    messages, _, _ = _build_messages(req)
    async for piece in deepseek_client.stream_chat(messages):
        yield piece
