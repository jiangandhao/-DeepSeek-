<template>
  <div>
    <el-row :gutter="16" class="cards">
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ stats.count }}</div><div class="label">血糖记录数</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ stats.avg }}</div><div class="label">平均血糖 mmol/L</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ stats.max }}</div><div class="label">最高值</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat"><div class="num">{{ stats.min }}</div><div class="label">最低值</div></div></el-card></el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>
        <div class="chart-header">
          <span>血糖趋势</span>
          <el-radio-group v-model="days" size="small" @change="load">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
            <el-radio-button :value="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" style="height: 420px"></div>
      <el-empty v-if="!records.length" description="暂无血糖数据,请先去『血糖记录』添加" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listGlucose } from '../api'

const days = ref(30)
const records = ref([])
const chartRef = ref(null)
let chart = null

const stats = computed(() => {
  if (!records.value.length) return { count: 0, avg: '-', max: '-', min: '-' }
  const vals = records.value.map((r) => Number(r.valueMmol))
  const sum = vals.reduce((a, b) => a + b, 0)
  return {
    count: vals.length,
    avg: (sum / vals.length).toFixed(1),
    max: Math.max(...vals).toFixed(1),
    min: Math.min(...vals).toFixed(1)
  }
})

async function load() {
  const from = new Date(Date.now() - days.value * 24 * 3600 * 1000).toISOString().slice(0, 19)
  records.value = await listGlucose({ from })
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const data = records.value.map((r) => [r.measuredAt.replace('T', ' '), Number(r.valueMmol)])
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 30, top: 30, bottom: 60 },
    xAxis: { type: 'category', data: data.map((d) => d[0]), axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: 'mmol/L', min: 0 },
    series: [
      {
        name: '血糖',
        type: 'line',
        smooth: true,
        data: data.map((d) => d[1]),
        itemStyle: { color: '#409eff' },
        markArea: {
          itemStyle: { color: 'rgba(103,194,58,0.12)' },
          data: [[{ yAxis: 3.9 }, { yAxis: 7.8 }]]
        },
        markLine: {
          silent: true,
          symbol: 'none',
          data: [
            { yAxis: 3.9, lineStyle: { color: '#e6a23c' }, label: { formatter: '偏低 3.9' } },
            { yAxis: 7.8, lineStyle: { color: '#f56c6c' }, label: { formatter: '偏高 7.8' } }
          ]
        }
      }
    ]
  })
}

function onResize() { chart && chart.resize() }

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart && chart.dispose()
})
</script>

<style scoped>
.cards .stat { text-align: center; }
.cards .num { font-size: 28px; font-weight: bold; color: #409eff; }
.cards .label { color: #909399; margin-top: 6px; font-size: 13px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
</style>
