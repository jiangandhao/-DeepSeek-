<template>
  <el-row :gutter="16">
    <el-col :span="14">
      <el-card>
        <template #header>
          <div class="hd"><span>🏥 智能体检推荐</span>
            <div>
              <el-select v-model="location" size="small" style="width: 160px" @change="loadCenters">
                <el-option v-for="l in locations" :key="l.name" :label="l.name" :value="l.name" />
              </el-select>
            </div>
          </div>
        </template>
        <p class="muted">按「地理距离(60%) + 机构繁忙度(40%)」综合匹配排序,匹配分越高越推荐。</p>
        <el-table :data="centers" v-loading="loading">
          <el-table-column prop="name" label="机构" min-width="160" />
          <el-table-column prop="distanceKm" label="距离(km)" width="90" />
          <el-table-column label="繁忙度" width="120">
            <template #default="{ row }">
              <el-progress :percentage="row.busyness" :stroke-width="10"
                :color="row.busyness > 70 ? '#f56c6c' : row.busyness > 40 ? '#e6a23c' : '#67c23a'" />
            </template>
          </el-table-column>
          <el-table-column label="匹配分" width="90">
            <template #default="{ row }">
              <el-tag :type="row.matchScore >= 70 ? 'success' : 'info'">{{ row.matchScore }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openBook(row)">预约</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="10">
      <el-card>
        <template #header><span>📅 我的预约</span></template>
        <el-table :data="appointments" size="small">
          <el-table-column prop="centerName" label="机构" show-overflow-tooltip />
          <el-table-column prop="examDate" label="日期" width="110" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'CANCELLED' ? 'info' : 'success'">
                {{ statusCn(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="" width="70">
            <template #default="{ row }">
              <el-button v-if="row.status === 'BOOKED'" text type="danger" size="small" @click="onCancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="dialog" title="预约体检" width="420px">
    <el-form label-width="80px">
      <el-form-item label="机构"><span>{{ current?.name }}</span></el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="bookForm.examDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="套餐">
        <el-select v-model="bookForm.pkg" style="width: 100%">
          <el-option v-for="p in currentPackages" :key="p" :label="p" :value="p" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="onBook">确认预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recommendCenters, bookAppointment, myAppointments, cancelAppointment } from '../api'

// 模拟用户定位(实际可用浏览器 Geolocation)
const locations = [
  { name: '市中心', lat: 31.2304, lng: 121.4737 },
  { name: '浦东', lat: 31.2215, lng: 121.5443 },
  { name: '徐汇', lat: 31.1902, lng: 121.4376 },
  { name: '长宁', lat: 31.2109, lng: 121.4012 }
]
const location = ref('市中心')
const centers = ref([])
const appointments = ref([])
const loading = ref(false)
const dialog = ref(false)
const current = ref(null)
const bookForm = ref({ examDate: '', pkg: '' })

const currentPackages = computed(() => (current.value?.packages ? current.value.packages.split(',') : []))
const statusCn = (s) => ({ BOOKED: '已预约', DONE: '已完成', CANCELLED: '已取消' }[s] || s)

async function loadCenters() {
  loading.value = true
  try {
    const loc = locations.find((l) => l.name === location.value)
    centers.value = await recommendCenters(loc.lat, loc.lng)
  } finally { loading.value = false }
}
async function loadAppointments() { appointments.value = await myAppointments() }

function openBook(row) {
  current.value = row
  bookForm.value = { examDate: '', pkg: currentPackages.value[0] || '' }
  dialog.value = true
}
async function onBook() {
  if (!bookForm.value.examDate) { ElMessage.warning('请选择日期'); return }
  await bookAppointment({ centerId: current.value.id, examDate: bookForm.value.examDate, pkg: bookForm.value.pkg })
  ElMessage.success('预约成功')
  dialog.value = false
  loadAppointments()
}
async function onCancel(id) {
  await cancelAppointment(id)
  ElMessage.success('已取消')
  loadAppointments()
}

onMounted(() => { loadCenters(); loadAppointments() })
</script>

<style scoped>
.hd { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #909399; font-size: 13px; margin-bottom: 10px; }
</style>
