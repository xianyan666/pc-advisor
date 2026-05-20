<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { testHardware } from '@/api/common'
import { getHardwareImage } from '@/utils/imageMapping'

const router = useRouter()
const route = useRoute()

const hardwareList = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const typeFilter = ref((route.query.type as string) || '')
const searchKeyword = ref('')

const types = ['CPU', 'GRAPHICS_CARD', 'MOTHERBOARD']

const typeLabels: Record<string, string> = {
  'CPU': 'CPU 处理器',
  'GRAPHICS_CARD': '显卡',
  'MOTHERBOARD': '主板',
}

const filteredList = computed(() => {
  let list = hardwareList.value
  if (typeFilter.value) {
    list = list.filter((h) => h.HardwareType === typeFilter.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(
      (h) =>
        String(h.HardwareName).toLowerCase().includes(kw) ||
        String(h.HardwareBrand).toLowerCase().includes(kw),
    )
  }
  return list
})

function getParams(item: Record<string, unknown>): { label: string; value: string }[] {
  const t = String(item.HardwareType || '')
  switch (t) {
    case 'CPU':
      return [
        { label: '核心', value: item.coreCount ? `${item.coreCount}核${item.threadCount || ''}线程` : '-' },
        { label: '频率', value: item.baseFreq ? `${item.baseFreq} GHz` : '-' },
        { label: 'TDP', value: item.tdpPower ? `${item.tdpPower}W` : '-' },
      ]
    case 'GRAPHICS_CARD':
      return [
        { label: '显存', value: item.vramCap ? `${item.vramCap} ${item.vramType || ''}`.trim() : '-' },
        { label: '核心', value: String(item.coreModel || '-') },
        { label: '功耗', value: item.powerConsump ? `${item.powerConsump}W` : '-' },
      ]
    case 'MOTHERBOARD':
      return [
        { label: '板型', value: String(item.mbForm || '-') },
        { label: '内存槽', value: item.memSlotCount ? `${item.memSlotCount}个` : '-' },
        { label: 'M.2', value: item.m2SlotCount ? `${item.m2SlotCount}个` : '-' },
      ]
    default:
      return [
        { label: '品牌', value: String(item.HardwareBrand || '-') },
        { label: '型号', value: String(item.HardwareModel || '-') },
      ]
  }
}

function goDetail(id: unknown) {
  router.push(`/hardware/${id}`)
}

async function loadHardware() {
  loading.value = true
  try {
    hardwareList.value = await testHardware()
  } catch {
    hardwareList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadHardware)
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <!-- 顶部工具栏 -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-700 m-0">DIY硬件</h1>
        <p class="text-sm text-gray-400 mt-1">浏览、对比、选择最适合你的硬件</p>
      </div>
      <div class="flex flex-col sm:flex-row gap-3 w-full md:w-auto">
        <div class="relative flex-1 sm:w-64">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索硬件名称或品牌..."
            class="w-full py-2 px-4 pl-10 rounded-full border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm"
          />
          <i class="fa fa-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"></i>
        </div>
        <select
          v-model="typeFilter"
          class="py-2 px-4 rounded-full border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-sm bg-white text-gray-600"
        >
          <option value="">全部类型</option>
          <option v-for="t in types" :key="t" :value="t">{{ typeLabels[t] || t }}</option>
        </select>
        <button
          class="py-2 px-6 bg-primary text-white rounded-full hover:bg-primary/90 transition-colors border-none cursor-pointer text-sm font-medium"
          @click="loadHardware"
          :disabled="loading"
        >
          <i class="fa fa-refresh mr-1" :class="{ 'fa-spin': loading }"></i>
          {{ loading ? '加载中' : '刷新' }}
        </button>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button
        class="px-4 py-1.5 rounded-full text-sm border cursor-pointer transition-all bg-white"
        :class="
          typeFilter === ''
            ? 'border-primary text-primary bg-primary/5 font-medium'
            : 'border-gray-200 text-gray-500 hover:border-primary hover:text-primary'
        "
        @click="typeFilter = ''"
      >
        <i class="fa fa-th-large mr-1"></i>全部
      </button>
      <button
        v-for="t in types"
        :key="t"
        class="px-4 py-1.5 rounded-full text-sm border cursor-pointer transition-all bg-white"
        :class="
          typeFilter === t
            ? 'border-primary text-primary bg-primary/5 font-medium'
            : 'border-gray-200 text-gray-500 hover:border-primary hover:text-primary'
        "
        @click="typeFilter = typeFilter === t ? '' : t"
      >
        {{ typeLabels[t] || t }}
      </button>
    </div>

    <!-- 结果统计 -->
    <div v-if="!loading" class="text-sm text-gray-400 mb-4">
      共 <span class="text-gray-600 font-medium">{{ filteredList.length }}</span> 款硬件
      <span v-if="typeFilter || searchKeyword">（已筛选）</span>
    </div>

    <!-- 硬件卡片网格 -->
    <div v-if="filteredList.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="item in filteredList"
        :key="item.HardwareID"
        class="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100 hover:shadow-md transition-all duration-300 flex flex-col cursor-pointer"
        @click="goDetail(item.HardwareID)"
      >
        <!-- 图片区 -->
        <div class="relative">
          <img
            :src="(item.imageUrl as string) || getHardwareImage(String(item.HardwareName))"
            :alt="String(item.HardwareName)"
            class="w-full h-44 object-cover"
          />
          <span class="absolute top-2 left-2 bg-primary/85 text-white text-xs px-2.5 py-1 rounded-full">
            {{ typeLabels[String(item.HardwareType)] || item.HardwareType }}
          </span>
          <!-- 评测数 -->
          <span
            v-if="(item.evaluationCount as number) > 0"
            class="absolute top-2 right-2 bg-white/90 text-primary text-xs px-2 py-1 rounded-full shadow-sm"
          >
            <i class="fa fa-file-text-o mr-1"></i>{{ item.evaluationCount }} 篇评测
          </span>
        </div>

        <!-- 信息区 -->
        <div class="p-4 flex flex-col flex-1">
          <h3 class="font-semibold text-gray-700 mb-1 line-clamp-1" :title="String(item.HardwareName)">
            {{ item.HardwareName }}
          </h3>
          <div class="flex items-center gap-1 text-xs text-gray-400 mb-3">
            <span>{{ item.HardwareBrand }}</span>
            <span v-if="item.HardwareModel">· {{ item.HardwareModel }}</span>
          </div>

          <!-- 参数区 -->
          <div class="bg-gray-50 rounded-lg p-3 mb-3 flex-1">
            <div class="grid grid-cols-3 gap-2">
              <div v-for="p in getParams(item)" :key="p.label" class="text-center">
                <div class="text-xs text-gray-400 mb-0.5">{{ p.label }}</div>
                <div class="text-sm font-medium text-gray-700 truncate" :title="p.value">{{ p.value }}</div>
              </div>
            </div>
          </div>

          <!-- 价格 + 操作 -->
          <div class="flex items-center justify-between">
            <div>
              <span class="text-secondary font-bold text-lg">¥{{ item.HardwarePrice }}</span>
            </div>
            <div class="flex gap-1.5">
              <!-- 查看评测 -->
              <button
                class="text-xs px-2.5 py-1.5 rounded-full bg-primary/10 text-primary hover:bg-primary hover:text-white transition-colors border-none cursor-pointer font-medium"
                title="查看评测"
                @click.stop="goDetail(item.HardwareID)"
              >
                <i class="fa fa-file-text-o mr-1"></i>评测
              </button>
              <button
                class="text-gray-400 hover:text-red-500 transition-colors border-none bg-transparent cursor-pointer w-8 h-8 flex items-center justify-center"
                title="收藏"
                @click.stop
              >
                <i class="fa fa-heart-o"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="text-center text-gray-400 py-20">
      <i class="fa fa-inbox text-5xl mb-4 block"></i>
      <p class="text-lg">暂未加载到硬件数据</p>
      <p class="text-sm mt-1">请确认数据库连接正常，或点击刷新按钮重试</p>
    </div>

    <!-- 加载骨架屏 -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div v-for="n in 8" :key="n" class="bg-white rounded-xl shadow-sm overflow-hidden animate-pulse">
        <div class="w-full h-44 bg-gray-200"></div>
        <div class="p-4">
          <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
          <div class="h-3 bg-gray-100 rounded w-1/2 mb-3"></div>
          <div class="h-14 bg-gray-100 rounded mb-3"></div>
          <div class="h-6 bg-gray-200 rounded w-1/3"></div>
        </div>
      </div>
    </div>
  </div>
</template>
