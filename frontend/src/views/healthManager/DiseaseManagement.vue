<template>
  <div class="disease-management">
    <el-card class="header-card">
      <div class="page-header">
        <h2>疾病管理</h2>
        <p>跟踪管理慢性疾病，获取专业的疾病管理建议</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 疾病列表 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的疾病档案</span>
              <el-button type="primary" size="small" @click="showAddDialog = true">添加</el-button>
            </div>
          </template>

          <div v-for="(disease, index) in diseases" :key="index"
               class="disease-item"
               :class="{ 'active': selectedDisease?.id === disease.id }"
               @click="selectDisease(disease)">
            <div class="disease-icon" :style="{ background: disease.color }">
              {{ disease.name.charAt(0) }}
            </div>
            <div class="disease-info">
              <div class="disease-name">{{ disease.name }}</div>
              <div class="disease-time">确诊时间: {{ disease.diagnoseDate }}</div>
            </div>
            <el-tag :type="disease.controlLevel === '控制良好' ? 'success' : 'warning'" size="small">
              {{ disease.controlLevel }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 疾病详情 -->
      <el-col :span="16">
        <el-card v-if="selectedDisease">
          <template #header>
            <div class="card-header">
              <span>{{ selectedDisease.name }} - 管理方案</span>
              <el-button @click="refreshPlan">
                <el-icon><Refresh /></el-icon>
                刷新方案
              </el-button>
            </div>
          </template>

          <!-- 疾病基本信息 -->
          <el-descriptions :column="2" border style="margin-bottom: 20px">
            <el-descriptions-item label="确诊时间">{{ selectedDisease.diagnoseDate }}</el-descriptions-item>
            <el-descriptions-item label="当前控制">
              <el-tag :type="selectedDisease.controlLevel === '控制良好' ? 'success' : 'warning'">
                {{ selectedDisease.controlLevel }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最近复查">{{ selectedDisease.lastCheckup }}</el-descriptions-item>
            <el-descriptions-item label="下次复查">{{ selectedDisease.nextCheckup }}</el-descriptions-item>
          </el-descriptions>

          <el-tabs v-model="activeTab">
            <!-- 用药管理 -->
            <el-tab-pane label="用药管理" name="medication">
              <div v-for="(med, idx) in selectedDisease.medications" :key="idx" class="med-item">
                <div class="med-header">
                  <span class="med-name">{{ med.name }}</span>
                  <el-tag size="small">{{ med.frequency }}</el-tag>
                </div>
                <div class="med-info">
                  <span>剂量: {{ med.dosage }}</span>
                  <span>时间: {{ med.time }}</span>
                  <span>注意事项: {{ med.note }}</span>
                </div>
                <el-divider v-if="idx < selectedDisease.medications.length - 1" />
              </div>
              <el-button type="primary" plain style="width: 100%; margin-top: 12px">
                <el-icon><Plus /></el-icon> 添加用药记录
              </el-button>
            </el-tab-pane>

            <!-- 监测指标 -->
            <el-tab-pane label="监测指标" name="monitor">
              <div ref="monitorChart" style="height: 300px"></div>
              <el-table :data="selectedDisease.monitorRecords" style="margin-top: 16px">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="indicator" label="指标" />
                <el-table-column prop="value" label="数值" />
                <el-table-column prop="unit" label="单位" width="80" />
                <el-table-column prop="status" label="状态">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '正常' ? 'success' : row.status === '偏高' ? 'warning' : 'danger'" size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 饮食指导 -->
            <el-tab-pane label="饮食指导" name="diet">
              <el-alert title="饮食建议" type="info" :closable="false" show-icon style="margin-bottom: 16px">
                以下是根据您的疾病情况生成的个性化饮食建议
              </el-alert>
              <div v-for="(tip, idx) in selectedDisease.dietTips" :key="idx" class="diet-tip-item">
                <el-icon :style="{ color: tip.type === '推荐' ? '#67C23A' : '#F56C6C' }">
                  <CircleCheckFilled v-if="tip.type === '推荐'" />
                  <CircleCloseFilled v-else />
                </el-icon>
                <span>{{ tip.content }}</span>
              </div>
            </el-tab-pane>

            <!-- 运动建议 -->
            <el-tab-pane label="运动建议" name="exercise">
              <div v-for="(tip, idx) in selectedDisease.exerciseTips" :key="idx" class="exercise-tip-item">
                <div class="tip-title">{{ tip.title }}</div>
                <div class="tip-content">{{ tip.content }}</div>
                <el-tag size="small" :type="tip.safe ? 'success' : 'danger'">
                  {{ tip.safe ? '推荐' : '避免' }}
                </el-tag>
              </div>
            </el-tab-pane>

            <!-- AI咨询 -->
            <el-tab-pane label="AI咨询" name="consult">
              <div class="consult-area">
                <div class="chat-messages">
                  <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['message', msg.role]">
                    <div class="message-avatar">
                      <el-avatar v-if="msg.role === 'user'" :size="36">我</el-avatar>
                      <el-avatar v-else :size="36" style="background: #409EFF">AI</el-avatar>
                    </div>
                    <div class="message-content">{{ msg.content }}</div>
                  </div>
                </div>
                <div class="chat-input">
                  <el-input v-model="chatInput" placeholder="输入您的问题..." @keyup.enter="sendMessage">
                    <template #append>
                      <el-button @click="sendMessage">
                        <el-icon><Promotion /></el-icon>
                      </el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-empty v-else description="请从左侧选择一个疾病查看详情" />
      </el-col>
    </el-row>

    <!-- 添加疾病对话框 -->
    <el-dialog v-model="showAddDialog" title="添加疾病记录" width="500px">
      <el-form :model="newDisease" label-width="100px">
        <el-form-item label="疾病名称">
          <el-input v-model="newDisease.name" placeholder="如：2型糖尿病" />
        </el-form-item>
        <el-form-item label="确诊时间">
          <el-date-picker v-model="newDisease.diagnoseDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-select v-model="newDisease.controlLevel" style="width: 100%">
            <el-option label="控制良好" value="控制良好" />
            <el-option label="需关注" value="需关注" />
            <el-option label="控制不佳" value="控制不佳" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addDisease">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Plus, Promotion, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import axios from 'axios'

const selectedDisease = ref(null)
const activeTab = ref('medication')
const showAddDialog = ref(false)
const chatInput = ref('')
const chatMessages = ref([
  { role: 'assistant', content: '您好！我是您的AI健康顾问。关于您的疾病管理，有什么我可以帮助的吗？' }
])

const newDisease = reactive({
  name: '',
  diagnoseDate: '',
  controlLevel: '控制良好'
})

const diseases = ref([
  {
    id: 1, name: '2型糖尿病', diagnoseDate: '2022-05-15', controlLevel: '控制良好',
    lastCheckup: '2024-01-10', nextCheckup: '2024-04-10', color: '#409EFF',
    medications: [
      { name: '二甲双胍', dosage: '500mg', frequency: '每日两次', time: '早晚餐后', note: '随餐服用' },
      { name: '格列美脲', dosage: '2mg', frequency: '每日一次', time: '早餐前', note: '注意低血糖' }
    ],
    monitorRecords: [
      { date: '2024-01-10', indicator: '空腹血糖', value: '6.2', unit: 'mmol/L', status: '正常' },
      { date: '2024-01-10', indicator: '餐后2h血糖', value: '8.5', unit: 'mmol/L', status: '正常' },
      { date: '2024-01-10', indicator: '糖化血红蛋白', value: '6.8', unit: '%', status: '正常' },
      { date: '2024-01-05', indicator: '空腹血糖', value: '7.1', unit: 'mmol/L', status: '偏高' }
    ],
    dietTips: [
      { type: '推荐', content: '多吃粗粮、杂粮，如燕麦、糙米等' },
      { type: '推荐', content: '多吃绿叶蔬菜，每天至少500g' },
      { type: '推荐', content: '适量摄入优质蛋白，如鱼、鸡胸肉' },
      { type: '避免', content: '避免含糖饮料和甜食' },
      { type: '避免', content: '避免精制碳水化合物，如白米饭、白面包' },
      { type: '避免', content: '避免油炸食品和高脂肪食物' }
    ],
    exerciseTips: [
      { title: '快走', content: '每次30分钟，每周5次', safe: true },
      { title: '游泳', content: '每次45分钟，每周3次', safe: true },
      { title: '骑自行车', content: '每次30分钟，每周3次', safe: true },
      { title: '高强度无氧运动', content: '可能导致血糖波动', safe: false }
    ]
  },
  {
    id: 2, name: '高血压', diagnoseDate: '2023-08-20', controlLevel: '需关注',
    lastCheckup: '2024-01-05', nextCheckup: '2024-03-05', color: '#E6A23C',
    medications: [
      { name: '氨氯地平', dosage: '5mg', frequency: '每日一次', time: '早晨', note: '不可突然停药' }
    ],
    monitorRecords: [
      { date: '2024-01-05', indicator: '收缩压', value: '142', unit: 'mmHg', status: '偏高' },
      { date: '2024-01-05', indicator: '舒张压', value: '92', unit: 'mmHg', status: '偏高' },
      { date: '2024-01-01', indicator: '收缩压', value: '138', unit: 'mmHg', status: '正常' },
      { date: '2024-01-01', indicator: '舒张压', value: '88', unit: 'mmHg', status: '正常' }
    ],
    dietTips: [
      { type: '推荐', content: '低盐饮食，每天食盐摄入不超过5g' },
      { type: '推荐', content: '多吃富含钾的食物，如香蕉、土豆' },
      { type: '避免', content: '避免腌制食品和加工食品' },
      { type: '避免', content: '避免饮酒' }
    ],
    exerciseTips: [
      { title: '散步', content: '每次30分钟，每天', safe: true },
      { title: '太极拳', content: '每次40分钟，每周3次', safe: true },
      { title: '举重', content: '可能导致血压升高', safe: false }
    ]
  }
])

const selectDisease = (disease) => {
  selectedDisease.value = disease
  activeTab.value = 'medication'
}

const refreshPlan = () => {
  ElMessage.success('已刷新管理方案')
}

const addDisease = () => {
  if (!newDisease.name) {
    ElMessage.warning('请输入疾病名称')
    return
  }
  diseases.value.push({
    id: Date.now(),
    name: newDisease.name,
    diagnoseDate: newDisease.diagnoseDate,
    controlLevel: newDisease.controlLevel,
    lastCheckup: '-',
    nextCheckup: '-',
    color: '#909399',
    medications: [],
    monitorRecords: [],
    dietTips: [],
    exerciseTips: []
  })
  showAddDialog.value = false
  ElMessage.success('添加成功')
}

const sendMessage = async () => {
  if (!chatInput.value.trim()) return
  const userMsg = chatInput.value
  chatMessages.value.push({ role: 'user', content: userMsg })
  chatInput.value = ''

  try {
    const res = await axios.post('/api/health-manager/consult', {
      disease: selectedDisease.value.name,
      question: userMsg
    })
    chatMessages.value.push({ role: 'assistant', content: res.data.answer })
  } catch {
    chatMessages.value.push({
      role: 'assistant',
      content: '根据您的情况，建议您定期监测相关指标，按时服药，保持良好的生活习惯。如有不适请及时就医。'
    })
  }
}
</script>

<style scoped>
.disease-management { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.disease-item {
  display: flex; align-items: center; padding: 12px;
  border-radius: 8px; cursor: pointer; transition: all 0.3s;
  margin-bottom: 8px; border: 2px solid transparent;
}
.disease-item:hover { background: #f5f7fa; }
.disease-item.active { background: #ecf5ff; border-color: #409EFF; }
.disease-icon {
  width: 48px; height: 48px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px; font-weight: bold; margin-right: 12px;
}
.disease-info { flex: 1; }
.disease-name { font-weight: 500; color: #303133; }
.disease-time { font-size: 12px; color: #909399; margin-top: 4px; }

.med-item { padding: 12px; background: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.med-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.med-name { font-weight: 500; color: #303133; }
.med-info { display: flex; gap: 16px; color: #606266; font-size: 13px; }

.diet-tip-item, .exercise-tip-item {
  display: flex; align-items: center; gap: 8px;
  padding: 12px; background: #f5f7fa; border-radius: 8px; margin-bottom: 8px;
}
.exercise-tip-item { justify-content: space-between; }
.tip-title { font-weight: 500; color: #303133; }
.tip-content { flex: 1; color: #606266; }

.consult-area { display: flex; flex-direction: column; height: 400px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.message { display: flex; gap: 12px; margin-bottom: 16px; }
.message.user { flex-direction: row-reverse; }
.message-content {
  max-width: 70%; padding: 12px; border-radius: 12px;
  background: #f0f2f5; color: #303133; line-height: 1.6;
}
.message.user .message-content { background: #409EFF; color: #fff; }
.chat-input { padding: 16px 0; }
</style>
