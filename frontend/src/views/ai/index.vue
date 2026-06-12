<template>
  <div class="ai-page">
    <div class="page-header">
      <h1>AI分析</h1>
      <p class="page-description">智能分析您的财务状况，提供个性化建议</p>
    </div>

    <!-- 月份选择器 -->
    <div class="card month-selector">
      <div class="selector-content">
        <label>分析月份:</label>
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          @change="handleMonthChange"
          style="width: 150px"
        />
        <el-button type="primary" :loading="analyzing" @click="analyzeMonth">
          <el-icon><TrendCharts /></el-icon>
          开始分析
        </el-button>
      </div>
    </div>

    <!-- AI分析结果 -->
    <div class="analysis-content" v-loading="analyzing">
      <div v-if="!analysisResult && !analyzing" class="no-analysis">
        <el-icon><TrendCharts /></el-icon>
        <p>选择月份开始AI分析</p>
      </div>
      
      <div v-else-if="analysisResult" class="analysis-results">
        <el-tabs v-model="activeSection" class="section-tabs">
          <el-tab-pane label="AI报告" name="report" />
          <el-tab-pane label="图表分析" name="charts" />
        </el-tabs>

        <div v-if="activeSection === 'report'" class="report-section">
          <div class="card ai-insights">
            <div class="card-header report-header">
              <div>
                <h3>智能洞察</h3>
                <p class="report-tip">本月 AI 分析报告支持周/月/年视图</p>
              </div>
              <el-radio-group v-model="reportPeriod" size="small" class="period-switch">
                <el-radio-button label="week">周报</el-radio-button>
                <el-radio-button label="month">月报</el-radio-button>
                <el-radio-button label="year">年报</el-radio-button>
              </el-radio-group>
            </div>
            
            <div class="insights-content">
              <div class="insight-summary">
                <h4>{{ analysisResult.periodReports[reportPeriod].title }}</h4>
                <div class="summary-text" v-html="analysisResult.periodReports[reportPeriod].content"></div>
              </div>

              <div class="insights-grid" v-if="analysisResult.insights?.length">
                <div
                  v-for="(insight, index) in analysisResult.insights"
                  :key="index"
                  class="insight-item"
                  :class="insight.type"
                >
                  <div class="insight-icon">
                    <el-icon>
                      <component :is="getInsightIcon(insight.type)" />
                    </el-icon>
                  </div>
                  <div class="insight-content">
                    <div class="insight-title">{{ insight.title }}</div>
                    <div class="insight-description">{{ insight.description }}</div>
                  </div>
                </div>
              </div>
            
              <div class="recommendations" v-if="analysisResult.recommendations?.length">
                <h4>理财建议</h4>
                <ul>
                  <li v-for="(rec, index) in analysisResult.recommendations" :key="index">
                    {{ rec }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeSection === 'charts'" class="charts-section">
          <div class="card chart-control-card">
            <div class="chart-control-title">支出/收入柱状图</div>
            <el-radio-group v-model="chartPeriod" size="small" class="chart-switch">
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>

          <div class="card chart-section">
            <div class="card-header">
              <h3>{{ chartPeriodLabel }} 支出 / 收入</h3>
            </div>
            <div class="chart-container">
              <div ref="barChartRef" class="chart"></div>
            </div>
          </div>

          <div class="card chart-grid">
            <div class="chart-card">
              <div class="card-header">
                <h3>每日支出趋势</h3>
              </div>
              <div class="chart-container">
                <div ref="trendChartRef" class="chart"></div>
              </div>
            </div>
            <div class="chart-card">
              <div class="card-header">
                <h3>支出结构</h3>
              </div>
              <div class="chart-container">
                <div ref="chartRef" class="chart"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getAiAnalysis, getMonthStatistics } from '@/api/ai'
import { getMonthStatistics as getStatistics } from '@/api/statistics'

// 响应式数据
const route = useRoute()
const analyzing = ref(false)
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const analysisResult = ref(null)
const activeSection = ref(route.query.section === 'charts' ? 'charts' : 'report')
const chartPeriod = ref('week')
const reportPeriod = ref('week')

// 图表实例
const chartRef = ref()
const trendChartRef = ref()
const barChartRef = ref()
let pieChart = null
let trendChart = null
let barChart = null

// 获取洞察图标
const getInsightIcon = (type) => {
  const iconMap = {
    'warning': 'Warning',
    'success': 'Select',
    'info': 'InfoFilled',
    'tip': 'Lightbulb'
  }
  return iconMap[type] || 'InfoFilled'
}

// 获取趋势样式类
const getTrendClass = (value) => {
  if (!value) return ''
  return value > 0 ? 'increase' : 'decrease'
}

// 格式化趋势
const formatTrend = (value) => {
  if (!value) return '0%'
  const prefix = value > 0 ? '+' : ''
  return `${prefix}${value.toFixed(1)}%`
}

// 月份变化处理
const handleMonthChange = () => {
  analysisResult.value = null
  if (chartRef.value) {
    pieChart?.dispose()
    pieChart = null
  }
  if (trendChartRef.value) {
    trendChart?.dispose()
    trendChart = null
  }
  if (barChartRef.value) {
    barChart?.dispose()
    barChart = null
  }
}

// 分析月份
const analyzeMonth = async () => {
  if (!selectedMonth.value) {
    ElMessage.warning('请选择月份')
    return
  }
  
  analyzing.value = true
  try {
    // 获取AI分析结果
    const aiData = await getAiAnalysis(selectedMonth.value)
    
    // 获取统计数据用于图表
    const statsData = await getStatistics(selectedMonth.value)
    
    // 模拟AI分析结果（实际应该从后端获取）
    const mockResult = {
      summary: {
        title: `${selectedMonth.value} 财务状况分析`,
        content: `本月总支出为 ¥${statsData.totalExpense || 0}，相比上月${Math.random() > 0.5 ? '有所增加' : '有所减少'}。主要支出集中在餐饮和购物类别，建议适当控制非必要开支。`
      },
      insights: [
        {
          type: 'warning',
          title: '支出偏高',
          description: '本月餐饮支出超过预算30%，建议适当控制外出就餐频率'
        },
        {
          type: 'success',
          title: '收入稳定',
          description: '本月收入保持稳定，财务状况良好'
        },
        {
          type: 'info',
          title: '储蓄建议',
          description: '建议将月收入的20%用于储蓄和投资'
        }
      ],
      recommendations: [
        '制定月度预算计划，控制不必要开支',
        '增加收入来源，提高财务稳定性',
        '建立应急基金，应对突发支出',
        '定期 review 财务状况，及时调整策略'
      ],
      trends: {
        monthOverMonth: (Math.random() - 0.5) * 20,
        dailyAverage: (statsData.totalExpense || 0) / 30,
        topCategory: '餐饮'
      },
      categoryData: [
        { name: '餐饮', value: Math.floor(Math.random() * 1000) + 500 },
        { name: '购物', value: Math.floor(Math.random() * 800) + 300 },
        { name: '交通', value: Math.floor(Math.random() * 500) + 200 },
        { name: '娱乐', value: Math.floor(Math.random() * 400) + 100 },
        { name: '其他', value: Math.floor(Math.random() * 300) + 100 }
      ],
      periodReports: {
        week: {
          title: '本周财务分析',
          content: `本周支出趋势保持稳定，收入较预算略有增长。建议继续保持当前消费节奏，适度增加储蓄。`
        },
        month: {
          title: '本月财务分析',
          content: `本月总支出为 ¥${statsData.totalExpense || 0}，日均支出为 ¥${(statsData.dailyAvgExpense || 0).toFixed(2)}。重点关注餐饮与购物类支出。`
        },
        year: {
          title: '本年财务分析',
          content: `本年度累计支出较去年同期${Math.random() > 0.5 ? '上升' : '下降'}。建议按照季度计划优化预算分配。`
        }
      },
      periodStats: {
        week: generatePeriodStats('week'),
        month: generatePeriodStats('month'),
        year: generatePeriodStats('year')
      },
      dailyData: generateDailyData()
    }
    
    analysisResult.value = mockResult
    
    // 等待DOM更新后初始化图表
    await nextTick()
    initCharts()
    
  } catch (error) {
    console.error('分析失败:', error)
    ElMessage.error('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

const generatePeriodStats = (type) => {
  if (type === 'week') {
    const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return {
      labels,
      expense: labels.map(() => Math.floor(Math.random() * 500) + 200),
      income: labels.map(() => Math.floor(Math.random() * 400) + 150)
    }
  }

  if (type === 'month') {
    const labels = ['第1周', '第2周', '第3周', '第4周']
    return {
      labels,
      expense: labels.map(() => Math.floor(Math.random() * 1800) + 800),
      income: labels.map(() => Math.floor(Math.random() * 1500) + 600)
    }
  }

  const labels = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return {
    labels,
    expense: labels.map(() => Math.floor(Math.random() * 6000) + 2000),
    income: labels.map(() => Math.floor(Math.random() * 5000) + 1800)
  }
}

const chartPeriodLabel = computed(() => {
  if (chartPeriod.value === 'week') return '周'
  if (chartPeriod.value === 'month') return '月'
  return '年'
})

const updateBarChart = () => {
  if (!analysisResult.value || !barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)

  const periodData = analysisResult.value.periodStats[chartPeriod.value]
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['支出', '收入'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '16%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: periodData.labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E8EDF3' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F2F6FA' } },
      axisLabel: { formatter: '¥{value}' }
    },
    series: [
      {
        name: '支出',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#FF3D71' },
        data: periodData.expense
      },
      {
        name: '收入',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#00D68F' },
        data: periodData.income
      }
    ]
  }

  barChart.setOption(option)
}

// 初始化图表
const initCharts = () => {
  if (!analysisResult.value) return
  
  // 初始化饼图
  if (chartRef.value) {
    pieChart = echarts.init(chartRef.value)
    
    const pieOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
      },
      legend: {
        bottom: '5%',
        left: 'center'
      },
      series: [
        {
          name: '支出分类',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: analysisResult.value.categoryData
        }
      ],
      color: ['#6C5CE7', '#00D68F', '#FFAA00', '#FF3D71', '#A0AEC0']
    }
    
    pieChart.setOption(pieOption)
  }
  
  // 初始化趋势图
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    
    const trendOption = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: analysisResult.value.dailyData.map(item => item.date.split('-').slice(1).join('/'))
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '日支出',
          type: 'line',
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(108, 92, 231, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(108, 92, 231, 0.1)'
                }
              ]
            }
          },
          lineStyle: {
            color: '#6C5CE7',
            width: 3
          },
          itemStyle: {
            color: '#6C5CE7'
          },
          data: analysisResult.value.dailyData.map(item => item.amount)
        }
      ]
    }
    
    trendChart.setOption(trendOption)
  }

  updateBarChart()
}

watch([chartPeriod, analysisResult], () => {
  if (analysisResult.value) {
    nextTick(() => {
      updateBarChart()
    })
  }
})

watch(
  () => route.query.section,
  (value) => {
    activeSection.value = value === 'charts' ? 'charts' : 'report'
  }
)

// 页面加载时自动分析当前月
onMounted(() => {
  analyzeMonth()
})

// 窗口大小变化时重新调整图表
window.addEventListener('resize', () => {
  pieChart?.resize()
  trendChart?.resize()
  barChart?.resize()
})
</script>

<style lang="scss" scoped>
.ai-page {
  max-width: 1200px;
  margin: 0 auto;
}

.month-selector {
  margin-bottom: 24px;
  
  .selector-content {
    display: flex;
    align-items: center;
    gap: 16px;
    
    label {
      font-size: 14px;
      color: var(--text-secondary);
      font-weight: 500;
    }
  }
}

.analysis-content {
  min-height: 400px;
  
  .no-analysis {
    text-align: center;
    padding: 80px 0;
    color: var(--text-light);
    
    .el-icon {
      font-size: 64px;
      margin-bottom: 16px;
      display: block;
    }
    
    p {
      font-size: 16px;
    }
  }
}

.analysis-results {
  display: grid;
  gap: 24px;
}

.section-tabs {
  margin-bottom: 16px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.report-tip {
  color: var(--text-secondary);
  margin: 8px 0 0;
}

.period-switch,
.chart-switch {
  display: inline-flex;
}

.chart-control-card {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.chart-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  padding: 0;
}

.chart-card .card-header {
  padding: 20px 24px 0;
}

.chart-card .chart-container {
  padding: 0 24px 24px;
}

.card.chart-section,
.card.chart-control-card,
.card.ai-insights {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.chart-container {
  min-height: 340px;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 900px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

.ai-insights {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
  }
  
  .insights-content {
    .insight-summary {
      margin-bottom: 32px;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 12px 0;
      }
      
      .summary-text {
        font-size: 14px;
        line-height: 1.6;
        color: var(--text-secondary);
        
        :deep(strong) {
          color: var(--primary-color);
          font-weight: 600;
        }
      }
    }
    
    .insights-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-bottom: 32px;
      
      .insight-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        border-radius: var(--radius-md);
        border-left: 4px solid;
        
        &.warning {
          background: rgba(255, 170, 0, 0.05);
          border-left-color: var(--warning-color);
        }
        
        &.success {
          background: rgba(0, 214, 143, 0.05);
          border-left-color: var(--success-color);
        }
        
        &.info {
          background: rgba(108, 92, 231, 0.05);
          border-left-color: var(--primary-color);
        }
        
        .insight-icon {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          .el-icon {
            font-size: 16px;
          }
        }
        
        &.warning .insight-icon {
          background: rgba(255, 170, 0, 0.1);
          color: var(--warning-color);
        }
        
        &.success .insight-icon {
          background: rgba(0, 214, 143, 0.1);
          color: var(--success-color);
        }
        
        &.info .insight-icon {
          background: rgba(108, 92, 231, 0.1);
          color: var(--primary-color);
        }
        
        .insight-content {
          flex: 1;
          
          .insight-title {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 4px;
          }
          
          .insight-description {
            font-size: 13px;
            color: var(--text-secondary);
            line-height: 1.4;
          }
        }
      }
    }
    
    .recommendations {
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 16px 0;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        
        li {
          font-size: 14px;
          color: var(--text-secondary);
          line-height: 1.6;
          margin-bottom: 8px;
          
          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

.chart-section {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
    
    .chart-legend {
      display: flex;
      gap: 16px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: var(--text-secondary);
        
        .legend-color {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }
      }
    }
  }
  
  .chart-container {
    .chart {
      width: 100%;
      height: 400px;
    }
  }
}

.trend-analysis {
  .card-header {
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
  }
  
  .trend-content {
    .trend-summary {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 20px;
      margin-bottom: 32px;
      
      .trend-item {
        text-align: center;
        
        .trend-label {
          font-size: 12px;
          color: var(--text-secondary);
          margin-bottom: 8px;
        }
        
        .trend-value {
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
          
          &.increase {
            color: var(--danger-color);
          }
          
          &.decrease {
            color: var(--success-color);
          }
        }
      }
    }
    
    .trend-chart {
      .chart {
        width: 100%;
        height: 300px;
      }
    }
  }
}

@media (max-width: 768px) {
  .month-selector .selector-content {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .insights-grid {
    grid-template-columns: 1fr;
  }
  
  .trend-summary {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .chart-section .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .chart-legend {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
