<template>
  <main class="student-question-page">
    <header class="student-topbar">
      <button class="brand" type="button" @click="router.push('/home')">
        <span class="brand-mark">Test</span>
        <span class="brand-text">AIJumpTest</span>
      </button>
      <nav class="top-actions">
        <el-button plain @click="router.push('/home')">返回首页</el-button>
        <el-button type="primary" @click="router.push('/admin/question-manage')">管理后台</el-button>
      </nav>
    </header>

    <section class="question-board">
      <div class="board-title">
        <h1>题库练习</h1>
        <p>按题型或知识点慢慢刷，点开题目可以看答案和解析。</p>
      </div>

      <el-card class="filter-card" shadow="always">
        <el-input
          v-model.trim="query.keyword"
          clearable
          placeholder="搜索题目内容"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="query.type" clearable placeholder="题目类型" @change="handleTypeFilterChange">
          <el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.difficulty" clearable placeholder="难度等级">
          <el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-card>

      <div class="content-grid">
        <section class="question-list">
          <el-skeleton v-if="loading" :rows="6" animated />

          <template v-else-if="questionList.length">
            <article v-for="item in questionList" :key="item.id" class="question-card" @click="goDetail(item.id)">
              <div class="card-tags">
                <el-tag size="small" :type="getTypeTag(item.type)" effect="light">{{ getTypeLabel(item.type) }}</el-tag>
                <el-tag size="small" :type="getDifficultyTag(item.difficulty)" effect="light">{{ getDifficultyLabel(item.difficulty) }}</el-tag>
                <el-tag size="small" type="info" effect="plain">{{ getCategoryName(item.categoryId) }}</el-tag>
              </div>
              <h3>{{ item.title }}</h3>
              <div class="card-meta">
                <span>👁 点开练习</span>
                <span>✓ {{ item.score || 0 }} 分</span>
              </div>
            </article>
          </template>

          <el-empty v-else description="暂无题目" />

          <div class="pager-row">
            <el-pagination
              v-model:current-page="page.current"
              v-model:page-size="page.size"
              layout="total, prev, pager, next, jumper"
              :total="page.total"
              @current-change="loadQuestions"
            />
          </div>
        </section>

        <aside class="category-panel">
          <div class="panel-head">
            <h3>题目分类</h3>
            <el-button size="small" plain @click="resetCategoryFilter">全部</el-button>
          </div>

          <el-tree
            v-loading="categoryLoading"
            ref="categoryTreeRef"
            :data="categoryTree"
            node-key="id"
            :props="treeProps"
            default-expand-all
            highlight-current
            :current-node-key="currentCategoryKey"
            @node-click="handleCategoryClick"
          >
            <template #default="{ data }">
              <span class="tree-node">
                <span>{{ data.name }}</span>
                <em>{{ data.questionCount || 0 }}</em>
              </span>
            </template>
          </el-tree>
        </aside>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getCategoryTree } from '../api/category'
import { getQuestionPage } from '../api/question'
import {
  DIFFICULTY_OPTIONS,
  QUESTION_TYPES,
  getDifficultyLabel,
  getDifficultyTag,
  getTypeLabel,
  getTypeTag,
  inferQuestionTypeByCategoryName
} from '../utils/questionMeta'

const router = useRouter()
const loading = ref(false)
const categoryLoading = ref(false)
const questionList = ref([])
const categoryTree = ref([])
const flatCategories = ref([])
const categoryTreeRef = ref(null)
const currentCategoryKey = ref(null)

const query = reactive({
  keyword: '',
  type: '',
  difficulty: '',
  categoryId: ''
})

const page = reactive({
  current: 1,
  size: 9,
  total: 0
})

const treeProps = {
  label: 'name',
  children: 'children'
}

const categoryNameMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item.name])))
const categoryByIdMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item])))

function flattenCategoryTree(list = []) {
  const result = []
  const walk = nodes => {
    nodes.forEach(node => {
      result.push(node)
      if (Array.isArray(node.children) && node.children.length) {
        walk(node.children)
      }
    })
  }
  walk(list)
  return result
}

function getCategoryName(categoryId) {
  if (!categoryId) return '未分类'
  return categoryNameMap.value.get(Number(categoryId)) || `分类 ${categoryId}`
}

async function loadCategoryTree() {
  categoryLoading.value = true
  try {
    const list = await getCategoryTree()
    categoryTree.value = Array.isArray(list) ? list : []
    flatCategories.value = flattenCategoryTree(categoryTree.value)
  } catch (error) {
    console.error('分类加载失败：', error)
    ElMessage.warning('分类加载失败')
  } finally {
    categoryLoading.value = false
  }
}

async function loadQuestions() {
  loading.value = true
  try {
    const result = await getQuestionPage({
      current: page.current,
      size: page.size,
      keyword: query.keyword,
      type: query.type,
      difficulty: query.difficulty,
      categoryId: query.categoryId
    })

    questionList.value = Array.isArray(result?.records) ? result.records : []
    page.total = Number(result?.total || 0)
    page.current = Number(result?.current || page.current)
    page.size = Number(result?.size || page.size)
  } catch (error) {
    console.error('题目加载失败：', error)
    ElMessage.error(error.message || '题目加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.current = 1
  loadQuestions()
}

function handleTypeFilterChange() {
  // 顶部手动切换题型时，清掉右侧分类树的二级分类，否则可能出现“类型和分类不属于同一主干”的空结果。
  query.categoryId = ''
  currentCategoryKey.value = null
  nextTick(() => categoryTreeRef.value?.setCurrentKey(null))
  page.current = 1
  loadQuestions()
}

function resetQuery() {
  query.keyword = ''
  query.type = ''
  query.difficulty = ''
  query.categoryId = ''
  currentCategoryKey.value = null
  nextTick(() => categoryTreeRef.value?.setCurrentKey(null))
  page.current = 1
  loadQuestions()
}

function handleCategoryClick(category) {
  currentCategoryKey.value = category.id
  page.current = 1

  const parentId = Number(category.parentId ?? category.parent_id ?? 0)

  // 点击主干分类：选择题 / 判断题 / 简答题
  // 后端问题表本身有 type 字段，所以主干筛选直接走 type，不传 categoryId。
  if (parentId === 0) {
    query.categoryId = ''
    query.type = inferQuestionTypeByCategoryName(category.name)
    loadQuestions()
    return
  }

  // 点击分支分类：Java基础 / Spring框架 / MySQL数据库 ...
  // 后端按 categoryId 精确查；同时补上父级 type，避免数据更稳。
  query.categoryId = category.id
  const parent = categoryByIdMap.value.get(parentId)
  const parentType = inferQuestionTypeByCategoryName(parent?.name)
  if (parentType) {
    query.type = parentType
  }

  loadQuestions()
}

function resetCategoryFilter() {
  query.type = ''
  query.categoryId = ''
  currentCategoryKey.value = null
  nextTick(() => categoryTreeRef.value?.setCurrentKey(null))
  page.current = 1
  loadQuestions()
}

function goDetail(id) {
  router.push(`/question/${id}`)
}

onMounted(async () => {
  await loadCategoryTree()
  await loadQuestions()
})
</script>

<style scoped>
.student-question-page{min-height:100vh;background:radial-gradient(circle at 8% 16%,rgba(64,217,188,.42),transparent 28%),radial-gradient(circle at 86% 86%,rgba(247,161,196,.45),transparent 34%),linear-gradient(135deg,#dff9f2 0%,#dce8f3 48%,#f5dceb 100%)}.student-topbar{height:74px;padding:0 42px;display:flex;align-items:center;justify-content:space-between;background:rgba(255,255,255,.86);border-bottom:1px solid rgba(255,255,255,.9);backdrop-filter:blur(16px);position:sticky;top:0;z-index:10}.brand{display:inline-flex;align-items:center;gap:12px;border:0;background:transparent;cursor:pointer}.brand-mark{width:38px;height:38px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;color:#fff;font-weight:800;background:linear-gradient(135deg,#246bfe,#3b2ee8)}.brand-text{font-size:24px;font-weight:800;color:#1f2a64}.top-actions{display:flex;gap:12px}.question-board{max-width:1220px;margin:0 auto;padding:42px 36px 80px}.board-title{color:#fff;text-shadow:0 2px 12px rgba(30,41,59,.25);margin-bottom:22px}.board-title h1{margin:0 0 8px;font-size:36px;font-weight:900}.board-title p{margin:0;font-size:15px}.filter-card{border:0;border-radius:18px;margin-bottom:22px}.filter-card :deep(.el-card__body){display:grid;grid-template-columns:minmax(240px,1fr) 180px 180px 88px 88px;gap:14px;align-items:center}.content-grid{display:grid;grid-template-columns:minmax(0,1fr) 300px;gap:22px}.question-list{min-height:520px}.question-card{padding:22px;margin-bottom:16px;border-radius:18px;background:rgba(255,255,255,.94);box-shadow:0 16px 32px rgba(20,33,61,.12);cursor:pointer;transition:.2s}.question-card:hover{transform:translateY(-4px);box-shadow:0 20px 42px rgba(20,33,61,.18)}.card-tags{display:flex;gap:8px;margin-bottom:12px}.question-card h3{margin:0 0 16px;font-size:17px;line-height:1.7;color:#111827}.card-meta{display:flex;justify-content:space-between;color:#6b7280;font-size:13px}.category-panel{padding:20px;border-radius:18px;background:rgba(255,255,255,.94);box-shadow:0 16px 32px rgba(20,33,61,.12);align-self:start}.panel-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px}.panel-head h3{margin:0;font-size:18px;font-weight:900}.tree-node{width:100%;display:flex;justify-content:space-between;gap:12px;padding-right:8px}.tree-node em{color:#6b7280;font-style:normal}.pager-row{display:flex;justify-content:center;margin-top:24px}@media(max-width:1180px){.content-grid{grid-template-columns:1fr}.filter-card :deep(.el-card__body){grid-template-columns:1fr 1fr}}
</style>
