<template>
  <div id="app">
    <el-container>
      <el-header v-if="showHeader">
        <div class="header-content">
          <h1>DeepSeek 健康管理系统</h1>
          <el-menu :default-active="activeMenu" mode="horizontal" router>
            <el-menu-item index="/dashboard">首页</el-menu-item>
            <el-sub-menu index="blood-sugar">
              <template #title>血糖管理</template>
              <el-menu-item index="/blood-sugar">血糖概览</el-menu-item>
              <el-menu-item index="/blood-sugar/diet">饮食方案</el-menu-item>
              <el-menu-item index="/blood-sugar/exercise">运动计划</el-menu-item>
              <el-menu-item index="/blood-sugar/trend">趋势分析</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="health-manager">
              <template #title>AI健管师</template>
              <el-menu-item index="/health-manager">风险评估</el-menu-item>
              <el-menu-item index="/health-manager/diet">饮食处方</el-menu-item>
              <el-menu-item index="/health-manager/exercise">运动推荐</el-menu-item>
              <el-menu-item index="/health-manager/disease">疾病管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="checkup">
              <template #title>智能体检</template>
              <el-menu-item index="/checkup">智能预约</el-menu-item>
              <el-menu-item index="/checkup/report">报告查看</el-menu-item>
              <el-menu-item index="/checkup/image">影像分析</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="risk-warning">
              <template #title>风险预警</template>
              <el-menu-item index="/risk-warning">风险分级</el-menu-item>
              <el-menu-item index="/risk-warning/early">预警通知</el-menu-item>
              <el-menu-item index="/risk-warning/prevention">预防方案</el-menu-item>
            </el-sub-menu>
          </el-menu>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ username }} <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'

export default {
  name: 'App',
  components: { ArrowDown },
  setup() {
    const router = useRouter()
    const username = ref('用户')
    const showHeader = computed(() => {
      const noHeaderRoutes = ['/login', '/register']
      return !noHeaderRoutes.includes(router.currentRoute.value.path)
    })
    const activeMenu = computed(() => router.currentRoute.value.path)

    const goToProfile = () => {
      router.push('/profile')
    }

    const logout = () => {
      localStorage.removeItem('token')
      router.push('/login')
    }

    return { username, showHeader, activeMenu, goToProfile, logout }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.header-content h1 {
  font-size: 20px;
  color: #409eff;
  margin-right: 50px;
  white-space: nowrap;
}

.el-menu {
  flex: 1;
  border: none;
}

.user-info {
  margin-left: auto;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.el-main {
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>
