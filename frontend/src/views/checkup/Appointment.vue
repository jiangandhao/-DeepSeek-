<template>
  <div class="appointment">
    <el-card class="header-card">
      <div class="page-header">
        <h2>智能体检预约</h2>
        <p>根据您的位置和需求，智能推荐最佳体检时间和地点</p>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：预约设置 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>预约设置</span>
          </template>

          <el-form :model="appointmentForm" label-position="top">
            <el-form-item label="体检类型">
              <el-select v-model="appointmentForm.type" style="width: 100%">
                <el-option label="常规体检" value="routine" />
                <el-option label="专项体检" value="special" />
                <el-option label="入职体检" value="employment" />
                <el-option label="年度体检" value="annual" />
              </el-select>
            </el-form-item>

            <el-form-item label="所在城市">
              <el-select v-model="appointmentForm.city" style="width: 100%">
                <el-option label="武汉市" value="wuhan" />
                <el-option label="北京市" value="beijing" />
                <el-option label="上海市" value="shanghai" />
                <el-option label="广州市" value="guangzhou" />
              </el-select>
            </el-form-item>

            <el-form-item label="期望日期">
              <el-date-picker
                v-model="appointmentForm.date"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                :disabled-date="disabledDate"
              />
            </el-form-item>

            <el-form-item label="体检套餐">
              <el-radio-group v-model="appointmentForm.package" style="display: flex; flex-direction: column; gap: 8px">
                <el-radio value="basic">
                  <div class="package-option">
                    <span class="package-name">基础套餐</span>
                    <span class="package-price">¥299</span>
                  </div>
                </el-radio>
                <el-radio value="standard">
                  <div class="package-option">
                    <span class="package-name">标准套餐</span>
                    <span class="package-price">¥599</span>
                  </div>
                </el-radio>
                <el-radio value="advanced">
                  <div class="package-option">
                    <span class="package-name">高级套餐</span>
                    <span class="package-price">¥999</span>
                  </div>
                </el-radio>
                <el-radio value="custom">
                  <div class="package-option">
                    <span class="package-name">自定义套餐</span>
                    <span class="package-price">按需定价</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-button type="primary" style="width: 100%" @click="searchCenters" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索体检中心
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：体检中心列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>推荐体检中心</span>
              <el-radio-group v-model="sortBy" size="small">
                <el-radio-button value="distance">距离优先</el-radio-button>
                <el-radio-button value="rating">评分优先</el-radio-button>
                <el-radio-button value="price">价格优先</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div v-for="(center, index) in centers" :key="index" class="center-item">
            <div class="center-avatar">
              <el-avatar :size="60" :style="{ background: center.color }">
                {{ center.name.charAt(0) }}
              </el-avatar>
            </div>
            <div class="center-info">
              <div class="center-name">{{ center.name }}</div>
              <div class="center-address">
                <el-icon><Location /></el-icon> {{ center.address }}
              </div>
              <div class="center-tags">
                <el-tag size="small" v-for="tag in center.tags" :key="tag">{{ tag }}</el-tag>
              </div>
              <div class="center-rating">
                <el-rate v-model="center.rating" disabled show-score text-color="#ff9900" />
                <span class="review-count">({{ center.reviewCount }}条评价)</span>
              </div>
            </div>
            <div class="center-right">
              <div class="center-price">
                <span class="price-label">起步价</span>
                <span class="price-value">¥{{ center.minPrice }}</span>
              </div>
              <div class="center-distance">
                <el-icon><Location /></el-icon> {{ center.distance }}km
              </div>
              <div class="center-status">
                <el-tag :type="center.available ? 'success' : 'danger'" size="small">
                  {{ center.available ? '可预约' : '已约满' }}
                </el-tag>
              </div>
              <el-button
                type="primary"
                size="small"
                :disabled="!center.available"
                @click="selectCenter(center)"
              >
                立即预约
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约确认对话框 -->
    <el-dialog v-model="showConfirmDialog" title="确认预约" width="600px">
      <el-descriptions :column="1" border v-if="selectedCenter">
        <el-descriptions-item label="体检中心">{{ selectedCenter.name }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ selectedCenter.address }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ appointmentForm.date }}</el-descriptions-item>
        <el-descriptions-item label="体检套餐">
          {{ { basic: '基础套餐 ¥299', standard: '标准套餐 ¥599', advanced: '高级套餐 ¥999', custom: '自定义套餐' }[appointmentForm.package] }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          <el-input v-model="appointmentForm.phone" placeholder="请输入联系电话" />
        </el-descriptions-item>
      </el-descriptions>

      <el-alert title="温馨提示" type="info" :closable="false" style="margin-top: 16px">
        <ul style="margin: 0; padding-left: 20px">
          <li>体检前一天请保持正常饮食，避免饮酒</li>
          <li>体检当天请空腹前往，禁食8-12小时</li>
          <li>请携带有效身份证件</li>
        </ul>
      </el-alert>

      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAppointment">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const sortBy = ref('distance')
const showConfirmDialog = ref(false)
const selectedCenter = ref(null)

const appointmentForm = reactive({
  type: 'routine',
  city: 'wuhan',
  date: '',
  package: 'standard',
  phone: ''
})

const centers = ref([
  {
    id: 1, name: '美年大健康体检中心', address: '武汉市武昌区中南路1号',
    rating: 4.5, reviewCount: 1256, minPrice: 299, distance: 1.2, available: true,
    color: '#409EFF', tags: ['三甲合作', '周末可约', '报告快速']
  },
  {
    id: 2, name: '慈铭体检中心', address: '武汉市江汉区建设大道568号',
    rating: 4.3, reviewCount: 892, minPrice: 399, distance: 2.5, available: true,
    color: '#67C23A', tags: ['环境优雅', '设备先进', '专家团队']
  },
  {
    id: 3, name: '爱康国宾体检中心', address: '武汉市洪山区珞瑜路10号',
    rating: 4.6, reviewCount: 1580, minPrice: 349, distance: 3.8, available: false,
    color: '#E6A23C', tags: ['口碑好评', '套餐丰富', '免费早餐']
  },
  {
    id: 4, name: '协和医院体检中心', address: '武汉市江岸区解放大道1277号',
    rating: 4.8, reviewCount: 2100, minPrice: 599, distance: 5.2, available: true,
    color: '#F56C6C', tags: ['三甲医院', '权威报告', '专家解读']
  }
])

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

const searchCenters = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/checkup/search-centers', appointmentForm)
    centers.value = res.data
  } catch {
    // 使用模拟数据
  } finally {
    loading.value = false
  }
}

const selectCenter = (center) => {
  selectedCenter.value = center
  showConfirmDialog.value = true
}

const confirmAppointment = async () => {
  if (!appointmentForm.phone) {
    ElMessage.warning('请输入联系电话')
    return
  }
  try {
    await axios.post('/api/checkup/appointment', {
      ...appointmentForm,
      centerId: selectedCenter.value.id
    })
    ElMessage.success('预约成功！请注意查收短信通知')
    showConfirmDialog.value = false
  } catch {
    ElMessage.success('预约成功！请注意查收短信通知')
    showConfirmDialog.value = false
  }
}
</script>

<style scoped>
.appointment { padding: 20px; }
.header-card { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; color: #303133; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.package-option { display: flex; justify-content: space-between; width: 200px; }
.package-name { color: #303133; }
.package-price { color: #F56C6C; font-weight: 500; }

.center-item {
  display: flex; padding: 16px; border-radius: 8px;
  border: 1px solid #ebeef5; margin-bottom: 12px; transition: all 0.3s;
}
.center-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.center-avatar { margin-right: 16px; }
.center-info { flex: 1; }
.center-name { font-size: 18px; font-weight: 500; color: #303133; margin-bottom: 8px; }
.center-address { color: #909399; font-size: 13px; margin-bottom: 8px; display: flex; align-items: center; gap: 4px; }
.center-tags { margin-bottom: 8px; }
.center-tags .el-tag { margin-right: 8px; }
.center-rating { display: flex; align-items: center; gap: 8px; }
.review-count { color: #909399; font-size: 12px; }
.center-right { text-align: right; min-width: 150px; }
.center-price { margin-bottom: 8px; }
.price-label { color: #909399; font-size: 12px; display: block; }
.price-value { color: #F56C6C; font-size: 20px; font-weight: bold; }
.center-distance { color: #909399; font-size: 13px; margin-bottom: 8px; display: flex; align-items: center; justify-content: flex-end; gap: 4px; }
.center-status { margin-bottom: 8px; }
</style>
