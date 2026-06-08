"""轻量 RAG 检索器。

默认实现:中文字符 bigram + 英文词 的 TF-IDF 余弦检索(仅依赖 numpy,
无需 GPU/大模型 embedding,Docker 镜像小、启动快)。

升级路径:将 _tokenize / 向量化替换为 sentence-transformers(bge-zh)+ ChromaDB
即可获得语义检索;接口 retrieve() 保持不变。见 requirements.txt 注释。
"""
import math
import re
from dataclasses import dataclass
from pathlib import Path

import numpy as np

KNOWLEDGE_DIR = Path(__file__).parent / "knowledge"
CJK = re.compile(r"[一-鿿]")
ASCII_WORD = re.compile(r"[a-zA-Z]+")


@dataclass
class Chunk:
    source: str
    title: str
    text: str


def _tokenize(text: str) -> list[str]:
    tokens: list[str] = []
    # 中文字符 bigram
    cjk_chars = CJK.findall(text)
    for i in range(len(cjk_chars) - 1):
        tokens.append(cjk_chars[i] + cjk_chars[i + 1])
    # 英文单词
    tokens.extend(w.lower() for w in ASCII_WORD.findall(text))
    return tokens


def _load_chunks() -> list[Chunk]:
    chunks: list[Chunk] = []
    if not KNOWLEDGE_DIR.exists():
        return chunks
    for md in sorted(KNOWLEDGE_DIR.glob("*.md")):
        content = md.read_text(encoding="utf-8")
        # 按 ## 二级标题切分
        parts = re.split(r"\n(?=## )", content)
        for part in parts:
            part = part.strip()
            if not part:
                continue
            first_line = part.splitlines()[0].lstrip("# ").strip()
            chunks.append(Chunk(source=md.stem, title=first_line, text=part))
    return chunks


class Retriever:
    def __init__(self) -> None:
        self.chunks = _load_chunks()
        self.vocab: dict[str, int] = {}
        self.idf: np.ndarray = np.zeros(0)
        self.matrix: np.ndarray = np.zeros((0, 0))
        if self.chunks:
            self._build_index()

    def _build_index(self) -> None:
        docs_tokens = [_tokenize(c.text) for c in self.chunks]
        # 构建词表
        for toks in docs_tokens:
            for t in set(toks):
                if t not in self.vocab:
                    self.vocab[t] = len(self.vocab)
        n_docs = len(self.chunks)
        n_terms = len(self.vocab)
        df = np.zeros(n_terms)
        tf = np.zeros((n_docs, n_terms))
        for i, toks in enumerate(docs_tokens):
            for t in toks:
                tf[i, self.vocab[t]] += 1
            for t in set(toks):
                df[self.vocab[t]] += 1
        self.idf = np.log((1 + n_docs) / (1 + df)) + 1
        self.matrix = self._normalize(tf * self.idf)

    @staticmethod
    def _normalize(m: np.ndarray) -> np.ndarray:
        norms = np.linalg.norm(m, axis=1, keepdims=True)
        norms[norms == 0] = 1
        return m / norms

    def _vectorize(self, text: str) -> np.ndarray:
        vec = np.zeros(len(self.vocab))
        for t in _tokenize(text):
            idx = self.vocab.get(t)
            if idx is not None:
                vec[idx] += 1
        vec = vec * self.idf
        norm = np.linalg.norm(vec)
        return vec / norm if norm > 0 else vec

    def retrieve(self, query: str, k: int = 3) -> list[dict]:
        if not self.chunks:
            return []
        qv = self._vectorize(query)
        scores = self.matrix @ qv
        top = np.argsort(scores)[::-1][:k]
        results = []
        for i in top:
            if scores[i] <= 0:
                continue
            c = self.chunks[i]
            results.append({"source": c.source, "title": c.title,
                            "text": c.text, "score": round(float(scores[i]), 4)})
        return results


# 单例(知识库静态,启动时构建一次)
retriever = Retriever()
