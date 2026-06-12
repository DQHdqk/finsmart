<template>
  <div class="mobile-bill">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="user-info">
        <el-avatar :size="48" icon="User" class="user-avatar" />
        <div class="welcome-text">
          <h2>账单管理</h2>
        </div>
      </div>
      <div class="notification-icon">
        <el-icon :size="24"><Bell /></el-icon>
      </div>
    </div>

    <!-- 月份统计卡片 -->
    <div class="stats-card">
      <div class="stats-content">
        <div class="stats-label">本月统计</div>
        <div class="stats-amounts">
          <div class="stat-item">
            <span class="stat-label">收入</span>
            <span class="stat-value income">¥{{ formatAmount(monthSummary.totalIncome || 0) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">支出</span>
            <span class="stat-value expense">¥{{ formatAmount(monthSummary.totalExpense || 0) }}</span>
          </div>
        </div>
      </div>
      <div class="stats-decoration">
        <div class="glow-circle"></div>
      </div>
    </div>

    <!-- 筛选控制 -->
    <div class="filter-controls">
      <el-date-picker
        v-model="currentMonth"
        type="month"
        placeholder="选择月份"
        format="YYYY年MM月"
        value-format="YYYY-MM"
        @change="handleMonthChange"
        class="month-picker"
      />
      
      <el-select
        v-model="billType"
        placeholder="类型"
        @change="handleTypeChange"
        class="type-select"
      >
        <el-option label="全部" value="" />
        <el-option label="支出" value="EXPENSE" />
        <el-option label="收入" value="INCOME" />
      </el-select>
    </div>

    <!-- 账单列表 -->
    <div class="bill-list">
      <div class="list-header">
        <h3>账单明细</h3>
        <el-button type="primary" size="small" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加
        </el-button>
      </div>
      
      <div class="transaction-list" v-loading="loading">
        <div v-if="billList.length === 0" class="empty-state">
          <el-icon><Document /></el-icon>
          <p>暂无账单记录</p>
          <el-button type="primary" @click="showAddDialog = true">
            添加第一笔账单
          </el-button>
        </div>
        
        <div v-else>
          <div
            v-for="(group, date) in groupedBills"
            :key="date"
            class="date-group"
          >
            <div class="date-header">
              <span class="date-text">{{ date }}</span>
              <span class="date-summary">
                收入: <span class="income">¥{{ group.income }}</span>
                支出: <span class="expense">¥{{ group.expense }}</span>
              </span>
            </div>
            
            <div
              v-for="bill in group.bills"
              :key="bill.id"
              class="transaction-item"
              @click="editBill(bill)"
            >
              <div class="transaction-left">
                <div class="transaction-icon" :style="{ backgroundColor: getCategoryColor(bill.categoryId) }">
                  {{ bill.categoryName?.charAt(0) || '分' }}
                </div>
                <div class="transaction-info">
                  <div class="transaction-name">{{ bill.categoryName }}</div>
                  <div class="transaction-date">{{ formatDate(bill.billDate) }}</div>
                </div>
              </div>
              <div class="transaction-amount" :class="bill.type === 'EXPENSE' ? 'expense' : 'income'">
                {{ bill.type === 'EXPENSE' ? '-' : '+' }}¥{{ bill.amount }}
              </div>
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
      <div class="nav-item" :class="{ active: $route.path === '/bill' }" @click="router.push('/bill')">
        <el-icon :size="20"><Document /></el-icon>
        <span>账单</span>
      </div>
      <div class="nav-item add-item" @click="showAddDialog = true">
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

    <!-- 添加账单弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加账单" width="90%" max-width="500px">
      <el-form :model="billForm" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="billForm.type">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="billForm.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in currentCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="billForm.amount" :precision="2" :min="0.01" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="billForm.billDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="billForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitBill">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getBillPage, addBill, updateBill, deleteBill } from '@/api/bill'
import { getCategoryList } from '@/api/category'

const router = useRouter()

// 数据状态
const billList = ref([])
const categories = ref({ EXPENSE: [], INCOME: [] })
const loading = ref(false)
const submitLoading = ref(false)

// 筛选状态
const currentMonth = ref(dayjs().format('YYYY-MM'))
const billType = ref('')

// 表单状态
const showAddDialog = ref(false)
const billForm = reactive({
  type: 'EXPENSE',
  categoryId: '',
  amount: 0,
  billDate: dayjs().format('YYYY-MM-DD'),
  remark: ''
})

// 计算属性
const monthSummary = computed(() => {
  const summary = { totalIncome: 0, totalExpense: 0 }
  billList.value.forEach(bill => {
    if (bill.type === 'INCOME' || bill.type === 2) {
      summary.totalIncome += bill.amount
    } else {
      summary.totalExpense += bill.amount
    }
  })
  return summary
})

const currentCategories = computed(() => {
  return categories.value[billForm.type] || []
})

const groupedBills = computed(() => {
  const groups = {}
  const filteredBills = billList.value.filter(bill => 
    !billType.value || bill.type === billType.value
  )
  
  filteredBills.forEach(bill => {
    const date = dayjs(bill.billDate).format('YYYY-MM-DD')
    if (!groups[date]) {
      groups[date] = {
        income: 0,
        expense: 0,
        bills: []
      }
    }
    
    if (bill.type === 'INCOME' || bill.type === 2) {
      groups[date].income += bill.amount
    } else {
      groups[date].expense += bill.amount
    }
    
    groups[date].bills.push(bill)
  })
  
  return groups
})

// 方法
const fetchBills = async () => {
  loading.value = true
  try {
    const data = await getBillPage({
      month: currentMonth.value,
      page: 1,
      pageSize: 1000
    })
    billList.value = data.records || []
  } catch (error) {
    console.error('获取账单失败:', error)
    ElMessage.error('获取账单失败')
  } finally {
    loading.value = false
  }
}

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

const handleMonthChange = () => {
  fetchBills()
}

const handleTypeChange = () => {
  // 类型改变时重新筛选显示
}

const getCategoryColor = (categoryId) => {
  const colors = ['#D2C4FE', '#B68FC4', '#C4D3FE', '#A8E6FF', '#E6C4FE']
  return colors[categoryId % colors.length]
}

const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

const formatAmount = (amount) => {
  return amount.toFixed(2)
}

const editBill = (bill) => {
  Object.assign(billForm, bill)
  showAddDialog.value = true
}

const submitBill = async () => {
  if (!billForm.categoryId || !billForm.amount) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitLoading.value = true
  try {
    if (billForm.id) {
      await updateBill(billForm)
      ElMessage.success('更新成功')
    } else {
      await addBill(billForm)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    fetchBills()
  } catch (error) {
    console.error('保存账单失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(billForm, {
    id: '',
    type: 'EXPENSE',
    categoryId: '',
    amount: 0,
    billDate: dayjs().format('YYYY-MM-DD'),
    remark: ''
  })
}

const showProfile = () => {
  ElMessage.info('个人中心开发中')
}

onMounted(() => {
  fetchBills()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.mobile-bill {
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

// 统计卡片
.stats-card {
  background: linear-gradient(135deg, #E6C4FE 0%, #A8E6FF 50%, #C4D3FE 100%);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  color: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(230, 196, 254, 0.3);
  
  .stats-content {
    position: relative;
    z-index: 2;
    
    .stats-label {
      font-size: 0.9rem;
      opacity: 0.9;
      margin-bottom: 12px;
    }
    
    .stats-amounts {
      display: flex;
      gap: 24px;
      
      .stat-item {
        display: flex;
        flex-direction: column;
        
        .stat-label {
          font-size: 0.85rem;
          opacity: 0.8;
          margin-bottom: 4px;
        }
        
        .stat-value {
          font-size: 1.25rem;
          font-weight: 700;
          
          &.income { color: #003212; }
          &.expense { color: #000000; }
        }
      }
    }
  }
  
  .stats-decoration {
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
  }
}

@keyframes glow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

// 筛选控制
.filter-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  
  .month-picker,
  .type-select {
    flex: 1;
  }
}

// 账单列表
.bill-list {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .list-header {
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
      
      p { margin: 0 0 16px 0; }
    }
  }
  
  .date-group {
    margin-bottom: 24px;
    
    .date-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #e9ecef;
      margin-bottom: 12px;
      
      .date-text {
        font-weight: 600;
        color: #2c3e50;
      }
      
      .date-summary {
        font-size: 0.85rem;
        color: #6c757d;
        
        .income { color: #2c3e50; }
        .expense { color: #2c3e50; }
      }
    }
  }
  
  .transaction-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f1f3f4;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: rgba(102, 126, 234, 0.05);
      border-radius: 8px;
      padding-left: 8px;
      padding-right: 8px;
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
      
      &.expense { color: #2c3e50; }
      &.income { color: #2c3e50; }
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
  .mobile-bill {
    padding: 0 12px;
  }
  
  .filter-controls {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
