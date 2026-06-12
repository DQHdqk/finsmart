<template>
  <transition
    :name="transitionName"
    @before-enter="beforeEnter"
    @enter="enter"
    @after-enter="afterEnter"
    @before-leave="beforeLeave"
    @leave="leave"
    @after-leave="afterLeave"
  >
    <slot />
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const transitionName = ref('page-transition')

// 根据路由变化决定过渡效果
watch(() => route.path, (toPath, fromPath) => {
  // 如果是从更深层的路由回来，使用不同的动画
  if (toPath.length < fromPath.length) {
    transitionName.value = 'slide-up'
  } else if (toPath.length > fromPath.length) {
    transitionName.value = 'slide-up'
  } else {
    transitionName.value = 'page-transition'
  }
})

// 动画钩子函数
const beforeEnter = (el) => {
  el.style.opacity = 0
  el.classList.add('gpu-accelerated')
}

const enter = (el, done) => {
  setTimeout(() => {
    el.style.opacity = 1
    el.style.transform = 'translateX(0)'
    done()
  }, 100)
}

const afterEnter = (el) => {
  el.classList.remove('gpu-accelerated')
}

const beforeLeave = (el) => {
  el.classList.add('gpu-accelerated')
}

const leave = (el, done) => {
  setTimeout(() => {
    el.style.opacity = 0
    el.style.transform = 'translateX(-30px)'
    done()
  }, 100)
}

const afterLeave = (el) => {
  el.classList.remove('gpu-accelerated')
}
</script>

<style scoped>
/* 过渡动画样式 */
.page-transition-enter-active,
.page-transition-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-transition-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.page-transition-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}
</style>
