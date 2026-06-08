<template>
  <el-card>
    <template #header><span>饮食记录</span></template>
    <el-form :inline="true" :model="form" class="add-form">
      <el-form-item label="餐次">
        <el-select v-model="form.mealType" style="width: 110px">
          <el-option v-for="m in meals" :key="m.v" :label="m.l" :value="m.v" />
        </el-select>
      </el-form-item>
      <el-form-item label="食物">
        <el-input v-model="form.food" placeholder="如:米饭一碗+青菜" style="width: 220px" />
      </el-form-item>
      <el-form-item label="热量">
        <el-input-number v-model="form.calories" :min="0" :step="50" />
      </el-form-item>
      <el-form-item label="碳水(g)">
        <el-input-number v-model="form.carbsG" :min="0" :precision="1" :step="1" />
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="form.eatenAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="onAdd">添加</el-button></el-form-item>
    </el-form>

    <el-table :data="records" stripe>
      <el-table-column prop="eatenAt" label="时间" :formatter="(r) => r.eatenAt?.replace('T', ' ')" />
      <el-table-column prop="mealType" label="餐次" :formatter="(r) => mealLabel(r.mealType)" />
      <el-table-column prop="food" label="食物" />
      <el-table-column prop="calories" label="热量(kcal)" />
      <el-table-column prop="carbsG" label="碳水(g)" />
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
import { addDiet, listDiet, deleteDiet } from '../api'

const meals = [
  { v: 'BREAKFAST', l: '早餐' },
  { v: 'LUNCH', l: '午餐' },
  { v: 'DINNER', l: '晚餐' },
  { v: 'SNACK', l: '加餐' }
]
const records = ref([])
const form = ref({ mealType: 'BREAKFAST', food: '', calories: 0, carbsG: 0, eatenAt: new Date().toISOString().slice(0, 19) })

const mealLabel = (v) => meals.find((m) => m.v === v)?.l || v

async function load() { records.value = await listDiet({}) }

async function onAdd() {
  if (!form.value.food) { ElMessage.warning('请输入食物'); return }
  await addDiet(form.value)
  ElMessage.success('添加成功')
  await load()
}

async function onDelete(id) {
  await deleteDiet(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.add-form { margin-bottom: 16px; }
</style>
