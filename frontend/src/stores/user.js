import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const username = ref('')
  const avatar = ref('')
  const realName = ref('')
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const usernameInitial = computed(() => {
    if (realName.value) return realName.value.charAt(0).toUpperCase()
    if (username.value) return username.value.charAt(0).toUpperCase()
    return 'U'
  })

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = ''
    userId.value = null
    username.value = ''
    avatar.value = ''
    realName.value = ''
    localStorage.removeItem('token')
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      if (res && res.data) {
        userId.value = res.data.id
        username.value = res.data.username || '用户'
        avatar.value = res.data.avatar || ''
        realName.value = res.data.realName || ''
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // token 失效时清除登录状态
      if (error.response && error.response.status === 401) {
        clearToken()
      }
    }
  }

  function updateUserInfo(info) {
    if (info.username !== undefined) username.value = info.username
    if (info.avatar !== undefined) avatar.value = info.avatar
    if (info.realName !== undefined) realName.value = info.realName
  }

  function logout() {
    userId.value = null
    username.value = ''
    avatar.value = ''
    realName.value = ''
    clearToken()
  }

  return {
    userId,
    username,
    avatar,
    realName,
    token,
    isLoggedIn,
    usernameInitial,
    setToken,
    clearToken,
    fetchUserInfo,
    updateUserInfo,
    logout
  }
})
