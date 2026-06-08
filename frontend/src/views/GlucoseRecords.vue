<template>
  <el-card>
    <template #header><span>血糖记录</span></template>
    <el-form :inline="true" :model="form" class="add-form">
      <el-form-item label="血糖值">
        <el-input-number v-model="form.valueMmol" :precision="1" :step="0.1" :min="0" :max="40" />
      </el-form-item>
      <el-form-item label="时段">
        <el-select v-model="form.period" placeholder="时段" style="width: 120px">
          <el-option v-for="p in periods" :key="p.v" :label="p.l" :value="p.v" />
        </el-select>
      </el-form-item>
      <el-form-item label="测量时间">
        <el-date-picker v-model="form.measuredAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onAdd">添加</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" stripe>
      <el-table-column prop="measuredAt" label="测量时间" :formatter="(r) => r.measuredAt?.replace('T', ' ')" />
      <el-table-column prop="valueMmol" label="血糖 (mmol/L)" />
      <el-table-column prop="period" label="时段" :formatter="(r) => periodLabel(r.period)" />
      <el-table-column prop="note" label="备注" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text type="danger" @click="onDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addGlucose, listGlucose, deleteGlucose } from '../api'

const periods = [
  { v: 'FASTING', l: '空腹' },
  { v: 'BEFORE_MEAL', l: '餐前' },
  { v: 'AFTER_MEAL', l: '餐后' },
  { v: 'BEDTIME', l: '睡前' },
  { v: 'RANDOM', l: '随机' }
]
const records = ref([])
const form = ref({ valueMmol: 6.0, period: 'FASTING', measuredAt: new Date().toISOString().slice(0, 19) })

const periodLabel = (v) => periods.find((p) => p.v === v)?.l || v

async function load() { records.value = await listGlucose({}) }

async function onAdd() {
  if (!form.value.measuredAt) { ElMessage.warning('请选择测量时间'); return }
  await addGlucose(form.value)
  ElMessage.success('添加成功')
  await load()
}

async function onDelete(id) {
  await deleteGlucose(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.add-form { margin-bottom: 16px; }
</style>
