# 交付清单与验收

## 一、可运行系统(三端 + Docker)

| 组件 | 路径 | 启动 | 状态 |
|---|---|---|---|
| 前端 Vue3 | `frontend/` | 容器 :5173 | ✅ 回归通过 |
| 后端 Spring Boot | `backend/` | 容器 :8080 | ✅ 回归通过 |
| AI 服务 FastAPI | `ai-service/` | 容器 :8000 | ✅ 回归通过 |
| MySQL / Redis | `deploy/` | 容器 :3306 / :6379 | ✅ healthy |

一键启动:`cd deploy && docker compose --env-file ../.env up -d --build`

## 二、功能模块(阶段 0–5 全部完成并端到端验证)

| 模块 | 说明 | 验证 |
|---|---|---|
| 用户认证 | 注册/登录 JWT,越权防护 | ✅ |
| 血糖/饮食/运动管理 | CRUD + 时间范围查询 | ✅ |
| 血糖概览 | ECharts 趋势图(正常区间/异常点) | ✅ |
| 血糖管理智能体 | RAG + DeepSeek 流式对话 | ✅ 真实 key |
| 血糖预测 | 线性趋势基线 + 指标 | ✅ |
| 异常预警 | 临床阈值 + 波动分级 | ✅ |
| AI 数智健管师 | 风险评估 + 个性化处方 | ✅ 真实 key |
| 疾病风险预警 | 可解释评分分级 + 落库 | ✅ |
| 智能体检 | 距离+繁忙度匹配 + 预约 | ✅ |
| 影像识别 | 肺结节检测 + 可视化 + 报告 | ✅ |
| 隐私脱敏 | AES 加密存储 + 脱敏展示 | ✅ DB密文/API脱敏 |
| 账户设置 | 个人信息 + 手机号脱敏 | ✅ |

## 三、技术亮点(对应原始要求)

- **DeepSeek 医疗适配**:提示工程 + RAG 检索增强(无需微调/GPU)✅
- **多模态融合**:结构化指标 + 影像结果统一进入评估 ✅(架构)
- **隐私脱敏**:AES 字段加密 + 脱敏展示(真实实现)✅
- **联邦学习**:FedAvg 最小 Demo,数据不出域 ✅ 可运行
- **影像识别**:肺结节检测演示(可升级 LUNA16 CNN)✅
- **千万级并发**:无状态+LB+读写分离+缓存+MQ 架构论述 + 压测(~1014 QPS)✅
- **数据治理**:脱敏 + AI 决策可追溯(`ai_advice.context`)✅

## 四、测试

| 范围 | 命令 | 结果 |
|---|---|---|
| AI 服务单测 | `cd ai-service && python -m pytest` | 20 passed |
| 后端单测 | `mvn test`(AesUtil/ExamService) | 7 passed |
| 全栈回归 | docker compose + 各模块 e2e | 全 200 |
| 并发压测 | `python deploy/loadtest.py` | ~1014 QPS / p95 112ms |

## 五、论文素材

| 素材 | 位置 |
|---|---|
| 系统架构 | `docs/architecture.md` |
| 接口文档 | `docs/api.md` |
| 数据库设计 | `docs/database.md` |
| 高并发与压测 | `docs/high-concurrency.md` |
| 论文大纲与亮点 | `docs/thesis-outline.md` |
| 预测模型对比实验 | `ai-service/notebooks/glucose_forecast_experiment.py` |
| 联邦学习对比实验 | `ai-service/notebooks/federated_learning_demo.py` |
| 合成数据生成 | `ai-service/data/generate_synthetic.py` |
| 演示数据导入 | `deploy/seed_demo.py` |

## 六、演示账号

`demo / demo123`(运行 `python deploy/seed_demo.py` 导入演示数据)。
AI 对话/方案需在 `.env` 配置真实 `DEEPSEEK_API_KEY`(已配置)。
