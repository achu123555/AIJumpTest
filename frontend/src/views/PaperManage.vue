<template>
  <section class="paper-page">
    <el-card class="search-card" shadow="always">
      <div class="search-row">
        <el-input
          v-model.trim="query.name"
          clearable
          :prefix-icon="Search"
          placeholder="请输入试卷名称"
          @keyup.enter="loadPaperList"
        />
        <el-select v-model="query.status" clearable placeholder="选择试卷状态">
          <el-option v-for="item in PAPER_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="loadPaperList">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="danger" :icon="Delete" :disabled="!selectedRows.length" @click="handleBatchDelete">
          批量删除({{ selectedRows.length }})
        </el-button>
        <el-button type="primary" :icon="Plus" @click="goCreate">创建新试卷</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="always">
      <el-table
        v-loading="loading"
        :data="paperList"
        stripe
        border
        empty-text="暂无试卷数据"
        @selection-change="selectedRows = $event"
      >
        <el-table-column type="selection" width="52" />
        <el-table-column prop="id" label="ID" width="90" align="center" />
        <el-table-column prop="name" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="试卷描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="题目数量" width="110" align="center">
          <template #default="{ row }">{{ row.questionCount || 0 }} 道</template>
        </el-table-column>
        <el-table-column label="总分" width="100" align="center">
          <template #default="{ row }">{{ normalizeScore(row.totalScore) }} 分</template>
        </el-table-column>
        <el-table-column label="考试时长" width="120" align="center">
          <template #default="{ row }">{{ row.duration || 0 }} 分钟</template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PUBLISHED'"
              size="small"
              type="warning"
              :icon="CircleClose"
              @click="handleSwitchStatus(row, 'DRAFT')"
            >停止</el-button>
            <el-button
              v-else
              size="small"
              type="success"
              :icon="VideoPlay"
              @click="handleSwitchStatus(row, 'PUBLISHED')"
            >发布</el-button>
            <el-button size="small" type="primary" :icon="Edit" @click="goEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose, Delete, Edit, Plus, Refresh, Search, VideoPlay } from '@element-plus/icons-vue'
import { deletePaper, getPaperList, switchPaperStatus } from '../api/paper'

const router = useRouter()
const loading = ref(false)
const paperList = ref([])
const selectedRows = ref([])
const query = reactive({ name: '', status: '' })

const PAPER_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT', type: 'info' },
  { label: '已发布', value: 'PUBLISHED', type: 'success' },
  { label: '已过期', value: 'EXPIRED', type: 'warning' }
]

function getStatusLabel(status) {
  return PAPER_STATUS_OPTIONS.find(item => item.value === status)?.label || status || '未知'
}

function getStatusType(status) {
  return PAPER_STATUS_OPTIONS.find(item => item.value === status)?.type || 'info'
}

function normalizeScore(score) {
  if (score === undefined || score === null || score === '') return 0
  const number = Number(score)
  return Number.isFinite(number) ? number : score
}

function formatDate(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

async function loadPaperList() {
  loading.value = true
  try {
    const list = await getPaperList({ name: query.name, status: query.status })
    paperList.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载试卷列表失败：', error)
    ElMessage.error(error.message || '加载试卷列表失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.name = ''
  query.status = ''
  loadPaperList()
}

function goCreate() {
  router.push('/admin/paper-create')
}

function goEdit(row) {
  router.push(`/admin/paper-edit/${row.id}`)
}

async function handleSwitchStatus(row, status) {
  const actionText = status === 'PUBLISHED' ? '发布' : '停止发布'
  try {
    await ElMessageBox.confirm(`确定要${actionText}试卷「${row.name}」吗？`, '状态切换确认', {
      type: 'warning'
    })
    await switchPaperStatus(row.id, status)
    ElMessage.success(`${actionText}成功`)
    loadPaperList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('切换试卷状态失败：', error)
    ElMessage.error(error.message || `${actionText}失败`)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除试卷「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
    await deletePaper(row.id)
    ElMessage.success('删除成功')
    loadPaperList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('删除试卷失败：', error)
    ElMessage.error(error.message || '删除失败')
  }
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) return

  try {
    await ElMessageBox.confirm(`确定批量删除选中的 ${selectedRows.value.length} 张试卷吗？`, '批量删除确认', {
      type: 'warning',
      confirmButtonText: '批量删除',
      confirmButtonClass: 'el-button--danger'
    })

    for (const row of selectedRows.value) {
      await deletePaper(row.id)
    }

    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadPaperList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('批量删除试卷失败：', error)
    ElMessage.error(error.message || '批量删除失败')
  }
}

onMounted(loadPaperList)
</script>

<style scoped>
.paper-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.search-card,
.table-card {
  border-radius: 10px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-row .el-input {
  width: 260px;
}

.search-row .el-select {
  width: 160px;
}

.table-card :deep(.el-card__body) {
  padding: 18px 20px;
}
</style>
