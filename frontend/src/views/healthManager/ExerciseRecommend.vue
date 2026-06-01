<template>
  <div class="exercise-recommend">
    <el-card class="header-card">
      <div class="page-header">
        <h2>AI运动推荐</h2>
        <p>根据您的身体状况和健康目标，智能推荐适合的运动方案</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 运动偏好设置 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>运动偏好设置</span>
          </template>
          <el-form :model="exercisePrefs" label-position="top">
            <el-form-item label="运动目标">
              <el-select v-model="exercisePrefs.goal" style="width: 100%">
                <el-option label="减脂塑形" value="weight_loss" />
                <el-option label="增强体质" value="fitness" />
                <el-option label="控制血糖" value="blood_sugar" />
                <el-option label="缓解压力" value="stress" />
                <el-option label="提高心肺功能" value="cardio" />
              </el-select>
            </el-form-item>
            <el-form-item label="运动时长偏好">
              <el-radio-group v-model="exercisePrefs.duration">
                <el-radio-button value="short">15-30分钟</el-radio-button>
                <el-radio-button value="medium">30-60分钟</el-radio-button>
                <el-radio-button value="long">60分钟以上</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="运动强度">
              <el-slider v-model="exercisePrefs.intensity" :min="1" :max="5" :marks="intensityMarks" />
            </el-form-item>
            <el-form-item label="可用器材">
              <el-checkbox-group v-model="exercisePrefs.equipment">
                <el-checkbox value="none">无器材</el-checkbox>
                <el-checkbox value="dumbbell">哑铃</el-checkbox>
                <el-checkbox value="yoga_mat">瑜伽垫</el-checkbox>
                <el-checkbox value="treadmill">跑步机</el-checkbox>
                <el-checkbox value="bicycle">动感单车</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="身体限制">
              <el-checkbox-group v-model="exercisePrefs.limitations">
                <el-checkbox value="knee">膝关节问题</el-checkbox>
                <el-checkbox value="waist">腰椎问题</el-checkbox>
                <el-checkbox value="heart">心脏问题</el-checkbox>
                <el-checkbox value="asthma">哮喘</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-button type="primary" style="width: 100%" @click="generatePlan" :loading="loading">
              <el-icon><MagicStick /></el-icon>
              生成运动方案
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 运动方案展示 -->
      <el-col :span="16">
        <el-card v-if="exercisePlan" class="plan-card">
          <template #header>
            <div class="card-header">
              <span>本周运动方案</span>
              <el-tag type="success">预计消耗: {{ exercisePlan.totalCalories }} kcal/周</el-tag>
            </div>
          </template>

          <!-- 每周运动日历 -->
          <div class="week-calendar">
            <div
              v-for="(day, index) in exercisePlan.weeklyPlan"
              :key="index"
              class="day-card"
              :class="{ 'rest-day': day.isRest, 'active-day': selectedDay === index }"
              @click="selectedDay = index"
            >
              <div class="day-name">{{ day.dayName }}</div>
              <div class="day-type">{{ day.isRest ? '休息' : day.type }}</div>
              <div class="day-duration" v-if="!day.isRest">{{ day.duration }}分钟</div>
            </div>
          </div>

          <!-- 选中日期的详细运动 -->
          <div v-if="selectedDayPlan" class="day-detail">
            <h4>{{ selectedDayPlan.dayName }} - {{ selectedDayPlan.type || '休息日' }}</h4>

            <div v-if="!selectedDayPlan.isRest" class="exercises">
              <div v-for="(exercise, idx) in selectedDayPlan.exercises" :key="idx" class="exercise-item">
                <div class="exercise-header">
                  <span class="exercise-name">{{ exercise.name }}</span>
                  <el-tag size="small">{{ exercise.intensity }}</el-tag>
                </div>
                <div class="exercise-info">
                  <span><el-icon><Timer /></el-icon> {{ exercise.duration }}分钟</span>
                  <span><el-icon><Flame /></el-icon> {{ exercise.calories }}kcal</span>
                  <span><el-icon><Document /></el-icon> {{ exercise.sets }}组 × {{ exercise.reps }}次</span>
                </div>
                <div class="exercise-steps">
                  <p v-for="(step, sIdx) in exercise.steps" :key="sIdx">{{ sIdx + 1 }}. {{ step }}</p>
                </div>
                <div class="exercise-tips" v-if="exercise.tips">
                  <el-alert :title="exercise.tips" type="info" :closable="false" show-icon />
                </div>
              </div>
            </div>

            <div v-else class="rest-content">
              <el-result icon="info" title="休息日" sub-title="适当进行轻度活动，如散步、拉伸，帮助身体恢复">
                <template #extra>
                  <el-button type="primary" @click="viewRecoveryTips">查看恢复建议</el-button>
                </template>
              </el-result>
            </div>
          </div>

          <!-- 运动统计 -->
          <el-divider>运动统计</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="本周运动次数" :value="exercisePlan.workoutDays" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="总运动时长" :value="exercisePlan.totalDuration" suffix="分钟" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="预计消耗热量" :value="exercisePlan.totalCalories" suffix="kcal" />
            </el-col>
          </el-row>
        </el-card>

        <el-empty v-else description="设置运动偏好后，点击生成运动方案" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Timer, Flame, Document } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const selectedDay = ref(0)
const exercisePlan = ref(null)

const exercisePrefs = reactive({
  goal: 'fitness',
  duration: 'medium',
  intensity: 3,
  equipment: ['none'],
  limitations: []
})

const intensityMarks = {
  1: '轻松',
  2: '适中',
  3: '中等',
  4: '较高',
  5: '高强度'
}

const selectedDayPlan = computed(() => {
  if (!exercisePlan.value) return null
  return exercisePlan.value.weeklyPlan[selectedDay.value]
})

const generatePlan = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/health-manager/exercise-recommend', exercisePrefs)
    exercisePlan.value = res.data
    ElMessage.success('运动方案生成成功')
  } catch (error) {
    // 模拟数据
    exercisePlan.value = {
      workoutDays: 5,
      totalDuration: 280,
      totalCalories: 1400,
      weeklyPlan: [
        {
          dayName: '周一', type: '有氧运动', duration: 45, isRest: false,
          exercises: [
            {
              name: '快走', intensity: '中等', duration: 30, calories: 180, sets: 1, reps: 1,
              steps: ['热身5分钟慢走', '快走20分钟(时速6km)', '放松5分钟慢走', '拉伸5分钟'],
              tips: '保持心率在120-140之间，感到微喘但能说话为宜'
            }
          ]
        },
        {
          dayName: '周二', type: '力量训练', duration: 40, isRest: false,
          exercises: [
            { name: '深蹲', intensity: '中等', duration: 10, calories: 60, sets: 3, reps: 15, steps: ['双脚与肩同宽', '屈髋屈膝下蹲', '膝盖不超过脚尖', '蹲至大腿与地面平行'], tips: '注意保持背部挺直' },
            { name: '俯卧撑', intensity: '较高', duration: 8, calories: 50, sets: 3, reps: 12, steps: ['双手撑地略宽于肩', '身体保持一条直线', '屈肘下降', '推起回到起始位置'], tips: '初学者可采用跪姿俯卧撑' },
            { name: '平板支撑', intensity: '中等', duration: 5, calories: 30, sets: 3, reps: 1, steps: ['双肘撑地', '身体保持一条直线', '收紧核心', '保持呼吸'], tips: '每次坚持30-60秒' }
          ]
        },
        {
          dayName: '周三', type: '瑜伽拉伸', duration: 40, isRest: false,
          exercises: [
            { name: '瑜伽流', intensity: '轻松', duration: 40, calories: 120, sets: 1, reps: 1, steps: ['猫牛式热身', '下犬式', '战士一式', '战士二式', '树式平衡', '婴儿式放松'], tips: '配合呼吸，动作缓慢流畅' }
          ]
        },
        {
          dayName: '周四', type: '休息日', duration: 0, isRest: true, exercises: []
        },
        {
          dayName: '周五', type: '有氧+力量', duration: 50, isRest: false,
          exercises: [
            { name: '跳绳', intensity: '较高', duration: 15, calories: 150, sets: 3, reps: 1, steps: ['热身活动脚踝', '双脚跳绳3分钟', '休息1分钟', '重复3组'], tips: '选择缓冲好的运动鞋' },
            { name: '哑铃训练', intensity: '中等', duration: 25, calories: 100, sets: 3, reps: 12, steps: ['哑铃弯举', '哑铃推举', '哑铃划船', '哑铃飞鸟'], tips: '选择适中重量，动作标准优先' }
          ]
        },
        {
          dayName: '周六', type: '户外运动', duration: 60, isRest: false,
          exercises: [
            { name: '骑行/慢跑', intensity: '适中', duration: 60, calories: 350, sets: 1, reps: 1, steps: ['热身5分钟', '匀速运动50分钟', '放松5分钟'], tips: '选择安全的路线，注意补水' }
          ]
        },
        {
          dayName: '周日', type: '休息日', duration: 0, isRest: true, exercises: []
        }
      ]
    }
  } finally {
    loading.value = false
  }
}

const viewRecoveryTips = () => {
  ElMessage.info('恢复建议：轻度拉伸、充足睡眠、适量补水')
}
</script>

<style scoped>
.exercise-recommend { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.week-calendar {
  display: flex; justify-content: space-between; margin-bottom: 20px;
}
.day-card {
  flex: 1; margin: 0 4px; padding: 12px; text-align: center;
  border-radius: 8px; cursor: pointer; transition: all 0.3s;
  background: #f5f7fa; border: 2px solid transparent;
}
.day-card:hover { background: #ecf5ff; }
.day-card.active-day { background: #ecf5ff; border-color: #409EFF; }
.day-card.rest-day { background: #f0f9eb; }
.day-name { font-weight: 500; color: #303133; margin-bottom: 4px; }
.day-type { font-size: 12px; color: #909399; }
.day-duration { font-size: 11px; color: #409EFF; margin-top: 4px; }

.day-detail { padding: 16px; background: #fafafa; border-radius: 8px; }
.day-detail h4 { margin: 0 0 16px 0; color: #303133; }

.exercise-item {
  padding: 16px; background: #fff; border-radius: 8px;
  margin-bottom: 12px; border: 1px solid #ebeef5;
}
.exercise-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.exercise-name { font-weight: 500; font-size: 16px; color: #303133; }
.exercise-info { display: flex; gap: 20px; margin-bottom: 12px; color: #606266; font-size: 14px; }
.exercise-info span { display: flex; align-items: center; gap: 4px; }
.exercise-steps { margin-bottom: 8px; }
.exercise-steps p { margin: 4px 0; color: #606266; font-size: 13px; }
.rest-content { padding: 20px 0; }
</style>
