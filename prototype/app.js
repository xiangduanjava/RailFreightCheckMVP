const axleData = [
  { id: 'AX-20240618-104', model: 'CW-200', trainNo: 'K65', status: '在修', latest: '轮对尺寸检测', repair: '中修' },
  { id: 'AX-20240618-112', model: 'CW-200', trainNo: 'K66', status: '待复检', latest: '超声波探伤', repair: '复检' },
  { id: 'AX-20240618-077', model: 'CW-120', trainNo: 'T31', status: '正常', latest: '参数复核', repair: '例行' },
  { id: 'AX-20240618-003', model: 'CW-200', trainNo: 'K41', status: '预警', latest: '磨损超标', repair: '预警处置' },
  { id: 'AX-20240618-221', model: 'CW-160', trainNo: 'G82', status: '已出库', latest: '交付签字', repair: '完工' }
];

const stationData = [
  { name: '轮对尺寸检测工位', done: 86, status: '正常', className: 'primary' },
  { name: '超声波探伤工位', done: 72, status: '待复检', className: 'warning' },
  { name: '修复装配工位', done: 94, status: '正常', className: 'success' },
  { name: '质检签字工位', done: 68, status: '待审批', className: 'warning' }
];

const scheduleData = [
  { name: 'K65 车次扣修', time: '09:30-11:00', progress: 82, state: '执行中' },
  { name: 'K66 车次巡检', time: '11:00-12:30', progress: 65, state: '调度中' },
  { name: 'T31 车次返修', time: '14:00-15:30', progress: 48, state: '等待装配' },
  { name: 'G82 车次交付', time: '15:30-17:00', progress: 93, state: '已完成 93%' }
];

const alertData = [
  { title: 'AX-20240618-003', note: '轮轴磨损超出标准，已转预警处理', level: 'high' },
  { title: 'AX-20240618-112', note: '超声波复检建议在 30 分钟内确认', level: 'mid' },
  { title: '车间任务超时', note: 'K66 车次已超出节拍计划 18 分钟', level: 'mid' },
  { title: '采集缺失', note: '1 条手持录入数据待补齐', level: 'low' }
];

const statusMap = {
  正常: 'ok',
  在修: 'ok',
  待复检: 'warn',
  预警: 'alert',
  已出库: 'ok',
  报废: 'error'
};

let activeFilter = '全部';

function applyAxleFilter() {
  const input = document.getElementById('searchInput');
  const keyword = (input ? input.value.trim().toLowerCase() : '');

  const filtered = axleData.filter(item => {
    const matchesFilter = activeFilter === '全部' || item.status === activeFilter;
    const haystack = `${item.id} ${item.model} ${item.trainNo} ${item.latest} ${item.repair}`.toLowerCase();
    const matchesKeyword = !keyword || haystack.includes(keyword);
    return matchesFilter && matchesKeyword;
  });

  const tbody = document.getElementById('axleTableBody');
  if (!tbody) return;

  if (!filtered.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="empty-row">暂无符合条件的轮轴数据</td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(item => `
    <tr data-id="${item.id}" class="clickable-row">
      <td>${item.id}</td>
      <td>${item.model}</td>
      <td>${item.trainNo}</td>
      <td><span class="status-pill ${statusMap[item.status] || 'ok'}">${item.status}</span></td>
      <td>${item.latest}</td>
      <td>${item.repair}</td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.clickable-row').forEach(row => {
    row.addEventListener('click', () => {
      const axleId = row.dataset.id;
      window.location.href = `axle-detail.html?id=${encodeURIComponent(axleId)}`;
    });
  });
}

function renderAxles() {
  const tbody = document.getElementById('axleTableBody');
  if (!tbody) return;

  const filterButtons = document.querySelectorAll('.filter-btn');
  filterButtons.forEach(button => {
    button.addEventListener('click', () => {
      activeFilter = button.dataset.filter;
      filterButtons.forEach(btn => btn.classList.toggle('active', btn === button));
      applyAxleFilter();
    });
  });

  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('input', applyAxleFilter);
  }

  applyAxleFilter();
}

function renderStations() {
  const list = document.getElementById('stationList');
  list.innerHTML = stationData.map(station => `
    <div class="station-row">
      <div class="station-meta">
        <strong>${station.name}</strong>
        <span>${station.status}</span>
      </div>
      <div style="display:flex; align-items:center; gap:10px;">
        <div class="progress-track">
          <div class="progress-fill ${station.className === 'warning' ? 'warning' : station.className === 'success' ? 'success' : ''}" style="width:${station.done}%"></div>
        </div>
        <strong>${station.done}%</strong>
      </div>
    </div>
  `).join('');
}

function renderSchedule() {
  const list = document.getElementById('scheduleList');
  list.innerHTML = scheduleData.map(item => `
    <div class="schedule-row">
      <div class="schedule-meta">
        <strong>${item.name}</strong>
        <span>${item.time}</span>
      </div>
      <div style="display:flex; align-items:center; gap:10px;">
        <div class="progress-track">
          <div class="progress-fill ${item.progress >= 80 ? 'success' : 'warning'}" style="width:${item.progress}%"></div>
        </div>
        <strong>${item.progress}%</strong>
      </div>
    </div>
  `).join('');
}

function renderAlerts() {
  const list = document.getElementById('alertList');
  list.innerHTML = alertData.map(item => `
    <div class="alert-row">
      <div>
        <strong>${item.title}</strong>
        <span>${item.note}</span>
      </div>
      <div class="alert-badge ${item.level}">${item.level === 'high' ? '高' : item.level === 'mid' ? '中' : '低'}</div>
    </div>
  `).join('');
}

renderAxles();
renderStations();
renderSchedule();
renderAlerts();
