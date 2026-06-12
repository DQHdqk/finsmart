<template>
  <div class="share-page">
    <div class="page-header">
      <h1>AA共享</h1>
      <p class="page-description">和朋友一起记账，公平分摊费用</p>
    </div>

    <!-- 创建AA共享 -->
    <div class="card create-share">
      <div class="create-header">
        <h3>创建新共享</h3>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建共享
        </el-button>
      </div>
    </div>

    <!-- 共享列表 -->
    <div class="card share-list">
      <div class="list-header">
        <h3>共享账单</h3>
        <div class="filter-tabs">
          <el-radio-group v-model="filterStatus" @change="handleFilterChange">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="PENDING">待结算</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <div class="share-content" v-loading="loading">
        <div v-if="shares.length === 0" class="empty-state">
          <el-icon><Share /></el-icon>
          <p>还没有共享账单</p>
          <el-button type="primary" @click="showCreateDialog = true">
            创建第一个共享账单
          </el-button>
        </div>
        
        <div v-else class="share-grid">
          <div
            v-for="share in shares"
            :key="share.id"
            class="share-card"
            @click="viewShareDetail(share)"
          >
            <div class="card-header">
              <div class="share-info">
                <h4>{{ share.title }}</h4>
                <div class="share-meta">
                  <span class="total-amount">总额: ¥{{ share.totalAmount }}</span>
                  <span class="member-count">{{ share.memberCount }}人</span>
                </div>
              </div>
              <!-- 把状态徽章和删除按钮放一起 -->
              <div style="display:flex; align-items:center; gap:8px;">
                <div class="status-badge" :class="String(share.status).toLowerCase()">
                  {{ getStatusText(share.status) }}
                </div>
                <el-button type="danger" link size="small" @click.stop="handleDelete(share)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div class="card-content">
              <div class="per-person">
                <span class="label">每人应付:</span>
                <span class="amount">¥{{ share.perPersonAmount }}</span>
              </div>
              
              <div class="progress-info">
                <div class="progress-text">
                  已支付 {{ share.paidCount }}/{{ share.memberCount }} 人
                </div>
                <div class="progress-bar">
                  <div
                    class="progress-fill"
                    :style="{ width: getProgressPercentage(share) + '%' }"
                  ></div>
                </div>
              </div>
              
              <div class="share-date">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(share.createDate) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建共享弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建AA共享"
      width="600px"
      :before-close="handleCloseCreateDialog"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="createForm.title"
            placeholder="请输入共享账单标题"
          />
        </el-form-item>
        
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number
            v-model="createForm.totalAmount"
            :precision="2"
            :step="0.01"
            :min="0.01"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="参与人员" prop="members">
          <div class="members-section">
            <div
              v-for="(member, index) in createForm.members"
              :key="index"
              class="member-item"
            >
              <el-input
                v-model="member.name"
                placeholder="成员姓名"
                style="flex: 1"
              />
              <el-button
                type="text"
                size="small"
                @click="removeMember(index)"
                :disabled="createForm.members.length <= 2"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="text" @click="addMember">
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="分摊方式">
          <el-radio-group v-model="createForm.splitType">
            <el-radio label="EQUAL">平均分摊</el-radio>
            <el-radio label="CUSTOM">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseCreateDialog">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleCreate">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="共享账单详情"
      width="700px"
      :before-close="handleCloseDetailDialog"
    >
      <div class="share-detail" v-if="currentShare">
        <div class="detail-header">
          <h3>{{ currentShare.title }}</h3>
          <div class="detail-summary">
            <div class="summary-item">
              <span class="label">总金额:</span>
              <span class="value">¥{{ currentShare.totalAmount }}</span>
            </div>
            <div class="summary-item">
              <span class="label">每人应付:</span>
              <span class="value">¥{{ currentShare.perPersonAmount }}</span>
            </div>
            <div class="summary-item">
              <span class="label">状态:</span>
              <span class="status-badge" :class="String(currentShare.status).toLowerCase()">
                {{ getStatusText(currentShare.status) }}
              </span>
            </div>
          </div>
          <div class="detail-progress">
            <div class="progress-text">
              已存入 ¥{{ currentShare.totalPaidAmount || 0 }} / ¥{{ currentShare.totalAmount }}
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: getProgressPercentage(currentShare) + '%' }"></div>
            </div>
          </div>
        </div>
        
        <div class="members-section">
          <h4>成员列表</h4>
          <div class="members-list">
            <div
              v-for="member in currentShare.members"
              :key="member.id"
              class="member-card"
            >
              <div class="member-info">
                <el-avatar :size="40">{{ member.name.charAt(0) }}</el-avatar>
                <div class="member-details">
                  <div class="member-name">{{ member.name }}</div>
                  <div class="member-amount">应付: ¥{{ member.amount }}　已存: ¥{{ member.paidAmount || 0 }}</div>
                </div>
              </div>
              
              <div class="member-status">
                <el-tag
                  :type="member.paid ? 'success' : 'warning'"
                  size="small"
                >
                  {{ member.paid ? '已支付' : '待支付' }}
                </el-tag>
                <el-button v-if="!member.paid" type="primary" size="small" @click="openPayDialog(member)">
                  存入
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="description-section" v-if="currentShare.description">
          <h4>描述</h4>
          <p>{{ currentShare.description }}</p>
        </div>
      </div>
      <!-- 存入弹窗 -->
      <el-dialog v-model="showPayDialog" :title="payingMemberName + ' 存入'" width="400px">
        <el-form label-width="80px">
          <el-form-item label="存入金额">
            <el-input-number v-model="payForm.paidAmount" :precision="2" :min="0.01" style="width:100%" />
          </el-form-item>
          <el-form-item label="存入时间">
            <el-date-picker
              v-model="payForm.paidTime"
              type="datetime"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              style="width:100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="payForm.paidRemark" placeholder="可选备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showPayDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmPay">确认存入</el-button>
        </template>
    </el-dialog>

    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getShareList, createShare, getShareDetail, markMemberPaid, deleteShare } from '@/api/share'
// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const filterStatus = ref('ALL')
// 存入弹窗
const showPayDialog = ref(false)
const payingMemberId = ref(null)
const payingMemberName = ref('')
const payForm = reactive({
  paidAmount: 0,
  paidTime: dayjs().format('YYYY-MM-DD HH:mm'),
  paidRemark: ''
})
// 共享数据
const shares = ref([])
const currentShare = ref(null)

// 创建表单
const createFormRef = ref()
const createForm = reactive({
  title: '',
  totalAmount: 0,
  members: [
    { name: '' },
    { name: '' }
  ],
  splitType: 'EQUAL',
  description: ''
})

const createRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入总金额', trigger: 'blur' }],
  members: [
    {
      validator: (rule, value, callback) => {
        const validMembers = value.filter(m => m.name.trim())
        if (validMembers.length < 2) {
          callback(new Error('至少需要2个成员'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 工具函数
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待结算',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    0: '待结算',    // 可能的数字状态值
    1: '已完成',
    2: '已取消'
  }
  return statusMap[status] || status || '未知状态'
}

const getProgressPercentage = (share) => {
  const total = Number(share.totalAmount)
  if (!total) return 0
  return Math.min((Number(share.totalPaidAmount || 0) / total) * 100, 100)
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取共享列表
const fetchShares = async () => {
  loading.value = true
  try {
    const data = await getShareList()
    console.log('获取共享列表原始数据:', data)
    
    // 检查数据结构并处理
    if (Array.isArray(data)) {
      shares.value = data.map(share => {
        console.log('处理共享项:', share)
        // 确保必要字段存在，如果不存在则设置默认值
        return {
          id: share.id,
          title: share.title,
          totalAmount: share.totalAmount || 0,
          totalPaidAmount: share.totalPaidAmount || 0,
          memberCount: share.memberCount || (share.members ? share.members.length : 0),
          paidCount: share.paidCount || (share.members ? share.members.filter(m => m.isPaid).length : 0),
          perPersonAmount: share.perPersonAmount || (share.totalAmount / (share.memberCount || 1)),
          status: String(share.status || 'PENDING'),
          createDate: share.createDate || share.createTime,
          members: share.members || []
        }
      })
    } else {
      shares.value = []
    }
    
    console.log('处理后的shares.value:', shares.value)
  } catch (error) {
    console.error('获取共享列表失败:', error)
    ElMessage.error('获取共享列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  console.log('筛选状态变化:', filterStatus.value)
  // 这里可以调用筛选接口
  if (filterStatus.value === 'ALL') {
    fetchShares()
  } else {
    console.log('筛选前shares数量:', shares.value.length)
    // 模拟筛选
    const filtered = shares.value.filter(share => share.status === filterStatus.value)
    console.log('筛选后shares数量:', filtered.length)
    shares.value = filtered
  }
}

// 查看详情
const viewShareDetail = async (share) => {
  try {
    const detail = await getShareDetail(share.id)
    currentShare.value = detail
    showDetailDialog.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 删除账单
const handleDelete = async (share) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除账单「${share.title}」吗？删除后无法恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await deleteShare(share.id)
    ElMessage.success('删除成功')
    fetchShares()
  } catch (error) {
    // 用户点"取消"时 ElMessageBox 会抛出 'cancel'，这种情况不算错误
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 标记已支付
// 点击按钮，打开弹窗
const openPayDialog = (member) => {
  payingMemberId.value = member.id
  payingMemberName.value = member.name
  // 默认填"还差多少"，而不是整份应付金额
  const amount = Number(member.amount || 0)
  const paid = Number(member.paidAmount || 0)
  const remaining = Math.max(amount - paid, 0)
  payForm.paidAmount = remaining > 0 ? remaining : 0.01
  payForm.paidTime = dayjs().format('YYYY-MM-DD HH:mm')
  payForm.paidRemark = ''
  showPayDialog.value = true
}

// 弹窗确认提交
const confirmPay = async () => {
  try {
    await markMemberPaid({
      memberId: payingMemberId.value,
      paidAmount: payForm.paidAmount,
      paidTime: payForm.paidTime,
      paidRemark: payForm.paidRemark
    })
    ElMessage.success('存入成功')
    showPayDialog.value = false
    if (currentShare.value) {
      currentShare.value = await getShareDetail(currentShare.value.id)
    }
    fetchShares()
  } catch (error) {
    ElMessage.error('存入失败')
  }
}

// 添加成员
const addMember = () => {
  createForm.members.push({ name: '' })
}

// 删除成员
const removeMember = (index) => {
  if (createForm.members.length > 2) {
    createForm.members.splice(index, 1)
  }
}

// 创建共享
const handleCreate = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    submitting.value = true
    
    const validMembers = createForm.members.filter(m => m.name.trim())
    const memberNames = validMembers.map(m => m.name.trim())
    
    // 打印提交数据确认格式
    console.log('提交数据:', {
      title: createForm.title,
      totalAmount: createForm.totalAmount,
      memberNames: memberNames,
      splitType: createForm.splitType,
      description: createForm.description
    })
    
    await createShare({
      title: createForm.title,
      totalAmount: createForm.totalAmount,
      memberNames: memberNames,
      splitType: createForm.splitType,
      description: createForm.description
    })
    
    ElMessage.success('创建成功')
    console.log('创建成功，开始刷新列表...')
    handleCloseCreateDialog()
    await fetchShares()
    console.log('列表刷新完成')
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 关闭创建弹窗
const handleCloseCreateDialog = () => {
  showCreateDialog.value = false
  
  // 重置表单
  Object.assign(createForm, {
    title: '',
    totalAmount: 0,
    members: [
      { name: '' },
      { name: '' }
    ],
    splitType: 'EQUAL',
    description: ''
  })
  
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
}

// 关闭详情弹窗
const handleCloseDetailDialog = () => {
  showDetailDialog.value = false
  currentShare.value = null
}

// 页面加载
onMounted(() => {
  fetchShares()
})
</script>

<style lang="scss" scoped>
.share-page {
  max-width: 900px;
  margin: 0 auto;
}

.create-share {
  margin-bottom: 24px;
  
  .create-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
  }
}

.share-list {
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

.share-content {
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

.share-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.share-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    
    .share-info {
      flex: 1;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
      }
      
      .share-meta {
        display: flex;
        gap: 12px;
        align-items: center;
        
        .total-amount {
          font-size: 14px;
          font-weight: 500;
          color: var(--primary-color);
        }
        
        .member-count {
          font-size: 12px;
          color: var(--text-secondary);
          background: var(--bg-gray);
          padding: 2px 8px;
          border-radius: 12px;
        }
      }
    }
    
    .status-badge {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 12px;
      font-weight: 500;
      
      &.pending {
        background: rgba(255, 170, 0, 0.1);
        color: var(--warning-color);
      }
      
      &.completed {
        background: rgba(0, 214, 143, 0.1);
        color: var(--success-color);
      }
      
      &.cancelled {
        background: rgba(255, 61, 113, 0.1);
        color: var(--danger-color);
      }
    }
  }
  
  .card-content {
    .per-person {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .label {
        font-size: 13px;
        color: var(--text-secondary);
      }
      
      .amount {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
    
    .progress-info {
      margin-bottom: 12px;
      
      .progress-text {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }
      
      .progress-bar {
        width: 100%;
        height: 6px;
        background: var(--border-color);
        border-radius: 3px;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          background: var(--success-color);
          border-radius: 3px;
          transition: width 0.3s ease;
        }
      }
    }
    
    .share-date {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: var(--text-secondary);
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.members-section {
  .member-item {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
  }
}

.share-detail {
  .detail-header {
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 16px 0;
    }
    
    .detail-summary {
      display: flex;
      gap: 24px;
      
      .summary-item {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .label {
          font-size: 14px;
          color: var(--text-secondary);
        }
        
        .value {
          font-size: 14px;
          font-weight: 600;
          color: var(--text-primary);
        }
        
        .status-badge {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 12px;
          font-weight: 500;
          
          &.pending {
            background: rgba(255, 170, 0, 0.1);
            color: var(--warning-color);
          }
          
          &.completed {
            background: rgba(0, 214, 143, 0.1);
            color: var(--success-color);
          }
          
          &.cancelled {
            background: rgba(255, 61, 113, 0.1);
            color: var(--danger-color);
          }
        }
      }
    }
  }
  
  .members-section {
    margin-bottom: 24px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 16px 0;
    }
    
    .members-list {
      .member-card {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        background: var(--bg-gray);
        border-radius: var(--radius-sm);
        margin-bottom: 8px;
        
        .member-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .member-details {
            .member-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--text-primary);
              margin-bottom: 2px;
            }
            
            .member-amount {
              font-size: 12px;
              color: var(--text-secondary);
            }
          }
        }
        
        .member-status {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
  
  .description-section {
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 12px 0;
    }
    
    p {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.5;
      margin: 0;
    }
  }
}

@media (max-width: 768px) {
  .share-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-summary {
    flex-direction: column;
    gap: 12px;
  }
}
.detail-progress {
  margin-top: 16px;
  .progress-text { font-size: 13px; color: var(--text-secondary); margin-bottom: 6px; }
  .progress-bar {
    width: 100%; height: 8px; background: var(--border-color);
    border-radius: 4px; overflow: hidden;
    .progress-fill {
      height: 100%; background: var(--success-color);
      border-radius: 4px; transition: width 0.3s ease;
    }
  }
}
</style>
