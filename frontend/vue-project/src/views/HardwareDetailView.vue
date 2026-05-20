<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { testHardware } from '@/api/common'
import { toggleCollect, checkCollected, createEvaluation, getEvaluations, uploadImage } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { EvaluationItem } from '@/api/user'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const hardware = ref<Record<string, unknown> | null>(null)
const loading = ref(true)

// 收藏状态
const collected = ref(false)
const collectId = ref('')
const collectLoading = ref(false)

// 评测列表
const evaluations = ref<EvaluationItem[]>([])
const evalLoading = ref(false)

// 评测表单
const showEvalForm = ref(false)
const evalForm = ref({
  evaluationTitle: '',
  perfTestData: '',
  usageExperience: '',
  prosAndCons: '',
})
const evalSubmitting = ref(false)
const evalSuccess = ref('')
const evalError = ref('')

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

// 图片上传
const uploadedImages = ref<string[]>([])
const uploadingImage = ref(false)
const uploadError = ref('')
const uploadInput = ref<HTMLInputElement | null>(null)

async function handleUploadImage(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploadError.value = ''

  if (!file.type.startsWith('image/')) {
    uploadError.value = '只允许上传图片文件'
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    uploadError.value = '图片大小不能超过 10MB'
    return
  }

  uploadingImage.value = true
  try {
    const res = await uploadImage(file)
    uploadedImages.value.push(res.url)
  } catch {
    uploadError.value = '图片上传失败，请稍后重试'
  } finally {
    uploadingImage.value = false
    if (target) target.value = ''
  }
}

function removeImage(index: number) {
  uploadedImages.value.splice(index, 1)
}

function triggerUpload() {
  uploadInput.value?.click()
}

const typeLabels: Record<string, string> = {
  'CPU': 'CPU 处理器',
  'GRAPHICS_CARD': '显卡',
  'MOTHERBOARD': '主板',
}

const typeSpecs: Record<string, { label: string; key: string; unit?: string }[]> = {
  'CPU': [
    { label: '核心数', key: 'coreCount', unit: '核' },
    { label: '线程数', key: 'threadCount', unit: '线程' },
    { label: '基础频率', key: 'baseFreq', unit: 'GHz' },
    { label: '接口类型', key: 'cpuInterface' },
    { label: 'TDP功耗', key: 'tdpPower', unit: 'W' },
    { label: '支持内存', key: 'cpuMemType' },
  ],
  'GRAPHICS_CARD': [
    { label: '核心型号', key: 'coreModel' },
    { label: '显存容量', key: 'vramCap' },
    { label: '显存类型', key: 'vramType' },
    { label: '功耗', key: 'powerConsump', unit: 'W' },
    { label: '显卡长度', key: 'gcLength', unit: 'mm' },
  ],
  'MOTHERBOARD': [
    { label: 'CPU接口', key: 'mbCpuInterface' },
    { label: '板型', key: 'mbForm' },
    { label: '内存插槽', key: 'memSlotCount', unit: '个' },
    { label: '支持内存', key: 'mbMemType' },
    { label: 'M.2插槽', key: 'm2SlotCount', unit: '个' },
  ],
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

async function loadHardware() {
  loading.value = true
  try {
    const list = await testHardware()
    const id = Number(route.params.id)
    const name = route.query.name as string | undefined

    if (!isNaN(id) && id > 0) {
      hardware.value = list.find((h) => Number(h.HardwareID) === id) || null
    } else if (name) {
      hardware.value = list.find((h) => {
        const hwName = String(h.HardwareName)
        return hwName.includes(name) || name.includes(hwName)
      }) || null
    } else {
      hardware.value = null
    }

    if (hardware.value && authStore.isLoggedIn) {
      const hwId = Number(hardware.value.HardwareID)
      await Promise.all([loadCollectStatus(hwId), loadEvaluations(hwId)])
    }
  } catch {
    hardware.value = null
  } finally {
    loading.value = false
  }
}

async function loadCollectStatus(hwId: number) {
  try {
    const res = await checkCollected(hwId)
    collected.value = res.collected
    collectId.value = res.collectId || ''
  } catch {
    collected.value = false
  }
}

async function loadEvaluations(hwId: number) {
  evalLoading.value = true
  try {
    const allEvals = await getEvaluations()
    evaluations.value = (allEvals || []).filter(
      (e) => e.hardware?.hardwareId === hwId
    )
  } catch {
    evaluations.value = []
  } finally {
    evalLoading.value = false
  }
}

async function handleCollect() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!hardware.value) return

  collectLoading.value = true
  try {
    const hwId = Number(hardware.value.HardwareID)
    const res = await toggleCollect(hwId)
    collected.value = res.collected
    if (res.collectId) {
      collectId.value = res.collectId
    }
  } catch {
    // 失败时保持原状态
  } finally {
    collectLoading.value = false
  }
}

async function handleSubmitEval() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!hardware.value) return

  if (!evalForm.value.evaluationTitle.trim()) {
    evalError.value = '请输入评测标题'
    return
  }
  if (!evalForm.value.usageExperience.trim()) {
    evalError.value = '请输入使用体验'
    return
  }

  evalSubmitting.value = true
  evalError.value = ''
  evalSuccess.value = ''
  uploadError.value = ''

  try {
    const hwId = Number(hardware.value.HardwareID)
    await createEvaluation({
      hardwareId: hwId,
      evaluationTitle: evalForm.value.evaluationTitle.trim(),
      perfTestData: evalForm.value.perfTestData.trim(),
      usageExperience: evalForm.value.usageExperience.trim(),
      prosAndCons: evalForm.value.prosAndCons.trim(),
      imageUrls: [...uploadedImages.value],
    })
    evalSuccess.value = '评测已提交，等待管理员审核后公开显示'
    evalForm.value = { evaluationTitle: '', perfTestData: '', usageExperience: '', prosAndCons: '' }
    uploadedImages.value = []
    showEvalForm.value = false
    // 重新加载评测列表
    await loadEvaluations(hwId)
  } catch {
    evalError.value = '发布失败，请稍后重试'
  } finally {
    evalSubmitting.value = false
  }
}

function goBack() {
  router.push('/hardware')
}

function goToHardware(id: number) {
  router.push(`/hardware/${id}`)
}

onMounted(loadHardware)
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <!-- 骨架屏 -->
    <template v-if="loading">
      <div class="animate-pulse">
        <div class="h-6 bg-gray-200 rounded w-24 mb-6"></div>
        <div class="bg-white rounded-xl p-6">
          <div class="flex flex-col md:flex-row gap-6">
            <div class="w-full md:w-80 h-56 bg-gray-200 rounded-lg"></div>
            <div class="flex-1 space-y-3">
              <div class="h-6 bg-gray-200 rounded w-2/3"></div>
              <div class="h-4 bg-gray-100 rounded w-1/3"></div>
              <div class="h-20 bg-gray-100 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 未找到 -->
    <template v-else-if="!hardware">
      <div class="text-center py-20">
        <i class="fa fa-frown-o text-5xl text-gray-300 mb-4 block"></i>
        <p class="text-gray-500 text-lg">未找到该硬件</p>
        <button class="mt-4 px-6 py-2 bg-primary text-white rounded-full border-none cursor-pointer" @click="goBack">
          返回列表
        </button>
      </div>
    </template>

    <!-- 硬件详情 -->
    <template v-else>
      <!-- 面包屑 -->
      <div class="text-sm text-gray-400 mb-4">
        <a href="/hardware" class="text-primary hover:underline no-underline" @click.prevent="goBack">DIY硬件</a>
        <span class="mx-2">/</span>
        <span class="text-gray-600">{{ hardware.HardwareName }}</span>
      </div>

      <!-- 基本信息卡片 -->
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex flex-col md:flex-row gap-6">
          <!-- 图片 -->
          <div class="w-full md:w-80 flex-shrink-0">
            <img
              :src="(hardware.imageUrl as string) || getHardwareImage(String(hardware.HardwareName))"
              :alt="String(hardware.HardwareName)"
              class="w-full h-56 object-cover rounded-lg cursor-zoom-in hover:shadow-md transition-shadow"
              @click="openLightbox((hardware.imageUrl as string) || getHardwareImage(String(hardware.HardwareName)))"
            />
          </div>
          <!-- 信息 -->
          <div class="flex-1">
            <div class="flex items-start justify-between">
              <div>
                <span class="inline-block bg-primary/10 text-primary text-xs px-2.5 py-1 rounded-full mb-2">
                  {{ typeLabels[String(hardware.HardwareType)] || hardware.HardwareType }}
                </span>
                <h1 class="text-xl font-bold text-gray-700 mt-1">{{ hardware.HardwareName }}</h1>
                <p class="text-sm text-gray-400 mt-1">{{ hardware.HardwareBrand }} · {{ hardware.HardwareModel }}</p>
              </div>
              <div class="text-right">
                <div class="text-3xl font-bold text-secondary">¥{{ hardware.HardwarePrice }}</div>
                <div class="text-xs text-gray-400 mt-1">参考价格</div>
              </div>
            </div>

            <!-- 规格参数 -->
            <div class="mt-5 grid grid-cols-2 md:grid-cols-3 gap-3">
              <div
                v-for="spec in (typeSpecs[String(hardware.HardwareType)] || [])"
                :key="spec.key"
                class="bg-gray-50 rounded-lg px-3 py-2"
              >
                <div class="text-xs text-gray-400">{{ spec.label }}</div>
                <div class="text-sm font-medium text-gray-700 mt-0.5">
                  {{ hardware[spec.key] ? hardware[spec.key] + (spec.unit || '') : '-' }}
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="mt-5 flex gap-3">
              <button
                v-if="!authStore.isLoggedIn"
                class="px-5 py-2 bg-primary text-white rounded-full hover:bg-primary/90 transition-colors border-none cursor-pointer text-sm font-medium"
                @click="router.push('/login')"
              >
                <i class="fa fa-heart-o mr-1"></i>收藏
              </button>
              <button
                v-else
                class="px-5 py-2 rounded-full transition-all border-none cursor-pointer text-sm font-medium"
                :class="collected ? 'bg-red-50 text-red-500 hover:bg-red-100' : 'bg-primary text-white hover:bg-primary/90'"
                :disabled="collectLoading"
                @click="handleCollect"
              >
                <i :class="collectLoading ? 'fa fa-spinner fa-spin' : collected ? 'fa fa-heart' : 'fa fa-heart-o'" class="mr-1"></i>
                {{ collectLoading ? '处理中...' : collected ? '已收藏' : '收藏' }}
              </button>
              <button
                class="px-5 py-2 bg-white border border-gray-200 text-gray-600 rounded-full hover:border-primary hover:text-primary transition-colors cursor-pointer text-sm"
                @click="showEvalForm = !showEvalForm"
              >
                <i class="fa fa-edit mr-1"></i>发布评测
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评测发布表单 -->
      <div v-if="showEvalForm" class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-bold text-gray-700 mb-4">
          <i class="fa fa-edit mr-2 text-primary"></i>发布评测
        </h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-600 mb-1.5">评测标题 <span class="text-red-400">*</span></label>
            <input
              v-model="evalForm.evaluationTitle"
              type="text"
              placeholder="输入评测标题，如：RTX 5090 深度评测"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-600 mb-1.5">性能测试数据</label>
            <textarea
              v-model="evalForm.perfTestData"
              rows="3"
              placeholder="输入性能测试数据，支持JSON格式"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm resize-none"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-600 mb-1.5">使用体验 <span class="text-red-400">*</span></label>
            <textarea
              v-model="evalForm.usageExperience"
              rows="4"
              placeholder="描述你的使用体验..."
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm resize-none"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-600 mb-1.5">优缺点</label>
            <textarea
              v-model="evalForm.prosAndCons"
              rows="3"
              placeholder="优点：...&#10;缺点：..."
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm resize-none"
            ></textarea>
          </div>

          <!-- 图片上传 -->
          <div>
            <label class="block text-sm font-medium text-gray-600 mb-1.5">配图（可选）</label>
            <input
              ref="uploadInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleUploadImage"
            />
            <div class="flex flex-wrap gap-2">
              <!-- 已上传的图片 -->
              <div
                v-for="(url, idx) in uploadedImages"
                :key="idx"
                class="relative w-20 h-20 rounded-lg overflow-hidden border border-gray-200 group"
              >
                <img :src="url" alt="preview" class="w-full h-full object-cover" />
                <button
                  class="absolute top-0.5 right-0.5 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center border-none cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
                  @click="removeImage(idx)"
                >
                  <i class="fa fa-times text-xs"></i>
                </button>
              </div>
              <!-- 上传按钮 -->
              <button
                v-if="uploadedImages.length < 6"
                class="w-20 h-20 rounded-lg border-2 border-dashed border-gray-300 hover:border-primary hover:text-primary transition-colors flex flex-col items-center justify-center cursor-pointer bg-transparent text-gray-400"
                :disabled="uploadingImage"
                @click="triggerUpload"
              >
                <i :class="uploadingImage ? 'fa fa-spinner fa-spin' : 'fa fa-plus'" class="text-lg"></i>
                <span class="text-xs mt-0.5">{{ uploadingImage ? '上传中' : '添加图片' }}</span>
              </button>
            </div>
            <p class="text-xs text-gray-400 mt-1">支持 JPG/PNG/WebP，单张不超过 10MB，最多 6 张</p>
            <p v-if="uploadError" class="text-xs text-red-500 mt-1">{{ uploadError }}</p>
          </div>

          <div v-if="evalError" class="bg-red-50 text-red-500 text-sm px-4 py-2.5 rounded-lg">{{ evalError }}</div>
          <div v-if="evalSuccess" class="bg-green-50 text-green-600 text-sm px-4 py-2.5 rounded-lg">{{ evalSuccess }}</div>

          <div class="flex gap-3">
            <button
              class="px-6 py-2.5 bg-primary text-white rounded-full text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors disabled:opacity-60"
              :disabled="evalSubmitting"
              @click="handleSubmitEval"
            >
              <i :class="evalSubmitting ? 'fa fa-spinner fa-spin' : 'fa fa-paper-plane'" class="mr-1.5"></i>
              {{ evalSubmitting ? '发布中...' : '提交评测' }}
            </button>
            <button
              class="px-6 py-2.5 bg-gray-100 text-gray-600 rounded-full text-sm border-none cursor-pointer hover:bg-gray-200 transition-colors"
              @click="showEvalForm = false"
            >
              取消
            </button>
          </div>
        </div>
      </div>

      <!-- 相关评测 -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold text-gray-700 m-0">
            <i class="fa fa-file-text-o mr-2 text-primary"></i>相关评测
          </h2>
          <span class="text-sm text-gray-400">{{ evaluations.length }} 篇</span>
        </div>

        <div v-if="evalLoading" class="text-center py-8">
          <i class="fa fa-spinner fa-spin text-2xl text-primary"></i>
        </div>

        <div v-else-if="evaluations.length === 0" class="text-center py-12 text-gray-400">
          <i class="fa fa-inbox text-4xl mb-3 block"></i>
          <p>暂无评测</p>
          <p class="text-sm mt-1">成为第一个评测此硬件的用户吧</p>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in evaluations"
            :key="item.evaluationId"
            class="border border-gray-100 rounded-lg p-4 hover:border-primary/30 hover:shadow-sm transition-all cursor-pointer"
            @click="router.push(`/evaluation/${item.evaluationId}`)"
          >
            <div class="flex items-start justify-between mb-2">
              <h3 class="text-base font-medium text-gray-700 m-0 hover:text-primary transition-colors">{{ item.evaluationTitle }}</h3>
              <span
                class="text-xs px-2 py-0.5 rounded-full flex-shrink-0 ml-2"
                :class="auditClasses[item.auditState] || 'bg-gray-100 text-gray-500'"
              >
                {{ auditLabels[item.auditState] || item.auditState }}
              </span>
            </div>
            <p class="text-sm text-gray-500 leading-relaxed line-clamp-3">{{ item.usageExperience }}</p>
            <div v-if="item.images && item.images.length" class="flex gap-1.5 mt-2 flex-wrap">
              <img
                v-for="(img, i) in item.images"
                :key="i"
                :src="img"
                alt="评测图片"
                class="w-16 h-16 object-cover rounded border border-gray-100 cursor-zoom-in hover:scale-110 transition-transform"
                @click.stop="openLightbox(img)"
              />
            </div>
            <div class="flex items-center text-xs text-gray-400 mt-2">
              <i class="fa fa-clock-o mr-1"></i>
              {{ item.publishTime?.substring(0, 10) }}
            </div>
          </div>
        </div>
      </div>
    </template>

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
