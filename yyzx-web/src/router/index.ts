import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Login from '@/components/Login.vue'
import AdminDashboard from '@/components/AdminDashboard.vue'
import BedManagement from "@/components/BedManagement.vue";

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true },

  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    // 需要认证但没有token，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录用户访问登录页，跳转到管理页
    next('/admin')
  } else {
    next()
  }
})

export default router