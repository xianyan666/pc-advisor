import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api/auth'
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const username = computed(() => userInfo.value?.username || '')

  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      role: res.data.role,
    }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res
  }

  async function register(data: RegisterRequest) {
    return registerApi(data)
  }

  function logout() {
    logoutApi().catch(() => {})
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function updateUsername(username: string) {
    if (!userInfo.value) return
    userInfo.value = {
      ...userInfo.value,
      username,
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return { token, userInfo, isLoggedIn, isAdmin, username, login, register, logout, updateUsername }
})
