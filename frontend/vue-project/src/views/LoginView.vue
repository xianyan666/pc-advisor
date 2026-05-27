<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Swal from 'sweetalert2'

const router = useRouter()
const authStore = useAuthStore()

const account = ref('')
const password = ref('')
const showPassword = ref(false)
const remember = ref(false)
const loading = ref(false)
const accountError = ref('')
const passwordError = ref('')

function clearErrors() {
  accountError.value = ''
  passwordError.value = ''
}

function validateForm(): boolean {
  clearErrors()
  let valid = true
  if (!account.value.trim()) {
    accountError.value = '请输入用户名、邮箱或手机号'
    valid = false
  }
  if (!password.value) {
    passwordError.value = '请输入密码'
    valid = false
  } else if (password.value.length < 6) {
    passwordError.value = '密码长度不能少于6位'
    valid = false
  }
  return valid
}

async function handleLogin() {
  if (!validateForm()) return

  loading.value = true
  try {
    await authStore.login({ username: account.value.trim(), password: password.value })

    if (remember.value) {
      localStorage.setItem('remember_me', 'true')
    }

    await Swal.fire({
      icon: 'success',
      title: '登录成功',
      text: `欢迎回来，${authStore.username}！`,
      timer: 2000,
      showConfirmButton: false,
    })
    router.push(authStore.isAdmin ? '/admin' : '/home')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '网络连接失败，请检查网络'
    Swal.fire({
      icon: 'error',
      title: '登录失败',
      text: msg,
      confirmButtonText: '确定',
      confirmButtonColor: '#165DFF',
    })
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4 md:p-6 relative bg-light">
    <!-- 登录卡片 -->
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg overflow-hidden fade-in">
      <div class="h-2 bg-gradient-to-r from-primary to-secondary"></div>

      <div class="p-6 md:p-8">
        <!-- Logo -->
        <div class="text-center mb-8">
          <div class="flex justify-center mb-3">
            <div class="w-12 h-12 rounded-full bg-gradient-to-r from-primary to-secondary flex items-center justify-center">
              <i class="fa fa-desktop text-white text-xl"></i>
            </div>
          </div>
          <h1 class="text-2xl font-bold text-gray-600 mb-2">PC硬件推荐系统</h1>
          <p class="text-gray-400 text-sm">欢迎回来，请输入您的账户信息</p>
        </div>

        <!-- 表单 -->
        <form @submit.prevent="handleLogin" class="space-y-5">
          <div class="space-y-2">
            <label for="account" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-user-o mr-1"></i> 用户名、邮箱或电话
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <i class="fa fa-user-o"></i>
              </span>
              <input
                id="account"
                v-model="account"
                type="text"
                class="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
                :class="{ '!border-red-500': accountError }"
                placeholder="请输入用户名、邮箱或手机号"
                @input="accountError = ''"
              />
            </div>
            <p v-if="accountError" class="text-red-500 text-xs">{{ accountError }}</p>
          </div>

          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <label for="password" class="block text-sm font-medium text-gray-500">
                <i class="fa fa-lock mr-1"></i> 密码
              </label>
              <router-link to="/forgot-password" class="text-xs text-primary hover:text-primary/80 no-underline">忘记密码?</router-link>
            </div>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <i class="fa fa-lock"></i>
              </span>
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                class="w-full pl-10 pr-10 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
                :class="{ '!border-red-500': passwordError }"
                placeholder="请输入密码"
                @input="passwordError = ''"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 transition-colors border-none bg-transparent cursor-pointer"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'fa fa-eye' : 'fa fa-eye-slash'"></i>
              </button>
            </div>
            <p v-if="passwordError" class="text-red-500 text-xs">{{ passwordError }}</p>
          </div>

          <div class="space-y-4">
            <div class="flex items-center">
              <input
                id="remember"
                v-model="remember"
                type="checkbox"
                class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary/30"
              />
              <label for="remember" class="ml-2 block text-sm text-gray-500">记住我（7天内自动登录）</label>
            </div>

            <button
              type="submit"
              class="w-full bg-gradient-to-r from-primary to-primary/80 text-white font-medium py-3 px-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center disabled:opacity-70 disabled:cursor-not-allowed border-none cursor-pointer"
              :disabled="loading"
            >
              <span>{{ loading ? '登录中...' : '登录账户' }}</span>
              <div v-if="loading" class="ml-2 w-5 h-5 border-3 border-white/30 border-t-white rounded-full animate-spin"></div>
              <i v-else class="fa fa-arrow-right ml-2"></i>
            </button>
          </div>
        </form>

        <div class="mt-8 text-center">
          <p class="text-gray-500 text-sm">
            还没有账户?
            <router-link to="/register" class="text-primary font-medium hover:text-primary/80 no-underline">立即注册</router-link>
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
