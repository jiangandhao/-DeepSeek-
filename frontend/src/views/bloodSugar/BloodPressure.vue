<template>
  <div class="blood-pressure-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>血压记录</span>
          <el-button type="primary" @click="handleAdd">添加血压记录</el-button>
        </div>
      </template>
      <el-table :data="recordList" style="width: 100%" v-loading="loading">
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
            <el-tag :type="getStatus(row.systolic, row.diastolic).type">
              {{ getStatus(row.systolic, row.diastolic).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑血压记录' : '添加血压记录'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="测量时间">
          <el-date-picker v-model="form.measureTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="收缩压(mmHg)">
          <el-input-number v-model="form.systolic" :min="60" :max="250" />
        </el-form-item>
        <el-form-item label="舒张压(mmHg)">
          <el-input-number v-model="form.diastolic" :min="40" :max="150" />
        </el-form-item>
        <el-form-item label="心率(bpm)">
          <el-input-number v-model="form.heartRate" :min="40" :max="200" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" placeholder="如：晨起测量、运动后" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getBloodPressureRecords, addBloodPressureRecord, deleteBloodPressureRecord } from '@/api/bloodPressure'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const recordList = ref([])

const form = reactive({
  measureTime: '',
  systolic: 120,
  diastolic: 80,
  heartRate: 70,
  note: ''
})

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

const getStatus = (systolic, diastolic) => {
  if (systolic == null || diastolic == null) return { label: '-', type: 'info' }
  if (systolic < 120 && diastolic < 80) return { label: '正常', type: 'success' }
  if (systolic < 140 && diastolic < 90) return { label: '偏高', type: 'warning' }
  return { label: '过高', type: 'danger' }
}

const resetForm = () => {
  form.measureTime = ''
  form.systolic = 120
  form.diastolic = 80
  form.heartRate = 70
  form.note = ''
  editingId.value = null
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getBloodPressureRecords(userStore.userId)
    recordList.value = (res?.data || []).sort((a, b) => {
      return (b.measureTime || '').localeCompare(a.measureTime || '')
    })
  } catch (error) {
    console.error('加载血压记录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  form.measureTime = row.measureTime
  form.systolic = row.systolic
  form.diastolic = row.diastolic
  form.heartRate = row.heartRate
  form.note = row.note || ''
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条血压记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteBloodPressureRecord(row.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除血压记录失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!form.measureTime) {
    ElMessage.warning('请选择测量时间')
    return
  }
  submitLoading.value = true
  try {
    const data = {
      userId: userStore.userId,
      measureTime: form.measureTime,
      systolic: form.systolic,
      diastolic: form.diastolic,
      heartRate: form.heartRate,
      note: form.note
    }
    if (isEdit.value) {
      await request({ url: `/blood-pressure/records/${editingId.value}`, method: 'put', data })
      ElMessage.success('更新成功')
    } else {
      await addBloodPressureRecord(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadRecords()
  } catch (error) {
    console.error('保存血压记录失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadRecords()
})
</script>

<style scoped>
.blood-pressure-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
