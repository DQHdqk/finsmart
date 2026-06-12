<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <transition name="sidebar-slide">
      <div v-if="sidebarVisible && !shouldHideSidebar" class="sidebar">
        <div class="logo">
          <h2>FinSmart</h2>
        </div>
        <el-menu
          :default-active="$route.path"
          class="sidebar-menu"
          background-color="#FFFFFF"
          text-color="#2C3E50"
          active-text-color="#6C5CE7"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            @click="handleMenuClick(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
        
        <!-- 用户信息 -->
        <div class="user-info">
          <el-dropdown @command="handleCommand" trigger="click" placement="top">
            <el-avatar :size="40" icon="User" class="user-avatar" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </transition>

    <!-- 遮罩层 -->
    <div v-if="!sidebarVisible && isMobile" class="sidebar-overlay" @click="toggleSidebar" />

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div v-if="!isHomePage || !isMobile" class="top-navbar">
        <div class="navbar-left">
          <!-- 展开菜单按钮 -->
          <el-button
            v-if="!sidebarVisible"
            type="primary"
            icon="Menu"
            circle
            size="large"
            @click="toggleSidebar"
            class="menu-toggle-btn"
          />
          <!-- 返回首页按钮 -->
          <el-button
            v-if="!isHomePage"
            type="text"
            icon="ArrowLeft"
            @click="goToHome"
            class="home-btn"
          >
            首页
          </el-button>
        </div>
        <div class="navbar-right">
          <!-- 用户头像（仅在侧边栏隐藏时显示） -->
          <el-avatar
            v-if="!sidebarVisible"
            :size="36"
            icon="User"
            class="navbar-avatar"
            @click="toggleSidebar"
          />
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="page-wrapper" :class="{ 'dashboard-mobile': ['/dashboard', '/subscription', '/wish', '/report', '/chart'].includes(route.path) && isMobile }">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const sidebarVisible = ref(true)
const screenWidth = ref(window.innerWidth)

// 菜单项配置
const menuItems = ref([
  {
    path: '/dashboard',
    title: '首页',
    icon: 'House'
  },
  {
    path: '/bill',
    title: '账单',
    icon: 'Document'
  },
  {
    path: '/chart',
    title: '图表',
    icon: 'TrendCharts'
  },
  {
    path: '/report',
    title: '报告',
    icon: 'DataAnalysis'
  },
  {
    path: '/budget',
    title: '预算',
    icon: 'Management'
  },
  {
    path: '/subscription',
    title: '订阅管家',
    icon: 'Bell'
  },
  {
    path: '/wish',
    title: '愿望清单',
    icon: 'Star'
  },
  {
    path: '/share',
    title: 'AA共享',
    icon: 'Share'
  }
])

// 计算属性：是否为移动端
const isMobile = computed(() => screenWidth.value <= 768)

// 计算属性：是否为首页
const isHomePage = computed(() => route.path === '/dashboard')

// 在移动端隐藏侧边栏
const shouldHideSidebar = computed(() => {
  return isMobile.value || 
         ['/dashboard', '/bill', '/subscription', '/wish', '/report', '/chart', '/share'].includes(route.path)
})

// 监听窗口大小变化
const handleResize = () => {
  screenWidth.value = window.innerWidth
  // 在桌面端宽度扩大时，展开侧边栏
  if (screenWidth.value > 768) {
    sidebarVisible.value = true
  }
}

// 切换侧边栏显示/隐藏
const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

// 移动端点击菜单项后关闭侧边栏
const closeSidebarOnMobile = () => {
  if (isMobile.value) {
    sidebarVisible.value = false
  }
}

// 菜单项点击事件处理
const handleMenuClick = (path) => {
  router.push(path)
  closeSidebarOnMobile()
}

// 返回首页
const goToHome = () => {
  router.push('/dashboard')
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
      .then(() => {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.success('退出成功')
      })
      .catch(() => {
        // 用户取消退出
      })
  } else if (command === 'profile') {
    // 个人信息功能，可以后续扩展
    ElMessage.info('个人信息功能开发中...')
  }
}

// 退出登录（保留作为备用）
const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: var(--bg-gray);
  position: relative;
}

// 侧边栏过渡动画
.sidebar-slide-enter-active,
.sidebar-slide-leave-active {
  transition: all 0.3s ease;
}

.sidebar-slide-enter-from {
  transform: translateX(-100%);
}

.sidebar-slide-leave-to {
  transform: translateX(-100%);
}

.sidebar {
  width: 240px;
  background: var(--bg-white);
  border-right: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1001;
  flex-shrink: 0;
}

.logo {
  padding: 24px 20px;
  border-bottom: 1px solid var(--border-color);
  
  h2 {
    color: var(--primary-color);
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.sidebar-menu {
  flex: 1;
  border: none;
  padding: 16px 0;
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin: 4px 12px;
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      background: rgba(108, 92, 231, 0.1);
      color: var(--primary-color);
    }
    
    &.is-active {
      background: var(--primary-color);
      color: white;
      
      .el-icon {
        color: white;
      }
    }
  }
  
  :deep(.el-icon) {
    margin-right: 12px;
    font-size: 18px;
  }
}

.user-info {
  padding: 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 12px;
  
  .user-avatar {
    cursor: pointer;
    transition: transform 0.2s ease;
    flex-shrink: 0;

    &:hover {
      transform: scale(1.1);
    }
  }
  
  .user-details {
    flex: 1;
    
    .username {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
      margin-bottom: 4px;
    }
  }
}

// 遮罩层
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  animation: fadeIn 0.3s ease;

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
}

// 主内容区
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部导航栏
.top-navbar {
  height: 60px;
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  flex-shrink: 0;
  gap: 16px;

  .navbar-left,
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .menu-toggle-btn {
    animation: slideInLeft 0.3s ease;

    @keyframes slideInLeft {
      from {
        opacity: 0;
        transform: translateX(-20px);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }
  }

  .home-btn {
    padding: 0 12px;
    font-size: 14px;

    &:hover {
      color: var(--primary-color);
    }
  }

  .navbar-avatar {
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.1);
    }
  }
}

// 页面内容包装器
.page-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-gray);
  
  // 在首页移动端不添加padding，因为dashboard自己处理了
  &.dashboard-mobile {
    padding: 0;
    background: #f8f9fa;
  }
}

// 桌面端
@media (min-width: 769px) {
  .sidebar-overlay {
    display: none !important;
  }

  .top-navbar {
    display: none;
  }

  .main-content {
    overflow-y: auto;
  }

  .page-wrapper {
    padding: 24px;
  }
}

// 平板和移动端
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    box-shadow: var(--shadow-lg);
  }

  .top-navbar {
    display: flex;
    background: var(--bg-white);
    border-bottom: 1px solid var(--border-color);
    z-index: 999;
  }

  .page-wrapper {
    padding: 16px;
  }
}
</style>
