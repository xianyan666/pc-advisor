<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminGetStats } from '@/api/user'
import type { AdminStats } from '@/api/user'

const stats = ref<AdminStats | null>(null)

onMounted(async () => {
  try { stats.value = await adminGetStats() } catch { /* */ }
})
</script>

<template>
  <div>
    <h1 class="text-white text-lg font-semibold mb-4">系统概览</h1>
    <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
      <div v-for="card in [
        { icon:'fa-microchip', label:'CPU', val: stats?.cpuCount, color:'#1f6feb' },
        { icon:'fa-picture-o', label:'显卡', val: stats?.gpuCount, color:'#238636' },
        { icon:'fa-server', label:'主板', val: stats?.mbCount, color:'#a371f7' },
        { icon:'fa-cubes', label:'硬件总数', val: stats?.hardwareCount, color:'#f0883e' },
      ]" :key="card.label" class="bg-[#161b22] border border-[#30363d] rounded-lg p-4">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded flex items-center justify-center" :style="{ background: card.color + '20', color: card.color }">
            <i :class="'fa ' + card.icon"></i>
          </div>
          <div>
            <div class="text-2xl font-bold text-white">{{ card.val ?? '-' }}</div>
            <div class="text-xs text-[#8b949e]">{{ card.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
      <div class="bg-[#161b22] border border-[#30363d] rounded-lg p-4 flex items-center gap-3">
        <div class="w-9 h-9 rounded bg-[#238636]/20 text-[#238636] flex items-center justify-center"><i class="fa fa-file-text-o"></i></div>
        <div>
          <div class="text-xl font-bold text-white">{{ stats?.evalCount ?? '-' }}</div>
          <div class="text-xs text-[#8b949e]">评测总数</div>
        </div>
      </div>
      <div class="bg-[#161b22] border border-[#30363d] rounded-lg p-4 flex items-center gap-3">
        <div class="w-9 h-9 rounded bg-[#f0883e]/20 text-[#f0883e] flex items-center justify-center"><i class="fa fa-clock-o"></i></div>
        <div>
          <div class="text-xl font-bold text-white">{{ stats?.pendingEvalCount ?? '-' }}</div>
          <div class="text-xs text-[#8b949e]">待审核评测</div>
        </div>
      </div>
      <div class="bg-[#161b22] border border-[#30363d] rounded-lg p-4 flex items-center gap-3">
        <div class="w-9 h-9 rounded bg-[#a371f7]/20 text-[#a371f7] flex items-center justify-center"><i class="fa fa-users"></i></div>
        <div>
          <div class="text-xl font-bold text-white">{{ stats?.userCount ?? '-' }}</div>
          <div class="text-xs text-[#8b949e]">用户数</div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
      <div class="bg-[#161b22] border border-[#30363d] rounded-lg p-4 flex items-center gap-3">
        <div class="w-9 h-9 rounded bg-[#1f6feb]/20 text-[#1f6feb] flex items-center justify-center"><i class="fa fa-comments"></i></div>
        <div>
          <div class="text-xl font-bold text-white">{{ stats?.commentCount ?? '-' }}</div>
          <div class="text-xs text-[#8b949e]">评论总数</div>
        </div>
      </div>
      <div class="bg-[#161b22] border border-[#30363d] rounded-lg p-4 flex items-center gap-3">
        <div class="w-9 h-9 rounded bg-[#f0883e]/20 text-[#f0883e] flex items-center justify-center"><i class="fa fa-comment-o"></i></div>
        <div>
          <div class="text-xl font-bold text-white">{{ stats?.pendingCommentCount ?? '-' }}</div>
          <div class="text-xs text-[#8b949e]">待审核评论</div>
        </div>
      </div>
    </div>
  </div>
</template>
