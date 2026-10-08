const axleProfiles = {
  'AX-20240618-104': {
    title: 'AX-20240618-104',
    model: 'CW-200',
    trainNo: 'K65',
    status: '在修中',
    statusClass: 'ok',
    factory: 'XX铁路轴承厂',
    produceDate: '2024-06-18',
    installDate: '2024-08-22',
    repairCount: '3 次',
    life: '2,100 km',
    wear: '2.8 mm',
    crack: '1.2 mm',
    vibration: '3.2 mm/s',
    temp: '41 ℃'
  },
  'AX-20240618-112': {
    title: 'AX-20240618-112',
    model: 'CW-200',
    trainNo: 'K66',
    status: '待复检',
    statusClass: 'warn',
    factory: 'XX铁路轴承厂',
    produceDate: '2024-06-18',
    installDate: '2024-08-22',
    repairCount: '2 次',
    life: '1,860 km',
    wear: '2.3 mm',
    crack: '1.0 mm',
    vibration: '2.8 mm/s',
    temp: '38 ℃'
  }
};

const params = new URLSearchParams(window.location.search);
const selectedId = params.get('id') || 'AX-20240618-104';
const profile = axleProfiles[selectedId] || axleProfiles['AX-20240618-104'];

const titleEl = document.querySelector('.detail-page h1');
const statusTag = document.querySelector('.profile-card .status-tag');
const detailTitle = document.querySelector('.detail-title');

if (titleEl) titleEl.textContent = profile.title;
if (statusTag) {
  statusTag.textContent = profile.status;
  statusTag.className = `status-tag ${profile.statusClass}`;
}
if (detailTitle) detailTitle.textContent = profile.title;

const dataFields = [
  ['车型', profile.model],
  ['车号', profile.trainNo],
  ['生产厂家', profile.factory],
  ['生产日期', profile.produceDate],
  ['装机日期', profile.installDate],
  ['当前状态', profile.status],
  ['累计检修', profile.repairCount],
  ['剩余寿命', profile.life]
];

const infoGrid = document.querySelector('.info-grid');
if (infoGrid) {
  infoGrid.innerHTML = dataFields.map(([label, value]) => `
    <div>
      <span>${label}</span>
      <strong>${value}</strong>
    </div>
  `).join('');
}

const metricValues = [
  ['轮径磨损', '66%', profile.wear],
  ['裂纹深度', '34%', profile.crack],
  ['振动值', '48%', profile.vibration],
  ['温升', '52%', profile.temp]
];

const metricsStack = document.querySelector('.metrics-stack');
if (metricsStack) {
  metricsStack.innerHTML = metricValues.map(([label, width, value]) => `
    <div class="metric-row">
      <span>${label}</span>
      <div class="mini-progress"><i style="width: ${width}"></i></div>
      <strong>${value}</strong>
    </div>
  `).join('');
}
