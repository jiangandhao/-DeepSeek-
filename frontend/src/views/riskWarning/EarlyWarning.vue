<template>
  <div class="early-warning">
    <el-card class="header-card">
      <div class="page-header">
        <h2>健康预警中心</h2>
        <p>实时监控健康指标变化，及时发送预警通知</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 预警统计 -->
      <el-col :span="8">
        <el-card class="warning-stats">
          <div class="stat-item critical">
            <div class="stat-number">{{ warnings.critical }}</div>
            <div class="stat-label">紧急预警</div>
          </div>
          <div class="stat-item warning">
            <div class="stat-number">{{ warnings.warning }}</div>
            <div class="stat-label">一般预警</div>
          </div>
          <div class="stat-item info">
            <div class="stat-number">{{ warnings.info }}</div>
            <div class="stat-label">提示信息</div>
          </div>
        </el-card>

        <!-- 预警设置 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>预警设置</span>
          </template>
          <el-form :model="alertSettings" label-position="top">
            <el-form-item label="预警方式">
              <el-checkbox-group v-model="alertSettings.methods">
                <el-checkbox value="app">App推送</el-checkbox>
                <el-checkbox value="sms">短信通知</el-checkbox>
                <el-checkbox value="email">邮件通知</el-checkbox>
                <el-checkbox value="phone">电话通知</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="预警时段">
              <el-time-picker
                v-model="alertSettings.timeRange"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="紧急预警电话">
              <el-input v-model="alertSettings.emergencyPhone" placeholder="请输入紧急联系人电话" />
            </el-form-item>
            <el-button type="primary" style="width: 100%" @click="saveSettings">
              保存设置
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 预警列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预警通知</span>
              <el-radio-group v-model="filterType" size="small">
                <el-radio-button value="all">全部</el-radio-button>
                <el-radio-button value="critical">紧急</el-radio-button>
                <el-radio-button value="warning">一般</el-radio-button>
                <el-radio-button value="info">提示</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div v-for="(alert, index) in filteredWarnings" :key="index"
               class="warning-item"
               :class="[alert.level, { 'unread': !alert.read }]">
            <div class="warning-icon">
              <el-icon v-if="alert.level === 'critical'" :size="24" color="#F56C6C"><CircleCloseFilled /></el-icon>
              <el-icon v-else-if="alert.level === 'warning'" :size="24" color="#E6A23C"><WarningFilled /></el-icon>
              <el-icon v-else :size="24" color="#409EFF"><InfoFilled /></el-icon>
            </div>
            <div class="warning-content">
              <div class="warning-header">
                <span class="warning-title">{{ alert.title }}</span>
                <span class="warning-time">{{ alert.time }}</span>
              </div>
              <div class="warning-desc">{{ alert.description }}</div>
              <div class="warning-action">
                <el-button v-if="alert.level === 'critical'" type="danger" size="small" @click="handleEmergency(alert)">
                  紧急处理
                </el-button>
                <el-button type="primary" size="small" @click="viewDetail(alert)">
                  查看详情
                </el-button>
                <el-button size="small" @click="markAsRead(alert)">
                  {{ alert.read ? '已读' : '标为已读' }}
                </el-button>
              </div>
            </div>
            <div class="warning-status">
              <el-tag :type="getLevelTagType(alert.level)" size="small">
                {{ getLevelLabel(alert.level) }}
              </el-tag>
            </div>
          </div>

          <el-empty v-if="!filteredWarnings.length" description="暂无预警信息" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="预警详情" width="600px">
      <div v-if="selectedWarning">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预警类型">{{ selectedWarning.title }}</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="getLevelTagType(selectedWarning.level)">
              {{ getLevelLabel(selectedWarning.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预警时间">{{ selectedWarning.time }}</el-descriptions-item>
          <el-descriptions-item label="预警内容">{{ selectedWarning.description }}</el-descriptions-item>
          <el-descriptions-item label="相关指标">{{ selectedWarning.indicator }}</el-descriptions-item>
          <el-descriptions-item label="当前数值">{{ selectedWarning.currentValue }}</el-descriptions-item>
          <el-descriptions-item label="正常范围">{{ selectedWarning.normalRange }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h4>AI处理建议</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(suggestion, idx) in selectedWarning.suggestions"
            :key="idx"
            :type="idx === 0 ? 'danger' : 'primary'"
          >
            {{ suggestion }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCloseFilled, WarningFilled, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'

const filterType = ref('all')
const showDetailDialog = ref(false)
const selectedWarning = ref(null)

const warnings = reactive({
  critical: 1,
  warning: 3,
  info: 5
})

const alertSettings = reactive({
  methods: ['app', 'sms'],
  timeRange: [new Date(2024, 0, 1, 8, 0), new Date(2024, 0, 1, 22, 0)],
  emergencyPhone: '13800138000'
})

const warningList = ref([
  {
    id: 1, level: 'critical', title: '血糖异常升高',
    description: '您最近一次空腹血糖检测值为 8.5mmol/L，远超正常范围（3.9-6.1mmol/L），建议立即就医。',
    time: '2024-01-15 09:30', read: false,
    indicator: '空腹血糖', currentValue: '8.5mmol/L', normalRange: '3.9-6.1mmol/L',
    suggestions: ['立即前往医院内分泌科就诊', '暂停高糖食物摄入', '记录当前症状和用药情况']
  },
  {
    id: 2, level: 'warning', title: '血压波动较大',
    description: '您近一周血压波动较大，收缩压在125-145mmHg之间，建议关注。',
    time: '2024-01-14 14:20', read: false,
    indicator: '收缩压', currentValue: '145mmHg', normalRange: '90-139mmHg',
    suggestions: ['每日定时测量血压', '减少盐分摄入', '避免情绪波动']
  },
  {
    id: 3, level: 'warning', title: '体重持续上升',
    description: '过去一个月体重增加2.5kg，BMI已达到25.2，建议调整饮食和运动。',
    time: '2024-01-13 10:15', read: true,
    indicator: 'BMI', currentValue: '25.2', normalRange: '18.5-23.9',
    suggestions: ['控制每日热量摄入', '增加有氧运动频率', '定期称重记录']
  },
  {
    id: 4, level: 'warning', title: '睡眠质量下降',
    description: '智能手环数据显示近一周平均睡眠时长不足6小时，深睡比例偏低。',
    time: '2024-01-12 08:00', read: true,
    indicator: '平均睡眠时长', currentValue: '5.8小时', normalRange: '7-9小时',
    suggestions: ['建立规律作息时间', '睡前避免使用电子设备', '创造良好睡眠环境']
  },
  {
    id: 5, level: 'info', title: '复查提醒',
    description: '距离您上次甲状腺检查已过6个月，建议进行复查。',
    time: '2024-01-11 09:00', read: true,
    indicator: '甲状腺B超', currentValue: '-', normalRange: '-',
    suggestions: ['预约甲状腺B超检查', '空腹前往', '携带上次检查报告']
  },
  {
    id: 6, level: 'info', title: '疫苗接种提醒',
    description: '您的流感疫苗即将到期，建议在流行季前完成接种。',
    time: '2024-01-10 09:00', read: true,
    indicator: '流感疫苗', currentValue: '-', normalRange: '-',
    suggestions: ['预约社区卫生服务中心', '确认无接种禁忌', '接种后观察30分钟']
  },
  {
    id: 7, level: 'info', title: '健康报告已生成',
    description: '您的月度健康分析报告已生成，点击查看。',
    time: '2024-01-09 08:00', read: true,
    indicator: '-', currentValue: '-', normalRange: '-',
    suggestions: ['查看报告详情', '关注异常指标']
  },
  {
    id: 8, level: 'info', title: '运动目标达成',
    description: '恭喜！您本周运动目标已达成100%，继续保持！',
    time: '2024-01-08 20:00', read: true,
    indicator: '周运动时长', currentValue: '150分钟', normalRange: '≥150分钟',
    suggestions: ['继续保持运动习惯', '适当增加运动强度']
  },
  {
    id: 9, level: 'info', title: '饮水提醒',
    description: '今日饮水量不足，建议增加饮水。',
    time: '2024-01-07 16:00', read: true,
    indicator: '日饮水量', currentValue: '800ml', normalRange: '1500-2000ml',
    suggestions: ['设置饮水提醒', '随身携带水杯']
  }
])

const filteredWarnings = computed(() => {
  if (filterType.value === 'all') return warningList.value
  return warningList.value.filter(w => w.level === filterType.value)
})

const getLevelTagType = (level) => {
  const map = { critical: 'danger', warning: 'warning', info: 'info' }
  return map[level] || 'info'
}

const getLevelLabel = (level) => {
  const map = { critical: '紧急', warning: '一般', info: '提示' }
  return map[level] || '未知'
}

const handleEmergency = (alert) => {
  ElMessageBox.confirm(
    '确认拨打紧急联系人电话？',
    '紧急处理',
    { confirmButtonText: '确认拨打', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    ElMessage.success('正在拨打紧急联系人电话...')
  })
}

const viewDetail = (alert) => {
  selectedWarning.value = alert
  showDetailDialog.value = true
}

const markAsRead = (alert) => {
  alert.read = true
}

const saveSettings = () => {
  ElMessage.success('预警设置已保存')
}
</script>

<style scoped>
.early-warning { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.warning-stats {
  display: flex; justify-content: space-around; padding: 20px 0;
}
.stat-item { text-align: center; }
.stat-number { font-size: 36px; font-weight: bold; }
.stat-label { font-size: 14px; margin-top: 8px; }
.stat-item.critical .stat-number { color: #F56C6C; }
.stat-item.warning .stat-number { color: #E6A23C; }
.stat-item.info .stat-number { color: #409EFF; }
.stat-item.critical .stat-label { color: #F56C6C; }
.stat-item.warning .stat-label { color: #E6A23C; }
.stat-item.info .stat-label { color: #409EFF; }

.warning-item {
  display: flex; padding: 16px; border-radius: 8px;
  margin-bottom: 12px; transition: all 0.3s;
  border-left: 4px solid transparent;
}
.warning-item.critical { background: #fef0f0; border-left-color: #F56C6C; }
.warning-item.warning { background: #fdf6ec; border-left-color: #E6A23C; }
.warning-item.info { background: #f4f4f5; border-left-color: #409EFF; }
.warning-item.unread { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.warning-icon { margin-right: 16px; padding-top: 4px; }
.warning-content { flex: 1; }
.warning-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.warning-title { font-weight: 500; color: #303133; font-size: 16px; }
.warning-time { color: #909399; font-size: 13px; }
.warning-desc { color: #606266; line-height: 1.6; margin-bottom: 12px; }
.warning-action { display: flex; gap: 8px; }
.warning-status { margin-left: 16px; }
</style>
