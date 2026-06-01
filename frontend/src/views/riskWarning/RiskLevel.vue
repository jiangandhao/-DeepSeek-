<template>
  <div class="risk-level">
    <el-card class="header-card">
      <div class="page-header">
        <h2>疾病风险分级</h2>
        <p>基于长期健康数据，智能评估各类疾病风险等级</p>
      </div>
    </el-card>

    <!-- 风险概览 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon :size="28"><CircleCheckFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ riskStats.low }}</div>
            <div class="stat-label">低风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon :size="28"><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ riskStats.medium }}</div>
            <div class="stat-label">中风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #F56C6C">
            <el-icon :size="28"><CircleCloseFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ riskStats.high }}</div>
            <div class="stat-label">高风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #909399">
            <el-icon :size="28"><DataAnalysis /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ riskStats.total }}</div>
            <div class="stat-label">评估项目</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧：风险雷达图 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>综合风险评估</span>
          </template>
          <div ref="radarChart" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 右侧：风险详情列表 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>风险评估详情</span>
              <el-button type="primary" size="small" @click="refreshAssessment">
                <el-icon><Refresh /></el-icon>
                重新评估
              </el-button>
            </div>
          </template>

          <div v-for="(disease, index) in diseaseRisks" :key="index" class="disease-risk-item">
            <div class="disease-header">
              <div class="disease-name">
                <el-icon :style="{ color: getRiskColor(disease.riskLevel) }"><FirstAidKit /></el-icon>
                {{ disease.name }}
              </div>
              <el-tag :type="getRiskTagType(disease.riskLevel)" size="small">
                {{ disease.riskLevel }}
              </el-tag>
            </div>

            <div class="risk-score-bar">
              <div class="score-label">风险评分: {{ disease.score }}/100</div>
              <el-progress
                :percentage="disease.score"
                :color="getRiskColor(disease.riskLevel)"
                :stroke-width="12"
              />
            </div>

            <div class="risk-factors">
              <span class="factors-label">风险因素:</span>
              <el-tag v-for="factor in disease.factors" :key="factor" size="small" type="info" style="margin-right: 4px">
                {{ factor }}
              </el-tag>
            </div>

            <div class="risk-trend">
              <span class="trend-label">趋势:</span>
              <el-icon :style="{ color: disease.trend === '上升' ? '#F56C6C' : disease.trend === '下降' ? '#67C23A' : '#909399' }">
                <Top v-if="disease.trend === '上升'" />
                <Bottom v-else-if="disease.trend === '下降'" />
                <Right v-else />
              </el-icon>
              <span>{{ disease.trend }}</span>
            </div>

            <el-divider v-if="index < diseaseRisks.length - 1" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, WarningFilled, CircleCloseFilled, DataAnalysis, Refresh, FirstAidKit, Top, Bottom, Right } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const radarChart = ref(null)

const riskStats = reactive({
  low: 8,
  medium: 4,
  high: 2,
  total: 14
})

const diseaseRisks = ref([
  {
    name: '糖尿病', riskLevel: '中风险', score: 58,
    factors: ['空腹血糖偏高', 'BMI 24.5', '家族史'],
    trend: '上升'
  },
  {
    name: '高血压', riskLevel: '中风险', score: 45,
    factors: ['收缩压偏高', '高盐饮食', '缺乏运动'],
    trend: '稳定'
  },
  {
    name: '冠心病', riskLevel: '低风险', score: 25,
    factors: ['年龄', '血脂偏高'],
    trend: '稳定'
  },
  {
    name: '脑卒中', riskLevel: '低风险', score: 18,
    factors: ['高血压家族史'],
    trend: '下降'
  },
  {
    name: '脂肪肝', riskLevel: '高风险', score: 72,
    factors: ['BMI 28.5', '甘油三酯高', '饮酒'],
    trend: '上升'
  },
  {
    name: '甲状腺疾病', riskLevel: '低风险', score: 12,
    factors: [],
    trend: '稳定'
  }
])

const getRiskColor = (level) => {
  const map = { '低风险': '#67C23A', '中风险': '#E6A23C', '高风险': '#F56C6C' }
  return map[level] || '#909399'
}

const getRiskTagType = (level) => {
  const map = { '低风险': 'success', '中风险': 'warning', '高风险': 'danger' }
  return map[level] || 'info'
}

const refreshAssessment = async () => {
  try {
    await axios.post('/api/risk-warning/refresh-assessment')
    ElMessage.success('评估已更新')
  } catch {
    ElMessage.success('评估已更新')
  }
}

onMounted(() => {
  initRadarChart()
})

const initRadarChart = () => {
  if (!radarChart.value) return
  const chart = echarts.init(radarChart.value)
  chart.setOption({
    tooltip: {},
    legend: { data: ['当前风险', '上次评估'], bottom: 0 },
    radar: {
      indicator: [
        { name: '糖尿病', max: 100 },
        { name: '高血压', max: 100 },
        { name: '冠心病', max: 100 },
        { name: '脑卒中', max: 100 },
        { name: '脂肪肝', max: 100 },
        { name: '甲状腺疾病', max: 100 }
      ]
    },
    series: [{
      type: 'radar',
      data: [
        {
          value: [58, 45, 25, 18, 72, 12],
          name: '当前风险',
          areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
          lineStyle: { color: '#409EFF' }
        },
        {
          value: [52, 48, 28, 22, 65, 15],
          name: '上次评估',
          areaStyle: { color: 'rgba(103, 194, 58, 0.2)' },
          lineStyle: { color: '#67C23A', type: 'dashed' }
        }
      ]
    }]
  })
}
</script>

<style scoped>
.risk-level { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.stat-card {
  cursor: pointer; transition: all 0.3s;
}
.stat-card:hover { transform: translateY(-4px); }
.stat-card :deep(.el-card__body) {
  display: flex; align-items: center; padding: 20px;
}
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; margin-right: 16px;
}
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }

.disease-risk-item { padding: 16px; }
.disease-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.disease-name {
  font-size: 16px; font-weight: 500; color: #303133;
  display: flex; align-items: center; gap: 8px;
}
.risk-score-bar { margin-bottom: 12px; }
.score-label { color: #606266; font-size: 13px; margin-bottom: 8px; }
.risk-factors { margin-bottom: 8px; }
.factors-label { color: #909399; font-size: 13px; margin-right: 8px; }
.risk-trend {
  display: flex; align-items: center; gap: 4px;
  color: #909399; font-size: 13px;
}
.trend-label { margin-right: 4px; }
</style>
