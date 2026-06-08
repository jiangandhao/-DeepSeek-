<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <h2 class="title">注册账号</h2>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名(3-32位)" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称(可选)" :prefix-icon="Avatar" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码(6-32位)" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onRegister">注册</el-button>
      </el-form>
      <div class="foot">已有账号?<router-link to="/login">去登录</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const form = ref({ username: '', password: '', nickname: '' })
const loading = ref(false)

async function onRegister() {
  loading.value = true
  try {
    const data = await register(form.value)
    auth.setAuth(data)
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff22, #67c23a22);
}
.auth-card { width: 360px; padding: 12px; }
.title { text-align: center; margin: 8px 0 24px; color: #303133; }
.foot { text-align: center; margin-top: 16px; font-size: 14px; }
</style>
