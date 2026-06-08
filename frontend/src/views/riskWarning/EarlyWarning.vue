<template>
  <div class="early-warning">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预警通知</span>
              <div>
                <el-button @click="showDismissed = !showDismissed">
                  {{ showDismissed ? '隐藏已忽略' : '显示已忽略' }}
                </el-button>
                <el-button @click="markAllRead">全部已读</el-button>
                <el-button type="primary" @click="showSettings">
                  <el-icon><Setting /></el-icon>
                  预警设置
                </el-button>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="全部预警" name="all">
              <div v-if="filteredWarnings.length === 0" class="empty-state">
                <el-empty description="暂无预警数据" />
              </div>
              <div v-for="warning in filteredWarnings" :key="warning.id" class="warning-item" :class="[warning.level, { unread: !warning.read, dismissed: warning.isDismissed }]">
                <div class="warning-icon">
                  <el-icon :size="24"><component :is="getWarningIcon(warning.level)" /></el-icon>
                </div>
                <div class="warning-content">
                  <div class="warning-header">
                    <el-tag :type="getLevelType(warning.level)" size="small">{{ getLevelLabel(warning.level) }}</el-tag>
                    <el-tag v-if="warning.isDismissed" type="info" size="small" style="margin-left: 8px;">已忽略</el-tag>
                    <span class="warning-time">{{ warning.time }}</span>
                  </div>
                  <h4>{{ warning.title }}</h4>
                  <p>{{ warning.content }}</p>
                  <div class="warning-actions">
                    <el-button type="primary" link @click="handleWarning(warning)">查看详情</el-button>
                    <el-button v-if="!warning.isDismissed" type="info" link @click="dismissWarning(warning)">忽略</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="高风险预警" name="high">
              <div v-for="warning in highWarnings" :key="warning.id" class="warning-item high unread">
                <div class="warning-icon">
                  <el-icon :size="24"><CircleCloseFilled /></el-icon>
                </div>
                <div class="warning-content">
                  <div class="warning-header">
                    <el-tag type="danger" size="small">高风险</el-tag>
                    <span class="warning-time">{{ warning.time }}</span>
                  </div>
                  <h4>{{ warning.title }}</h4>
                  <p>{{ warning.content }}</p>
                  <el-alert :title="warning.action" type="error" :closable="false" show-icon style="margin-top: 10px;" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="健康提醒" name="reminder">
              <div v-for="reminder in reminders" :key="reminder.id" class="reminder-item">
                <div class="reminder-icon" :class="reminder.type">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="reminder-content">
                  <h4>{{ reminder.title }}</h4>
                  <p>{{ reminder.content }}</p>
                  <span class="reminder-time">{{ reminder.time }}</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>预警统计</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item" :class="{ active: activeFilter === 'today' }" @click="filterByStat('today')">
              <div class="stat-icon-wrap danger">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ warningStats.today }}</span>
                <span class="stat-label">今日预警</span>
              </div>
            </div>
            <div class="stat-item" :class="{ active: activeFilter === 'week' }" @click="filterByStat('week')">
              <div class="stat-icon-wrap warning">
                <el-icon><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ warningStats.week }}</span>
                <span class="stat-label">本周预警</span>
              </div>
            </div>
            <div class="stat-item" :class="{ active: activeFilter === 'unread' }" @click="filterByStat('unread')">
              <div class="stat-icon-wrap info">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ warningStats.unread }}</span>
                <span class="stat-label">未处理</span>
              </div>
            </div>
            <div class="stat-item" :class="{ active: activeFilter === 'resolved' }" @click="filterByStat('resolved')">
              <div class="stat-icon-wrap success">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ warningStats.resolved }}</span>
                <span class="stat-label">已处理</span>
              </div>
            </div>
          </div>
          <div class="filter-hint" v-if="activeFilter">
            <span>当前筛选: {{ getFilterLabel(activeFilter) }}</span>
            <el-button type="primary" link @click="clearFilter">清除筛选</el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>预警阈值设置</span>
          </template>
          <el-form label-width="100px">
            <el-form-item label="血糖上限">
              <el-input-number v-model="thresholds.bloodSugarHigh" :min="5" :max="15" :step="0.1" />
              <span style="margin-left: 5px;">mmol/L</span>
            </el-form-item>
            <el-form-item label="血压上限">
              <el-input-number v-model="thresholds.bloodPressureHigh" :min="100" :max="200" />
              <span style="margin-left: 5px;">mmHg</span>
            </el-form-item>
            <el-form-item label="心率上限">
              <el-input-number v-model="thresholds.heartRateHigh" :min="60" :max="150" />
              <span style="margin-left: 5px;">bpm</span>
            </el-form-item>
            <el-form-item label="BMI上限">
              <el-input-number v-model="thresholds.bmiHigh" :min="18" :max="40" :step="0.1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveThresholds">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警设置对话框 -->
    <el-dialog v-model="showSettingsDialog" title="预警设置" width="600px">
      <el-tabs v-model="settingsTab">
        <el-tab-pane label="阈值设置" name="threshold">
          <el-form label-width="120px" class="settings-form">
            <el-form-item label="血糖预警上限">
              <el-input-number v-model="settingsForm.bloodSugarHigh" :min="5" :max="15" :step="0.1" :precision="1" />
              <span class="unit-text">mmol/L</span>
              <span class="hint-text">空腹血糖超过此值将触发预警</span>
            </el-form-item>
            <el-form-item label="血糖预警下限">
              <el-input-number v-model="settingsForm.bloodSugarLow" :min="2" :max="5" :step="0.1" :precision="1" />
              <span class="unit-text">mmol/L</span>
              <span class="hint-text">空腹血糖低于此值将触发预警</span>
            </el-form-item>
            <el-form-item label="血压预警上限">
              <el-input-number v-model="settingsForm.bloodPressureHigh" :min="100" :max="200" :step="5" />
              <span class="unit-text">mmHg</span>
              <span class="hint-text">收缩压超过此值将触发预警</span>
            </el-form-item>
            <el-form-item label="心率预警上限">
              <el-input-number v-model="settingsForm.heartRateHigh" :min="60" :max="150" :step="5" />
              <span class="unit-text">bpm</span>
              <span class="hint-text">静息心率超过此值将触发预警</span>
            </el-form-item>
            <el-form-item label="心率预警下限">
              <el-input-number v-model="settingsForm.heartRateLow" :min="40" :max="60" :step="5" />
              <span class="unit-text">bpm</span>
              <span class="hint-text">静息心率低于此值将触发预警</span>
            </el-form-item>
            <el-form-item label="BMI预警上限">
              <el-input-number v-model="settingsForm.bmiHigh" :min="18" :max="40" :step="0.5" :precision="1" />
              <span class="hint-text">BMI超过此值将触发预警</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="通知设置" name="notification">
          <el-form label-width="120px" class="settings-form">
            <el-form-item label="开启预警通知">
              <el-switch v-model="settingsForm.enableNotification" />
            </el-form-item>
            <el-form-item label="高风险预警">
              <el-checkbox v-model="settingsForm.notifyHigh">弹窗提醒</el-checkbox>
              <el-checkbox v-model="settingsForm.notifyHighSound">声音提醒</el-checkbox>
            </el-form-item>
            <el-form-item label="中风险预警">
              <el-checkbox v-model="settingsForm.notifyMedium">弹窗提醒</el-checkbox>
              <el-checkbox v-model="settingsForm.notifyMediumSound">声音提醒</el-checkbox>
            </el-form-item>
            <el-form-item label="低风险预警">
              <el-checkbox v-model="settingsForm.notifyLow">弹窗提醒</el-checkbox>
            </el-form-item>
            <el-form-item label="免打扰时间">
              <el-time-picker
                v-model="settingsForm.quietHoursStart"
                placeholder="开始时间"
                format="HH:mm"
                style="width: 120px;"
              />
              <span style="margin: 0 10px;">至</span>
              <el-time-picker
                v-model="settingsForm.quietHoursEnd"
                placeholder="结束时间"
                format="HH:mm"
                style="width: 120px;"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 预警详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="预警详情" width="500px">
      <div v-if="currentWarning" class="warning-detail">
        <div class="detail-header">
          <el-tag :type="getLevelType(currentWarning.level)" size="large">
            {{ getLevelLabel(currentWarning.level) }}
          </el-tag>
          <span class="detail-time">{{ currentWarning.time }}</span>
        </div>

        <h3 class="detail-title">{{ currentWarning.title }}</h3>

        <div class="detail-section">
          <h4>预警内容</h4>
          <p>{{ currentWarning.content }}</p>
        </div>

        <div class="detail-section" v-if="currentWarning.action">
          <h4>建议措施</h4>
          <el-alert :title="currentWarning.action" type="warning" :closable="false" show-icon />
        </div>

        <div class="detail-section">
          <h4>相关数据</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="预警类型">{{ getTypeLabel(currentWarning.type) }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">{{ getLevelLabel(currentWarning.level) }}</el-descriptions-item>
            <el-descriptions-item label="触发时间">{{ currentWarning.time }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentWarning.read ? 'success' : 'danger'" size="small">
                {{ currentWarning.read ? '已读' : '未读' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>处理建议</h4>
          <ul class="suggestion-list">
            <li>请及时关注相关健康指标</li>
            <li>如有不适，建议尽快就医</li>
            <li>可调整生活习惯改善指标</li>
            <li>定期复查，跟踪健康状况</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="info" @click="handleDismiss">忽略预警</el-button>
        <el-button type="primary" @click="handleMarkRead">标记已读</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Setting, Bell, CircleCloseFilled, Warning, DataLine, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getWarnings, markWarningRead, markAllWarningsRead, dismissWarning as dismissWarningApi, getWarningStats, getWarningThresholds, updateWarningThresholds } from '@/api/riskWarning'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('all')
const activeFilter = ref(null)
const showDismissed = ref(false)
const showDetailDialog = ref(false)
const currentWarning = ref(null)
const showSettingsDialog = ref(false)
const settingsTab = ref('threshold')

const settingsForm = reactive({
  bloodSugarHigh: 7.0,
  bloodSugarLow: 3.9,
  bloodPressureHigh: 140,
  heartRateHigh: 100,
  heartRateLow: 50,
  bmiHigh: 24,
  enableNotification: true,
  notifyHigh: true,
  notifyHighSound: true,
  notifyMedium: true,
  notifyMediumSound: false,
  notifyLow: true,
  quietHoursStart: null,
  quietHoursEnd: null
})

const warnings = ref([])
const reminders = ref([
  { id: 1, type: 'medication', title: '用药提醒', content: '二甲双胍 500mg - 晚餐后服用', time: '18:00' },
  { id: 2, type: 'exercise', title: '运动提醒', content: '今日还未运动，建议进行30分钟散步', time: '19:00' },
  { id: 3, type: 'checkup', title: '复查提醒', content: '心血管科复查还有15天', time: '全天' },
  { id: 4, type: 'measurement', title: '测量提醒', content: '请测量今日睡前血压', time: '21:00' }
])

const thresholds = ref({
  bloodSugarHigh: 7.0,
  bloodPressureHigh: 140,
  heartRateHigh: 100,
  bmiHigh: 24
})

const warningStats = ref({
  today: 0,
  week: 0,
  unread: 0,
  resolved: 0
})

const highWarnings = computed(() => warnings.value.filter(w => (w.warningLevel || w.level) === 'high'))

// 根据筛选条件过滤预警列表
const filteredWarnings = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekAgo = new Date(today)
  weekAgo.setDate(weekAgo.getDate() - 7)

  return warnings.value.filter(warning => {
    // 如果不显示已忽略的预警，则过滤掉
    if (!showDismissed.value && warning.isDismissed) {
      return false
    }

    // 如果没有筛选条件，显示所有预警
    if (!activeFilter.value) {
      return true
    }

    const warningTime = warning.time ? new Date(warning.time.replace(/-/g, '/')) : null

    switch (activeFilter.value) {
      case 'today':
        // 筛选今日预警
        return warningTime && warningTime >= today
      case 'week':
        // 筛选本周预警
        return warningTime && warningTime >= weekAgo
      case 'unread':
        // 筛选未处理预警（未忽略）
        return !warning.isDismissed
      case 'resolved':
        // 筛选已处理预警（已忽略）
        return warning.isDismissed
      default:
        return true
    }
  })
})

const getLevelType = (level) => {
  const map = { 'high': 'danger', 'medium': 'warning', 'low': 'info' }
  return map[level] || 'info'
}

const getWarningIcon = (level) => {
  const map = { 'high': 'CircleCloseFilled', 'medium': 'WarningFilled', 'low': 'InfoFilled' }
  return map[level] || 'InfoFilled'
}

const getLevelLabel = (level) => {
  const map = { 'high': '高风险', 'medium': '中风险', 'low': '低风险' }
  return map[level] || level
}

const loadWarnings = async () => {
  try {
    const params = {
      includeDismissed: showDismissed.value ? true : undefined
    }
    const res = await getWarnings(userStore.userId, params)
    warnings.value = (res.data || []).map(w => ({
      ...w,
      isDismissed: w.dismissed ? 1 : 0
    }))
  } catch (error) {
    console.error('加载预警失败:', error)
  }
}

const loadWarningStats = async () => {
  try {
    const res = await getWarningStats(userStore.userId)
    warningStats.value = res.data || warningStats.value
  } catch (error) {
    console.error('加载预警统计失败:', error)
  }
}

const loadThresholds = async () => {
  try {
    const res = await getWarningThresholds(userStore.userId)
    if (res.data) {
      thresholds.value = res.data
    }
  } catch (error) {
    console.error('加载阈值失败:', error)
  }
}

const markAllRead = async () => {
  try {
    await markAllWarningsRead(userStore.userId)
    warnings.value.forEach(w => w.isRead = 1)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleWarning = async (warning) => {
  currentWarning.value = warning
  showDetailDialog.value = true
  // 标记为已读
  if (!warning.read) {
    try {
      await markWarningRead(warning.id)
      warning.read = true
      warning.isRead = 1
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const handleMarkRead = async () => {
  if (currentWarning.value) {
    try {
      await markWarningRead(currentWarning.value.id)
      currentWarning.value.read = true
      currentWarning.value.isRead = 1
      ElMessage.success('已标记为已读')
      showDetailDialog.value = false
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }
}

const handleDismiss = async () => {
  if (currentWarning.value) {
    try {
      await dismissWarningApi(currentWarning.value.id)
      currentWarning.value.isDismissed = 1
      ElMessage.success('已忽略该预警')
      showDetailDialog.value = false
      // 从列表中移除
      warnings.value = warnings.value.filter(w => w.id !== currentWarning.value.id)
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }
}

const getTypeLabel = (type) => {
  const map = {
    'blood_sugar': '血糖预警',
    'blood_pressure': '血压预警',
    'heart_rate': '心率预警',
    'bmi': 'BMI预警',
    'exercise': '运动提醒',
    'diet': '饮食提醒',
    'sleep': '睡眠提醒',
    'medication': '用药提醒'
  }
  return map[type] || type || '健康预警'
}

const dismissWarning = async (warning) => {
  try {
    await dismissWarningApi(warning.id)
    warning.isDismissed = 1
    // 重新加载统计数据和预警列表
    await Promise.all([loadWarningStats(), loadWarnings()])
    ElMessage.success('已忽略')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const filterByStat = (type) => {
  if (activeFilter.value === type) {
    activeFilter.value = null
    activeTab.value = 'all'
    return
  }
  activeFilter.value = type
  switch (type) {
    case 'today':
      // 筛选今日预警
      activeTab.value = 'all'
      break
    case 'week':
      // 筛选本周预警
      activeTab.value = 'all'
      break
    case 'unread':
      // 筛选未处理预警
      activeTab.value = 'all'
      break
    case 'resolved':
      // 筛选已处理预警
      activeTab.value = 'all'
      break
  }
}

const clearFilter = () => {
  activeFilter.value = null
  activeTab.value = 'all'
}

const getFilterLabel = (filter) => {
  const map = {
    'today': '今日预警',
    'week': '本周预警',
    'unread': '未处理预警',
    'resolved': '已处理预警'
  }
  return map[filter] || ''
}

const showSettings = () => {
  // 将当前阈值设置填充到表单
  settingsForm.bloodSugarHigh = thresholds.value.bloodSugarHigh || 7.0
  settingsForm.bloodPressureHigh = thresholds.value.bloodPressureHigh || 140
  settingsForm.heartRateHigh = thresholds.value.heartRateHigh || 100
  settingsForm.bmiHigh = thresholds.value.bmiHigh || 24
  showSettingsDialog.value = true
}

const saveSettings = async () => {
  try {
    // 保存阈值设置
    await updateWarningThresholds(userStore.userId, {
      bloodSugarHigh: settingsForm.bloodSugarHigh,
      bloodSugarLow: settingsForm.bloodSugarLow,
      bloodPressureHigh: settingsForm.bloodPressureHigh,
      heartRateHigh: settingsForm.heartRateHigh,
      heartRateLow: settingsForm.heartRateLow,
      bmiHigh: settingsForm.bmiHigh
    })
    // 更新本地阈值
    thresholds.value = {
      bloodSugarHigh: settingsForm.bloodSugarHigh,
      bloodSugarLow: settingsForm.bloodSugarLow,
      bloodPressureHigh: settingsForm.bloodPressureHigh,
      heartRateHigh: settingsForm.heartRateHigh,
      heartRateLow: settingsForm.heartRateLow,
      bmiHigh: settingsForm.bmiHigh
    }
    showSettingsDialog.value = false
    ElMessage.success('预警设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const saveThresholds = async () => {
  try {
    await updateWarningThresholds(userStore.userId, thresholds.value)
    ElMessage.success('预警阈值保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 监听 showDismissed 变化，重新加载预警列表
watch(showDismissed, () => {
  loadWarnings()
})

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadWarnings()
  loadWarningStats()
  loadThresholds()
})
</script>

<style scoped>
.early-warning {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warning-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: #f5f7fa;
}
.warning-item.unread {
  background: #ecf5ff;
  border-left: 3px solid #409EFF;
}
.warning-item.high {
  background: #fef0f0;
  border-left: 3px solid #F56C6C;
}
.warning-item.dismissed {
  opacity: 0.6;
  background: #f5f5f5;
}
.warning-icon {
  flex-shrink: 0;
}
.warning-item.high .warning-icon { color: #F56C6C; }
.warning-item.medium .warning-icon { color: #E6A23C; }
.warning-item.low .warning-icon { color: #909399; }
.warning-content {
  flex: 1;
}
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.warning-time {
  color: #999;
  font-size: 12px;
}
.warning-content h4 {
  margin: 0 0 5px 0;
}
.warning-content p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
.warning-actions {
  margin-top: 10px;
}
.reminder-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.reminder-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.reminder-icon.medication { background: #e6f7ff; color: #409EFF; }
.reminder-icon.exercise { background: #f0f9eb; color: #67C23A; }
.reminder-icon.checkup { background: #fdf6ec; color: #E6A23C; }
.reminder-icon.measurement { background: #f4f4f5; color: #909399; }
.reminder-content h4 {
  margin: 0 0 5px 0;
}
.reminder-content p {
  margin: 0 0 5px 0;
  color: #666;
}
.reminder-time {
  color: #999;
  font-size: 12px;
}

/* 预警详情对话框样式 */
.warning-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-time {
  color: #999;
  font-size: 14px;
}

.detail-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #333;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  font-weight: 600;
}

.detail-section p {
  margin: 0;
  color: #333;
  line-height: 1.6;
}

.suggestion-list {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.suggestion-list li {
  margin-bottom: 8px;
  line-height: 1.5;
}

.suggestion-list li:last-child {
  margin-bottom: 0;
}

/* 预警设置对话框样式 */
.settings-form {
  padding: 10px 0;
}

.settings-form .el-form-item {
  margin-bottom: 20px;
}

.unit-text {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}

.hint-text {
  display: block;
  margin-top: 4px;
  color: #C0C4CC;
  font-size: 12px;
}

/* 预警统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.stat-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon-wrap.danger {
  background: #fef0f0;
  color: #F56C6C;
}

.stat-icon-wrap.warning {
  background: #fdf6ec;
  color: #E6A23C;
}

.stat-icon-wrap.info {
  background: #ecf5ff;
  color: #409EFF;
}

.stat-icon-wrap.success {
  background: #f0f9eb;
  color: #67C23A;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.filter-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 10px 12px;
  background: #ecf5ff;
  border-radius: 8px;
  font-size: 13px;
  color: #409EFF;
}

.empty-state {
  padding: 40px 0;
}
</style>
