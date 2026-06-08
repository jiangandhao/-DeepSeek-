<template>
  <el-card>
    <template #header><span>运动记录</span></template>
    <el-form :inline="true" :model="form" class="add-form">
      <el-form-item label="类型">
        <el-input v-model="form.type" placeholder="如:快走/跑步/游泳" style="width: 150px" />
      </el-form-item>
      <el-form-item label="时长(分)">
        <el-input-number v-model="form.durationMin" :min="1" :step="5" />
      </el-form-item>
      <el-form-item label="强度">
        <el-select v-model="form.intensity" style="width: 100px">
          <el-option v-for="i in intensities" :key="i.v" :label="i.l" :value="i.v" />
        </el-select>
      </el-form-item>
      <el-form-item label="消耗(kcal)">
        <el-input-number v-model="form.calories" :min="0" :step="50" />
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="form.doneAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="onAdd">添加</el-button></el-form-item>
    </el-form>

    <el-table :data="records" stripe>
      <el-table-column prop="doneAt" label="时间" :formatter="(r) => r.doneAt?.replace('T', ' ')" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="durationMin" label="时长(分)" />
      <el-table-column prop="intensity" label="强度" :formatter="(r) => intLabel(r.intensity)" />
      <el-table-column prop="calories" label="消耗(kcal)" />
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
import { addExercise, listExercise, deleteExercise } from '../api'

const intensities = [
  { v: 'LOW', l: '低' },
  { v: 'MEDIUM', l: '中' },
  { v: 'HIGH', l: '高' }
]
const records = ref([])
const form = ref({ type: '', durationMin: 30, intensity: 'MEDIUM', calories: 0, doneAt: new Date().toISOString().slice(0, 19) })

const intLabel = (v) => intensities.find((i) => i.v === v)?.l || v

async function load() { records.value = await listExercise({}) }

async function onAdd() {
  if (!form.value.type) { ElMessage.warning('请输入运动类型'); return }
  await addExercise(form.value)
  ElMessage.success('添加成功')
  await load()
}

async function onDelete(id) {
  await deleteExercise(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.add-form { margin-bottom: 16px; }
</style>
