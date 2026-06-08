<template>
  <div id="app">
    <!-- 登录/注册页面不显示导航 -->
    <router-view v-if="!showLayout" />

    <!-- 主布局 -->
    <div v-else class="app-layout">
      <!-- 侧边导航栏 -->
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <div class="logo-mark">
            <svg viewBox="0 0 40 40" class="logo-svg">
              <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5" />
              <path d="M20 8 C14 14, 14 26, 20 32 C26 26, 26 14, 20 8Z" fill="currentColor" opacity="0.3" />
              <circle cx="20" cy="20" r="4" fill="currentColor" />
            </svg>
          </div>
          <transition name="fade">
            <div v-if="!sidebarCollapsed" class="logo-text">
              <span class="brand-name">Vitalis</span>
              <span class="brand-tagline">Health Intelligence</span>
            </div>
          </transition>
        </div>

        <nav class="sidebar-nav">
          <div
            v-for="item in navItems"
            :key="item.path"
            class="nav-group"
          >
            <div class="nav-item" :class="{ active: isActive(item.path) }">
              <router-link :to="item.path" class="nav-item-main">
                <div class="nav-icon" v-html="item.icon"></div>
                <transition name="fade">
                  <span v-if="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
                </transition>
                <transition name="fade">
                  <span v-if="!sidebarCollapsed && item.badge" class="nav-badge">{{ item.badge }}</span>
                </transition>
              </router-link>
              <button
                v-if="item.children && item.children.length > 0 && !sidebarCollapsed"
                class="expand-arrow"
                :class="{ expanded: isExpanded(item.path) }"
                @click.stop="toggleMenu(item.path)"
              >
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 6 15 12 9 18" />
                </svg>
              </button>
            </div>
            <!-- 子菜单 -->
            <transition name="submenu">
              <div
                v-if="item.children && item.children.length > 0 && isExpanded(item.path) && !sidebarCollapsed"
                class="sub-menu"
              >
                <router-link
                  v-for="child in item.children"
                  :key="child.path"
                  :to="child.path"
                  class="sub-item"
                  :class="{ active: $route.path === child.path }"
                >
                  {{ child.label }}
                </router-link>
              </div>
            </transition>
          </div>
        </nav>

        <div class="sidebar-footer">
          <button class="collapse-btn" @click="toggleSidebar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path v-if="!sidebarCollapsed" d="M15 18l-6-6 6-6" />
              <path v-else d="M9 18l6-6-6-6" />
            </svg>
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <div class="main-area">
        <!-- 顶部栏 -->
        <header class="top-bar">
          <div class="top-bar-left">
            <h2 class="page-title">{{ currentPageTitle }}</h2>
            <div class="breadcrumb">
              <span class="breadcrumb-item">首页</span>
              <span v-if="currentPageTitle" class="breadcrumb-sep">/</span>
              <span v-if="currentPageTitle" class="breadcrumb-item active">{{ currentPageTitle }}</span>
            </div>
          </div>
          <div class="top-bar-right">
            <div class="search-box">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
              </svg>
              <input
                type="text"
                placeholder="搜索健康数据..."
                v-model="searchQuery"
                @input="handleSearch"
                @keyup.enter="goToSearch"
                @focus="showSearchPanel = true"
              />
              <!-- 搜索下拉面板 -->
              <transition name="dropdown">
                <div v-if="showSearchPanel && searchResults.length > 0" class="search-panel" @click.stop>
                  <div
                    v-for="result in searchResults"
                    :key="result.path"
                    class="search-result-item"
                    @click.stop="navigateTo(result.path)"
                  >
                    <div class="search-result-icon" v-html="result.icon"></div>
                    <div class="search-result-info">
                      <span class="search-result-title">{{ result.title }}</span>
                      <span class="search-result-desc">{{ result.desc }}</span>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
            <div class="notification-wrapper">
              <button class="icon-btn notification-btn" @click.stop="toggleNotificationPanel">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
                <span v-if="notificationCount > 0" class="notification-badge">{{ notificationCount }}</span>
              </button>
              <!-- 通知下拉面板 -->
              <transition name="dropdown">
                <div v-if="showNotificationPanel" class="notification-panel" @click.stop>
                  <div class="notification-header">
                    <span class="notification-title">预警通知</span>
                    <span class="notification-count">{{ notificationCount }} 条未读</span>
                  </div>
                  <div class="notification-list">
                    <div v-if="notifications.length === 0" class="notification-empty">
                      <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#9a9a9a" stroke-width="1.5">
                        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                      </svg>
                      <span>暂无预警通知</span>
                    </div>
                    <div
                      v-for="item in notifications"
                      :key="item.id"
                      class="notification-item"
                      :class="{ unread: !item.isRead }"
                    >
                      <div class="notification-dot-wrapper">
                        <span v-if="!item.isRead" class="notification-dot-indicator"></span>
                      </div>
                      <div class="notification-content">
                        <div class="notification-item-header">
                          <span class="notification-level" :class="item.level">{{ getLevelText(item.level) }}</span>
                          <span class="notification-time">{{ formatTime(item.createTime) }}</span>
                        </div>
                        <p class="notification-text">{{ item.title }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="notification-footer">
                    <button class="notification-view-all" @click.stop="goToNotifications">查看全部预警</button>
                  </div>
                </div>
              </transition>
            </div>
            <div class="user-avatar" @click="toggleUserMenu">
              <div class="avatar-circle" :class="{ 'has-avatar': userAvatar }">
                <img v-if="userAvatar" :src="userAvatar" class="avatar-image" />
                <span v-else>{{ usernameInitial }}</span>
              </div>
              <div class="user-status"></div>
            </div>
            <!-- 用户菜单 -->
            <transition name="dropdown">
              <div v-if="showUserMenu" class="user-dropdown" @click.stop>
                <div class="dropdown-header">
                  <div class="dropdown-avatar" :class="{ 'has-avatar': userAvatar }">
                    <img v-if="userAvatar" :src="userAvatar" class="dropdown-avatar-image" />
                    <span v-else>{{ usernameInitial }}</span>
                  </div>
                  <div class="dropdown-info">
                    <span class="dropdown-name">{{ username }}</span>
                    <span class="dropdown-role">健康管理师</span>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item" @click.stop="goToProfile">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
                  </svg>
                  个人中心
                </a>
                <a class="dropdown-item" @click.stop="logout">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  退出登录
                </a>
              </div>
            </transition>
          </div>
        </header>

        <!-- 页面内容 -->
        <main class="content-area">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </div>

    <!-- 点击外部关闭菜单 - 使用 pointer-events 避免事件冲突 -->
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getWarningStats, getWarnings } from '@/api/riskWarning'

export default {
  name: 'App',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const sidebarCollapsed = ref(false)
    const showUserMenu = ref(false)
    const searchQuery = ref('')
    const showSearchPanel = ref(false)
    const searchResults = ref([])
    const notificationCount = ref(0)
    const showNotificationPanel = ref(false)
    const notifications = ref([])

    // 搜索功能 - 可搜索的页面和功能
    const searchableItems = [
      { path: '/dashboard', title: '健康概览', desc: '查看健康数据总览', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#1a6b5a" stroke-width="1.5"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>' },
      { path: '/blood-sugar', title: '血糖管理', desc: '记录和分析血糖数据', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#c75450" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>' },
      { path: '/blood-sugar/diet', title: '饮食记录', desc: '记录每日饮食情况', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#c4956a" stroke-width="1.5"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/></svg>' },
      { path: '/blood-sugar/exercise', title: '运动记录', desc: '记录运动数据', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#4a9d7e" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>' },
      { path: '/blood-sugar/trend', title: '健康记录', desc: '查看血糖、血压、体重历史记录', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#5a8fbf" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>' },
      { path: '/health-manager', title: 'AI 健管师', desc: '获取AI健康建议', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#1a6b5a" stroke-width="1.5"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/><path d="M12 6v6l4 2"/></svg>' },
      { path: '/checkup', title: '智能体检', desc: '预约和管理体检', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#c4956a" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>' },
      { path: '/risk-warning', title: '风险预警', desc: '查看健康风险预警', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#c75450" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>' },
      { path: '/profile', title: '个人中心', desc: '管理个人信息', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#5a8fbf" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' }
    ]

    // 搜索处理
    const handleSearch = () => {
      const query = searchQuery.value.trim().toLowerCase()
      if (!query) {
        searchResults.value = []
        return
      }
      searchResults.value = searchableItems.filter(item =>
        item.title.toLowerCase().includes(query) ||
        item.desc.toLowerCase().includes(query)
      ).slice(0, 5)
    }

    // 跳转搜索结果
    const goToSearch = () => {
      if (searchResults.value.length > 0) {
        navigateTo(searchResults.value[0].path)
      }
    }

    // 导航到指定页面
    const navigateTo = (path) => {
      router.push(path)
      searchQuery.value = ''
      showSearchPanel.value = false
      searchResults.value = []
    }

    // 跳转到早期预警页面
    const goToNotifications = () => {
      showNotificationPanel.value = false
      router.push('/risk-warning/early')
    }

    // 切换通知面板
    const toggleNotificationPanel = () => {
      showNotificationPanel.value = !showNotificationPanel.value
      showUserMenu.value = false
      showSearchPanel.value = false
      if (showNotificationPanel.value) {
        loadNotifications()
      }
    }


    // 加载通知数量（未读预警数）
    const loadNotificationCount = async () => {
      if (!userStore.userId) return
      try {
        const res = await getWarningStats(userStore.userId)
        if (res && res.data) {
          // 使用 unread 字段表示未读预警数量
          notificationCount.value = res.data.unread || 0
        }
      } catch (error) {
        console.error('加载通知数量失败:', error)
      }
    }

    // 加载预警列表
    const loadNotifications = async () => {
      if (!userStore.userId) return
      try {
        const res = await getWarnings(userStore.userId)
        if (res && res.data) {
          // 后端返回的数据结构: { id, level, title, content, read, time }
          notifications.value = (res.data || []).slice(0, 5).map(item => ({
            id: item.id,
            title: item.title,
            level: item.level,
            isRead: item.read === true,
            createTime: item.time
          }))
        }
      } catch (error) {
        console.error('加载预警列表失败:', error)
      }
    }

    // 获取预警级别文本
    const getLevelText = (level) => {
      const levelMap = { high: '紧急', medium: '中等', low: '低' }
      return levelMap[level] || '未知'
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      // 后端返回的是 "yyyy-MM-dd HH:mm" 格式的字符串
      const date = new Date(time.replace(/-/g, '/'))
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return time.substring(5, 10) // 返回 MM-DD
    }

    const noLayoutRoutes = ['/login', '/register']
    const showLayout = computed(() => {
      return !noLayoutRoutes.includes(router.currentRoute.value.path)
    })

    const username = computed(() => userStore.realName || userStore.username || '用户')
    const usernameInitial = computed(() => userStore.usernameInitial)
    const userAvatar = computed(() => userStore.avatar)

    // 登录后自动获取用户信息
    watch(() => userStore.token, (newToken) => {
      if (newToken) {
        userStore.fetchUserInfo()
      }
    }, { immediate: true })

    // 监听userId变化，加载通知数量
    watch(() => userStore.userId, (newUserId) => {
      if (newUserId) {
        loadNotificationCount()
      }
    })

    // 监听路由变化，刷新通知数量（从预警页面返回时更新）
    watch(() => router.currentRoute.value.path, (newPath) => {
      if (userStore.userId) {
        loadNotificationCount()
      }
    })

    const navItems = [
      {
        path: '/dashboard',
        label: '健康概览',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>',
        children: []
      },
      {
        path: '/blood-sugar',
        label: '血糖管理',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>',
        children: [
          { path: '/blood-sugar/diet', label: '饮食记录' },
          { path: '/blood-sugar/exercise', label: '运动记录' },
          { path: '/blood-sugar/blood-pressure', label: '血压记录' },
          { path: '/blood-sugar/weight', label: '体重记录' },
          { path: '/blood-sugar/trend', label: '健康记录' }
        ]
      },
      {
        path: '/health-manager',
        label: 'AI 健管师',
        badge: 'AI',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/><path d="M12 6v6l4 2"/></svg>',
        children: [
          { path: '/health-manager/diet', label: '饮食处方' },
          { path: '/health-manager/exercise', label: '运动推荐' },
          { path: '/health-manager/disease', label: '慢病管理' }
        ]
      },
      {
        path: '/checkup',
        label: '智能体检',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
        children: [
          { path: '/checkup/report', label: '体检报告' },
          { path: '/checkup/image', label: '影像分析' }
        ]
      },
      {
        path: '/risk-warning',
        label: '风险预警',
        badge: '3',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
        children: [
          { path: '/risk-warning/early', label: '早期预警' },
          { path: '/risk-warning/prevention', label: '预防方案' }
        ]
      }
    ]

    // 子菜单展开状态
    const expandedMenus = ref(new Set())

    const toggleMenu = (path) => {
      if (expandedMenus.value.has(path)) {
        expandedMenus.value.delete(path)
      } else {
        expandedMenus.value.add(path)
      }
    }

    const isExpanded = (path) => {
      return expandedMenus.value.has(path)
    }

    // 根据当前路由自动展开对应菜单
    const autoExpandByRoute = () => {
      const currentPath = router.currentRoute.value.path
      const parent = navItems.find(n =>
        n.children && n.children.length > 0 && currentPath.startsWith(n.path)
      )
      if (parent) {
        expandedMenus.value.add(parent.path)
      }
    }

    watch(() => router.currentRoute.value.path, autoExpandByRoute)

    const currentPageTitle = computed(() => {
      const path = router.currentRoute.value.path
      const item = navItems.find(n => path.startsWith(n.path))
      return item ? item.label : ''
    })

    const isActive = (path) => {
      return router.currentRoute.value.path.startsWith(path)
    }

    const toggleSidebar = () => {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }

    const goToProfile = () => {
      router.push('/profile')
      nextTick(() => {
        showUserMenu.value = false
      })
    }

    const logout = () => {
      userStore.logout()
      router.push('/login')
      nextTick(() => {
        showUserMenu.value = false
      })
    }

    // 点击外部关闭菜单
    const handleClickOutside = (e) => {
      // 关闭用户菜单
      if (!e.target.closest('.user-avatar') && !e.target.closest('.user-dropdown')) {
        showUserMenu.value = false
      }
      // 关闭通知面板
      if (!e.target.closest('.notification-wrapper')) {
        showNotificationPanel.value = false
      }
      // 关闭搜索面板
      if (!e.target.closest('.search-box')) {
        showSearchPanel.value = false
      }
    }

    onMounted(() => {
      // 使用 setTimeout 避免立即触发
      setTimeout(() => {
        document.addEventListener('click', handleClickOutside)
      }, 0)
      autoExpandByRoute()
      loadNotificationCount()
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })

    return {
      username, sidebarCollapsed, showUserMenu, showLayout,
      usernameInitial, userAvatar, navItems, currentPageTitle,
      expandedMenus, toggleMenu, isExpanded,
      isActive, toggleSidebar, toggleUserMenu, goToProfile, logout,
      searchQuery, showSearchPanel, searchResults, notificationCount,
      showNotificationPanel, notifications,
      handleSearch, goToSearch, navigateTo, goToNotifications,
      toggleNotificationPanel, loadNotificationCount,
      getLevelText, formatTime
    }
  }
}
</script>

<style>
/* ============================================
   VITALIS HEALTH DESIGN SYSTEM
   Medical Luxury × Organic Wellness
   ============================================ */

@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap');

:root {
  /* Primary Palette - Deep Emerald */
  --color-primary: #1a6b5a;
  --color-primary-light: #2d8f7a;
  --color-primary-dark: #0f4d40;
  --color-primary-muted: #1a6b5a20;

  /* Accent - Warm Gold */
  --color-accent: #c4956a;
  --color-accent-light: #d4ad8a;
  --color-accent-dark: #a67b52;

  /* Neutral Palette */
  --color-bg: #faf8f5;
  --color-bg-warm: #f5f0ea;
  --color-bg-card: #ffffff;
  --color-bg-sidebar: #0f2b25;

  /* Text */
  --color-text: #2c2c2c;
  --color-text-secondary: #6b6b6b;
  --color-text-muted: #9a9a9a;
  --color-text-inverse: #f5f0ea;

  /* Semantic */
  --color-success: #4a9d7e;
  --color-warning: #d4a843;
  --color-danger: #c75450;
  --color-info: #5a8fbf;

  /* Typography */
  --font-display: 'Cormorant Garamond', Georgia, serif;
  --font-body: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;

  /* Spacing */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 48px;
  --space-3xl: 64px;

  /* Radius */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;

  /* Shadows */
  --shadow-sm: 0 1px 3px rgba(15, 43, 37, 0.06);
  --shadow-md: 0 4px 12px rgba(15, 43, 37, 0.08);
  --shadow-lg: 0 8px 30px rgba(15, 43, 37, 0.12);
  --shadow-glow: 0 0 20px rgba(26, 107, 90, 0.15);

  /* Transitions */
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

  /* Sidebar */
  --sidebar-width: 260px;
  --sidebar-collapsed: 72px;
}

/* ============================================
   RESET & BASE
   ============================================ */
*, *::before, *::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  font-family: var(--font-body);
  color: var(--color-text);
  background-color: var(--color-bg);
  line-height: 1.6;
  overflow-x: hidden;
}

/* Subtle noise texture overlay */
body::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.015;
  z-index: 9999;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

#app {
  min-height: 100vh;
}

a {
  text-decoration: none;
  color: inherit;
}

/* ============================================
   LAYOUT
   ============================================ */
.app-layout {
  display: flex;
  min-height: 100vh;
}

/* ============================================
   SIDEBAR
   ============================================ */
.sidebar {
  width: var(--sidebar-width);
  background: var(--color-bg-sidebar);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 100;
  transition: width 0.4s var(--ease-out);
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(26, 107, 90, 0.15) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(196, 149, 106, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed);
}

.sidebar-header {
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-md);
  position: relative;
}

.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  color: var(--color-accent);
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.logo-svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--color-text-inverse);
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.brand-tagline {
  font-size: 0.65rem;
  color: var(--color-accent);
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-weight: 500;
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-md) var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  overflow-y: auto;
}

.nav-group {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  border-radius: var(--radius-md);
  transition: all 0.3s var(--ease-out);
  position: relative;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.nav-item.active {
  background: rgba(26, 107, 90, 0.3);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: var(--color-accent);
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
}

.nav-item-main {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  flex: 1;
  color: rgba(245, 240, 234, 0.6);
  text-decoration: none;
  transition: color 0.2s;
  cursor: pointer;
}

.nav-item:hover .nav-item-main,
.nav-item.active .nav-item-main {
  color: var(--color-text-inverse);
}

.expand-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin-right: var(--space-sm);
  background: none;
  border: none;
  color: rgba(245, 240, 234, 0.35);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.3s var(--ease-out);
  flex-shrink: 0;
}

.expand-arrow:hover {
  color: rgba(245, 240, 234, 0.8);
  background: rgba(255, 255, 255, 0.06);
}

.expand-arrow svg {
  transition: transform 0.3s var(--ease-out);
}

.expand-arrow.expanded svg {
  transform: rotate(90deg);
}

/* 子菜单 */
.sub-menu {
  display: flex;
  flex-direction: column;
  padding: 2px 0 2px 28px;
  border-left: 1px solid rgba(26, 107, 90, 0.2);
  margin-left: 26px;
  margin-bottom: var(--space-xs);
}

.sub-item {
  display: block;
  padding: 8px var(--space-md);
  font-size: 0.82rem;
  color: rgba(245, 240, 234, 0.45);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all 0.2s var(--ease-out);
  cursor: pointer;
}

.sub-item:hover {
  color: rgba(245, 240, 234, 0.85);
  background: rgba(255, 255, 255, 0.04);
}

.sub-item.active {
  color: var(--color-accent);
  background: rgba(196, 149, 106, 0.08);
}

/* 子菜单展开/收起动画 */
.submenu-enter-active {
  transition: all 0.3s var(--ease-out);
  overflow: hidden;
}

.submenu-leave-active {
  transition: all 0.2s var(--ease-out);
  overflow: hidden;
}

.submenu-enter-from,
.submenu-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-4px);
}

.submenu-enter-to,
.submenu-leave-from {
  opacity: 1;
  max-height: 200px;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-label {
  font-size: 0.9rem;
  font-weight: 500;
}

.nav-badge {
  margin-left: auto;
  background: var(--color-accent);
  color: var(--color-bg-sidebar);
  font-size: 0.65rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  letter-spacing: 0.05em;
}

.sidebar-footer {
  padding: var(--space-md);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-sm);
  background: none;
  border: none;
  color: rgba(245, 240, 234, 0.4);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.collapse-btn:hover {
  color: var(--color-text-inverse);
  background: rgba(255, 255, 255, 0.06);
}

/* ============================================
   MAIN AREA
   ============================================ */
.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left 0.4s var(--ease-out);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sidebar.collapsed ~ .main-area {
  margin-left: var(--sidebar-collapsed);
}

/* ============================================
   TOP BAR
   ============================================ */
.top-bar {
  height: 72px;
  background: var(--color-bg-card);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.85);
}

.top-bar-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: 0.01em;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.breadcrumb-sep {
  opacity: 0.4;
}

.breadcrumb-item.active {
  color: var(--color-primary);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.search-box {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  background: var(--color-bg);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  transition: all 0.3s var(--ease-out);
  position: relative;
}

.search-box:focus-within {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-glow);
  background: var(--color-bg-card);
}

.search-box svg {
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.search-box input {
  border: none;
  background: none;
  outline: none;
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--color-text);
  width: 180px;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
}

/* 搜索面板 */
.search-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 320px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 200;
  overflow: hidden;
  max-height: 360px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  cursor: pointer;
  transition: all 0.2s;
}

.search-result-item:hover {
  background: var(--color-bg);
}

.search-result-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.search-result-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.search-result-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text);
}

.search-result-desc {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

/* 通知徽章 */
.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  background: var(--color-danger);
  color: white;
  font-size: 0.6rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  border: 2px solid var(--color-bg-card);
  padding: 0 4px;
}

/* 通知包装器 */
.notification-wrapper {
  position: relative;
}

/* 通知面板 */
.notification-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: -60px;
  width: 360px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 200;
  overflow: hidden;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-lg);
  background: var(--color-bg-warm);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.notification-title {
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}

.notification-count {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-2xl) var(--space-lg);
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.notification-item {
  display: flex;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

.notification-item:hover {
  background: var(--color-bg);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background: rgba(26, 107, 90, 0.02);
}

.notification-dot-wrapper {
  width: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  padding-top: 6px;
}

.notification-dot-indicator {
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notification-level {
  font-size: 0.65rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  letter-spacing: 0.05em;
}

.notification-level.high {
  background: rgba(199, 84, 80, 0.1);
  color: var(--color-danger);
}

.notification-level.medium {
  background: rgba(212, 168, 67, 0.1);
  color: var(--color-warning);
}

.notification-level.low {
  background: rgba(90, 143, 191, 0.1);
  color: var(--color-info);
}

.notification-time {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}

.notification-text {
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-footer {
  padding: var(--space-md) var(--space-lg);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  text-align: center;
}

.notification-view-all {
  background: none;
  border: none;
  color: var(--color-primary);
  font-family: var(--font-body);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}

.notification-view-all:hover {
  color: var(--color-primary-dark);
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--color-bg);
  color: var(--color-text);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.user-avatar {
  position: relative;
  cursor: pointer;
}

.avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.85rem;
  transition: transform 0.2s var(--ease-spring);
  overflow: hidden;
}

.avatar-circle.has-avatar {
  background: none;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar:hover .avatar-circle {
  transform: scale(1.08);
}

.user-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: var(--color-success);
  border-radius: 50%;
  border: 2px solid var(--color-bg-card);
}

/* ============================================
   USER DROPDOWN
   ============================================ */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 200;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--color-bg-warm);
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
  overflow: hidden;
}

.dropdown-avatar.has-avatar {
  background: none;
}

.dropdown-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dropdown-info {
  display: flex;
  flex-direction: column;
}

.dropdown-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.dropdown-role {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: var(--color-bg);
  color: var(--color-text);
}

.dropdown-item:last-child {
  color: var(--color-danger);
}

.dropdown-item:last-child:hover {
  background: rgba(199, 84, 80, 0.06);
}

/* ============================================
   CONTENT AREA
   ============================================ */
.content-area {
  flex: 1;
  padding: var(--space-xl);
  background: var(--color-bg);
  position: relative;
}

/* Organic background shape */
.content-area::before {
  content: '';
  position: fixed;
  top: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(26, 107, 90, 0.03) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* ============================================
   TRANSITIONS
   ============================================ */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s var(--ease-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.page-enter-active {
  transition: all 0.4s var(--ease-out);
}

.page-leave-active {
  transition: all 0.2s var(--ease-out);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.dropdown-enter-active {
  transition: all 0.3s var(--ease-spring);
}

.dropdown-leave-active {
  transition: all 0.2s var(--ease-out);
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 99;
}

/* ============================================
   SCROLLBAR
   ============================================ */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

/* ============================================
   ELEMENT PLUS OVERRIDES
   ============================================ */
.el-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.el-button--primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.el-button--primary:hover {
  background: var(--color-primary-light);
  border-color: var(--color-primary-light);
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .main-area {
    margin-left: 0;
  }

  .search-box {
    display: none;
  }
}
</style>
