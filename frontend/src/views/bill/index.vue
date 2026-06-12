<template>
  <div class="bill-page">
    <!-- 优雅的页面头部 -->
    <div class="bill-header">
      <div class="header-content">
        <h1 class="header-title">账单管理</h1>
        <p class="header-subtitle">记录每一笔收支，掌握财务状况</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="btn-primary" @click="showAddBill = true">
          <el-icon><Plus /></el-icon>
          添加账单
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-controls">
        <div class="filter-left">
          <el-date-picker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="handleMonthChange"
            class="filter-date-picker"
          />
          
          <el-select
            v-model="billType"
            placeholder="收支类型"
            @change="handleTypeChange"
            class="filter-select"
          >
            <el-option label="全部" value="" />
            <el-option label="支出" value="EXPENSE" />
            <el-option label="收入" value="INCOME" />
          </el-select>
          
          <el-button 
            v-if="selectedBills.length > 0"
            type="danger" 
            class="btn-danger"
            @click="batchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedBills.length }})
          </el-button>
        </div>
        
        <div class="filter-summary">
          <div class="summary-card">
            <div class="summary-item">
              <span class="summary-label">收入</span>
              <span class="summary-value income">¥{{ formatAmount(monthSummary.totalIncome || 0) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">支出</span>
              <span class="summary-value expense">¥{{ formatAmount(monthSummary.totalExpense || 0) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 账单列表 -->
    <div class="card bill-list">
      <div class="bill-content" v-loading="loading">
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
            class="bill-group"
          >
            <div class="group-header">
              <span class="group-date">{{ date }}</span>
              <span class="group-summary">
                收入: <span class="income">¥{{ group.income }}</span>
                支出: <span class="expense">¥{{ group.expense }}</span>
              </span>
            </div>
            
            <div
              v-for="bill in group.bills"
              :key="bill.id"
              class="bill-item"
              :class="{ selected: selectedBills.includes(bill) }"
              @click="editBill(bill)"
            >
              <div class="bill-checkbox">
                <el-checkbox 
                  :model-value="selectedBills.includes(bill)"
                  @change="(checked) => handleSelectionChange(checked ? [...selectedBills, bill] : selectedBills.filter(b => b !== bill))"
                  @click.stop
                />
              </div>
              
              <div class="bill-category">
                <div class="category-icon" :style="{ backgroundColor: getCategoryColor(bill.categoryId) }">
                  {{ bill.categoryName?.charAt(0) }}
                </div>
                <div class="category-info">
                  <div class="category-name">{{ bill.categoryName }}</div>
                  <div class="bill-note" v-if="bill.remark">{{ bill.remark }}</div>
                </div>
              </div>
              
              <div class="bill-amount" :class="{ expense: bill.type === 1, income: bill.type === 2 }">
                {{ bill.type === 1 ? '-' : '+' }}¥{{ formatAmount(bill.amount) }}
              </div>
              
              <div class="bill-actions">
                <el-button type="text" size="small" @click.stop="editBill(bill)">
                  编辑
                </el-button>
                <el-button type="text" size="small" @click.stop="deleteBill(bill.id)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 悬浮添加按钮 -->
    <el-button
      type="primary"
      class="fab-button"
      circle
      size="large"
      @click="showAddDialog = true"
    >
      <el-icon><Plus /></el-icon>
    </el-button>

    <!-- 添加/编辑账单弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingBill ? '编辑账单' : '添加账单'"
      width="500px"
      :before-close="handleCloseDialog"
    >
      <el-form
        ref="billFormRef"
        :model="billForm"
        :rules="billRules"
        label-width="80px"
      >
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="billForm.type">
            <el-radio-button label="EXPENSE">支出</el-radio-button>
            <el-radio-button label="INCOME">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="billForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
            @change="handleCategoryChange"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="billForm.amount"
            :precision="2"
            :step="0.01"
            :min="0.01"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="日期" prop="billDate">
          <el-date-picker
            v-model="billForm.billDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="note">
          <el-input
            v-model="billForm.note"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingBill ? '更新' : '添加' }}
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
import { getBillPage, addBill, updateBill, deleteBill as deleteBillApi } from '@/api/bill'
import { getCategoryList } from '@/api/category'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const editingBill = ref(null)

// 筛选条件
const currentMonth = ref(dayjs().format('YYYY-MM'))
const billType = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 账单数据
const billList = ref([])
const monthSummary = ref({
  totalIncome: 0,
  totalExpense: 0
})

// 分类数据
const categories = ref([])

// 表单数据
const billFormRef = ref()
const billForm = reactive({
  type: 'EXPENSE',
  categoryId: '',
  amount: 0,
  billDate: dayjs().format('YYYY-MM-DD'), // 改为billDate字段
  note: ''
})

const billRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  billDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

// 计算属性
const filteredCategories = computed(() => {
  // 显示所有分类，不再根据type过滤
  return categories.value
})

const groupedBills = computed(() => {
  const groups = {}
  
  billList.value.forEach(bill => {
    const date = dayjs(bill.billDate || bill.date).format('YYYY年MM月DD日')
    if (!groups[date]) {
      groups[date] = {
        income: 0,
        expense: 0,
        bills: []
      }
    }
    
    groups[date].bills.push(bill)
    
    if (bill.type === 2) {
      groups[date].income += bill.amount
    } else {
      groups[date].expense += bill.amount
    }
  })
  
  return groups
})

// 获取分类列表（带缓存）
const fetchCategories = async () => {
  try {
    const data = await getCategoryList()
    categories.value = data || []
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类失败')
  }
}

// 获取账单列表（优化性能）
const fetchBills = async () => {
  loading.value = true
  try {
    const params = {
      month: currentMonth.value,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加类型筛选 - 优化类型转换
    if (billType.value) {
      params.type = billType.value === 'EXPENSE' ? 1 : 2
    }
    
    const data = await getBillPage(params)
    billList.value = data.records || []
    total.value = data.total || 0
    
    // 前端重新计算月度统计以确保正确性
    const calculatedSummary = { totalIncome: 0, totalExpense: 0 }
    data.records?.forEach(bill => {
      if (bill.type === 'INCOME' || bill.type === 2) {
        calculatedSummary.totalIncome += bill.amount
      } else {
        calculatedSummary.totalExpense += bill.amount
      }
    })
    
    monthSummary.value = calculatedSummary
  } catch (error) {
    console.error('获取账单失败:', error)
    ElMessage.error('获取账单失败')
  } finally {
    loading.value = false
  }
}

// 批量数据获取（性能优化）
const fetchAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchCategories(),
      fetchBills()
    ])
  } catch (error) {
    console.error('数据获取失败:', error)
  } finally {
    loading.value = false
  }
}

// 月份变化（防抖优化）
const handleMonthChange = () => {
  currentPage.value = 1
  fetchBills()
}

// 类型变化（防抖优化）
const handleTypeChange = () => {
  currentPage.value = 1
  fetchBills()
}

// 格式化金额显示
const formatAmount = (amount) => {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

// 获取分类颜色（优化）
const getCategoryColor = (categoryId) => {
  const colors = ['#D2C4FE', '#B68FC4', '#C4D3FE', '#A8E6FF', '#E6C4FE']
  return colors[categoryId % colors.length]
}

// 批量删除功能
const selectedBills = ref([])
const handleSelectionChange = (selection) => {
  selectedBills.value = selection
}

const batchDelete = async () => {
  if (selectedBills.value.length === 0) {
    ElMessage.warning('请选择要删除的账单')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedBills.value.length} 条账单吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除逻辑
    for (const bill of selectedBills.value) {
      await deleteBill(bill.id)
    }
    
    ElMessage.success('批量删除成功')
    selectedBills.value = []
    fetchBills()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 分页变化
const handlePageChange = () => {
  fetchBills()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchBills()
}

// 处理分类选择变化
const handleCategoryChange = (categoryId) => {
  if (!categoryId) return
  
  // 根据选择的分类自动设置type
  const selectedCategory = categories.value.find(cat => cat.id === categoryId)
  if (selectedCategory) {
    // 后端返回的分类type是Integer类型：1=支出，2=收入
    // 转换为前端使用的字符串类型
    billForm.type = selectedCategory.type === 1 ? 'EXPENSE' : 'INCOME'
  }
}

// 编辑账单
const editBill = (bill) => {
  editingBill.value = bill
  Object.assign(billForm, {
    type: bill.type,
    categoryId: bill.categoryId,
    amount: bill.amount,
    billDate: bill.billDate || bill.date, // 支持两种字段名
    note: bill.remark || bill.note || ''
  })
  showAddDialog.value = true
}

// 删除账单
const deleteBill = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这笔账单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteBillApi(id)
    ElMessage.success('删除成功')
    fetchBills()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!billFormRef.value) return
  
  try {
    await billFormRef.value.validate()
    submitting.value = true
    
    // 确保type字段有值，并转换为后端需要的Integer类型
    const submitData = {
      ...billForm,
      type: billForm.type === 'EXPENSE' ? 1 : 2,
      remark: billForm.note // 将note字段映射为remark字段
    }
    
    if (editingBill.value) {
      await updateBill({
        ...submitData,
        id: editingBill.value.id
      })
      ElMessage.success('更新成功')
    } else {
      await addBill(submitData)
      ElMessage.success('添加成功')
    }
    
    handleCloseDialog()
    fetchBills()
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
  editingBill.value = null
  
  // 重置表单
  Object.assign(billForm, {
    type: 'EXPENSE',
    categoryId: '',
    amount: 0,
    billDate: dayjs().format('YYYY-MM-DD'), // 改为billDate字段
    note: ''
  })
  
  if (billFormRef.value) {
    billFormRef.value.resetFields()
  }
}

// 页面加载
onMounted(() => {
  fetchAllData()
})
</script>

<style lang="scss" scoped>
.bill-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  font-family: var(--font-family);
  color: var(--text-primary);
}

.bill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
  flex-wrap: wrap;
  gap: 20px;

  .header-content {
    .header-title {
      font-size: 2rem;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
      letter-spacing: -0.5px;
    }

    .header-subtitle {
      font-size: 1rem;
      color: var(--text-secondary);
      font-weight: var(--font-weight-normal);
    }
  }

  .header-actions {
    .btn-primary {
      padding: 12px 24px;
      font-size: 1rem;
      font-weight: var(--font-weight-medium);
    }
  }
}

.filter-section {
  margin-bottom: 32px;
}

.filter-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255,255,255,0.7);
  box-shadow: var(--shadow-md);
}

.filter-left {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-date-picker,
.filter-select,
.btn-danger {
  border-radius: var(--radius-lg);
}

.filter-date-picker,
.filter-select {
  border: 1px solid rgba(226,232,240,0.7);
  background: rgba(255,255,255,0.9);
}

.btn-danger {
  padding: 10px 18px;
  font-size: 0.95rem;
  font-weight: var(--font-weight-medium);
  color: #fff;
  background: linear-gradient(135deg, #EF4444 0%, #FB7185 100%);
  border: none;
}

.filter-summary {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.summary-card {
  display: flex;
  gap: 24px;
  padding: 14px 20px;
  background: rgba(246,247,251,0.95);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(226,232,240,0.75);
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  .summary-label {
    font-size: 0.85rem;
    color: var(--text-secondary);
    margin-bottom: 6px;
  }

  .summary-value {
    font-size: 1rem;
    font-weight: var(--font-weight-semibold);

    &.income { color: #2c3e50; }
    &.expense { color: #2c3e50; }
  }
}

.bill-content {
  min-height: 400px;
  padding: 24px 0;
}

.bill-group {
  margin-bottom: 24px;

  .group-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: rgba(235, 248, 255, 0.9);
    border-radius: var(--radius-lg);
    margin-bottom: 12px;

    .group-date {
      font-weight: 600;
      color: var(--text-primary);
    }

    .group-summary {
      font-size: 0.92rem;
      color: var(--text-secondary);

      .income { color: #2c3e50; }
      .expense { color: #2c3e50; }
    }
  }
}

.bill-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--radius-xl);
  margin-bottom: 10px;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;

  &:hover {
    background: rgba(124,92,255,0.06);
    transform: translateX(1px);

    .bill-actions { opacity: 1; }
    .bill-checkbox { opacity: 1; }
  }

  &.selected {
    background: rgba(124,92,255,0.1);
    border-left: 3px solid var(--primary-color);
  }

  .bill-checkbox {
    margin-right: 12px;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  .bill-category {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;

    .category-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ffffff;
      font-weight: 600;
      font-size: 14px;
      background: linear-gradient(135deg, rgba(124,92,255,0.22), rgba(94,207,255,0.22));
    }

    .category-info {
      .category-name {
        font-size: 0.95rem;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .bill-note {
        font-size: 0.9rem;
        color: var(--text-secondary);
      }
    }
  }

  .bill-amount {
    font-size: 1rem;
    font-weight: 700;
    margin-right: 16px;

    &.expense { color: #2c3e50; }
    &.income { color: #2c3e50; }
  }

  .bill-actions {
    opacity: 0;
    transition: opacity 0.2s ease;

    .el-button {
      padding: 4px 8px;
      font-size: 12px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 22px 0;
  border-top: 1px solid rgba(226,232,240,0.7);
  margin-top: 20px;
}

.fab-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  font-size: 20px;
  box-shadow: var(--shadow-lg);
  z-index: 100;

  &:hover { transform: scale(1.05); }
}

@media (max-width: 768px) {
  .bill-page { padding: 0 16px; }

  .bill-header {
    justify-content: center;
    text-align: center;

    .header-actions { width: 100%; display: flex; justify-content: center; }
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left { justify-content: center; }

  .bill-item {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;

    .bill-actions { opacity: 1; }
  }

  .fab-button {
    bottom: 20px;
    right: 20px;
  }
}
</style>

