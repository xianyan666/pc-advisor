<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { resetForgottenPassword } from '@/api/auth'

const router = useRouter()

const account = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const loading = ref(false)
const accountError = ref('')
const passwordError = ref('')
const confirmError = ref('')

function clearErrors() {
  accountError.value = ''
  passwordError.value = ''
  confirmError.value = ''
}

function validateForm() {
  clearErrors()
  let valid = true

  if (!account.value.trim()) {
    accountError.value = '请输入用户名、邮箱地址或电话号'
    valid = false
  }
  if (!newPassword.value) {
    passwordError.value = '请输入新密码'
    valid = false
  } else if (newPassword.value.length < 6 || newPassword.value.length > 32) {
    passwordError.value = '新密码长度需在6-32之间'
    valid = false
  }
  if (!confirmPassword.value) {
    confirmError.value = '请再次输入新密码'
    valid = false
  } else if (confirmPassword.value !== newPassword.value) {
    confirmError.value = '两次输入的密码不一致'
    valid = false
  }

  return valid
}

async function handleReset() {
  if (!validateForm()) return

  loading.value = true
  try {
    const res = await resetForgottenPassword({
      account: account.value.trim(),
      newPassword: newPassword.value,
    })
    await Swal.fire({
      icon: 'success',
      title: '重置成功',
      text: res.message || '密码已重置，请使用新密码登录',
      confirmButtonText: '去登录',
      confirmButtonColor: '#165DFF',
    })
    router.push('/login')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '重置失败，请稍后再试'
    Swal.fire({
      icon: 'error',
      title: '重置失败',
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
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg overflow-hidden fade-in">
      <div class="h-2 bg-gradient-to-r from-primary to-secondary"></div>

      <div class="p-6 md:p-8">
        <div class="text-center mb-8">
          <div class="flex justify-center mb-3">
            <div class="w-12 h-12 rounded-full bg-gradient-to-r from-primary to-secondary flex items-center justify-center">
              <i class="fa fa-key text-white text-xl"></i>
            </div>
          </div>
          <h1 class="text-2xl font-bold text-gray-600 mb-2">忘记密码</h1>
          <p class="text-gray-400 text-sm">输入用户名、邮箱地址或电话号即可重置密码</p>
        </div>

        <form class="space-y-5" @submit.prevent="handleReset">
          <div class="space-y-2">
            <label for="account" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-user-o mr-1"></i> 用户名、邮箱地址或电话号
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
                placeholder="请输入用户名、邮箱地址或电话号"
                @input="accountError = ''"
              />
            </div>
            <p v-if="accountError" class="text-red-500 text-xs">{{ accountError }}</p>
          </div>

          <div class="space-y-2">
            <label for="newPassword" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-lock mr-1"></i> 新密码
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <i class="fa fa-lock"></i>
              </span>
              <input
                id="newPassword"
                v-model="newPassword"
                :type="showPassword ? 'text' : 'password'"
                class="w-full pl-10 pr-10 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
                :class="{ '!border-red-500': passwordError }"
                placeholder="请输入新密码"
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

          <div class="space-y-2">
            <label for="confirmPassword" class="block text-sm font-medium text-gray-500">
              <i class="fa fa-check-circle-o mr-1"></i> 确认新密码
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <i class="fa fa-check-circle-o"></i>
              </span>
              <input
                id="confirmPassword"
                v-model="confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                class="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all duration-200"
                :class="{ '!border-red-500': confirmError }"
                placeholder="请再次输入新密码"
                @input="confirmError = ''"
              />
            </div>
            <p v-if="confirmError" class="text-red-500 text-xs">{{ confirmError }}</p>
          </div>

          <button
            type="submit"
            class="w-full bg-gradient-to-r from-primary to-primary/80 text-white font-medium py-3 px-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center disabled:opacity-70 disabled:cursor-not-allowed border-none cursor-pointer"
            :disabled="loading"
          >
            <span>{{ loading ? '重置中...' : '重置密码' }}</span>
            <div v-if="loading" class="ml-2 w-5 h-5 border-3 border-white/30 border-t-white rounded-full animate-spin"></div>
            <i v-else class="fa fa-refresh ml-2"></i>
          </button>
        </form>

        <div class="mt-8 text-center">
          <p class="text-gray-500 text-sm">
            想起密码了？
            <router-link to="/login" class="text-primary font-medium hover:text-primary/80 no-underline">返回登录</router-link>
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
