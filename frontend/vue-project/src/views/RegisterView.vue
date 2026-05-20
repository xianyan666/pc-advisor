<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Swal from 'sweetalert2'

const router = useRouter()
const authStore = useAuthStore()

const userName = ref('')
const password = ref('')
const confirmPassword = ref('')
const userMailbox = ref('')
const userPhone = ref('')
const showPassword = ref(false)
const loading = ref(false)
const isSubmitting = ref(false)

const errors = ref({
  userName: '',
  password: '',
  confirmPassword: '',
  userMailbox: '',
  userPhone: '',
})

function clearError(field: keyof typeof errors.value) {
  errors.value[field] = ''
}

function validateForm(): boolean {
  let valid = true
  errors.value = { userName: '', password: '', confirmPassword: '', userMailbox: '', userPhone: '' }

  if (!userName.value.trim()) {
    errors.value.userName = '请输入用户名'
    valid = false
  } else if (userName.value.trim().length < 2) {
    errors.value.userName = '用户名长度不能少于2位'
    valid = false
  }

  if (!password.value) {
    errors.value.password = '请输入密码'
    valid = false
  } else if (password.value.length < 6) {
    errors.value.password = '密码长度不能少于6位'
    valid = false
  }

  if (!confirmPassword.value) {
    errors.value.confirmPassword = '请确认密码'
    valid = false
  } else if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    valid = false
  }

  if (userMailbox.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userMailbox.value)) {
    errors.value.userMailbox = '邮箱格式不正确'
    valid = false
  }

  if (userPhone.value && !/^1[3-9]\d{9}$/.test(userPhone.value)) {
    errors.value.userPhone = '手机号格式不正确'
    valid = false
  }

  return valid
}

async function handleRegister() {
  if (isSubmitting.value) return
  if (!validateForm()) return

  isSubmitting.value = true
  loading.value = true
  try {
    await authStore.register({
      userName: userName.value.trim(),
      password: password.value,
      userMailbox: userMailbox.value.trim() || undefined,
      userPhone: userPhone.value.trim() || undefined,
    })

    await Swal.fire({
      icon: 'success',
      title: '注册成功',
      text: '请使用您的账号登录',
      timer: 2000,
      showConfirmButton: false,
    })
    router.push('/login')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '网络连接失败，请检查网络'
    Swal.fire({
      icon: 'error',
      title: '注册失败',
      text: msg,
      confirmButtonText: '确定',
      confirmButtonColor: '#165DFF',
    })
  } finally {
    isSubmitting.value = false
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4 md:p-6 bg-light">
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg overflow-hidden fade-in">
      <div class="h-2 bg-gradient-to-r from-primary to-secondary"></div>

      <div class="p-6 md:p-8">
        <div class="text-center mb-8">
          <div class="flex justify-center mb-3">
            <div class="w-12 h-12 rounded-full bg-gradient-to-r from-primary to-secondary flex items-center justify-center">
              <i class="fa fa-desktop text-white text-xl"></i>
            </div>
          </div>
          <h1 class="text-2xl font-bold text-gray-600 mb-2">创建账户</h1>
          <p class="text-gray-400 text-sm">注册成为PC硬件推荐系统用户</p>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-4">
          <!-- 用户名 -->
          <div class="space-y-1.5">
            <label for="userName" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-user-o mr-1"></i> 用户名
            </label>
            <input
              id="userName"
              v-model="userName"
              type="text"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
              :class="{ '!border-red-500': errors.userName }"
              placeholder="请输入用户名"
              @input="clearError('userName')"
            />
            <p v-if="errors.userName" class="text-red-500 text-xs">{{ errors.userName }}</p>
          </div>

          <!-- 密码 -->
          <div class="space-y-1.5">
            <label for="password" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-lock mr-1"></i> 密码
            </label>
            <div class="relative">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                class="w-full px-4 pr-10 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
                :class="{ '!border-red-500': errors.password }"
                placeholder="请输入密码（至少6位）"
                @input="clearError('password')"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 transition-colors border-none bg-transparent cursor-pointer"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'fa fa-eye' : 'fa fa-eye-slash'"></i>
              </button>
            </div>
            <p v-if="errors.password" class="text-red-500 text-xs">{{ errors.password }}</p>
          </div>

          <!-- 确认密码 -->
          <div class="space-y-1.5">
            <label for="confirmPassword" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-lock mr-1"></i> 确认密码
            </label>
            <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
              :class="{ '!border-red-500': errors.confirmPassword }"
              placeholder="请再次输入密码"
              @input="clearError('confirmPassword')"
            />
            <p v-if="errors.confirmPassword" class="text-red-500 text-xs">{{ errors.confirmPassword }}</p>
          </div>

          <!-- 邮箱 -->
          <div class="space-y-1.5">
            <label for="userMailbox" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-envelope-o mr-1"></i> 邮箱
            </label>
            <input
              id="userMailbox"
              v-model="userMailbox"
              type="email"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
              :class="{ '!border-red-500': errors.userMailbox }"
              placeholder="请输入邮箱地址"
              @input="clearError('userMailbox')"
            />
            <p v-if="errors.userMailbox" class="text-red-500 text-xs">{{ errors.userMailbox }}</p>
          </div>

          <!-- 手机号 -->
          <div class="space-y-1.5">
            <label for="userPhone" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-phone mr-1"></i> 手机号
            </label>
            <input
              id="userPhone"
              v-model="userPhone"
              type="tel"
              class="w-full px-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
              :class="{ '!border-red-500': errors.userPhone }"
              placeholder="请输入手机号"
              @input="clearError('userPhone')"
            />
            <p v-if="errors.userPhone" class="text-red-500 text-xs">{{ errors.userPhone }}</p>
          </div>

          <!-- 注册按钮 -->
          <div class="pt-2">
            <button
              type="submit"
              class="w-full bg-gradient-to-r from-primary to-primary/80 text-white font-medium py-3 px-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center disabled:opacity-70 disabled:cursor-not-allowed border-none cursor-pointer"
              :disabled="loading"
            >
              <span>{{ loading ? '注册中...' : '注册账户' }}</span>
              <div v-if="loading" class="ml-2 w-5 h-5 border-3 border-white/30 border-t-white rounded-full animate-spin"></div>
              <i v-else class="fa fa-arrow-right ml-2"></i>
            </button>
          </div>
        </form>

        <div class="mt-8 text-center">
          <p class="text-gray-500 text-sm">
            已有账户?
            <router-link to="/login" class="text-primary font-medium hover:text-primary/80 no-underline">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-in {
  animation: fadeIn 0.5s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
