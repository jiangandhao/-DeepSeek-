<template>
  <div class="report-view">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>体检报告</span>
              <div>
                <el-button @click="exportReport">
                  <el-icon><Download /></el-icon> 导出PDF
                </el-button>
                <el-button type="primary" @click="analyzeReport" :loading="analyzing">
                  <el-icon><MagicStick /></el-icon> AI解读报告
                </el-button>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="报告概览" name="overview">
              <div class="report-header">
                <h3>{{ reportInfo.hospital }} 体检报告</h3>
                <p>体检日期: {{ reportInfo.date }} | 报告编号: {{ reportInfo.reportNo }}</p>
              </div>

              <el-divider />

              <h4>总体评估</h4>
              <div class="overall-result">
                <div class="result-icon" :class="reportInfo.overallLevel">
                  <el-icon :size="48"><component :is="reportInfo.overallIcon" /></el-icon>
                </div>
                <div class="result-text">
                  <h3>{{ reportInfo.overallText }}</h3>
                  <p>{{ reportInfo.summary }}</p>
                </div>
              </div>

              <el-divider />

              <h4>异常指标汇总</h4>
              <el-table :data="abnormalItems" border>
                <el-table-column prop="category" label="检查项目" width="150" />
                <el-table-column prop="item" label="指标名称" width="180" />
                <el-table-column prop="result" label="检查结果" width="120">
                  <template #default="{ row }">
                    <span class="abnormal-value">{{ row.result }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="reference" label="参考范围" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '偏高' ? 'danger' : 'warning'" size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="建议" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="详细报告" name="detail">
              <el-collapse v-model="activeCategories">
                <el-collapse-item v-for="category in reportDetails" :key="category.name" :title="category.name" :name="category.name">
                  <el-table :data="category.items" border size="small">
                    <el-table-column prop="item" label="项目" width="180" />
                    <el-table-column prop="result" label="结果" width="120">
                      <template #default="{ row }">
                        <span :class="{ 'abnormal': row.isAbnormal }">{{ row.result }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column prop="reference" label="参考范围" width="120" />
                    <el-table-column prop="status" label="状态" width="80">
                      <template #default="{ row }">
                        <el-tag :type="row.isAbnormal ? 'danger' : 'success'" size="small">
                          {{ row.isAbnormal ? '异常' : '正常' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-collapse-item>
              </el-collapse>
            </el-tab-pane>

            <el-tab-pane label="趋势分析" name="trend">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血糖趋势</span></template>
                    <div ref="bloodSugarChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血压趋势</span></template>
                    <div ref="bloodPressureChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
              </el-row>
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血脂趋势</span></template>
                    <div ref="lipidChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <template #header><span>肝功能趋势</span></template>
                    <div ref="liverChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
              </el-row>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-if="aiAnalysis">
          <template #header>
            <span><el-icon><MagicStick /></el-icon> AI报告解读</span>
          </template>
          <div class="ai-analysis">
            <div v-for="(section, index) in aiAnalysis" :key="index" class="analysis-section">
              <h4>{{ section.title }}</h4>
              <p>{{ section.content }}</p>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>历史报告</span>
          </template>
          <div v-for="item in historyReports" :key="item.id" class="history-item" @click="loadReport(item)">
            <div class="history-info">
              <h4>{{ item.hospital }}</h4>
              <p>{{ item.date }}</p>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download, MagicStick, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getReports, getReportDetail, analyzeReport as analyzeReportApi, exportReport as exportReportApi } from '@/api/checkup'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('overview')
const activeCategories = ref(['一般检查', '血常规'])
const analyzing = ref(false)
const aiAnalysis = ref(null)
const currentReportId = ref(null)

const reportInfo = ref({
  hospital: '',
  date: '',
  reportNo: '',
  overallLevel: 'warning',
  overallIcon: 'Warning',
  overallText: '',
  summary: ''
})

const abnormalItems = ref([])
const reportDetails = ref([])
const historyReports = ref([])

const loadReports = async () => {
  try {
    const res = await getReports(userStore.userId)
    historyReports.value = res.data || []
    if (historyReports.value.length > 0) {
      await loadReportDetail(historyReports.value[0].id)
    }
  } catch (error) {
    console.error('加载报告列表失败:', error)
  }
}

const loadReportDetail = async (reportId) => {
  try {
    const res = await getReportDetail(reportId)
    if (res.data) {
      currentReportId.value = reportId
      const data = res.data
      reportInfo.value = {
        hospital: data.hospitalName || '',
        date: data.checkupDate || '',
        reportNo: data.reportNo || '',
        overallLevel: data.riskLevel === 1 ? 'success' : data.riskLevel === 2 ? 'warning' : 'danger',
        overallIcon: data.riskLevel === 1 ? 'CircleCheckFilled' : 'Warning',
        overallText: data.riskLevel === 1 ? '各项指标正常' : '有异常指标需关注',
        summary: data.summary || ''
      }
      abnormalItems.value = data.abnormalItems || []
      reportDetails.value = data.reportDetails || []
    }
  } catch (error) {
    console.error('加载报告详情失败:', error)
  }
}

const analyzeReport = async () => {
  if (!currentReportId.value) {
    ElMessage.warning('请先选择报告')
    return
  }
  analyzing.value = true
  try {
    const res = await analyzeReportApi(currentReportId.value)
    aiAnalysis.value = res.data?.analysis || []
    ElMessage.success('AI解读完成')
  } catch (error) {
    ElMessage.error('解读失败')
  } finally {
    analyzing.value = false
  }
}

const exportReport = async () => {
  if (!currentReportId.value) {
    ElMessage.warning('请先选择报告')
    return
  }
  try {
    const res = await exportReportApi(currentReportId.value)
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `体检报告_${reportInfo.value.reportNo}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('报告导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const loadReport = (item) => {
  loadReportDetail(item.id)
}

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadReports()
})
</script>

<style scoped>
.report-view {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-header {
  text-align: center;
}
.report-header h3 {
  margin: 0 0 5px 0;
}
.report-header p {
  color: #666;
}
.overall-result {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
.result-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.result-icon.warning { background: #fdf6ec; color: #E6A23C; }
.result-icon.success { background: #f0f9eb; color: #67C23A; }
.result-icon.danger { background: #fef0f0; color: #F56C6C; }
.result-text h3 {
  margin: 0 0 8px 0;
}
.result-text p {
  margin: 0;
  color: #666;
}
.abnormal-value {
  color: #F56C6C;
  font-weight: bold;
}
.abnormal {
  color: #F56C6C;
  font-weight: bold;
}
.ai-analysis {
  max-height: 600px;
  overflow-y: auto;
}
.analysis-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.analysis-section h4 {
  margin: 0 0 8px 0;
  color: #333;
}
.analysis-section p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.history-item:hover {
  background: #f5f7fa;
}
.history-info h4 {
  margin: 0 0 5px 0;
}
.history-info p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
</style>
