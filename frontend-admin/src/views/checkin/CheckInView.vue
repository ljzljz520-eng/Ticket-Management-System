<template>
  <div class="checkin-page">
    <div class="page-header">
      <h2>检票管理</h2>
      <p>检票入园、出园登记、检票记录</p>
    </div>

    <!-- 今日统计卡片 -->
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">今日检票数</div>
        <div class="stat-value">{{ stats.totalCheckIn }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">在园人数</div>
        <div class="stat-value">{{ stats.inPark }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">已出园</div>
        <div class="stat-value">{{ stats.checkedOut }}</div>
      </el-card>
    </div>

    <div class="page-card">
      <!-- 检票入园操作区 -->
      <div class="checkin-area">
        <el-input
          v-model="checkInForm.orderNo"
          placeholder="请输入订单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleCheckIn"
        />
        <el-input
          v-model="checkInForm.gateNo"
          placeholder="闸机号（可选）"
          clearable
          style="width: 140px"
          @keyup.enter="handleCheckIn"
        />
        <el-button type="primary" :loading="checkInLoading" @click="handleCheckIn">
          检票入园
        </el-button>
      </div>

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
            v-model="query.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="loadData"
          >
            <el-option label="在园" value="CHECKED_IN" />
            <el-option label="已出园" value="CHECKED_OUT" />
          </el-select>
          <el-date-picker
            v-model="query.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 160px"
            @change="loadData"
          />
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
      </div>

      <!-- 检票记录表格 -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="visitorName" label="游客姓名" width="100" />
        <el-table-column prop="scenicSpotName" label="景区名称" min-width="120" />
        <el-table-column prop="checkInTime" label="入园时间" width="170" />
        <el-table-column prop="checkOutTime" label="出园时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" effect="light">
              {{ row.status === 'CHECKED_IN' ? '在园' : '已出园' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作员" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'CHECKED_IN'"
              type="primary"
              link
              @click="handleCheckOut(row)"
            >
              出园登记
            </el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  checkIn,
  checkOut,
  getCheckInRecords,
  getTodayCheckInStats
} from '@/api/checkin'
import { getActiveSpots } from '@/api/scenic'

const loading = ref(false)
const checkInLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const spotList = ref<any[]>([])
const stats = reactive({
  totalCheckIn: 0,
  inPark: 0,
  checkedOut: 0
})

const checkInForm = reactive({
  orderNo: '',
  gateNo: ''
})

const query = reactive({
  page: 1,
  size: 10,
  scenicSpotId: undefined as number | undefined,
  status: '',
  date: ''
})

async function loadSpots() {
  try {
    const res: any = await getActiveSpots()
    spotList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载景区列表失败')
  }
}

async function loadStats() {
  try {
    const res: any = await getTodayCheckInStats()
    const d = res.data || {}
    stats.totalCheckIn = d.totalCheckIn ?? 0
    stats.inPark = d.inPark ?? 0
    stats.checkedOut = d.checkedOut ?? 0
  } catch (e) {
    ElMessage.error('加载今日统计失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size,
      scenicSpotId: query.scenicSpotId || undefined,
      status: query.status || undefined,
      date: query.date || undefined
    }
    const res: any = await getCheckInRecords(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total ?? 0
  } catch (e) {
    ElMessage.error('加载检票记录失败')
  } finally {
    loading.value = false
  }
}

async function handleCheckIn() {
  const orderNo = checkInForm.orderNo?.trim()
  if (!orderNo) {
    ElMessage.warning('请输入订单号')
    return
  }
  checkInLoading.value = true
  try {
    await checkIn({
      orderNo,
      gateNo: checkInForm.gateNo?.trim() || undefined
    })
    ElMessage.success('检票入园成功')
    checkInForm.orderNo = ''
    checkInForm.gateNo = ''
    loadStats()
    loadData()
  } catch (e) {
    ElMessage.error('检票入园失败')
  } finally {
    checkInLoading.value = false
  }
}

async function handleCheckOut(row: any) {
  try {
    await ElMessageBox.confirm('确定该游客已出园？', '出园登记', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await checkOut(row.id)
    ElMessage.success('出园登记成功')
    loadStats()
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('出园登记失败')
  }
}

onMounted(() => {
  loadSpots()
  loadStats()
  loadData()
})
</script>

<style lang="scss" scoped>
.stats-cards {
  display: flex;
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-6);
}
.stat-card {
  flex: 1;
  text-align: center;
  .stat-label {
    font-size: var(--text-sm);
    color: var(--color-gray-500);
    margin-bottom: var(--spacing-2);
  }
  .stat-value {
    font-size: var(--text-2xl);
    font-weight: 600;
    color: var(--color-primary);
  }
}
.checkin-area {
  display: flex;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
  padding: var(--spacing-4);
  background: var(--color-gray-50);
  border-radius: var(--radius-md);
}
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
