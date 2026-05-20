<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendations } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { RecommendInput, RecommendResult, RecommendResultItem } from '@/api/user'

const router = useRouter()

const form = ref<RecommendInput>({
  isGaming: true,
  budget: 8000,
  minCpuCores: 6,
  preferredGpuSeries: '',
  preferredMoboSize: '',
  preferredBrands: [],
})

const brandOptions = ['华硕', '微星', '技嘉', '七彩虹', 'Intel', 'AMD', 'NVIDIA']
const gpuSeriesOptions = ['RTX30系', 'RTX40系', 'RTX50系', 'RX7000系', '不限']
const moboSizeOptions = ['ATX', 'M-ATX', 'ITX', '不限']
const cpuCoreOptions = [4, 6, 8, 12, 16]

const loading = ref(false)
const result = ref<RecommendResult | null>(null)
const errorMsg = ref('')
const imageMap = ref<Record<number, string>>({})

function toggleBrand(brand: string) {
  const brands = form.value.preferredBrands || []
  const idx = brands.indexOf(brand)
  if (idx >= 0) brands.splice(idx, 1)
  else brands.push(brand)
}

async function submit() {
  loading.value = true
  errorMsg.value = ''
  result.value = null
  try {
    const data = { ...form.value }
    if (!data.preferredGpuSeries || data.preferredGpuSeries === '不限') data.preferredGpuSeries = ''
    if (!data.preferredMoboSize || data.preferredMoboSize === '不限') data.preferredMoboSize = ''
    result.value = await getRecommendations(data)
    if (result.value?.imageMap) imageMap.value = result.value.imageMap
  } catch {
    errorMsg.value = '请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function scoreColor(score: number) {
  if (score >= 80) return 'text-green-500'
  if (score >= 50) return 'text-primary'
  return 'text-gray-400'
}
function scoreBg(score: number) {
  if (score >= 80) return 'bg-green-500'
  if (score >= 50) return 'bg-primary'
  return 'bg-gray-400'
}
function hwImage(id: number, name: string) {
  return imageMap.value[id] || getHardwareImage(name)
}
function goToHardware(id: number) {
  router.push(`/hardware/${id}`)
}
</script>

<template>
  <div class="container mx-auto px-4 py-6 max-w-6xl">
    <h1 class="text-2xl font-bold text-gray-700 mb-1">
      <i class="fa fa-cogs text-primary mr-2"></i>PC配置推荐
    </h1>
    <p class="text-sm text-gray-400 mb-6">根据需求智能匹配 CPU + 显卡 + 主板 组合方案</p>

    <div class="grid grid-cols-1 lg:grid-cols-7 gap-6">
      <!-- 左侧表单 -->
      <div class="lg:col-span-3">
        <div class="bg-white rounded-xl shadow-sm p-5 sticky top-20">
          <div class="mb-5">
            <label class="block text-sm font-bold text-gray-600 mb-2">使用场景 <span class="text-red-400">*</span></label>
            <div class="flex gap-2">
              <button
                class="flex-1 py-2.5 rounded-lg text-sm font-medium border-2 transition-all cursor-pointer"
                :class="form.isGaming ? 'border-primary bg-primary/5 text-primary' : 'border-gray-200 bg-white text-gray-400 hover:border-gray-300'"
                @click="form.isGaming = true"
              ><i class="fa fa-gamepad mr-1"></i>游戏</button>
              <button
                class="flex-1 py-2.5 rounded-lg text-sm font-medium border-2 transition-all cursor-pointer"
                :class="!form.isGaming ? 'border-primary bg-primary/5 text-primary' : 'border-gray-200 bg-white text-gray-400 hover:border-gray-300'"
                @click="form.isGaming = false"
              ><i class="fa fa-desktop mr-1"></i>办公</button>
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-bold text-gray-600 mb-1.5">预算上限（元）<span class="text-red-400">*</span></label>
            <div class="relative">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">¥</span>
              <input v-model.number="form.budget" type="number" min="2000" max="100000" step="500"
                class="w-full pl-8 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-sm" />
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-bold text-gray-600 mb-1.5">最小CPU核心数<span class="text-red-400">*</span></label>
            <div class="flex gap-2 flex-wrap">
              <button v-for="n in cpuCoreOptions" :key="n"
                class="px-3 py-1.5 rounded-full text-sm border transition-all cursor-pointer"
                :class="form.minCpuCores === n ? 'border-primary bg-primary text-white' : 'border-gray-200 text-gray-500 hover:border-gray-300 bg-white'"
                @click="form.minCpuCores = n">{{ n }} 核</button>
            </div>
          </div>

          <div class="border-t border-gray-100 my-4"></div>
          <p class="text-xs text-gray-400 mb-3">以下为软性偏好（可选，用于排序优化）</p>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-600 mb-1.5">显卡系列偏好</label>
            <select v-model="form.preferredGpuSeries"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 bg-white">
              <option value="">不限</option>
              <option v-for="s in gpuSeriesOptions.filter(s => s !== '不限')" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-600 mb-1.5">主板尺寸偏好</label>
            <select v-model="form.preferredMoboSize"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 bg-white">
              <option value="">不限</option>
              <option v-for="s in moboSizeOptions.filter(s => s !== '不限')" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>

          <div class="mb-5">
            <label class="block text-sm font-medium text-gray-600 mb-1.5">品牌偏好（可多选）</label>
            <div class="flex flex-wrap gap-1.5">
              <button v-for="b in brandOptions" :key="b"
                class="px-2.5 py-1 rounded-full text-xs border transition-all cursor-pointer"
                :class="(form.preferredBrands || []).includes(b) ? 'border-primary bg-primary/10 text-primary' : 'border-gray-200 text-gray-400 hover:border-gray-300 bg-white'"
                @click="toggleBrand(b)">{{ b }}</button>
            </div>
          </div>

          <button
            class="w-full py-2.5 bg-primary text-white rounded-lg text-sm font-medium border-none cursor-pointer hover:bg-primary/90 transition-colors disabled:opacity-60"
            :disabled="loading" @click="submit">
            <i :class="loading ? 'fa fa-spinner fa-spin' : 'fa fa-search'" class="mr-1.5"></i>
            {{ loading ? '分析中...' : '开始推荐' }}
          </button>
          <p v-if="errorMsg" class="text-red-500 text-xs mt-2 text-center">{{ errorMsg }}</p>
        </div>
      </div>

      <!-- 右侧结果 -->
      <div class="lg:col-span-4">
        <div v-if="!result && !loading" class="bg-white rounded-xl shadow-sm p-12 text-center">
          <i class="fa fa-lightbulb-o text-5xl text-gray-300 mb-4 block"></i>
          <p class="text-gray-400">设置需求，点击"开始推荐"查看方案</p>
        </div>

        <div v-if="loading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse">
            <div class="h-5 bg-gray-200 rounded w-1/2 mb-3"></div>
            <div class="h-3 bg-gray-100 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-gray-100 rounded w-2/3"></div>
          </div>
        </div>

        <div v-if="result && !result.passed" class="bg-white rounded-xl shadow-sm p-8">
          <div class="text-center mb-4">
            <i class="fa fa-frown-o text-4xl text-orange-300 mb-3 block"></i>
            <h3 class="text-lg font-bold text-gray-600">未找到匹配配置</h3>
          </div>
          <div class="bg-orange-50 border border-orange-200 rounded-lg p-4 text-sm text-gray-600 leading-relaxed">
            {{ result.suggestion }}
          </div>
        </div>

        <div v-if="result && result.passed && result.results.length" class="space-y-4">
          <div class="text-sm text-gray-400">共 {{ result.totalCandidates }} 个匹配方案，展示最优 {{ result.results.length }} 个</div>

          <div v-for="(item, idx) in result.results" :key="idx"
            class="bg-white rounded-xl shadow-sm overflow-hidden">
            <!-- 排名头 -->
            <div class="flex items-center gap-3 p-4 pb-2">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-bold flex-shrink-0"
                :class="idx === 0 ? 'bg-secondary' : idx === 1 ? 'bg-primary' : 'bg-gray-400'">{{ idx + 1 }}</div>
              <div class="flex-1">
                <div class="flex items-center gap-2 text-xs text-gray-400">
                  <span>{{ item.cpuName?.split(' ').pop() }}</span>
                  <span>+</span>
                  <span>{{ item.gpuName?.split(' ').slice(-2).join(' ') || '核显' }}</span>
                  <span>+</span>
                  <span>{{ item.mbName?.split(' ').pop() }}</span>
                </div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-bold text-secondary">¥{{ item.totalPrice }}</div>
                <div class="flex items-center gap-2 mt-0.5">
                  <div class="w-20 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all" :class="scoreBg(item.score)" :style="{ width: item.score + '%' }"></div>
                  </div>
                  <span class="text-xs font-bold" :class="scoreColor(item.score)">{{ item.score }}%</span>
                </div>
              </div>
            </div>

            <!-- 匹配标签 -->
            <div class="px-4 pb-1">
              <div class="flex flex-wrap gap-1.5">
                <span v-for="h in item.hitDetails" :key="h" class="text-xs px-2 py-0.5 rounded-full bg-green-50 text-green-600"><i class="fa fa-check mr-0.5"></i>{{ h }}</span>
                <span v-for="m in item.missDetails" :key="m" class="text-xs px-2 py-0.5 rounded-full bg-gray-50 text-gray-400"><i class="fa fa-minus mr-0.5"></i>{{ m }}</span>
              </div>
            </div>

            <!-- 三件套详情 -->
            <div class="grid grid-cols-3 gap-3 p-4 pt-2">
              <!-- CPU -->
              <div class="bg-gray-50 rounded-lg p-3 cursor-pointer hover:bg-gray-100 transition-colors" @click="goToHardware(item.cpuId)">
                <img :src="hwImage(item.cpuId, item.cpuName)" alt="CPU" class="w-full h-20 object-cover rounded mb-2" />
                <div class="text-xs text-gray-400">CPU</div>
                <div class="text-sm font-medium text-gray-700 truncate" :title="item.cpuName">{{ item.cpuName }}</div>
                <div class="text-xs text-gray-400 mt-0.5">{{ item.cpuCores }}核 · ¥{{ item.cpuPrice }}</div>
              </div>
              <!-- GPU -->
              <div v-if="item.gpuId" class="bg-gray-50 rounded-lg p-3 cursor-pointer hover:bg-gray-100 transition-colors" @click="goToHardware(item.gpuId)">
                <img :src="hwImage(item.gpuId, item.gpuName!)" alt="GPU" class="w-full h-20 object-cover rounded mb-2" />
                <div class="text-xs text-gray-400">显卡</div>
                <div class="text-sm font-medium text-gray-700 truncate" :title="item.gpuName!">{{ item.gpuName }}</div>
                <div class="text-xs text-gray-400 mt-0.5">{{ item.gpuSeries }} · ¥{{ item.gpuPrice }}</div>
              </div>
              <div v-else class="bg-gray-50 rounded-lg p-3 flex items-center justify-center">
                <span class="text-gray-400 text-xs">核显方案</span>
              </div>
              <!-- 主板 -->
              <div class="bg-gray-50 rounded-lg p-3 cursor-pointer hover:bg-gray-100 transition-colors" @click="goToHardware(item.mbId)">
                <img :src="hwImage(item.mbId, item.mbName)" alt="MB" class="w-full h-20 object-cover rounded mb-2" />
                <div class="text-xs text-gray-400">主板</div>
                <div class="text-sm font-medium text-gray-700 truncate" :title="item.mbName">{{ item.mbName }}</div>
                <div class="text-xs text-gray-400 mt-0.5">{{ item.mbForm }} · ¥{{ item.mbPrice }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
