<template>
  <section class="record-page">
    <el-card shadow="always" class="filter-card">
      <div class="filter-row">
        <el-input v-model.trim="query.studentName" clearable placeholder="考生姓名" @keyup.enter="loadRecords" />
        <el-select v-model="query.paperId" clearable filterable placeholder="选择试卷">
          <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
        </el-select>
        <el-select v-model="query.status" clearable placeholder="考试状态">
          <el-option label="进行中" value="进行中" />
          <el-option label="已批阅" value="已批阅" />
        </el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="loadRecords">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="always" class="table-card">
      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="考生姓名" width="130" />
        <el-table-column prop="paperName" label="试卷名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="得分" width="120" align="center">
          <template #default="{ row }">{{ row.score ?? 0 }} / {{ row.totalScore ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已批阅' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="windowSwitches" label="切屏次数" width="110" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="130" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" title="考试详情" size="70%">
      <div v-loading="detailLoading">
        <el-alert v-if="detail.appraisal" type="info" :closable="false" show-icon class="mb16">
          <template #title>AI 综合评语</template>
          <div>{{ detail.appraisal }}</div>
        </el-alert>

        <el-descriptions border :column="3" class="mb16">
          <el-descriptions-item label="考生">{{ detail.studentName }}</el-descriptions-item>
          <el-descriptions-item label="试卷">{{ detail.detailPaper?.name }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ detail.score || 0 }} / {{ detail.detailPaper?.totalScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="切屏">{{ detail.windowSwitches || 0 }} 次</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ detail.endTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detailRows" border stripe>
          <el-table-column prop="index" label="题号" width="80" align="center" />
          <el-table-column prop="title" label="题目" min-width="260" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="100" align="center" />
          <el-table-column prop="userAnswer" label="考生答案" min-width="160" show-overflow-tooltip />
          <el-table-column prop="scoreText" label="得分" width="100" align="center" />
          <el-table-column prop="aiCorrection" label="批阅意见" min-width="260" show-overflow-tooltip />
        </el-table>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getPaperList } from '../api/paper'
import { getExamDetail, getExamRecords } from '../api/exam'

const loading = ref(false)
const detailLoading = ref(false)
const drawerVisible = ref(false)
const records = ref([])
const papers = ref([])
const detail = reactive({})
const query = reactive({ studentName: '', paperId: '', status: '' })

const detailRows = computed(() => {
  const answers = Array.isArray(detail.answerRecords) ? detail.answerRecords : []
  const questions = Array.isArray(detail.detailPaper?.questions) ? detail.detailPaper.questions : []
  const questionMap = new Map(questions.map((q, index) => [Number(q.id), { ...q, index: index + 1 }]))
  return answers.map(record => {
    const question = questionMap.get(Number(record.questionId)) || {}
    const maxScore = question.realScore ?? question.score ?? 0
    return {
      ...record,
      index: question.index || '-',
      title: question.title || `题目 ${record.questionId}`,
      type: question.type || '-',
      scoreText: `${record.score || 0}/${maxScore}`
    }
  })
})

async function loadPapers() {
  try {
    const list = await getPaperList()
    papers.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error(error)
  }
}

async function loadRecords() {
  loading.value = true
  try {
    const list = await getExamRecords(query)
    records.value = Array.isArray(list) ? list : []
  } catch (error) {
    ElMessage.error(error.message || '加载考试记录失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { studentName: '', paperId: '', status: '' })
  loadRecords()
}

async function openDetail(id) {
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const result = await getExamDetail(id)
    Object.keys(detail).forEach(key => delete detail[key])
    Object.assign(detail, result || {})
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadPapers()
  loadRecords()
})
</script>

<style scoped>
.record-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-row .el-input,
.filter-row .el-select {
  width: 220px;
}

.mb16 {
  margin-bottom: 16px;
}
</style>
