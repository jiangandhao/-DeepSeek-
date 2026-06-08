# 前端 (Vue 3 + Vite)

## 本地开发

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173,/api 代理到 localhost:8080
```

## 构建

```bash
npm run build    # 产物在 dist/
```

## 页面

| 路由 | 页面 |
|---|---|
| `/dashboard` | 血糖概览(ECharts 趋势图 + 统计) |
| `/assistant` | AI 血糖智能体(流式对话)+ 预测/预警 |
| `/health-manager` | 数智健管师(档案/风险仪表盘/AI 处方/预警) |
| `/exam` | 智能体检推荐与预约 |
| `/imaging` | 影像识别(肺结节检测) |
| `/glucose` `/diet` `/exercise` | 数据录入管理 |
| `/account` | 账户设置(手机号脱敏展示) |

技术:Element Plus、Pinia、Vue Router、Axios、ECharts、marked。
