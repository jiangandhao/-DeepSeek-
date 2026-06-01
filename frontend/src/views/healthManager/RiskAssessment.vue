<template>
  <div class="risk-assessment">
    <el-card class="header-card">
      <div class="page-header">
        <h2>健康风险评估</h2>
        <p>综合分析您的健康数据，评估疾病风险等级</p>
      </div>
    </el-card>

    <!-- 风险评估表单 -->
    <el-card class="assess-card">
      <template #header>
        <div class="card-header">
          <span>健康信息录入</span>
          <el-tag type="info">请如实填写</el-tag>
        </div>
      </template>

      <el-form :model="healthForm" label-width="120px" ref="formRef">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="healthForm.age" :min="1" :max="120" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="healthForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)">
              <el-input-number v-model="healthForm.height" :min="50" :max="250" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="healthForm.weight" :min="20" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">生活习惯</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="吸烟状况">
              <el-select v-model="healthForm.smoking" style="width: 100%">
                <el-option label="从不吸烟" value="never" />
                <el-option label="已戒烟" value="quit" />
                <el-option label="偶尔吸烟" value="occasional" />
                <el-option label="经常吸烟" value="frequent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="饮酒状况">
              <el-select v-model="healthForm.drinking" style="width: 100%">
                <el-option label="从不饮酒" value="never" />
                <el-option label="偶尔饮酒" value="occasional" />
                <el-option label="经常饮酒" value="frequent" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="运动频率">
              <el-select v-model="healthForm.exercise" style="width: 100%">
                <el-option label="几乎不运动" value="none" />
                <el-option label="每周1-2次" value="low" />
                <el-option label="每周3-5次" value="medium" />
                <el-option label="每天运动" value="high" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="睡眠质量">
              <el-select v-model="healthForm.sleep" style="width: 100%">
                <el-option label="很差" value="poor" />
                <el-option label="一般" value="fair" />
                <el-option label="良好" value="good" />
                <el-option label="优秀" value="excellent" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">家族病史</el-divider>
        <el-form-item label="家族病史">
          <el-checkbox-group v-model="healthForm.familyHistory">
            <el-checkbox value="diabetes">糖尿病</el-checkbox>
            <el-checkbox value="hypertension">高血压</el-checkbox>
            <el-checkbox value="heart_disease">心脏病</el-checkbox>
            <el-checkbox value="cancer">癌症</el-checkbox>
            <el-checkbox value="stroke">脑卒中</el-checkbox>
            <el-checkbox value="obesity">肥胖症</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitAssessment" :loading="loading">
            开始评估
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评估结果 -->
    <el-card v-if="assessmentResult" class="result-card">
      <template #header>
        <div class="card-header">
          <span>评估结果</span>
          <el-tag :type="getRiskTagType(assessmentResult.riskLevel)">
            {{ assessmentResult.riskLevel }}
          </el-tag>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <div class="risk-score-box">
            <el-progress
              type="dashboard"
              :percentage="assessmentResult.score"
              :color="getRiskColor(assessmentResult.score)"
              :width="180"
            />
            <div class="score-label">综合健康评分</div>
          </div>
        </el-col>
        <el-col :span="16">
          <div class="risk-details">
            <h4>风险分析详情</h4>
            <div v-for="(item, index) in assessmentResult.riskItems" :key="index" class="risk-item">
              <div class="risk-item-header">
                <span class="risk-name">{{ item.name }}</span>
                <el-tag size="small" :type="getRiskTagType(item.level)">{{ item.level }}</el-tag>
              </div>
              <el-progress
                :percentage="item.score"
                :color="getRiskColor(item.score)"
                :show-text="false"
                style="margin: 8px 0"
              />
              <p class="risk-desc">{{ item.description }}</p>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <div class="recommendations">
        <h4>AI健康建议</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(rec, index) in assessmentResult.recommendations"
            :key="index"
            :type="rec.urgent ? 'danger' : 'primary'"
            :timestamp="rec.category"
            placement="top"
          >
            {{ rec.content }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formRef = ref(null)
const loading = ref(false)
const assessmentResult = ref(null)

const healthForm = reactive({
  age: 35,
  gender: 1,
  height: 170,
  weight: 70,
  smoking: 'never',
  drinking: 'occasional',
  exercise: 'medium',
  sleep: 'good',
  familyHistory: []
})

const submitAssessment = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/health-manager/risk-assessment', healthForm)
    assessmentResult.value = res.data
    ElMessage.success('评估完成')
  } catch (error) {
    ElMessage.error('评估失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  assessmentResult.value = null
}

const getRiskTagType = (level) => {
  const map = { '低风险': 'success', '中风险': 'warning', '高风险': 'danger' }
  return map[level] || 'info'
}

const getRiskColor = (score) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped>
.risk-assessment {
  padding: 20px;
}
.header-card {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}
.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.assess-card, .result-card {
  margin-bottom: 20px;
}
.risk-score-box {
  text-align: center;
  padding: 20px;
}
.score-label {
  margin-top: 16px;
  font-size: 16px;
  color: #606266;
}
.risk-details h4 {
  margin: 0 0 16px 0;
  color: #303133;
}
.risk-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}
.risk-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.risk-name {
  font-weight: 500;
  color: #303133;
}
.risk-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.recommendations h4 {
  margin: 0 0 16px 0;
  color: #303133;
}
</style>
