<template>
  <section class="result-page">
    <el-card v-loading="loading" class="result-card" shadow="always">
      <div class="result-head">
        <h1>考试结果</h1>
        <el-tag size="large" type="success">{{ detail.status || '已批阅' }}</el-tag>
      </div>

      <div class="score-box">
        <div class="score-number">{{ detail.score ?? 0 }}</div>
        <div class="score-total">/ {{ paper.totalScore ?? 0 }} 分</div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="考生姓名">{{ detail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="试卷名称">{{ paper.name }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="切屏次数">{{ detail.windowSwitches || 0 }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ paper.duration || 0 }} 分钟</el-descriptions-item>
      </el-descriptions>

      <el-alert class="appraisal" type="info" show-icon :closable="false">
        <template #title>AI 综合评语</template>
        <div>{{ detail.appraisal || '暂无评语' }}</div>
      </el-alert>

      <el-table :data="answerRows" border stripe class="answer-table">
        <el-table-column prop="index" label="题号" width="80" align="center" />
        <el-table-column prop="title" label="题目" min-width="260" show-overflow-tooltip />
        <el-table-column prop="userAnswer" label="你的答案" min-width="160" show-overflow-tooltip />
        <el-table-column prop="scoreText" label="得分" width="100" align="center" />
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect ? 'success' : 'danger'">{{ row.isCorrect ? '正确' : '未满分' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiCorrection" label="批阅意见" min-width="260" show-overflow-tooltip />
      </el-table>

      <div class="actions">
        <el-button @click="router.push('/exam/list')">返回试卷列表</el-button>
        <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamDetail } from '../api/exam'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = reactive({})
const paper = reactive({})

const answerRows = computed(() => {
  const answers = Array.isArray(detail.answerRecords) ? detail.answerRecords : []
  const questions = Array.isArray(paper.questions) ? paper.questions : []
  const questionMap = new Map(questions.map((q, index) => [Number(q.id), { ...q, index: index + 1 }]))

  return answers.map(record => {
    const question = questionMap.get(Number(record.questionId)) || {}
    const maxScore = question.realScore ?? question.score ?? 0
    return {
      ...record,
      index: question.index || '-',
      title: question.title || `题目 ${record.questionId}`,
      scoreText: `${record.score || 0}/${maxScore}`,
      isCorrect: Number(record.isCorrect) === 1
    }
  })
})

async function loadDetail() {
  loading.value = true
  try {
    const result = await getExamDetail(route.params.id)
    Object.assign(detail, result || {})
    Object.assign(paper, result?.detailPaper || {})
  } catch (error) {
    ElMessage.error(error.message || '加载考试结果失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.result-page {
  min-height: 100vh;
  background: #e9e9e9;
  padding: 36px 0 80px;
}

.result-card {
  width: min(1000px, calc(100vw - 48px));
  margin: 0 auto;
}

.result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-head h1 {
  margin: 0;
}

.score-box {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  margin: 22px 0;
}

.score-number {
  font-size: 64px;
  font-weight: 800;
  color: #1677ff;
}

.score-total {
  font-size: 24px;
  margin-bottom: 12px;
  color: #606266;
}

.appraisal {
  margin: 22px 0;
}

.answer-table {
  margin-top: 20px;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
