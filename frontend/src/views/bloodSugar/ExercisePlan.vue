<template>
  <div class="exercise-plan-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运动计划</span>
          <el-button type="primary" @click="handleAdd">添加运动记录</el-button>
        </div>
      </template>
      <el-table :data="exerciseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="startTime" label="日期" width="180">
          <template #default="{ row }">
            {{ row.startTime ? row.startTime.split(' ')[0] : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="exerciseType" label="运动类型" width="120"></el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="120"></el-table-column>
        <el-table-column prop="calorieConsumed" label="消耗热量(kcal)" width="140"></el-table-column>
        <el-table-column prop="note" label="备注"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑运动记录' : '添加运动记录'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="运动类型">
          <el-input v-model="form.exerciseType" placeholder="如：快走、跑步、游泳" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.duration" :min="1" />
        </el-form-item>
        <el-form-item label="消耗热量(kcal)">
          <el-input-number v-model="form.calorieConsumed" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" placeholder="运动备注" />
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
import { getExerciseRecords, addExerciseRecord, updateExerciseRecord, deleteExerciseRecord } from '@/api/bloodSugar'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const exerciseList = ref([])

const form = reactive({
  exerciseType: '',
  duration: 30,
  calorieConsumed: 0,
  startTime: '',
  note: ''
})

const resetForm = () => {
  form.exerciseType = ''
  form.duration = 30
  form.calorieConsumed = 0
  form.startTime = ''
  form.note = ''
  editingId.value = null
}

const loadExerciseRecords = async () => {
  loading.value = true
  try {
    const res = await getExerciseRecords(userStore.userId)
    exerciseList.value = res?.data || []
  } catch (error) {
    console.error('加载运动记录失败:', error)
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
  form.exerciseType = row.exerciseType
  form.duration = row.duration
  form.calorieConsumed = row.calorieConsumed
  form.startTime = row.startTime
  form.note = row.note
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条运动记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteExerciseRecord(row.id)
    ElMessage.success('删除成功')
    loadExerciseRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除运动记录失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!form.exerciseType) {
    ElMessage.warning('请输入运动类型')
    return
  }
  submitLoading.value = true
  try {
    const data = {
      userId: userStore.userId,
      exerciseType: form.exerciseType,
      duration: form.duration,
      calorieConsumed: form.calorieConsumed,
      startTime: form.startTime,
      endTime: '',
      note: form.note
    }
    if (isEdit.value) {
      await updateExerciseRecord(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addExerciseRecord(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadExerciseRecords()
  } catch (error) {
    console.error('保存运动记录失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadExerciseRecords()
})
</script>

<style scoped>
.exercise-plan-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
