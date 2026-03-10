<template>
  <div class="scenic-page">
    <div class="page-header">
      <h2>景区管理</h2>
      <p>管理景区基本信息、开放状态</p>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.name" placeholder="搜索景区名称" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增景区</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="景区名称" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="开放时间" width="150">
          <template #default="{ row }">{{ row.openTime }} - {{ row.closeTime }}</template>
        </el-table-column>
        <el-table-column prop="maxCapacity" label="最大容量" width="100" />
        <el-table-column prop="currentVisitors" label="当前人数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggle(row)">
              {{ row.status === 1 ? '关闭' : '开放' }}
            </el-button>
            <el-popconfirm title="确定删除该景区？" @confirm="handleDelete(row.id)">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑景区' : '新增景区'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="景区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入景区名称" />
        </el-form-item>
        <el-form-item label="景区地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入景区地址" />
        </el-form-item>
        <el-form-item label="景区描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入景区描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开放时间">
              <el-input v-model="form.openTime" placeholder="如 08:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关闭时间">
              <el-input v-model="form.closeTime" placeholder="如 18:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最大容量" prop="maxCapacity">
              <el-input-number v-model="form.maxCapacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getScenicSpots, createScenicSpot, updateScenicSpot, deleteScenicSpot, toggleScenicSpotStatus } from '@/api/scenic'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 10, name: '' })

const form = reactive({
  name: '', description: '', address: '', openTime: '08:00', closeTime: '18:00',
  maxCapacity: 5000, contactPhone: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入景区名称', trigger: 'blur' }],
  maxCapacity: [{ required: true, message: '请输入最大容量', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getScenicSpots(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, { name: row.name, description: row.description, address: row.address,
      openTime: row.openTime, closeTime: row.closeTime, maxCapacity: row.maxCapacity, contactPhone: row.contactPhone })
  } else {
    Object.assign(form, { name: '', description: '', address: '', openTime: '08:00', closeTime: '18:00', maxCapacity: 5000, contactPhone: '' })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateScenicSpot(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createScenicSpot(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

async function handleToggle(row: any) {
  await toggleScenicSpotStatus(row.id)
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(id: number) {
  await deleteScenicSpot(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
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
