<template>
  <div class="subscription-page">
    <div class="page-header">
      <h1>订阅管家</h1>
      <p class="page-description">管理您的订阅服务，避免忘记续费</p>
    </div>

    <!-- 本月订阅统计 -->
    <div class="card stats-summary">
      <div class="stats-item">
        <div class="stats-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stats-content">
          <div class="stats-value">¥{{ monthTotalExpense }}</div>
          <div class="stats-label">本月订阅总支出</div>
        </div>
      </div>
      
      <div class="stats-item">
        <div class="stats-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stats-content">
          <div class="stats-value">{{ upcomingCount }}</div>
          <div class="stats-label">7天内到期</div>
        </div>
      </div>
    </div>

    <!-- 订阅列表 -->
    <div class="card subscription-list">
      <div class="list-header">
        <h3>订阅列表</h3>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加订阅
        </el-button>
      </div>
      
      <div class="subscription-content" v-loading="loading">
        <div v-if="subscriptions.length === 0" class="empty-state">
          <el-icon><Bell /></el-icon>
          <p>暂无订阅服务</p>
          <el-button type="primary" @click="showAddDialog = true">
            添加第一个订阅
          </el-button>
        </div>
        
        <div v-else class="subscription-grid">
          <div
            v-for="subscription in subscriptions"
            :key="subscription.id"
            class="subscription-card"
            :class="{ 'upcoming': isUpcoming(subscription.nextDate) }"
          >
            <div class="card-header">
              <div class="subscription-info">
                <h4>{{ subscription.name }}</h4>
                <div class="subscription-meta">
                  <span class="cycle">{{ getCycleText(subscription.cycle) }}</span>
                  <span class="amount">¥{{ subscription.amount }}</span>
                </div>
              </div>
              <div class="card-actions">
                <el-button type="text" size="small" @click="editSubscription(subscription)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click="deleteSubscription(subscription.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="card-content">
              <div class="next-billing">
                <div class="billing-label">下次扣费</div>
                <div class="billing-date" :class="{ 'soon': isUpcoming(subscription.nextBillingDate) }">
                  {{ formatDate(subscription.nextDate) }}
                </div>
                <div class="days-left" :class="{ 'warning': isUpcoming(subscription.nextBillingDate) }">
                  {{ getDaysLeft(subscription.nextDate) }}天后
                </div>
              </div>
              
              <div class="subscription-description" v-if="subscription.description">
                {{ subscription.description }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑订阅弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingSubscription ? '编辑订阅' : '添加订阅'"
      width="500px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="subscriptionFormRef"
        :model="subscriptionForm"
        :rules="subscriptionRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="subscriptionForm.name"
            placeholder="请输入订阅服务名称"
          />
        </el-form-item>
        
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="subscriptionForm.amount"
            :precision="2"
            :step="0.01"
            :min="0.01"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="周期" prop="cycle">
          <el-select
            v-model="subscriptionForm.cycle"
            placeholder="请选择扣费周期"
            style="width: 100%"
          >
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每季度" value="QUARTERLY" />
            <el-option label="每半年" value="SEMI_ANNUALLY" />
            <el-option label="每年" value="ANNUALLY" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="subscriptionForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="subscriptionForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingSubscription ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getSubscriptionList, addSubscription, updateSubscription, deleteSubscription as deleteSubscriptionApi } from '@/api/subscription'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const editingSubscription = ref(null)

// 订阅数据
const subscriptions = ref([])

// 表单数据
const subscriptionFormRef = ref()
const subscriptionForm = reactive({
  name: '',
  amount: 0,
  cycle: 'MONTHLY',
  startDate: dayjs().format('YYYY-MM-DD'),
  description: ''
})

const subscriptionRules = {
  name: [{ required: true, message: '请输入订阅名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  cycle: [{ required: true, message: '请选择周期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

// 计算属性
const monthTotalExpense = computed(() => {
  const currentMonth = dayjs().format('YYYY-MM')
  return subscriptions.value
    .filter(sub => {
      const billingMonth = dayjs(sub.nextDate).format('YYYY-MM')  // 改这里
      return billingMonth === currentMonth
    })
    .reduce((total, sub) => total + sub.amount, 0)
    .toFixed(2)  // 顺手加个保留两位小数
})

const upcomingCount = computed(() => {
  return subscriptions.value.filter(sub => isUpcoming(sub.nextDate)).length  // 改这里
})

// 工具函数
const isUpcoming = (date) => {
  const days = dayjs(date).diff(dayjs().startOf('day'), 'day')
  return days >= 0 && days <= 7
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

const getDaysLeft = (date) => {
  // startOf('day') 去掉时分秒，避免因为时间差导致天数偏差1天
  return dayjs(date).diff(dayjs().startOf('day'), 'day')
}

const getCycleText = (cycle) => {
  const cycleMap = {
    'MONTHLY': '每月',
    'QUARTERLY': '每季度',
    'SEMI_ANNUALLY': '每半年',
    'ANNUALLY': '每年'
  }
  return cycleMap[cycle] || cycle
}

// 获取订阅列表
const fetchSubscriptions = async () => {
  loading.value = true
  try {
    const data = await getSubscriptionList()
    subscriptions.value = data || []
  } catch (error) {
    console.error('获取订阅失败:', error)
    ElMessage.error('获取订阅失败')
  } finally {
    loading.value = false
  }
}

// 编辑订阅
const editSubscription = (subscription) => {
  editingSubscription.value = subscription
  Object.assign(subscriptionForm, {
    name: subscription.name,
    amount: subscription.amount,
    cycle: subscription.cycle,
    startDate: subscription.startDate,
    description: subscription.description || ''
  })
  showAddDialog.value = true
}

// 删除订阅
const deleteSubscription = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个订阅吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSubscriptionApi(id)
    ElMessage.success('删除成功')
    fetchSubscriptions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!subscriptionFormRef.value) return
  
  try {
    await subscriptionFormRef.value.validate()
    submitting.value = true
    
    if (editingSubscription.value) {
      await updateSubscription({
        ...subscriptionForm,
        id: editingSubscription.value.id
      })
      ElMessage.success('更新成功')
    } else {
      await addSubscription(subscriptionForm)
      ElMessage.success('添加成功')
    }
    
    handleCloseDialog()
    fetchSubscriptions()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleCloseDialog = () => {
  showAddDialog.value = false
  editingSubscription.value = null
  
  // 重置表单
  Object.assign(subscriptionForm, {
    name: '',
    amount: 0,
    cycle: 'MONTHLY',
    startDate: dayjs().format('YYYY-MM-DD'),
    description: ''
  })
  
  if (subscriptionFormRef.value) {
    subscriptionFormRef.value.resetFields()
  }
}

// 页面加载
onMounted(() => {
  fetchSubscriptions()
})
</script>

<style lang="scss" scoped>
.subscription-page {
  max-width: 900px;
  margin: 0 auto;
}

.stats-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  
  .stats-item {
    flex: 1;
    background: var(--bg-white);
    border-radius: var(--radius-lg);
    padding: 24px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }
    
    .stats-icon {
      width: 56px;
      height: 56px;
      background: rgba(108, 92, 231, 0.1);
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        font-size: 24px;
        color: var(--primary-color);
      }
    }
    
    .stats-content {
      flex: 1;
      
      .stats-value {
        font-size: 28px;
        font-weight: 600;
        color: var(--primary-color);
        margin-bottom: 4px;
      }
      
      .stats-label {
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
  }
}

.subscription-list {
  .list-header {
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
}

.subscription-content {
  min-height: 400px;
  
  .empty-state {
    text-align: center;
    padding: 60px 0;
    color: var(--text-light);
    
    .el-icon {
      font-size: 64px;
      margin-bottom: 16px;
      display: block;
    }
    
    p {
      margin-bottom: 20px;
      font-size: 16px;
    }
  }
}

.subscription-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.subscription-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
  
  &.upcoming {
    border-color: var(--warning-color);
    background: rgba(255, 170, 0, 0.05);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    
    .subscription-info {
      flex: 1;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }
      
      .subscription-meta {
        display: flex;
        gap: 12px;
        align-items: center;
        
        .cycle {
          font-size: 12px;
          color: var(--text-secondary);
          background: var(--bg-gray);
          padding: 2px 8px;
          border-radius: 12px;
        }
        
        .amount {
          font-size: 14px;
          font-weight: 600;
          color: var(--primary-color);
        }
      }
    }
    
    .card-actions {
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s ease;
      
      .el-button {
        padding: 4px;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  &:hover .card-actions {
    opacity: 1;
  }
  
  .card-content {
    .next-billing {
      margin-bottom: 12px;
      
      .billing-label {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }
      
      .billing-date {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        margin-bottom: 2px;
        
        &.soon {
          color: var(--warning-color);
          font-weight: 600;
        }
      }
      
      .days-left {
        font-size: 12px;
        color: var(--text-secondary);
        
        &.warning {
          color: var(--warning-color);
          font-weight: 500;
        }
      }
    }
    
    .subscription-description {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.4;
      padding-top: 8px;
      border-top: 1px solid var(--border-color);
    }
  }
}

@media (max-width: 768px) {
  .stats-summary {
    flex-direction: column;
  }
  
  .subscription-grid {
    grid-template-columns: 1fr;
  }
  
  .subscription-card {
    .card-actions {
      opacity: 1;
    }
  }
}
</style>
