<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">🩺 健康管理</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon><span>血糖概览</span>
        </el-menu-item>
        <el-menu-item index="/assistant">
          <el-icon><ChatDotRound /></el-icon><span>AI 智能体</span>
        </el-menu-item>
        <el-menu-item index="/health-manager">
          <el-icon><FirstAidKit /></el-icon><span>数智健管师</span>
        </el-menu-item>
        <el-menu-item index="/exam">
          <el-icon><OfficeBuilding /></el-icon><span>智能体检</span>
        </el-menu-item>
        <el-menu-item index="/imaging">
          <el-icon><Picture /></el-icon><span>影像识别</span>
        </el-menu-item>
        <el-menu-item index="/glucose">
          <el-icon><Histogram /></el-icon><span>血糖记录</span>
        </el-menu-item>
        <el-menu-item index="/diet">
          <el-icon><Bowl /></el-icon><span>饮食记录</span>
        </el-menu-item>
        <el-menu-item index="/exercise">
          <el-icon><Basketball /></el-icon><span>运动记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <el-button text @click="router.push('/account')">
          <el-icon><User /></el-icon>&nbsp;你好,{{ auth.nickname || auth.username }}
        </el-button>
        <el-button text type="danger" @click="onLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Histogram, Bowl, Basketball, ChatDotRound, FirstAidKit, OfficeBuilding, Picture, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #001529; }
.logo { color: #fff; font-size: 18px; text-align: center; padding: 18px 0; font-weight: bold; }
.menu { border-right: none; background: #001529; }
.menu :deep(.el-menu-item) { color: #b7c0cd; }
.menu :deep(.el-menu-item.is-active) { color: #fff; background: #1890ff; }
.header { display: flex; align-items: center; justify-content: flex-end; gap: 16px; background: #fff; border-bottom: 1px solid #eee; }
.welcome { color: #606266; }
</style>
