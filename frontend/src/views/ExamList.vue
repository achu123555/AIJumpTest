<template>
  <section class="exam-list-page">
    <div class="page-head">
      <el-button text :icon="Back" @click="router.push('/home')">返回</el-button>
      <span class="divider"></span>
      <h2>选择试卷开始考试</h2>
    </div>

    <div class="search-box">
      <el-input
        v-model.trim="query.name"
        placeholder="按试卷名称搜索"
        clearable
        @keyup.enter="loadPapers"
      >
        <template #append>
          <el-button :icon="Search" :loading="loading" @click="loadPapers" />
        </template>
      </el-input>
    </div>

    <div v-loading="loading" class="paper-list">
      <el-card
        v-for="paper in papers"
        :key="paper.id"
        class="paper-card"
        shadow="hover"
      >
        <div class="paper-main">
          <div class="paper-info">
            <h3>{{ paper.name }}</h3>
            <p>{{ paper.description || '暂无描述' }}</p>
            <div class="paper-meta">
              <span><el-icon><Document /></el-icon> 题目数量：{{ paper.questionCount || 0 }} 道</span>
              <span><el-icon><Trophy /></el-icon> 总分：{{ paper.totalScore || 0 }} 分</span>
              <span><el-icon><Timer /></el-icon> 考试时长：{{ paper.duration || 0 }} 分钟</span>
            </div>
          </div>

          <el-button type="primary" @click="goStart(paper.id)">开始考试</el-button>
        </div>
      </el-card>

      <el-empty v-if="!loading && papers.length === 0" description="暂无可参加的试卷" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Document, Search, Timer, Trophy } from '@element-plus/icons-vue'
import { getExamPaperList } from '../api/exam'

const router = useRouter()
const loading = ref(false)
const papers = ref([])
const query = reactive({ name: '' })

async function loadPapers() {
  loading.value = true
  try {
    const list = await getExamPaperList({ name: query.name })
    papers.value = Array.isArray(list) ? list : []
  } catch (error) {
    ElMessage.error(error.message || '加载试卷失败')
  } finally {
    loading.value = false
  }
}

function goStart(paperId) {
  router.push(`/exam/start/${paperId}`)
}

onMounted(loadPapers)
</script>

<style scoped>
.exam-list-page {
  min-height: 100vh;
  background: #e9e9e9;
  padding: 42px 0 80px;
}

.page-head,
.search-box,
.paper-list {
  width: min(1080px, calc(100vw - 48px));
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0;
  font-size: 18px;
}

.divider {
  width: 1px;
  height: 18px;
  background: #c9cdd4;
}

.search-box {
  display: flex;
  justify-content: center;
  margin-bottom: 22px;
}

.search-box .el-input {
  width: 540px;
}

.paper-list {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.paper-card {
  border-radius: 10px;
}

.paper-card:hover {
  border-color: #409eff;
}

.paper-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.paper-info h3 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
}

.paper-info p {
  margin: 0 0 14px;
  color: #303133;
}

.paper-meta {
  display: flex;
  gap: 22px;
  color: #606266;
  font-size: 14px;
}

.paper-meta span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
</style>
