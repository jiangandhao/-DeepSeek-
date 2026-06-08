from app.rag.retriever import retriever


def test_knowledge_loaded():
    assert len(retriever.chunks) > 0
    assert len(retriever.vocab) > 0


def test_retrieve_returns_relevant():
    results = retriever.retrieve("餐后血糖高怎么运动", k=3)
    assert len(results) >= 1
    # 分数降序
    scores = [r["score"] for r in results]
    assert scores == sorted(scores, reverse=True)
    # 运动相关查询应命中 exercise 知识
    assert any(r["source"] == "exercise" for r in results)


def test_retrieve_diet_query():
    results = retriever.retrieve("糖尿病饮食 主食 碳水", k=2)
    assert any(r["source"] == "diet" for r in results)


def test_retrieve_empty_query_safe():
    results = retriever.retrieve("xyzqwerty不存在词", k=3)
    assert isinstance(results, list)
