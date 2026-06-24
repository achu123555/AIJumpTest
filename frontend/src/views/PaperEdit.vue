<template>
  <section class="paper-edit-page">
    <div class="page-head">
      <el-button text :icon="Back" @click="goBack">返回</el-button>
      <span class="divider"></span>
      <h2>{{ isEdit ? '编辑试卷' : '创建新试卷' }}</h2>
    </div>

    <el-tabs v-model="activeTab" class="paper-tabs">
      <el-tab-pane label="手动组卷" name="manual">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="base-form">
          <el-form-item label="试卷名称" prop="name">
            <el-input v-model.trim="form.name" maxlength="80" show-word-limit placeholder="请输入试卷名称" />
          </el-form-item>
          <el-form-item label="考试时长(分钟)" prop="duration">
            <el-input-number v-model="form.duration" :min="1" :max="600" controls-position="right" />
          </el-form-item>
          <el-form-item label="试卷描述">
            <el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="300" show-word-limit placeholder="请输入试卷描述" />
          </el-form-item>
        </el-form>

        <div class="select-title">选择题目</div>

        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-input v-model.trim="questionQuery.keyword" clearable placeholder="搜索题目内容" @keyup.enter="handleQuestionSearch" />
            <el-select v-model="questionQuery.type" clearable placeholder="请选择题目类型" @change="handleTypeChange">
              <el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tree-select
              v-model="questionQuery.categoryId"
              :data="categoryTree"
              :props="treeProps"
              node-key="id"
              check-strictly
              filterable
              clearable
              placeholder="请选择题目分类"
            />
            <el-select v-model="questionQuery.difficulty" clearable placeholder="题目难度">
              <el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" :icon="Search" :loading="questionLoading" @click="handleQuestionSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuestionQuery">重置</el-button>
          </div>
        </el-card>

        <el-table
          v-loading="questionLoading || detailLoading"
          :data="questionList"
          border
          stripe
          height="520"
          row-key="id"
          empty-text="暂无题目数据"
          class="question-table"
        >
          <el-table-column width="56" align="center">
            <template #header>
              <el-checkbox
                :model-value="visibleRowsChecked"
                :indeterminate="visibleRowsIndeterminate"
                @change="toggleVisibleRows"
              />
            </template>
            <template #default="{ row }">
              <el-checkbox :model-value="isSelected(row.id)" @change="checked => toggleQuestion(row, checked)" />
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="90" align="center" />
          <el-table-column label="题干" min-width="460" show-overflow-tooltip>
            <template #default="{ row }">{{ row.title }}</template>
          </el-table-column>
          <el-table-column label="题目类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getTypeTag(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="题目难度" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="getDifficultyTag(row.difficulty)">{{ getDifficultyLabel(row.difficulty) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="所属分类" width="150" align="center">
            <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
          </el-table-column>
          <el-table-column label="分数" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-input-number
                v-model="scoreMap[row.id]"
                :min="0"
                :max="100"
                :precision="1"
                :step="1"
                size="small"
                controls-position="right"
                @change="handleScoreChange(row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="summary-bar">
          <div>已选题目：<strong>{{ selectedQuestionCount }}</strong> 题</div>
          <div>试卷总分：<strong>{{ totalScore }}</strong> 分</div>
          <el-button type="primary" :loading="submitLoading" @click="submitPaper">
            {{ isEdit ? '更新试卷' : '创建试卷' }}
          </el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="AI智能组卷" name="ai">
        <el-alert
          title="当前后端 /api/papers/intelligent 接口还没有完整实现"
          type="warning"
          show-icon
          :closable="false"
        >
          <template #default>
            建议先使用“手动组卷”。后端 intelligentCreatePaper 方法完成后，这里可以按题型、分类、数量、分值配置规则并调用智能组卷接口。
          </template>
        </el-alert>

        <el-empty description="智能组卷规则配置区域预留" />
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Refresh, Search } from '@element-plus/icons-vue'
import { getCategoryTree } from '../api/category'
import { getQuestionPage } from '../api/question'
import { createPaper, getPaperDetail, updatePaper } from '../api/paper'
import { DIFFICULTY_OPTIONS, QUESTION_TYPES, getDifficultyLabel, getDifficultyTag, getTypeLabel, getTypeTag, inferQuestionTypeByCategoryName } from '../utils/questionMeta'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const activeTab = ref('manual')
const detailLoading = ref(false)
const questionLoading = ref(false)
const submitLoading = ref(false)
const categoryTree = ref([])
const flatCategories = ref([])
const questionList = ref([])

const form = reactive({
  name: '',
  duration: 60,
  description: ''
})

const questionQuery = reactive({
  keyword: '',
  type: '',
  categoryId: '',
  difficulty: ''
})

// selectedQuestionMap：保存已选题目的完整行数据，切换筛选条件后不会丢失。
const selectedQuestionMap = reactive({})

// scoreMap：key 为 questionId，value 为该题在当前试卷中的实际分值。
const scoreMap = reactive({})

const treeProps = { label: 'name', children: 'children' }
const isEdit = computed(() => Boolean(route.params.id))
const paperId = computed(() => route.params.id)

const rules = {
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'change' }]
}

const categoryNameMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item.name])))
const selectedQuestionIds = computed(() => Object.keys(selectedQuestionMap))
const selectedQuestionCount = computed(() => selectedQuestionIds.value.length)
const totalScore = computed(() => {
  return selectedQuestionIds.value
    .reduce((sum, id) => sum + Number(scoreMap[id] || 0), 0)
    .toFixed(1)
    .replace(/\.0$/, '')
})

const visibleRowsChecked = computed(() => {
  return questionList.value.length > 0 && questionList.value.every(row => isSelected(row.id))
})

const visibleRowsIndeterminate = computed(() => {
  const checkedCount = questionList.value.filter(row => isSelected(row.id)).length
  return checkedCount > 0 && checkedCount < questionList.value.length
})

function formatId(id) {
  return String(id)
}

function isSelected(id) {
  return Boolean(selectedQuestionMap[formatId(id)])
}

function flattenCategoryTree(list = []) {
  const result = []
  const walk = nodes => {
    nodes.forEach(node => {
      result.push(node)
      if (Array.isArray(node.children) && node.children.length) walk(node.children)
    })
  }
  walk(list)
  return result
}

function getCategoryName(categoryId) {
  if (!categoryId) return '未分类'
  return categoryNameMap.value.get(Number(categoryId)) || `分类 ${categoryId}`
}

function normalizeScore(value, fallback = 5) {
  const number = Number(value)
  if (!Number.isFinite(number) || number < 0) return fallback
  return number
}

function goBack() {
  router.push('/admin/paper-manage')
}

async function loadCategoryTree() {
  try {
    const list = await getCategoryTree()
    categoryTree.value = Array.isArray(list) ? list : []
    flatCategories.value = flattenCategoryTree(categoryTree.value)
  } catch (error) {
    console.error('加载分类失败：', error)
    ElMessage.error(error.message || '加载分类失败')
  }
}

async function loadQuestions() {
  questionLoading.value = true
  try {
    const result = await getQuestionPage({
      current: 1,
      size: 200,
      keyword: questionQuery.keyword,
      type: questionQuery.type,
      categoryId: questionQuery.categoryId,
      difficulty: questionQuery.difficulty
    })

    const records = Array.isArray(result?.records) ? result.records : []
    questionList.value = records

    // 给当前页题目初始化分值，默认使用题库默认分 score。
    records.forEach(row => {
      const key = formatId(row.id)
      if (scoreMap[key] === undefined || scoreMap[key] === null) {
        scoreMap[key] = normalizeScore(row.realScore ?? row.score, 5)
      }
    })
  } catch (error) {
    console.error('加载题目失败：', error)
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    questionLoading.value = false
  }
}

function handleQuestionSearch() {
  loadQuestions()
}

function resetQuestionQuery() {
  questionQuery.keyword = ''
  questionQuery.type = ''
  questionQuery.categoryId = ''
  questionQuery.difficulty = ''
  loadQuestions()
}

function handleTypeChange() {
  questionQuery.categoryId = ''
  loadQuestions()
}

function toggleQuestion(row, checked) {
  const key = formatId(row.id)

  if (checked) {
    selectedQuestionMap[key] = row
    if (scoreMap[key] === undefined || scoreMap[key] === null) {
      scoreMap[key] = normalizeScore(row.realScore ?? row.score, 5)
    }
    return
  }

  delete selectedQuestionMap[key]
}

function toggleVisibleRows(checked) {
  questionList.value.forEach(row => toggleQuestion(row, checked))
}

function handleScoreChange(row) {
  const key = formatId(row.id)
  scoreMap[key] = normalizeScore(scoreMap[key], row.score || 5)
}

async function loadPaperDetail() {
  if (!isEdit.value) return

  detailLoading.value = true
  try {
    const detail = await getPaperDetail(paperId.value)
    form.name = detail?.name || ''
    form.description = detail?.description || ''
    form.duration = detail?.duration || 60

    Object.keys(selectedQuestionMap).forEach(key => delete selectedQuestionMap[key])
    Object.keys(scoreMap).forEach(key => delete scoreMap[key])

    const questions = Array.isArray(detail?.questions) ? detail.questions : []
    questions.forEach(question => {
      const key = formatId(question.id)
      selectedQuestionMap[key] = question
      scoreMap[key] = normalizeScore(question.realScore ?? question.score, 5)
    })
  } catch (error) {
    console.error('加载试卷详情失败：', error)
    ElMessage.error(error.message || '加载试卷详情失败')
  } finally {
    detailLoading.value = false
  }
}

function buildQuestionScoreMap() {
  const questions = {}
  selectedQuestionIds.value.forEach(id => {
    questions[id] = normalizeScore(scoreMap[id], 0)
  })
  return questions
}

async function submitPaper() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!selectedQuestionCount.value) {
    ElMessage.warning('请至少选择一道题目')
    return
  }

  const payload = {
    name: form.name,
    description: form.description,
    duration: form.duration,
    questions: buildQuestionScoreMap()
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updatePaper(paperId.value, payload)
      ElMessage.success('试卷更新成功')
    } else {
      await createPaper(payload)
      ElMessage.success('试卷创建成功')
    }
    router.push('/admin/paper-manage')
  } catch (error) {
    console.error('保存试卷失败：', error)
    ElMessage.error(error.message || '保存试卷失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadCategoryTree()
  await loadQuestions()
  await loadPaperDetail()
  await nextTick()
})
</script>

<style scoped>
.paper-edit-page {
  max-width: 1380px;
  margin: 0 auto;
  color: #1f2329;
}

.page-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}

.page-head h2 {
  margin: 0;
  font-size: 22px;
}

.divider {
  width: 1px;
  height: 18px;
  background: #dcdfe6;
}

.paper-tabs {
  background: transparent;
}

.base-form {
  width: 640px;
  margin-top: 18px;
}

.base-form :deep(.el-input),
.base-form :deep(.el-textarea) {
  width: 520px;
}

.select-title {
  margin: 20px 0 10px;
  font-size: 20px;
  font-weight: 700;
}

.filter-card {
  margin-bottom: 16px;
  background: #f8fbff;
  border-color: #dbe8f7;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-row .el-input {
  width: 260px;
}

.filter-row .el-select,
.filter-row .el-tree-select {
  width: 220px;
}

.question-table {
  background: #fff;
}

.summary-bar {
  margin-top: 18px;
  padding: 20px 28px;
  border: 1px solid #dcdfe6;
  background: #f5fbff;
  border-radius: 8px;
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  align-items: center;
  gap: 24px;
  font-size: 18px;
}

.summary-bar strong {
  color: #1677ff;
  font-size: 22px;
  margin: 0 4px;
}

@media (max-width: 900px) {
  .base-form,
  .base-form :deep(.el-input),
  .base-form :deep(.el-textarea) {
    width: 100%;
  }

  .summary-bar {
    grid-template-columns: 1fr;
  }
}
</style>
