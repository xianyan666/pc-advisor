<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const mobileMenuOpen = ref(false)
const headerShadow = ref(false)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function isActive(path: string) {
  return route.path === path
}

function onScroll() {
  headerShadow.value = window.scrollY > 50
}

onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <div class="min-h-screen bg-light">
    <!-- 头部导航 -->
    <header
      class="bg-white sticky top-0 z-50 transition-all duration-300"
      :class="{ 'shadow-md': headerShadow }"
    >
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between py-3 border-b border-gray-200">
          <div class="flex items-center">
            <a href="/" class="flex items-center no-underline">
              <div class="w-8 h-8 rounded-full bg-gradient-to-r from-primary to-secondary flex items-center justify-center">
                <i class="fa fa-desktop text-white text-sm"></i>
              </div>
              <span class="ml-2 text-gray-600 font-medium">PC硬件推荐系统</span>
            </a>
          </div>

          <div class="hidden md:flex items-center flex-1 max-w-xl mx-8">
            <div class="relative w-full">
              <input
                type="text"
                placeholder="搜索CPU、显卡、主板等硬件产品..."
                class="w-full py-2 px-4 pl-10 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
              <i class="fa fa-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"></i>
            </div>
          </div>

          <div class="flex items-center space-x-4">
            <!-- 未登录 -->
            <template v-if="!authStore.isLoggedIn">
              <router-link
                to="/login"
                class="hidden md:block text-gray-500 hover:text-primary transition-colors no-underline"
              >
                <i class="fa fa-user-circle-o mr-1"></i> 登录
              </router-link>
            </template>
            <!-- 已登录 -->
            <template v-else>
              <div class="hidden md:flex items-center space-x-3">
                <router-link
                  to="/user"
                  class="flex items-center text-sm text-gray-600 hover:text-primary transition-colors no-underline cursor-pointer"
                >
                  <i class="fa fa-user-circle text-primary mr-1 text-base"></i>
                  {{ authStore.username }}
                </router-link>
                <button
                  @click="handleLogout"
                  class="text-xs text-gray-400 hover:text-red-500 transition-colors border-none bg-transparent cursor-pointer"
                >
                  <i class="fa fa-sign-out mr-1"></i>退出
                </button>
              </div>
            </template>
            <button
              class="md:hidden text-gray-500 border-none bg-transparent cursor-pointer"
              @click="mobileMenuOpen = !mobileMenuOpen"
            >
              <i class="fa fa-bars text-xl"></i>
            </button>
          </div>
        </div>

        <!-- 桌面导航 -->
        <nav class="hidden md:block">
          <ul class="flex space-x-8 list-none m-0 p-0">
            <li>
              <router-link
                to="/home"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/home') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                首页
              </router-link>
            </li>
            <li>
              <router-link
                to="/hardware"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/hardware') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                DIY硬件
              </router-link>
            </li>
            <li>
              <router-link
                to="/evaluations"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/evaluations') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                硬件评测
              </router-link>
            </li>
            <li>
              <router-link
                to="/ranking"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/ranking') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                推荐榜
              </router-link>
            </li>
            <li>
              <router-link
                to="/recommend"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/recommend') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                配置推荐
              </router-link>
            </li>
            <li>
              <router-link
                v-if="authStore.isAdmin"
                to="/admin"
                class="py-3 inline-block font-medium no-underline"
                :class="isActive('/admin') ? 'nav-active' : 'text-gray-500 hover:text-primary transition-colors'"
              >
                管理后台
              </router-link>
            </li>
          </ul>
        </nav>

        <!-- 移动端搜索 -->
        <div class="md:hidden py-2 border-t border-gray-200">
          <div class="relative">
            <input
              type="text"
              placeholder="搜索CPU、显卡、主板等硬件产品..."
              class="w-full py-2 px-4 pl-10 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
            />
            <i class="fa fa-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"></i>
          </div>
        </div>

        <!-- 移动端菜单 -->
        <div class="md:hidden" :class="{ hidden: !mobileMenuOpen }">
          <ul class="py-2 list-none m-0 p-0">
            <li>
              <router-link to="/home" class="block px-2 py-3 font-medium no-underline" :class="isActive('/home') ? 'text-primary' : 'text-gray-500'">
                首页
              </router-link>
            </li>
            <li>
              <router-link to="/hardware" class="block px-2 py-3 no-underline" :class="isActive('/hardware') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                DIY硬件
              </router-link>
            </li>
            <li>
              <router-link to="/evaluations" class="block px-2 py-3 no-underline" :class="isActive('/evaluations') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                硬件评测
              </router-link>
            </li>
            <li>
              <router-link to="/ranking" class="block px-2 py-3 no-underline" :class="isActive('/ranking') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                推荐榜
              </router-link>
            </li>
            <li>
              <router-link to="/recommend" class="block px-2 py-3 no-underline" :class="isActive('/recommend') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                配置推荐
              </router-link>
            </li>
            <li v-if="authStore.isAdmin">
              <router-link to="/admin" class="block px-2 py-3 no-underline" :class="isActive('/admin') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                管理后台
              </router-link>
            </li>
            <template v-if="authStore.isLoggedIn">
              <li class="border-t border-gray-200 pt-2">
                <router-link to="/user" class="block px-2 py-2 no-underline" :class="isActive('/user') ? 'text-primary' : 'text-gray-500 hover:bg-gray-100'">
                  <i class="fa fa-user-circle-o mr-1"></i> 个人中心
                </router-link>
              </li>
              <li>
                <button @click="handleLogout" class="block w-full text-left px-2 py-2 text-gray-500 hover:bg-gray-100 border-none bg-transparent cursor-pointer">
                  <i class="fa fa-sign-out mr-1"></i> 退出登录
                </button>
              </li>
            </template>
            <li v-if="!authStore.isLoggedIn" class="border-t border-gray-200 pt-2">
              <router-link to="/login" class="block px-2 py-2 text-gray-500 hover:bg-gray-100 no-underline">
                <i class="fa fa-user-circle-o mr-1"></i> 登录/注册
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main>
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="bg-white border-t border-gray-200 py-6 mt-8">
      <div class="container mx-auto px-4 text-center text-xs text-gray-400">
        © 2025 PC硬件推荐系统
      </div>
    </footer>
  </div>
</template>
