<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminGetHardware, adminCreateHardware, adminUpdateHardware, adminDeleteHardware } from '@/api/user'
import type { AdminHardwareItem } from '@/api/user'

const list = ref<AdminHardwareItem[]>([])
const loading = ref(true)
const typeFilter = ref<string>('')
const showForm = ref(false)
const editing = ref<AdminHardwareItem | null>(null)
const form = ref<Record<string, unknown>>({ hardwareType: 'CPU', hardwareName: '', hardwareBrand: '', hardwareModel: '', hardwarePrice: 0 })
const formError = ref('')
const submitting = ref(false)

const typeLabels: Record<string, string> = { CPU: 'CPU', GRAPHICS_CARD: '显卡', MOTHERBOARD: '主板' }
const typeTabs = [
  { key: '', label: '全部', icon: 'fa-cubes' },
  { key: 'CPU', label: 'CPU', icon: 'fa-microchip' },
  { key: 'GRAPHICS_CARD', label: '显卡', icon: 'fa-picture-o' },
  { key: 'MOTHERBOARD', label: '主板', icon: 'fa-server' },
]
const filteredList = computed(() =>
  typeFilter.value ? list.value.filter(h => h.hardwareType === typeFilter.value) : list.value
)

async function load() {
  loading.value = true
  try { list.value = await adminGetHardware() } catch { list.value = [] }
  loading.value = false
}

function openAdd() {
  editing.value = null
  form.value = { hardwareType: typeFilter.value || 'CPU', hardwareName: '', hardwareBrand: '', hardwareModel: '', hardwarePrice: 0 }
  formError.value = ''
  showForm.value = true
}
function openEdit(item: AdminHardwareItem) {
  editing.value = item
  form.value = { ...item }
  formError.value = ''
  showForm.value = true
}
async function save() {
  if (!form.value.hardwareName) { formError.value = '硬件名称不能为空'; return }
  submitting.value = true
  try {
    if (editing.value) {
      await adminUpdateHardware(editing.value.hardwareId, form.value)
    } else {
      await adminCreateHardware(form.value)
    }
    showForm.value = false
    await load()
  } catch { formError.value = '操作失败' }
  submitting.value = false
}
async function del(item: AdminHardwareItem) {
  if (!confirm(`确定删除 "${item.hardwareName}"？`)) return
  try { await adminDeleteHardware(item.hardwareId); await load() } catch { /* */ }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-white text-lg font-semibold">硬件管理</h1>
      <button class="px-3 py-1.5 bg-[#238636] text-white text-xs rounded-md border-none cursor-pointer hover:bg-[#2ea043] transition-colors" @click="openAdd">
        <i class="fa fa-plus mr-1"></i>添加硬件
      </button>
    </div>

    <!-- 分类标签 -->
    <div class="flex gap-2 mb-4">
      <button
        v-for="tab in typeTabs" :key="tab.key"
        class="px-3 py-1.5 text-xs rounded-md border transition-colors cursor-pointer"
        :class="typeFilter === tab.key ? 'bg-[#1f6feb]/20 border-[#1f6feb] text-[#58a6ff]' : 'border-[#30363d] text-[#8b949e] hover:text-[#c9d1d9] bg-transparent'"
        @click="typeFilter = tab.key"
      >
        <i :class="'fa ' + tab.icon + ' mr-1'"></i>{{ tab.label }}
      </button>
    </div>

    <!-- 表格 -->
    <div class="bg-[#161b22] border border-[#30363d] rounded-lg overflow-hidden">
      <table v-if="!loading && filteredList.length" class="w-full text-sm border-collapse">
        <thead>
          <tr class="border-b border-[#30363d] text-[#8b949e] text-xs">
            <th class="px-3 py-2 text-left">ID</th>
            <th class="px-3 py-2 text-left">名称</th>
            <th class="px-3 py-2 text-left">类型</th>
            <th class="px-3 py-2 text-left">品牌</th>
            <th class="px-3 py-2 text-right">价格</th>
            <th class="px-3 py-2 text-center">状态</th>
            <th class="px-3 py-2 text-right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredList" :key="item.hardwareId" class="border-b border-[#21262d] hover:bg-[#1c2128] transition-colors">
            <td class="px-3 py-2 text-[#8b949e] font-mono text-xs">{{ item.hardwareId }}</td>
            <td class="px-3 py-2 text-[#c9d1d9] font-medium">{{ item.hardwareName }}</td>
            <td class="px-3 py-2"><span class="text-xs px-1.5 py-0.5 rounded bg-[#1f6feb]/20 text-[#58a6ff]">{{ typeLabels[item.hardwareType] || item.hardwareType }}</span></td>
            <td class="px-3 py-2 text-[#8b949e]">{{ item.hardwareBrand }}</td>
            <td class="px-3 py-2 text-right text-[#c9d1d9]">¥{{ item.hardwarePrice }}</td>
            <td class="px-3 py-2 text-center"><span class="text-xs px-1.5 py-0.5 rounded" :class="item.auditState==='approved'?'bg-[#238636]/20 text-[#3fb950]':'bg-[#f0883e]/20 text-[#f0883e]'">{{ item.auditState==='approved'?'已发布':'草稿' }}</span></td>
            <td class="px-3 py-2 text-right">
              <button class="text-[#58a6ff] hover:text-[#79c0ff] border-none bg-transparent cursor-pointer text-xs mr-2" @click="openEdit(item)">编辑</button>
              <button class="text-[#f85149] hover:text-[#ff7b72] border-none bg-transparent cursor-pointer text-xs" @click="del(item)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="loading" class="p-8 text-center text-[#8b949e] text-sm">加载中...</div>
      <div v-else class="p-8 text-center text-[#8b949e] text-sm">{{ typeFilter ? '暂无' + typeLabels[typeFilter] + '数据' : '暂无硬件数据' }}</div>
    </div>

    <!-- 表单弹窗 -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="showForm = false">
      <div class="bg-[#161b22] border border-[#30363d] rounded-xl w-full max-w-md p-6">
        <h2 class="text-white font-semibold mb-4">{{ editing ? '编辑硬件' : '添加硬件' }}</h2>
        <div class="space-y-3">
          <div v-if="!editing">
            <label class="block text-xs text-[#8b949e] mb-1">类型</label>
            <select v-model="form.hardwareType" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]">
              <option value="CPU">CPU</option>
              <option value="GRAPHICS_CARD">显卡</option>
              <option value="MOTHERBOARD">主板</option>
            </select>
          </div>
          <div>
            <label class="block text-xs text-[#8b949e] mb-1">名称 <span class="text-[#f85149]">*</span></label>
            <input v-model="form.hardwareName" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：Intel 酷睿 i5-13400F" />
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs text-[#8b949e] mb-1">品牌</label>
              <input v-model="form.hardwareBrand" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" />
            </div>
            <div>
              <label class="block text-xs text-[#8b949e] mb-1">型号</label>
              <input v-model="form.hardwareModel" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" />
            </div>
          </div>
          <div>
            <label class="block text-xs text-[#8b949e] mb-1">价格 (¥)</label>
            <input v-model.number="form.hardwarePrice" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" />
          </div>

          <!-- CPU 详情 -->
          <template v-if="(editing?.hardwareType || form.hardwareType) === 'CPU'">
            <div class="border-t border-[#30363d] pt-3 mt-1">
              <p class="text-xs text-[#8b949e] mb-2 uppercase tracking-wide">CPU 规格</p>
              <div class="grid grid-cols-2 gap-3">
                <div><label class="block text-xs text-[#8b949e] mb-1">核心数</label><input v-model.number="form.coreCount" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">线程数</label><input v-model.number="form.threadCount" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">基础频率 (GHz)</label><input v-model.number="form.baseFreq" type="number" step="0.1" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">TDP功耗 (W)</label><input v-model.number="form.tdpPower" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">接口类型</label><input v-model="form.interfaceType" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：LGA 1700" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">支持内存</label><input v-model="form.supportMemType" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：DDR5" /></div>
              </div>
            </div>
          </template>

          <!-- GPU 详情 -->
          <template v-if="(editing?.hardwareType || form.hardwareType) === 'GRAPHICS_CARD'">
            <div class="border-t border-[#30363d] pt-3 mt-1">
              <p class="text-xs text-[#8b949e] mb-2 uppercase tracking-wide">显卡 规格</p>
              <div class="grid grid-cols-2 gap-3">
                <div><label class="block text-xs text-[#8b949e] mb-1">核心型号</label><input v-model="form.coreModel" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：AD107" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">显存容量</label><input v-model="form.vramCap" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：8GB" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">显存类型</label><input v-model="form.vramType" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：GDDR6" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">功耗 (W)</label><input v-model.number="form.powerConsump" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">显卡长度 (mm)</label><input v-model.number="form.gcLength" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
              </div>
            </div>
          </template>

          <!-- 主板 详情 -->
          <template v-if="(editing?.hardwareType || form.hardwareType) === 'MOTHERBOARD'">
            <div class="border-t border-[#30363d] pt-3 mt-1">
              <p class="text-xs text-[#8b949e] mb-2 uppercase tracking-wide">主板 规格</p>
              <div class="grid grid-cols-2 gap-3">
                <div><label class="block text-xs text-[#8b949e] mb-1">CPU接口</label><input v-model="form.cpuInterface" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：LGA 1700" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">板型</label><input v-model="form.mbForm" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：ATX" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">内存插槽数</label><input v-model.number="form.memSlotCount" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">M.2插槽数</label><input v-model.number="form.m2SlotCount" type="number" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" /></div>
                <div><label class="block text-xs text-[#8b949e] mb-1">支持内存</label><input v-model="form.mbSupportMemType" class="w-full px-3 py-2 bg-[#0d1117] border border-[#30363d] rounded-md text-[#c9d1d9] text-sm focus:outline-none focus:border-[#1f6feb]" placeholder="如：DDR5" /></div>
              </div>
            </div>
          </template>

          <p v-if="formError" class="text-[#f85149] text-xs">{{ formError }}</p>
        </div>
        <div class="flex justify-end gap-2 mt-4">
          <button class="px-4 py-1.5 text-[#c9d1d9] text-xs rounded-md border border-[#30363d] bg-transparent cursor-pointer hover:bg-[#1c2128]" @click="showForm = false">取消</button>
          <button class="px-4 py-1.5 bg-[#238636] text-white text-xs rounded-md border-none cursor-pointer hover:bg-[#2ea043] disabled:opacity-50" :disabled="submitting" @click="save">{{ submitting ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
