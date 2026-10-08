<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand-wrap">
        <div class="brand-mark">铁</div>
        <div>
          <div class="brand-title">检修决策平台</div>
          <div class="brand-subtitle">Rail Freight Check</div>
        </div>
      </div>

      <nav class="nav">
        <router-link to="/axles" class="nav-item" :class="{ active: $route.name === 'axle-list' }"><span>🏠</span> 总览</router-link>
        <router-link to="/axles" class="nav-item" :class="{ active: $route.name === 'axle-detail' }"><span>🧩</span> 轮轴详情</router-link>
        <router-link to="/axles/new" class="nav-item" :class="{ active: $route.name === 'axle-new' }"><span>➕</span> 新建轮轴</router-link>
        <router-link to="/device" class="nav-item" :class="{ active: $route.name === 'device-collection' }"><span>📡</span> 设备采集</router-link>
        <span class="nav-item nav-disabled"><span>🧭</span> 生产调度</span>
        <span class="nav-item nav-disabled"><span>⚠️</span> 预警中心</span>
        <span class="nav-item nav-disabled"><span>📊</span> 报表统计</span>
        <span class="nav-item nav-disabled"><span>🖥️</span> 车间大屏</span>
      </nav>
    </aside>

    <router-view />
  </div>
</template>

<style>
.nav-disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
