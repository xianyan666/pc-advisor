<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getHardwareImage } from '@/utils/imageMapping'
import { getAllEvaluations } from '@/api/user'
import type { AllEvaluationItem } from '@/api/user'
import Chart from 'chart.js/auto'

const router = useRouter()
const authStore = useAuthStore()

function goToHardware(name: string) {
  router.push({ path: '/hardware-detail', query: { name } })
}

// 轮播图
const slides = [
  {
    img: '/images/products/GeForce RTX 5090.webp',
    title: 'RTX 5090 震撼上市',
    desc: '全新架构，性能翻倍，游戏体验再升级',
    link: '#',
    linkText: '了解详情',
  },
  {
    img: '/images/products/ASUS ROG Maximus Z890 Hero.webp',
    title: '高端主板选购指南',
    desc: '2025年最值得买的高性能主板推荐',
    link: '#',
    linkText: '查看榜单',
  },
]
const currentSlide = ref(0)
let slideTimer: ReturnType<typeof setInterval> | null = null

function showSlide(index: number) {
  currentSlide.value = index
}
function nextSlide() {
  currentSlide.value = (currentSlide.value + 1) % slides.length
}
function prevSlide() {
  currentSlide.value = (currentSlide.value - 1 + slides.length) % slides.length
}
function goToSlide(index: number) {
  if (slideTimer) clearInterval(slideTimer)
  showSlide(index)
  slideTimer = setInterval(nextSlide, 5000)
}
function slidePrev() {
  if (slideTimer) clearInterval(slideTimer)
  prevSlide()
  slideTimer = setInterval(nextSlide, 5000)
}
function slideNext() {
  if (slideTimer) clearInterval(slideTimer)
  nextSlide()
  slideTimer = setInterval(nextSlide, 5000)
}

// 硬件分类
const categories = [
  { icon: 'fa-microchip', label: 'CPU', type: 'CPU' },
  { icon: 'fa-picture-o', label: '显卡', type: 'GRAPHICS_CARD' },
  { icon: 'fa-server', label: '主板', type: 'MOTHERBOARD' },
]

// 热门产品
const hotProducts = [
  { img: 'https://picsum.photos/id/96/400/300', badge: '新品', badgeClass: 'bg-secondary', title: 'NVIDIA GeForce RTX 5090 24GB 旗舰显卡', rating: 4.8, reviews: 126, price: 9999, oldPrice: 11999 },
  { img: 'https://picsum.photos/id/160/400/300', badge: '热销', badgeClass: 'bg-primary', title: 'Intel 酷睿 i9-14900K 处理器', rating: 4.2, reviews: 89, price: 4299, oldPrice: 4599 },
  { img: 'https://picsum.photos/id/20/400/300', badge: '', badgeClass: '', title: '华硕 ROG Maximus Z790 高端主板', rating: 4.9, reviews: 235, price: 3299, oldPrice: 3599 },
  { img: 'https://picsum.photos/id/180/400/300', badge: '秒杀', badgeClass: 'bg-red-500', title: 'AMD Ryzen 9 7950X 处理器', rating: 4.7, reviews: 312, price: 3999, oldPrice: 4599 },
]

// 排行榜
const rankings = [
  { rank: 1, img: 'https://picsum.photos/id/180/100/100', title: 'AMD Ryzen 7 7800X3D 处理器', price: 2499 },
  { rank: 2, img: 'https://picsum.photos/id/96/100/100', title: '华硕 ROG Strix RTX 4070 Ti', price: 6299 },
  { rank: 3, img: 'https://picsum.photos/id/20/100/100', title: '微星 MPG Z790 EDGE WIFI 主板', price: 2199 },
  { rank: 4, img: 'https://picsum.photos/id/160/100/100', title: 'Intel 酷睿 i7-14700K 处理器', price: 3299 },
  { rank: 5, img: 'https://picsum.photos/id/96/100/100', title: '七彩虹 RTX 5080 显卡', price: 7999 },
]

function rankBadgeClass(rank: number) {
  if (rank === 1) return 'bg-secondary'
  if (rank === 2) return 'bg-gray-300'
  if (rank === 3) return 'bg-orange-600/70'
  return 'bg-gray-400'
}

// 首页评测
const homeEvaluations = ref<AllEvaluationItem[]>([])

// 新品
const newProducts = [
  { img: 'https://picsum.photos/id/180/300/300', title: 'AMD Ryzen 9 9950X3D 处理器', price: 5999 },
  { img: 'https://picsum.photos/id/96/300/300', title: 'NVIDIA GeForce RTX 5080 显卡', price: 7999 },
  { img: 'https://picsum.photos/id/20/300/300', title: '华硕 ROG Maximus Z890 主板', price: 3299 },
  { img: 'https://picsum.photos/id/160/300/300', title: 'Intel 酷睿 i9-14900KS 处理器', price: 4599 },
  { img: 'https://picsum.photos/id/96/300/300', title: 'AMD Radeon RX 9070 XT 显卡', price: 5499 },
  { img: 'https://picsum.photos/id/20/300/300', title: '微星 MPG Z890 Carbon WiFi 主板', price: 2999 },
]

// 返回顶部
const showBackTop = ref(false)
function onScroll() {
  showBackTop.value = window.scrollY > 300
}
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 图表
let chartInstance: Chart | null = null
function initChart() {
  const canvas = document.getElementById('priceChart') as HTMLCanvasElement | null
  if (!canvas) return
  chartInstance = new Chart(canvas, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
      datasets: [
        {
          label: '显卡价格',
          data: [8500, 8200, 7900, 7600, 7300, 6900],
          borderColor: '#165DFF',
          backgroundColor: 'rgba(22, 93, 255, 0.1)',
          tension: 0.3,
          fill: true,
        },
        {
          label: 'CPU价格',
          data: [4200, 4100, 3900, 3800, 3700, 3500],
          borderColor: '#FF7D00',
          backgroundColor: 'rgba(255, 125, 0, 0.1)',
          tension: 0.3,
          fill: true,
        },
        {
          label: '主板价格',
          data: [2800, 2750, 2680, 2600, 2550, 2499],
          borderColor: '#36CFC9',
          backgroundColor: 'rgba(54, 207, 201, 0.1)',
          tension: 0.3,
          fill: true,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top', labels: { font: { size: 11 }, boxWidth: 10 } },
      },
      scales: {
        y: { beginAtZero: false },
        x: { grid: { display: false } },
      },
    },
  })
}

onMounted(async () => {
  showSlide(0)
  slideTimer = setInterval(nextSlide, 5000)
  window.addEventListener('scroll', onScroll)
  initChart()
  try {
    const list = await getAllEvaluations()
    homeEvaluations.value = (list || []).slice(0, 4)
  } catch { /* ignore */ }
})

onUnmounted(() => {
  if (slideTimer) clearInterval(slideTimer)
  if (chartInstance) chartInstance.destroy()
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div>
    <!-- 轮播图 -->
    <section class="relative rounded-xl overflow-hidden mb-8 mx-4 mt-4">
      <div class="carousel relative h-[300px] md:h-[400px] lg:h-[500px]">
        <div
          v-for="(slide, idx) in slides"
          :key="idx"
          class="absolute inset-0 transition-opacity duration-500"
          :class="currentSlide === idx ? 'opacity-100' : 'opacity-0 pointer-events-none'"
        >
          <img :src="slide.img" :alt="slide.title" class="w-full h-full object-cover">
          <div class="absolute inset-0 bg-gradient-to-r from-black/60 to-transparent flex items-center">
            <div class="text-white ml-6 md:ml-12 max-w-md">
              <h2 class="text-2xl md:text-4xl font-bold mb-2 text-shadow">{{ slide.title }}</h2>
              <p class="text-sm md:text-base mb-4 text-gray-100">{{ slide.desc }}</p>
              <a :href="slide.link" class="inline-block bg-secondary hover:bg-secondary/90 text-white no-underline px-4 py-2 rounded-md transition-colors">
                {{ slide.linkText }} <i class="fa fa-angle-right ml-1"></i>
              </a>
            </div>
          </div>
        </div>

        <button class="absolute left-2 top-1/2 -translate-y-1/2 bg-black/30 hover:bg-black/50 text-white w-8 h-8 rounded-full flex items-center justify-center transition-colors border-none cursor-pointer" @click="slidePrev">
          <i class="fa fa-angle-left text-xl"></i>
        </button>
        <button class="absolute right-2 top-1/2 -translate-y-1/2 bg-black/30 hover:bg-black/50 text-white w-8 h-8 rounded-full flex items-center justify-center transition-colors border-none cursor-pointer" @click="slideNext">
          <i class="fa fa-angle-right text-xl"></i>
        </button>

        <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
          <span
            v-for="(_, idx) in slides"
            :key="idx"
            class="carousel-dot cursor-pointer"
            :class="{ active: currentSlide === idx }"
            @click="goToSlide(idx)"
          ></span>
        </div>
      </div>
    </section>

    <div class="container mx-auto px-4">
      <!-- 硬件分类 -->
      <section class="mb-8">
        <div class="bg-white rounded-xl shadow-sm p-4">
          <h2 class="text-lg font-bold text-gray-600 mb-4">硬件分类</h2>
          <div class="category-scrollbar overflow-x-auto pb-2">
            <ul class="flex space-x-6 md:space-x-8 min-w-max list-none p-0">
              <li v-for="cat in categories" :key="cat.label" class="text-center">
                <a
                  class="flex flex-col items-center no-underline cursor-pointer"
                  @click.prevent="router.push({ path: '/hardware', query: { type: cat.type } })"
                >
                  <div class="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary mb-2 hover:bg-primary/20 transition-colors">
                    <i :class="'fa ' + cat.icon + ' text-xl'"></i>
                  </div>
                  <span class="text-sm text-gray-600">{{ cat.label }}</span>
                </a>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 热门推荐 -->
      <section class="mb-8">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-bold text-gray-600">热门硬件推荐</h2>
          <router-link to="/hardware" class="text-primary hover:text-primary/80 text-sm flex items-center no-underline">
            查看更多 <i class="fa fa-angle-right ml-1"></i>
          </router-link>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="(product, idx) in hotProducts" :key="idx" class="bg-white rounded-xl shadow-sm overflow-hidden card-hover cursor-pointer" @click="goToHardware(product.title)">
            <div class="relative">
              <img :src="getHardwareImage(product.title)" :alt="product.title" class="w-full h-48 object-cover">
              <span v-if="product.badge" class="absolute top-2 left-2 text-white text-xs px-2 py-1 rounded" :class="product.badgeClass">{{ product.badge }}</span>
            </div>
            <div class="p-4">
              <h3 class="font-medium text-gray-600 mb-1 line-clamp-1">{{ product.title }}</h3>
              <div class="flex items-center text-yellow-400 text-sm mb-2">
                <i v-for="n in 5" :key="n" :class="n <= Math.floor(product.rating) ? 'fa fa-star' : n - 0.5 <= product.rating ? 'fa fa-star-half-o' : 'fa fa-star-o'"></i>
                <span class="text-gray-400 ml-1">{{ product.rating }} ({{ product.reviews }})</span>
              </div>
              <div class="flex justify-between items-center">
                <div>
                  <span class="text-secondary font-bold text-lg">¥{{ product.price }}</span>
                  <span class="text-gray-400 text-sm line-through ml-1">¥{{ product.oldPrice }}</span>
                </div>
                <button class="bg-primary/10 hover:bg-primary/20 text-primary w-8 h-8 rounded-full flex items-center justify-center transition-colors border-none cursor-pointer" @click.stop>
                  <i class="fa fa-shopping-cart"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 排行榜 + 资讯 -->
      <section class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <!-- 排行榜 -->
        <div class="lg:col-span-1 bg-white rounded-xl shadow-sm p-4">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-bold text-gray-600">热门推荐榜</h2>
          </div>
          <ul class="space-y-4 list-none p-0">
            <li v-for="item in rankings" :key="item.rank" class="flex items-center p-2 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer" @click="goToHardware(item.title)">
              <span
                class="w-6 h-6 rounded-full text-white flex items-center justify-center text-sm font-bold mr-3 flex-shrink-0"
                :class="rankBadgeClass(item.rank)"
              >
                {{ item.rank }}
              </span>
              <img :src="getHardwareImage(item.title)" :alt="item.title" class="w-12 h-12 object-cover rounded flex-shrink-0">
              <div class="flex-1 ml-3">
                <h3 class="text-sm font-medium text-gray-600 line-clamp-1">{{ item.title }}</h3>
                <span class="text-secondary text-sm font-bold">¥{{ item.price }}</span>
              </div>
            </li>
          </ul>
          <router-link to="/ranking" class="block text-center text-primary text-sm mt-4 hover:underline no-underline">查看完整榜单</router-link>
        </div>

        <!-- 硬件评测 -->
        <div class="lg:col-span-2 bg-white rounded-xl shadow-sm p-4">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-bold text-gray-600">硬件评测</h2>
            <router-link to="/evaluations" class="text-primary hover:text-primary/80 text-sm no-underline">更多评测 <i class="fa fa-angle-right ml-1"></i></router-link>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="item in homeEvaluations"
              :key="item.evaluationId"
              class="flex cursor-pointer hover:bg-gray-50 rounded-lg p-1 transition-colors"
              @click="router.push(`/evaluation/${item.evaluationId}`)"
            >
              <img
                :src="(item.images && item.images[0]) || (item.hardware?.imageUrl) || getHardwareImage(item.hardware?.hardwareName || '')"
                :alt="item.evaluationTitle"
                class="w-28 h-20 object-cover rounded-lg flex-shrink-0"
              />
              <div class="ml-3 flex-1">
                <h3 class="font-medium text-gray-600 text-sm line-clamp-2 hover:text-primary transition-colors">{{ item.evaluationTitle }}</h3>
                <div class="flex items-center text-gray-400 text-xs mt-2">
                  <span><i class="fa fa-user-circle-o mr-1"></i> {{ item.publisherName }}</span>
                  <span class="mx-2">|</span>
                  <span><i class="fa fa-clock-o mr-1"></i> {{ item.publishTime?.substring(0, 10) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="homeEvaluations.length === 0" class="text-center py-8 text-gray-400 text-sm">
            <i class="fa fa-file-text-o text-2xl mb-2 block"></i>
            暂无评测
          </div>

          <div class="mt-6">
            <h3 class="text-base font-bold text-gray-600 mb-3">硬件价格趋势</h3>
            <div class="h-48">
              <canvas id="priceChart"></canvas>
            </div>
          </div>
        </div>
      </section>

      <!-- 新品上市 -->
      <section class="mb-8">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-bold text-gray-600">新品上市</h2>
          <router-link to="/hardware" class="text-primary hover:text-primary/80 text-sm flex items-center no-underline">
            更多新品 <i class="fa fa-angle-right ml-1"></i>
          </router-link>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <div v-for="(item, idx) in newProducts" :key="idx" class="bg-white rounded-xl shadow-sm overflow-hidden card-hover cursor-pointer" @click="goToHardware(item.title)">
            <div class="relative">
              <img :src="getHardwareImage(item.title)" :alt="item.title" class="w-full h-32 object-cover">
              <span class="absolute top-2 left-2 bg-primary text-white text-xs px-2 py-1 rounded">新品</span>
            </div>
            <div class="p-3">
              <h3 class="text-sm font-medium text-gray-600 line-clamp-2">{{ item.title }}</h3>
              <div class="flex justify-between items-center mt-2">
                <span class="text-secondary text-sm font-bold">¥{{ item.price }}</span>
                <button class="text-gray-400 hover:text-primary transition-colors border-none bg-transparent cursor-pointer" @click.stop><i class="fa fa-heart-o"></i></button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 返回顶部 -->
    <button
      class="fixed bottom-6 right-6 bg-primary text-white w-12 h-12 rounded-full shadow-lg flex items-center justify-center transition-all duration-300 border-none cursor-pointer z-40"
      :class="showBackTop ? 'opacity-100 visible' : 'opacity-0 invisible'"
      @click="scrollToTop"
    >
      <i class="fa fa-angle-up text-xl"></i>
    </button>
  </div>
</template>

<style scoped>
.text-shadow {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.carousel-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}
.carousel-dot.active {
  background-color: #fff;
  width: 30px;
  border-radius: 6px;
}
</style>
