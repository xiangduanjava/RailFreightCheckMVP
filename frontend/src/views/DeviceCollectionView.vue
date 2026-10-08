<template>
  <main class="main-panel detail-page">
    <header class="topbar">
      <div>
        <div class="eyebrow">工位数据自动采集</div>
        <h1>设备采集与同步</h1>
      </div>
      <div class="topbar-actions">
        <button class="primary-btn" @click="openModal">手动补录</button>
      </div>
    </header>

    <section class="device-grid">
      <div class="panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow">在线设备</div>
            <h2>工位设备状态</h2>
          </div>
        </div>
        <div class="device-list">
          <div v-for="d in devices" :key="d.code" class="device-item">
            <div>
              <strong>{{ d.name }}</strong>
              <span>设备编号：{{ d.code }}</span>
            </div>
            <span class="status-tag" :class="d.tone">{{ d.status }}</span>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <div>
            <div class="eyebrow">实时同步</div>
            <h2>数据上传状态</h2>
          </div>
        </div>
        <div class="upload-chart">
          <div class="ring-wrap" :style="{ background: ringBg }">
            <div class="ring-ring">{{ syncRate }}</div>
          </div>
          <div class="upload-stats">
            <div><span>今日采集</span><strong>{{ summary.todayCollected }}</strong></div>
            <div><span>已上传</span><strong>{{ summary.uploaded }}</strong></div>
            <div><span>待同步</span><strong>{{ summary.pending }}</strong></div>
          </div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <div class="eyebrow">采集日志</div>
          <h2>最近 5 条同步记录</h2>
        </div>
      </div>
      <div class="log-list">
        <div v-for="(l, i) in logs" :key="l.detectionTime + '-' + i" class="log-item">
          <span>{{ fmtTime(l.detectionTime) }}</span>
          <strong>{{ l.station }}</strong>
          <p>{{ l.axleId }} 检测数据已上传</p>
          <span class="status-tag ok">{{ l.syncStatus }}</span>
        </div>
        <div v-if="!logs.length" class="empty-row">暂无同步记录</div>
      </div>
    </section>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h2>手动补录</h2>
          <button class="modal-close" aria-label="关闭" @click="closeModal">×</button>
        </div>

        <div class="modal-grid">
          <div class="field">
            <label class="field-label" for="d-axleId">轮轴ID <span class="req">*</span></label>
            <input id="d-axleId" v-model="form.axleId" type="text" placeholder="如 AX-20240618-104" />
          </div>
          <div class="field">
            <label class="field-label" for="d-station">工位 <span class="req">*</span></label>
            <select id="d-station" v-model="form.station">
              <option value="">请选择工位</option>
              <option v-for="s in stationOptions" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
          <div class="field full">
            <label class="field-label" for="d-time">检测时间 <span class="req">*</span></label>
            <input id="d-time" v-model="form.detectionTime" type="datetime-local" />
          </div>
          <div class="field">
            <label class="field-label" for="d-wear">轮径磨损 (mm) <span v-if="form.station === '轮对尺寸检测机'" class="req">*</span></label>
            <input id="d-wear" v-model="form.wear" type="text" inputmode="decimal" placeholder="如 2.5" />
          </div>
          <div class="field">
            <label class="field-label" for="d-crack">裂纹深度 (mm) <span v-if="form.station === '超声波探伤仪'" class="req">*</span></label>
            <input id="d-crack" v-model="form.crack" type="text" inputmode="decimal" placeholder="如 1.2" />
          </div>
          <div class="field">
            <label class="field-label" for="d-vibration">振动值 (mm/s)</label>
            <input id="d-vibration" v-model="form.vibration" type="text" inputmode="decimal" placeholder="如 3.2" />
          </div>
          <div class="field">
            <label class="field-label" for="d-temp">温升 (℃)</label>
            <input id="d-temp" v-model="form.temp" type="text" inputmode="decimal" placeholder="如 41" />
          </div>
          <div class="field full">
            <label class="field-label" for="d-remark">备注</label>
            <textarea id="d-remark" v-model="form.remark" rows="2" placeholder="选填"></textarea>
          </div>
        </div>

        <div class="modal-actions">
          <button class="ghost-btn" @click="closeModal">取消</button>
          <button class="primary-btn" :disabled="submitting" @click="onSubmit">提交</button>
        </div>
      </div>
    </div>

    <div class="toast" :class="{ show: showToast }">{{ toastMsg }}</div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { submitDetection, listDetections, getDetectionSummary } from '../api/detection'

const devices = [
  { name: '轮对尺寸检测机', code: 'D-01', status: '在线', tone: 'ok' },
  { name: '超声波探伤仪', code: 'U-02', status: '校准中', tone: 'warn' },
  { name: '自动称重系统', code: 'W-07', status: '在线', tone: 'ok' },
  { name: '手持终端补录', code: 'M-12', status: '1 条待补录', tone: 'alert' }
]

const stationOptions = ['轮对尺寸检测机', '超声波探伤仪', '手持终端补录']

const summary = ref({ todayCollected: 0, uploaded: 0, pending: 0 })
const logs = ref([])
const showModal = ref(false)
const submitting = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
let toastTimer = null

const form = ref({
  axleId: '',
  station: '',
  detectionTime: '',
  wear: '',
  crack: '',
  vibration: '',
  temp: '',
  remark: ''
})

const syncRate = computed(() => {
  const s = summary.value
  if (!s || s.todayCollected === 0) return '100%'
  return Math.round((s.uploaded / s.todayCollected) * 100) + '%'
})

const ringBg = computed(() => {
  const s = summary.value
  const pct = s && s.todayCollected > 0 ? Math.min(100, Math.round((s.uploaded / s.todayCollected) * 100)) : 100
  const deg = Math.round(pct * 3.6)
  return `conic-gradient(var(--primary) 0 ${deg}deg, #edf2fb ${deg}deg 360deg)`
})

function nowLocal() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`
}

function isFilled(v) {
  return v != null && String(v).trim() !== ''
}

function toNumber(v) {
  return isFilled(v) ? Number(String(v).trim()) : null
}

function toIso(v) {
  if (!v) return null
  return v.length === 16 ? v + ':00' : v
}

function fmtTime(iso) {
  if (!iso) return ''
  const t = String(iso).split('T')[1]
  return t ? t.slice(0, 5) : ''
}

function toast(message) {
  toastMsg.value = message
  showToast.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (showToast.value = false), 2200)
}

function openModal() {
  form.value = {
    axleId: '',
    station: '',
    detectionTime: nowLocal(),
    wear: '',
    crack: '',
    vibration: '',
    temp: '',
    remark: ''
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function validate() {
  const missing = []
  if (!isFilled(form.value.axleId)) missing.push('轮轴ID')
  if (!form.value.station) missing.push('工位')
  if (!isFilled(form.value.detectionTime)) missing.push('检测时间')

  const hasAny = ['wear', 'crack', 'vibration', 'temp'].some((k) => isFilled(form.value[k]))
  if (!hasAny) {
    missing.push('检测指标')
  } else {
    if (form.value.station === '轮对尺寸检测机' && !isFilled(form.value.wear)) missing.push('轮径磨损')
    if (form.value.station === '超声波探伤仪' && !isFilled(form.value.crack)) missing.push('裂纹深度')
  }
  return missing
}

function buildPayload() {
  const f = form.value
  return {
    axleId: f.axleId.trim(),
    station: f.station,
    detectionTime: toIso(f.detectionTime),
    wear: toNumber(f.wear),
    crack: toNumber(f.crack),
    vibration: toNumber(f.vibration),
    temp: toNumber(f.temp),
    remark: isFilled(f.remark) ? f.remark.trim() : null
  }
}

async function refresh() {
  const [s, l] = await Promise.all([getDetectionSummary(), listDetections(5)])
  summary.value = s
  logs.value = l
}

async function onSubmit() {
  const missing = validate()
  if (missing.length) {
    toast('请补全必填项：' + missing.join('、'))
    return
  }
  submitting.value = true
  try {
    await submitDetection(buildPayload())
    closeModal()
    toast('补录成功，检测数据已同步')
    await refresh()
  } catch (err) {
    toast(err.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(refresh)
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: grid;
  place-items: center;
  z-index: 50;
}

.modal {
  width: min(640px, 92vw);
  background: var(--panel);
  border-radius: 20px;
  box-shadow: 0 24px 64px rgba(24, 35, 70, 0.24);
  padding: 22px 24px;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.modal-header h2 {
  margin: 0;
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 1.6rem;
  line-height: 1;
  cursor: pointer;
  color: var(--muted);
}

.modal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field.full {
  grid-column: 1 / -1;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text);
}

.req {
  color: var(--danger);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  padding: 11px 13px;
  font: inherit;
  font-size: 0.9rem;
  color: var(--text);
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: rgba(30, 96, 255, 0.45);
  box-shadow: 0 0 0 4px rgba(30, 96, 255, 0.08);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.log-item {
  grid-template-columns: 60px 1.1fr 1.5fr auto;
}

.toast {
  position: fixed;
  right: 28px;
  bottom: 28px;
  background: #14213d;
  color: #fff;
  padding: 14px 20px;
  border-radius: 14px;
  box-shadow: var(--shadow);
  font-size: 0.9rem;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.25s ease;
  pointer-events: none;
  z-index: 100;
}

.toast.show {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 720px) {
  .modal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
