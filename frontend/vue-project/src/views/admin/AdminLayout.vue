<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(true)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

const menu = [
  { path: '/admin', icon: 'fa-dashboard', label: '概览' },
  { path: '/admin/hardware', icon: 'fa-cubes', label: '硬件管理' },
  { path: '/admin/audit', icon: 'fa-check-square-o', label: '审核评测' },
]
</script>

<template>
  <div class="flex min-h-screen bg-[#0d1117]">
    <!-- 侧边栏 -->
    <aside
      class="w-56 bg-[#161b22] border-r border-[#30363d] flex-shrink-0 transition-all duration-200"
      :class="{ '-ml-56': !sidebarOpen }"
    >
      <div class="p-4 border-b border-[#30363d]">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded bg-[#238636] flex items-center justify-center">
            <i class="fa fa-shield text-white text-xs"></i>
          </div>
          <span class="text-white font-semibold text-sm">管理后台</span>
        </div>
      </div>
      <nav class="p-2">
        <router-link
          v-for="item in menu"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-2 px-3 py-2 rounded-md text-sm no-underline mb-0.5 transition-colors"
          :class="route.path === item.path ? 'bg-[#1f6feb]/20 text-[#58a6ff]' : 'text-[#8b949e] hover:bg-[#1c2128] hover:text-[#c9d1d9]'"
        >
          <i :class="'fa ' + item.icon + ' w-4 text-center'"></i>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <!-- 主内容 -->
    <div class="flex-1 flex flex-col min-w-0">
      <header class="h-12 bg-[#161b22] border-b border-[#30363d] flex items-center px-4 gap-3 flex-shrink-0">
        <button
          class="text-[#8b949e] hover:text-[#c9d1d9] border-none bg-transparent cursor-pointer"
          @click="sidebarOpen = !sidebarOpen"
        >
          <i class="fa fa-bars"></i>
        </button>
        <span class="text-xs text-[#8b949e] flex-1">PC Advisor Admin</span>
        <div class="flex items-center gap-3">
          <span class="text-xs text-[#c9d1d9]">
            <i class="fa fa-user-circle text-[#58a6ff] mr-1"></i>{{ authStore.username }}
          </span>
          <button
            class="text-xs text-[#8b949e] hover:text-[#f85149] border-none bg-transparent cursor-pointer transition-colors"
            @click="handleLogout"
          >
            <i class="fa fa-sign-out mr-1"></i>退出
          </button>
        </div>
      </header>
      <main class="flex-1 p-4 overflow-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>
