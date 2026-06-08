<template>
  <div class="weight-record-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>体重记录</span>
          <el-button type="primary" @click="handleAdd">记录体重</el-button>
        </div>
      </template>

      <!-- 当前体重信息 -->
      <el-descriptions :column="4" border style="margin-bottom: 20px;">
        <el-descriptions-item label="当前体重">{{ currentWeight ? currentWeight.toFixed(1) + ' kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标体重">{{ targetWeight ? targetWeight.toFixed(1) + ' kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ height ? height + ' cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="BMI">{{ bmi }}</el-descriptions-item>
      </el-descriptions>

      <!-- 体重趋势图 -->
      <div ref="chartRef" style="width: 100%; height: 300px; margin-bottom: 20px;"></div>

      <!-- 体重记录表格 -->
      <el-table :data="recordList" style="width: 100%" v-loading="loading">
        <el-table-column prop="date" label="日期" width="180" />
        <el-table-column prop="weight" label="体重(kg)" width="140">
          <template #default="{ row }">
            {{ row.weight != null ? row.weight.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="变化" width="120">
          <template #default="{ row, $index }">
            <span v-if="$index < recordList.length - 1 && recordList[$index + 1].weight != null && row.weight != null"
              :style="{ color: row.weight < recordList[$index + 1].weight ? '#67c23a' : row.weight > recordList[$index + 1].weight ? '#f56c6c' : '#909399' }">
              {{ row.weight < recordList[$index + 1].weight ? '↓' : row.weight > recordList[$index + 1].weight ? '↑' : '-' }}
              {{ Math.abs(row.weight - recordList[$index + 1].weight).toFixed(1) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.weight != null && row.weight <= 65 ? 'success' : 'warning'">
              {{ row.weight != null && row.weight <= 65 ? '达标' : '未达标' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 记录体重对话框 -->
    <el-dialog v-model="dialogVisible" title="记录体重" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="30" :max="200" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserHealthProfile, updateUserHealthProfile, getWeightTrend } from '@/api/healthManager'
import * as echarts from 'echarts'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const chartRef = ref(null)
let chartInstance = null

const currentWeight = ref(null)
const targetWeight = ref(65)
const height = ref(null)
const recordList = ref([])

const bmi = computed(() => {
  if (!currentWeight.value || !height.value) return '-'
  const h = height.value / 100
  return (currentWeight.value / (h * h)).toFixed(1)
})

const form = ref({ weight: 60 })

// 加载体重趋势数据
const loadWeightData = async () => {
  loading.value = true
  try {
    // 获取健康档案
    const profileRes = await getUserHealthProfile(userStore.userId)
    if (profileRes && profileRes.data) {
      currentWeight.value = profileRes.data.weight
      height.value = profileRes.data.height
    }

    // 获取体重趋势
    const res = await getWeightTrend(userStore.userId, '30d')
    if (res && res.data) {
      const dates = res.data.dates || []
      const weights = res.data.weights || []
      recordList.value = dates.map((d, i) => ({ date: d, weight: weights[i] })).reverse()
      renderChart(dates, weights)
    }
  } catch (error) {
    console.error('加载体重数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 渲染趋势图
const renderChart = (dates, weights) => {
  if (!chartRef.value) return
  try {
    if (chartInstance) chartInstance.dispose()
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['体重', '目标'], selectedMode: false },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
      xAxis: { type: 'category', data: dates.map(d => d.replace('-', '/')), boundaryGap: false },
      yAxis: { type: 'value', name: 'kg', min: Math.min(...weights.filter(w => w != null)) - 2, max: Math.max(...weights.filter(w => w != null)) + 2 },
      series: [
        { name: '体重', type: 'line', data: weights, smooth: true, symbol: 'circle', symbolSize: 8, lineStyle: { color: '#E6A23C', width: 3 }, itemStyle: { color: '#E6A23C' } },
        { name: '目标', type: 'line', data: dates.map(() => targetWeight.value), lineStyle: { color: '#67c23a', type: 'dashed', width: 2 }, itemStyle: { color: '#67c23a' }, symbol: 'none' }
      ]
    })
  } catch (e) {
    console.error('图表渲染失败:', e)
  }
}

const handleAdd = () => {
  form.value.weight = currentWeight.value || 60
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    await updateUserHealthProfile(userStore.userId, { weight: form.value.weight })
    ElMessage.success('记录成功')
    dialogVisible.value = false
    loadWeightData()
  } catch (error) {
    console.error('保存体重失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadWeightData()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.weight-record-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
