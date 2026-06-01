<template>
  <div class="prevention-plan">
    <el-card class="header-card">
      <div class="page-header">
        <h2>个性化预防方案</h2>
        <p>根据您的健康风险评估，AI为您定制专属预防方案</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 风险选择 -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>风险项目</span>
          </template>
          <div v-for="(disease, index) in diseaseList" :key="index"
               class="disease-item"
               :class="{ 'active': selectedDisease?.id === disease.id }"
               @click="selectDisease(disease)">
            <div class="disease-icon" :style="{ background: disease.color }">
              {{ disease.name.charAt(0) }}
            </div>
            <div class="disease-info">
              <div class="disease-name">{{ disease.name }}</div>
              <div class="disease-risk">
                <el-tag :type="getRiskTagType(disease.riskLevel)" size="small">
                  {{ disease.riskLevel }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 预防方案详情 -->
      <el-col :span="18">
        <el-card v-if="selectedDisease">
          <template #header>
            <div class="card-header">
              <span>{{ selectedDisease.name }} - 预防方案</span>
              <el-button @click="exportPlan">
                <el-icon><Download /></el-icon> 导出方案
              </el-button>
            </div>
          </template>

          <!-- 方案概览 -->
          <el-row :gutter="20" style="margin-bottom: 20px">
            <el-col :span="8">
              <el-statistic title="预计降低风险" :value="selectedDisease.reduction" suffix="%" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="执行周期" :value="selectedDisease.cycle" suffix="天" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="完成进度" :value="selectedDisease.progress" suffix="%" />
            </el-col>
          </el-row>

          <el-progress :percentage="selectedDisease.progress" :stroke-width="20" style="margin-bottom: 20px" />

          <el-tabs v-model="activeTab">
            <!-- 饮食方案 -->
            <el-tab-pane label="饮食方案" name="diet">
              <div class="plan-section">
                <h4><el-icon><Food /></el-icon> 推荐食物</h4>
                <div class="food-grid">
                  <div v-for="(food, idx) in selectedDisease.dietPlan.recommended" :key="idx" class="food-card good">
                    <el-icon color="#67C23A"><CircleCheckFilled /></el-icon>
                    <span>{{ food }}</span>
                  </div>
                </div>

                <h4><el-icon><WarningFilled /></el-icon> 限制食物</h4>
                <div class="food-grid">
                  <div v-for="(food, idx) in selectedDisease.dietPlan.restricted" :key="idx" class="food-card bad">
                    <el-icon color="#F56C6C"><CircleCloseFilled /></el-icon>
                    <span>{{ food }}</span>
                  </div>
                </div>

                <h4><el-icon><Document /></el-icon> 饮食建议</h4>
                <el-timeline>
                  <el-timeline-item v-for="(tip, idx) in selectedDisease.dietPlan.tips" :key="idx">
                    {{ tip }}
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-tab-pane>

            <!-- 运动方案 -->
            <el-tab-pane label="运动方案" name="exercise">
              <div class="plan-section">
                <div v-for="(plan, idx) in selectedDisease.exercisePlan" :key="idx" class="exercise-item">
                  <div class="exercise-header">
                    <span class="exercise-name">{{ plan.name }}</span>
                    <el-tag size="small">{{ plan.frequency }}</el-tag>
                  </div>
                  <div class="exercise-desc">{{ plan.description }}</div>
                  <div class="exercise-benefits">
                    <span class="benefits-label">益处:</span>
                    <el-tag v-for="benefit in plan.benefits" :key="benefit" size="small" type="success">
                      {{ benefit }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 生活方式 -->
            <el-tab-pane label="生活方式" name="lifestyle">
              <div class="plan-section">
                <div v-for="(item, idx) in selectedDisease.lifestylePlan" :key="idx" class="lifestyle-item">
                  <div class="lifestyle-icon">
                    <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
                  </div>
                  <div class="lifestyle-content">
                    <div class="lifestyle-title">{{ item.title }}</div>
                    <div class="lifestyle-desc">{{ item.description }}</div>
                    <div class="lifestyle-tips">
                      <el-tag v-for="tip in item.tips" :key="tip" size="small" type="info">{{ tip }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 定期检查 -->
            <el-tab-pane label="定期检查" name="checkup">
              <div class="plan-section">
                <el-table :data="selectedDisease.checkupPlan" style="width: 100%">
                  <el-table-column prop="item" label="检查项目" width="150" />
                  <el-table-column prop="frequency" label="检查频率" width="120" />
                  <el-table-column prop="purpose" label="检查目的" />
                  <el-table-column label="下次检查" width="120">
                    <template #default="{ row }">
                      <span :class="{ 'urgent': isUrgent(row.nextDate) }">{{ row.nextDate }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>

            <!-- 用药指导 -->
            <el-tab-pane label="用药指导" name="medication">
              <div class="plan-section">
                <el-alert
                  v-if="selectedDisease.medicationPlan.length === 0"
                  title="暂无用药指导"
                  type="info"
                  description="根据您的风险评估，目前无需特殊用药，请保持健康生活方式。"
                  :closable="false"
                  show-icon
                />
                <div v-else v-for="(med, idx) in selectedDisease.medicationPlan" :key="idx" class="med-item">
                  <div class="med-name">{{ med.name }}</div>
                  <div class="med-info">
                    <span>剂量: {{ med.dosage }}</span>
                    <span>频率: {{ med.frequency }}</span>
                    <span>注意: {{ med.note }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-empty v-else description="请从左侧选择一个风险项目查看预防方案" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Food, WarningFilled, Document, CircleCheckFilled, CircleCloseFilled, Sunrise, Moon, Coffee } from '@element-plus/icons-vue'
import axios from 'axios'

const activeTab = ref('diet')
const selectedDisease = ref(null)

const diseaseList = ref([
  {
    id: 1, name: '糖尿病', riskLevel: '中风险', color: '#E6A23C',
    reduction: 35, cycle: 90, progress: 45,
    dietPlan: {
      recommended: ['燕麦', '糙米', '苦瓜', '西兰花', '鱼肉', '豆制品', '坚果', '绿叶蔬菜'],
      restricted: ['白米饭', '白面包', '含糖饮料', '甜点', '油炸食品', '加工肉类'],
      tips: [
        '每日主食控制在250-300g，粗细搭配',
        '每餐蔬菜不少于200g，优先食用',
        '蛋白质选择优质来源，如鱼、禽、蛋、奶',
        '定时定量，避免暴饮暴食'
      ]
    },
    exercisePlan: [
      { name: '快走', frequency: '每周5次', description: '每次30-45分钟，时速5-6km', benefits: ['降低血糖', '改善胰岛素敏感性'] },
      { name: '游泳', frequency: '每周2次', description: '每次30-60分钟', benefits: ['全身运动', '保护关节'] },
      { name: '抗阻训练', frequency: '每周2次', description: '每次20-30分钟', benefits: ['增加肌肉量', '提高基础代谢'] }
    ],
    lifestylePlan: [
      { title: '规律作息', description: '保持每天7-8小时睡眠', icon: 'Moon', color: '#409EFF', tips: ['23:00前入睡', '避免熬夜', '午休20-30分钟'] },
      { title: '戒烟限酒', description: '彻底戒烟，限制酒精摄入', icon: 'CircleCloseFilled', color: '#F56C6C', tips: ['完全戒烟', '男性每日酒精<25g', '女性每日酒精<15g'] },
      { title: '压力管理', description: '学会调节压力，保持心情愉悦', icon: 'Coffee', color: '#67C23A', tips: ['冥想放松', '培养兴趣爱好', '社交活动'] }
    ],
    checkupPlan: [
      { item: '空腹血糖', frequency: '每3个月', purpose: '监测血糖控制情况', nextDate: '2024-04-15' },
      { item: '糖化血红蛋白', frequency: '每3个月', purpose: '评估长期血糖水平', nextDate: '2024-04-15' },
      { item: '眼底检查', frequency: '每年1次', purpose: '筛查糖尿病视网膜病变', nextDate: '2024-07-15' },
      { item: '肾功能', frequency: '每半年', purpose: '筛查糖尿病肾病', nextDate: '2024-07-15' }
    ],
    medicationPlan: []
  },
  {
    id: 2, name: '高血压', riskLevel: '中风险', color: '#E6A23C',
    reduction: 30, cycle: 60, progress: 60,
    dietPlan: {
      recommended: ['芹菜', '香蕉', '低脂牛奶', '全谷物', '鱼类', '豆类', '新鲜水果'],
      restricted: ['腌制食品', '加工肉类', '咸菜', '酱油', '味精', '方便面'],
      tips: [
        '每日食盐摄入不超过5g',
        '增加钾的摄入，如香蕉、土豆',
        '多吃富含钙的食物，如牛奶、豆制品'
      ]
    },
    exercisePlan: [
      { name: '散步', frequency: '每天', description: '每次30分钟', benefits: ['降低血压', '改善心血管'] },
      { name: '太极拳', frequency: '每周3次', description: '每次40分钟', benefits: ['放松身心', '调节血压'] }
    ],
    lifestylePlan: [
      { title: '低盐饮食', description: '严格控制食盐摄入', icon: 'Document', color: '#E6A23C', tips: ['使用限盐勺', '避免高盐食物', '阅读食品标签'] },
      { title: '情绪稳定', description: '避免情绪大幅波动', icon: 'Sunrise', color: '#67C23A', tips: ['避免争吵', '学会放松', '培养耐心'] }
    ],
    checkupPlan: [
      { item: '血压监测', frequency: '每天', purpose: '监测血压变化', nextDate: '每日' },
      { item: '血脂检查', frequency: '每半年', purpose: '评估心血管风险', nextDate: '2024-07-20' }
    ],
    medicationPlan: [
      { name: '氨氯地平', dosage: '5mg', frequency: '每日一次', note: '早晨服用，不可突然停药' }
    ]
  },
  {
    id: 3, name: '脂肪肝', riskLevel: '高风险', color: '#F56C6C',
    reduction: 40, cycle: 120, progress: 20,
    dietPlan: {
      recommended: ['蔬菜', '水果', '全谷物', '鱼类', '豆制品', '绿茶'],
      restricted: ['油炸食品', '肥肉', '动物内脏', '甜食', '酒精', '含糖饮料'],
      tips: [
        '严格戒酒',
        '控制总热量摄入',
        '减少饱和脂肪酸摄入',
        '增加膳食纤维摄入'
      ]
    },
    exercisePlan: [
      { name: '有氧运动', frequency: '每周5次', description: '每次45-60分钟，心率达到最大心率的60-70%', benefits: ['减少肝脏脂肪', '改善肝功能'] },
      { name: '力量训练', frequency: '每周2次', description: '每次30分钟', benefits: ['增加肌肉量', '提高代谢'] }
    ],
    lifestylePlan: [
      { title: '严格戒酒', description: '酒精是脂肪肝的重要致病因素', icon: 'CircleCloseFilled', color: '#F56C6C', tips: ['完全戒酒', '避免含酒精饮料', '社交场合以茶代酒'] },
      { title: '控制体重', description: '目标减重5-10%', icon: 'Moon', color: '#409EFF', tips: ['每周减重0.5-1kg', '避免极端节食', '持续监测体重'] }
    ],
    checkupPlan: [
      { item: '肝功能', frequency: '每3个月', purpose: '监测肝功能变化', nextDate: '2024-04-15' },
      { item: '腹部B超', frequency: '每半年', purpose: '评估脂肪肝程度', nextDate: '2024-07-15' },
      { item: '血脂检查', frequency: '每3个月', purpose: '监测血脂水平', nextDate: '2024-04-15' }
    ],
    medicationPlan: []
  }
])

const getRiskTagType = (level) => {
  const map = { '低风险': 'success', '中风险': 'warning', '高风险': 'danger' }
  return map[level] || 'info'
}

const selectDisease = (disease) => {
  selectedDisease.value = disease
  activeTab.value = 'diet'
}

const isUrgent = (dateStr) => {
  if (dateStr === '每日') return false
  const date = new Date(dateStr)
  const now = new Date()
  const diff = date.getTime() - now.getTime()
  return diff < 30 * 24 * 60 * 60 * 1000 // 30天内
}

const exportPlan = () => {
  ElMessage.success('预防方案导出成功')
}
</script>

<style scoped>
.prevention-plan { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.disease-item {
  display: flex; align-items: center; padding: 12px;
  border-radius: 8px; cursor: pointer; margin-bottom: 8px;
  border: 2px solid transparent; transition: all 0.3s;
}
.disease-item:hover { background: #f5f7fa; }
.disease-item.active { background: #ecf5ff; border-color: #409EFF; }
.disease-icon {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: bold; margin-right: 12px;
}
.disease-info { flex: 1; }
.disease-name { font-weight: 500; color: #303133; margin-bottom: 4px; }

.plan-section { padding: 16px 0; }
.plan-section h4 {
  display: flex; align-items: center; gap: 8px;
  margin: 0 0 12px 0; color: #303133;
}

.food-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  margin-bottom: 20px;
}
.food-card {
  display: flex; align-items: center; gap: 8px;
  padding: 12px; border-radius: 8px; font-size: 14px;
}
.food-card.good { background: #f0f9eb; color: #67C23A; }
.food-card.bad { background: #fef0f0; color: #F56C6C; }

.exercise-item {
  padding: 16px; background: #f5f7fa; border-radius: 8px;
  margin-bottom: 12px;
}
.exercise-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.exercise-name { font-weight: 500; color: #303133; font-size: 16px; }
.exercise-desc { color: #606266; margin-bottom: 8px; }
.exercise-benefits { display: flex; align-items: center; gap: 8px; }
.benefits-label { color: #909399; font-size: 13px; }

.lifestyle-item {
  display: flex; padding: 16px; background: #f5f7fa;
  border-radius: 8px; margin-bottom: 12px;
}
.lifestyle-icon { margin-right: 16px; }
.lifestyle-content { flex: 1; }
.lifestyle-title { font-weight: 500; color: #303133; margin-bottom: 4px; }
.lifestyle-desc { color: #606266; margin-bottom: 8px; }
.lifestyle-tips { display: flex; gap: 8px; flex-wrap: wrap; }

.med-item {
  padding: 12px; background: #f5f7fa; border-radius: 8px;
  margin-bottom: 8px;
}
.med-name { font-weight: 500; color: #303133; margin-bottom: 4px; }
.med-info { display: flex; gap: 16px; color: #606266; font-size: 13px; }

.urgent { color: #F56C6C; font-weight: 500; }
</style>
