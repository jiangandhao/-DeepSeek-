<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card>
        <template #header><span>📋 健康档案</span></template>
        <el-form :model="profile" label-width="90px">
          <el-form-item label="身高(cm)">
            <el-input-number v-model="profile.heightCm" :min="50" :max="250" :precision="1" />
          </el-form-item>
          <el-form-item label="体重(kg)">
            <el-input-number v-model="profile.weightKg" :min="20" :max="300" :precision="1" />
          </el-form-item>
          <el-form-item label="糖尿病类型">
            <el-select v-model="profile.diabetesType" style="width: 160px">
              <el-option :value="0" label="无" />
              <el-option :value="1" label="1型" />
              <el-option :value="2" label="2型" />
              <el-option :value="3" label="妊娠" />
            </el-select>
          </el-form-item>
          <el-form-item label="家族病史">
            <el-input v-model="profile.familyHistory" placeholder="如:父亲2型糖尿病" />
          </el-form-item>
          <el-form-item label="基础疾病">
            <el-input v-model="profile.chronic" placeholder="如:高血压、高血脂" />
          </el-form-item>
          <el-button type="primary" @click="onSave">保存档案</el-button>
        </el-form>
      </el-card>

      <el-card style="margin-top: 16px">
        <template #header>
          <div class="hd"><span>🎯 疾病风险评估</span>
            <el-button size="small" type="warning" :loading="riskLoading" @click="onAssess">评估风险</el-button>
          </div>
        </template>
        <div v-if="risk" class="risk-box">
          <el-progress type="dashboard" :percentage="riskPercent" :color="riskColor">
            <template #default>
              <span class="risk-level" :style="{ color: riskColor }">{{ levelCn(risk.level) }}风险</span>
              <div class="risk-score">评分 {{ risk.score }}</div>
            </template>
          </el-progress>
          <div class="metrics" v-if="risk.metrics">
            <el-tag v-if="risk.metrics.mean" type="info">均值 {{ risk.metrics.mean }}</el-tag>
            <el-tag v-if="risk.metrics.est_hba1c" type="info">估算HbA1c {{ risk.metrics.est_hba1c }}%</el-tag>
            <el-tag v-if="risk.metrics.bmi" type="info">BMI {{ risk.metrics.bmi }}</el-tag>
          </div>
          <ul class="factors">
            <li v-for="(f, i) in risk.factors" :key="i">{{ f }}</li>
          </ul>
        </div>
        <el-empty v-else description="点击『评估风险』生成风险分级" :image-size="60" />
      </el-card>
    </el-col>

    <el-col :span="14">
      <el-card class="plan-card">
        <template #header>
          <div class="hd"><span>🩺 AI 数智健管师方案</span>
            <el-button size="small" type="primary" :loading="planLoading" @click="onPlan">生成健康方案</el-button>
          </div>
        </template>
        <div v-if="planContent" class="plan-content" v-html="renderMd(planContent)"></div>
        <el-empty v-else description="生成包含风险解读 + 饮食处方 + 运动计划的个性化方案" />
      </el-card>

      <el-card style="margin-top: 16px">
        <template #header>
          <div class="hd"><span>⚠️ 风险预警记录</span>
            <el-button size="small" @click="loadAlerts">刷新</el-button>
          </div>
        </template>
        <el-table :data="alerts" size="small" max-height="240">
          <el-table-column prop="createdAt" label="时间" width="160" :formatter="(r) => r.createdAt?.replace('T',' ')" />
          <el-table-column prop="level" label="等级" width="80">
            <template #default="{ row }">
              <el-tag :type="row.level === 'HIGH' ? 'danger' : 'warning'" size="small">{{ levelCn(row.level) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="内容" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-button v-if="!row.isRead" text size="small" @click="onRead(row.id)">标记已读</el-button>
              <span v-else class="muted">已读</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { getProfile, saveProfile, assessRisk, healthPlan, listAlerts, readAlert } from '../api'

const profile = ref({ heightCm: 170, weightKg: 65, diabetesType: 0, familyHistory: '', chronic: '' })
const risk = ref(null)
const alerts = ref([])
const planContent = ref('')
const riskLoading = ref(false)
const planLoading = ref(false)

const renderMd = (t) => marked.parse(t || '')
const levelCn = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l)
const riskPercent = computed(() => (risk.value ? Math.min(risk.value.score * 8, 100) : 0))
const riskColor = computed(() => {
  const l = risk.value?.level
  return l === 'HIGH' ? '#f56c6c' : l === 'MEDIUM' ? '#e6a23c' : '#67c23a'
})

async function loadProfile() {
  const p = await getProfile()
  if (p) profile.value = { ...profile.value, ...p }
}
async function onSave() {
  await saveProfile(profile.value)
  ElMessage.success('档案已保存')
}
async function onAssess() {
  riskLoading.value = true
  try {
    risk.value = await assessRisk()
    loadAlerts()
  } finally { riskLoading.value = false }
}
async function onPlan() {
  planLoading.value = true
  planContent.value = ''
  try {
    const res = await healthPlan({})
    planContent.value = res.content
  } catch (e) {
    ElMessage.error('生成失败,请检查 DeepSeek API Key')
  } finally { planLoading.value = false }
}
async function loadAlerts() { alerts.value = await listAlerts() }
async function onRead(id) { await readAlert(id); loadAlerts() }

onMounted(() => { loadProfile(); loadAlerts() })
</script>

<style scoped>
.hd { display: flex; justify-content: space-between; align-items: center; }
.risk-box { text-align: center; }
.risk-level { font-size: 18px; font-weight: bold; }
.risk-score { font-size: 12px; color: #909399; margin-top: 4px; }
.metrics { margin: 12px 0; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.factors { text-align: left; color: #606266; font-size: 13px; padding-left: 20px; }
.plan-content { max-height: 520px; overflow-y: auto; line-height: 1.7; }
.plan-content :deep(h1), .plan-content :deep(h2), .plan-content :deep(h3) { font-size: 16px; margin: 10px 0 6px; }
.muted { color: #c0c4cc; font-size: 12px; }
</style>
