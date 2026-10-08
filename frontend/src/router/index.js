import { createRouter, createWebHistory } from 'vue-router'
import AxleListView from '../views/AxleListView.vue'
import AxleDetailView from '../views/AxleDetailView.vue'
import AxleNewView from '../views/AxleNewView.vue'
import DeviceCollectionView from '../views/DeviceCollectionView.vue'

const routes = [
  { path: '/', redirect: '/axles' },
  { path: '/axles', name: 'axle-list', component: AxleListView },
  { path: '/axles/new', name: 'axle-new', component: AxleNewView },
  { path: '/axles/:id', name: 'axle-detail', component: AxleDetailView, props: true },
  { path: '/device', name: 'device-collection', component: DeviceCollectionView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
