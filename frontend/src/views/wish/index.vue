<template>
  <div class="wish-page">
    <div class="page-header">
      <h1>愿望清单</h1>
      <p class="page-description">设定目标，逐步实现你的愿望</p>
    </div>

    <!-- 愿望统计 -->
    <div class="stats-summary">
      <div class="stats-item">
        <div class="stats-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stats-content">
          <div class="stats-value">{{ wishes.length }}</div>
          <div class="stats-label">总愿望数</div>
        </div>
      </div>
      
      <div class="stats-item">
        <div class="stats-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stats-content">
          <div class="stats-value">{{ completedWishes }}</div>
          <div class="stats-label">已完成</div>
        </div>
      </div>
      
      <div class="stats-item">
        <div class="stats-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stats-content">
          <div class="stats-value">¥{{ totalSaved }}</div>
          <div class="stats-label">总存入金额</div>
        </div>
      </div>
    </div>

    <!-- 愿望列表 -->
    <div class="card wish-list">
      <div class="list-header">
        <h3>我的愿望</h3>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加愿望
        </el-button>
      </div>
      
      <div class="wish-content" v-loading="loading">
        <div v-if="wishes.length === 0" class="empty-state">
          <el-icon><Star /></el-icon>
          <p>还没有设定愿望</p>
          <el-button type="primary" @click="showAddDialog = true">
            设定第一个愿望
          </el-button>
        </div>
        
        <div v-else class="wish-grid">
          <div
            v-for="wish in wishes"
            :key="wish.id"
            class="wish-card"
            :class="{ 'completed': isCompleted(wish) }"
          >
            <div class="card-header">
              <div class="wish-info">
                <h4>{{ wish.name }}</h4>
                <div class="wish-target">目标: ¥{{ wish.targetAmount }}</div>
              </div>
              <div class="card-actions">
                <el-button type="text" size="small" @click="saveMoney(wish)">
                  <el-icon><Money /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click="editWish(wish)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click="deleteWish(wish.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="card-content">
              <div class="progress-section">
                <div class="progress-info">
                  <span class="saved-amount">已存: ¥{{ wish.savedAmount }}</span>
                  <span class="progress-percentage">{{ getProgressPercentage(wish) }}%</span>
                </div>
                <div class="progress-bar">
                  <div
                    class="progress-fill"
                    :class="getProgressClass(wish)"
                    :style="{ width: getProgressPercentage(wish) + '%' }"
                  ></div>
                </div>
              </div>
              
              <div class="wish-details">
                <div class="target-date">
                  <el-icon><Calendar /></el-icon>
                  <span>目标日期: {{ formatDate(wish.targetDate) }}</span>
                </div>
                <div class="remaining" v-if="!isCompleted(wish)">
                  还需: ¥{{ getRemainingAmount(wish) }}
                </div>
                <div class="completed-badge" v-else>
                  <el-icon><Select /></el-icon>
                  已完成
                </div>
              </div>
              
              <div class="wish-description" v-if="wish.description">
                {{ wish.description }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑愿望弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingWish ? '编辑愿望' : '添加愿望'"
      width="500px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="wishFormRef"
        :model="wishForm"
        :rules="wishRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="wishForm.name"
            placeholder="请输入愿望名称"
          />
        </el-form-item>
        
        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number
            v-model="wishForm.targetAmount"
            :precision="2"
            :step="0.01"
            :min="0.01"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="目标日期" prop="targetDate">
          <el-date-picker
            v-model="wishForm.targetDate"
            type="date"
            placeholder="选择目标日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="wishForm.description"
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
            {{ editingWish ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 存入金额弹窗 -->
    <el-dialog
      v-model="showSaveDialog"
      title="存入金额"
      width="400px"
      :before-close="handleCloseSaveDialog"
    >
      <div class="save-form">
        <div class="wish-summary">
          <h4>{{ currentWish?.name }}</h4>
          <div class="progress-summary">
            <span>当前: ¥{{ currentWish?.savedAmount || 0 }}</span>
            <span>目标: ¥{{ currentWish?.targetAmount }}</span>
          </div>
        </div>
        
        <el-form
          ref="saveFormRef"
          :model="saveForm"
          :rules="saveRules"
          label-width="80px"
        >
          <el-form-item label="存入金额" prop="amount">
            <el-input-number
              v-model="saveForm.amount"
              :precision="2"
              :step="0.01"
              :min="0.01"
              :max="getMaxSaveAmount()"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseSaveDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            确认存入
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
import { getWishList, addWish, deleteWish as deleteWishApi, saveToWish } from '@/api/wish'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showSaveDialog = ref(false)
const editingWish = ref(null)
const currentWish = ref(null)

// 愿望数据
const wishes = ref([])

// 表单数据
const wishFormRef = ref()
const wishForm = reactive({
  name: '',
  targetAmount: 0,
  targetDate: '',
  description: ''
})

const wishRules = {
  name: [{ required: true, message: '请输入愿望名称', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  targetDate: [{ required: true, message: '请选择目标日期', trigger: 'change' }]
}

// 存入金额表单
const saveFormRef = ref()
const saveForm = reactive({
  amount: 0
})

const saveRules = {
  amount: [{ required: true, message: '请输入存入金额', trigger: 'blur' }]
}

// 计算属性
const completedWishes = computed(() => {
  return wishes.value.filter(wish => isCompleted(wish)).length
})

const totalSaved = computed(() => {
  return wishes.value.reduce((total, wish) => total + (wish.savedAmount || 0), 0)
})

// 工具函数
const isCompleted = (wish) => {
  return (wish.savedAmount || 0) >= wish.targetAmount
}

const getProgressPercentage = (wish) => {
  if (!wish.targetAmount) return 0
  const percentage = ((wish.savedAmount || 0) / wish.targetAmount) * 100
  return Math.min(Math.round(percentage), 100)
}

const getProgressClass = (wish) => {
  const percentage = getProgressPercentage(wish)
  if (percentage <= 30) return 'low'
  if (percentage <= 70) return 'medium'
  return 'high'
}

const getRemainingAmount = (wish) => {
  const remaining = wish.targetAmount - (wish.savedAmount || 0)
  return Math.max(remaining, 0)
}

const getMaxSaveAmount = () => {
  if (!currentWish.value) return 0
  return getRemainingAmount(currentWish.value)
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

// 获取愿望列表
const fetchWishes = async () => {
  loading.value = true
  try {
    const data = await getWishList()
    wishes.value = data || []
  } catch (error) {
    console.error('获取愿望失败:', error)
    ElMessage.error('获取愿望失败')
  } finally {
    loading.value = false
  }
}

// 编辑愿望
const editWish = (wish) => {
  editingWish.value = wish
  Object.assign(wishForm, {
    name: wish.name,
    targetAmount: wish.targetAmount,
    targetDate: wish.targetDate,
    description: wish.description || ''
  })
  showAddDialog.value = true
}

// 删除愿望
const deleteWish = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个愿望吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteWishApi(id)
    ElMessage.success('删除成功')
    fetchWishes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 存入金额
const saveMoney = (wish) => {
  currentWish.value = wish
  saveForm.amount = 0
  showSaveDialog.value = true
}

// 提交愿望表单
const handleSubmit = async () => {
  if (!wishFormRef.value) return
  
  try {
    await wishFormRef.value.validate()
    submitting.value = true
    
    if (editingWish.value) {
      // 这里应该有更新愿望的API，暂时用添加
      ElMessage.info('编辑功能待实现')
    } else {
      await addWish(wishForm)
      ElMessage.success('添加成功')
    }
    
    handleCloseDialog()
    fetchWishes()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 处理存入金额
const handleSave = async () => {
  if (!saveFormRef.value || !currentWish.value) return
  
  try {
    await saveFormRef.value.validate()
    saving.value = true
    
    await saveToWish(currentWish.value.id, saveForm.amount)
    ElMessage.success('存入成功')
    
    handleCloseSaveDialog()
    fetchWishes()
  } catch (error) {
    console.error('存入失败:', error)
    ElMessage.error('存入失败')
  } finally {
    saving.value = false
  }
}

// 关闭愿望弹窗
const handleCloseDialog = () => {
  showAddDialog.value = false
  editingWish.value = null
  
  // 重置表单
  Object.assign(wishForm, {
    name: '',
    targetAmount: 0,
    targetDate: '',
    description: ''
  })
  
  if (wishFormRef.value) {
    wishFormRef.value.resetFields()
  }
}

// 关闭存入弹窗
const handleCloseSaveDialog = () => {
  showSaveDialog.value = false
  currentWish.value = null
  saveForm.amount = 0
  
  if (saveFormRef.value) {
    saveFormRef.value.resetFields()
  }
}

// 页面加载
onMounted(() => {
  fetchWishes()
})
</script>

<style lang="scss" scoped>
.wish-page {
  max-width: 900px;
  margin: 0 auto;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
  
  .stats-item {
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

.wish-list {
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

.wish-content {
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

.wish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.wish-card {
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
  
  &.completed {
    border-color: var(--success-color);
    background: rgba(0, 214, 143, 0.05);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    
    .wish-info {
      flex: 1;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }
      
      .wish-target {
        font-size: 14px;
        color: var(--primary-color);
        font-weight: 500;
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
    .progress-section {
      margin-bottom: 16px;
      
      .progress-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .saved-amount {
          font-size: 14px;
          color: var(--text-primary);
          font-weight: 500;
        }
        
        .progress-percentage {
          font-size: 14px;
          font-weight: 600;
          color: var(--primary-color);
        }
      }
      
      .progress-bar {
        width: 100%;
        height: 8px;
        background: var(--border-color);
        border-radius: 4px;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          border-radius: 4px;
          transition: width 0.3s ease;
          
          &.low {
            background: var(--danger-color);
          }
          
          &.medium {
            background: var(--warning-color);
          }
          
          &.high {
            background: var(--success-color);
          }
        }
      }
    }
    
    .wish-details {
      margin-bottom: 12px;
      
      .target-date {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 4px;
        
        .el-icon {
          font-size: 14px;
        }
      }
      
      .remaining {
        font-size: 13px;
        color: var(--text-secondary);
      }
      
      .completed-badge {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: var(--success-color);
        font-weight: 500;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
    
    .wish-description {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.4;
      padding-top: 8px;
      border-top: 1px solid var(--border-color);
    }
  }
}

.save-form {
  .wish-summary {
    background: var(--bg-gray);
    padding: 16px;
    border-radius: var(--radius-sm);
    margin-bottom: 20px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    
    .progress-summary {
      display: flex;
      gap: 16px;
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

@media (max-width: 768px) {
  .stats-summary {
    grid-template-columns: 1fr;
  }
  
  .wish-grid {
    grid-template-columns: 1fr;
  }
  
  .wish-card {
    .card-actions {
      opacity: 1;
    }
  }
}
</style>
