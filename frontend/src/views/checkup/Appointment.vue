<template>
  <div class="appointment">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>智能体检预约</span>
            </div>
          </template>

          <el-steps :active="currentStep" finish-status="success" align-center>
            <el-step title="选择套餐" />
            <el-step title="选择机构" />
            <el-step title="选择时间" />
            <el-step title="确认预约" />
          </el-steps>

          <!-- 步骤1: 选择套餐 -->
          <div v-show="currentStep === 0" class="step-content">
            <h3>请选择体检套餐</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="pkg in packages" :key="pkg.id">
                <el-card shadow="hover" :class="{ 'selected': selectedPackage?.id === pkg.id }" @click="selectPackage(pkg)">
                  <div class="package-card">
                    <h4>{{ pkg.name }}</h4>
                    <div class="price">¥{{ pkg.price }}</div>
                    <ul>
                      <li v-for="item in pkg.items" :key="item">{{ item }}</li>
                    </ul>
                    <el-tag :type="pkg.suitable === '推荐' ? 'danger' : 'info'" size="small">
                      {{ pkg.suitable }}
                    </el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤2: 选择机构 -->
          <div v-show="currentStep === 1" class="step-content">
            <div class="location-header">
              <h3>请选择体检机构</h3>
              <div class="location-status">
                <span v-if="locationLoading" class="loading-text">
                  <el-icon class="is-loading"><Location /></el-icon> 正在定位...
                </span>
                <span v-else-if="userLocation" class="location-success">
                  <el-icon><Location /></el-icon> 已定位（{{ userLocation.lat.toFixed(4) }}, {{ userLocation.lng.toFixed(4) }}）
                </span>
                <span v-else-if="locationError" class="location-error">
                  {{ locationError }}
                </span>
                <el-button size="small" @click="refreshLocation" :loading="locationLoading">
                  刷新定位
                </el-button>
              </div>
            </div>
            <el-row :gutter="20">
              <el-col :span="12" v-for="center in centers" :key="center.id">
                <el-card shadow="hover" :class="{ 'selected': selectedCenter?.id === center.id }" @click="selectCenter(center)">
                  <div class="center-card">
                    <h4>{{ center.name }}</h4>
                    <p><el-icon><Location /></el-icon> {{ center.address }}</p>
                    <p v-if="center.phone"><el-icon><Phone /></el-icon> {{ center.phone }}</p>
                    <div class="center-info">
                      <el-rate v-model="center.rating" disabled show-score />
                      <span class="distance">{{ center.distance }}</span>
                    </div>
                    <el-tag :type="center.waitTime < 30 ? 'success' : 'warning'" size="small">
                      预计等待: {{ center.waitTime }}分钟
                    </el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤3: 选择时间 -->
          <div v-show="currentStep === 2" class="step-content">
            <h3>请选择预约时间</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card>
                  <template #header><span>选择日期</span></template>
                  <el-date-picker
                    v-model="selectedDate"
                    type="date"
                    placeholder="选择日期"
                    :disabled-date="disabledDate"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    @change="handleDateChange"
                  />
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <template #header><span>选择时段</span></template>
                  <div class="time-slots">
                    <div v-for="slot in timeSlots" :key="slot.time"
                         :class="['slot', { 'selected': selectedTime === slot.time, 'disabled': !slot.available }]"
                         @click="slot.available && (selectedTime = slot.time)">
                      <span>{{ slot.time }}</span>
                      <el-tag v-if="!slot.available" type="info" size="small">已满</el-tag>
                      <span v-else class="slot-count">剩余{{ slot.count }}个</span>
                    </div>
                    <div v-if="timeSlots.length === 0" class="no-slots">
                      暂无可用时段
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤4: 确认预约 -->
          <div v-show="currentStep === 3" class="step-content">
            <h3>确认预约信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="体检套餐">{{ selectedPackage?.name }}</el-descriptions-item>
              <el-descriptions-item label="体检机构">{{ selectedCenter?.name }}</el-descriptions-item>
              <el-descriptions-item label="预约日期">{{ selectedDate }}</el-descriptions-item>
              <el-descriptions-item label="预约时段">{{ selectedTime }}</el-descriptions-item>
              <el-descriptions-item label="套餐价格">
                <span class="price">¥{{ selectedPackage?.price }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <el-alert title="预约须知" type="info" :closable="false" style="margin-top: 20px;">
              <ul>
                <li>体检前一天请保持清淡饮食，避免饮酒</li>
                <li>体检当日请空腹前往，禁食禁水8小时以上</li>
                <li>请携带本人身份证原件</li>
              </ul>
            </el-alert>
          </div>

          <div class="step-actions">
            <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
            <el-button v-if="currentStep < 3" type="primary" @click="nextStep" :disabled="!canNext">
              下一步
            </el-button>
            <el-button v-if="currentStep === 3" type="success" @click="confirmAppointment" :loading="loading">
              确认预约
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>我的预约</span>
          </template>
          <div v-for="item in myAppointments" :key="item.id" class="appointment-item" @click="showDetail(item)" style="cursor: pointer;">
            <div class="appt-info">
              <h4>{{ item.packageName || item.package }}</h4>
              <p>{{ item.centerName || item.center }}</p>
              <p>{{ item.appointmentDate || item.date }} {{ item.appointmentTime || item.time }}</p>
            </div>
            <div class="appt-actions">
              <el-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
              <el-button v-if="item.status === 0 || item.status === 1" size="small" type="danger" @click.stop="cancelAppointment(item)">取消</el-button>
            </div>
          </div>
          <div v-if="myAppointments.length === 0" style="text-align: center; color: #999; padding: 20px;">
            暂无预约
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>智能推荐</span>
          </template>
          <div class="ai-recommend">
            <el-icon><MagicStick /></el-icon>
            <p>根据您的健康档案，AI推荐您进行：</p>
            <ul v-if="aiRecommendations.length > 0">
              <li v-for="(item, index) in aiRecommendations" :key="index">{{ item }}</li>
            </ul>
            <ul v-else>
              <li>胸部CT检查（家族有肺部疾病史）</li>
              <li>糖化血红蛋白检测（血糖偏高）</li>
              <li>颈动脉超声（年龄>40岁）</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="500px">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="预约编号">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="体检套餐">{{ currentDetail.packageName }}</el-descriptions-item>
        <el-descriptions-item label="体检机构">{{ currentDetail.centerName }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ currentDetail.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="预约时段">{{ currentDetail.appointmentTime }}</el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button v-if="currentDetail && (currentDetail.status === 0 || currentDetail.status === 1)" type="danger" @click="cancelFromDetail">取消预约</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Location, Phone, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckupPackages, getCheckupCenters, getTimeSlots, createAppointment, getAppointments, cancelAppointment as cancelAppointmentApi, getAiRecommendation } from '@/api/checkup'
import { getNearbyHospitals } from '@/api/location'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentStep = ref(0)
const loading = ref(false)
const selectedPackage = ref(null)
const selectedCenter = ref(null)
const selectedDate = ref(new Date())
const selectedTime = ref('')

const packages = ref([])
const centers = ref([])
const timeSlots = ref([])
const myAppointments = ref([])
const aiRecommendations = ref([])

// 定位相关
const userLocation = ref(null)
const locationLoading = ref(false)
const locationError = ref('')

// 预约详情弹窗
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

const showDetail = (item) => {
  currentDetail.value = item
  detailDialogVisible.value = true
}

const cancelFromDetail = async () => {
  if (!currentDetail.value) return
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelAppointmentApi(currentDetail.value.id)
    ElMessage.success('预约已取消')
    detailDialogVisible.value = false
    await loadMyAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败')
    }
  }
}

const canNext = computed(() => {
  if (currentStep.value === 0) return selectedPackage.value !== null
  if (currentStep.value === 1) return selectedCenter.value !== null
  if (currentStep.value === 2) return selectedTime.value !== ''
  return true
})

const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

const selectPackage = (pkg) => { selectedPackage.value = pkg }
const selectCenter = (center) => { selectedCenter.value = center }

const nextStep = () => {
  if (canNext.value) {
    currentStep.value++
    if (currentStep.value === 2) {
      loadTimeSlots()
    }
  }
}

const prevStep = () => {
  if (currentStep.value > 0) currentStep.value--
}

const loadPackages = async () => {
  try {
    const res = await getCheckupPackages()
    packages.value = res.data || []
  } catch (error) {
    console.error('加载套餐失败:', error)
  }
}

const loadCenters = async () => {
  try {
    const res = await getCheckupCenters()
    centers.value = res.data || []
  } catch (error) {
    console.error('加载机构失败:', error)
  }
}

// 获取用户位置
const getUserLocation = () => {
  if (!navigator.geolocation) {
    locationError.value = '您的浏览器不支持定位功能'
    return
  }

  locationLoading.value = true
  locationError.value = ''

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { latitude, longitude } = position.coords
      userLocation.value = { lat: latitude, lng: longitude }
      locationLoading.value = false

      // 加载附近医院
      await loadNearbyHospitals(latitude, longitude)
    },
    (error) => {
      locationLoading.value = false
      switch (error.code) {
        case error.PERMISSION_DENIED:
          locationError.value = '定位权限被拒绝，请在浏览器设置中允许定位'
          break
        case error.POSITION_UNAVAILABLE:
          locationError.value = '位置信息不可用'
          break
        case error.TIMEOUT:
          locationError.value = '定位请求超时'
          break
        default:
          locationError.value = '获取位置失败'
      }
      // 定位失败时使用默认数据
      loadCenters()
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 300000
    }
  )
}

// 加载附近医院
const loadNearbyHospitals = async (lat, lng) => {
  try {
    const res = await getNearbyHospitals(lat, lng, 10)
    centers.value = res.data || []
  } catch (error) {
    console.error('加载附近医院失败:', error)
    // 失败时使用默认数据
    loadCenters()
  }
}

// 刷新定位
const refreshLocation = () => {
  getUserLocation()
}

const loadTimeSlots = async () => {
  if (!selectedCenter.value || !selectedDate.value) return
  try {
    // selectedDate 可能是 Date 对象或字符串
    let dateStr
    if (typeof selectedDate.value === 'string') {
      dateStr = selectedDate.value
    } else {
      dateStr = selectedDate.value.toISOString().split('T')[0]
    }
    const res = await getTimeSlots(selectedCenter.value.id, dateStr)
    timeSlots.value = res.data || []
  } catch (error) {
    console.error('加载时间段失败:', error)
  }
}

const handleDateChange = (date) => {
  selectedTime.value = ''
  loadTimeSlots()
}

const loadMyAppointments = async () => {
  try {
    const res = await getAppointments(userStore.userId)
    myAppointments.value = res.data || []
  } catch (error) {
    console.error('加载预约列表失败:', error)
  }
}

const loadAiRecommendations = async () => {
  try {
    const res = await getAiRecommendation(userStore.userId)
    aiRecommendations.value = res.data?.recommendedPackages || []
  } catch (error) {
    console.error('加载AI推荐失败:', error)
  }
}

const cancelAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelAppointmentApi(appointment.id)
    ElMessage.success('预约已取消')
    await loadMyAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败')
    }
  }
}

const confirmAppointment = async () => {
  loading.value = true
  try {
    // selectedDate 可能是字符串或 Date 对象
    let dateStr
    if (typeof selectedDate.value === 'string') {
      dateStr = selectedDate.value
    } else {
      dateStr = selectedDate.value.toISOString().split('T')[0]
    }
    const appointmentData = {
      userId: userStore.userId,
      packageName: selectedPackage.value.name,
      centerName: selectedCenter.value.name,
      appointmentDate: dateStr,
      appointmentTime: selectedTime.value
    }
    await createAppointment(appointmentData)
    ElMessage.success('预约成功！请注意查收短信通知')
    currentStep.value = 0
    selectedPackage.value = null
    selectedCenter.value = null
    selectedTime.value = ''
    await loadMyAppointments()
  } catch (error) {
    ElMessage.error('预约失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 等待用户信息加载完成
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  loadPackages()
  getUserLocation() // 获取定位并加载附近医院
  loadMyAppointments()
  loadAiRecommendations()
})

onUnmounted(() => {
  // 清理状态
  timeSlots.value = []
})
</script>

<style scoped>
.appointment {
  padding: 20px;
}
.step-content {
  margin: 30px 0;
}
.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}
.package-card, .center-card {
  text-align: center;
}
.package-card h4, .center-card h4 {
  margin: 0 0 10px 0;
}
.price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}
.package-card ul {
  list-style: none;
  padding: 0;
  text-align: left;
  font-size: 13px;
  color: #666;
}
.package-card ul li::before {
  content: '✓ ';
  color: #67C23A;
}
.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.center-card p {
  margin: 5px 0;
  color: #666;
  font-size: 13px;
}
.center-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
}
.distance {
  color: #999;
}
.time-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}
.slot.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.slot.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.slot-count {
  color: #67C23A;
  font-size: 12px;
}
.no-slots {
  text-align: center;
  color: #999;
  padding: 20px;
}
.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.appt-info h4 {
  margin: 0 0 5px 0;
}
.appt-info p {
  margin: 2px 0;
  color: #666;
  font-size: 13px;
}
.ai-recommend {
  text-align: center;
  padding: 10px;
}
.ai-recommend .el-icon {
  font-size: 32px;
  color: #409EFF;
}
.ai-recommend ul {
  text-align: left;
  padding-left: 20px;
}
.ai-recommend li {
  margin: 5px 0;
  font-size: 13px;
}
.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.location-header h3 {
  margin: 0;
}
.location-status {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.loading-text {
  color: #409EFF;
}
.location-success {
  color: #67C23A;
}
.location-error {
  color: #F56C6C;
}
</style>
