<template>
  <div class="budget-page">
    <div class="page-header">
      <h1>预算管理</h1>
      <p class="page-description">设置和管理您的月度预算</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-date-picker
        v-model="selectedMonth"
        type="month"
        placeholder="选择月份"
        format="YYYY-MM"
        value-format="YYYY-MM"
        @change="fetchBudgetData"
      />
      <el-button type="primary" @click="showAddBudget = true">
        <el-icon><Plus /></el-icon>
        新建预算
      </el-button>
    </div>

    <!-- 预算列表 -->
    <div v-loading="loading" class="budget-content">
      <div v-if="budgetList.length === 0" class="empty-state">
        <el-icon><Document /></el-icon>
        <p>暂无预算数据</p>
      </div>

      <div v-else class="budget-list">
        <div v-for="item in budgetList" :key="item.id" class="budget-card">
          <div class="budget-header">
            <div class="category-info">
              <div class="category-name">{{ item.categoryName }}</div>
              <div class="budget-amount">预算: ¥{{ item.budgetAmount }}</div>
            </div>
            <el-button type="text" @click="deleteBudget(item.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <div class="budget-stats">
            <div class="stat">
              <span>已用: ¥{{ item.usedAmount }}</span>
            </div>
            <div class="stat">
              <span class="warning">剩余: ¥{{ item.remainingAmount }}</span>
            </div>
          </div>

          <el-progress
            :percentage="item.percentage"
            :color="item.percentage > 100 ? '#FF3D71' : (item.percentage > 70 ? '#FFAA00' : '#00D68F')"
          />
        </div>
      </div>
    </div>

    <!-- 新建预算弹窗 -->
    <el-dialog v-model="showAddBudget" title="新建预算" width="400px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0.01" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddBudget = false">取消</el-button>
        <el-button type="primary" @click="addBudget">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getBudgetList, addBudget as apiBudgetAdd, deleteBudget as apiBudgetDelete } from '@/api/budget'
import { getCategoryList } from '@/api/category'

const selectedMonth = ref(dayjs().format('YYYY-MM'))
const budgetList = ref([])
const categories = ref([])
const loading = ref(false)
const showAddBudget = ref(false)

const formData = ref({
  categoryId: '',
  amount: 0
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
}

const fetchBudgetData = async () => {
  loading.value = true
  try {
    const data = await getBudgetList(selectedMonth.value)
    budgetList.value = data.map(item => ({
      ...item,
      percentage: item.budgetAmount ? Math.round((item.usedAmount / item.budgetAmount) * 100) : 0,
      remainingAmount: Math.max(0, item.budgetAmount - item.usedAmount)
    }))
  } catch (error) {
    console.error('获取预算数据失败:', error)
    ElMessage.error('获取预算数据失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const data = await getCategoryList(2)
    categories.value = data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const addBudget = async () => {
  try {
    await apiBudgetAdd({
      categoryId: formData.value.categoryId,
      amount: formData.value.amount,
      month: selectedMonth.value
    })
    ElMessage.success('预算创建成功')
    showAddBudget.value = false
    formData.value = { categoryId: '', amount: 0 }
    fetchBudgetData()
  } catch (error) {
    console.error('创建预算失败:', error)
    ElMessage.error('创建预算失败')
  }
}

const deleteBudget = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该预算吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await apiBudgetDelete(id)
    ElMessage.success('预算删除成功')
    fetchBudgetData()
  } catch (error) {
    console.error('删除预算失败:', error)
  }
}

onMounted(() => {
  fetchBudgetData()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.budget-page {
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.budget-content {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-light);

  .el-icon {
    font-size: 48px;
    margin-bottom: 16px;
    display: block;
  }
}

.budget-list {
  display: grid;
  gap: 16px;
}

.budget-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);

  .budget-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .category-info {
      .category-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .budget-amount {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }
  }

  .budget-stats {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;

    .stat {
      font-size: 14px;
      color: var(--text-secondary);

      .warning {
        color: var(--danger-color);
      }
    }
  }
}
</style>
