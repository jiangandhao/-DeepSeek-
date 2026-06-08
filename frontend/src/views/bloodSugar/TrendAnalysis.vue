<template>
  <div class="health-records-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康记录</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="loadData"
          />
        </div>
      </template>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="血糖记录" name="bloodSugar">
          <el-table :data="bloodSugarRecords" stripe style="width: 100%">
            <el-table-column prop="measureTime" label="测量时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.measureTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="value" label="血糖值(mmol/L)" width="140" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.type).type">{{ getTypeTag(row.type).label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getSugarStatus(row.value, row.type).type">
                  {{ getSugarStatus(row.value, row.type).label }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="血压记录" name="bloodPressure">
          <el-table :data="bloodPressureRecords" stripe style="width: 100%">
            <el-table-column prop="measureTime" label="测量时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.measureTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="systolic" label="收缩压(mmHg)" width="140" />
            <el-table-column prop="diastolic" label="舒张压(mmHg)" width="140" />
            <el-table-column prop="heartRate" label="心率(bpm)" width="120" />
            <el-table-column prop="note" label="备注" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPressureStatus(row.systolic, row.diastolic).type">
                  {{ getPressureStatus(row.systolic, row.diastolic).label }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="体重记录" name="weight">
          <el-table :data="weightRecords" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="180" />
            <el-table-column prop="weight" label="体重(kg)" width="140">
              <template #default="{ row }">
                {{ row.weight != null ? row.weight.toFixed(1) : '-' }}
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
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBloodSugarRecords } from '@/api/bloodSugar'
import { getBloodPressureRecords } from '@/api/bloodPressure'
import { getWeightTrend } from '@/api/healthManager'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('bloodSugar')
const dateRange = ref([])
const bloodSugarRecords = ref([])
const bloodPressureRecords = ref([])
const weightRecords = ref([])

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 血糖类型标签
const getTypeTag = (type) => {
  const map = {
    fasting: { label: '空腹', type: 'primary' },
    before: { label: '餐前', type: 'info' },
    after: { label: '餐后', type: 'warning' }
  }
  return map[type] || { label: type || '-', type: 'info' }
}

// 血糖状态判断
const getSugarStatus = (value, type) => {
  if (value == null) return { label: '-', type: 'info' }
  if (type === 'fasting') {
    if (value < 3.9) return { label: '偏低', type: 'danger' }
    if (value <= 6.1) return { label: '正常', type: 'success' }
    if (value <= 7.0) return { label: '偏高', type: 'warning' }
    return { label: '过高', type: 'danger' }
  } else {
    if (value < 7.8) return { label: '正常', type: 'success' }
    if (value <= 11.1) return { label: '偏高', type: 'warning' }
    return { label: '过高', type: 'danger' }
  }
}

// 血压状态判断
const getPressureStatus = (systolic, diastolic) => {
  if (systolic == null || diastolic == null) return { label: '-', type: 'info' }
  if (systolic < 120 && diastolic < 80) return { label: '正常', type: 'success' }
  if (systolic < 140 && diastolic < 90) return { label: '偏高', type: 'warning' }
  return { label: '过高', type: 'danger' }
}

// 日期过滤
const filterByDate = (records, timeField = 'measureTime') => {
  if (!dateRange.value || dateRange.value.length !== 2) return records
  const [start, end] = dateRange.value
  return records.filter(r => {
    const d = (r[timeField] || '').replace('T', ' ').split(' ')[0]
    return d >= start && d <= end
  })
}

// 加载数据
const loadData = async () => {
  if (!userStore.userId) return
  try {
    if (activeTab.value === 'bloodSugar') {
      const res = await getBloodSugarRecords(userStore.userId)
      const records = (res.data || []).filter(r => r != null)
      bloodSugarRecords.value = filterByDate(records).sort((a, b) => {
        return (b.measureTime || '').localeCompare(a.measureTime || '')
      })
    } else if (activeTab.value === 'bloodPressure') {
      const res = await getBloodPressureRecords(userStore.userId)
      const records = (res.data || []).filter(r => r != null)
      bloodPressureRecords.value = filterByDate(records).sort((a, b) => {
        return (b.measureTime || '').localeCompare(a.measureTime || '')
      })
    } else if (activeTab.value === 'weight') {
      const res = await getWeightTrend(userStore.userId, '30d')
      if (res && res.data) {
        const dates = res.data.dates || []
        const weights = res.data.weights || []
        let records = dates.map((d, i) => ({ date: d, weight: weights[i] }))
        if (dateRange.value && dateRange.value.length === 2) {
          const [start, end] = dateRange.value
          records = records.filter(r => r.date >= start && r.date <= end)
        }
        weightRecords.value = records.reverse()
      }
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  }
}

onMounted(async () => {
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadData()
})
</script>

<style scoped>
.health-records-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-tabs__content) {
  padding-top: 10px;
}
</style>
