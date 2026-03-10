<template>
  <div class="order-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <p>管理订单、售票、支付与退款</p>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select
            v-model="query.status"
            placeholder="订单状态"
            clearable
            style="width: 140px"
            @change="loadData"
          >
            <el-option
              v-for="(label, key) in statusMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
          <el-select
            v-model="query.scenicSpotId"
            placeholder="选择景区"
            clearable
            filterable
            style="width: 160px"
            @change="loadData"
          >
            <el-option
              v-for="item in spotList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-input
            v-model="query.orderNo"
            placeholder="订单号"
            clearable
            style="width: 180px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-input
            v-model="query.visitorName"
            placeholder="游客姓名"
            clearable
            style="width: 140px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            @change="loadData"
          />
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openSellDialog">售票</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="scenicSpotName" label="景区名称" min-width="120" />
        <el-table-column prop="visitorName" label="游客姓名" width="100" />
        <el-table-column prop="visitorPhone" label="游客手机" width="120" />
        <el-table-column prop="visitDate" label="游览日期" width="110" />
        <el-table-column prop="totalAmount" label="总金额" width="90" align="right" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]" effect="light">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetailDialog(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              @click="handlePay(row)"
            >支付</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="warning"
              link
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button
              v-if="row.status === 'PAID'"
              type="danger"
              link
              @click="handleRefund(row)"
            >退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </div>

    <!-- 售票对话框 (CreateOrderDialog) -->
    <el-dialog v-model="sellDialogVisible" title="售票" width="560px" destroy-on-close :close-on-click-modal="false">
      <el-steps :active="sellStep" finish-status="success" align-center class="sell-steps">
        <el-step title="选择景区" />
        <el-step title="选择票种" />
        <el-step title="填写信息" />
      </el-steps>

      <!-- 步骤1: 选择景区 -->
      <div v-show="sellStep === 0" class="sell-step-content">
        <el-form-item label="选择景区">
          <el-select
            v-model="sellForm.scenicSpotId"
            placeholder="请选择景区"
            filterable
            style="width: 100%"
            @change="onScenicChange"
          >
            <el-option
              v-for="item in spotList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <!-- 步骤2: 选择票种和数量 -->
      <div v-show="sellStep === 1" class="sell-step-content">
        <el-table :data="ticketTypesForSell" max-height="260">
          <el-table-column prop="name" label="票种" />
          <el-table-column prop="sellPrice" label="售价" width="80" align="right" />
          <el-table-column prop="stock" label="库存" width="70" align="right" />
          <el-table-column label="数量" width="140" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="sellForm.items[row.id]"
                :min="0"
                :max="row.stock"
                size="small"
                controls-position="right"
                class="quantity-input"
              />
            </template>
          </el-table-column>
        </el-table>
        <p v-if="ticketTypesForSell.length === 0" class="empty-tip">该景区暂无可用票种，请先上架票种</p>
      </div>

      <!-- 步骤3: 填写游客信息 -->
      <div v-show="sellStep === 2" class="sell-step-content">
        <el-form ref="sellFormRef" :model="sellForm" :rules="sellRules" label-width="100px">
          <el-form-item label="游客姓名" prop="visitorName">
            <el-input v-model="sellForm.visitorName" placeholder="请输入游客姓名" />
          </el-form-item>
          <el-form-item label="游客手机" prop="visitorPhone">
            <el-input v-model="sellForm.visitorPhone" placeholder="请输入游客手机" />
          </el-form-item>
          <el-form-item label="身份证号" prop="visitorIdCard">
            <el-input v-model="sellForm.visitorIdCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="游览日期" prop="visitDate">
            <el-date-picker
              v-model="sellForm.visitDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择游览日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="sellForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button v-if="sellStep > 0" @click="sellStep--">上一步</el-button>
        <el-button v-if="sellStep < 2" type="primary" :disabled="!canNextStep" @click="sellStep++">
          下一步
        </el-button>
        <el-button v-if="sellStep === 2" type="primary" :loading="sellLoading" @click="handleSellSubmit">
          提交订单
        </el-button>
        <el-button @click="sellDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="640px" destroy-on-close>
      <div v-if="orderDetail" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagMap[orderDetail.status]">
              {{ statusMap[orderDetail.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="景区名称">{{ orderDetail.scenicSpotName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ orderDetail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="游客姓名">{{ orderDetail.visitorName }}</el-descriptions-item>
          <el-descriptions-item label="游客手机">{{ orderDetail.visitorPhone }}</el-descriptions-item>
          <el-descriptions-item label="身份证号" :span="2">{{ orderDetail.visitorIdCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="游览日期">{{ orderDetail.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ orderDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 16px">订单明细</h4>
        <el-table :data="orderDetail.items || []" size="small" border>
          <el-table-column prop="ticketTypeName" label="票种" />
          <el-table-column prop="quantity" label="数量" width="80" align="right" />
          <el-table-column prop="price" label="单价" width="90" align="right" />
          <el-table-column prop="amount" label="小计" width="90" align="right" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getOrders,
  getOrderDetail,
  createOrder,
  payOrder,
  cancelOrder,
  refundOrder
} from '@/api/order'
import { getActiveSpots } from '@/api/scenic'
import { getActiveTicketTypes } from '@/api/ticket'

const statusMap: Record<string, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
  USED: '已使用'
}

const statusTagMap: Record<string, string> = {
  PENDING: 'warning',
  PAID: 'success',
  CANCELLED: 'info',
  REFUNDED: 'danger',
  USED: 'primary'
}

const loading = ref(false)
const sellLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const spotList = ref<any[]>([])
const ticketTypesForSell = ref<any[]>([])
const orderDetail = ref<any>(null)
const sellDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const sellStep = ref(0)
const sellFormRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  visitorName: '',
  status: '',
  scenicSpotId: undefined as number | undefined,
  dateRange: [] as string[]
})

const sellForm = reactive({
  scenicSpotId: undefined as number | undefined,
  items: {} as Record<number, number>,
  visitorName: '',
  visitorPhone: '',
  visitorIdCard: '',
  visitDate: '',
  remark: ''
})

const sellRules: FormRules = {
  visitorName: [{ required: true, message: '请输入游客姓名', trigger: 'blur' }],
  visitorPhone: [{ required: true, message: '请输入游客手机', trigger: 'blur' }],
  visitDate: [{ required: true, message: '请选择游览日期', trigger: 'change' }]
}

const canNextStep = computed(() => {
  if (sellStep.value === 0) return !!sellForm.scenicSpotId
  if (sellStep.value === 1) {
    const totalQty = Object.values(sellForm.items).reduce((a, b) => a + (b || 0), 0)
    return totalQty > 0
  }
  return true
})

async function loadSpots() {
  try {
    const res: any = await getActiveSpots()
    spotList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载景区列表失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
      orderNo: query.orderNo || undefined,
      visitorName: query.visitorName || undefined,
      status: query.status || undefined,
      scenicSpotId: query.scenicSpotId || undefined
    }
    if (query.dateRange?.length === 2) {
      params.startDate = query.dateRange[0]
      params.endDate = query.dateRange[1]
    }
    const res: any = await getOrders(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

function resetSellForm() {
  sellStep.value = 0
  sellForm.scenicSpotId = undefined
  sellForm.items = {}
  sellForm.visitorName = ''
  sellForm.visitorPhone = ''
  sellForm.visitorIdCard = ''
  sellForm.visitDate = ''
  sellForm.remark = ''
  ticketTypesForSell.value = []
}

function openSellDialog() {
  resetSellForm()
  sellDialogVisible.value = true
}

async function onScenicChange() {
  ticketTypesForSell.value = []
  sellForm.items = {}
  if (!sellForm.scenicSpotId) return
  try {
    const res: any = await getActiveTicketTypes(sellForm.scenicSpotId)
    ticketTypesForSell.value = res.data || []
    ticketTypesForSell.value.forEach((t: any) => {
      sellForm.items[t.id] = 0
    })
  } catch (e) {
    ElMessage.error('加载票种失败')
  }
}

async function handleSellSubmit() {
  const valid = await sellFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const items = Object.entries(sellForm.items)
    .filter(([, qty]) => (qty || 0) > 0)
    .map(([id, qty]) => ({ ticketTypeId: Number(id), quantity: qty }))
  if (items.length === 0) {
    ElMessage.warning('请至少选择一种票种并填写数量')
    return
  }
  sellLoading.value = true
  try {
    await createOrder({
      scenicSpotId: sellForm.scenicSpotId,
      items,
      visitorName: sellForm.visitorName,
      visitorPhone: sellForm.visitorPhone,
      visitorIdCard: sellForm.visitorIdCard || undefined,
      visitDate: sellForm.visitDate,
      remark: sellForm.remark || undefined
    })
    ElMessage.success('订单创建成功')
    sellDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('创建订单失败')
  } finally {
    sellLoading.value = false
  }
}

async function openDetailDialog(row: any) {
  try {
    const res: any = await getOrderDetail(row.id)
    orderDetail.value = res.data
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载订单详情失败')
  }
}

async function handlePay(row: any) {
  try {
    await ElMessageBox.confirm('确定已收到该订单的支付？', '确认支付', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await payOrder(row.id)
    ElMessage.success('支付成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('支付失败')
  }
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm('确定要取消该订单？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

async function handleRefund(row: any) {
  try {
    await ElMessageBox.confirm('确定要退款？退款后无法撤销。', '确认退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await refundOrder(row.id)
    ElMessage.success('退款成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('退款失败')
  }
}

onMounted(() => {
  loadSpots()
  loadData()
})
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-4);
}
.toolbar-left {
  display: flex;
  gap: var(--spacing-2);
  flex-wrap: wrap;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-4);
}
.sell-steps {
  margin-bottom: var(--spacing-6);
}
.sell-step-content {
  min-height: 180px;
  padding: var(--spacing-4) 0;
}
.empty-tip {
  color: var(--color-gray-500);
  text-align: center;
  padding: var(--spacing-8);
}
.order-detail :deep(.el-descriptions__label) {
  width: 100px;
}
.quantity-input {
  width: 120px;
  max-width: 100%;
}
.quantity-input :deep(.el-input-number) {
  width: 100%;
}
</style>
