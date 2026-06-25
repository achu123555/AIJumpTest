<template>
  <main class="question-detail-page">
    <header class="student-topbar">
      <button class="brand" type="button" @click="router.push('/home')">
        <span class="brand-mark">Test</span>
        <span class="brand-text">AIJumpTest</span>
      </button>
      <nav class="top-actions">
        <el-button plain @click="router.push('/questions')">返回题库</el-button>
        <el-button type="primary" @click="router.push('/admin/question-manage')">管理后台</el-button>
      </nav>
    </header>

    <section class="detail-wrap">
      <el-card v-loading="loading" class="practice-card" shadow="always">
        <template v-if="question">
          <div class="question-head">
            <div class="tags">
              <el-tag :type="getTypeTag(question.type)">{{ getTypeLabel(question.type) }}</el-tag>
              <el-tag :type="getDifficultyTag(question.difficulty)">{{ getDifficultyLabel(question.difficulty) }}</el-tag>
              <el-tag type="warning">{{ question.score || 0 }} 分</el-tag>
            </div>
            <h1>{{ question.title }}</h1>
          </div>

          <div v-if="isChoice" class="answer-area">
            <el-checkbox-group v-if="isMultiple" v-model="selectedAnswers" :disabled="showResult">
              <label
                v-for="(choice, index) in choices"
                :key="choice.id || index"
                class="option-line"
                :class="getOptionClass(index)"
              >
                <el-checkbox :label="optionLabel(index)">
                  <span class="option-label">{{ optionLabel(index) }}.</span>
                  <span>{{ choice.content }}</span>
                </el-checkbox>
              </label>
            </el-checkbox-group>

            <el-radio-group v-else v-model="selectedAnswer" class="radio-group" :disabled="showResult">
              <label
                v-for="(choice, index) in choices"
                :key="choice.id || index"
                class="option-line"
                :class="getOptionClass(index)"
              >
                <el-radio :label="optionLabel(index)">
                  <span class="option-label">{{ optionLabel(index) }}.</span>
                  <span>{{ choice.content }}</span>
                </el-radio>
              </label>
            </el-radio-group>
          </div>

          <div v-else-if="isJudge" class="answer-area">
            <el-radio-group v-model="selectedAnswer" :disabled="showResult">
              <el-radio-button label="正确" />
              <el-radio-button label="错误" />
            </el-radio-group>
          </div>

          <div v-else class="answer-area">
            <el-input
              v-model="shortAnswer"
              type="textarea"
              :rows="5"
              :disabled="showResult"
              placeholder="写下你的答案，点击确定后查看参考答案和解析"
            />
          </div>

          <div class="action-row">
            <el-button type="primary" size="large" @click="confirmAnswer">确定</el-button>
            <el-button type="success" size="large" :loading="nextLoading" @click="goNextQuestion">下一题</el-button>
            <el-button size="large" @click="router.push('/questions')">返回题库</el-button>
          </div>

          <div v-if="showResult" class="result-box">
            <div class="result-status" :class="isAnswerCorrect ? 'right' : 'wrong'">
              <strong>{{ isAnswerCorrect ? '回答正确' : '回答错误' }}</strong>
              <span v-if="!isAnswerCorrect">别急，看完解析再冲下一题。</span>
              <span v-else>不错，继续保持。</span>
            </div>
            <div class="result-section">
              <h3>参考答案</h3>
              <p>{{ correctAnswer || '暂无参考答案' }}</p>
            </div>
            <div class="result-section">
              <h3>题目解析</h3>
              <p>{{ question.analysis || '暂无解析' }}</p>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="题目不存在或已被删除" />
      </el-card>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionById, getQuestionPage } from '../api/question'
import {
  getDifficultyLabel,
  getDifficultyTag,
  getTypeLabel,
  getTypeTag,
  normalizeQuestionType,
  optionLabel
} from '../utils/questionMeta'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const nextLoading = ref(false)
const question = ref(null)
const selectedAnswer = ref('')
const selectedAnswers = ref([])
const shortAnswer = ref('')
const showResult = ref(false)
const isAnswerCorrect = ref(false)

const currentType = computed(() => normalizeQuestionType(question.value?.type))
const isChoice = computed(() => currentType.value === 'CHOICE')
const isJudge = computed(() => currentType.value === 'JUDGE')
const choices = computed(() => {
  const list = Array.isArray(question.value?.questionChoiceList) ? question.value.questionChoiceList : []
  return list.slice().sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0))
})

const correctAnswer = computed(() => {
  const answer = question.value?.questionAnswer?.answer
  if (answer) return answer

  const labels = choices.value
    .map((choice, index) => (choice.isCorrect ? optionLabel(index) : ''))
    .filter(Boolean)

  return labels.join(',')
})

const correctParts = computed(() => parseChoiceAnswer(correctAnswer.value))

const isMultiple = computed(() => {
  return Boolean(question.value?.isMultiple) || correctParts.value.length > 1 || choices.value.filter(item => item.isCorrect).length > 1
})

function resetAnswerState() {
  selectedAnswer.value = ''
  selectedAnswers.value = []
  shortAnswer.value = ''
  showResult.value = false
  isAnswerCorrect.value = false
}

async function loadQuestion() {
  loading.value = true
  resetAnswerState()

  try {
    question.value = await getQuestionById(route.params.id)
  } catch (error) {
    console.error('题目详情加载失败：', error)
    ElMessage.error(error.message || '题目详情加载失败')
    question.value = null
  } finally {
    loading.value = false
  }
}

function parseChoiceAnswer(answer) {
  return String(answer || '')
    .split(/[，,]/)
    .map(item => item.trim().toUpperCase())
    .filter(Boolean)
    .sort()
}

function normalizeJudgeAnswer(answer) {
  const text = String(answer || '').trim().toLowerCase()
  if (['正确', '对', 'true', 't', '1', 'yes', '是'].includes(text)) return '正确'
  if (['错误', '错', 'false', 'f', '0', 'no', '否'].includes(text)) return '错误'
  return String(answer || '').trim()
}

function normalizeTextAnswer(answer) {
  return String(answer || '')
    .trim()
    .replace(/\s+/g, '')
    .toLowerCase()
}

function hasAnswered() {
  if (isChoice.value && isMultiple.value) return selectedAnswers.value.length > 0
  if (isChoice.value || isJudge.value) return Boolean(selectedAnswer.value)
  return Boolean(shortAnswer.value.trim())
}

function confirmAnswer() {
  if (!hasAnswered()) {
    ElMessage.warning('请先作答，再点击确定')
    return
  }

  if (isChoice.value && isMultiple.value) {
    const selected = selectedAnswers.value.map(item => String(item).trim().toUpperCase()).sort().join(',')
    isAnswerCorrect.value = selected === correctParts.value.join(',')
  } else if (isChoice.value) {
    isAnswerCorrect.value = String(selectedAnswer.value).trim().toUpperCase() === correctParts.value[0]
  } else if (isJudge.value) {
    isAnswerCorrect.value = normalizeJudgeAnswer(selectedAnswer.value) === normalizeJudgeAnswer(correctAnswer.value)
  } else {
    isAnswerCorrect.value = normalizeTextAnswer(shortAnswer.value) === normalizeTextAnswer(correctAnswer.value)
  }

  showResult.value = true
  ElMessage[isAnswerCorrect.value ? 'success' : 'warning'](isAnswerCorrect.value ? '回答正确' : '回答错误，看看解析再继续')
}

function getOptionClass(index) {
  if (!showResult.value) return ''

  const label = optionLabel(index)
  const isCorrectOption = correctParts.value.includes(label)
  const isSelected = isMultiple.value ? selectedAnswers.value.includes(label) : selectedAnswer.value === label

  return {
    'is-correct': isCorrectOption,
    'is-wrong': isSelected && !isCorrectOption
  }
}

async function goNextQuestion() {
  if (!question.value?.id) return

  nextLoading.value = true
  try {
    const params = {
      current: 1,
      size: 1000
    }

    if (question.value.categoryId) {
      params.categoryId = question.value.categoryId
    } else if (question.value.type) {
      params.type = normalizeQuestionType(question.value.type)
    }

    const result = await getQuestionPage(params)
    const list = Array.isArray(result?.records) ? result.records : []
    const currentId = Number(question.value.id)
    const currentIndex = list.findIndex(item => Number(item.id) === currentId)

    if (currentIndex >= 0 && currentIndex < list.length - 1) {
      router.push(`/question/${list[currentIndex + 1].id}`)
      return
    }

    ElMessage.info('没有下一题了~')
  } catch (error) {
    console.error('加载下一题失败：', error)
    ElMessage.error(error.message || '加载下一题失败')
  } finally {
    nextLoading.value = false
  }
}

watch(
  () => route.params.id,
  () => loadQuestion()
)

onMounted(loadQuestion)
</script>

<style scoped>
.question-detail-page{min-height:100vh;background:radial-gradient(circle at 8% 16%,rgba(64,217,188,.42),transparent 28%),radial-gradient(circle at 86% 86%,rgba(247,161,196,.45),transparent 34%),linear-gradient(135deg,#dff9f2 0%,#dce8f3 48%,#f5dceb 100%)}
.student-topbar{height:74px;padding:0 42px;display:flex;align-items:center;justify-content:space-between;background:rgba(255,255,255,.86);border-bottom:1px solid rgba(255,255,255,.9);backdrop-filter:blur(16px)}
.brand{display:inline-flex;align-items:center;gap:12px;border:0;background:transparent;cursor:pointer}
.brand-mark{width:38px;height:38px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;color:#fff;font-weight:800;background:linear-gradient(135deg,#246bfe,#3b2ee8)}
.brand-text{font-size:24px;font-weight:800;color:#1f2a64}
.top-actions{display:flex;gap:12px}
.detail-wrap{max-width:980px;margin:0 auto;padding:54px 32px 90px}
.practice-card{border:0;border-radius:24px;min-height:560px}
.practice-card :deep(.el-card__body){padding:38px 44px}
.question-head .tags{display:flex;gap:10px;margin-bottom:16px}
.question-head h1{margin:0;font-size:26px;line-height:1.65;color:#111827}
.answer-area{margin-top:30px}
.radio-group,.answer-area :deep(.el-checkbox-group){width:100%}
.option-line{display:block;padding:16px 18px;margin-bottom:14px;border-radius:14px;background:#f8fafc;border:1px solid #e5e7eb;cursor:pointer;transition:.2s}
.option-line:hover{background:#f1f5f9}
.option-line.is-correct{background:#ecfdf5;border-color:#86efac}
.option-line.is-wrong{background:#fef2f2;border-color:#fca5a5}
.option-label{display:inline-block;min-width:28px;font-weight:800}
.action-row{margin-top:30px;display:flex;gap:14px;flex-wrap:wrap}
.result-box{margin-top:30px;border-radius:18px;background:#f8fafc;border:1px solid #e5e7eb;overflow:hidden}
.result-status{padding:18px 24px;display:flex;gap:12px;align-items:center;border-bottom:1px solid #e5e7eb}
.result-status strong{font-size:18px}
.result-status.right{background:#ecfdf5;color:#047857}
.result-status.wrong{background:#fff7ed;color:#c2410c}
.result-section{padding:22px 24px}
.result-section+.result-section{border-top:1px solid #e5e7eb}
.result-section h3{margin:0 0 10px;font-size:17px;font-weight:900}
.result-section p{margin:0;line-height:1.8;color:#374151;white-space:pre-wrap}
@media(max-width:720px){.student-topbar{padding:0 18px}.brand-text{font-size:18px}.top-actions{gap:8px}.detail-wrap{padding:28px 16px 60px}.practice-card :deep(.el-card__body){padding:26px 22px}.question-head h1{font-size:21px}.action-row .el-button{width:100%;margin-left:0!important}}
</style>
