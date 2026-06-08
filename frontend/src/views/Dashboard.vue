<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <section class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <p class="welcome-greeting">{{ greeting }}，{{ username }}</p>
          <h1 class="welcome-title">健康概览</h1>
          <p class="welcome-desc">您的整体健康状况良好，今日有 <strong>{{ warningCount }}</strong> 项待办事项</p>
        </div>
        <div class="welcome-art">
          <div class="art-circle art-circle-1"></div>
          <div class="art-circle art-circle-2"></div>
          <div class="art-circle art-circle-3"></div>
          <div class="art-pulse"></div>
        </div>
      </div>
    </section>

    <!-- 统计卡片 -->
    <section class="stats-section">
      <router-link
        v-for="(stat, index) in stats"
        :key="index"
        :to="stat.path"
        class="stat-card"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <div class="stat-icon-wrap" :style="{ background: stat.iconBg }">
          <div class="stat-icon" v-html="stat.icon"></div>
        </div>
        <div class="stat-info">
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-trend" :class="stat.trendType">
            <svg v-if="stat.trendType === 'up'" viewBox="0 0 12 12" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 8l4-4 4 4" />
            </svg>
            <svg v-else-if="stat.trendType === 'down'" viewBox="0 0 12 12" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M2 4l4 4 4-4" />
            </svg>
            <svg v-else viewBox="0 0 12 12" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="6" cy="6" r="4" />
            </svg>
            {{ stat.trend }}
          </span>
        </div>
        <div class="stat-sparkline" v-html="stat.sparkline"></div>
        <div class="stat-arrow">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 18l6-6-6-6" />
          </svg>
        </div>
      </router-link>
    </section>

    <!-- 主要内容区域 -->
    <section class="main-grid">
      <!-- 左侧：健康趋势 -->
      <div class="grid-card chart-card">
        <div class="card-header">
          <div class="card-title-group">
            <h3 class="card-title">健康趋势</h3>
            <span class="card-subtitle">近7天数据</span>
          </div>
          <div class="card-tabs">
            <button
              v-for="tab in chartTabs"
              :key="tab.key"
              class="tab-btn"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
        <div class="chart-container">
          <div ref="chartRef" class="chart-canvas"></div>
        </div>
      </div>

      <!-- 右侧：快速操作 -->
      <div class="grid-card actions-card">
        <div class="card-header">
          <h3 class="card-title">快速操作</h3>
        </div>
        <div class="actions-grid">
          <router-link v-for="action in quickActions" :key="action.path" :to="action.path" class="action-item">
            <div class="action-icon" :style="{ background: action.bg }">
              <div v-html="action.icon"></div>
            </div>
            <span class="action-label">{{ action.label }}</span>
            <span class="action-desc">{{ action.desc }}</span>
          </router-link>
        </div>
      </div>

      <!-- 左下：最近活动 -->
      <div class="grid-card activity-card">
        <div class="card-header">
          <h3 class="card-title">最近活动</h3>
          <a href="#" class="view-all">查看全部</a>
        </div>
        <div class="activity-list">
          <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-dot" :style="{ background: activity.color }"></div>
            <div class="activity-line" v-if="index < recentActivities.length - 1"></div>
            <div class="activity-content">
              <div class="activity-header">
                <span class="activity-title">{{ activity.title }}</span>
                <span class="activity-time">{{ activity.time }}</span>
              </div>
              <p class="activity-desc">{{ activity.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右下：健康评分 -->
      <div class="grid-card score-card">
        <div class="card-header">
          <h3 class="card-title">健康评分</h3>
          <span class="card-badge">本月</span>
        </div>
        <div class="score-content">
          <div class="score-ring-container">
            <svg viewBox="0 0 120 120" class="score-ring">
              <circle cx="60" cy="60" r="52" fill="none" stroke="rgba(26,107,90,0.1)" stroke-width="8" />
              <circle
                cx="60" cy="60" r="52"
                fill="none"
                stroke="url(#scoreGradient)"
                stroke-width="8"
                stroke-linecap="round"
                :stroke-dasharray="326.73"
                :stroke-dashoffset="326.73 * (1 - score / 100)"
                transform="rotate(-90 60 60)"
                class="score-progress"
              />
              <defs>
                <linearGradient id="scoreGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#1a6b5a" />
                  <stop offset="100%" stop-color="#4a9d7e" />
                </linearGradient>
              </defs>
            </svg>
            <div class="score-value">
              <span class="score-number">{{ score }}</span>
              <span class="score-unit">分</span>
            </div>
          </div>
          <div class="score-details">
            <div class="score-item" v-for="item in scoreDetails" :key="item.label">
              <div class="score-item-header">
                <span class="score-item-label">{{ item.label }}</span>
                <span class="score-item-value">{{ item.value }}</span>
              </div>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI 建议 -->
    <section class="ai-section">
      <div class="ai-card">
        <div class="ai-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z" />
            <path d="M12 6v6l4 2" />
          </svg>
        </div>
        <div class="ai-content">
          <h4 class="ai-title">AI 健康建议</h4>
          <p class="ai-text">根据您近7天的血糖数据，建议您适当减少晚餐碳水摄入量，并在餐后30分钟进行15分钟散步，有助于改善餐后血糖峰值。</p>
        </div>
        <button class="ai-action">查看详情</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getWarningStats } from '@/api/riskWarning'
import { getBloodSugarTrend } from '@/api/bloodSugar'
import { getBloodPressureTrend } from '@/api/bloodPressure'
import { getWeightTrend } from '@/api/healthManager'

const router = useRouter()
const userStore = useUserStore()
const username = computed(() => userStore.username || '用户')
const activeTab = ref('blood-sugar')
const chartRef = ref(null)
const chartInstance = ref(null)
const score = ref(86)
const warningCount = ref(0)

// 加载预警统计
const loadWarningStats = async () => {
  if (!userStore.userId) return
  try {
    const res = await getWarningStats(userStore.userId)
    if (res && res.data) {
      // 使用 unread 字段表示未读预警数量
      warningCount.value = res.data.unread || 0
    }
  } catch (error) {
    console.error('加载预警统计失败:', error)
  }
}

// 图表数据缓存
const chartData = ref({
  dates: [],
  series: []
})

// 生成最近 N 天的日期列表 YYYY-MM-DD
const getLastNDays = (n) => {
  const days = []
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(d.toISOString().split('T')[0])
  }
  return days
}

// 解析 measureTime 兼容 ISO T 格式
const parseDate = (timeStr) => {
  if (!timeStr) return null
  return timeStr.replace('T', ' ').split(' ')[0]
}

// 加载血糖趋势数据
const loadBloodSugarTrend = async () => {
  if (!userStore.userId) return
  try {
    const res = await getBloodSugarTrend(userStore.userId, '7d')
    const records = (res && res.data && res.data.records) ? res.data.records : []
    if (records.length > 0) {
      // 用实际数据的日期，最多取最近7天
      const allDates = [...new Set(records.map(r => parseDate(r.measureTime)).filter(Boolean))].sort().slice(-7)
      const fasting = allDates.map(date => {
        const dayRecs = records.filter(r => parseDate(r.measureTime) === date && r.type === 'fasting' && r.value != null)
        return dayRecs.length > 0 ? Number((dayRecs.reduce((s, r) => s + Number(r.value), 0) / dayRecs.length).toFixed(1)) : null
      })
      const postprandial = allDates.map(date => {
        const dayRecs = records.filter(r => parseDate(r.measureTime) === date && r.type === 'after' && r.value != null)
        return dayRecs.length > 0 ? Number((dayRecs.reduce((s, r) => s + Number(r.value), 0) / dayRecs.length).toFixed(1)) : null
      })
      return {
        dates: allDates.map(d => d.split('-')[1] + '/' + d.split('-')[2]),
        series: [
          { name: '空腹血糖', data: fasting, color: '#1a6b5a' },
          { name: '餐后血糖', data: postprandial, color: '#c4956a' }
        ],
        yAxisName: 'mmol/L'
      }
    }
  } catch (error) {
    console.error('加载血糖趋势失败:', error)
  }
  // 返回默认数据
  const last7 = getLastNDays(7)
  return {
    dates: last7.map(d => d.split('-')[1] + '/' + d.split('-')[2]),
    series: [
      { name: '空腹血糖', data: [5.8, 6.1, 5.9, 6.2, 5.7, 6.0, 5.9], color: '#1a6b5a' },
      { name: '餐后血糖', data: [7.2, 7.8, 7.5, 8.1, 7.3, 7.6, 7.4], color: '#c4956a' }
    ],
    yAxisName: 'mmol/L'
  }
}

// 加载血压趋势数据
const loadBloodPressureTrend = async () => {
  if (!userStore.userId) return
  try {
    const res = await getBloodPressureTrend(userStore.userId, '7d')
    if (res && res.data) {
      const dates = (res.data.dates || getLastNDays(7)).map(d => d.replace('-', '/'))
      return {
        dates,
        series: [
          { name: '收缩压', data: res.data.systolic || [], color: '#F56C6C' },
          { name: '舒张压', data: res.data.diastolic || [], color: '#409EFF' }
        ],
        yAxisName: 'mmHg'
      }
    }
  } catch (error) {
    console.error('加载血压趋势失败:', error)
  }
  // 返回默认数据
  const last7 = getLastNDays(7)
  return {
    dates: last7.map(d => d.split('-')[1] + '/' + d.split('-')[2]),
    series: [
      { name: '收缩压', data: [118, 122, 115, 120, 118, 125, 116], color: '#F56C6C' },
      { name: '舒张压', data: [75, 78, 72, 76, 74, 80, 73], color: '#409EFF' }
    ],
    yAxisName: 'mmHg'
  }
}

// 加载体重趋势数据
const loadWeightTrend = async () => {
  if (!userStore.userId) return
  try {
    const res = await getWeightTrend(userStore.userId, '7d')
    if (res && res.data) {
      const dates = (res.data.dates || getLastNDays(7)).map(d => d.replace('-', '/'))
      return {
        dates,
        series: [
          { name: '体重', data: res.data.weights || [], color: '#E6A23C' }
        ],
        yAxisName: 'kg'
      }
    }
  } catch (error) {
    console.error('加载体重趋势失败:', error)
  }
  // 返回默认数据
  return {
    dates: getLastNDays(7).map(d => d.split('-')[1] + '/' + d.split('-')[2]),
    series: [
      { name: '体重', data: [70.2, 70.5, 70.1, 70.3, 70.0, 70.4, 70.2], color: '#E6A23C' }
    ],
    yAxisName: 'kg'
  }
}

// 更新图表数据 — 销毁重建，彻底避免 coordSys 丢失
const updateChart = (config) => {
  if (!chartInstance.value || !config || !config.series || !config.dates) return

  const dates = config.dates.filter(d => d != null)
  if (dates.length === 0) return

  // 确保每个 series 的 data 长度与 dates 一致，缺失补 null
  const padData = (arr, len) => {
    if (!Array.isArray(arr)) return new Array(len).fill(null)
    const result = arr.slice(0, len)
    while (result.length < len) result.push(null)
    return result.map(v => (v != null ? v : null))
  }

  const validSeries = config.series.filter(s => s && s.name)
  if (validSeries.length === 0) return

  // 销毁旧实例后重建，彻底避免 notMerge 导致的 coordSys 丢失问题
  try {
    const el = chartInstance.value.getDom()
    chartInstance.value.dispose()
    chartInstance.value = echarts.init(el)
    window.removeEventListener('resize', handleResize)
    window.addEventListener('resize', handleResize)

    const option = {
      tooltip: { trigger: 'axis' },
      legend: { data: validSeries.map(s => s.name), right: 0, top: 0, selectedMode: false },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: { type: 'value', name: config.yAxisName || '' },
      series: validSeries.map(s => ({
        name: s.name,
        type: 'line',
        data: padData(s.data, dates.length),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: s.color, width: 3 },
        itemStyle: { color: s.color }
      }))
    }

    chartInstance.value.setOption(option)
  } catch (e) {
    console.error('ECharts updateChart error:', e)
  }
}

// 根据标签加载数据
const loadTrendDataByTab = async (tab) => {
  let config = null

  switch (tab) {
    case 'blood-sugar':
      config = await loadBloodSugarTrend()
      break
    case 'blood-pressure':
      config = await loadBloodPressureTrend()
      break
    case 'weight':
      config = await loadWeightTrend()
      break
    default:
      config = await loadBloodSugarTrend()
  }

  if (config) {
    chartData.value = config
    updateChart(config)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  await loadTrendDataByTab(activeTab.value)
}

// 监听tab切换
watch(activeTab, async (newTab) => {
  await loadTrendDataByTab(newTab)
})

// 监听路由变化，刷新预警数量
watch(() => router.currentRoute.value.path, () => {
  if (userStore.userId) {
    loadWarningStats()
  }
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const stats = computed(() => [
  {
    label: '今日血糖',
    value: '5.6',
    trend: '正常',
    trendType: 'normal',
    path: '/blood-sugar',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>',
    iconBg: 'rgba(26, 107, 90, 0.1)',
    sparkline: '<svg viewBox="0 0 80 24" width="80" height="24"><polyline points="0,18 10,14 20,16 30,10 40,12 50,8 60,10 70,6 80,4" fill="none" stroke="#1a6b5a" stroke-width="2"/></svg>'
  },
  {
    label: '本周运动',
    value: '4.5h',
    trend: '+12%',
    trendType: 'up',
    path: '/blood-sugar/exercise',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>',
    iconBg: 'rgba(196, 149, 106, 0.1)',
    sparkline: '<svg viewBox="0 0 80 24" width="80" height="24"><polyline points="0,20 10,16 20,18 30,12 40,14 50,10 60,8 70,6 80,4" fill="none" stroke="#c4956a" stroke-width="2"/></svg>'
  },
  {
    label: '睡眠质量',
    value: '85分',
    trend: '+5%',
    trendType: 'up',
    path: '/health-manager',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>',
    iconBg: 'rgba(90, 143, 191, 0.1)',
    sparkline: '<svg viewBox="0 0 80 24" width="80" height="24"><polyline points="0,14 10,16 20,12 30,14 40,10 50,12 60,8 70,10 80,6" fill="none" stroke="#5a8fbf" stroke-width="2"/></svg>'
  },
  {
    label: '风险预警',
    value: String(warningCount.value),
    trend: warningCount.value > 0 ? '待处理' : '已处理',
    trendType: warningCount.value > 0 ? 'warning' : 'normal',
    path: '/risk-warning/early',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    iconBg: 'rgba(199, 84, 80, 0.1)',
    sparkline: '<svg viewBox="0 0 80 24" width="80" height="24"><polyline points="0,12 10,14 20,10 30,16 40,12 50,14 60,10 70,12 80,8" fill="none" stroke="#c75450" stroke-width="2"/></svg>'
  }
])

const chartTabs = [
  { key: 'blood-sugar', label: '血糖' },
  { key: 'blood-pressure', label: '血压' },
  { key: 'weight', label: '体重' }
]

const quickActions = [
  {
    path: '/blood-sugar',
    label: '记录血糖',
    desc: '记录今日血糖数据',
    bg: 'rgba(26, 107, 90, 0.08)',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#1a6b5a" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>'
  },
  {
    path: '/checkup',
    label: '预约体检',
    desc: '智能体检预约',
    bg: 'rgba(196, 149, 106, 0.08)',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#c4956a" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>'
  },
  {
    path: '/health-manager',
    label: 'AI 咨询',
    desc: '获取健康建议',
    bg: 'rgba(90, 143, 191, 0.08)',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#5a8fbf" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'
  },
  {
    path: '/risk-warning',
    label: '风险评估',
    desc: '查看风险报告',
    bg: 'rgba(199, 84, 80, 0.08)',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#c75450" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>'
  }
]

const recentActivities = [
  { title: '完成血糖检测', desc: '空腹血糖 5.6 mmol/L，处于正常范围', time: '10:30', color: '#1a6b5a' },
  { title: '体检报告已生成', desc: '年度综合体检报告已可查看', time: '昨天', color: '#c4956a' },
  { title: '风险评估完成', desc: '心血管风险等级：低风险', time: '2天前', color: '#5a8fbf' },
  { title: '饮食计划更新', desc: 'AI 已根据您的血糖数据调整饮食建议', time: '3天前', color: '#4a9d7e' }
]

const scoreDetails = [
  { label: '血糖控制', value: '92分', percent: 92, color: '#1a6b5a' },
  { label: '运动达标', value: '78分', percent: 78, color: '#c4956a' },
  { label: '饮食规律', value: '85分', percent: 85, color: '#5a8fbf' },
  { label: '睡眠质量', value: '88分', percent: 88, color: '#4a9d7e' }
]

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadWarningStats()

  // 延迟初始化图表，确保 DOM 已渲染，再加载数据
  nextTick(() => {
    setTimeout(() => {
      if (chartRef.value) {
        initChart()
        // 图表初始化后再加载趋势数据，避免竞态
        loadTrendData()
      }
    }, 100)
  })
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

const initChart = () => {
  if (!chartRef.value) return
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  chartInstance.value = echarts.init(chartRef.value)
  const chart = chartInstance.value
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['空腹血糖', '餐后血糖'],
      right: 0,
      top: 0,
      selectedMode: false
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: 'mmol/L',
      min: 4,
      max: 10
    },
    series: [
      {
        name: '空腹血糖',
        type: 'line',
        data: [5.8, 6.1, 5.9, 6.2, 5.7, 6.0, 5.9],
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#1a6b5a',
          width: 3
        },
        itemStyle: {
          color: '#1a6b5a'
        }
      },
      {
        name: '餐后血糖',
        type: 'line',
        data: [7.2, 7.8, 7.5, 8.1, 7.3, 7.6, 7.4],
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#c4956a',
          width: 3
        },
        itemStyle: {
          color: '#c4956a'
        }
      }
    ]
  }

  try {
    chart.setOption(option)
  } catch (e) {
    console.error('ECharts initChart error:', e)
  }
  window.addEventListener('resize', handleResize)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap');

.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'DM Sans', sans-serif;
}

/* ============================================
   WELCOME SECTION
   ============================================ */
.welcome-section {
  margin-bottom: var(--space-xl);
}

.welcome-content {
  background: linear-gradient(135deg, #0f2b25, #1a4a3e);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl) var(--space-3xl);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 80% 50%, rgba(196, 149, 106, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 20% 80%, rgba(26, 107, 90, 0.1) 0%, transparent 40%);
}

.welcome-text {
  position: relative;
  z-index: 1;
}

.welcome-greeting {
  font-size: 0.85rem;
  color: rgba(245, 240, 234, 0.6);
  margin-bottom: var(--space-sm);
  letter-spacing: 0.05em;
}

.welcome-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 2.2rem;
  font-weight: 600;
  color: #f5f0ea;
  margin-bottom: var(--space-md);
  letter-spacing: 0.02em;
}

.welcome-desc {
  font-size: 0.95rem;
  color: rgba(245, 240, 234, 0.7);
  line-height: 1.6;
}

.welcome-desc strong {
  color: #c4956a;
  font-weight: 600;
}

.welcome-art {
  position: relative;
  width: 160px;
  height: 160px;
  flex-shrink: 0;
}

.art-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(196, 149, 106, 0.2);
}

.art-circle-1 {
  width: 160px;
  height: 160px;
  top: 0;
  left: 0;
  animation: rotate 20s linear infinite;
}

.art-circle-2 {
  width: 120px;
  height: 120px;
  top: 20px;
  left: 20px;
  border-color: rgba(26, 107, 90, 0.3);
  animation: rotate 15s linear infinite reverse;
}

.art-circle-3 {
  width: 80px;
  height: 80px;
  top: 40px;
  left: 40px;
  border-color: rgba(196, 149, 106, 0.3);
  animation: rotate 10s linear infinite;
}

.art-pulse {
  position: absolute;
  width: 20px;
  height: 20px;
  top: 70px;
  left: 70px;
  background: #c4956a;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.3); opacity: 1; }
}

/* ============================================
   STATS SECTION
   ============================================ */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.stat-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  animation: fadeInUp 0.6s ease both;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.stat-arrow {
  position: absolute;
  right: var(--space-md);
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: all 0.3s ease;
  color: var(--color-text-muted);
}

.stat-card:hover .stat-arrow {
  opacity: 1;
  right: var(--space-lg);
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon {
  color: var(--color-primary);
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  letter-spacing: 0.03em;
}

.stat-value {
  font-family: 'Cormorant Garamond', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.7rem;
  font-weight: 600;
}

.stat-trend.up { color: var(--color-success); }
.stat-trend.down { color: var(--color-danger); }
.stat-trend.normal { color: var(--color-success); }
.stat-trend.warning { color: var(--color-warning); }

.stat-sparkline {
  position: absolute;
  bottom: 0;
  right: 0;
  opacity: 0.3;
}

/* ============================================
   MAIN GRID
   ============================================ */
.main-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: var(--space-xl);
  margin-bottom: var(--space-xl);
}

.grid-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
}

.card-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-text);
}

.card-subtitle {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.card-badge {
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-muted);
  padding: 4px 10px;
  border-radius: var(--radius-full);
}

.view-all {
  font-size: 0.8rem;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.view-all:hover {
  color: var(--color-primary-dark);
}

/* Chart */
.card-tabs {
  display: flex;
  gap: 4px;
  background: var(--color-bg);
  padding: 3px;
  border-radius: var(--radius-full);
}

.tab-btn {
  padding: 6px 14px;
  border: none;
  background: none;
  border-radius: var(--radius-full);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: var(--color-bg-card);
  color: var(--color-text);
  box-shadow: var(--shadow-sm);
}

.chart-container {
  width: 100%;
}

.chart-canvas {
  width: 100%;
  height: 280px;
}

/* Actions */
.actions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-md);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-lg) var(--space-md);
  border-radius: var(--radius-md);
  border: 1px solid rgba(0, 0, 0, 0.04);
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  cursor: pointer;
}

.action-item:hover {
  border-color: var(--color-primary);
  background: rgba(26, 107, 90, 0.02);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text);
}

.action-desc {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  text-align: center;
}

/* Activity */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.activity-item {
  display: flex;
  gap: var(--space-md);
  position: relative;
  padding-bottom: var(--space-lg);
}

.activity-item:last-child {
  padding-bottom: 0;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
  position: relative;
  z-index: 1;
}

.activity-line {
  position: absolute;
  left: 4px;
  top: 18px;
  bottom: 0;
  width: 2px;
  background: rgba(0, 0, 0, 0.06);
}

.activity-content {
  flex: 1;
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.activity-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text);
}

.activity-time {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

.activity-desc {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* Score */
.score-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xl);
}

.score-ring-container {
  position: relative;
  width: 140px;
  height: 140px;
}

.score-ring {
  width: 100%;
  height: 100%;
}

.score-progress {
  transition: stroke-dashoffset 1.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.score-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.score-number {
  font-family: 'Cormorant Garamond', serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1;
}

.score-unit {
  display: block;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.score-details {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.score-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.score-item-label {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}

.score-item-value {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text);
}

.score-bar {
  height: 6px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.score-bar-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 1s cubic-bezier(0.16, 1, 0.3, 1);
}

/* ============================================
   AI SECTION
   ============================================ */
.ai-card {
  background: linear-gradient(135deg, rgba(26, 107, 90, 0.04), rgba(196, 149, 106, 0.04));
  border: 1px solid rgba(26, 107, 90, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  display: flex;
  align-items: flex-start;
  gap: var(--space-lg);
}

.ai-icon {
  width: 48px;
  height: 48px;
  background: rgba(26, 107, 90, 0.1);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  flex-shrink: 0;
}

.ai-content {
  flex: 1;
}

.ai-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
}

.ai-text {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.ai-action {
  padding: 10px 20px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-family: 'DM Sans', sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  align-self: center;
}

.ai-action:hover {
  background: var(--color-primary-light);
  transform: translateY(-1px);
}

/* ============================================
   RESPONSIVE
   ============================================ */
@media (max-width: 1024px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .main-grid {
    grid-template-columns: 1fr;
  }

  .welcome-art {
    display: none;
  }
}

@media (max-width: 640px) {
  .stats-section {
    grid-template-columns: 1fr;
  }

  .welcome-content {
    padding: var(--space-xl);
  }

  .welcome-title {
    font-size: 1.6rem;
  }

  .ai-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
