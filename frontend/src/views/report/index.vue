<template>
  <div class="report-page">
    <div class="page-header">
      <h1>财务报告</h1>
      <p class="page-description">查看您的财务报告分析</p>
    </div>

    <!-- 报告周期选择 -->
    <div class="report-header">
      <el-radio-group v-model="reportPeriod" @change="fetchReportData" size="large">
        <el-radio-button label="week">周报</el-radio-button>
        <el-radio-button label="month">月报</el-radio-button>
        <el-radio-button label="year">年报</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" class="report-content">
      <div v-if="reportData" class="content">
        <!-- 统计摘要 -->
        <div class="stats-summary">
          <div class="stat-card income">
            <div class="stat-label">总收入</div>
            <div class="stat-value">¥{{ reportData.totalIncome || 0 }}</div>
          </div>
          <div class="stat-card expense">
            <div class="stat-label">总支出</div>
            <div class="stat-value">¥{{ reportData.totalExpense || 0 }}</div>
          </div>
          <div class="stat-card balance">
            <div class="stat-label">结余</div>
            <div class="stat-value">¥{{ reportData.balance || 0 }}</div>
          </div>
        </div>

        <!-- AI分析报告 -->
        <div class="card ai-report">
          <div class="card-header">
            <h3>AI分析报告</h3>
          </div>
          <div class="report-text">
            {{ reportData.aiAnalysis || '暂无数据' }}
          </div>
        </div>

        <!-- 分类支出 -->
        <div class="card category-stats">
          <div class="card-header">
            <h3>分类支出</h3>
          </div>
          <div class="category-list">
            <div v-for="item in reportData.categoryStats" :key="item.categoryId" class="category-item">
              <div class="category-name">{{ item.categoryName }}</div>
              <div class="category-amount">¥{{ item.amount }}</div>
              <el-progress :percentage="calculatePercentage(item.amount, reportData.totalExpense)" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getStatisticsReport } from '@/api/statistics'

const reportPeriod = ref('month')
const reportData = ref(null)
const loading = ref(false)
const showAddDialog = ref(false)
const chartRef = ref(null)

const fetchReportData = async () => {
  loading.value = true
  try {
    const data = await getStatisticsReport(reportPeriod.value)
    console.log('获取到的报告数据:', data) // 添加调试信息
    reportData.value = data
    
    // 渲染图表
    await nextTick()
    renderChart()
  } catch (error) {
    console.error('获取报告数据失败:', error)
    ElMessage.error('获取报告数据失败')
  } finally {
    loading.value = false
  }
}

const calculatePercentage = (amount, total) => {
  if (!total) return 0
  return Math.round((amount / total) * 100)
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value || !reportData.value) return
  
  const chart = echarts.init(chartRef.value)
  
  // 模拟趋势数据（实际应该从API获取）
  const trendData = {
    dates: [],
    income: [],
    expense: []
  }
  
  // 根据报告周期生成模拟数据
  const period = reportPeriod.value
  const now = dayjs()
  
  if (period === 'week') {
    // 生成7天的数据
    for (let i = 6; i >= 0; i--) {
      trendData.dates.push(now.subtract(i, 'day').format('MM-DD'))
      trendData.income.push(Math.floor(Math.random() * 500) + 100)
      trendData.expense.push(Math.floor(Math.random() * 300) + 50)
    }
  } else if (period === 'month') {
    // 生成30天的数据
    for (let i = 29; i >= 0; i--) {
      trendData.dates.push(now.subtract(i, 'day').format('MM-DD'))
      trendData.income.push(Math.floor(Math.random() * 500) + 100)
      trendData.expense.push(Math.floor(Math.random() * 300) + 50)
    }
  } else if (period === 'year') {
    // 生成12个月的数据
    for (let i = 11; i >= 0; i--) {
      trendData.dates.push(now.subtract(i, 'month').format('MM月'))
      trendData.income.push(Math.floor(Math.random() * 5000) + 1000)
      trendData.expense.push(Math.floor(Math.random() * 3000) + 500)
    }
  }
  
  const option = {
    title: {
      text: '收支趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['收入', '支出'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: trendData.income,
        itemStyle: {
          color: '#52c41a'
        },
        areaStyle: {
          color: 'rgba(82, 196, 26, 0.1)'
        }
      },
      {
        name: '支出',
        type: 'line',
        data: trendData.expense,
        itemStyle: {
          color: '#ff4d4f'
        },
        areaStyle: {
          color: 'rgba(255, 77, 79, 0.1)'
        }
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  fetchReportData()
})
</script>

<style lang="scss" scoped>
.report-page {
  max-width: 1200px;
  margin: 0 auto;
}

.report-header {
  margin-bottom: 24px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.report-content {
  min-height: 400px;
}

.content {
  display: grid;
  gap: 24px;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  .stat-card {
    background: var(--bg-white);
    border-radius: var(--radius-lg);
    padding: 24px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);

    .stat-label {
      color: var(--text-secondary);
      font-size: 14px;
      margin-bottom: 12px;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 700;
    }

    &.income {
      .stat-value {
        color: var(--success-color);
      }
    }

    &.expense {
      .stat-value {
        color: var(--danger-color);
      }
    }

    &.balance {
      .stat-value {
        color: var(--primary-color);
      }
    }
  }
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

.report-text {
  line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap;
}

.category-list {
  display: grid;
  gap: 16px;

  .category-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .category-name {
      width: 100px;
      color: var(--text-secondary);
    }

    .category-amount {
      width: 80px;
      text-align: right;
      font-weight: 600;
      color: var(--text-primary);
    }

    .el-progress {
      flex: 1;
    }
  }
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: 1fr;
  }
}

// 移动端样式
.mobile-report-container {
  .mobile-report-content {
    .period-selector {
      margin-bottom: 24px;
      text-align: center;
    }
    
    .stats-cards {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      margin-bottom: 24px;
      
      .stat-card {
        background: white;
        border-radius: 12px;
        padding: 16px;
        text-align: center;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        .stat-label {
          font-size: 0.8rem;
          color: #6c757d;
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 1.1rem;
          font-weight: 700;
          
          &.income { color: #27ae60; }
          &.expense { color: #e74c3c; }
          &.balance { color: #C4D3FE; }
        }
      }
    }
    
    .chart-section {
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
