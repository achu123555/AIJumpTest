<template>
  <section class="exam-take-page">
    <div v-loading="loading" class="exam-wrapper">
      <header class="exam-header">
        <div>
          <h2>{{ paper.name }}</h2>
          <div class="student-line"><el-icon><User /></el-icon> 考生：{{ exam.studentName }}</div>
        </div>
        <div class="time-box">
          <div class="remain"><el-icon><Timer /></el-icon> 剩余时间：{{ remainText }}</div>
          <el-progress :percentage="progressPercent" :show-text="true" />
        </div>
      </header>

      <main class="question-area">
        <template v-for="group in groupedQuestions" :key="group.type">
          <div v-if="group.questions.length" class="question-section">
            <div class="section-title">{{ group.title }}</div>

            <div v-for="question in group.questions" :key="question.id" class="question-card">
              <div class="question-title">
                第 {{ questionIndexMap[question.id] }} 题（{{ getQuestionScore(question) }}分）
              </div>
              <div class="question-text">{{ question.title }}</div>

              <template v-if="question.type === 'CHOICE'">
                <el-checkbox-group
                  v-if="question.multi"
                  v-model="answers[question.id]"
                  class="option-list"
                  @change="markAnswered(question.id)"
                >
                  <el-checkbox
                    v-for="(choice, index) in question.questionChoiceList"
                    :key="choice.id || index"
                    :label="optionLetter(index)"
                    border
                  >
                    {{ optionLetter(index) }}. {{ choice.content }}
                  </el-checkbox>
                </el-checkbox-group>

                <el-radio-group
                  v-else
                  v-model="answers[question.id]"
                  class="option-list"
                  @change="markAnswered(question.id)"
                >
                  <el-radio
                    v-for="(choice, index) in question.questionChoiceList"
                    :key="choice.id || index"
                    :label="optionLetter(index)"
                    border
                  >
                    {{ optionLetter(index) }}. {{ choice.content }}
                  </el-radio>
                </el-radio-group>
              </template>

              <template v-else-if="question.type === 'JUDGE'">
                <el-radio-group v-model="answers[question.id]" class="judge-options" @change="markAnswered(question.id)">
                  <el-radio label="TRUE" border>正确</el-radio>
                  <el-radio label="FALSE" border>错误</el-radio>
                </el-radio-group>
              </template>

              <template v-else>
                <div class="text-answer-wrap">
                  <el-input
                    v-model="answers[question.id]"
                    type="textarea"
                    :rows="5"
                    maxlength="1000"
                    show-word-limit
                    placeholder="请输入你的答案（禁止粘贴，请手动输入）"
                    @paste.prevent="handlePasteBlocked"
                    @input="markAnswered(question.id)"
                  />
                  <el-tag type="warning" effect="light">此区域禁止粘贴</el-tag>
                </div>
              </template>
            </div>
          </div>
        </template>
      </main>

      <footer class="submit-bar">
        <div>已作答：{{ answeredCount }} / {{ questions.length }} 题</div>
        <el-button type="primary" size="large" :loading="submitting" @click="confirmSubmit">交卷</el-button>
      </footer>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, User } from '@element-plus/icons-vue'
import { getExamDetail, submitExam } from '../api/exam'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const exam = reactive({})
const paper = reactive({})
const questions = ref([])
const answers = reactive({})
const windowSwitches = ref(0)
const remainingSeconds = ref(0)
let timer = null

const groupedQuestions = computed(() => [
  { type: 'CHOICE', title: '一、选择题', questions: questions.value.filter(item => item.type === 'CHOICE') },
  { type: 'JUDGE', title: '二、判断题', questions: questions.value.filter(item => item.type === 'JUDGE') },
  { type: 'TEXT', title: '三、简答题', questions: questions.value.filter(item => item.type === 'TEXT') }
])

const questionIndexMap = computed(() => {
  const map = {}
  questions.value.forEach((item, index) => { map[item.id] = index + 1 })
  return map
})

const answeredCount = computed(() => {
  return questions.value.filter(q => {
    const value = answers[q.id]
    return Array.isArray(value) ? value.length > 0 : value !== undefined && value !== null && String(value).trim() !== ''
  }).length
})

const progressPercent = computed(() => {
  if (!questions.value.length) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

const remainText = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

function optionLetter(index) {
  return String.fromCharCode(65 + index)
}

function getQuestionScore(question) {
  return question.realScore ?? question.score ?? 0
}

function markAnswered() {
  // 空方法用于触发 Vue 响应式更新，关键逻辑在 answeredCount 计算属性中。
}

function handlePasteBlocked() {
  ElMessage.warning('简答题区域禁止粘贴，请手动输入')
}

function handleVisibilityChange() {
  if (document.hidden) {
    windowSwitches.value += 1
    ElMessage.warning(`检测到切换窗口 ${windowSwitches.value} 次`)
  }
}

function initAnswers() {
  questions.value.forEach(question => {
    if (question.type === 'CHOICE' && question.multi) {
      answers[question.id] = []
    } else {
      answers[question.id] = ''
    }
  })
}

function startTimer() {
  clearInterval(timer)
  const duration = Number(paper.duration || 0) * 60
  remainingSeconds.value = duration

  timer = setInterval(() => {
    remainingSeconds.value -= 1
    if (remainingSeconds.value <= 0) {
      remainingSeconds.value = 0
      clearInterval(timer)
      ElMessage.warning('考试时间已到，系统自动交卷')
      submitPaper(true)
    }
  }, 1000)
}

async function loadDetail() {
  loading.value = true
  try {
    const detail = await getExamDetail(route.params.id)
    Object.assign(exam, detail || {})
    Object.assign(paper, detail?.detailPaper || {})
    questions.value = Array.isArray(detail?.detailPaper?.questions) ? detail.detailPaper.questions : []
    initAnswers()
    await nextTick()
    startTimer()
  } catch (error) {
    ElMessage.error(error.message || '加载考试失败')
  } finally {
    loading.value = false
  }
}

async function confirmSubmit() {
  await ElMessageBox.confirm(
    `当前已作答 ${answeredCount.value}/${questions.value.length} 题，确认交卷吗？提交后不可修改答案。`,
    '确认交卷',
    { type: 'warning' }
  )
  await submitPaper(false)
}

function normalizeSubmitAnswers() {
  const result = {}
  questions.value.forEach(question => {
    const value = answers[question.id]
    if (Array.isArray(value)) {
      result[question.id] = value.join(',')
    } else {
      result[question.id] = value ?? ''
    }
  })
  return result
}

async function submitPaper(auto = false) {
  if (submitting.value) return
  submitting.value = true
  try {
    clearInterval(timer)
    await submitExam(route.params.id, {
      answers: normalizeSubmitAnswers(),
      windowSwitches: windowSwitches.value
    })
    ElMessage.success(auto ? '时间到，已自动交卷' : '交卷成功')
    router.replace(`/exam/result/${route.params.id}`)
  } catch (error) {
    ElMessage.error(error.message || '交卷失败')
    if (!auto) startTimer()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  loadDetail()
})

onBeforeUnmount(() => {
  clearInterval(timer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.exam-take-page {
  min-height: 100vh;
  background: #e9e9e9;
  padding: 22px 0 80px;
}

.exam-wrapper {
  width: 920px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #cfd3dc;
}

.exam-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  padding: 20px 24px;
}

.exam-header h2 {
  margin: 0 0 8px;
  font-size: 18px;
}

.student-line,
.remain {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #303133;
}

.time-box {
  width: 220px;
  color: #d03030;
  font-weight: 600;
}

.question-area {
  padding: 20px;
}

.section-title {
  background: #dedede;
  border: 1px solid #c7c7c7;
  padding: 12px 16px;
  font-weight: 700;
}

.question-card {
  border: 1px solid #dcdfe6;
  border-top: 0;
  padding: 18px;
}

.question-title {
  font-weight: 700;
  margin-bottom: 8px;
}

.question-text {
  font-weight: 600;
  line-height: 1.8;
  margin-bottom: 14px;
}

.option-list {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.option-list :deep(.el-radio),
.option-list :deep(.el-checkbox) {
  /* 选项内容可能很长，不能固定 320px，否则文字会越框。 */
  width: min(100%, 620px);
  height: auto;
  min-height: 42px;
  margin-right: 0;
  display: flex;
  align-items: flex-start;
  padding: 10px 14px;
  white-space: normal;
}

.option-list :deep(.el-radio__label),
.option-list :deep(.el-checkbox__label) {
  flex: 1;
  min-width: 0;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  line-height: 1.55;
}

.judge-options {
  display: flex;
  gap: 12px;
}

.text-answer-wrap {
  position: relative;
}

.text-answer-wrap .el-tag {
  position: absolute;
  right: 12px;
  top: 8px;
}

.submit-bar {
  position: sticky;
  bottom: 0;
  background: #f5f7fa;
  border-top: 1px solid #dcdfe6;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 80px;
}
</style>
