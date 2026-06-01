<template>
  <div class="report-view">
    <el-card class="header-card">
      <div class="page-header">
        <h2>体检报告查看</h2>
        <p>智能解读体检报告，可视化展示健康指标变化趋势</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：报告列表 -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>我的报告</span>
          </template>
          <div v-for="(report, index) in reports" :key="index"
               class="report-item"
               :class="{ 'active': selectedReport?.id === report.id }"
               @click="selectReport(report)">
            <div class="report-date">{{ report.date }}</div>
            <div class="report-type">{{ report.type }}</div>
            <el-tag size="small" :type="report.status === '已解读' ? 'success' : 'info'">
              {{ report.status }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：报告详情 -->
      <el-col :span="18">
        <el-card v-if="selectedReport">
          <template #header>
            <div class="card-header">
              <span>{{ selectedReport.date }} - {{ selectedReport.type }}</span>
              <div>
                <el-button type="primary" @click="exportReport">
                  <el-icon><Download /></el-icon> 导出报告
                </el-button>
                <el-button @click="shareReport">
                  <el-icon><Share /></el-icon> 分享
                </el-button>
              </div>
            </div>
          </template>

          <!-- 报告摘要 -->
          <el-row :gutter="20" style="margin-bottom: 20px">
            <el-col :span="6">
              <el-statistic title="总评分" :value="selectedReport.score" :suffix="'分'">
                <template #suffix>
                  <span :style="{ color: selectedReport.score >= 80 ? '#67C23A' : '#F56C6C' }">分</span>
                </template>
              </el-statistic>
            </el-col>
            <el-col :span="6">
              <el-statistic title="异常项目" :value="selectedReport.abnormalCount" :suffix="'项'" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="正常项目" :value="selectedReport.normalCount" :suffix="'项'" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="建议复查" :value="selectedReport.reviewCount" :suffix="'项'" />
            </el-col>
          </el-row>

          <el-divider />

          <!-- 指标详情 -->
          <el-tabs v-model="activeCategory">
            <el-tab-pane label="全部指标" name="all">
              <el-table :data="selectedReport.items" style="width: 100%">
                <el-table-column prop="category" label="分类" width="120" />
                <el-table-column prop="name" label="项目名称" width="150" />
                <el-table-column prop="value" label="检测值" width="100" />
                <el-table-column prop="unit" label="单位" width="80" />
                <el-table-column prop="referenceRange" label="参考范围" width="120" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="说明">
                  <template #default="{ row }">
                    <span class="item-desc">{{ row.description }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="异常项目" name="abnormal">
              <div v-for="(item, index) in abnormalItems" :key="index" class="abnormal-item">
                <div class="abnormal-header">
                  <span class="item-name">{{ item.name }}</span>
                  <el-tag type="danger" size="small">{{ item.status }}</el-tag>
                </div>
                <div class="abnormal-value">
                  检测值: <strong>{{ item.value }} {{ item.unit }}</strong>
                  (参考范围: {{ item.referenceRange }})
                </div>
                <div class="abnormal-desc">
                  <el-alert :title="item.description" type="warning" :closable="false" show-icon />
                </div>
                <div class="abnormal-suggestion">
                  <strong>AI建议:</strong> {{ item.suggestion }}
                </div>
              </div>
              <el-empty v-if="!abnormalItems.length" description="没有异常项目" />
            </el-tab-pane>

            <el-tab-pane label="趋势分析" name="trend">
              <div ref="trendChart" style="height: 400px"></div>
            </el-tab-pane>

            <el-tab-pane label="AI解读" name="ai">
              <div class="ai-analysis">
                <el-alert title="AI智能解读" type="info" :closable="false" show-icon style="margin-bottom: 16px">
                  以下分析基于您的体检数据，由AI自动生成，仅供参考
                </el-alert>

                <div class="analysis-section">
                  <h4>整体健康状况</h4>
                  <p>{{ selectedReport.aiAnalysis.overall }}</p>
                </div>

                <div class="analysis-section">
                  <h4>重点关注项目</h4>
                  <ul>
                    <li v-for="(item, idx) in selectedReport.aiAnalysis.keyFindings" :key="idx">
                      {{ item }}
                    </li>
                  </ul>
                </div>

                <div class="analysis-section">
                  <h4>改善建议</h4>
                  <el-timeline>
                    <el-timeline-item
                      v-for="(rec, idx) in selectedReport.aiAnalysis.recommendations"
                      :key="idx"
                      :type="rec.urgent ? 'danger' : 'primary'"
                    >
                      {{ rec.content }}
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-empty v-else description="请从左侧选择一份报告查看" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Share } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const trendChart = ref(null)
const activeCategory = ref('all')
const selectedReport = ref(null)

const reports = ref([
  {
    id: 1, date: '2024-01-15', type: '年度体检', status: '已解读', score: 82,
    abnormalCount: 3, normalCount: 42, reviewCount: 1,
    items: [
      { category: '血常规', name: '白细胞计数', value: '6.5', unit: '10^9/L', referenceRange: '3.5-9.5', status: '正常', description: '正常范围' },
      { category: '血常规', name: '红细胞计数', value: '4.8', unit: '10^12/L', referenceRange: '4.3-5.8', status: '正常', description: '正常范围' },
      { category: '血糖', name: '空腹血糖', value: '6.8', unit: 'mmol/L', referenceRange: '3.9-6.1', status: '偏高', description: '略高于正常范围，需关注' },
      { category: '血糖', name: '糖化血红蛋白', value: '6.2', unit: '%', referenceRange: '4.0-6.0', status: '偏高', description: '反映近3个月平均血糖水平' },
      { category: '肝功能', name: '谷丙转氨酶', value: '35', unit: 'U/L', referenceRange: '0-40', status: '正常', description: '肝功能正常' },
      { category: '肾功能', name: '肌酐', value: '78', unit: 'umol/L', referenceRange: '44-133', status: '正常', description: '肾功能正常' },
      { category: '血脂', name: '总胆固醇', value: '5.8', unit: 'mmol/L', referenceRange: '2.8-5.7', status: '偏高', description: '略高于正常范围' },
      { category: '血压', name: '收缩压', value: '128', unit: 'mmHg', referenceRange: '90-139', status: '正常', description: '正常范围' },
      { category: '血压', name: '舒张压', value: '82', unit: 'mmHg', referenceRange: '60-89', status: '正常', description: '正常范围' },
      { category: '尿常规', name: '尿蛋白', value: '阴性', unit: '-', referenceRange: '阴性', status: '正常', description: '正常范围' }
    ],
    aiAnalysis: {
      overall: '您的整体健康状况良好，评分为82分。主要需要关注血糖和血脂指标，建议通过饮食调整和适当运动来改善。',
      keyFindings: [
        '空腹血糖6.8mmol/L，略高于正常范围（3.9-6.1），建议控制碳水化合物摄入',
        '糖化血红蛋白6.2%，接近正常上限，需持续监测',
        '总胆固醇5.8mmol/L，略高于正常范围，建议减少高脂食物摄入'
      ],
      recommendations: [
        { content: '建议每3个月复查一次空腹血糖和糖化血红蛋白', urgent: true },
        { content: '减少精制碳水化合物摄入，增加蔬菜和优质蛋白', urgent: true },
        { content: '每周进行150分钟中等强度有氧运动', urgent: false },
        { content: '减少高胆固醇食物摄入，如动物内脏、蛋黄等', urgent: false },
        { content: '保持规律作息，避免熬夜', urgent: false }
      ]
    }
  },
  {
    id: 2, date: '2023-07-20', type: '半年体检', status: '已解读', score: 85,
    abnormalCount: 2, normalCount: 43, reviewCount: 0,
    items: [], aiAnalysis: { overall: '整体状况良好', keyFindings: [], recommendations: [] }
  }
])

const abnormalItems = computed(() => {
  if (!selectedReport.value) return []
  return selectedReport.value.items.filter(item => item.status !== '正常')
})

const selectReport = (report) => {
  selectedReport.value = report
  if (report.items.length === 0) {
    // 模拟加载报告详情
    report.items = reports.value[0].items
    report.aiAnalysis = reports.value[0].aiAnalysis
  }
  nextTick(() => {
    if (activeCategory.value === 'trend') initTrendChart()
  })
}

watch(activeCategory, (val) => {
  if (val === 'trend') nextTick(() => initTrendChart())
})

const getStatusType = (status) => {
  const map = { '正常': 'success', '偏高': 'warning', '偏低': 'warning', '异常': 'danger' }
  return map[status] || 'info'
}

const initTrendChart = () => {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['空腹血糖', '总胆固醇', '收缩压'] },
    xAxis: { type: 'category', data: ['2023-01', '2023-07', '2024-01'] },
    yAxis: { type: 'value' },
    series: [
      { name: '空腹血糖', type: 'line', data: [5.8, 6.2, 6.8], smooth: true },
      { name: '总胆固醇', type: 'line', data: [5.2, 5.5, 5.8], smooth: true },
      { name: '收缩压', type: 'line', data: [125, 126, 128], smooth: true }
    ]
  })
}

const exportReport = () => {
  ElMessage.success('报告导出成功')
}

const shareReport = () => {
  ElMessage.success('分享链接已复制')
}
</script>

<style scoped>
.report-view { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.report-item {
  padding: 12px; border-radius: 8px; cursor: pointer;
  transition: all 0.3s; margin-bottom: 8px; border: 2px solid transparent;
}
.report-item:hover { background: #f5f7fa; }
.report-item.active { background: #ecf5ff; border-color: #409EFF; }
.report-date { font-weight: 500; color: #303133; margin-bottom: 4px; }
.report-type { font-size: 12px; color: #909399; margin-bottom: 4px; }

.item-desc { color: #909399; font-size: 12px; }

.abnormal-item {
  padding: 16px; background: #fdf6ec; border-radius: 8px;
  margin-bottom: 12px; border-left: 4px solid #E6A23C;
}
.abnormal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.item-name { font-weight: 500; color: #303133; }
.abnormal-value { color: #606266; margin-bottom: 8px; }
.abnormal-desc { margin-bottom: 8px; }
.abnormal-suggestion { color: #606266; font-size: 14px; }

.ai-analysis { padding: 16px; }
.analysis-section { margin-bottom: 20px; }
.analysis-section h4 { margin: 0 0 12px 0; color: #303133; }
.analysis-section p { color: #606266; line-height: 1.8; margin: 0; }
.analysis-section ul { margin: 0; padding-left: 20px; }
.analysis-section li { color: #606266; margin-bottom: 8px; line-height: 1.6; }
</style>
