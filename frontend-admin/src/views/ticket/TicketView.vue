<template>
  <div class="ticket-page">
    <div class="page-header">
      <h2>票种管理</h2>
      <p>管理景区票种、售价、库存及上下架状态</p>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select
            v-model="query.scenicSpotId"
            placeholder="选择景区"
            clearable
            filterable
            style="width: 180px"
            @change="loadData"
          >
            <el-option
              v-for="item in spotList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="query.category"
            placeholder="类别"
            clearable
            style="width: 120px"
            @change="loadData"
          >
            <el-option
              v-for="(label, key) in categoryMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
          <el-input
            v-model="query.name"
            placeholder="搜索票种名称"
            clearable
            style="width: 200px"
            @clear="loadData"
            @keyup.enter="loadData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增票种</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="票种名称" min-width="120" />
        <el-table-column prop="scenicSpotName" label="所属景区" min-width="120" />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">{{ categoryMap[row.category] || row.category }}</template>
        </el-table-column>
        <el-table-column prop="price" label="售价" width="90" align="right" />
        <el-table-column prop="originalPrice" label="原价" width="90" align="right" />
        <el-table-column prop="stock" label="库存" width="80" align="right" />
        <el-table-column prop="dailyLimit" label="每日限额" width="100" align="right" />
        <el-table-column prop="soldToday" label="今日已售" width="90" align="right" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="primary" link @click="openStockDialog(row)">库存调整</el-button>
            <el-popconfirm title="确定删除该票种？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑票种' : '新增票种'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属景区" prop="scenicSpotId">
          <el-select
            v-model="form.scenicSpotId"
            placeholder="请选择景区"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in spotList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="票种名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入票种名称" />
        </el-form-item>
        <el-form-item label="票种描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入票种描述" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
            <el-option
              v-for="(label, key) in categoryMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每日限额" prop="dailyLimit">
              <el-input-number v-model="form.dailyLimit" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="有效天数" prop="validDays">
          <el-input-number v-model="form.validDays" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存调整弹窗 -->
    <el-dialog v-model="stockDialogVisible" title="库存调整" width="400px" destroy-on-close>
      <el-form ref="stockFormRef" :model="stockForm" :rules="stockRules" label-width="100px">
        <el-form-item label="票种">{{ currentTicket?.name }}</el-form-item>
        <el-form-item label="当前库存">{{ currentTicket?.stock }}</el-form-item>
        <el-form-item label="调整数量" prop="stock">
          <el-input-number v-model="stockForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getTicketTypes,
  createTicketType,
  updateTicketType,
  deleteTicketType,
  toggleTicketTypeStatus,
  adjustTicketStock
} from '@/api/ticket'
import { getActiveSpots } from '@/api/scenic'

const categoryMap: Record<string, string> = {
  ADULT: '成人票',
  CHILD: '儿童票',
  SENIOR: '老人票',
  STUDENT: '学生票',
  GROUP: '团体票'
}

const loading = ref(false)
const submitLoading = ref(false)
const stockLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const spotList = ref<any[]>([])
const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const currentTicket = ref<any>(null)
const formRef = ref<FormInstance>()
const stockFormRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  scenicSpotId: undefined as number | undefined,
  category: ''
})

const form = reactive({
  scenicSpotId: undefined as number | undefined,
  name: '',
  description: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  dailyLimit: 0,
  validDays: 1,
  category: 'ADULT'
})

const stockForm = reactive({ stock: 0 })

const rules: FormRules = {
  scenicSpotId: [{ required: true, message: '请选择景区', trigger: 'change' }],
  name: [{ required: true, message: '请输入票种名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const stockRules: FormRules = {
  stock: [{ required: true, message: '请输入调整后库存', trigger: 'blur' }]
}

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
      name: query.name || undefined,
      category: query.category || undefined,
      scenicSpotId: query.scenicSpotId || undefined
    }
    const res: any = await getTicketTypes(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('加载票种列表失败')
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, {
      scenicSpotId: row.scenicSpotId,
      name: row.name,
      description: row.description,
      price: row.price,
      originalPrice: row.originalPrice,
      stock: row.stock,
      dailyLimit: row.dailyLimit ?? 0,
      validDays: row.validDays ?? 1,
      category: row.category || 'ADULT'
    })
  } else {
    Object.assign(form, {
      scenicSpotId: undefined,
      name: '',
      description: '',
      price: 0,
      originalPrice: 0,
      stock: 0,
      dailyLimit: 0,
      validDays: 1,
      category: 'ADULT'
    })
  }
  dialogVisible.value = true
}

function openStockDialog(row: any) {
  currentTicket.value = row
  stockForm.stock = row.stock
  stockDialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateTicketType(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createTicketType(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleStockSubmit() {
  const valid = await stockFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!currentTicket.value) return
  stockLoading.value = true
  try {
    await adjustTicketStock(currentTicket.value.id, stockForm.stock)
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('库存调整失败')
  } finally {
    stockLoading.value = false
  }
}

async function handleToggle(row: any) {
  try {
    await toggleTicketTypeStatus(row.id)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteTicketType(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
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
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-4);
}
</style>
