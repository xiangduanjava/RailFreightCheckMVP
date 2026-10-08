<template>
  <main class="main-panel detail-page" v-if="axle">
    <header class="topbar">
      <div>
        <div class="eyebrow">轮轴数字身份证</div>
        <h1>{{ axle.axleId }}</h1>
      </div>
      <div class="topbar-actions">
        <button class="ghost-btn" @click="onExportHistory">导出履历</button>
        <button class="primary-btn" @click="showUpdate = !showUpdate">更新状态</button>
      </div>
    </header>

    <section class="profile-header">
      <div class="profile-card">
        <div class="profile-main">
          <div class="barcode">||||||||||||||||||||||||</div>
          <div>
            <div class="detail-label">轮轴编号</div>
            <div class="detail-title">{{ axle.axleId }}</div>
          </div>
        </div>
        <span class="status-tag" :class="statusClass(axle.status)">{{ axle.status }}</span>
      </div>
    </section>

    <section v-if="showUpdate" class="panel update-panel">
      <div class="panel-header">
        <div>
          <div class="eyebrow">状态管理</div>
          <h2>更新轮轴状态</h2>
        </div>
      </div>
      <div class="update-controls">
        <label>
          生命周期状态
          <select v-model="form.lifecycleStatus">
            <option value="">不变更</option>
            <option v-for="l in lifecycleOptions" :key="l" :value="l">{{ l }}</option>
          </select>
        </label>
        <label>
          检修流程状态
          <select v-model="form.repairStatus">
            <option value="">不变更</option>
            <option v-for="r in repairOptions" :key="r" :value="r">{{ r }}</option>
          </select>
        </label>
        <button class="primary-btn" @click="save">保存</button>
        <button class="ghost-btn" @click="markScrap">标记报废</button>
      </div>
    </section>

    <section class="detail-grid-2">
      <div class="panel detail-panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow">基础信息</div>
            <h2>轮轴信息卡</h2>
          </div>
        </div>
        <div class="info-grid">
          <div><span>车型</span><strong>{{ axle.model }}</strong></div>
          <div><span>车号</span><strong>{{ axle.trainNo }}</strong></div>
          <div><span>生产厂家</span><strong>{{ axle.factory }}</strong></div>
          <div><span>生产日期</span><strong>{{ axle.produceDate }}</strong></div>
          <div><span>装机日期</span><strong>{{ axle.installDate || '—' }}</strong></div>
          <div><span>当前状态</span><strong>{{ axle.status }}</strong></div>
          <div><span>累计检修</span><strong>{{ axle.repairCount }} 次</strong></div>
          <div><span>剩余寿命</span><strong>{{ axle.remainingLife }} km</strong></div>
        </div>
      </div>

      <div class="panel detail-panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow">检修参数</div>
            <h2>关键检测指标</h2>
          </div>
        </div>
        <div class="metrics-stack">
          <div v-for="m in metrics" :key="m.label" class="metric-row">
            <span>{{ m.label }}</span>
            <div class="mini-progress"><i :style="{ width: m.pct }"></i></div>
            <strong>{{ m.value }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <div class="eyebrow">全生命周期</div>
          <h2>履历记录</h2>
        </div>
      </div>
      <div class="history-list">
        <div v-for="(h, i) in axle.history" :key="h.eventTime + i" class="history-item">
          <span class="history-date">{{ fmtDate(h.eventTime) }}</span>
          <div>
            <strong>{{ h.eventType }}</strong>
            <p>{{ h.note }}</p>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAxle, updateAxleStatus } from '../api/axle'
import { statusClass } from '../status'

const route = useRoute()
const axle = ref(null)
const showUpdate = ref(false)
const form = ref({ lifecycleStatus: '', repairStatus: '' })

const lifecycleOptions = ['在役', '检修', '报废']
const repairOptions = ['在修', '待复检', '待验收', '已出库']

const metrics = computed(() => {
  if (!axle.value) return []
  return [
    { label: '轮径磨损', value: `${axle.value.wear} mm`, pct: pct(axle.value.wear, 5) },
    { label: '裂纹深度', value: `${axle.value.crack} mm`, pct: pct(axle.value.crack, 4) },
    { label: '振动值', value: `${axle.value.vibration} mm/s`, pct: pct(axle.value.vibration, 6) },
    { label: '温升', value: `${axle.value.temp} ℃`, pct: pct(axle.value.temp, 80) }
  ]
})

function pct(v, max) {
  if (v == null) return '0%'
  return Math.min(100, Math.round((v / max) * 100)) + '%'
}

function fmtDate(iso) {
  if (!iso) return ''
  return String(iso).replace('T', ' ').slice(0, 16)
}

async function load() {
  axle.value = await getAxle(route.params.id)
}

async function save() {
  await updateAxleStatus(route.params.id, { ...form.value })
  form.value = { lifecycleStatus: '', repairStatus: '' }
  showUpdate.value = false
  await load()
}

async function markScrap() {
  await updateAxleStatus(route.params.id, { lifecycleStatus: '报废' })
  showUpdate.value = false
  await load()
}

function onExportHistory() {
  window.alert('导出履历：后续模块实现')
}

onMounted(load)
</script>

<style scoped>
.update-panel {
  margin-bottom: 16px;
}
.update-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}
.update-controls label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.82rem;
  color: var(--muted, #6c757d);
}
.update-controls select {
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 0.9rem;
}
</style>
