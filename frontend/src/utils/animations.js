// 动画工具类

export class AnimationUtils {
  // 数字滚动动画
  static animateNumber(element, start, end, duration = 1000) {
    const startTime = performance.now()
    const updateNumber = (currentTime) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      
      const current = Math.floor(start + (end - start) * this.easeOutCubic(progress))
      element.textContent = current.toLocaleString()
      
      if (progress < 1) {
        requestAnimationFrame(updateNumber)
      }
    }
    
    requestAnimationFrame(updateNumber)
  }
  
  // 缓动函数
  static easeOutCubic(t) {
    return 1 - Math.pow(1 - t, 3)
  }
  
  static easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
  }
  
  // 添加类动画
  static addClassWithAnimation(element, className, animationType = 'fade-in') {
    element.classList.add(animationType)
    setTimeout(() => {
      element.classList.remove(animationType)
      element.classList.add(className)
    }, 300)
  }
  
  // 震动效果
  static shake(element, intensity = 10, duration = 500) {
    element.style.animation = `none`
    setTimeout(() => {
      element.style.animation = `shake ${duration}ms ease-in-out`
    }, 10)
    
    setTimeout(() => {
      element.style.animation = ''
    }, duration)
  }
  
  // 脉冲效果
  static pulse(element, duration = 2000) {
    element.classList.add('pulse-animation')
    setTimeout(() => {
      element.classList.remove('pulse-animation')
    }, duration)
  }
  
  // 弹跳效果
  static bounce(element) {
    element.classList.add('bounce-animation')
    setTimeout(() => {
      element.classList.remove('bounce-animation')
    }, 1000)
  }
  
  // 淡入效果
  static fadeIn(element, duration = 300) {
    element.style.opacity = '0'
    element.style.display = 'block'
    
    const start = performance.now()
    const fadeIn = (currentTime) => {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.opacity = progress
      
      if (progress < 1) {
        requestAnimationFrame(fadeIn)
      }
    }
    
    requestAnimationFrame(fadeIn)
  }
  
  // 淡出效果
  static fadeOut(element, duration = 300) {
    const start = performance.now()
    const fadeOut = (currentTime) => {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.opacity = 1 - progress
      
      if (progress < 1) {
        requestAnimationFrame(fadeOut)
      } else {
        element.style.display = 'none'
      }
    }
    
    requestAnimationFrame(fadeOut)
  }
  
  // 滑入效果
  static slideIn(element, direction = 'up', duration = 400) {
    const transforms = {
      up: 'translateY(20px)',
      down: 'translateY(-20px)',
      left: 'translateX(-20px)',
      right: 'translateX(20px)'
    }
    
    element.style.transform = transforms[direction]
    element.style.opacity = '0'
    element.style.display = 'block'
    
    const start = performance.now()
    const slideIn = (currentTime) => {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      const easeProgress = this.easeOutCubic(progress)
      
      element.style.transform = `scale(${1 - (1 - easeProgress) * 0.1}) ${transforms[direction].replace(/[0-9.-]/g, (match) => match * (1 - easeProgress))}`
      element.style.opacity = easeProgress
      
      if (progress < 1) {
        requestAnimationFrame(slideIn)
      } else {
        element.style.transform = ''
        element.style.opacity = ''
      }
    }
    
    requestAnimationFrame(slideIn)
  }
  
  // 加载动画
  static createLoadingSpinner(container, size = 40) {
    const spinner = document.createElement('div')
    spinner.className = 'loading-spinner'
    spinner.style.width = `${size}px`
    spinner.style.height = `${size}px`
    
    for (let i = 0; i < 3; i++) {
      const dot = document.createElement('span')
      spinner.appendChild(dot)
    }
    
    container.appendChild(spinner)
    return spinner
  }
  
  // 骨架屏动画
  static createSkeleton(container, lines = 3) {
    const skeleton = document.createElement('div')
    skeleton.className = 'skeleton-container'
    
    for (let i = 0; i < lines; i++) {
      const line = document.createElement('div')
      line.className = 'skeleton-line skeleton-animation'
      line.style.height = `${16 + Math.random() * 8}px`
      line.style.marginBottom = '8px'
      skeleton.appendChild(line)
    }
    
    container.appendChild(skeleton)
    return skeleton
  }
  
  // 通知动画
  static showNotification(message, type = 'info', duration = 3000) {
    const notification = document.createElement('div')
    notification.className = `notification notification-${type} notification-slide-in`
    notification.textContent = message
    
    document.body.appendChild(notification)
    
    setTimeout(() => {
      notification.classList.remove('notification-slide-in')
      notification.classList.add('notification-slide-out')
      
      setTimeout(() => {
        document.body.removeChild(notification)
      }, 400)
    }, duration)
  }
  
  // 列表交错动画
  static staggeredAnimation(elements, animationClass, delay = 100) {
    elements.forEach((element, index) => {
      setTimeout(() => {
        element.classList.add(animationClass)
      }, index * delay)
    })
  }
  
  // 滚动触发动画
  static observeScroll(elements, animationClass, threshold = 0.1) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add(animationClass)
          observer.unobserve(entry.target)
        }
      })
    }, { threshold })
    
    elements.forEach(element => observer.observe(element))
    return observer
  }
  
  // 打字机效果
  static typewriter(element, text, speed = 50) {
    let index = 0
    element.textContent = ''
    
    const type = () => {
      if (index < text.length) {
        element.textContent += text.charAt(index)
        index++
        setTimeout(type, speed)
      }
    }
    
    type()
  }
  
  // 粒子效果
  static createParticles(container, count = 20) {
    const particles = []
    
    for (let i = 0; i < count; i++) {
      const particle = document.createElement('div')
      particle.className = 'particle'
      particle.style.cssText = `
        position: absolute;
        width: 4px;
        height: 4px;
        background: var(--primary-color);
        border-radius: 50%;
        pointer-events: none;
        left: ${Math.random() * 100}%;
        top: ${Math.random() * 100}%;
        opacity: 0;
      `
      
      container.appendChild(particle)
      particles.push(particle)
      
      // 随机动画
      const duration = 1000 + Math.random() * 2000
      const delay = Math.random() * 1000
      
      setTimeout(() => {
        particle.style.transition = `all ${duration}ms ease-out`
        particle.style.opacity = '1'
        particle.style.transform = `translate(${Math.random() * 200 - 100}px, ${Math.random() * 200 - 100}px) scale(0)`
        
        setTimeout(() => {
          particle.style.opacity = '0'
        }, duration - 200)
      }, delay)
    }
    
    return particles
  }
  
  // 清理动画
  static cleanup(element) {
    element.style.animation = ''
    element.style.transition = ''
    element.style.transform = ''
    element.style.opacity = ''
  }
}

// Vue组合式API动画钩子
export function useAnimation() {
  const animateNumber = (ref, end, duration = 1000) => {
    if (!ref.value) return
    
    const start = 0
    const startTime = performance.now()
    
    const update = (currentTime) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      
      const current = Math.floor(start + (end - start) * AnimationUtils.easeOutCubic(progress))
      ref.value.textContent = current.toLocaleString()
      
      if (progress < 1) {
        requestAnimationFrame(update)
      }
    }
    
    requestAnimationFrame(update)
  }
  
  const fadeIn = (ref, duration = 300) => {
    if (!ref.value) return
    
    ref.value.style.opacity = '0'
    ref.value.style.display = 'block'
    
    const start = performance.now()
    const animate = (currentTime) => {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      ref.value.style.opacity = progress
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      }
    }
    
    requestAnimationFrame(animate)
  }
  
  const shake = (ref, intensity = 10, duration = 500) => {
    if (!ref.value) return
    
    ref.value.style.animation = 'none'
    setTimeout(() => {
      ref.value.style.animation = `shake ${duration}ms ease-in-out`
    }, 10)
    
    setTimeout(() => {
      ref.value.style.animation = ''
    }, duration)
  }
  
  return {
    animateNumber,
    fadeIn,
    shake
  }
}
