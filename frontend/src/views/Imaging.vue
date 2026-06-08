<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card>
        <template #header><span>🫁 肺结节检测(影像识别)</span></template>
        <el-upload
          drag
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          :on-change="onSelect"
        >
          <el-icon class="up-icon"><UploadFilled /></el-icon>
          <div>将 CT 切片图像拖到此处,或<em>点击上传</em></div>
          <template #tip>
            <div class="tip">支持 PNG/JPG;演示算法,非临床诊断。</div>
          </template>
        </el-upload>
        <el-button v-if="rawUrl" type="primary" :loading="loading" style="margin-top: 12px; width: 100%" @click="run">
          开始检测
        </el-button>
        <div v-if="rawUrl" class="preview">
          <p class="muted">原始图像:</p>
          <img :src="rawUrl" class="img" />
        </div>
      </el-card>
    </el-col>

    <el-col :span="14">
      <el-card>
        <template #header>
          <div class="hd"><span>🔍 检测结果</span>
            <el-tag v-if="result" :type="levelType(result.level)">{{ levelCn(result.level) }}风险</el-tag>
          </div>
        </template>
        <div v-if="result">
          <img :src="result.annotated_image" class="img annotated" />
          <el-alert :title="result.summary" type="info" :closable="false" style="margin: 12px 0" />
          <el-table :data="result.candidates" size="small" max-height="220">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="位置(x,y)" :formatter="(r) => `${r.x}, ${r.y}`" />
            <el-table-column prop="diameter_px" label="直径(px)" width="100" />
            <el-table-column label="置信度" width="120">
              <template #default="{ row }">
                <el-progress :percentage="Math.round(row.confidence * 100)" :stroke-width="10" />
              </template>
            </el-table-column>
          </el-table>
          <p class="note">{{ result.note }}</p>
        </div>
        <el-empty v-else description="上传图像并点击『开始检测』" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { detectImage } from '../api'

const file = ref(null)
const rawUrl = ref('')
const result = ref(null)
const loading = ref(false)

const levelCn = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l)
const levelType = (l) => (l === 'HIGH' ? 'danger' : l === 'MEDIUM' ? 'warning' : 'success')

function onSelect(f) {
  file.value = f.raw
  rawUrl.value = URL.createObjectURL(f.raw)
  result.value = null
}

async function run() {
  if (!file.value) { ElMessage.warning('请先选择图像'); return }
  loading.value = true
  try {
    result.value = await detectImage(file.value)
  } catch (e) {
    ElMessage.error('检测失败:' + (e.message || ''))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.hd { display: flex; justify-content: space-between; align-items: center; }
.up-icon { font-size: 42px; color: #c0c4cc; }
.tip { color: #909399; font-size: 12px; margin-top: 6px; }
.preview { margin-top: 12px; }
.img { max-width: 100%; border-radius: 6px; border: 1px solid #eee; }
.annotated { display: block; }
.muted { color: #909399; font-size: 13px; }
.note { color: #c0c4cc; font-size: 12px; margin-top: 8px; }
</style>
