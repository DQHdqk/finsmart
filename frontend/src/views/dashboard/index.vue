<template>
  <div class="dashboard">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="user-info">
        <el-dropdown @command="handleCommand" trigger="click" placement="bottom">
          <el-avatar :size="48" icon="User" class="user-avatar" />
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
        <div class="welcome-text">
          <h2>你好 Farhan，欢迎回来</h2>
        </div>
      </div>
      <div class="notification-icon">
        <el-icon :size="24"><Bell /></el-icon>
      </div>
    </div>

    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-content">
        <div class="balance-label">可用余额</div>
        <div class="balance-amount">￥{{ monthStats.totalIncome ? (monthStats.totalIncome - monthStats.totalExpense).toFixed(2) : '0.00' }}</div>
        <div class="balance-expiry">EX 06/10</div>
      </div>
      <div class="balance-decoration">
        <div class="glow-circle"></div>
        <div class="add-button">
          <el-icon :size="24"><Plus /></el-icon>
        </div>
      </div>
    </div>

    <!-- 快捷操作按钮 -->
    <div class="action-buttons">
      <div class="action-button" @click="router.push('/bill')">
        <div class="action-icon">💸</div>
        <div class="action-label">账单管理</div>
      </div>
      <div class="action-button" @click="router.push('/subscription')">
        <div class="action-icon">🔔</div>
        <div class="action-label">订阅提醒</div>
      </div>
      <div class="action-button" @click="router.push('/share')">
        <div class="action-icon">💳</div>
        <div class="action-label">AA共享</div>
      </div>
      <div class="action-button" @click="router.push('/wish')">
        <div class="action-icon">🌟</div>
        <div class="action-label">愿望清单</div>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="recent-transactions">
      <div class="transactions-header">
        <h3>最近交易</h3>
        <div class="see-all" @click="router.push('/bill')">查看全部</div>
      </div>
      <div class="transaction-list" v-loading="billsLoading">
        <div v-if="recentBills.length === 0" class="empty-state">
          <el-icon><Document /></el-icon>
          <p>暂无交易记录</p>
        </div>
        <div v-else>
          <div v-for="(bill, index) in recentBills" :key="bill.id" class="transaction-item">
            <div class="transaction-left">
              <div class="transaction-icon" :style="{ backgroundColor: getCategoryColor(bill.categoryId) }">
                {{ bill.categoryName?.charAt(0) || '分' }}
              </div>
              <div class="transaction-info">
                <div class="transaction-name">{{ bill.categoryName }}</div>
                <div class="transaction-date">{{ formatDate(bill.billDate) }}</div>
              </div>
            </div>
            <div class="transaction-amount" :class="bill.type === 'EXPENSE' || bill.type === 1 ? 'expense' : 'income'">
              {{ bill.type === 'EXPENSE' || bill.type === 1 ? '-' : '+' }}￥{{ bill.amount }}
            </div>
          </div>
        </div>
      </div>
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
      <div class="nav-item add-item" @click="showRecordMenu = !showRecordMenu">
        <div class="add-icon">
          <el-icon :size="24"><Plus /></el-icon>
        </div>
        <span>记账方式</span>
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


    <!-- 记账弹窗菜单 -->
    <transition name="popup">
      <div v-if="showRecordMenu" class="record-menu">
        <div class="menu-item" @click="startSmartRecord">
          <div class="item-icon">✍️</div>
          <div class="item-title">智能记账</div>
          <div class="item-desc">文字描述</div>
        </div>
        <div class="menu-item" @click="startPhotoRecord">
          <div class="item-icon">📷</div>
          <div class="item-title">拍照识别</div>
          <div class="item-desc">上传图片</div>
        </div>
        <div class="menu-item" @click="startManualRecord">
          <div class="item-icon">📝</div>
          <div class="item-title">手动记账</div>
          <div class="item-desc">传统方式</div>
        </div>
        <div class="menu-item disabled" @click="showAppOnlyTip">
          <div class="item-icon">⚡</div>
          <div class="item-title">一键闪记</div>
          <div class="item-desc">仅App支持</div>
        </div>
      </div>
    </transition>

    <!-- 记账弹窗 - 智能记账 -->
    <el-dialog v-model="showSmartRecord" title="智能记账" width="500px">
      <el-input
        v-model="smartRecordText"
        type="textarea"
        :rows="4"
        placeholder="例如：早餐花了15块，午餐47块，打车23块..."
      />
      <template #footer>
        <el-button @click="showSmartRecord = false">取消</el-button>
        <el-button type="primary" :loading="recordLoading" @click="submitSmartRecord">识别</el-button>
      </template>
    </el-dialog>

    <!-- 记账弹窗 - 拍照识别 -->
    <el-dialog v-model="showPhotoRecord" title="拍照识别" width="500px">
      <el-upload
        accept="image/*"
        :auto-upload="false"
        @change="handlePhotoChange"
      >
        <el-button>选择图片</el-button>
      </el-upload>
      <template #footer>
        <el-button @click="showPhotoRecord = false">取消</el-button>
        <el-button type="primary" :loading="recordLoading" @click="submitPhotoRecord">识别</el-button>
      </template>
    </el-dialog>

    <!-- 记账弹窗 - 结果确认 -->
    <el-dialog v-model="showRecordResult" title="确认账单信息" width="500px">
      <el-form :model="recordForm" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="recordForm.type">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="recordForm.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in expenseCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="recordForm.amount" :precision="2" :min="0.01" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="recordForm.billDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordResult = false">取消</el-button>
        <el-button type="primary" :loading="recordLoading" @click="submitRecord">保存</el-button>
      </template>
    </el-dialog>

    <!-- 手动记账弹窗 -->
    <el-dialog v-model="showManualRecord" title="手动记账" width="500px">
      <el-form :model="recordForm" label-width="80px" @submit.prevent="submitRecord">
        <el-form-item label="类型">
          <el-radio-group v-model="recordForm.type" @change="updateCategories">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="recordForm.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in currentCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="recordForm.amount" :precision="2" :min="0.01" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="recordForm.billDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showManualRecord = false">取消</el-button>
        <el-button type="primary" :loading="recordLoading" @click="submitRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getMonthStatistics, getTodayStatistics } from '@/api/statistics'
import { getBillPage, addBill } from '@/api/bill'
import { getCategoryList } from '@/api/category'
import { textOCR, imageOCR } from '@/api/ocr'

const router = useRouter()

// 统计数据
const monthStats = ref({})
const todayStats = ref({})
const recentBills = ref([])
const categories = ref({ EXPENSE: [], INCOME: [] })
const billsLoading = ref(false)

// UI状态
const showRecordMenu = ref(false)
const showSmartRecord = ref(false)
const showPhotoRecord = ref(false)
const showManualRecord = ref(false)
const showRecordResult = ref(false)
const recordLoading = ref(false)

// 记账表单
const recordForm = reactive({
  type: 'EXPENSE',
  categoryId: '',
  amount: 0,
  billDate: dayjs().format('YYYY-MM-DD'),
  remark: ''
})

const smartRecordText = ref('')
const selectedPhoto = ref(null)
const miniChartRef = ref(null)
let miniChart = null

// 获取当前分类列表
const currentCategories = computed(() => categories.value[recordForm.type] || [])
const expenseCategories = computed(() => categories.value.EXPENSE || [])

// 获取分类颜色
const getCategoryColor = (categoryId) => {
  const colors = ['#D2C4FE', '#B68FC4', '#C4D3FE', '#A8E6FF', '#E6C4FE']
  return colors[categoryId % colors.length]
}

// 格式化日期
const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

// 获取月度统计
const fetchMonthStats = async () => {
  try {
    const currentMonth = dayjs().format('YYYY-MM')
    const data = await getMonthStatistics(currentMonth)
    
    // 如果API返回的数据没有dailyAvgExpense字段，则计算它
    if (!data.dailyAvgExpense && data.totalExpense) {
      const daysInMonth = dayjs().daysInMonth()
      data.dailyAvgExpense = data.totalExpense / daysInMonth
    }
    
    monthStats.value = data
  } catch (error) {
    console.error('获取月度统计失败:', error)
  }
}

// 获取今日统计
const fetchTodayStats = async () => {
  try {
    const data = await getTodayStatistics()
    todayStats.value = data
  } catch (error) {
    console.error('获取今日统计失败:', error)
  }
}

// 获取最近账单
const fetchRecentBills = async () => {
  billsLoading.value = true
  try {
    const currentMonth = dayjs().format('YYYY-MM')
    const data = await getBillPage({
      month: currentMonth,
      page: 1,
      pageSize: 5
    })
    recentBills.value = data.records || []
  } catch (error) {
    console.error('获取账单失败:', error)
    ElMessage.error('获取账单失败')
  } finally {
    billsLoading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const expenseData = await getCategoryList(1)
    const incomeData = await getCategoryList(2)
    categories.value = {
      EXPENSE: expenseData,
      INCOME: incomeData
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 初始化迷你图表
const initMiniChart = () => {
  if (!miniChartRef.value || !monthStats.value.dailyAvgExpense) return

  if (!miniChart) {
    miniChart = echarts.init(miniChartRef.value)
  }

  const avg = monthStats.value.dailyAvgExpense
  const mockData = [0.8, 1.0, 0.9, 1.2, 1.1, 0.7, 0.95].map(m => Math.round(avg * m))

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: 0, right: 0, top: 5, bottom: 0, containLabel: false },
    xAxis: { type: 'category', show: false },
    yAxis: { type: 'value', show: false },
    series: [{
      data: mockData,
      type: 'bar',
      itemStyle: { color: '#6C5CE7' },
      show: false
    }]
  }

  miniChart.setOption(option)
}

// 记账功能
const startSmartRecord = () => {
  showRecordMenu.value = false
  showSmartRecord.value = true
}

const startPhotoRecord = () => {
  showRecordMenu.value = false
  showPhotoRecord.value = true
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

const startManualRecord = () => {
  showRecordMenu.value = false
  showManualRecord.value = true
}

const submitSmartRecord = async () => {
  if (!smartRecordText.value.trim()) {
    ElMessage.warning('请输入消费描述')
    return
  }

  recordLoading.value = true
  try {
    const result = await textOCR(smartRecordText.value)
    Object.assign(recordForm, result)
    showSmartRecord.value = false
    showRecordResult.value = true
  } catch (error) {
    console.error('识别失败:', error)
    ElMessage.error('识别失败')
  } finally {
    recordLoading.value = false
  }
}

const handlePhotoChange = (file) => {
  selectedPhoto.value = file.raw
}

const submitPhotoRecord = async () => {
  if (!selectedPhoto.value) {
    ElMessage.warning('请选择图片')
    return
  }

  recordLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedPhoto.value)
    const result = await imageOCR(formData)
    Object.assign(recordForm, result)
    showPhotoRecord.value = false
    showRecordResult.value = true
  } catch (error) {
    console.error('识别失败:', error)
    ElMessage.error('识别失败')
  } finally {
    recordLoading.value = false
  }
}

const submitRecord = async () => {
  recordLoading.value = true
  try {
    await addBill({
      categoryId: recordForm.categoryId,
      type: recordForm.type,
      amount: recordForm.amount,
      billDate: recordForm.billDate,
      remark: recordForm.remark
    })
    ElMessage.success('记账成功')
    showSmartRecord.value = false
    showPhotoRecord.value = false
    showManualRecord.value = false
    showRecordResult.value = false
    fetchRecentBills()
    fetchMonthStats()
    fetchTodayStats()
  } catch (error) {
    console.error('记账失败:', error)
    ElMessage.error('记账失败')
  } finally {
    recordLoading.value = false
  }
}

const updateCategories = () => {
  recordForm.categoryId = ''
}

const showAppOnlyTip = () => {
  ElMessage.info('此功能仅App端支持')
}

const showMoreMenu = () => {
  ElMessage.info('更多功能开发中')
}

const showProfile = () => {
  ElMessage.info('个人中心开发中')
}

// 导航函数
const goToReport = () => {
  router.push('/report')
}

const goToChart = () => {
  router.push('/chart')
}

onMounted(() => {
  fetchMonthStats()
  fetchTodayStats()
  fetchRecentBills()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 100%;
  margin: 0;
  padding: 0 16px;
  font-family: var(--font-family);
  color: var(--text-primary);
  background: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 80px; // 为底部导航栏留出空间
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
      background: linear-gradient(135deg, #b391cb 0%, #D2C4FE 100%);
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

// 余额卡片
.balance-card {
  background: linear-gradient(135deg, #E6C4FE 0%, #A8E6FF 50%, #C4D3FE 100%);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  color: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(230, 196, 254, 0.3);
  
  .balance-content {
    position: relative;
    z-index: 2;
    
    .balance-label {
      font-size: 0.9rem;
      opacity: 0.9;
      margin-bottom: 8px;
    }
    
    .balance-amount {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 8px;
    }
    
    .balance-expiry {
      font-size: 0.85rem;
      opacity: 0.8;
    }
  }
  
  .balance-decoration {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    
    .glow-circle {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      position: absolute;
      top: -40px;
      right: -20px;
      animation: glow 3s ease-in-out infinite;
    }
    
    .add-button {
      width: 40px;
      height: 40px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
      }
    }
  }
}

@keyframes glow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

// 快捷操作按钮
.action-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  .action-button {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    background: white;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
    
    .action-icon {
      font-size: 1.5rem;
    }
    
    .action-label {
      font-size: 0.75rem;
      color: #6c757d;
      font-weight: 500;
    }
  }
}

// 最近交易
.recent-transactions {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 8px rgb(26, 125, 164);
  
  .transactions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      font-size: 1.1rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
    
    .see-all {
      color: #C4D3FE;
      cursor: pointer;
      font-size: 0.9rem;
      font-weight: 500;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
  
  .transaction-list {
    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #6c757d;
      
      .el-icon {
        font-size: 48px;
        margin-bottom: 16px;
        display: block;
      }
      
      p { margin: 0; }
    }
  }
  
  .transaction-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f1f3f4;
    
    &:last-child {
      border-bottom: none;
    }
    
    .transaction-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .transaction-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: 600;
        font-size: 14px;
      }
      
      .transaction-info {
        .transaction-name {
          font-size: 0.95rem;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 2px;
        }
        
        .transaction-date {
          font-size: 0.85rem;
          color: #6c757d;
        }
      }
    }
    
    .transaction-amount {
      font-size: 1rem;
      font-weight: 700;
      
      &.expense { color: #000000; }
      &.income { color: #27ae60; }
    }
  }
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
  box-shadow: 0 -2px 10px rgba(107, 155, 161, 0.865);
  
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
        background: #E6C4FE;
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

// 记账弹窗样式
.record-menu {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 1001;
  min-width: 200px;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: #f8f9fa;
    }
    
    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .item-icon {
      font-size: 1.2rem;
    }
    
    .item-title {
      font-weight: 600;
      color: #2c3e50;
    }
    
    .item-desc {
      font-size: 0.8rem;
      color: #6c757d;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    padding: 0 12px;
  }
  
  .action-buttons {
    gap: 12px;
    
    .action-button {
      padding: 12px 6px;
      
      .action-icon {
        font-size: 1.2rem;
      }
      
      .action-label {
        font-size: 0.7rem;
      }
    }
  }
}
</style>

