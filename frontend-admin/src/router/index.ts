import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据总览', icon: 'DataAnalysis' }
      },
      {
        path: 'scenic',
        name: 'Scenic',
        component: () => import('@/views/scenic/ScenicView.vue'),
        meta: { title: '景区管理', icon: 'Place' }
      },
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('@/views/ticket/TicketView.vue'),
        meta: { title: '票种管理', icon: 'Ticket' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/OrderView.vue'),
        meta: { title: '订单管理', icon: 'Document' }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('@/views/checkin/CheckInView.vue'),
        meta: { title: '检票管理', icon: 'CircleCheck' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserView.vue'),
        meta: { title: '用户管理', icon: 'User', admin: true }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/log/LogView.vue'),
        meta: { title: '操作日志', icon: 'Notebook', admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - 景区票务管理系统`

  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
