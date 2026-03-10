<template>
  <div class="log-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <p>查看系统操作记录</p>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="query.username"
            placeholder="用户名"
            clearable
            style="width: 160px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
          <el-input
            v-model="query.operation"
            placeholder="操作描述"
            clearable
            style="width: 200px"
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
      </div>

      <!-- 日志列表 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="100" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 || row.success ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 || row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  username: '',
  operation: '',
  dateRange: [] as string[]
})

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
      username: query.username || undefined,
      operation: query.operation || undefined
    }
    if (query.dateRange?.length === 2) {
      params.startDate = query.dateRange[0]
      params.endDate = query.dateRange[1]
    }
    const res: any = await getLogs(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
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
