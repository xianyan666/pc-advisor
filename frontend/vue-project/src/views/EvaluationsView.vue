<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllEvaluations } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { AllEvaluationItem } from '@/api/user'

const router = useRouter()

const evaluations = ref<AllEvaluationItem[]>([])
const loading = ref(true)

const typeLabels: Record<string, string> = {
  CPU: 'CPU',
  GRAPHICS_CARD: '显卡',
  MOTHERBOARD: '主板',
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

onMounted(async () => {
  try {
    evaluations.value = await getAllEvaluations()
  } catch {
    evaluations.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container mx-auto px-4 py-6 max-w-5xl">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-700">
        <i class="fa fa-file-text-o text-primary mr-2"></i>硬件评测
      </h1>
      <p class="text-sm text-gray-400 mt-1">浏览所有硬件评测，了解真实使用体验</p>
    </div>

    <!-- 加载中 -->
    <template v-if="loading">
      <div class="animate-pulse space-y-4">
        <div v-for="i in 4" :key="i" class="bg-white rounded-xl p-4 flex gap-4">
          <div class="w-24 h-24 bg-gray-200 rounded-lg flex-shrink-0"></div>
          <div class="flex-1 space-y-2">
            <div class="h-5 bg-gray-200 rounded w-2/3"></div>
            <div class="h-3 bg-gray-100 rounded w-1/3"></div>
            <div class="h-12 bg-gray-100 rounded"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 空状态 -->
    <template v-else-if="evaluations.length === 0">
      <div class="text-center py-20">
        <i class="fa fa-inbox text-5xl text-gray-300 mb-4 block"></i>
        <p class="text-gray-400 text-lg">暂无评测</p>
        <p class="text-sm text-gray-300 mt-1">成为第一个发布评测的用户吧</p>
      </div>
    </template>

    <!-- 评测列表 -->
    <template v-else>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="item in evaluations"
          :key="item.evaluationId"
          class="bg-white rounded-xl shadow-sm p-5 hover:shadow-md hover:-translate-y-0.5 transition-all cursor-pointer"
          @click="router.push(`/evaluation/${item.evaluationId}`)"
        >
          <!-- 标题 -->
          <h3 class="text-base font-bold text-gray-700 mb-2 line-clamp-2 leading-snug hover:text-primary transition-colors">
            {{ item.evaluationTitle }}
          </h3>

          <!-- 摘要 -->
          <p class="text-sm text-gray-500 line-clamp-2 mb-3 leading-relaxed">
            {{ item.usageExperience }}
          </p>

          <!-- 关联硬件 -->
          <div v-if="item.hardware" class="flex items-center gap-3 mb-3 bg-gray-50 rounded-lg p-2">
            <img
              :src="item.hardware.imageUrl || getHardwareImage(item.hardware.hardwareName)"
              :alt="item.hardware.hardwareName"
              class="w-10 h-10 object-cover rounded flex-shrink-0"
            />
            <div class="flex-1 min-w-0">
              <span class="inline-block text-xs px-1.5 py-0.5 rounded bg-primary/10 text-primary mb-0.5">
                {{ typeLabels[item.hardware.hardwareType] || item.hardware.hardwareType }}
              </span>
              <p class="text-xs text-gray-600 truncate">{{ item.hardware.hardwareName }}</p>
            </div>
          </div>

          <!-- 配图预览 -->
          <div v-if="item.images && item.images.length" class="flex gap-1.5 mb-3">
            <img
              v-for="(img, i) in item.images.slice(0, 3)"
              :key="i"
              :src="img"
              alt="配图"
              class="w-14 h-14 object-cover rounded border border-gray-100 cursor-zoom-in hover:scale-110 transition-transform"
              @click.stop="openLightbox(img)"
            />
            <div
              v-if="item.images.length > 3"
              class="w-14 h-14 rounded bg-gray-100 flex items-center justify-center text-xs text-gray-400"
            >
              +{{ item.images.length - 3 }}
            </div>
          </div>

          <!-- 底部信息 -->
          <div class="flex items-center justify-between text-xs text-gray-400">
            <span><i class="fa fa-user-circle-o mr-1"></i>{{ item.publisherName }}</span>
            <span><i class="fa fa-clock-o mr-1"></i>{{ item.publishTime?.substring(0, 10) }}</span>
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
