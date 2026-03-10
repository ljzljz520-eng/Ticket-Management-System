<template>
  <div class="user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <p>管理系统用户、角色与状态</p>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="query.keyword"
            placeholder="用户名/姓名/手机/邮箱"
            clearable
            style="width: 240px"
            @clear="loadData"
            @keyup.enter="loadData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select
            v-model="query.role"
            placeholder="角色"
            clearable
            style="width: 120px"
            @change="loadData"
          >
            <el-option
              v-for="(label, key) in roleMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
          <el-select
            v-model="query.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="loadData"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ roleMap[row.role] || row.role }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleToggle(row)"
              :disabled="row.username === 'admin'"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)" :disabled="row.username === 'admin'">
              <template #reference>
                <el-button type="danger" link :disabled="row.username === 'admin'">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="isEdit ? editRules : createRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="(label, key) in roleMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  toggleUserStatus,
  resetPassword
} from '@/api/user'

const roleMap: Record<string, string> = {
  ADMIN: '管理员',
  USER: '普通用户'
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  role: '',
  status: undefined as number | undefined
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'USER'
})

const createRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  phone: [
    { required: false, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const editRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: false, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      role: query.role || undefined,
      status: query.status !== undefined ? query.status : undefined
    }
    const res: any = await getUsers(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  isEdit.value = !!row
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, {
      username: row.username,
      password: '',
      realName: row.realName || '',
      phone: row.phone || '',
      email: row.email || '',
      role: row.role || 'USER'
    })
  } else {
    Object.assign(form, {
      username: '',
      password: '',
      realName: '',
      phone: '',
      email: '',
      role: 'USER'
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      const { password, ...updateData } = form as any
      await updateUser(editId.value, updateData)
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
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

async function handleToggle(row: any) {
  try {
    await toggleUserStatus(row.id)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleResetPwd(row: any) {
  try {
    await ElMessageBox.confirm('确定将密码重置为 123456？', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('重置密码失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
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
