# 高并发架构设计与压测

> 面向「千万级用户」的可扩展性设计论述,辅以单机小规模压测数据佐证。

## 1. 可扩展架构方案

```
            ┌──────────── CDN(前端静态资源 / 影像) ────────────┐
用户 ──▶ LB(Nginx/SLB)──┬──▶ 后端实例×N(无状态,JWT)──┬──▶ MySQL 主(写)
                         │                              │      └─▶ MySQL 从×N(读,读写分离)
                         │                              ├──▶ Redis 集群(缓存/会话/热点)
                         └──▶ AI 服务×N(无状态)         └──▶ MQ(Kafka/RocketMQ,AI生成等耗时任务削峰)
                                  └──▶ DeepSeek API / 向量库
```

### 关键设计点
1. **无状态服务**:后端基于 JWT 鉴权、AI 服务本身无状态 → 可任意水平扩容,LB 轮询/最少连接分发。
2. **读写分离 + 分库分表**:血糖等时序数据以 `user_id` 为分片键水平拆分;读多写少,配置多个只读从库。
3. **多级缓存**:Redis 缓存用户画像、最近血糖、AI 方案等热点数据,降低 DB 压力;本地缓存兜底。
4. **异步削峰**:AI 方案生成、影像识别、风险评估等耗时操作经消息队列异步化,前端轮询/推送结果,避免线程阻塞。
5. **CDN**:前端构建产物与影像静态资源走 CDN,就近访问。
6. **限流降级**:网关层令牌桶限流,AI 服务故障时降级为规则引擎(预测/异常检测本就不依赖 LLM)。
7. **可观测性**:actuator + Prometheus/Grafana 监控 QPS/延迟/错误率。

### 容量估算思路
千万级注册用户中,日活按 10% = 100 万,峰值集中在早/晚测血糖时段。按峰值 QPS 估算所需后端实例数 = 峰值QPS / 单实例QPS / 安全系数,结合下方单机基准线性外推并预留冗余。

## 2. 单机压测(基准数据)

环境:本机 Docker Compose 单实例(后端 1 + MySQL 1 + Redis 1),压测脚本 `deploy/loadtest.py`。

```bash
python deploy/loadtest.py --requests 600 --concurrency 50
```

示例结果(并发 50,各 600 请求):

| 接口 | QPS | 成功率 | p50 | p95 | p99 |
|---|---|---|---|---|---|
| GET /api/glucose(鉴权 + 查询84条记录) | ~1014 | 100% | 40ms | 112ms | 139ms |
| GET /actuator/health | ~2535 | 100% | 17ms | 28ms | 34ms |

> 单实例即可稳定支撑约千级 QPS 的带库查询;按无状态水平扩展 + 读写分离 + 缓存,可线性提升至万级以上,叠加分库分表与异步削峰支撑更大规模。

## 3. JMeter 压测(可选,论文规范化)

亦可用 Apache JMeter 编排更完整的压测场景:
1. 新建 Thread Group(线程数=并发用户,Ramp-up,循环次数)。
2. HTTP Request Defaults 设置服务器 `localhost:8080`。
3. 前置 HTTP 请求登录获取 token → JSON Extractor 提取 `data.token` → HTTP Header Manager 注入 `Authorization: Bearer ${token}`。
4. 添加业务请求(查血糖/录入/风险评估),加 Summary Report、Aggregate Report、Response Times Over Time 监听器。
5. 命令行非 GUI 运行:`jmeter -n -t plan.jmx -l result.jtl -e -o report/`。

关注指标:Throughput(吞吐)、Average/90%/99% Line、Error %。
