<template>
  <div class="prevention-plan">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个性化预防方案</span>
              <el-button type="primary" @click="generatePlan" :loading="loading">
                <el-icon><MagicStick /></el-icon>
                AI生成方案
              </el-button>
            </div>
          </template>

          <el-collapse v-model="activePlan">
            <el-collapse-item title="🏃 运动处方" name="exercise">
              <div class="plan-section">
                <h4>运动建议</h4>
                <el-table :data="exercisePlan" border size="small">
                  <el-table-column prop="type" label="运动类型" width="120" />
                  <el-table-column prop="frequency" label="频率" width="120" />
                  <el-table-column prop="duration" label="时长" width="100" />
                  <el-table-column prop="intensity" label="强度" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getIntensityType(row.intensity)">{{ getIntensityLabel(row.intensity) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="note" label="注意事项" />
                </el-table>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🥗 饮食方案" name="diet">
              <div class="plan-section">
                <h4>饮食原则</h4>
                <ul>
                  <li v-for="(principle, index) in dietPrinciples" :key="index">{{ principle }}</li>
                </ul>

                <h4>每日营养目标</h4>
                <el-row :gutter="20">
                  <el-col :span="6" v-for="nutrient in nutritionTargets" :key="nutrient.name">
                    <el-statistic :title="nutrient.name" :value="nutrient.value" :suffix="nutrient.unit" />
                  </el-col>
                </el-row>
              </div>
            </el-collapse-item>

            <el-collapse-item title="💊 用药指导" name="medication">
              <div class="plan-section">
                <el-table :data="medications" border size="small">
                  <el-table-column prop="name" label="药物名称" width="150" />
                  <el-table-column prop="purpose" label="用途" width="150" />
                  <el-table-column prop="dosage" label="剂量" width="120" />
                  <el-table-column prop="frequency" label="服用频率" width="120" />
                  <el-table-column prop="sideEffects" label="注意事项" />
                </el-table>
                <el-alert title="请遵医嘱用药，勿自行调整剂量" type="warning" :closable="false" style="margin-top: 15px;" />
              </div>
            </el-collapse-item>

            <el-collapse-item title="📋 定期检查" name="checkup">
              <div class="plan-section">
                <el-timeline>
                  <el-timeline-item v-for="item in checkupSchedule" :key="item.id"
                    :timestamp="item.date" placement="top" :type="item.urgent ? 'danger' : 'primary'">
                    <el-card>
                      <h4>{{ item.title }}</h4>
                      <p>{{ item.description }}</p>
                      <el-tag v-if="item.urgent" type="danger" size="small">紧急</el-tag>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🧘 心理健康" name="mental">
              <div class="plan-section">
                <h4>心理调适建议</h4>
                <div v-for="(advice, index) in mentalAdvice" :key="index" class="mental-item">
                  <el-icon :style="{ color: advice.color }"><component :is="advice.icon" /></el-icon>
                  <div>
                    <h4>{{ advice.title }}</h4>
                    <p>{{ advice.content }}</p>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>方案执行进度</span>
          </template>
          <div v-for="item in progress" :key="item.name" class="progress-item">
            <div class="progress-header">
              <span>{{ item.name }}</span>
              <span>{{ item.completed }}/{{ item.total }}</span>
            </div>
            <el-progress :percentage="(item.completed / item.total) * 100" :color="item.color" />
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>健康目标</span>
          </template>
          <div v-for="goal in healthGoals" :key="goal.name" class="goal-item">
            <div class="goal-icon" :class="goal.status">
              <el-icon><component :is="goal.icon" /></el-icon>
            </div>
            <div class="goal-info">
              <h4>{{ goal.name }}</h4>
              <p>{{ goal.target }}</p>
              <el-progress :percentage="goal.progress" :show-text="false" />
            </div>
            <el-button size="small" type="primary" @click="updateGoalProgress(goal)">更新进度</el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>专家建议</span>
          </template>
          <div class="expert-advice">
            <div class="expert-avatar">
              <el-avatar :size="48">AI</el-avatar>
            </div>
            <div class="expert-content">
              <p>{{ expertAdvice }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPreventionPlan, generatePreventionPlan as generatePreventionPlanApi, getHealthGoals, updateHealthGoal as updateHealthGoalApi, getHealthAdvice } from '@/api/riskWarning'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const activePlan = ref(['exercise'])

const exercisePlan = ref([])
const dietPrinciples = ref([])
const nutritionTargets = ref([])
const medications = ref([])
const checkupSchedule = ref([])
const mentalAdvice = ref([])
const progress = ref([])
const healthGoals = ref([])
const expertAdvice = ref('')

const loadPlan = async () => {
  try {
    const res = await getPreventionPlan(userStore.userId)
    if (res.data) {
      const data = res.data
      exercisePlan.value = data.exercisePlan || []
      dietPrinciples.value = data.dietPrinciples || []
      nutritionTargets.value = data.nutritionTargets || []
      medications.value = data.medications || []
      checkupSchedule.value = data.checkupSchedule || []
      mentalAdvice.value = data.mentalAdvice || []
      progress.value = data.progress || []
    }
  } catch (error) {
    console.error('加载预防方案失败:', error)
  }
}

const loadGoals = async () => {
  try {
    const res = await getHealthGoals(userStore.userId)
    healthGoals.value = res.data || []
  } catch (error) {
    console.error('加载健康目标失败:', error)
  }
}

const loadAdvice = async () => {
  try {
    const res = await getHealthAdvice(userStore.userId)
    expertAdvice.value = res.data || ''
  } catch (error) {
    console.error('加载健康建议失败:', error)
  }
}

const getIntensityType = (intensity) => {
  const map = { 'low': 'success', 'medium': 'warning', 'high': 'danger' }
  return map[intensity] || 'info'
}

const getIntensityLabel = (intensity) => {
  const map = { 'low': '低', 'medium': '中', 'high': '高' }
  return map[intensity] || intensity
}

const updateGoalProgress = async (goal) => {
  try {
    await updateHealthGoalApi(userStore.userId, goal.id, {
      progress: goal.progress,
      status: goal.status,
      currentValue: goal.currentValue
    })
    ElMessage.success('目标进度已更新')
  } catch (error) {
    console.error('更新目标进度失败:', error)
  }
}

const generatePlan = async () => {
  loading.value = true
  try {
    await generatePreventionPlanApi({ userId: userStore.userId })
    await loadPlan()
    ElMessage.success('预防方案生成成功')
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadPlan()
  loadGoals()
  loadAdvice()
})
</script>

<style scoped>
.prevention-plan {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.plan-section {
  padding: 15px;
}
.plan-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}
.plan-section ul {
  padding-left: 20px;
}
.plan-section li {
  margin-bottom: 8px;
  line-height: 1.6;
}
.mental-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.mental-item .el-icon {
  font-size: 24px;
  flex-shrink: 0;
}
.mental-item h4 {
  margin: 0 0 5px 0;
}
.mental-item p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
.progress-item {
  margin-bottom: 15px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.goal-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.goal-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.goal-icon.good { background: #f0f9eb; color: #67C23A; }
.goal-icon.in-progress { background: #fdf6ec; color: #E6A23C; }
.goal-info {
  flex: 1;
}
.goal-info h4 {
  margin: 0 0 5px 0;
}
.goal-info p {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 13px;
}
.expert-advice {
  display: flex;
  gap: 15px;
}
.expert-content p {
  margin: 0;
  line-height: 1.8;
  color: #333;
}
</style>
