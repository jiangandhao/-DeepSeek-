# DeepSeek 全流程健康管理系统

融合 DeepSeek 大模型与医疗健康数据，构建全流程健康管理体系，实现血糖管理、体检优化及疾病预警的智能化与个性化。

## 项目架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端应用层 (Vue.js)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │血糖管理   │ │AI健管师   │ │智能体检   │ │风险预警   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
├─────────────────────────────────────────────────────────────┤
│                      后端服务层 (Java Spring Boot)            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │血糖管理   │ │健康管家   │ │体检服务   │ │预警服务   │       │
│  │Controller│ │Controller│ │Controller│ │Controller│       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
├─────────────────────────────────────────────────────────────┤
│                      AI模型层 (Python)                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                    │
│  │血糖预测   │ │风险评估   │ │影像分析   │  DeepSeek LLM     │
│  │模型      │ │模型      │ │模型      │  + 行业微调        │
│  └──────────┘ └──────────┘ └──────────┘                    │
├─────────────────────────────────────────────────────────────┤
│                      数据层 (MySQL + Redis)                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │用户数据   │ │健康档案   │ │体检报告   │ │预警记录   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
```

## 功能模块

### 1. 血糖管理AI智能体
- 血糖数据记录与可视化
- 血糖趋势分析与预测
- AI饮食调整建议
- AI运动方案推荐
- 异常波动预警

### 2. AI数智健管师
- 健康风险评估
- 个性化饮食处方
- 智能运动推荐
- 疾病档案管理
- AI健康咨询

### 3. 智能体检优化
- 智能体检预约（地理位置+繁忙度匹配）
- 体检报告查看与AI解读
- 影像智能分析（如肺结节检测）
- 报告可视化展示

### 4. 疾病风险预警
- 疾病风险分级评估
- 实时预警通知
- 个性化预防方案
- 健康趋势监控

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| **前端** | Vue 3 + Element Plus + ECharts + Pinia |
| **后端** | Java Spring Boot 3.1 + MyBatis-Plus |
| **AI模型** | Python + PyTorch + TensorFlow + DeepSeek API |
| **数据库** | MySQL 8.0 + Redis |
| **部署** | Docker + Nginx |

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- Python 3.8+
- MySQL 8.0+
- Docker & Docker Compose (可选)

### 1. 克隆项目
```bash
git clone <repository-url>
cd deepseek-health-management
```

### 2. 启动数据库
```bash
# 使用Docker启动MySQL
docker run -d --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=health_management \
  mysql:8.0

# 导入初始化脚本
mysql -u root -p health_management < data/database/init.sql
```

### 3. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```

### 4. 启动AI模型服务
```bash
cd ai-model
pip install -r requirements.txt
python inference/api.py
```

### 5. 启动前端服务
```bash
cd frontend
npm install
npm run serve
```

### 6. 使用Docker Compose一键启动
```bash
cd deploy
docker-compose up -d
```

## 项目结构

```
deepseek-health-management/
├── frontend/                          # 前端应用 (Vue.js)
│   ├── src/
│   │   ├── api/                       # API接口
│   │   │   ├── bloodSugar.js          # 血糖管理API
│   │   │   ├── healthManager.js       # 健管师API
│   │   │   ├── checkup.js             # 体检服务API
│   │   │   └── riskWarning.js         # 预警服务API
│   │   ├── views/
│   │   │   ├── bloodSugar/            # 血糖管理模块
│   │   │   │   └── Dashboard.vue
│   │   │   ├── healthManager/         # AI健管师模块
│   │   │   │   ├── RiskAssessment.vue
│   │   │   │   ├── DietPrescription.vue
│   │   │   │   ├── ExerciseRecommend.vue
│   │   │   │   └── DiseaseManagement.vue
│   │   │   ├── checkup/               # 智能体检模块
│   │   │   │   ├── Appointment.vue
│   │   │   │   ├── ReportView.vue
│   │   │   │   └── ImageAnalysis.vue
│   │   │   └── riskWarning/           # 风险预警模块
│   │   │       ├── RiskLevel.vue
│   │   │       ├── EarlyWarning.vue
│   │   │       └── PreventionPlan.vue
│   │   ├── router/                    # 路由配置
│   │   └── App.vue                    # 主组件
│   └── package.json
│
├── backend/                           # 后端服务 (Java Spring Boot)
│   └── src/main/java/com/health/
│       ├── controller/                # 控制器
│       │   ├── BloodSugarController.java
│       │   ├── HealthManagerController.java
│       │   ├── CheckupController.java
│       │   └── RiskWarningController.java
│       ├── service/                   # 服务层
│       │   ├── BloodSugarService.java
│       │   ├── HealthManagerService.java
│       │   ├── CheckupService.java
│       │   └── RiskWarningService.java
│       ├── entity/                    # 实体类
│       └── mapper/                    # 数据访问层
│
├── ai-model/                          # AI模型 (Python)
│   ├── deepseek/                      # DeepSeek客户端
│   ├── models/                        # 模型定义
│   │   ├── blood_sugar_model.py
│   │   └── risk_assessment_model.py
│   ├── inference/                     # 推理API
│   └── training/                      # 训练脚本
│
├── data/                              # 数据层
│   └── database/
│       └── init.sql                   # 数据库初始化脚本
│
└── deploy/                            # 部署配置
    ├── docker-compose.yml
    └── nginx/
```

## API接口文档

### 血糖管理
- `GET /api/blood-sugar/records` - 获取血糖记录
- `POST /api/blood-sugar/records` - 添加血糖记录
- `GET /api/blood-sugar/trend` - 获取趋势数据
- `POST /api/blood-sugar/diet/recommendation` - 获取饮食建议

### AI健管师
- `POST /api/health-manager/risk-assessment` - 健康风险评估
- `POST /api/health-manager/diet-prescription` - 生成饮食处方
- `POST /api/health-manager/exercise-recommend` - 生成运动方案
- `POST /api/health-manager/consult` - AI健康咨询

### 智能体检
- `POST /api/checkup/search-centers` - 搜索体检中心
- `POST /api/checkup/appointment` - 创建预约
- `GET /api/checkup/reports` - 获取报告列表
- `POST /api/checkup/image-analysis` - 影像分析

### 风险预警
- `GET /api/risk-warning/assessment` - 获取风险评估
- `GET /api/risk-warning/warnings` - 获取预警列表
- `GET /api/risk-warning/prevention-plans` - 获取预防方案

## 开发团队

华中科技大学 2026年大三实训案例五

## 许可证

本项目仅用于教学实训目的
