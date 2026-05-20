<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRanking } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { RankingData, RankItem } from '@/api/user'

const router = useRouter()
const activeTab = ref<'cpu' | 'gpu' | 'mb'>('cpu')
const data = ref<RankingData | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    data.value = await getRanking()
  } catch {
    data.value = null
  } finally {
    loading.value = false
  }
})

function goToHardware(id: number) {
  router.push(`/hardware/${id}`)
}

function rankMedal(idx: number) {
  if (idx === 0) return 'bg-secondary'
  if (idx === 1) return 'bg-primary'
  if (idx === 2) return 'bg-orange-400'
  return 'bg-gray-300'
}
</script>

<template>
  <div class="container mx-auto px-4 py-6 max-w-5xl">
    <h1 class="text-2xl font-bold text-gray-700 mb-1">
      <i class="fa fa-trophy text-secondary mr-2"></i>硬件推荐榜
    </h1>
    <p class="text-sm text-gray-400 mb-6">基于性价比综合评分，实时排序</p>

    <!-- Tab -->
    <div class="bg-white rounded-xl shadow-sm mb-6">
      <div class="flex border-b border-gray-200">
        <button
          v-for="tab in [
            { key: 'cpu', label: 'CPU 处理器', icon: 'fa-microchip' },
            { key: 'gpu', label: '显卡', icon: 'fa-picture-o' },
            { key: 'mb', label: '主板', icon: 'fa-server' },
          ]" :key="tab.key"
          class="flex-1 py-3.5 text-center text-sm font-medium border-none bg-transparent cursor-pointer transition-all duration-200 relative"
          :class="activeTab === tab.key ? 'text-primary' : 'text-gray-400 hover:text-gray-600'"
          @click="activeTab = tab.key as any"
        >
          <i :class="'fa ' + tab.icon + ' mr-1.5'"></i>{{ tab.label }}
          <span v-if="activeTab === tab.key" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary rounded-full"></span>
        </button>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="p-6">
        <div class="animate-pulse space-y-4">
          <div v-for="i in 8" :key="i" class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-gray-200"></div>
            <div class="w-14 h-14 rounded bg-gray-200"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-200 rounded w-2/3"></div>
              <div class="h-3 bg-gray-100 rounded w-1/3"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空 -->
      <div v-else-if="!data" class="p-12 text-center text-gray-400">
        <i class="fa fa-exclamation-circle text-3xl mb-3 block"></i>
        数据加载失败
      </div>

      <!-- CPU榜 -->
      <template v-else-if="activeTab === 'cpu'">
        <div class="p-4">
          <div class="text-xs text-gray-400 mb-3 flex items-center gap-6 px-2">
            <span class="w-10 text-center">排名</span>
            <span class="flex-1">型号</span>
            <span class="w-16 text-center">核心</span>
            <span class="w-16 text-center">频率</span>
            <span class="w-20 text-right">价格</span>
            <span class="w-16 text-right">评分</span>
          </div>
          <div
            v-for="(item, idx) in data.cpuRanking"
            :key="item.hardwareId"
            class="flex items-center gap-3 p-2 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
            @click="goToHardware(item.hardwareId)"
          >
            <div class="w-10 flex justify-center flex-shrink-0">
              <span
                class="w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold"
                :class="rankMedal(idx)"
              >{{ idx + 1 }}</span>
            </div>
            <img
              :src="item.imageUrl || getHardwareImage(item.hardwareName)"
              class="w-12 h-12 object-cover rounded flex-shrink-0"
            />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-700 truncate">{{ item.hardwareName }}</div>
              <div class="text-xs text-gray-400">{{ item.hardwareBrand }} · {{ item.interfaceType }}</div>
            </div>
            <span class="w-16 text-center text-xs text-gray-600 flex-shrink-0">{{ item.coreCount }}核{{ item.threadCount }}线</span>
            <span class="w-16 text-center text-xs text-gray-600 flex-shrink-0">{{ item.baseFreq }}GHz</span>
            <span class="w-20 text-right text-sm font-bold text-secondary flex-shrink-0">¥{{ item.hardwarePrice }}</span>
            <span class="w-16 text-right text-xs font-bold flex-shrink-0" :class="item.score >= 5 ? 'text-green-500' : item.score >= 3 ? 'text-primary' : 'text-gray-400'">{{ item.score }}</span>
          </div>
        </div>
      </template>

      <!-- GPU榜 -->
      <template v-else-if="activeTab === 'gpu'">
        <div class="p-4">
          <div class="text-xs text-gray-400 mb-3 flex items-center gap-6 px-2">
            <span class="w-10 text-center">排名</span>
            <span class="flex-1">型号</span>
            <span class="w-16 text-center">显存</span>
            <span class="w-16 text-center">功耗</span>
            <span class="w-20 text-right">价格</span>
            <span class="w-16 text-right">评分</span>
          </div>
          <div
            v-for="(item, idx) in data.gpuRanking"
            :key="item.hardwareId"
            class="flex items-center gap-3 p-2 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
            @click="goToHardware(item.hardwareId)"
          >
            <div class="w-10 flex justify-center flex-shrink-0">
              <span class="w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold" :class="rankMedal(idx)">{{ idx + 1 }}</span>
            </div>
            <img :src="item.imageUrl || getHardwareImage(item.hardwareName)" class="w-12 h-12 object-cover rounded flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-700 truncate">{{ item.hardwareName }}</div>
              <div class="text-xs text-gray-400">{{ item.hardwareBrand }} · {{ item.coreModel }}</div>
            </div>
            <span class="w-16 text-center text-xs text-gray-600 flex-shrink-0">{{ item.vramCap }}</span>
            <span class="w-16 text-center text-xs text-gray-600 flex-shrink-0">{{ item.powerConsump }}W</span>
            <span class="w-20 text-right text-sm font-bold text-secondary flex-shrink-0">¥{{ item.hardwarePrice }}</span>
            <span class="w-16 text-right text-xs font-bold flex-shrink-0" :class="item.score >= 2 ? 'text-green-500' : item.score >= 1 ? 'text-primary' : 'text-gray-400'">{{ item.score }}</span>
          </div>
        </div>
      </template>

      <!-- 主板榜 -->
      <template v-else>
        <div class="p-4">
          <div class="text-xs text-gray-400 mb-3 flex items-center gap-6 px-2">
            <span class="w-10 text-center">排名</span>
            <span class="flex-1">型号</span>
            <span class="w-16 text-center">板型</span>
            <span class="w-20 text-center">接口/插槽</span>
            <span class="w-20 text-right">价格</span>
            <span class="w-16 text-right">评分</span>
          </div>
          <div
            v-for="(item, idx) in data.mbRanking"
            :key="item.hardwareId"
            class="flex items-center gap-3 p-2 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
            @click="goToHardware(item.hardwareId)"
          >
            <div class="w-10 flex justify-center flex-shrink-0">
              <span class="w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold" :class="rankMedal(idx)">{{ idx + 1 }}</span>
            </div>
            <img :src="item.imageUrl || getHardwareImage(item.hardwareName)" class="w-12 h-12 object-cover rounded flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-700 truncate">{{ item.hardwareName }}</div>
              <div class="text-xs text-gray-400">{{ item.hardwareBrand }} · {{ item.cpuInterface }}</div>
            </div>
            <span class="w-16 text-center text-xs text-gray-600 flex-shrink-0">{{ item.mbForm }}</span>
            <span class="w-20 text-center text-xs text-gray-600 flex-shrink-0">{{ item.memSlotCount }}槽 / {{ item.m2SlotCount }}×M.2</span>
            <span class="w-20 text-right text-sm font-bold text-secondary flex-shrink-0">¥{{ item.hardwarePrice }}</span>
            <span class="w-16 text-right text-xs font-bold flex-shrink-0" :class="item.score >= 10 ? 'text-green-500' : item.score >= 6 ? 'text-primary' : 'text-gray-400'">{{ item.score }}</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
