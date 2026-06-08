<template>
  <el-card style="max-width: 520px">
    <template #header><span>👤 账户设置</span></template>
    <el-form :model="form" label-width="90px">
      <el-form-item label="用户名">
        <el-input v-model="info.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" style="width: 140px">
          <el-option :value="0" label="未知" />
          <el-option :value="1" label="男" />
          <el-option :value="2" label="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="留空则不修改" />
        <div class="muted">
          当前:{{ info.phoneMasked || '未设置' }}
          <el-tag size="small" type="success" effect="plain">AES 加密存储 · 脱敏展示</el-tag>
        </div>
      </el-form-item>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMe, updateMe } from '../api'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const info = ref({})
const form = ref({ nickname: '', gender: 0, phone: '' })
const saving = ref(false)

async function load() {
  info.value = await getMe()
  form.value.nickname = info.value.nickname
  form.value.gender = info.value.gender ?? 0
  form.value.phone = ''
}

async function onSave() {
  saving.value = true
  try {
    const payload = { nickname: form.value.nickname, gender: form.value.gender }
    if (form.value.phone) payload.phone = form.value.phone
    await updateMe(payload)
    ElMessage.success('已保存')
    // 同步昵称到全局
    auth.nickname = form.value.nickname
    localStorage.setItem('nickname', form.value.nickname)
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.muted { color: #909399; font-size: 12px; margin-top: 6px; display: flex; align-items: center; gap: 8px; }
</style>
