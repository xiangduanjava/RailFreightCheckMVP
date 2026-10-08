// ===== 新建轮轴 · 基础信息录入 =====

// 自动生成轮轴编号：AX-YYYYMMDD-XXX
function genAxleId() {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const suffix = String(Math.floor(Math.random() * 900) + 100);
  return `AX-${y}${m}${d}-${suffix}`;
}

// 必填字段定义（用于完整性统计与校验）
const requiredFields = [
  { id: 'model', label: '车型' },
  { id: 'trainNo', label: '车号' },
  { id: 'repair', label: '修程' },
  { id: 'axlePos', label: '轴位' },
  { id: 'factory', label: '生产厂家' },
  { id: 'produceDate', label: '生产日期' },
  { id: 'nominalWheel', label: '公称轮径' },
  { id: 'actualWheel', label: '实测轮径' }
];

// 预览字段映射（仅展示基础信息卡）
const previewFields = [
  { id: 'model', label: '车型' },
  { id: 'trainNo', label: '车号' },
  { id: 'repair', label: '修程' },
  { id: 'axlePos', label: '轴位' },
  { id: 'factory', label: '生产厂家' },
  { id: 'produceDate', label: '生产日期' }
];

function getVal(id) {
  const el = document.getElementById(id);
  return el ? el.value.trim() : '';
}

function setVal(id, value) {
  const el = document.getElementById(id);
  if (el) el.value = value;
}

// 校验并高亮
function validateRequired() {
  const missing = [];
  requiredFields.forEach(({ id, label }) => {
    const el = document.getElementById(id);
    if (!el) return;
    const empty = !el.value.trim();
    el.closest('.field').classList.toggle('error', empty);
    if (empty) missing.push(label);
  });
  return missing;
}

// 完整性统计：渲染进度条 + 清单
function renderCompleteness() {
  const total = requiredFields.length;
  let filled = 0;

  requiredFields.forEach(({ id, label }) => {
    const filledFlag = getVal(id) !== '';
    if (filledFlag) filled++;
    const item = document.querySelector(`#checkList [data-req="${id}"]`);
    if (item) item.classList.toggle('done', filledFlag);
  });

  const pct = Math.round((filled / total) * 100);
  const bar = document.getElementById('completenessBar');
  const num = document.getElementById('completenessNum');
  if (bar) bar.style.width = `${pct}%`;
  if (num) num.textContent = `${pct}%`;
}

// 实时预览
function renderPreview() {
  const grid = document.getElementById('previewGrid');
  if (!grid) return;

  grid.innerHTML = previewFields
    .map(({ id, label }) => {
      const value = getVal(id);
      return `
        <div>
          <span>${label}</span>
          <strong>${value || '—'}</strong>
        </div>
      `;
    })
    .join('');
}

// Toast 轻提示
let toastTimer;
function toast(message) {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = message;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 2200);
}

// 初始化
function init() {
  // 生成编号
  setVal('axleId', genAxleId());
  const regenBtn = document.getElementById('regenId');
  if (regenBtn) regenBtn.addEventListener('click', () => setVal('axleId', genAxleId()));

  // 渲染必填项清单
  const checkList = document.getElementById('checkList');
  if (checkList) {
    checkList.innerHTML = requiredFields
      .map(({ id, label }) => `
        <div class="check-item" data-req="${id}">
          <span class="tick">✓</span>
          <span>${label}</span>
        </div>
      `)
      .join('');
  }

  // 绑定输入事件：实时预览 + 完整性 + 清除错误态
  const fieldIds = [
    ...requiredFields.map((f) => f.id),
    ...previewFields.map((f) => f.id),
    'side',
    'bearing',
    'material',
    'serialNo',
    'certNo',
    'installDate',
    'journalDia',
    'flangeThick',
    'rimThick',
    'backGauge',
    'remark'
  ];

  const uniqueIds = [...new Set(fieldIds)];
  uniqueIds.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.addEventListener('input', () => {
      el.closest('.field').classList.remove('error');
      renderCompleteness();
      renderPreview();
    });
  });

  // 保存草稿
  const draftButtons = [document.getElementById('btnDraft'), document.getElementById('btnDraft2')];
  draftButtons.forEach((btn) =>
    btn && btn.addEventListener('click', () => toast('草稿已保存（原型演示）'))
  );

  // 下一步：校验必填项
  const nextButtons = [document.getElementById('btnNext'), document.getElementById('btnNext2')];
  nextButtons.forEach((btn) =>
    btn &&
    btn.addEventListener('click', () => {
      const missing = validateRequired();
      renderCompleteness();
      if (missing.length) {
        toast(`请补全必填项：${missing.join('、')}`);
        return;
      }

      // 推进流程步骤（原型）
      const line1 = document.getElementById('line1');
      const step2 = document.getElementById('step2');
      const line2 = document.getElementById('line2');
      const step3 = document.getElementById('step3');
      const step1 = document.querySelector('.step.active');

      if (step1) step1.classList.remove('active'), step1.classList.add('done');
      if (line1) line1.classList.add('done');
      if (step2) step2.classList.add('active');
      if (step2) step2.classList.remove('done');
      if (line2) line2.classList.remove('done');
      if (step3) step3.classList.remove('active');

      toast('基础信息已保存，进入「检测参数」录入（原型演示）');
    })
  );

  // 取消：返回上一页
  const cancelButtons = [document.getElementById('btnCancel'), document.getElementById('btnCancel2')];
  cancelButtons.forEach((btn) =>
    btn && btn.addEventListener('click', () => (window.history.length > 1 ? history.back() : (location.href = 'index.html')))
  );

  // 初始渲染
  renderCompleteness();
  renderPreview();
}

init();
