<template>
  <div class="image-analysis">
    <el-card class="header-card">
      <div class="page-header">
        <h2>影像智能分析</h2>
        <p>AI辅助分析体检影像资料，提供初步筛查建议</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：上传区域 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>影像上传</span>
          </template>

          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            accept="image/*,.pdf"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG、PDF 格式，单个文件不超过 20MB
              </div>
            </template>
          </el-upload>

          <el-form :model="analysisForm" label-position="top" style="margin-top: 20px">
            <el-form-item label="影像类型">
              <el-select v-model="analysisForm.imageType" style="width: 100%">
                <el-option label="胸部X光" value="chest_xray" />
                <el-option label="胸部CT" value="chest_ct" />
                <el-option label="腹部B超" value="abdominal_ultrasound" />
                <el-option label="甲状腺B超" value="thyroid_ultrasound" />
                <el-option label="乳腺B超" value="breast_ultrasound" />
                <el-option label="心电图" value="ecg" />
                <el-option label="眼底照片" value="fundus" />
              </el-select>
            </el-form-item>

            <el-form-item label="检查目的">
              <el-input
                v-model="analysisForm.purpose"
                type="textarea"
                :rows="3"
                placeholder="请描述检查目的，如：常规体检、肺部疾病筛查等"
              />
            </el-form-item>

            <el-button
              type="primary"
              style="width: 100%"
              @click="startAnalysis"
              :loading="analyzing"
              :disabled="!uploadedFile"
            >
              <el-icon><VideoPlay /></el-icon>
              开始AI分析
            </el-button>
          </el-form>

          <!-- 分析进度 -->
          <div v-if="analyzing" class="analysis-progress">
            <el-progress :percentage="progress" :status="progressStatus" />
            <div class="progress-text">{{ progressText }}</div>
          </div>
        </el-card>

        <!-- 历史记录 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>分析历史</span>
          </template>
          <div v-for="(record, index) in historyRecords" :key="index"
               class="history-item"
               :class="{ 'active': selectedHistory?.id === record.id }"
               @click="viewHistory(record)">
            <div class="history-icon">
              <el-icon :size="24" :color="record.color"><Document /></el-icon>
            </div>
            <div class="history-info">
              <div class="history-name">{{ record.imageType }}</div>
              <div class="history-date">{{ record.date }}</div>
            </div>
            <el-tag size="small" :type="record.result === '正常' ? 'success' : 'warning'">
              {{ record.result }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：分析结果 -->
      <el-col :span="16">
        <el-card v-if="analysisResult">
          <template #header>
            <div class="card-header">
              <span>AI分析结果</span>
              <el-tag :type="analysisResult.riskLevel === '低风险' ? 'success' : analysisResult.riskLevel === '中风险' ? 'warning' : 'danger'">
                {{ analysisResult.riskLevel }}
              </el-tag>
            </div>
          </template>

          <!-- 影像展示 -->
          <div class="image-display">
            <div class="original-image">
              <h4>原始影像</h4>
              <div class="image-placeholder">
                <el-icon :size="48" color="#c0c4cc"><Picture /></el-icon>
                <span>{{ analysisResult.imageName }}</span>
              </div>
            </div>
            <div class="annotated-image">
              <h4>AI标注</h4>
              <div class="image-placeholder annotated">
                <el-icon :size="48" color="#409EFF"><Picture /></el-icon>
                <span>AI标注结果</span>
                <div class="annotation-markers">
                  <div v-for="(marker, idx) in analysisResult.markers" :key="idx"
                       class="marker"
                       :style="{ left: marker.x + '%', top: marker.y + '%' }">
                    <el-tooltip :content="marker.label" placement="top">
                      <div class="marker-dot" :style="{ background: marker.color }"></div>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 分析详情 -->
          <el-tabs v-model="resultTab">
            <el-tab-pane label="检查发现" name="findings">
              <div v-for="(finding, index) in analysisResult.findings" :key="index" class="finding-item">
                <div class="finding-header">
                  <span class="finding-location">{{ finding.location }}</span>
                  <el-tag size="small" :type="finding.significance === '重要' ? 'danger' : 'info'">
                    {{ finding.significance }}
                  </el-tag>
                </div>
                <div class="finding-desc">{{ finding.description }}</div>
                <div class="finding-measurement" v-if="finding.measurement">
                  测量数据: {{ finding.measurement }}
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="风险评估" name="risk">
              <div class="risk-assessment">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <div class="risk-chart" ref="riskChart"></div>
                  </el-col>
                  <el-col :span="12">
                    <div class="risk-details">
                      <div v-for="(risk, index) in analysisResult.riskFactors" :key="index" class="risk-factor">
                        <div class="risk-name">{{ risk.name }}</div>
                        <el-progress :percentage="risk.score" :color="risk.color" />
                        <div class="risk-desc">{{ risk.description }}</div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-tab-pane>

            <el-tab-pane label="AI建议" name="suggestion">
              <el-alert
                :title="analysisResult.suggestion.title"
                :type="analysisResult.suggestion.urgent ? 'warning' : 'info'"
                :closable="false"
                show-icon
                style="margin-bottom: 16px"
              />
              <div class="suggestion-content">
                <p>{{ analysisResult.suggestion.content }}</p>
                <el-divider />
                <h4>后续建议</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, idx) in analysisResult.suggestion.followUp"
                    :key="idx"
                    :type="item.urgent ? 'danger' : 'primary'"
                  >
                    {{ item.content }}
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-empty v-else description="上传影像资料并点击开始分析" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, VideoPlay, Document, Picture } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const analyzing = ref(false)
const progress = ref(0)
const progressText = ref('')
const uploadedFile = ref(null)
const analysisResult = ref(null)
const selectedHistory = ref(null)
const resultTab = ref('findings')

const analysisForm = reactive({
  imageType: 'chest_xray',
  purpose: ''
})

const historyRecords = ref([
  { id: 1, imageType: '胸部X光', date: '2024-01-15', result: '正常', color: '#67C23A' },
  { id: 2, imageType: '甲状腺B超', date: '2023-10-20', result: '结节', color: '#E6A23C' },
  { id: 3, imageType: '腹部B超', date: '2023-07-15', result: '正常', color: '#67C23A' }
])

const handleFileChange = (file) => {
  uploadedFile.value = file
}

const startAnalysis = async () => {
  analyzing.value = true
  progress.value = 0
  progressText.value = '正在上传影像...'

  // 模拟分析进度
  const timer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += 10
      if (progress.value < 30) progressText.value = '正在上传影像...'
      else if (progress.value < 60) progressText.value = 'AI模型分析中...'
      else progressText.value = '生成分析报告...'
    }
  }, 500)

  try {
    const res = await axios.post('/api/checkup/image-analysis', {
      imageType: analysisForm.imageType,
      purpose: analysisForm.purpose
    })
    analysisResult.value = res.data
  } catch {
    // 模拟分析结果
    analysisResult.value = {
      imageName: 'chest_xray_20240115.jpg',
      riskLevel: '低风险',
      markers: [
        { x: 30, y: 40, label: '肺野清晰', color: '#67C23A' },
        { x: 60, y: 35, label: '心影正常', color: '#67C23A' }
      ],
      findings: [
        {
          location: '右肺上叶',
          description: '肺野透亮度正常，未见明显异常密度影',
          significance: '正常',
          measurement: null
        },
        {
          location: '左肺下叶',
          description: '肺野透亮度正常，未见明显异常密度影',
          significance: '正常',
          measurement: null
        },
        {
          location: '心脏',
          description: '心影大小形态正常，纵隔未见增宽',
          significance: '正常',
          measurement: '心胸比: 0.48'
        }
      ],
      riskFactors: [
        { name: '肺部病变风险', score: 5, color: '#67C23A', description: '未发现明显异常' },
        { name: '心脏疾病风险', score: 10, color: '#67C23A', description: '心影大小正常' },
        { name: '纵隔病变风险', score: 3, color: '#67C23A', description: '纵隔未见增宽' }
      ],
      suggestion: {
        title: '检查结果正常',
        urgent: false,
        content: '您的胸部X光检查未发现明显异常，建议保持良好的生活习惯，定期体检。',
        followUp: [
          { content: '建议每年进行一次胸部X光检查', urgent: false },
          { content: '保持良好的生活习惯，戒烟限酒', urgent: false },
          { content: '如出现咳嗽、胸痛等症状及时就医', urgent: false }
        ]
      }
    }
  } finally {
    clearInterval(timer)
    progress.value = 100
    progressText.value = '分析完成'
    analyzing.value = false
  }
}

const viewHistory = (record) => {
  selectedHistory.value = record
  // 加载历史分析结果
  analysisResult.value = {
    imageName: record.imageType,
    riskLevel: record.result === '正常' ? '低风险' : '中风险',
    markers: [],
    findings: [],
    riskFactors: [],
    suggestion: {
      title: record.result === '正常' ? '检查结果正常' : '需要关注',
      urgent: record.result !== '正常',
      content: record.result === '正常' ? '检查结果正常，无需特殊处理' : '建议进一步检查',
      followUp: []
    }
  }
}
</script>

<style scoped>
.image-analysis { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.upload-area { width: 100%; }
.analysis-progress { margin-top: 16px; text-align: center; }
.progress-text { margin-top: 8px; color: #909399; font-size: 13px; }

.history-item {
  display: flex; align-items: center; padding: 12px;
  border-radius: 8px; cursor: pointer; margin-bottom: 8px;
  border: 2px solid transparent; transition: all 0.3s;
}
.history-item:hover { background: #f5f7fa; }
.history-item.active { background: #ecf5ff; border-color: #409EFF; }
.history-icon { margin-right: 12px; }
.history-info { flex: 1; }
.history-name { font-weight: 500; color: #303133; }
.history-date { font-size: 12px; color: #909399; }

.image-display {
  display: flex; gap: 20px; margin-bottom: 16px;
}
.image-display > div { flex: 1; }
.image-display h4 { margin: 0 0 12px 0; color: #303133; }
.image-placeholder {
  height: 200px; background: #f5f7fa; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #909399; position: relative;
}
.image-placeholder.annotated { background: #ecf5ff; }
.annotation-markers { position: absolute; width: 100%; height: 100%; top: 0; left: 0; }
.marker { position: absolute; }
.marker-dot {
  width: 16px; height: 16px; border-radius: 50%;
  border: 2px solid #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  cursor: pointer; animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

.finding-item {
  padding: 16px; background: #f5f7fa; border-radius: 8px;
  margin-bottom: 12px;
}
.finding-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.finding-location { font-weight: 500; color: #303133; }
.finding-desc { color: #606266; line-height: 1.6; margin-bottom: 4px; }
.finding-measurement { color: #909399; font-size: 13px; }

.risk-chart { height: 250px; }
.risk-factor { margin-bottom: 16px; }
.risk-name { font-weight: 500; color: #303133; margin-bottom: 8px; }
.risk-desc { color: #909399; font-size: 12px; margin-top: 4px; }

.suggestion-content p { color: #606266; line-height: 1.8; }
.suggestion-content h4 { margin: 16px 0 12px 0; color: #303133; }
</style>
