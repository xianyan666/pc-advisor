<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminGetEvaluations, adminAuditEvaluation, adminDeleteEvaluation, adminGetComments, adminAuditComment, adminDeleteComment } from '@/api/user'
import type { AdminEvalItem, AdminCommentItem } from '@/api/user'

const activeTab = ref<'evaluation' | 'comment'>('evaluation')
const filter = ref<'pending' | 'approved' | 'rejected'>('pending')
const list = ref<AdminEvalItem[]>([])
const commentList = ref<AdminCommentItem[]>([])
const expanded = ref<number | null>(null)
const loading = ref(false)

const filterLabels: Record<string, string> = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
const typeLabels: Record<string, string> = { CPU: 'CPU', GRAPHICS_CARD: '显卡', MOTHERBOARD: '主板' }

async function loadEvals() {
  loading.value = true
  try { list.value = await adminGetEvaluations(filter.value) } catch { list.value = [] }
  loading.value = false
}
async function loadComments() {
  loading.value = true
  try { commentList.value = await adminGetComments(filter.value) } catch { commentList.value = [] }
  loading.value = false
}
function load() {
  if (activeTab.value === 'evaluation') loadEvals()
  else loadComments()
}
async function auditEval(id: number, action: string) {
  try { await adminAuditEvaluation(id, action); await loadEvals() } catch { /* */ }
}
async function auditComment(id: string, action: string) {
  try { await adminAuditComment(id, action); await loadComments() } catch { /* */ }
}
async function delEval(id: number) {
  if (!confirm('确定要删除这篇评测？所有相关评论和图片也将被删除。')) return
  try { await adminDeleteEvaluation(id); await loadEvals() } catch { alert('删除失败') }
}
async function delComment(id: string) {
  if (!confirm('确定要删除这条评论？')) return
  try { await adminDeleteComment(id); await loadComments() } catch { alert('删除失败') }
}

function switchTab(tab: 'evaluation' | 'comment') {
  activeTab.value = tab
  filter.value = 'pending'
  load()
}

onMounted(loadEvals)
</script>

<template>
  <div>
    <h1 class="text-white text-lg font-semibold mb-4">内容审核</h1>

    <!-- Tab 切换 -->
    <div class="flex gap-1 mb-4 bg-[#0d1117] rounded-lg p-0.5 w-fit">
      <button
        class="px-4 py-1.5 text-xs rounded-md border-none cursor-pointer transition-colors"
        :class="activeTab === 'evaluation' ? 'bg-[#1f6feb] text-white' : 'bg-transparent text-[#8b949e] hover:text-[#c9d1d9]'"
        @click="switchTab('evaluation')"
      >评测审核</button>
      <button
        class="px-4 py-1.5 text-xs rounded-md border-none cursor-pointer transition-colors"
        :class="activeTab === 'comment' ? 'bg-[#1f6feb] text-white' : 'bg-transparent text-[#8b949e] hover:text-[#c9d1d9]'"
        @click="switchTab('comment')"
      >评论审核</button>
    </div>

    <!-- 筛选 -->
    <div class="flex gap-2 mb-4">
      <button
        v-for="k in ['pending','approved','rejected']" :key="k"
        class="px-3 py-1.5 text-xs rounded-md border transition-colors cursor-pointer"
        :class="filter === k ? 'bg-[#1f6feb]/20 border-[#1f6feb] text-[#58a6ff]' : 'border-[#30363d] text-[#8b949e] hover:text-[#c9d1d9] bg-transparent'"
        @click="filter = k as any; load()"
      >{{ filterLabels[k] }}</button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="p-8 text-center text-[#8b949e] text-sm">加载中...</div>

    <!-- ==================== 评测审核列表 ==================== -->
    <template v-else-if="activeTab === 'evaluation'">
      <div v-if="!list.length" class="p-8 text-center text-[#8b949e] text-sm">暂无{{ filterLabels[filter] }}的评测</div>
      <div v-else class="space-y-3">
        <div v-for="item in list" :key="item.evaluationId" class="bg-[#161b22] border border-[#30363d] rounded-lg p-4">
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-[#c9d1d9] font-medium text-sm">{{ item.evaluationTitle }}</span>
                <span class="text-xs px-1.5 py-0.5 rounded" :class="item.auditState==='pending'?'bg-[#f0883e]/20 text-[#f0883e]':item.auditState==='approved'?'bg-[#238636]/20 text-[#3fb950]':'bg-[#f85149]/20 text-[#f85149]'">{{ filterLabels[item.auditState] }}</span>
                <span v-if="item.hardwareName" class="text-xs px-1.5 py-0.5 rounded bg-[#1f6feb]/20 text-[#58a6ff]">{{ typeLabels[item.hardwareType || ''] || item.hardwareType }}</span>
              </div>
              <div class="text-xs text-[#8b949e]">
                <span><i class="fa fa-user-circle-o mr-1"></i>{{ item.publisherName }}</span>
                <span class="mx-2">|</span>
                <span><i class="fa fa-clock-o mr-1"></i>{{ item.publishTime?.substring(0, 16) }}</span>
                <span v-if="item.hardwareName" class="mx-2">|</span>
                <span v-if="item.hardwareName"><i class="fa fa-cube mr-1"></i>{{ item.hardwareName }}</span>
              </div>

              <!-- 展开完整详情 -->
              <div v-if="expanded === item.evaluationId" class="mt-4 space-y-4">
                <div>
                  <h4 class="text-xs font-semibold text-[#8b949e] mb-1.5 uppercase tracking-wide">使用体验</h4>
                  <div class="p-3 bg-[#0d1117] rounded-md text-sm text-[#c9d1d9] leading-relaxed whitespace-pre-wrap">{{ item.usageExperience || '无' }}</div>
                </div>
                <div v-if="item.perfTestData">
                  <h4 class="text-xs font-semibold text-[#8b949e] mb-1.5 uppercase tracking-wide">性能测试数据</h4>
                  <div class="p-3 bg-[#0d1117] rounded-md text-sm text-[#c9d1d9] leading-relaxed whitespace-pre-wrap font-mono">{{ item.perfTestData }}</div>
                </div>
                <div v-if="item.prosAndCons">
                  <h4 class="text-xs font-semibold text-[#8b949e] mb-1.5 uppercase tracking-wide">优缺点</h4>
                  <div class="p-3 bg-[#0d1117] rounded-md text-sm text-[#c9d1d9] leading-relaxed whitespace-pre-wrap">{{ item.prosAndCons }}</div>
                </div>
                <div v-if="item.images && item.images.length">
                  <h4 class="text-xs font-semibold text-[#8b949e] mb-1.5 uppercase tracking-wide">配图 ({{ item.images.length }}张)</h4>
                  <div class="flex gap-2 flex-wrap">
                    <img v-for="(img, i) in item.images" :key="i" :src="img" class="w-24 h-18 object-cover rounded border border-[#30363d] hover:scale-105 transition-transform cursor-pointer" />
                  </div>
                </div>
              </div>
              <button class="text-xs text-[#58a6ff] hover:text-[#79c0ff] mt-3 border-none bg-transparent cursor-pointer" @click="expanded = expanded === item.evaluationId ? null : item.evaluationId">
                {{ expanded === item.evaluationId ? '收起详情' : '展开完整详情' }}
              </button>
            </div>
            <!-- 审核按钮 -->
            <div v-if="item.auditState === 'pending'" class="flex gap-2 flex-shrink-0">
              <button class="px-3 py-1 bg-[#238636] text-white text-xs rounded-md border-none cursor-pointer hover:bg-[#2ea043] transition-colors" @click="auditEval(item.evaluationId, 'approved')">
                <i class="fa fa-check mr-1"></i>通过
              </button>
              <button class="px-3 py-1 bg-transparent text-[#f85149] text-xs rounded-md border border-[#f85149]/30 cursor-pointer hover:bg-[#da3633]/20 transition-colors" @click="auditEval(item.evaluationId, 'rejected')">
                <i class="fa fa-times mr-1"></i>拒绝
              </button>
            </div>
            <!-- 删除按钮（已审核项） -->
            <div v-else class="flex gap-2 flex-shrink-0">
              <button class="px-3 py-1 bg-transparent text-[#f85149] text-xs rounded-md border border-[#f85149]/30 cursor-pointer hover:bg-[#da3633]/20 transition-colors" @click="delEval(item.evaluationId)">
                <i class="fa fa-trash-o mr-1"></i>删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== 评论审核列表 ==================== -->
    <template v-else>
      <div v-if="!commentList.length" class="p-8 text-center text-[#8b949e] text-sm">暂无{{ filterLabels[filter] }}的评论</div>
      <div v-else class="space-y-3">
        <div v-for="item in commentList" :key="item.commentId" class="bg-[#161b22] border border-[#30363d] rounded-lg p-4">
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs px-1.5 py-0.5 rounded" :class="item.auditState==='pending'?'bg-[#f0883e]/20 text-[#f0883e]':item.auditState==='approved'?'bg-[#238636]/20 text-[#3fb950]':'bg-[#f85149]/20 text-[#f85149]'">{{ filterLabels[item.auditState] }}</span>
                <span class="text-xs text-[#58a6ff] cursor-pointer hover:underline" @click="$router.push(`/evaluation/${item.evaluationId}`)">评测: {{ item.evaluationTitle }}</span>
              </div>
              <div class="text-xs text-[#8b949e] mb-2">
                <span><i class="fa fa-user-circle-o mr-1"></i>{{ item.userName }}</span>
                <span class="mx-2">|</span>
                <span><i class="fa fa-clock-o mr-1"></i>{{ item.publishTime?.substring(0, 16) }}</span>
              </div>
              <div class="p-3 bg-[#0d1117] rounded-md text-sm text-[#c9d1d9] leading-relaxed whitespace-pre-wrap">{{ item.content }}</div>
              <div v-if="item.images && item.images.length" class="flex gap-2 flex-wrap mt-2">
                <img v-for="(img, i) in item.images" :key="i" :src="img" class="w-20 h-20 object-cover rounded border border-[#30363d] hover:scale-105 transition-transform cursor-pointer" />
              </div>
            </div>
            <!-- 审核按钮 -->
            <div v-if="item.auditState === 'pending'" class="flex gap-2 flex-shrink-0">
              <button class="px-3 py-1 bg-[#238636] text-white text-xs rounded-md border-none cursor-pointer hover:bg-[#2ea043] transition-colors" @click="auditComment(item.commentId, 'approved')">
                <i class="fa fa-check mr-1"></i>通过
              </button>
              <button class="px-3 py-1 bg-transparent text-[#f85149] text-xs rounded-md border border-[#f85149]/30 cursor-pointer hover:bg-[#da3633]/20 transition-colors" @click="auditComment(item.commentId, 'rejected')">
                <i class="fa fa-times mr-1"></i>拒绝
              </button>
            </div>
            <!-- 删除按钮（已审核项） -->
            <div v-else class="flex gap-2 flex-shrink-0">
              <button class="px-3 py-1 bg-transparent text-[#f85149] text-xs rounded-md border border-[#f85149]/30 cursor-pointer hover:bg-[#da3633]/20 transition-colors" @click="delComment(item.commentId)">
                <i class="fa fa-trash-o mr-1"></i>删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
