<template>
  <section class="ranking-home">
    <header class="top-bar">
      <button class="brand" type="button" @click="router.push('/home')">
        <span class="brand-mark">Test</span>
        <span class="brand-text">AIJumpTest</span>
      </button>
      <nav class="top-actions">
        <el-button @click="router.push('/home')">首页</el-button>
        <el-button type="primary" @click="router.push('/exam/list')">开始考试</el-button>
      </nav>
    </header>

    <main class="page-inner">
      <section class="hero-card">
        <div>
          <p class="eyebrow">Exam Ranking</p>
          <h1>考试排行榜</h1>
          <p class="desc">按试卷查看已批阅成绩，排名优先按得分降序，同分按交卷时间排序。</p>
        </div>
        <div class="rank-podium">
          <div v-for="item in topThree" :key="item.examRecordId" class="podium-card" :class="`rank-${item.rank}`">
            <span>TOP {{ item.rank }}</span>
            <strong>{{ item.studentName || '暂无' }}</strong>
            <em>{{ item.score ?? 0 }} 分</em>
          </div>
        </div>
      </section>

      <section class="filter-card">
        <el-select v-model="query.paperId" clearable filterable placeholder="选择试卷" class="paper-select">
          <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
        </el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="loadRanking">查询排行</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </section>

      <section class="ranking-list" v-loading="loading">
        <el-empty v-if="!ranking.length && !loading" description="暂无排行数据" />
        <article v-for="item in ranking" v-else :key="item.examRecordId" class="rank-row" :class="{ champion: item.rank === 1 }">
          <div class="rank-number">{{ item.rank }}</div>
          <div class="student-info">
            <h3>{{ item.studentName }}</h3>
            <p>{{ item.paperName }}</p>
          </div>
          <div class="score-box">
            <strong>{{ item.score ?? 0 }}</strong>
            <span>/ {{ item.totalScore ?? 0 }} 分</span>
          </div>
          <div class="meta-box">
            <span>用时：{{ formatDuration(item.durationSeconds) }}</span>
            <span>交卷：{{ item.endTime || '-' }}</span>
          </div>
        </article>
      </section>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getExamPaperList, getExamRanking } from '../api/exam'

const router = useRouter()
const loading = ref(false)
const papers = ref([])
const ranking = ref([])
const query = reactive({ paperId: '' })

const topThree = computed(() => ranking.value.slice(0, 3))

function formatDuration(seconds) {
  const value = Number(seconds || 0)
  const minute = Math.floor(value / 60)
  const second = value % 60
  return `${minute}分${String(second).padStart(2, '0')}秒`
}

async function loadPapers() {
  try {
    const list = await getExamPaperList()
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
.ranking-home {
  min-height: 100vh;
  background: #f3f6fb;
  color: #0f172a;
}

.top-bar {
  height: 72px;
  padding: 0 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.86);
  border-bottom: 1px solid #e6edf7;
  backdrop-filter: blur(16px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

.brand-text {
  font-size: 22px;
  font-weight: 800;
}

.top-actions { display: flex; gap: 12px; }

.page-inner {
  max-width: 1120px;
  margin: 0 auto;
  padding: 38px 20px 80px;
}

.hero-card {
  min-height: 220px;
  border-radius: 28px;
  padding: 38px;
  display: grid;
  grid-template-columns: 1fr 460px;
  gap: 32px;
  align-items: center;
  color: #fff;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, .92), rgba(37, 99, 235, .86)),
    repeating-linear-gradient(45deg, rgba(255,255,255,.08) 0 2px, transparent 2px 18px);
  box-shadow: 0 24px 70px rgba(37, 99, 235, .24);
}

.eyebrow {
  margin: 0 0 10px;
  text-transform: uppercase;
  letter-spacing: .16em;
  color: #bfdbfe;
}

.hero-card h1 {
  margin: 0 0 14px;
  font-size: 42px;
}

.desc {
  margin: 0;
  line-height: 1.8;
  color: rgba(255,255,255,.82);
}

.rank-podium {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.podium-card {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255,255,255,.14);
  border: 1px solid rgba(255,255,255,.2);
}

.podium-card span,
.podium-card strong,
.podium-card em { display: block; }
.podium-card span { color: #bfdbfe; }
.podium-card strong { margin: 10px 0; font-size: 18px; }
.podium-card em { font-style: normal; color: #fde68a; }

.filter-card {
  margin: 26px 0 18px;
  padding: 18px;
  border-radius: 18px;
  display: flex;
  gap: 12px;
  align-items: center;
  background: #fff;
  box-shadow: 0 12px 34px rgba(15, 23, 42, .08);
}

.paper-select { width: 360px; }

.ranking-list {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-row {
  padding: 18px 22px;
  border-radius: 18px;
  display: grid;
  grid-template-columns: 64px 1fr 150px 260px;
  gap: 18px;
  align-items: center;
  background: #fff;
  border: 1px solid #e5eaf3;
  box-shadow: 0 10px 28px rgba(15, 23, 42, .06);
}

.rank-row.champion {
  border-color: #facc15;
  background: linear-gradient(90deg, #fff7ed, #fff);
}

.rank-number {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

.student-info h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.student-info p,
.meta-box span {
  margin: 0;
  color: #64748b;
}

.score-box strong {
  font-size: 28px;
  color: #2563eb;
}

.score-box span {
  color: #64748b;
}

.meta-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}
</style>
