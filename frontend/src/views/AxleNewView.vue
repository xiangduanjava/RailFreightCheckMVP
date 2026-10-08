<template>
  <main class="main-panel">
    <header class="topbar">
      <div>
        <div class="eyebrow">轮轴建档</div>
        <h1>新建轮轴 · 基础信息录入</h1>
      </div>
      <div class="topbar-actions">
        <button class="ghost-btn" @click="onCancel">取消</button>
        <button class="ghost-btn" @click="onSaveDraft">保存草稿</button>
        <button class="primary-btn" :disabled="submitting" @click="onSubmit">提交</button>
      </div>
    </header>

    <section class="form-stepper" aria-label="录入流程">
      <div class="step active">
        <span class="num">1</span>
        基础信息
      </div>
      <div class="step-line"></div>
      <div class="step">
        <span class="num">2</span>
        检测参数
      </div>
      <div class="step-line"></div>
      <div class="step">
        <span class="num">3</span>
        确认提交
      </div>
    </section>

    <section class="form-layout">
      <div class="form-main">
        <!-- 轮轴身份 -->
        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">Step 1 · 身份信息</div>
              <h2>轮轴身份</h2>
            </div>
          </div>

          <div class="form-grid">
            <div class="field full">
              <div class="field-label">轮轴编号 <span class="hint">（系统自动生成）</span></div>
              <input type="text" value="由系统按「AX-日期-序号」规则生成，提交后生效" readonly />
            </div>

            <div class="field" :class="{ error: errors.model }">
              <label class="field-label" for="model">车型 <span class="req">*</span></label>
              <select id="model" v-model="form.model" @input="clearError('model')">
                <option value="">请选择车型</option>
                <option v-for="m in modelOptions" :key="m" :value="m">{{ m }}</option>
              </select>
            </div>

            <div class="field" :class="{ error: errors.trainNo }">
              <label class="field-label" for="trainNo">车号 <span class="req">*</span></label>
              <input id="trainNo" v-model="form.trainNo" type="text" placeholder="如 K65" @input="clearError('trainNo')" />
            </div>

            <div class="field" :class="{ error: errors.repair }">
              <label class="field-label" for="repair">修程 <span class="req">*</span></label>
              <select id="repair" v-model="form.repair" @input="clearError('repair')">
                <option value="">请选择修程</option>
                <option v-for="r in repairOptions" :key="r" :value="r">{{ r }}</option>
              </select>
            </div>

            <div class="field" :class="{ error: errors.axlePos }">
              <label class="field-label" for="axlePos">轴位 <span class="req">*</span></label>
              <select id="axlePos" v-model="form.axlePos" @input="clearError('axlePos')">
                <option value="">请选择轴位</option>
                <option v-for="p in axlePosOptions" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>

            <div class="field">
              <label class="field-label" for="side">左右侧</label>
              <select id="side" v-model="form.side">
                <option v-for="s in sideOptions" :key="s" :value="s">{{ s }}</option>
              </select>
            </div>

            <div class="field">
              <label class="field-label" for="bearing">轴承型号</label>
              <input id="bearing" v-model="form.bearing" type="text" placeholder="如 353130B" />
            </div>
          </div>
        </div>

        <!-- 制造信息 -->
        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">Step 1 · 制造信息</div>
              <h2>制造与来源</h2>
            </div>
          </div>

          <div class="form-grid">
            <div class="field" :class="{ error: errors.factory }">
              <label class="field-label" for="factory">生产厂家 <span class="req">*</span></label>
              <input id="factory" v-model="form.factory" type="text" placeholder="如 XX铁路轴承厂" @input="clearError('factory')" />
            </div>

            <div class="field" :class="{ error: errors.produceDate }">
              <label class="field-label" for="produceDate">生产日期 <span class="req">*</span></label>
              <input id="produceDate" v-model="form.produceDate" type="date" @input="clearError('produceDate')" />
            </div>

            <div class="field">
              <label class="field-label" for="material">钢号 / 材质</label>
              <select id="material" v-model="form.material">
                <option v-for="m in materialOptions" :key="m" :value="m">{{ m }}</option>
              </select>
            </div>

            <div class="field">
              <label class="field-label" for="serialNo">出厂编号</label>
              <input id="serialNo" v-model="form.serialNo" type="text" placeholder="如 MF-2024-01938" />
            </div>

            <div class="field">
              <label class="field-label" for="certNo">合格证编号</label>
              <input id="certNo" v-model="form.certNo" type="text" placeholder="如 HG-2024-1187" />
            </div>

            <div class="field">
              <label class="field-label" for="installDate">装机日期</label>
              <input id="installDate" v-model="form.installDate" type="date" />
            </div>
          </div>
        </div>

        <!-- 尺寸参数 -->
        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">Step 1 · 尺寸参数</div>
              <h2>关键尺寸</h2>
            </div>
          </div>

          <div class="form-grid">
            <div class="field" :class="{ error: errors.nominalWheel }">
              <label class="field-label" for="nominalWheel">公称轮径 <span class="req">*</span></label>
              <div class="unit-wrap">
                <input id="nominalWheel" v-model="form.nominalWheel" type="text" inputmode="decimal" placeholder="如 840" @input="clearError('nominalWheel')" />
                <span class="unit">mm</span>
              </div>
            </div>

            <div class="field" :class="{ error: errors.actualWheel }">
              <label class="field-label" for="actualWheel">实测轮径 <span class="req">*</span></label>
              <div class="unit-wrap">
                <input id="actualWheel" v-model="form.actualWheel" type="text" inputmode="decimal" placeholder="如 839.2" @input="clearError('actualWheel')" />
                <span class="unit">mm</span>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="journalDia">轴颈直径</label>
              <div class="unit-wrap">
                <input id="journalDia" v-model="form.journalDia" type="text" inputmode="decimal" placeholder="如 130" />
                <span class="unit">mm</span>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="flangeThick">轮缘厚度</label>
              <div class="unit-wrap">
                <input id="flangeThick" v-model="form.flangeThick" type="text" inputmode="decimal" placeholder="如 32" />
                <span class="unit">mm</span>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="rimThick">轮辋厚度</label>
              <div class="unit-wrap">
                <input id="rimThick" v-model="form.rimThick" type="text" inputmode="decimal" placeholder="如 65" />
                <span class="unit">mm</span>
              </div>
            </div>

            <div class="field">
              <label class="field-label" for="backGauge">轮对内侧距</label>
              <div class="unit-wrap">
                <input id="backGauge" v-model="form.backGauge" type="text" inputmode="decimal" placeholder="如 1353" />
                <span class="unit">mm</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">Step 1 · 其他</div>
              <h2>备注</h2>
            </div>
          </div>

          <div class="form-grid">
            <div class="field full">
              <textarea id="remark" v-model="form.remark" rows="3" placeholder="选填：记录轮轴的特殊情况、检修备注等"></textarea>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button class="ghost-btn" @click="onCancel">取消</button>
          <button class="ghost-btn" @click="onNextDetection">下一步：检测参数 →</button>
          <button class="primary-btn" :disabled="submitting" @click="onSubmit">提交</button>
        </div>
      </div>

      <aside class="form-side">
        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">录入完整性</div>
              <h2>必填项进度</h2>
            </div>
          </div>

          <div class="completeness-head">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: completeness.pct + '%' }"></div>
            </div>
            <span class="completeness-num">{{ completeness.pct }}%</span>
          </div>

          <div class="check-list">
            <div v-for="item in checkList" :key="item.key" class="check-item" :class="{ done: item.done }">
              <span class="tick">✓</span>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>

        <div class="panel preview-card">
          <div class="panel-header">
            <div>
              <div class="eyebrow">实时预览</div>
              <h2>轮轴信息卡</h2>
            </div>
          </div>

          <div class="barcode">||||||||||||||||||||||||</div>
          <div class="preview-grid">
            <div v-for="item in preview" :key="item.key">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-header">
            <div>
              <div class="eyebrow">提示</div>
              <h2>录入说明</h2>
            </div>
          </div>

          <ul class="help-list">
            <li>带 <span style="color: var(--danger)">*</span> 为必填项，填写后右侧进度自动更新。</li>
            <li>轮轴编号由系统按「AX-日期-序号」规则自动生成。</li>
            <li>提交后轮轴将进入「在役」状态，可前往详情页查看。</li>
            <li>尺寸参数需与实测数据一致，单位为 mm。</li>
          </ul>
        </div>
      </aside>
    </section>

    <div class="toast" :class="{ show: showToast }">{{ toastMsg }}</div>
  </main>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createAxle } from '../api/axle'

const router = useRouter()

const modelOptions = ['CW-200', 'CW-160', 'CW-120']
const repairOptions = ['例行检查', '中修', '大修', '复检']
const axlePosOptions = ['1位轴', '2位轴', '3位轴', '4位轴']
const sideOptions = ['左侧', '右侧']
const materialOptions = ['50钢', '35CrMo', 'EA4T']

const requiredFields = [
  { key: 'model', label: '车型' },
  { key: 'trainNo', label: '车号' },
  { key: 'repair', label: '修程' },
  { key: 'axlePos', label: '轴位' },
  { key: 'factory', label: '生产厂家' },
  { key: 'produceDate', label: '生产日期' },
  { key: 'nominalWheel', label: '公称轮径' },
  { key: 'actualWheel', label: '实测轮径' }
]

const previewFields = [
  { key: 'model', label: '车型' },
  { key: 'trainNo', label: '车号' },
  { key: 'repair', label: '修程' },
  { key: 'axlePos', label: '轴位' },
  { key: 'factory', label: '生产厂家' },
  { key: 'produceDate', label: '生产日期' }
]

const form = ref({
  model: '',
  trainNo: '',
  repair: '',
  axlePos: '',
  side: '左侧',
  bearing: '',
  material: '50钢',
  serialNo: '',
  certNo: '',
  factory: '',
  produceDate: '',
  installDate: '',
  nominalWheel: '',
  actualWheel: '',
  journalDia: '',
  flangeThick: '',
  rimThick: '',
  backGauge: '',
  remark: ''
})

const errors = ref({})
const submitting = ref(false)
const toastMsg = ref('')
const showToast = ref(false)
let toastTimer = null

const completeness = computed(() => {
  const filled = requiredFields.filter((f) => isFilled(form.value[f.key])).length
  return { filled, total: requiredFields.length, pct: Math.round((filled / requiredFields.length) * 100) }
})

const checkList = computed(() =>
  requiredFields.map((f) => ({ ...f, done: isFilled(form.value[f.key]) }))
)

const preview = computed(() =>
  previewFields.map((f) => ({ ...f, value: form.value[f.key] || '—' }))
)

function isFilled(v) {
  return v != null && String(v).trim() !== ''
}

function clearError(key) {
  if (errors.value[key]) {
    const next = { ...errors.value }
    delete next[key]
    errors.value = next
  }
}

function validateRequired() {
  const missing = []
  const next = {}
  requiredFields.forEach((f) => {
    if (!isFilled(form.value[f.key])) {
      missing.push(f.label)
      next[f.key] = true
    }
  })
  errors.value = next
  return missing
}

function toNumber(v) {
  if (!isFilled(v)) return null
  return Number(String(v).trim())
}

function buildPayload() {
  const f = form.value
  return {
    model: f.model,
    trainNo: f.trainNo,
    repair: f.repair,
    axlePos: f.axlePos,
    side: f.side || null,
    bearing: f.bearing || null,
    material: f.material || null,
    serialNo: f.serialNo || null,
    certNo: f.certNo || null,
    factory: f.factory,
    produceDate: f.produceDate || null,
    installDate: f.installDate || null,
    nominalWheel: toNumber(f.nominalWheel),
    actualWheel: toNumber(f.actualWheel),
    journalDia: toNumber(f.journalDia),
    flangeThick: toNumber(f.flangeThick),
    rimThick: toNumber(f.rimThick),
    backGauge: toNumber(f.backGauge),
    remark: f.remark || null
  }
}

function toast(message) {
  toastMsg.value = message
  showToast.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (showToast.value = false), 2200)
}

async function onSubmit() {
  const missing = validateRequired()
  if (missing.length) {
    toast('请补全必填项：' + missing.join('、'))
    return
  }
  submitting.value = true
  try {
    const res = await createAxle(buildPayload())
    router.push({ name: 'axle-detail', params: { id: res.axleId } })
  } catch (err) {
    toast(err.response?.data?.message || '创建失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function onNextDetection() {
  toast('检测参数录入：后续模块实现')
}

function onSaveDraft() {
  toast('草稿保存：后续模块实现')
}

function onCancel() {
  router.push({ name: 'axle-list' })
}
</script>

<style scoped>
.form-stepper {
  display: flex;
  align-items: center;
  margin: 4px 0 22px;
  padding: 14px 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
}

.step {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--muted);
  font-weight: 600;
  font-size: 0.86rem;
  white-space: nowrap;
}

.step .num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #edf2fb;
  color: var(--muted);
  font-weight: 700;
  font-size: 0.82rem;
  flex: none;
}

.step.active {
  color: var(--text);
}
.step.active .num {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 6px 12px rgba(30, 96, 255, 0.25);
}

.step-line {
  flex: 1;
  height: 2px;
  background: var(--line);
  margin: 0 16px;
  min-width: 40px;
  border-radius: 999px;
}

.form-layout {
  display: grid;
  grid-template-columns: 1.6fr 0.9fr;
  gap: 20px;
  align-items: start;
}

.form-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(200px, 1fr));
  gap: 18px 22px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 7px;
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
  padding: 12px 14px;
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

.field input[readonly] {
  background: #f5f8fd;
  color: var(--muted);
}

.field .hint {
  font-size: 0.72rem;
  color: var(--muted);
}

.field .unit-wrap {
  position: relative;
}
.field .unit-wrap input {
  padding-right: 56px;
}
.field .unit {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--muted);
  font-size: 0.8rem;
  pointer-events: none;
}

.field.error input,
.field.error select {
  border-color: var(--danger);
  box-shadow: 0 0 0 4px rgba(255, 90, 95, 0.08);
}

.field.full {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 2px;
}

.form-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 20px;
}

.completeness-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.completeness-head .progress-track {
  width: auto;
  flex: 1;
}

.completeness-num {
  font-size: 1.7rem;
  font-weight: 800;
  color: var(--primary);
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 11px;
  margin-top: 14px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.82rem;
  color: var(--muted);
}

.check-item .tick {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 0.68rem;
  background: #edf2fb;
  color: transparent;
  flex: none;
}

.check-item.done {
  color: var(--text);
}
.check-item.done .tick {
  background: rgba(39, 179, 106, 0.15);
  color: var(--success);
}

.preview-card {
  background: linear-gradient(135deg, #edf4ff 0%, #f5f9ff 100%);
}

.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 14px;
  margin-top: 14px;
}

.preview-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--line);
}

.preview-grid span {
  font-size: 0.7rem;
  color: var(--muted);
}
.preview-grid strong {
  font-size: 0.86rem;
}

.help-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 0.78rem;
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
  padding: 0;
}

.help-list li {
  list-style: none;
  padding-left: 18px;
  position: relative;
}

.help-list li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 7px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary);
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

@media (max-width: 1100px) {
  .form-layout {
    grid-template-columns: 1fr;
  }
  .form-side {
    position: static;
  }
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .form-stepper {
    overflow-x: auto;
  }
  .step-line {
    min-width: 24px;
  }
}
</style>
