# 系统架构设计

## 1. 总体架构

系统采用「前后端分离 + 独立 AI 微服务」三层架构,各层职责单一、可独立部署与扩展。

```
┌─────────────┐   REST/SSE    ┌──────────────────┐   REST    ┌────────────────────┐
│  Vue3 前端   │ ─────────────▶│  Spring Boot 后端 │ ─────────▶│  Python FastAPI     │
│ Element Plus │               │  认证/CRUD/编排   │           │  DeepSeek/RAG/预测   │
│ ECharts      │◀───────────── │  网关与权限       │◀───────── │  异常检测            │
└─────────────┘     JSON       └────────┬─────────┘   JSON    └─────────┬──────────┘
                                        │                               │
                                ┌───────┴────────┐              ┌───────┴────────┐
                                │ MySQL │ Redis   │              │ 向量库/知识库   │ ──▶ DeepSeek API
                                └────────────────┘              └────────────────┘
```

## 2. 各层职责

| 层 | 技术 | 职责 |
|---|---|---|
| 前端 | Vue3 + Vite + Element Plus + ECharts | 交互、数据录入、趋势可视化、AI 流式对话 |
| 业务后端 | Spring Boot 3 + Spring Security + MyBatis-Plus | 认证授权(JWT)、数据 CRUD、AI 编排网关、权限/脱敏 |
| AI 服务 | FastAPI + httpx + numpy | DeepSeek 调用、RAG 检索、血糖预测、异常检测 |
| 存储 | MySQL / Redis / 向量库 | 结构化数据 / 缓存会话 / 知识库向量 |
| 大模型 | DeepSeek API(deepseek-chat) | 个性化方案生成、健康问答 |

**设计原则**:前端只与后端通信;后端是数据归属方与统一入口;AI 服务无状态,便于水平扩展。

## 3. 血糖管理智能体数据流

```
用户提问 / 生成方案
   │
   ▼
后端 AiService:汇总用户近 30 条血糖 + 饮食 + 运动 + 画像
   │ (构造请求,glucose 字段转 snake_case 对齐 AI 服务 schema)
   ▼
AI 服务 glucose_agent:
   ① _summarize 数据摘要(含异常检测结果)
   ② RAG retriever 检索 Top-K 医学知识(糖尿病饮食/运动/控制指南)
   ③ 组装结构化 Prompt(系统角色 + 数据 + 知识 + 任务)
   ④ 调用 DeepSeek(流式/非流式)
   ▼
SSE 流式回传:AI 服务 → 后端(WebClient 透传)→ 前端(fetch 读流)
   ▼
非流式结果落库 ai_advice 表,供历史回看
```

## 4. 关键技术选型说明

- **RAG 检索**:首期采用「中文字符 bigram + TF-IDF 余弦」轻量检索(仅依赖 numpy),Docker 镜像小、启动快、无需 GPU。接口 `retriever.retrieve()` 保持稳定,后续可平滑升级为 `sentence-transformers(bge-zh)+ ChromaDB` 语义检索(见 `requirements.txt` 注释)。
- **血糖预测**:线性趋势 + 残差校正基线(`app/prediction/predictor.py`),返回样本内 MAE/RMSE 指标。`notebooks/glucose_forecast_experiment.py` 提供与 Naive/MovingAvg 的对比实验,供论文引用。
- **异常检测**:基于临床阈值(低血糖 < 3.9、空腹 ≥ 7.0、餐后 ≥ 11.1、显著升高 ≥ 13.9)+ 统计波动(标准差),分 LOW/MEDIUM/HIGH 三级。
- **流式对话**:DeepSeek SSE → FastAPI StreamingResponse → Spring WebClient `bodyToFlux` → Spring MVC SSE → 前端 fetch reader,全链路流式。
- **DeepSeek 客户端**:封装于 `app/deepseek/client.py`,兼容 OpenAI `/chat/completions` 协议,支持 `httpx[socks]`(适配存在 SOCKS 代理的部署环境)与 function calling 预留。

## 5. 安全与数据治理

- **认证**:JWT 无状态鉴权,密钥与有效期可配;`JwtAuthFilter` 解析后注入 `SecurityContext`,`UserContext` 提供当前用户ID。
- **越权防护**:所有记录的查询/删除均校验 `userId` 归属。
- **隐私脱敏**:敏感字段(手机号等)加密存储,前端按权限脱敏展示(数据治理章节)。
- **统一异常**:后端 `GlobalExceptionHandler` 将 AI 服务错误(WebClient 502)转为可读提示;AI 服务对 DeepSeek 错误统一转 502 + 操作建议。

## 6. 可扩展性(面向高并发的设计)

针对「千万级用户」的论述性设计(毕设以架构方案 + 小规模压测佐证):
- **无状态服务**:后端 JWT + AI 服务无状态 → 可水平扩展,前置 Nginx/LB 负载均衡。
- **缓存**:Redis 缓存热点数据与会话,降低 DB 压力。
- **数据库**:读写分离、按 user_id 分库分表;时序血糖数据可冷热分层。
- **削峰**:AI 生成等耗时任务可经消息队列(如 RocketMQ/Kafka)异步化。
- **静态资源**:前端走 CDN。

## 7. 扩展模块(阶段4/5)

- **AI 数智健管师**(`ai-service/app/agents/health_manager.py`):复用血糖智能体的数据摘要与 RAG,叠加用户画像/家族史 + 风险评分,生成风险解读 + 饮食处方 + 运动计划。
- **疾病风险预警**(`ai-service/app/risk/scorer.py`):可解释规则模型,综合平均血糖、估算 HbA1c(ADAG 公式)、波动性、高血糖占比、BMI、年龄、家族史等打分,输出低/中/高分级;后端对中/高风险落库 `alert` 表。
- **智能体检优化**(后端 `ExamService`):haversine 经纬度距离 + 机构繁忙度加权(距离60% + 繁忙40%)排序推荐,支持预约管理。
- **影像识别**(`ai-service/app/imaging/detector.py`):肺结节检测演示,经典 CV 流程(高斯平滑 + 阈值分割 + 连通域 + 圆度筛选)输出候选框 + 标注图 + 报告;接口可平滑替换为 LUNA16 预训练 CNN。
- **隐私脱敏**(后端 `AesUtil` + `EncryptTypeHandler`):敏感字段(手机号)AES 透明加密落库,API 按权限脱敏展示(`138****5678`),原文不出后端。
- **联邦学习 Demo**(`ai-service/notebooks/federated_learning_demo.py`):FedAvg 模拟多医院本地训练 + 参数聚合,数据不出域,效果接近集中式、优于各端单训。

## 8. 部署

`deploy/docker-compose.yml` 一键编排 mysql / redis / ai-service / backend / frontend 五个服务,`init.sql` 自动建表。详见根目录 `README.md`。
