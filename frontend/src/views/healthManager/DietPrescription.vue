<template>
  <div class="diet-prescription">
    <el-card class="header-card">
      <div class="page-header">
        <h2>AI饮食处方</h2>
        <p>基于您的健康数据，AI为您定制个性化饮食方案</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：用户健康档案 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>我的健康档案</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="身高">{{ healthProfile.height }} cm</el-descriptions-item>
            <el-descriptions-item label="体重">{{ healthProfile.weight }} kg</el-descriptions-item>
            <el-descriptions-item label="BMI">{{ bmi }}</el-descriptions-item>
            <el-descriptions-item label="BMI评级">
              <el-tag :type="bmiTagType">{{ bmiLevel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="血糖状态">
              <el-tag :type="bloodSugarTagType">{{ healthProfile.bloodSugarStatus }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="过敏食物">
              <el-tag v-for="item in healthProfile.allergies" :key="item" size="small" style="margin-right: 4px">
                {{ item }}
              </el-tag>
              <span v-if="!healthProfile.allergies.length">无</span>
            </el-descriptions-item>
            <el-descriptions-item label="饮食偏好">
              {{ healthProfile.dietPreference || '无特殊偏好' }}
            </el-descriptions-item>
          </el-descriptions>

          <el-button type="primary" style="width: 100%; margin-top: 16px" @click="generateDietPlan">
            <el-icon><MagicStick /></el-icon>
            AI生成饮食方案
          </el-button>
        </el-card>
      </el-col>

      <!-- 右侧：饮食方案 -->
      <el-col :span="16">
        <el-card v-if="dietPlan" class="diet-plan-card">
          <template #header>
            <div class="card-header">
              <span>今日饮食方案</span>
              <div>
                <el-tag type="success">总热量: {{ dietPlan.totalCalories }} kcal</el-tag>
                <el-tag type="info" style="margin-left: 8px">蛋白质: {{ dietPlan.protein }}g</el-tag>
                <el-tag type="warning" style="margin-left: 8px">碳水: {{ dietPlan.carbs }}g</el-tag>
              </div>
            </div>
          </template>

          <!-- 餐次安排 -->
          <el-tabs v-model="activeMeal">
            <el-tab-pane label="早餐" name="breakfast">
              <div class="meal-content">
                <div v-for="(food, index) in dietPlan.breakfast" :key="index" class="food-item">
                  <div class="food-info">
                    <span class="food-name">{{ food.name }}</span>
                    <span class="food-amount">{{ food.amount }}</span>
                  </div>
                  <div class="food-calories">{{ food.calories }} kcal</div>
                  <div class="food-tags">
                    <el-tag v-for="tag in food.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="午餐" name="lunch">
              <div class="meal-content">
                <div v-for="(food, index) in dietPlan.lunch" :key="index" class="food-item">
                  <div class="food-info">
                    <span class="food-name">{{ food.name }}</span>
                    <span class="food-amount">{{ food.amount }}</span>
                  </div>
                  <div class="food-calories">{{ food.calories }} kcal</div>
                  <div class="food-tags">
                    <el-tag v-for="tag in food.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="晚餐" name="dinner">
              <div class="meal-content">
                <div v-for="(food, index) in dietPlan.dinner" :key="index" class="food-item">
                  <div class="food-info">
                    <span class="food-name">{{ food.name }}</span>
                    <span class="food-amount">{{ food.amount }}</span>
                  </div>
                  <div class="food-calories">{{ food.calories }} kcal</div>
                  <div class="food-tags">
                    <el-tag v-for="tag in food.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="加餐" name="snack">
              <div class="meal-content">
                <div v-for="(food, index) in dietPlan.snack" :key="index" class="food-item">
                  <div class="food-info">
                    <span class="food-name">{{ food.name }}</span>
                    <span class="food-amount">{{ food.amount }}</span>
                  </div>
                  <div class="food-calories">{{ food.calories }} kcal</div>
                  <div class="food-tags">
                    <el-tag v-for="tag in food.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>

          <!-- 营养分布图 -->
          <el-divider>营养分布</el-divider>
          <div ref="nutritionChart" style="height: 300px"></div>
        </el-card>

        <el-empty v-else description="点击左侧按钮生成AI饮食方案" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const nutritionChart = ref(null)
const activeMeal = ref('breakfast')
const dietPlan = ref(null)

const healthProfile = reactive({
  height: 170,
  weight: 70,
  bloodSugarStatus: '正常',
  allergies: ['花生'],
  dietPreference: '清淡饮食'
})

const bmi = computed(() => {
  const height = healthProfile.height / 100
  return (healthProfile.weight / (height * height)).toFixed(1)
})

const bmiLevel = computed(() => {
  const val = parseFloat(bmi.value)
  if (val < 18.5) return '偏瘦'
  if (val < 24) return '正常'
  if (val < 28) return '偏胖'
  return '肥胖'
})

const bmiTagType = computed(() => {
  const val = parseFloat(bmi.value)
  if (val < 18.5) return 'warning'
  if (val < 24) return 'success'
  if (val < 28) return 'warning'
  return 'danger'
})

const bloodSugarTagType = computed(() => {
  return healthProfile.bloodSugarStatus === '正常' ? 'success' : 'danger'
})

const generateDietPlan = async () => {
  try {
    const res = await axios.post('/api/health-manager/diet-prescription', healthProfile)
    dietPlan.value = res.data
    ElMessage.success('饮食方案生成成功')
    // 渲染营养分布图
    setTimeout(() => initNutritionChart(), 100)
  } catch (error) {
    // 模拟数据
    dietPlan.value = {
      totalCalories: 1800,
      protein: 75,
      carbs: 220,
      fat: 55,
      breakfast: [
        { name: '全麦面包', amount: '2片(60g)', calories: 150, tags: ['低GI', '高纤维'] },
        { name: '水煮鸡蛋', amount: '1个', calories: 70, tags: ['优质蛋白'] },
        { name: '脱脂牛奶', amount: '250ml', calories: 90, tags: ['补钙'] },
        { name: '小番茄', amount: '5颗', calories: 25, tags: ['维生素C'] }
      ],
      lunch: [
        { name: '糙米饭', amount: '150g', calories: 180, tags: ['低GI', '饱腹感强'] },
        { name: '清蒸鱼', amount: '100g', calories: 120, tags: ['优质蛋白', '低脂'] },
        { name: '西兰花炒胡萝卜', amount: '200g', calories: 80, tags: ['高纤维', '维生素'] },
        { name: '紫菜蛋花汤', amount: '1碗', calories: 40, tags: ['低热量'] }
      ],
      dinner: [
        { name: '杂粮粥', amount: '200ml', calories: 100, tags: ['低GI'] },
        { name: '鸡胸肉沙拉', amount: '150g', calories: 150, tags: ['高蛋白', '低脂'] },
        { name: '清炒时蔬', amount: '150g', calories: 60, tags: ['高纤维'] }
      ],
      snack: [
        { name: '苹果', amount: '1个(中)', calories: 80, tags: ['低GI', '维生素'] },
        { name: '原味酸奶', amount: '100g', calories: 70, tags: ['益生菌', '补钙'] }
      ]
    }
    setTimeout(() => initNutritionChart(), 100)
  }
}

const initNutritionChart = () => {
  if (!nutritionChart.value) return
  const chart = echarts.init(nutritionChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      data: [
        { value: dietPlan.value.protein * 4, name: '蛋白质', itemStyle: { color: '#409EFF' } },
        { value: dietPlan.value.carbs * 4, name: '碳水化合物', itemStyle: { color: '#67C23A' } },
        { value: dietPlan.value.fat * 9, name: '脂肪', itemStyle: { color: '#E6A23C' } }
      ]
    }]
  })
}
</script>

<style scoped>
.diet-prescription { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.meal-content { padding: 8px 0; }
.food-item {
  display: flex; align-items: center; padding: 12px;
  background: #f5f7fa; border-radius: 8px; margin-bottom: 8px;
}
.food-info { flex: 1; }
.food-name { font-weight: 500; color: #303133; margin-right: 8px; }
.food-amount { color: #909399; font-size: 13px; }
.food-calories { width: 80px; text-align: right; color: #409EFF; font-weight: 500; }
.food-tags { width: 200px; text-align: right; }
.food-tags .el-tag { margin-left: 4px; }
</style>
