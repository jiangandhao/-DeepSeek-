<template>
  <div class="risk-assessment">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="assessment-form">
          <template #header>
            <div class="card-header">
              <span>健康风险评估</span>
              <el-button type="primary" @click="startAssessment" :loading="loading">
                开始评估
              </el-button>
            </div>
          </template>

          <el-form :model="assessmentForm" label-width="120px" ref="formRef">
            <el-divider content-position="left">基本信息</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="年龄" prop="age">
                  <el-input-number v-model="assessmentForm.age" :min="1" :max="120" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="assessmentForm.gender">
                    <el-radio label="male">男</el-radio>
                    <el-radio label="female">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">生活习惯</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="吸烟" prop="smoking">
                  <el-select v-model="assessmentForm.smoking">
                    <el-option label="从不" value="never" />
                    <el-option label="偶尔" value="occasional" />
                    <el-option label="经常" value="regular" />
                    <el-option label="已戒烟" value="quit" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="饮酒" prop="drinking">
                  <el-select v-model="assessmentForm.drinking">
                    <el-option label="从不" value="never" />
                    <el-option label="偶尔" value="occasional" />
                    <el-option label="经常" value="regular" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="运动频率" prop="exercise">
                  <el-select v-model="assessmentForm.exercise">
                    <el-option label="几乎不运动" value="none" />
                    <el-option label="每周1-2次" value="low" />
                    <el-option label="每周3-5次" value="medium" />
                    <el-option label="每天运动" value="high" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="睡眠质量" prop="sleep">
                  <el-select v-model="assessmentForm.sleep">
                    <el-option label="很差" value="poor" />
                    <el-option label="一般" value="fair" />
                    <el-option label="良好" value="good" />
                    <el-option label="很好" value="excellent" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">家族病史</el-divider>
            <el-form-item label="家族病史" prop="familyHistory">
              <el-checkbox-group v-model="assessmentForm.familyHistory">
                <el-checkbox label="hypertension">高血压</el-checkbox>
                <el-checkbox label="diabetes">糖尿病</el-checkbox>
                <el-checkbox label="heartDisease">心脏病</el-checkbox>
                <el-checkbox label="cancer">癌症</el-checkbox>
                <el-checkbox label="stroke">中风</el-checkbox>
                <el-checkbox label="none">无</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-divider content-position="left">体检指标</el-divider>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="assessmentForm.height" :min="100" :max="250" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="assessmentForm.weight" :min="30" :max="200" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="BMI">
                  <el-input :value="bmi" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="收缩压(mmHg)">
                  <el-input-number v-model="assessmentForm.systolicBP" :min="60" :max="250" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="舒张压(mmHg)">
                  <el-input-number v-model="assessmentForm.diastolicBP" :min="40" :max="150" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="血糖(mmol/L)">
                  <el-input-number v-model="assessmentForm.bloodSugar" :min="2" :max="30" :step="0.1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="总胆固醇">
                  <el-input-number v-model="assessmentForm.cholesterol" :min="2" :max="15" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="甘油三酯">
                  <el-input-number v-model="assessmentForm.triglyceride" :min="0.5" :max="10" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="尿酸(μmol/L)">
                  <el-input-number v-model="assessmentForm.uricAcid" :min="100" :max="800" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="result-card">
          <template #header>
            <span>评估结果</span>
          </template>

          <div v-if="result" class="result-content">
            <div class="risk-level" :class="result.level">
              <el-icon :size="48"><Warning /></el-icon>
              <h2>{{ result.levelText }}</h2>
              <p>综合风险评分: {{ result.score }}分</p>
            </div>

            <el-divider />

            <h4>疾病风险分析</h4>
            <div v-for="item in result.risks" :key="item.disease" class="risk-item">
              <div class="risk-label">
                <span>{{ item.disease }}</span>
                <el-tag :type="getRiskType(item.level)" size="small">{{ item.level }}</el-tag>
              </div>
              <el-progress :percentage="item.probability" :color="getRiskColor(item.level)" />
            </div>

            <el-divider />

            <h4>预防建议</h4>
            <ul class="suggestions">
              <li v-for="(suggestion, index) in result.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>

          <el-empty v-else description="请填写信息后开始评估" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHealthRiskAssessment } from '../../api/healthManager'

const loading = ref(false)
const result = ref(null)
const userId = ref(1) // 从登录状态获取

const assessmentForm = ref({
  age: 30,
  gender: 'male',
  smoking: 'never',
  drinking: 'never',
  exercise: 'medium',
  sleep: 'good',
  familyHistory: [],
  height: 170,
  weight: 65,
  systolicBP: 120,
  diastolicBP: 80,
  bloodSugar: 5.6,
  cholesterol: 4.5,
  triglyceride: 1.5,
  uricAcid: 350
})

const bmi = computed(() => {
  const h = assessmentForm.value.height / 100
  return (assessmentForm.value.weight / (h * h)).toFixed(1)
})

const getRiskType = (level) => {
  const map = { 'low': 'success', 'medium': 'warning', 'high': 'danger' }
  return map[level] || 'info'
}

const getRiskColor = (level) => {
  const map = { 'low': '#67C23A', 'medium': '#E6A23C', 'high': '#F56C6C' }
  return map[level] || '#909399'
}

const startAssessment = async () => {
  loading.value = true
  try {
    const data = {
      userId: userId.value,
      ...assessmentForm.value,
      bmi: parseFloat(bmi.value)
    }

    const res = await getHealthRiskAssessment(data)
    result.value = res.data

    ElMessage.success('评估完成')
  } catch (error) {
    ElMessage.error('评估失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.risk-assessment {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.risk-level {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
}
.risk-level.low { background: #f0f9eb; color: #67C23A; }
.risk-level.medium { background: #fdf6ec; color: #E6A23C; }
.risk-level.high { background: #fef0f0; color: #F56C6C; }
.risk-item {
  margin-bottom: 15px;
}
.risk-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.suggestions {
  padding-left: 20px;
}
.suggestions li {
  margin-bottom: 8px;
  line-height: 1.6;
}
</style>
