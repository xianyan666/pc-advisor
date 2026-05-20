<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEvaluationById, createComment, deleteComment, uploadImage } from '@/api/user'
import { getHardwareImage } from '@/utils/imageMapping'
import type { EvaluationDetail } from '@/api/user'

const route = useRoute()
const router = useRouter()

const evaluation = ref<EvaluationDetail | null>(null)
const loading = ref(true)
const errorMsg = ref('')

const typeLabels: Record<string, string> = {
  CPU: 'CPU',
  GRAPHICS_CARD: '显卡',
  MOTHERBOARD: '主板',
}

// 图片灯箱
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

// 评论
const commentText = ref('')
const commentImages = ref<string[]>([])
const commentSubmitting = ref(false)
const uploadingImage = ref(false)
const commentError = ref('')
const commentSuccess = ref('')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const currentUserId = computed(() => JSON.parse(localStorage.getItem('userInfo') || '{}')?.userId || '')

const deletingCmtId = ref<string | null>(null)

async function handleDeleteComment(commentId: string) {
  if (!confirm('确定要删除这条评论吗？')) return
  deletingCmtId.value = commentId
  try {
    await deleteComment(commentId)
    if (evaluation.value) {
      evaluation.value.comments = evaluation.value.comments.filter(c => c.commentId !== commentId)
    }
  } catch {
    alert('删除失败，请稍后重试')
  } finally {
    deletingCmtId.value = null
  }
}

async function handleUploadCommentImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input?.files?.[0]
  if (!file) return
  uploadingImage.value = true
  try {
    const res = await uploadImage(file)
    commentImages.value.push(res.url)
  } catch {
    commentError.value = '图片上传失败'
  } finally {
    uploadingImage.value = false
    if (input) input.value = ''
  }
}

function removeCommentImage(index: number) {
  commentImages.value.splice(index, 1)
}

async function handleSubmitComment() {
  if (!commentText.value.trim()) {
    commentError.value = '请输入评论内容'
    return
  }
  commentError.value = ''
  commentSubmitting.value = true
  try {
    const id = Number(route.params.id)
    await createComment(id, {
      content: commentText.value.trim(),
      imageUrls: [...commentImages.value],
    })
    commentText.value = ''
    commentImages.value = []
    commentError.value = ''
    // 评论需要审核，不直接加入列表
    commentSuccess.value = '评论已提交，等待管理员审核后显示'
    setTimeout(() => { commentSuccess.value = '' }, 5000)
  } catch {
    commentError.value = '评论发送失败'
  } finally {
    commentSubmitting.value = false
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (isNaN(id) || id <= 0) {
    errorMsg.value = '评测不存在'
    loading.value = false
    return
  }
  try {
    evaluation.value = await getEvaluationById(id)
  } catch {
    errorMsg.value = '评测加载失败'
  } finally {
    loading.value = false
  }
})

function goToHardware() {
  if (evaluation.value?.hardware) {
    router.push(`/hardware/${evaluation.value.hardware.hardwareId}`)
  }
}
</script>

<template>
  <div class="container mx-auto px-4 py-6 max-w-4xl">
    <!-- 加载 -->
    <template v-if="loading">
      <div class="animate-pulse space-y-4">
        <div class="h-6 bg-gray-200 rounded w-24"></div>
        <div class="bg-white rounded-xl p-6 space-y-3">
          <div class="h-8 bg-gray-200 rounded w-3/4"></div>
          <div class="h-4 bg-gray-100 rounded w-1/3"></div>
          <div class="h-40 bg-gray-100 rounded mt-4"></div>
        </div>
      </div>
    </template>

    <!-- 错误 -->
    <template v-else-if="errorMsg || !evaluation">
      <div class="text-center py-20">
        <i class="fa fa-frown-o text-5xl text-gray-300 mb-4 block"></i>
        <p class="text-gray-500 text-lg">{{ errorMsg || '评测不存在' }}</p>
        <button
          class="mt-4 px-6 py-2 bg-primary text-white rounded-full border-none cursor-pointer hover:bg-primary/90 transition-colors"
          @click="router.push('/home')"
        >
          返回首页
        </button>
      </div>
    </template>

    <!-- 评测内容 -->
    <template v-else>
      <!-- 顶部导航 -->
      <div class="text-sm text-gray-400 mb-4 flex items-center gap-1 flex-wrap">
        <a href="/hardware" class="text-primary hover:underline no-underline" @click.prevent="router.push('/hardware')">DIY硬件</a>
        <span class="mx-1">/</span>
        <a
          v-if="evaluation.hardware"
          class="text-primary hover:underline no-underline"
          @click.prevent="goToHardware"
        >
          {{ evaluation.hardware.hardwareName }}
        </a>
        <span v-else class="text-gray-400">未知硬件</span>
        <span class="mx-1">/</span>
        <span class="text-gray-600">评测详情</span>
      </div>

      <!-- 评测正文 -->
      <div class="bg-white rounded-xl shadow-sm p-6 md:p-8">
        <!-- 标题区 -->
        <div class="mb-6">
          <div class="flex items-center gap-2 mb-2">
            <span class="inline-block bg-primary/10 text-primary text-xs px-2.5 py-1 rounded-full">
              评测
            </span>
            <span
              class="text-xs px-2 py-0.5 rounded-full"
              :class="evaluation.auditState === 'approved' ? 'bg-green-100 text-green-700' : evaluation.auditState === 'pending' ? 'bg-yellow-100 text-yellow-700' : 'bg-red-100 text-red-700'"
            >
              {{ evaluation.auditState === 'approved' ? '已通过' : evaluation.auditState === 'pending' ? '审核中' : '未通过' }}
            </span>
          </div>
          <h1 class="text-2xl font-bold text-gray-800 leading-snug">{{ evaluation.evaluationTitle }}</h1>
          <div class="flex items-center gap-4 mt-3 text-sm text-gray-400">
            <span><i class="fa fa-user-circle-o mr-1"></i>{{ evaluation.publisherName }}</span>
            <span><i class="fa fa-clock-o mr-1"></i>{{ evaluation.publishTime?.substring(0, 16) }}</span>
          </div>
        </div>

        <!-- 关联硬件卡片 -->
        <div
          v-if="evaluation.hardware"
          class="bg-gray-50 rounded-xl p-4 mb-6 flex items-center gap-4 cursor-pointer hover:bg-gray-100 transition-colors"
          @click="goToHardware"
        >
          <img
            :src="evaluation.hardware.imageUrl || getHardwareImage(evaluation.hardware.hardwareName)"
            :alt="evaluation.hardware.hardwareName"
            class="w-16 h-16 object-cover rounded-lg flex-shrink-0"
          />
          <div class="flex-1 min-w-0">
            <span class="inline-block bg-primary/10 text-primary text-xs px-2 py-0.5 rounded-full mb-1">
              {{ typeLabels[evaluation.hardware.hardwareType] || evaluation.hardware.hardwareType }}
            </span>
            <h3 class="text-sm font-medium text-gray-700 truncate">{{ evaluation.hardware.hardwareName }}</h3>
            <p class="text-xs text-gray-400 mt-0.5">{{ evaluation.hardware.hardwareBrand }} · ¥{{ evaluation.hardware.hardwarePrice }}</p>
          </div>
          <i class="fa fa-angle-right text-gray-300"></i>
        </div>

        <!-- 评测配图 -->
        <div v-if="evaluation.images && evaluation.images.length" class="mb-6">
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <img
              v-for="(img, i) in evaluation.images"
              :key="i"
              :src="img"
              alt="评测配图"
              class="w-full h-48 object-cover rounded-lg border border-gray-100 cursor-zoom-in hover:shadow-md hover:scale-[1.02] transition-all"
              @click="openLightbox(img)"
            />
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="border-t border-gray-100 my-6"></div>

        <!-- 使用体验 -->
        <section class="mb-8">
          <h2 class="text-lg font-bold text-gray-700 mb-3 flex items-center">
            <i class="fa fa-star text-primary mr-2"></i>使用体验
          </h2>
          <div class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">{{ evaluation.usageExperience }}</div>
        </section>

        <!-- 性能测试数据 -->
        <section v-if="evaluation.perfTestData" class="mb-8">
          <h2 class="text-lg font-bold text-gray-700 mb-3 flex items-center">
            <i class="fa fa-bar-chart text-primary mr-2"></i>性能测试数据
          </h2>
          <div class="bg-gray-50 rounded-xl p-4 text-sm text-gray-600 font-mono leading-relaxed whitespace-pre-wrap">{{ evaluation.perfTestData }}</div>
        </section>

        <!-- 优缺点 -->
        <section v-if="evaluation.prosAndCons" class="mb-6">
          <h2 class="text-lg font-bold text-gray-700 mb-3 flex items-center">
            <i class="fa fa-balance-scale text-primary mr-2"></i>优缺点分析
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="line in evaluation.prosAndCons.split('\n').filter(l => l.trim())"
              :key="line"
              class="flex items-start gap-2 text-sm"
              :class="line.includes('优点') || line.startsWith('优') ? 'text-green-700 bg-green-50 rounded-lg p-3' : line.includes('缺点') || line.startsWith('缺') ? 'text-red-600 bg-red-50 rounded-lg p-3' : 'text-gray-600 bg-gray-50 rounded-lg p-3'"
            >
              <i
                class="mt-0.5 flex-shrink-0"
                :class="line.includes('优点') || line.startsWith('优') ? 'fa fa-check-circle text-green-400' : line.includes('缺点') || line.startsWith('缺') ? 'fa fa-exclamation-circle text-red-400' : 'fa fa-circle text-gray-400 text-xs'"
              ></i>
              <span>{{ line }}</span>
            </div>
          </div>
        </section>

        <!-- 底部分隔 -->
        <div class="border-t border-gray-100 mt-8 pt-4 text-center text-xs text-gray-400">
          评测发布于 {{ evaluation.publishTime?.substring(0, 16) }}
        </div>
      </div>

      <!-- 评论区 -->
      <div class="bg-white rounded-xl shadow-sm p-6 md:p-8 mt-4">
        <h2 class="text-lg font-bold text-gray-700 mb-5 flex items-center">
          <i class="fa fa-comments text-primary mr-2"></i>评论
          <span class="ml-2 text-sm font-normal text-gray-400">({{ evaluation.comments?.length || 0 }})</span>
        </h2>

        <!-- 评论输入区 -->
        <div v-if="isLoggedIn" class="mb-6">
          <textarea
            v-model="commentText"
            class="w-full border border-gray-200 rounded-xl p-4 text-sm text-gray-700 resize-none focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 transition-colors"
            rows="3"
            placeholder="写下你的评论..."
          ></textarea>

          <!-- 已上传的图片预览 -->
          <div v-if="commentImages.length" class="flex flex-wrap gap-2 mt-3">
            <div
              v-for="(img, i) in commentImages"
              :key="i"
              class="relative w-20 h-20 rounded-lg overflow-hidden border border-gray-200 group"
            >
              <img :src="img" alt="comment image" class="w-full h-full object-cover" />
              <button
                class="absolute top-0.5 right-0.5 w-5 h-5 rounded-full bg-black/60 text-white flex items-center justify-center border-none cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
                @click="removeCommentImage(i)"
              >
                <i class="fa fa-times text-xs"></i>
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between mt-3">
            <label class="inline-flex items-center gap-1.5 text-sm text-gray-400 cursor-pointer hover:text-primary transition-colors">
              <i class="fa fa-image"></i>
              <span>{{ uploadingImage ? '上传中...' : '添加图片' }}</span>
              <input type="file" accept="image/*" class="hidden" @change="handleUploadCommentImage" :disabled="uploadingImage" />
            </label>
            <button
              class="px-5 py-2 bg-primary text-white rounded-full text-sm border-none cursor-pointer hover:bg-primary/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="commentSubmitting || !commentText.trim()"
              @click="handleSubmitComment"
            >
              {{ commentSubmitting ? '发送中...' : '发表评论' }}
            </button>
          </div>
          <p v-if="commentError" class="text-xs text-red-400 mt-2">{{ commentError }}</p>
          <p v-if="commentSuccess" class="text-xs text-green-500 mt-2">{{ commentSuccess }}</p>
        </div>
        <div v-else class="mb-6 p-4 bg-gray-50 rounded-xl text-center text-sm text-gray-400">
          <a href="/login" class="text-primary hover:underline" @click.prevent="router.push('/login')">登录</a>后即可评论
        </div>

        <!-- 评论列表 -->
        <div v-if="evaluation.comments && evaluation.comments.length" class="space-y-4">
          <div
            v-for="comment in evaluation.comments"
            :key="comment.commentId"
            class="border-t border-gray-50 pt-4 first:border-t-0 first:pt-0"
          >
            <div class="flex items-center gap-2 mb-2">
              <div class="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center text-xs font-bold flex-shrink-0">
                {{ comment.userName?.charAt(0) || '匿' }}
              </div>
              <span class="text-sm font-medium text-gray-700">{{ comment.userName }}</span>
              <span class="text-xs text-gray-400">{{ comment.publishTime?.substring(0, 16) }}</span>
              <button
                v-if="comment.userId === currentUserId"
                class="ml-auto text-xs text-gray-300 hover:text-red-400 border-none bg-transparent cursor-pointer transition-colors"
                :disabled="deletingCmtId === comment.commentId"
                @click="handleDeleteComment(comment.commentId)"
              >
                <i :class="deletingCmtId === comment.commentId ? 'fa fa-spinner fa-spin' : 'fa fa-trash-o'"></i>
              </button>
            </div>
            <p class="text-sm text-gray-600 leading-relaxed ml-10">{{ comment.content }}</p>
            <!-- 评论配图 -->
            <div v-if="comment.images && comment.images.length" class="flex flex-wrap gap-2 mt-2 ml-10">
              <img
                v-for="(img, i) in comment.images"
                :key="i"
                :src="img"
                alt="评论配图"
                class="w-24 h-24 object-cover rounded-lg border border-gray-100 cursor-zoom-in hover:shadow-md hover:scale-[1.05] transition-all"
                @click="openLightbox(img)"
              />
            </div>
          </div>
        </div>
        <div v-else class="text-center py-8 text-sm text-gray-400">
          <i class="fa fa-commenting-o text-3xl text-gray-200 mb-2 block"></i>
          暂无评论，来说点什么吧
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
