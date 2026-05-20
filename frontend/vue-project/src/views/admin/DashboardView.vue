<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { testTables, testDb } from '@/api/common'

const userStore = useUserStore()
const tables = ref<string[]>([])
const dbOk = ref(false)

onMounted(async () => {
  try {
    const dbResult = await testDb()
    dbOk.value = dbResult === '数据库连接成功'
  } catch {
    dbOk.value = false
  }
  try {
    tables.value = await testTables()
  } catch {
    tables.value = []
  }
  await userStore.fetchUsers()
})
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-600 m-0">管理后台</h1>
      <span class="text-sm text-gray-400">
        <i class="fa fa-calendar mr-1"></i>
        {{ new Date().toLocaleDateString('zh-CN') }}
      </span>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
      <div class="bg-white rounded-xl shadow-sm p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center flex-shrink-0">
          <i class="fa fa-database text-green-600 text-lg"></i>
        </div>
        <div>
          <p class="text-sm text-gray-400 mb-1">数据库状态</p>
          <p class="text-lg font-bold" :class="dbOk ? 'text-green-600' : 'text-red-500'">
            {{ dbOk ? '已连接' : '未连接' }}
          </p>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
          <i class="fa fa-table text-blue-600 text-lg"></i>
        </div>
        <div>
          <p class="text-sm text-gray-400 mb-1">数据表</p>
          <p class="text-lg font-bold text-gray-600">{{ tables.length }} 张</p>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-orange-100 flex items-center justify-center flex-shrink-0">
          <i class="fa fa-users text-orange-600 text-lg"></i>
        </div>
        <div>
          <p class="text-sm text-gray-400 mb-1">用户数</p>
          <p class="text-lg font-bold text-gray-600">{{ userStore.users.length }}</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 数据库表 -->
      <div class="bg-white rounded-xl shadow-sm p-5">
        <h2 class="text-lg font-bold text-gray-600 mb-4">数据库表</h2>
        <div class="flex flex-wrap gap-2" v-if="tables.length > 0">
          <span v-for="t in tables" :key="t" class="px-3 py-1.5 bg-blue-50 text-blue-600 rounded-full text-sm">
            {{ t }}
          </span>
        </div>
        <p v-else class="text-gray-400 text-sm">暂无数据</p>
      </div>

      <!-- 快捷操作 -->
      <div class="bg-white rounded-xl shadow-sm p-5">
        <h2 class="text-lg font-bold text-gray-600 mb-4">快捷操作</h2>
        <div class="grid grid-cols-2 gap-3">
          <button class="p-4 bg-gray-50 rounded-lg text-center hover:bg-gray-100 transition-colors border-none cursor-pointer">
            <i class="fa fa-plus-circle text-primary text-xl block mb-2"></i>
            <span class="text-sm text-gray-600">添加硬件</span>
          </button>
          <button class="p-4 bg-gray-50 rounded-lg text-center hover:bg-gray-100 transition-colors border-none cursor-pointer">
            <i class="fa fa-file-text-o text-primary text-xl block mb-2"></i>
            <span class="text-sm text-gray-600">审核评测</span>
          </button>
          <button class="p-4 bg-gray-50 rounded-lg text-center hover:bg-gray-100 transition-colors border-none cursor-pointer">
            <i class="fa fa-bar-chart text-primary text-xl block mb-2"></i>
            <span class="text-sm text-gray-600">数据统计</span>
          </button>
          <button class="p-4 bg-gray-50 rounded-lg text-center hover:bg-gray-100 transition-colors border-none cursor-pointer">
            <i class="fa fa-cog text-primary text-xl block mb-2"></i>
            <span class="text-sm text-gray-600">系统设置</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="bg-white rounded-xl shadow-sm p-5">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-bold text-gray-600">用户列表</h2>
        <button class="text-sm text-primary hover:text-primary/80 border-none bg-transparent cursor-pointer">
          <i class="fa fa-refresh mr-1"></i> 刷新
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full border-collapse text-sm" v-if="userStore.users.length > 0">
          <thead>
            <tr>
              <th class="px-4 py-3 text-left bg-gray-50 font-semibold text-gray-500 border-b border-gray-200">用户ID</th>
              <th class="px-4 py-3 text-left bg-gray-50 font-semibold text-gray-500 border-b border-gray-200">用户名</th>
              <th class="px-4 py-3 text-left bg-gray-50 font-semibold text-gray-500 border-b border-gray-200">邮箱</th>
              <th class="px-4 py-3 text-left bg-gray-50 font-semibold text-gray-500 border-b border-gray-200">角色</th>
              <th class="px-4 py-3 text-left bg-gray-50 font-semibold text-gray-500 border-b border-gray-200">创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userStore.users" :key="user.userId" class="hover:bg-gray-50 transition-colors">
              <td class="px-4 py-3 border-b border-gray-100 text-gray-500 font-mono text-xs">{{ user.userId }}</td>
              <td class="px-4 py-3 border-b border-gray-100 font-medium text-gray-600">{{ user.username }}</td>
              <td class="px-4 py-3 border-b border-gray-100 text-gray-400">{{ user.email || '-' }}</td>
              <td class="px-4 py-3 border-b border-gray-100">
                <span
                  class="text-xs px-2 py-1 rounded-full"
                  :class="user.role === 'ADMIN' ? 'bg-red-50 text-red-600' : 'bg-blue-50 text-blue-600'"
                >
                  {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </span>
              </td>
              <td class="px-4 py-3 border-b border-gray-100 text-gray-400 text-xs">{{ user.createTime || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else-if="!userStore.loading" class="text-gray-400 text-sm text-center py-8">暂无用户数据</p>
        <p v-else class="text-gray-400 text-sm text-center py-8">加载中...</p>
      </div>
    </div>
  </div>
</template>
