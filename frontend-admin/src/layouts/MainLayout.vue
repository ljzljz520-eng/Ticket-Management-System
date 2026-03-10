<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '240px'" class="sidebar">
      <div class="sidebar-header">
        <div class="logo-wrapper">
          <el-icon :size="28" color="#fff"><Ticket /></el-icon>
          <transition name="fade">
            <span v-if="!isCollapsed" class="logo-text">票务管理</span>
          </transition>
        </div>
      </div>

      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapsed"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.75)"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
          class="menu-item"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="top-header">
        <div class="header-left">
          <el-button
            :icon="isCollapsed ? 'Expand' : 'Fold'"
            text
            @click="isCollapsed = !isCollapsed"
            class="collapse-btn"
          />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-icon><User /></el-icon>
                  {{ userStore.userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition name="slide-up" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const currentRoute = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '')

const allMenuItems = [
  { path: '/dashboard', title: '数据总览', icon: 'DataAnalysis' },
  { path: '/scenic', title: '景区管理', icon: 'Place' },
  { path: '/ticket', title: '票种管理', icon: 'Ticket' },
  { path: '/order', title: '订单管理', icon: 'Document' },
  { path: '/checkin', title: '检票管理', icon: 'CircleCheck' },
  { path: '/user', title: '用户管理', icon: 'User', admin: true },
  { path: '/log', title: '操作日志', icon: 'Notebook', admin: true }
]

const menuItems = computed(() => {
  if (userStore.isAdmin) return allMenuItems
  return allMenuItems.filter(item => !item.admin)
})

function handleCommand(command: string) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: linear-gradient(180deg, #1a2332 0%, #0f172a 100%);
  transition: width var(--duration-normal) var(--ease-default);
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.sidebar-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  white-space: nowrap;
}

.logo-text {
  font-family: var(--font-heading);
  font-size: var(--text-lg);
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.05em;
}

.sidebar-menu {
  border-right: none;
  padding: var(--spacing-2) var(--spacing-2);

  :deep(.el-menu-item) {
    border-radius: var(--radius-md);
    margin-bottom: 2px;
    height: 44px;
    line-height: 44px;
    transition: all var(--duration-fast) var(--ease-default);

    &:hover {
      background: rgba(255, 255, 255, 0.08) !important;
    }

    &.is-active {
      background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)) !important;
      color: #fff !important;
      box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
    }
  }
}

.main-container {
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-gray-200);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-6);
  box-shadow: var(--shadow-sm);
  z-index: 5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
}

.collapse-btn {
  font-size: 20px;
  color: var(--color-gray-600);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  cursor: pointer;
  padding: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-md);
  transition: background var(--duration-fast);

  &:hover {
    background: var(--color-gray-100);
  }
}

.user-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-gray-700);
}

.main-content {
  padding: var(--spacing-6);
  overflow-y: auto;
  background: transparent;
}
</style>
