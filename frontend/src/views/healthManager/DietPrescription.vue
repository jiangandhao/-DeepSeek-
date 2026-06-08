<template>
  <div class="diet-prescription">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个性化饮食处方</span>
              <el-button type="primary" @click="generateDiet" :loading="loading">
                <el-icon><MagicStick /></el-icon>
                AI生成饮食方案
              </el-button>
            </div>
          </template>

          <el-form :model="dietForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="dietForm.height" :min="100" :max="250" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="dietForm.weight" :min="30" :max="200" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="目标体重">
                  <el-input-number v-model="dietForm.targetWeight" :min="30" :max="200" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="健康目标">
                  <el-select v-model="dietForm.goal">
                    <el-option label="减重" value="lose" />
                    <el-option label="增重" value="gain" />
                    <el-option label="维持" value="maintain" />
                    <el-option label="控制血糖" value="bloodSugar" />
                    <el-option label="控制血压" value="bloodPressure" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="饮食偏好">
                  <el-select v-model="dietForm.preference">
                    <el-option label="普通饮食" value="normal" />
                    <el-option label="素食" value="vegetarian" />
                    <el-option label="低碳水" value="lowCarb" />
                    <el-option label="地中海饮食" value="mediterranean" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="过敏食物">
                  <el-select v-model="dietForm.allergies" multiple>
                    <el-option label="牛奶" value="milk" />
                    <el-option label="鸡蛋" value="egg" />
                    <el-option label="海鲜" value="seafood" />
                    <el-option label="花生" value="peanut" />
                    <el-option label="大豆" value="soy" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card v-if="dietPlan" style="margin-top: 20px;">
          <template #header>
            <span>今日饮食方案</span>
          </template>

          <el-collapse v-model="activeMeal">
            <el-collapse-item title="🌅 早餐 (建议 7:00-8:00)" name="breakfast">
              <div class="meal-content">
                <div v-for="item in dietPlan.breakfast" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>早餐总热量: {{ dietPlan.breakfastCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="☀️ 午餐 (建议 12:00-13:00)" name="lunch">
              <div class="meal-content">
                <div v-for="item in dietPlan.lunch" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>午餐总热量: {{ dietPlan.lunchCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🌙 晚餐 (建议 18:00-19:00)" name="dinner">
              <div class="meal-content">
                <div v-for="item in dietPlan.dinner" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>晚餐总热量: {{ dietPlan.dinnerCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🍎 加餐建议" name="snack">
              <div class="meal-content">
                <div v-for="item in dietPlan.snacks" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.time }} | {{ item.calories }}千卡</p>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>

          <el-divider />

          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="每日总热量" :value="dietPlan.totalCalories" suffix="千卡" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="蛋白质" :value="dietPlan.protein" suffix="g" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="碳水化合物" :value="dietPlan.carbs" suffix="g" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="脂肪" :value="dietPlan.fat" suffix="g" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>营养摄入分析</span>
          </template>
          <div ref="nutritionChart" style="height: 300px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>饮食注意事项</span>
          </template>
          <ul class="tips-list">
            <li v-for="(tip, index) in dietTips" :key="index">
              <el-icon><InfoFilled /></el-icon>
              {{ tip }}
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import { MagicStick, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getDietPrescription } from '@/api/healthManager'
import * as echarts from 'echarts'

const loading = ref(false)
const activeMeal = ref(['breakfast'])
const dietPlan = ref(null)
const nutritionChart = ref(null)
let chartInstance = null

const dietForm = ref({
  height: 170,
  weight: 65,
  targetWeight: 60,
  goal: 'maintain',
  preference: 'normal',
  allergies: []
})

const dietTips = ref([
  '每日饮水量建议1500-2000ml',
  '细嚼慢咽，每餐20-30分钟',
  '避免暴饮暴食，定时定量',
  '减少油炸、烧烤类食物摄入',
  '多吃蔬菜水果，保证膳食纤维摄入'
])

const initNutritionChart = () => {
  if (nutritionChart.value && dietPlan.value) {
    if (chartInstance) {
      chartInstance.dispose()
    }
    chartInstance = echarts.init(nutritionChart.value)
    const option = {
      tooltip: { trigger: 'item' },
      legend: { bottom: '5%', selectedMode: false },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: [
          { value: dietPlan.value.protein || 0, name: '蛋白质', itemStyle: { color: '#1a6b5a' } },
          { value: dietPlan.value.carbs || 0, name: '碳水化合物', itemStyle: { color: '#c4956a' } },
          { value: dietPlan.value.fat || 0, name: '脂肪', itemStyle: { color: '#5a8fbf' } }
        ]
      }]
    }
    chartInstance.setOption(option)
    window.addEventListener('resize', () => chartInstance && chartInstance.resize())
  }
}

const generateDiet = async () => {
  loading.value = true
  try {
    const res = await getDietPrescription(dietForm.value)
    dietPlan.value = res.data
    ElMessage.success('饮食方案生成成功')
    nextTick(() => {
      initNutritionChart()
    })
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.diet-prescription {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meal-content {
  padding: 10px;
}
.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.food-info h4 {
  margin: 0 0 5px 0;
}
.food-info p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.meal-total {
  text-align: right;
  padding: 10px 0;
  font-weight: bold;
  color: #409EFF;
}
.tips-list {
  list-style: none;
  padding: 0;
}
.tips-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
</style>
