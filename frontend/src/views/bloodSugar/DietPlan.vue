<template>
  <div class="diet-plan-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>饮食计划</span>
          <el-button type="primary" @click="handleAdd">添加饮食记录</el-button>
        </div>
      </template>
      <el-table :data="dietList" style="width: 100%" v-loading="loading">
        <el-table-column prop="recordDate" label="日期" width="180"></el-table-column>
        <el-table-column prop="mealType" label="餐次" width="120">
          <template #default="{ row }">
            {{ mealTypeMap[row.mealType] || row.mealType }}
          </template>
        </el-table-column>
        <el-table-column prop="foodName" label="食物"></el-table-column>
        <el-table-column prop="calorie" label="热量(kcal)" width="120"></el-table-column>
        <el-table-column prop="carbohydrate" label="碳水(g)" width="100"></el-table-column>
        <el-table-column prop="protein" label="蛋白质(g)" width="100"></el-table-column>
        <el-table-column prop="fat" label="脂肪(g)" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑饮食记录' : '添加饮食记录'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="form.mealType" placeholder="选择餐次">
            <el-option label="早餐" value="breakfast" />
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
            <el-option label="加餐" value="snack" />
          </el-select>
        </el-form-item>
        <el-form-item label="食物名称">
          <el-input v-model="form.foodName" placeholder="请输入食物名称" />
        </el-form-item>
        <el-form-item label="食物用量(g)">
          <el-input-number v-model="form.foodAmount" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="热量(kcal)">
          <el-input-number v-model="form.calorie" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="碳水(g)">
          <el-input-number v-model="form.carbohydrate" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="蛋白质(g)">
          <el-input-number v-model="form.protein" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="脂肪(g)">
          <el-input-number v-model="form.fat" :min="0" :precision="1" />
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
import { getDietRecords, addDietRecord, updateDietRecord, deleteDietRecord } from '@/api/bloodSugar'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const mealTypeMap = {
  breakfast: '早餐',
  lunch: '午餐',
  dinner: '晚餐',
  snack: '加餐'
}

const dietList = ref([])

const form = reactive({
  recordDate: '',
  mealType: 'breakfast',
  foodName: '',
  foodAmount: 0,
  calorie: 0,
  carbohydrate: 0,
  protein: 0,
  fat: 0
})

const resetForm = () => {
  form.recordDate = ''
  form.mealType = 'breakfast'
  form.foodName = ''
  form.foodAmount = 0
  form.calorie = 0
  form.carbohydrate = 0
  form.protein = 0
  form.fat = 0
  editingId.value = null
}

const loadDietRecords = async () => {
  loading.value = true
  try {
    const res = await getDietRecords(userStore.userId)
    dietList.value = res?.data || []
  } catch (error) {
    console.error('加载饮食记录失败:', error)
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
  form.recordDate = row.recordDate
  form.mealType = row.mealType
  form.foodName = row.foodName
  form.foodAmount = row.foodAmount
  form.calorie = row.calorie
  form.carbohydrate = row.carbohydrate
  form.protein = row.protein
  form.fat = row.fat
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条饮食记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDietRecord(row.id)
    ElMessage.success('删除成功')
    loadDietRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除饮食记录失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!form.foodName) {
    ElMessage.warning('请输入食物名称')
    return
  }
  submitLoading.value = true
  try {
    const data = {
      userId: userStore.userId,
      recordDate: form.recordDate,
      mealType: form.mealType,
      foodName: form.foodName,
      foodAmount: form.foodAmount,
      calorie: form.calorie,
      carbohydrate: form.carbohydrate,
      protein: form.protein,
      fat: form.fat
    }
    if (isEdit.value) {
      await updateDietRecord(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addDietRecord(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadDietRecords()
  } catch (error) {
    console.error('保存饮食记录失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadDietRecords()
})
</script>

<style scoped>
.diet-plan-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
