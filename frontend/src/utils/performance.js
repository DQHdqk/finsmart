// 动画性能监控和优化

export class AnimationPerformance {
  constructor() {
    this.frameCount = 0
    this.lastTime = performance.now()
    this.fps = 60
    this.isLowPerformance = false
    this.animationQueue = []
    this.isProcessing = false
  }
  
  // FPS监控
  startFPSMonitoring() {
    const monitor = () => {
      this.frameCount++
      const currentTime = performance.now()
      
      if (currentTime - this.lastTime >= 1000) {
        this.fps = this.frameCount
        this.frameCount = 0
        this.lastTime = currentTime
        
        // 检测性能问题
        if (this.fps < 30) {
          this.isLowPerformance = true
          this.optimizeAnimations()
        } else if (this.fps > 50) {
          this.isLowPerformance = false
        }
      }
      
      requestAnimationFrame(monitor)
    }
    
    requestAnimationFrame(monitor)
  }
  
  // 优化动画
  optimizeAnimations() {
    // 减少动画复杂度
    document.documentElement.style.setProperty('--transition-normal', '0.15s ease')
    document.documentElement.style.setProperty('--transition-slow', '0.3s ease')
    
    // 禁用一些非关键动画
    const animatedElements = document.querySelectorAll('.pulse-animation, .bounce-animation')
    animatedElements.forEach(el => {
      el.style.animation = 'none'
    })
    
    // 降低阴影复杂度
    const cards = document.querySelectorAll('.card')
    cards.forEach(card => {
      card.style.boxShadow = 'var(--shadow-sm)'
    })
  }
  
  // 恢复正常动画
  restoreAnimations() {
    document.documentElement.style.setProperty('--transition-normal', '0.3s ease')
    document.documentElement.style.setProperty('--transition-slow', '0.5s ease')
  }
  
  // 动画队列管理
  queueAnimation(callback, priority = 'normal') {
    this.animationQueue.push({ callback, priority })
    
    if (!this.isProcessing) {
      this.processQueue()
    }
  }
  
  processQueue() {
    this.isProcessing = true
    
    const processNext = () => {
      if (this.animationQueue.length === 0) {
        this.isProcessing = false
        return
      }
      
      // 根据优先级排序
      this.animationQueue.sort((a, b) => {
        const priorities = { high: 3, normal: 2, low: 1 }
        return priorities[b.priority] - priorities[a.priority]
      })
      
      const animation = this.animationQueue.shift()
      
      // 使用requestAnimationFrame确保在下一帧执行
      requestAnimationFrame(() => {
        try {
          animation.callback()
        } catch (error) {
          console.error('Animation error:', error)
        }
        
        // 继续处理队列
        setTimeout(processNext, this.isLowPerformance ? 50 : 16)
      })
    }
    
    processNext()
  }
  
  // 节流动画
  throttleAnimation(callback, delay = 16) {
    let lastCall = 0
    return (...args) => {
      const now = performance.now()
      if (now - lastCall >= delay) {
        lastCall = now
        callback(...args)
      }
    }
  }
  
  // 防抖动画
  debounceAnimation(callback, delay = 100) {
    let timeout
    return (...args) => {
      clearTimeout(timeout)
      timeout = setTimeout(() => callback(...args), delay)
    }
  }
  
  // 检测设备性能
  detectDevicePerformance() {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    
    if (!gl) {
      return 'low'
    }
    
    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
    if (debugInfo) {
      const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL)
      
      // 检测低端GPU
      if (renderer.includes('Intel') && renderer.includes('HD Graphics')) {
        return 'low'
      }
      
      // 检测移动设备
      if (/Mobi|Android/i.test(navigator.userAgent)) {
        return 'medium'
      }
    }
    
    // 检测CPU核心数
    if (navigator.hardwareConcurrency && navigator.hardwareConcurrency < 4) {
      return 'low'
    }
    
    return 'high'
  }
  
  // 根据设备性能调整动画
  adjustAnimationsForDevice() {
    const performance = this.detectDevicePerformance()
    
    switch (performance) {
      case 'low':
        this.setLowPerformanceMode()
        break
      case 'medium':
        this.setMediumPerformanceMode()
        break
      case 'high':
        this.setHighPerformanceMode()
        break
    }
  }
  
  setLowPerformanceMode() {
    // 禁用复杂动画
    document.documentElement.classList.add('reduce-motion')
    
    // 减少动画数量
    const animatedElements = document.querySelectorAll('[class*="animation"]')
    animatedElements.forEach((el, index) => {
      if (index > 5) {
        el.classList.add('reduce-motion')
      }
    })
    
    // 简化阴影
    document.documentElement.style.setProperty('--shadow-lg', 'var(--shadow-sm)')
    document.documentElement.style.setProperty('--shadow-xl', 'var(--shadow-sm)')
  }
  
  setMediumPerformanceMode() {
    // 保持基本动画，但减少复杂度
    const complexAnimations = document.querySelectorAll('.gradient-animation, .particle')
    complexAnimations.forEach(el => {
      el.classList.add('reduce-motion')
    })
  }
  
  setHighPerformanceMode() {
    // 启用所有动画效果
    document.documentElement.classList.remove('reduce-motion')
  }
  
  // 内存使用监控
  monitorMemoryUsage() {
    if (performance.memory) {
      const memory = performance.memory
      const used = memory.usedJSHeapSize / memory.jsHeapSizeLimit
      
      if (used > 0.8) {
        // 内存使用过高，清理不必要的动画
        this.cleanupAnimations()
      }
    }
  }
  
  // 清理动画
  cleanupAnimations() {
    // 移除已完成的事件监听器
    const animatedElements = document.querySelectorAll('[class*="animation"]')
    animatedElements.forEach(el => {
      if (el.offsetParent === null) {
        // 元素不在DOM中，移除动画类
        el.className = el.className.replace(/animation-\w+/g, '')
      }
    })
    
    // 强制垃圾回收（如果可用）
    if (window.gc) {
      window.gc()
    }
  }
  
  // 预加载关键动画
  preloadCriticalAnimations() {
    const criticalAnimations = [
      'fade-in',
      'slide-in-up',
      'count-animation'
    ]
    
    criticalAnimations.forEach(animationName => {
      const style = document.createElement('style')
      style.textContent = `
        .${animationName} {
          will-change: transform, opacity;
        }
      `
      document.head.appendChild(style)
    })
  }
  
  // 初始化性能监控
  init() {
    this.startFPSMonitoring()
    this.adjustAnimationsForDevice()
    this.preloadCriticalAnimations()
    
    // 定期监控内存使用
    setInterval(() => {
      this.monitorMemoryUsage()
    }, 10000)
  }
}

// 全局性能管理器
export const animationPerformance = new AnimationPerformance()

// Vue插件
export const AnimationPerformancePlugin = {
  install(app) {
    app.config.globalProperties.$animationPerformance = animationPerformance
    
    // 在应用启动时初始化
    app.mixin({
      mounted() {
        if (this.$options.name === 'App') {
          animationPerformance.init()
        }
      }
    })
  }
}

// 自定义指令
export const vOptimizeAnimation = {
  mounted(el, binding) {
    const { value = 'normal' } = binding
    
    switch (value) {
      case 'low':
        el.classList.add('reduce-motion')
        break
      case 'high':
        el.classList.add('gpu-accelerated')
        break
      default:
        el.classList.add('will-change-transform')
    }
  },
  unmounted(el) {
    // 清理
    el.classList.remove('reduce-motion', 'gpu-accelerated', 'will-change-transform')
  }
}

// 组合式API
export function useAnimationPerformance() {
  const queueAnimation = (callback, priority = 'normal') => {
    animationPerformance.queueAnimation(callback, priority)
  }
  
  const optimizeForDevice = () => {
    animationPerformance.adjustAnimationsForDevice()
  }
  
  const isLowPerformance = computed(() => animationPerformance.isLowPerformance)
  
  return {
    queueAnimation,
    optimizeForDevice,
    isLowPerformance
  }
}
