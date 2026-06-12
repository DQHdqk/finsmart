<template>
  <div v-if="isMobile" class="mobile-chart-container">
    <MobileLayout title="数据图表" @add-click="showAddDialog = true">
      <div class="mobile-chart-content">
        <!-- 图表周期选择 -->
        <div class="period-selector">
          <el-radio-group v-model="chartPeriod" @change="fetchChartData" size="small">
            <el-radio-button label="week">周</el-radio-button>
            <el-radio-button label="month">月</el-radio-button>
            <el-radio-button label="year">年</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 收支趋势图 -->
        <div class="chart-section">
          <div class="chart-card">
            <h3>收支趋势</h3>
            <div ref="trendChartRef" style="height: 250px;"></div>
          </div>
        </div>

        <!-- 支出结构图 -->
        <div class="chart-section">
          <div class="chart-card">
            <h3>支出结构</h3>
            <div ref="pieChartRef" style="height: 250px;"></div>
          </div>
        </div>
      </div>
    </MobileLayout>
  </div>
  <div v-else class="chart-page">
    <div class="page-header">
      <h1>数据图表</h1>
      <p class="page-description">查看您的收支趋势和支出结构</p>
    </div>

    <!-- 图表周期选择 -->
    <div class="chart-header">
      <el-radio-group v-model="chartPeriod" @change="fetchChartData" size="large">
        <el-radio-button label="week">周</el-radio-button>
        <el-radio-button label="month">月</el-radio-button>
        <el-radio-button label="year">年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" class="chart-content">
      <div v-if="trendData && pieData" class="content">
        <!-- 收支趋势柱状图 -->
        <div class="card trend-chart-card">
          <div class="card-header">
            <h3>收支趋势</h3>
          </div>
          <div class="chart-container">
            <div ref="trendChartRef" class="chart"></div>
          </div>
        </div>

        <!-- 支出结构饼图 -->
        <div class="card expense-chart-card">
          <div class="card-header">
            <h3>支出结构</h3>
          </div>
          <div class="chart-container">
            <div ref="pieChartRef" class="chart"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getStatisticsTrend, getMonthStatistics } from '@/api/statistics'
import MobileLayout from '@/components/MobileLayout.vue'

// 检测是否为移动端
const isMobile = computed(() => window.innerWidth <= 768)

const chartPeriod = ref('month')
const trendData = ref(null)
const pieData = ref(null)
const loading = ref(false)
const showAddDialog = ref(false)

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const fetchChartData = async () => {
  loading.value = true
  try {
    console.log('开始获取图表数据，周期:', chartPeriod.value)
    
    // 获取趋势数据
    const trendResult = await getStatisticsTrend(chartPeriod.value)
    console.log('趋势数据:', trendResult)
    trendData.value = trendResult
    
    // 获取当月支出结构数据
    const currentMonth = dayjs().format('YYYY-MM')
    console.log('当前月份:', currentMonth)
    const pieResult = await getMonthStatistics(currentMonth)
    console.log('支出结构数据:', pieResult)
    pieData.value = pieResult
    
    console.log('trendData.value:', trendData.value)
    console.log('pieData.value:', pieData.value)
    
    await nextTick()
    console.log('开始初始化图表...')
    
    // 添加延迟确保DOM完全渲染
    setTimeout(() => {
      initCharts()
      console.log('图表初始化完成')
    }, 100)
  } catch (error) {
    console.error('获取图表数据失败:', error)
    ElMessage.error('获取图表数据失败')
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  console.log('initCharts被调用')
  console.log('trendChartRef.value:', trendChartRef.value)
  console.log('pieChartRef.value:', pieChartRef.value)
  console.log('trendData.value:', trendData.value)
  console.log('pieData.value:', pieData.value)
  
  // 初始化趋势柱状图
  if (trendChartRef.value && trendData.value) {
    console.log('开始初始化趋势图')
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
      console.log('趋势图实例创建成功')
    }

    const trendOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['支出', '收入']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: trendData.value.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '支出',
          type: 'bar',
          data: trendData.value.expenses || [],
          itemStyle: { color: '#FF3D71' },
          animationDelay: (idx) => idx * 100,
          animationDuration: 1000
        },
        {
          name: '收入',
          type: 'bar',
          data: trendData.value.incomes || [],
          itemStyle: { color: '#00D68F' },
          animationDelay: (idx) => idx * 100,
          animationDuration: 1000
        }
      ],
      animation: true,
      animationDuration: 1500,
      animationEasing: 'cubicOut'
    }

    console.log('趋势图配置:', trendOption)
    trendChart.setOption(trendOption)
    console.log('趋势图设置完成')
  } else {
    console.log('趋势图初始化条件不满足')
  }

  // 初始化支出结构饼图
  if (pieChartRef.value && pieData.value) {
    console.log('开始初始化饼图')
    if (!pieChart) {
      pieChart = echarts.init(pieChartRef.value)
      console.log('饼图实例创建成功')
    }

    const categoryData = pieData.value.categoryStats || []
    console.log('分类数据:', categoryData)

    const pieOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
      },
      series: [
        {
          name: '支出分类',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '50%'],
          data: categoryData.map((item, index) => ({
            name: item.categoryName,
            value: item.amount,
            animationDelay: index * 100,
            animationDuration: 1000
          })),
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          animationType: 'scale',
          animationEasing: 'elasticOut',
          animationDelay: (idx) => Math.random() * 200
        }
      ],
      animation: true,
      animationDuration: 2000,
      animationEasing: 'cubicOut'
    }

    console.log('饼图配置:', pieOption)
    pieChart.setOption(pieOption)
    console.log('饼图设置完成')
  } else {
    console.log('饼图初始化条件不满足')
    console.log('pieChartRef.value:', pieChartRef.value)
    console.log('pieData.value:', pieData.value)
  }
}

watch(() => chartPeriod.value, () => {
  fetchChartData()
})

onMounted(() => {
  fetchChartData()
  
  window.addEventListener('resize', () => {
    trendChart?.resize()
    pieChart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.chart-page {
  max-width: 1200px;
  margin: 0 auto;
}

.chart-header {
  margin-bottom: 24px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.chart-content {
  min-height: 400px;
}

.content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);

  .card-header {
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color);

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
}

.chart-container {
  height: 400px;
  width: 100%;

  .chart {
    width: 100%;
    height: 100%;
    min-height: 350px;
  }
}

@media (max-width: 900px) {
  .content {
    grid-template-columns: 1fr;
  }
}

// 移动端样式
.mobile-chart-container {
  .mobile-chart-content {
    .period-selector {
      margin-bottom: 24px;
      text-align: center;
    }
    
    .chart-section {
      margin-bottom: 24px;
      
      .chart-card {
        background: white;
        border-radius: 16px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        h3 {
          font-size: 1.1rem;
          font-weight: 600;
          color: #2c3e50;
          margin: 0 0 16px 0;
        }
      }
    }
  }
}
</style>
