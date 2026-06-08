import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: Number(localStorage.getItem('userId')) || null,
    username: localStorage.getItem('username') || '',
    nickname: localStorage.getItem('nickname') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    setAuth({ token, userId, username, nickname }) {
      this.token = token
      this.userId = userId
      this.username = username
      this.nickname = nickname
      localStorage.setItem('token', token)
      localStorage.setItem('userId', userId)
      localStorage.setItem('username', username)
      localStorage.setItem('nickname', nickname || '')
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.nickname = ''
      localStorage.clear()
    }
  }
})
