<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getCollections, getEvaluations, deleteEvaluation, updateProfileUsername, resetProfilePassword, deleteAccount } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { CollectedItem, EvaluationItem } from '@/api/user'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref<'collections' | 'evaluations'>('collections')
const collections = ref<CollectedItem[]>([])
const evaluations = ref<EvaluationItem[]>([])
const loading = ref(true)
const errorMsg = ref('')
const usernameForm = ref(authStore.username)
const newPassword = ref('')
const profileSaving = ref(false)
const passwordSaving = ref(false)
const accountDeleting = ref(false)
const profileMsg = ref('')
const profileError = ref('')

async function handleUpdateUsername() {
  const nextUsername = usernameForm.value.trim()
  profileMsg.value = ''
  profileError.value = ''
  if (!nextUsername) {
    profileError.value = '用户名不能为空'
    return
  }
  profileSaving.value = true
  try {
    const result = await updateProfileUsername(nextUsername)
    authStore.updateUsername(result.username)
    usernameForm.value = result.username
    profileMsg.value = result.message || '用户名修改成功'
  } catch (error) {
    profileError.value = error instanceof Error ? error.message : '用户名修改失败'
  } finally {
    profileSaving.value = false
  }
}

async function handleResetPassword() {
  profileMsg.value = ''
  profileError.value = ''
  if (!newPassword.value.trim()) {
    profileError.value = '请输入新密码'
    return
  }
  if (!confirm('确定直接重置当前账号密码吗？')) return
  passwordSaving.value = true
  try {
    const result = await resetProfilePassword(newPassword.value.trim())
    newPassword.value = ''
    profileMsg.value = result.message || '密码重置成功'
  } catch (error) {
    profileError.value = error instanceof Error ? error.message : '密码重置失败'
  } finally {
    passwordSaving.value = false
  }
}

async function handleDeleteAccount() {
  profileMsg.value = ''
  profileError.value = ''
  const firstConfirm = confirm('注销账户后，您的收藏、评测和评论将被永久删除，且无法恢复。确定继续吗？')
  if (!firstConfirm) return
  const secondConfirm = confirm(`请再次确认：确定注销当前账号「${authStore.username}」吗？`)
  if (!secondConfirm) return

  accountDeleting.value = true
  try {
    await deleteAccount()
    authStore.logout()
    router.replace('/login')
  } catch (error) {
    profileError.value = error instanceof Error ? error.message : '账户注销失败'
  } finally {
    accountDeleting.value = false
  }
}

// 灯箱
const lightboxVisible = ref(false)
const lightboxSrc = ref('')
const lightboxScale = ref(1)
function openLightbox(src: string) {
  lightboxSrc.value = src
  lightboxScale.value = 1
  lightboxVisible.value = true
}
function closeLightbox() {
  lightboxVisible.value = false
}
function onLightboxWheel(e: WheelEvent) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  lightboxScale.value = Math.max(0.5, Math.min(5, lightboxScale.value + delta))
}

const typeLabels: Record<string, string> = {
  CPU: 'CPU',
  GRAPHICS_CARD: '显卡',
  MOTHERBOARD: '主板',
}

const auditLabels: Record<string, string> = {
  pending: '审核中',
  approved: '已通过',
  rejected: '未通过',
}

const auditClasses: Record<string, string> = {
  pending: 'bg-yellow-100 text-yellow-700',
  approved: 'bg-green-100 text-green-700',
  rejected: 'bg-red-100 text-red-700',
}

const deletingEvalId = ref<number | null>(null)

async function handleDeleteEval(id: number, e: Event) {
  e.stopPropagation()
  if (!confirm('确定要删除这篇评测吗？相关的评论也会被删除。')) return
  deletingEvalId.value = id
  try {
    await deleteEvaluation(id)
    evaluations.value = evaluations.value.filter(item => item.evaluationId !== id)
  } catch {
    alert('删除失败，请稍后重试')
  } finally {
    deletingEvalId.value = null
  }
}

async function loadData() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [colRes, evalRes] = await Promise.all([
      getCollections(),
      getEvaluations(),
    ])
    collections.value = colRes || []
    evaluations.value = evalRes || []
  } catch {
    errorMsg.value = '加载数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goToHardware(id: number) {
  router.push(`/hardware/${id}`)
}

function goToHardwareByName(name: string) {
  router.push({ path: '/hardware-detail', query: { name } })
}

onMounted(loadData)
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <!-- 用户信息卡片 -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="flex items-center">
        <div class="w-16 h-16 rounded-full bg-gradient-to-br from-primary to-blue-400 flex items-center justify-center flex-shrink-0">
          <i class="fa fa-user text-white text-2xl"></i>
        </div>
        <div class="ml-4">
          <h1 class="text-xl font-bold text-gray-700 m-0">{{ authStore.username }}</h1>
          <p class="text-sm text-gray-400 mt-1">
            <i class="fa fa-calendar-o mr-1"></i> 加入于 PC硬件推荐系统
          </p>
        </div>
      </div>
      <div class="mt-5 grid grid-cols-3 gap-4">
        <div class="text-center p-3 bg-gray-50 rounded-lg">
          <div class="text-2xl font-bold text-primary">{{ collections.length }}</div>
          <div class="text-xs text-gray-400 mt-1">收藏硬件</div>
        </div>
        <div class="text-center p-3 bg-gray-50 rounded-lg">
          <div class="text-2xl font-bold text-primary">{{ evaluations.length }}</div>
          <div class="text-xs text-gray-400 mt-1">发布评测</div>
        </div>
        <div class="text-center p-3 bg-gray-50 rounded-lg">
          <div class="text-2xl font-bold text-primary">{{ evaluations.filter(e => e.auditState === 'approved').length }}</div>
          <div class="text-xs text-gray-400 mt-1">已通过评测</div>
        </div>
      </div>
    </div>

    <!-- 个人信息修改 -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-lg font-bold text-gray-700 m-0">个人信息</h2>
          <p class="text-sm text-gray-400 mt-1">修改用户名，或无需验证码直接重置密码</p>
        </div>
        <i class="fa fa-cog text-primary text-xl"></i>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="bg-gray-50 rounded-lg p-4">
          <label class="block text-sm font-medium text-gray-600 mb-2">用户名</label>
          <div class="flex gap-2">
            <input
              v-model="usernameForm"
              type="text"
              class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10"
              placeholder="请输入新的用户名"
            />
            <button
              class="px-4 py-2 bg-primary text-white rounded-lg text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors disabled:opacity-60"
              :disabled="profileSaving"
              @click="handleUpdateUsername"
            >
              {{ profileSaving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
        <div class="bg-gray-50 rounded-lg p-4">
          <label class="block text-sm font-medium text-gray-600 mb-2">重置密码</label>
          <div class="flex gap-2">
            <input
              v-model="newPassword"
              type="password"
              class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10"
              placeholder="输入新密码，无需验证码"
            />
            <button
              class="px-4 py-2 bg-secondary text-white rounded-lg text-sm border-none cursor-pointer hover:bg-secondary/90 transition-colors disabled:opacity-60"
              :disabled="passwordSaving"
              @click="handleResetPassword"
            >
              {{ passwordSaving ? '重置中...' : '重置' }}
            </button>
          </div>
        </div>
      </div>
      <div v-if="profileMsg" class="mt-3 text-sm text-green-600 bg-green-50 px-3 py-2 rounded-lg">{{ profileMsg }}</div>
      <div v-if="profileError" class="mt-3 text-sm text-red-500 bg-red-50 px-3 py-2 rounded-lg">{{ profileError }}</div>
    </div>

    <!-- 账户注销 -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6 border border-red-100">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-lg font-bold text-red-600 m-0">注销账户</h2>
          <p class="text-sm text-gray-400 mt-1">永久删除当前账号以及关联的收藏、评测和评论</p>
        </div>
        <button
          class="px-4 py-2 bg-red-500 text-white rounded-lg text-sm border-none cursor-pointer hover:bg-red-600 transition-colors disabled:opacity-60"
          :disabled="accountDeleting"
          @click="handleDeleteAccount"
        >
          <i :class="accountDeleting ? 'fa fa-spinner fa-spin mr-1.5' : 'fa fa-trash-o mr-1.5'"></i>
          {{ accountDeleting ? '注销中...' : '注销账户' }}
        </button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="bg-white rounded-xl shadow-sm">
      <div class="flex border-b border-gray-200">
        <button
          class="flex-1 py-3.5 text-center text-sm font-medium border-none bg-transparent cursor-pointer transition-all duration-200 relative"
          :class="activeTab === 'collections' ? 'text-primary' : 'text-gray-400 hover:text-gray-600'"
          @click="activeTab = 'collections'"
        >
          <i class="fa fa-heart mr-1.5"></i>我的收藏
          <span
            v-if="activeTab === 'collections'"
            class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary rounded-full"
          ></span>
        </button>
        <button
          class="flex-1 py-3.5 text-center text-sm font-medium border-none bg-transparent cursor-pointer transition-all duration-200 relative"
          :class="activeTab === 'evaluations' ? 'text-primary' : 'text-gray-400 hover:text-gray-600'"
          @click="activeTab = 'evaluations'"
        >
          <i class="fa fa-file-text-o mr-1.5"></i>我的评测
          <span
            v-if="activeTab === 'evaluations'"
            class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary rounded-full"
          ></span>
        </button>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="p-8">
        <div class="animate-pulse space-y-4">
          <div v-for="i in 3" :key="i" class="flex gap-4">
            <div class="w-20 h-20 bg-gray-200 rounded-lg flex-shrink-0"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-200 rounded w-3/4"></div>
              <div class="h-3 bg-gray-100 rounded w-1/2"></div>
              <div class="h-3 bg-gray-100 rounded w-1/4"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-else-if="errorMsg" class="p-8 text-center">
        <i class="fa fa-exclamation-circle text-4xl text-gray-300 mb-3 block"></i>
        <p class="text-gray-400">{{ errorMsg }}</p>
        <button
          @click="loadData"
          class="mt-4 px-5 py-2 bg-primary text-white rounded-full text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors"
        >
          重新加载
        </button>
      </div>

      <!-- 收藏列表 -->
      <template v-else-if="activeTab === 'collections'">
        <div v-if="collections.length === 0" class="p-12 text-center">
          <i class="fa fa-heart-o text-5xl text-gray-300 mb-4 block"></i>
          <p class="text-gray-400 text-base">暂无收藏的硬件</p>
          <p class="text-gray-300 text-sm mt-1">浏览硬件页面，点击收藏按钮添加</p>
          <button
            @click="router.push('/hardware')"
            class="mt-4 px-5 py-2 bg-primary text-white rounded-full text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors"
          >
            去逛逛
          </button>
        </div>
        <div v-else class="divide-y divide-gray-100">
          <div
            v-for="item in collections"
            :key="item.collectId"
            class="flex items-center p-4 hover:bg-gray-50 transition-colors cursor-pointer"
            @click="goToHardware(item.hardwareId)"
          >
            <img
              :src="item.imageUrl || getHardwareImage(item.hardwareName)"
              :alt="item.hardwareName"
              class="w-20 h-20 object-cover rounded-lg flex-shrink-0"
            />
            <div class="ml-4 flex-1 min-w-0">
              <div class="flex items-center">
                <span class="inline-block bg-primary/10 text-primary text-xs px-2 py-0.5 rounded-full mr-2">
                  {{ typeLabels[item.hardwareType] || item.hardwareType }}
                </span>
                <h3 class="text-sm font-medium text-gray-700 m-0 truncate">{{ item.hardwareName }}</h3>
              </div>
              <p class="text-xs text-gray-400 mt-1">{{ item.hardwareBrand }} · {{ item.hardwareModel }}</p>
              <div class="flex items-center justify-between mt-2">
                <span class="text-secondary font-bold text-sm">¥{{ item.hardwarePrice }}</span>
                <span class="text-xs text-gray-300">
                  <i class="fa fa-clock-o mr-1"></i>{{ item.collectTime?.substring(0, 10) }}
                </span>
              </div>
            </div>
            <i class="fa fa-angle-right text-gray-300 ml-2"></i>
          </div>
        </div>
      </template>

      <!-- 评测列表 -->
      <template v-else>
        <div v-if="evaluations.length === 0" class="p-12 text-center">
          <i class="fa fa-file-text-o text-5xl text-gray-300 mb-4 block"></i>
          <p class="text-gray-400 text-base">暂无发布的评测</p>
          <p class="text-gray-300 text-sm mt-1">在硬件详情页可以撰写评测</p>
          <button
            @click="router.push('/hardware')"
            class="mt-4 px-5 py-2 bg-primary text-white rounded-full text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors"
          >
            去逛逛
          </button>
        </div>
        <div v-else class="divide-y divide-gray-100">
          <div
            v-for="item in evaluations"
            :key="item.evaluationId"
            class="p-4 hover:bg-gray-50 transition-colors cursor-pointer"
            @click="router.push(`/evaluation/${item.evaluationId}`)"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h3 class="text-sm font-medium text-gray-700 m-0 truncate">{{ item.evaluationTitle }}</h3>
                  <span
                    class="text-xs px-2 py-0.5 rounded-full flex-shrink-0"
                    :class="auditClasses[item.auditState] || 'bg-gray-100 text-gray-500'"
                  >
                    {{ auditLabels[item.auditState] || item.auditState }}
                  </span>
                  <button
                    v-if="item.auditState !== 'pending'"
                    class="ml-auto px-2 py-0.5 text-xs text-red-400 hover:text-red-500 hover:bg-red-50 rounded border-none bg-transparent cursor-pointer transition-colors"
                    :disabled="deletingEvalId === item.evaluationId"
                    @click.stop="handleDeleteEval(item.evaluationId, $event)"
                  >
                    <i :class="deletingEvalId === item.evaluationId ? 'fa fa-spinner fa-spin' : 'fa fa-trash-o'"></i>
                  </button>
                </div>
                <p class="text-xs text-gray-400 mt-1 line-clamp-2">{{ item.usageExperience }}</p>
                <div v-if="item.images && item.images.length" class="flex gap-1 mt-2 flex-wrap">
                  <img
                    v-for="(img, i) in item.images"
                    :key="i"
                    :src="img"
                    alt="评测图片"
                    class="w-14 h-14 object-cover rounded border border-gray-100 cursor-zoom-in hover:scale-110 transition-transform"
                    @click.stop="openLightbox(img)"
                  />
                </div>
                <div class="flex items-center mt-2 text-xs text-gray-400 gap-4">
                  <span v-if="item.hardware">
                    <i class="fa fa-cube mr-1"></i>
                    <span
                      class="text-primary cursor-pointer hover:underline"
                      @click.stop="goToHardware(item.hardware!.hardwareId)"
                    >
                      {{ item.hardware.hardwareName }}
                    </span>
                  </span>
                  <span>
                    <i class="fa fa-clock-o mr-1"></i>{{ item.publishTime?.substring(0, 10) }}
                  </span>
                </div>
              </div>
              <img
                v-if="item.hardware"
                :src="item.hardware.imageUrl || getHardwareImage(item.hardware.hardwareName)"
                :alt="item.hardware.hardwareName"
                class="w-16 h-16 object-cover rounded-lg flex-shrink-0 ml-3"
              />
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 灯箱 -->
    <Teleport to="body">
      <div
        v-if="lightboxVisible"
        class="fixed inset-0 z-[100] bg-black/80 flex items-center justify-center"
        @click="closeLightbox"
        @wheel="onLightboxWheel"
      >
        <button
          class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 text-white flex items-center justify-center border-none cursor-pointer text-xl transition-colors z-10"
          @click.stop="closeLightbox"
        >
          <i class="fa fa-times"></i>
        </button>
        <span class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/50 text-white text-xs px-3 py-1 rounded-full z-10">
          {{ Math.round(lightboxScale * 100) }}%
        </span>
        <img
          :src="lightboxSrc"
          alt="预览"
          class="object-contain rounded-lg shadow-2xl transition-transform duration-150"
          :style="{ transform: `scale(${lightboxScale})`, maxWidth: '90vw', maxHeight: '90vh' }"
          @click.stop
        />
      </div>
    </Teleport>
  </div>
</template>
