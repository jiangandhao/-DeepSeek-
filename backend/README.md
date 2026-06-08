# 业务后端 (Spring Boot 3)

认证授权(JWT)、健康数据 CRUD、AI 编排网关、智能体检、风险预警、隐私脱敏。

## 本地开发(需 JDK 17 + Maven)

```bash
cd backend
mvn spring-boot:run
```

依赖 MySQL(localhost:3306/health)与 Redis(localhost:6379)。可先用 `docker compose up -d mysql redis` 起依赖。

接口文档:启动后访问 http://localhost:8080/doc.html

## 测试

```bash
mvn test
# 无本地 Maven 时可用 Docker:
docker run --rm -v "$(pwd)":/app -v health-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn test
```

覆盖:`AesUtilTest`(字段加解密/脱敏)、`ExamServiceTest`(haversine 距离与匹配排序)。

## 关键包

| 包 | 职责 |
|---|---|
| `controller` | REST 接口 |
| `service` | 业务逻辑(含 `AiService` 编排、`ExamService` 匹配、`AuthService`) |
| `security` | JWT 生成/过滤/上下文 |
| `common` | 统一响应、异常、`AesUtil` 加密、`EncryptTypeHandler` |
| `config` | Security、MyBatis-Plus、WebClient、Crypto |
| `entity`/`mapper`/`dto` | 数据模型与映射 |
