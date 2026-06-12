<template>
  <div class="mobile-layout">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="user-info">
        <el-avatar :size="48" icon="User" class="user-avatar" />
        <div class="welcome-text">
          <h2>{{ title }}</h2>
        </div>
      </div>
      <div class="notification-icon">
        <el-icon :size="24"><Bell /></el-icon>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">
      <slot></slot>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-navbar">
      <div class="nav-item" :class="{ active: $route.path === '/dashboard' }" @click="router.push('/dashboard')">
        <el-icon :size="20"><House /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" :class="{ active: $route.path === '/report' }" @click="router.push('/report')">
        <el-icon :size="20"><Document /></el-icon>
        <span>报告</span>
      </div>
      <div class="nav-item add-item" @click="$emit('add-click')">
        <div class="add-icon">
          <el-icon :size="24"><Plus /></el-icon>
        </div>
        <span>记账</span>
      </div>
      <div class="nav-item" :class="{ active: $route.path === '/chart' }" @click="router.push('/chart')">
        <el-icon :size="20"><TrendCharts /></el-icon>
        <span>图表</span>
      </div>
      <div class="nav-item" :class="{ active: $route.path === '/profile' }" @click="showProfile">
        <el-icon :size="20"><User /></el-icon>
        <span>个人中心</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const props = defineProps({
  title: {
    type: String,
    default: 'FinSmart'
  }
})

const emit = defineEmits(['add-click'])

const router = useRouter()

const showProfile = () => {
  ElMessage.info('个人中心开发中')
}
</script>

<style lang="scss" scoped>
.mobile-layout {
  max-width: 100%;
  margin: 0;
  padding: 0 16px;
  font-family: var(--font-family);
  color: var(--text-primary);
  background: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 80px;
}

// 用户信息头部
.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .user-avatar {
      background: linear-gradient(135deg, #E6C4FE 0%, #D2C4FE 100%);
    }
    
    .welcome-text h2 {
      font-size: 1.25rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }
  
  .notification-icon {
    cursor: pointer;
    color: #6c757d;
    
    &:hover {
      color: #495057;
    }
  }
}

// 页面内容
.page-content {
  flex: 1;
}

// 底部导航栏
.bottom-navbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
  z-index: 1000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  
  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    color: #6c757d;
    
    &:hover {
      color: #E6C4FE;
    }
    
    &.active {
      color: #C4D3FE;
    }
    
    &.add-item {
      .add-icon {
        width: 48px;
        height: 48px;
        background: #000;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 2px;
        transition: all 0.3s ease;
        
        &:hover {
          background: #333;
          transform: scale(1.05);
        }
      }
      
      span {
        font-size: 0.7rem;
        font-weight: 500;
      }
    }
    
    span {
      font-size: 0.75rem;
      font-weight: 500;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .mobile-layout {
    padding: 0 12px;
  }
}
</style>
