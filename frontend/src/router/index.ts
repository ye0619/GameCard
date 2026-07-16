import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/templates',
      name: 'templates',
      component: () => import('@/views/TemplatesPage.vue'),
    },
    {
      path: '/editor',
      name: 'editor',
      component: () => import('@/views/EditorPage.vue'),
    },
    {
      path: '/works',
      name: 'works',
      component: () => import('@/views/WorksPage.vue'),
    },
  ],
})

export default router
