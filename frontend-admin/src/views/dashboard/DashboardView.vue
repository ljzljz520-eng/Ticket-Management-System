<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>数据总览</h2>
      <p>实时掌握景区运营状况</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="(card, index) in statCards" :key="card.title">
        <div class="stat-card" :style="{ animationDelay: `${index * 80}ms` }">
          <div class="stat-icon" :style="{ background: card.bgColor }">
            <el-icon :size="24" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">{{ card.title }}</span>
            <span class="stat-value">{{ card.prefix }}{{ card.value }}{{ card.suffix }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <div class="page-card chart-card">
          <h3 class="card-title">销售趋势</h3>
          <div ref="salesChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card chart-card">
          <h3 class="card-title">票种销售占比</h3>
          <div ref="pieChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="page-card chart-card">
          <h3 class="card-title">游客趋势</h3>
          <div ref="visitorChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card chart-card">
          <h3 class="card-title">景区数据对比</h3>
          <div ref="spotChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getSalesTrend, getTicketTypeSales, getVisitorTrend, getScenicSpotComparison } from '@/api/statistics'

const overview = ref<any>({})
const salesChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const visitorChartRef = ref<HTMLElement>()
const spotChartRef = ref<HTMLElement>()

let salesChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let visitorChart: echarts.ECharts | null = null
let spotChart: echarts.ECharts | null = null

const statCards = computed(() => [
  {
    title: '今日收入',
    value: formatAmount(overview.value.todayIncome || 0),
    prefix: '¥',
    suffix: '',
    icon: 'Wallet',
    color: '#2563EB',
    bgColor: 'rgba(37, 99, 235, 0.1)'
  },
  {
    title: '今日订单',
    value: overview.value.todayOrders || 0,
    prefix: '',
    suffix: '笔',
    icon: 'Document',
    color: '#059669',
    bgColor: 'rgba(5, 150, 105, 0.1)'
  },
  {
    title: '今日游客',
    value: overview.value.todayVisitors || 0,
    prefix: '',
    suffix: '人',
    icon: 'UserFilled',
    color: '#F59E0B',
    bgColor: 'rgba(245, 158, 11, 0.1)'
  },
  {
    title: '当前在园',
    value: overview.value.currentVisitors || 0,
    prefix: '',
    suffix: '人',
    icon: 'Place',
    color: '#EF4444',
    bgColor: 'rgba(239, 68, 68, 0.1)'
  }
])

function formatAmount(val: number): string {
  if (val >= 10000) return (val / 10000).toFixed(1) + '万'
  return val.toFixed(0)
}

async function loadData() {
  try {
    const [overviewRes, salesRes, pieRes, visitorRes, spotRes]: any[] = await Promise.all([
      getOverview(),
      getSalesTrend(),
      getTicketTypeSales(),
      getVisitorTrend(),
      getScenicSpotComparison()
    ])

    overview.value = overviewRes.data || {}
    initSalesChart(salesRes.data || [])
    initPieChart(pieRes.data || [])
    initVisitorChart(visitorRes.data || [])
    initSpotChart(spotRes.data || [])
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

function initSalesChart(data: any[]) {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)
  salesChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 30, right: 20, bottom: 30, left: 60 },
    xAxis: { type: 'category', data: data.map((d: any) => d.date), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: data.map((d: any) => d.paid_amount || d.total_amount || 0),
        lineStyle: { color: '#2563EB', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.02)' }
          ])
        },
        itemStyle: { color: '#2563EB' }
      }
    ]
  })
}

function initPieChart(data: any[]) {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '55%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { fontSize: 11 },
      data: data.map((d: any) => ({ name: d.ticket_name, value: d.total_amount || 0 })),
      color: ['#2563EB', '#059669', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#06B6D4']
    }]
  })
}

function initVisitorChart(data: any[]) {
  if (!visitorChartRef.value) return
  visitorChart = echarts.init(visitorChartRef.value)
  visitorChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 30, right: 20, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: data.map((d: any) => d.date), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    series: [{
      name: '游客数',
      type: 'bar',
      data: data.map((d: any) => d.visitor_count || 0),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#059669' },
          { offset: 1, color: '#10B981' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

function initSpotChart(data: any[]) {
  if (!spotChartRef.value) return
  spotChart = echarts.init(spotChartRef.value)
  spotChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 30, right: 20, bottom: 40, left: 80 },
    xAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    yAxis: { type: 'category', data: data.map((d: any) => d.scenic_name), axisLabel: { fontSize: 11 } },
    series: [{
      name: '销售额',
      type: 'bar',
      data: data.map((d: any) => d.total_amount || 0),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#F59E0B' },
          { offset: 1, color: '#FBBF24' }
        ]),
        borderRadius: [0, 4, 4, 0]
      }
    }]
  })
}

function handleResize() {
  salesChart?.resize()
  pieChart?.resize()
  visitorChart?.resize()
  spotChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  pieChart?.dispose()
  visitorChart?.dispose()
  spotChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    .stat-card {
      display: flex;
      align-items: center;
      gap: var(--spacing-4);
      animation: cardStagger var(--duration-slow) var(--ease-out) both;
    }
  }
}

@keyframes cardStagger {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-gray-500);
  margin-bottom: 2px;
}

.stat-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-gray-900);
  font-family: var(--font-body);
}

.chart-card {
  .card-title {
    font-family: var(--font-heading);
    font-size: var(--text-base);
    font-weight: 600;
    color: var(--color-gray-800);
    margin-bottom: var(--spacing-4);
  }
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
