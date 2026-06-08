<template>
  <div class="user-profile-container">
    <el-card>
      <template #header>
        <span>个人资料</span>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="头像">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerFileInput">
              <el-avatar :size="100" :src="form.avatar" class="user-avatar">
                <span class="avatar-placeholder">{{ form.name ? form.name[0] : '用' }}</span>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleFileChange"
            />
            <div class="avatar-actions">
              <el-button type="primary" size="small" @click="triggerFileInput">
                <el-icon><Upload /></el-icon>
                上传新头像
              </el-button>
              <el-button v-if="form.avatar" type="danger" size="small" @click="removeAvatar">
                <el-icon><Delete /></el-icon>
                移除头像
              </el-button>
            </div>
            <p class="avatar-tip">支持 JPG、PNG 格式，建议尺寸 200x200 像素</p>
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD"></el-date-picker>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="身高(cm)">
          <el-input-number v-model="form.height" :min="100" :max="250" :precision="1"></el-input-number>
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="30" :max="200" :precision="1"></el-input-number>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
          <el-button @click="loadProfile">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 头像裁剪对话框 -->
    <el-dialog v-model="showCropDialog" title="裁剪头像" width="400px" :close-on-click-modal="false">
      <div class="crop-container">
        <div class="crop-preview">
          <img :src="cropImageSrc" ref="cropImageRef" @load="initCrop" />
        </div>
        <div class="crop-result">
          <p>预览：</p>
          <div class="crop-preview-circle">
            <canvas ref="previewCanvas" width="100" height="100"></canvas>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCropDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCrop">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Upload, Delete } from '@element-plus/icons-vue'
import { getCurrentUser } from '@/api/auth'
import { getUserHealthProfile, updateUserHealthProfile } from '@/api/healthManager'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const fileInput = ref(null)
const saving = ref(false)

// 头像裁剪相关
const showCropDialog = ref(false)
const cropImageSrc = ref('')
const cropImageRef = ref(null)
const previewCanvas = ref(null)
const selectedFile = ref(null)

// 裁剪状态
const cropState = reactive({
  x: 0,
  y: 0,
  size: 0,
  dragging: false,
  startX: 0,
  startY: 0
})

const form = reactive({
  avatar: '',
  username: '',
  name: '',
  gender: 'male',
  birthday: '',
  phone: '',
  email: '',
  height: 170,
  weight: 65
})

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（最大 5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  selectedFile.value = file

  // 读取文件并显示裁剪对话框
  const reader = new FileReader()
  reader.onload = (e) => {
    cropImageSrc.value = e.target.result
    showCropDialog.value = true
  }
  reader.readAsDataURL(file)

  // 清除 input 值，允许重新选择同一文件
  event.target.value = ''
}

// 初始化裁剪
const initCrop = () => {
  const img = cropImageRef.value
  if (!img) return

  // 计算裁剪区域（取图片中心正方形）
  const size = Math.min(img.naturalWidth, img.naturalHeight)
  cropState.size = size
  cropState.x = (img.naturalWidth - size) / 2
  cropState.y = (img.naturalHeight - size) / 2

  // 绘制预览
  updatePreview()
}

// 更新预览
const updatePreview = () => {
  const img = cropImageRef.value
  const canvas = previewCanvas.value
  if (!img || !canvas) return

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, 100, 100)

  // 绘制裁剪后的图片到预览 canvas
  ctx.drawImage(
    img,
    cropState.x, cropState.y, cropState.size, cropState.size,
    0, 0, 100, 100
  )
}

// 确认裁剪
const confirmCrop = () => {
  const canvas = previewCanvas.value
  if (!canvas) return

  // 将 canvas 转换为 base64
  const dataUrl = canvas.toDataURL('image/png')
  form.avatar = dataUrl

  showCropDialog.value = false
  ElMessage.success('头像已更新')
}

// 移除头像
const removeAvatar = () => {
  ElMessageBox.confirm('确定要移除头像吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.avatar = ''
    ElMessage.success('头像已移除')
  }).catch(() => {})
}

const loadProfile = async () => {
  try {
    const userRes = await getCurrentUser()
    if (userRes && userRes.data) {
      form.username = userRes.data.username || ''
      form.name = userRes.data.realName || ''
      form.gender = userRes.data.gender === 1 ? 'male' : userRes.data.gender === 2 ? 'female' : 'male'
      form.phone = userRes.data.phone || ''
      form.email = userRes.data.email || ''
      form.avatar = userRes.data.avatar || ''
      if (userRes.data.birthday) {
        form.birthday = userRes.data.birthday
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }

  if (userStore.userId) {
    try {
      const profileRes = await getUserHealthProfile(userStore.userId)
      if (profileRes && profileRes.data) {
        form.height = profileRes.data.height || 170
        form.weight = profileRes.data.weight || 65
        if (profileRes.data.birthday) {
          form.birthday = profileRes.data.birthday
        }
      }
    } catch (error) {
      console.error('加载健康档案失败:', error)
    }
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateUserHealthProfile(userStore.userId, {
      height: form.height,
      weight: form.weight,
      birthday: form.birthday,
      realName: form.name,
      phone: form.phone,
      email: form.email,
      gender: form.gender === 'male' ? 1 : 2,
      avatar: form.avatar
    })

    // 更新 store 中的用户信息
    if (userStore.updateUserInfo) {
      userStore.updateUserInfo({
        realName: form.name,
        avatar: form.avatar
      })
    }

    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!userStore.userId && userStore.token) {
    await userStore.fetchUserInfo()
  }
  await loadProfile()
})
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  width: 100px;
  height: 100px;
}

.user-avatar {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1a6b5a, #2d8f7a);
}

.avatar-placeholder {
  font-size: 36px;
  color: white;
  font-weight: 600;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
  gap: 4px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 20px;
}

.avatar-actions {
  display: flex;
  gap: 12px;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 裁剪对话框样式 */
.crop-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.crop-preview {
  width: 100%;
  max-height: 300px;
  overflow: hidden;
  border: 1px solid #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.crop-preview img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
}

.crop-result {
  display: flex;
  align-items: center;
  gap: 16px;
}

.crop-result p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.crop-preview-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #1a6b5a;
}

.crop-preview-circle canvas {
  width: 100%;
  height: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-card__header) {
  font-size: 18px;
  font-weight: 600;
}
</style>
