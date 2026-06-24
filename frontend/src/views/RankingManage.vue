<template>
  <section class="ranking-page">
    <el-card shadow="always" class="filter-card">
      <div class="filter-row">
        <el-select v-model="query.paperId" clearable filterable placeholder="选择试卷查看排行榜">
          <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
        </el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="loadRanking">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="always">
      <el-table v-loading="loading" :data="ranking" border stripe>
        <el-table-column prop="rank" label="排名" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.rank <= 3" type="warning">第 {{ row.rank }} 名</el-tag>
            <span v-else>第 {{ row.rank }} 名</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="考生姓名" width="150" />
        <el-table-column prop="paperName" label="试卷名称" min-width="260" show-overflow-tooltip />
        <el-table-column label="得分" width="140" align="center">
          <template #default="{ row }">{{ row.score ?? 0 }} / {{ row.totalScore ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="用时" width="120" align="center">
          <template #default="{ row }">{{ formatDuration(row.durationSeconds) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="交卷时间" width="180" />
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getPaperList } from '../api/paper'
import { getExamRanking } from '../api/exam'

const loading = ref(false)
const papers = ref([])
const ranking = ref([])
const query = reactive({ paperId: '' })

function formatDuration(seconds) {
  const value = Number(seconds || 0)
  const minute = Math.floor(value / 60)
  const second = value % 60
  return `${minute}分${second}秒`
}

async function loadPapers() {
  try {
    const list = await getPaperList()
    papers.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error(error)
  }
}

async function loadRanking() {
  loading.value = true
  try {
    const list = await getExamRanking({ paperId: query.paperId })
    ranking.value = Array.isArray(list) ? list : []
  } catch (error) {
    ElMessage.error(error.message || '加载排行榜失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.paperId = ''
  loadRanking()
}

onMounted(() => {
  loadPapers()
  loadRanking()
})
</script>

<style scoped>
.ranking-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-row .el-select {
  width: 320px;
}
</style>
