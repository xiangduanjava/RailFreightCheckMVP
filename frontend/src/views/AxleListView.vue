<template>
  <main class="main-panel">
    <header class="topbar">
      <div>
        <div class="eyebrow">MVP 运行状态</div>
        <h1>铁路货车智能检修系统</h1>
      </div>
      <div class="topbar-actions">
        <input
          v-model="keyword"
          class="search-box"
          type="text"
          placeholder="🔎 搜索轮轴ID / 车号 / 故障类型"
          @input="onKeywordInput"
        />
        <button class="ghost-btn" @click="onExport">导出报表</button>
        <button class="primary-btn" @click="onNewTask">新增任务</button>
      </div>
    </header>

    <section class="filter-bar" aria-label="筛选条件">
      <button
        v-for="f in filters"
        :key="f"
        class="filter-btn"
        :class="{ active: activeFilter === f }"
        @click="activeFilter = f"
      >
        {{ f }}
      </button>
    </section>

    <section class="content-grid">
      <div class="panel large-panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow">轮轴数字身份</div>
            <h2>轮轴全生命周期追踪</h2>
          </div>
          <button class="primary-btn" @click="onNewAxle">新建轮轴</button>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>轮轴ID</th>
                <th>车型</th>
                <th>车号</th>
                <th>状态</th>
                <th>最新检测</th>
                <th>修程</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="a in axles"
                :key="a.axleId"
                class="clickable-row"
                @click="$router.push({ name: 'axle-detail', params: { id: a.axleId } })"
              >
                <td>{{ a.axleId }}</td>
                <td>{{ a.model }}</td>
                <td>{{ a.trainNo }}</td>
                <td><span class="status-pill" :class="statusClass(a.status)">{{ a.status }}</span></td>
                <td>{{ a.latestDetection }}</td>
                <td>{{ a.repairGrade || '—' }}</td>
              </tr>
              <tr v-if="!axles.length">
                <td colspan="6" class="empty-row">暂无符合条件的轮轴数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listAxles } from '../api/axle'
import { statusClass } from '../status'

const router = useRouter()

const filters = ['全部', '在修', '待复检', '预警', '已出库']

const axles = ref([])
const activeFilter = ref('全部')
const keyword = ref('')
let timer = null

async function fetchList() {
  const params = {}
  if (activeFilter.value !== '全部') params.status = activeFilter.value
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  axles.value = await listAxles(params)
}

function onKeywordInput() {
  clearTimeout(timer)
  timer = setTimeout(fetchList, 300)
}

function onExport() {
  // 导出报表：后续模块实现
  window.alert('导出报表：后续模块实现')
}

function onNewTask() {
  // 新增任务：后续模块实现
  window.alert('新增任务：后续模块实现')
}

function onNewAxle() {
  router.push({ name: 'axle-new' })
}

watch(activeFilter, fetchList)
onMounted(fetchList)
</script>
