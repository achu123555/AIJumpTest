<template>
  <el-dialog
    v-model="dialogVisible"
    title="AI智能生成题目"
    width="980px"
    destroy-on-close
    class="ai-generate-dialog"
    @closed="handleClosed"
  >
    <div class="ai-steps-wrap">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="设置参数" description="配置生成要求" />
        <el-step title="AI生成中" description="请稍候..." />
        <el-step title="预览确认" description="检查生成结果" />
        <el-step title="导入完成" description="确认导入结果" />
      </el-steps>
    </div>

    <!-- 第一步：填写 AI 生成参数 -->
    <el-form
      v-if="activeStep === 0"
      ref="generateFormRef"
      :model="generateForm"
      :rules="generateRules"
      label-width="116px"
      class="ai-param-form"
    >
      <el-form-item label="题目主题" prop="topic">
        <el-input
          v-model.trim="generateForm.topic"
          maxlength="120"
          show-word-limit
          placeholder="例如：Spring框架、Java并发编程、MySQL数据库等"
        />
        <div class="form-tip">描述你想要生成题目的技术领域或知识点</div>
      </el-form-item>

      <el-form-item label="生成数量" prop="count">
        <div class="inline-control">
          <el-input-number v-model="generateForm.count" :min="1" :max="20" :step="1" />
          <span class="form-tip inline-tip">建议一次生成 5-10 道题，质量更高</span>
        </div>
      </el-form-item>

      <el-form-item label="题目类型" prop="typeList">
        <el-checkbox-group v-model="generateForm.typeList" :max="1">
          <el-checkbox label="CHOICE">选择题</el-checkbox>
          <el-checkbox label="JUDGE">判断题</el-checkbox>
          <el-checkbox label="TEXT">简答题</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="难度等级" prop="difficulty">
        <el-radio-group v-model="generateForm.difficulty">
          <el-radio label="EASY">简单</el-radio>
          <el-radio label="MEDIUM">中等</el-radio>
          <el-radio label="HARD">困难</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="题目分类" prop="categoryId">
        <el-select
          v-model="generateForm.categoryId"
          filterable
          clearable
          placeholder="请选择当前题型下的分类"
          @change="handleCategoryChange"
        >
          <el-option
            v-for="item in currentCategoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <div class="form-tip">下拉框显示分类名称，实际提交给后端的是分类 ID。</div>
      </el-form-item>

      <el-form-item label="包含多选题" v-if="selectedType === 'CHOICE'">
        <el-switch v-model="generateForm.multi" />
        <span class="form-tip inline-tip">选择题中是否包含多选题</span>
      </el-form-item>

      <el-form-item label="额外要求">
        <el-input
          v-model.trim="generateForm.requirement"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="例如：重点考察实际应用场景、包含代码示例、侧重面试常考点等"
        />
        <div class="form-tip">可选，用于指导 AI 生成更符合需求的题目</div>
      </el-form-item>
    </el-form>

    <!-- 第二步：AI 生成中 -->
    <div v-else-if="activeStep === 1" class="generating-box">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <h3>AI正在为您生成题目...</h3>
      <p>主题：{{ generateForm.topic }}</p>
      <p>数量：{{ generateForm.count }} 道</p>
      <p>类型：{{ getTypeLabel(selectedType) }}</p>
      <el-progress :percentage="progress" class="ai-progress" />
    </div>

    <!-- 第三步：预览生成结果 -->
    <div v-else-if="activeStep === 2" class="preview-box">
      <el-alert
        type="success"
        show-icon
        :closable="false"
        :title="`AI生成完成！共生成 ${previewList.length} 道题目`"
        class="result-alert"
      />
      <p class="preview-tip">您可以点击题目进行编辑修改，确认无误后批量导入</p>

      <el-table :data="previewList" border stripe height="430" empty-text="暂无生成结果">
        <el-table-column label="题目内容" min-width="280">
          <template #default="{ row }">
            <div class="preview-title">{{ row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="95" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTag(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getDifficultyTag(row.difficulty)">{{ getDifficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选项/答案" min-width="250">
          <template #default="{ row }">
            <template v-if="normalizeQuestionType(row.type) === 'CHOICE'">
              <p
                v-for="(choice, index) in sortedChoices(row)"
                :key="choice.uid || choice.id || index"
                :class="['choice-line', { correct: choice.isCorrect }]"
              >
                {{ optionLabel(index) }}. {{ choice.content }}
              </p>
            </template>
            <span v-else>{{ row.questionAnswer?.answer || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="解析" min-width="200">
          <template #default="{ row }">
            <div class="analysis-text">{{ row.analysis || '暂无解析' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row, $index }">
            <el-button size="small" type="primary" :icon="Edit" @click="openPreviewEdit(row, $index)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 第四步：导入完成 -->
    <div v-else class="finish-box">
      <el-result icon="success" title="导入完成" :sub-title="`已成功导入 ${importedCount} 道 AI 生成题目`">
        <template #extra>
          <el-button type="primary" @click="closeAndRefresh">返回题目列表</el-button>
          <el-button @click="resetToFirstStep">继续生成</el-button>
        </template>
      </el-result>
    </div>

    <template #footer>
      <template v-if="activeStep === 0">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generateLoading" @click="startGenerate">开始生成</el-button>
      </template>
      <template v-else-if="activeStep === 1">
        <el-button disabled>AI生成中，请稍候...</el-button>
      </template>
      <template v-else-if="activeStep === 2">
        <el-button @click="resetToFirstStep">重新生成</el-button>
        <el-button type="primary" :loading="importLoading" @click="confirmImport">
          确认导入({{ previewList.length }}道)
        </el-button>
      </template>
    </template>

    <!-- 预览编辑弹窗：导入前允许人工修正 AI 生成结果 -->
    <el-dialog v-model="editDialog.visible" title="编辑AI生成题目" width="760px" append-to-body destroy-on-close>
      <el-form :model="editForm" label-width="96px" class="preview-edit-form">
        <el-form-item label="题目内容" required>
          <el-input v-model.trim="editForm.title" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="editForm.difficulty">
            <el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="editForm.score" :min="1" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="题目解析">
          <el-input v-model.trim="editForm.analysis" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>

        <template v-if="normalizeQuestionType(editForm.type) === 'CHOICE'">
          <el-form-item label="是否多选">
            <el-switch v-model="editForm.multi" />
          </el-form-item>
          <el-form-item
            v-for="(choice, index) in editForm.questionChoiceList"
            :key="choice.uid || index"
            :label="`选项${optionLabel(index)}`"
            required
          >
            <div class="edit-choice-row">
              <el-input v-model.trim="choice.content" placeholder="请输入选项内容" />
              <el-checkbox v-model="choice.isCorrect" @change="handleEditChoiceCorrectChange(index)">正确</el-checkbox>
            </div>
          </el-form-item>
        </template>

        <el-form-item v-else-if="normalizeQuestionType(editForm.type) === 'JUDGE'" label="参考答案">
          <el-radio-group v-model="editForm.questionAnswer.answer">
            <el-radio-button label="true">正确</el-radio-button>
            <el-radio-button label="false">错误</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-else label="参考答案">
          <el-input v-model.trim="editForm.questionAnswer.answer" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="savePreviewEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Loading } from '@element-plus/icons-vue'
import { addQuestion, generateAiQuestions } from '../api/question'
import {
  DIFFICULTY_OPTIONS,
  getDifficultyLabel,
  getDifficultyTag,
  getTypeLabel,
  getTypeTag,
  normalizeDifficulty,
  normalizeQuestionType,
  optionLabel
} from '../utils/questionMeta'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  categoryTree: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'imported'])

const activeStep = ref(0)
const progress = ref(0)
const progressTimer = ref(null)
const generateLoading = ref(false)
const importLoading = ref(false)
const importedCount = ref(0)
const previewList = ref([])
const generateFormRef = ref(null)

const generateForm = reactive(createDefaultGenerateForm())
const editDialog = reactive({ visible: false, index: -1 })
const editForm = reactive(createEmptyPreviewQuestion())

const dialogVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const selectedType = computed(() => generateForm.typeList[0] || 'CHOICE')
const typeRootNameMap = {
  CHOICE: '选择题',
  JUDGE: '判断题',
  TEXT: '简答题'
}
const flatCategories = computed(() => flattenCategoryTree(props.categoryTree))
const categoryNameMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item.name])))

// 根据当前题型只展示对应父分类下面的子分类。
// 例如 type=TEXT 时，只显示“简答题”下面的 Spring框架、MySQL数据库等。
const currentCategoryOptions = computed(() => {
  const rootName = typeRootNameMap[selectedType.value]
  const root = props.categoryTree.find(item => item.name === rootName)
  return Array.isArray(root?.children) ? root.children : []
})
const selectedCategoryName = computed(() => categoryNameMap.value.get(Number(generateForm.categoryId)) || '')

const generateRules = {
  topic: [{ required: true, message: '请输入题目主题', trigger: 'blur' }],
  count: [{ required: true, message: '请输入生成数量', trigger: 'change' }],
  typeList: [
    {
      validator: (_rule, value, callback) => {
        if (!Array.isArray(value) || value.length !== 1) {
          callback(new Error('请选择一种题目类型'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择题目分类', trigger: 'change' }]
}

watch(
  () => props.modelValue,
  visible => {
    if (visible) {
      resetToFirstStep()
    }
  }
)

watch(
  selectedType,
  () => {
    // 切换题型后，分类所属父级也变了，必须清空重新选择。
    generateForm.categoryId = null
  }
)

function createDefaultGenerateForm() {
  return {
    topic: '',
    count: 5,
    typeList: ['CHOICE'],
    difficulty: 'MEDIUM',
    categoryId: null,
    multi: false,
    requirement: ''
  }
}

function createEmptyPreviewQuestion() {
  return {
    title: '',
    type: 'CHOICE',
    categoryId: null,
    multi: false,
    difficulty: 'MEDIUM',
    score: 5,
    analysis: '',
    questionAnswer: { answer: '', keywords: '' },
    questionChoiceList: []
  }
}

function flattenCategoryTree(list = []) {
  const result = []
  const walk = nodes => {
    ;(nodes || []).forEach(node => {
      result.push(node)
      if (Array.isArray(node.children) && node.children.length) {
        walk(node.children)
      }
    })
  }
  walk(list)
  return result
}

function clearProgressTimer() {
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
    progressTimer.value = null
  }
}

function startMockProgress() {
  clearProgressTimer()
  progress.value = 8
  progressTimer.value = setInterval(() => {
    if (progress.value < 88) {
      progress.value += Math.random() * 8
      progress.value = Math.min(88, Number(progress.value.toFixed(2)))
    }
  }, 600)
}

async function startGenerate() {
  try {
    await generateFormRef.value?.validate()
  } catch (_error) {
    return
  }

  generateLoading.value = true
  activeStep.value = 1
  startMockProgress()

  try {
    const payload = buildGeneratePayload()
    const result = await generateAiQuestions(payload)
    const list = normalizeAiResult(result)

    if (!list.length) {
      throw new Error('AI没有返回可预览的题目')
    }

    previewList.value = list.map(normalizePreviewQuestion)
    progress.value = 100
    activeStep.value = 2
    ElMessage.success(`AI生成完成，共 ${previewList.value.length} 道题目`)
  } catch (error) {
    console.error('AI生成题目失败：', error)
    ElMessage.error(error.message || 'AI生成题目失败，请稍后再试')
    activeStep.value = 0
  } finally {
    clearProgressTimer()
    generateLoading.value = false
  }
}

function buildGeneratePayload() {
  const requirementParts = [`题目主题：${generateForm.topic}`]
  if (generateForm.requirement) {
    requirementParts.push(`额外要求：${generateForm.requirement}`)
  }

  return {
    // 主题内容合并到 requirement 里，让后端 buildAiQuestionPrompt 统一使用。
    categoryId: generateForm.categoryId,
    categoryName: selectedCategoryName.value,
    type: selectedType.value,
    multi: selectedType.value === 'CHOICE' ? Boolean(generateForm.multi) : false,
    difficulty: generateForm.difficulty,
    score: 5,
    count: generateForm.count,
    requirement: requirementParts.join('；')
  }
}

function handleCategoryChange() {
  // v-model 保存的是分类 id，categoryName 通过 selectedCategoryName 计算出来。
}

function normalizeAiResult(result) {
  if (Array.isArray(result)) return result
  if (Array.isArray(result?.records)) return result.records
  if (Array.isArray(result?.data)) return result.data
  if (Array.isArray(result?.questions)) return result.questions
  return []
}

function normalizePreviewQuestion(question = {}) {
  const type = normalizeQuestionType(question.type || selectedType.value)
  const choices = Array.isArray(question.questionChoiceList)
    ? question.questionChoiceList.map((choice, index) => ({
        uid: `${Date.now()}_${Math.random()}_${index}`,
        content: choice.content || '',
        isCorrect: Boolean(choice.isCorrect),
        sort: Number(choice.sort || index + 1)
      }))
    : []

  let answer = question.questionAnswer || { answer: '', keywords: '' }
  if (type === 'JUDGE') {
    answer = {
      ...answer,
      answer: normalizeJudgeAnswer(answer.answer)
    }
  }

  return {
    ...question,
    type,
    categoryId: generateForm.categoryId,
    multi: type === 'CHOICE' ? Boolean(question.multi ?? question.isMultiple ?? generateForm.multi) : false,
    difficulty: normalizeDifficulty(question.difficulty || generateForm.difficulty),
    score: Number(question.score || 5),
    analysis: question.analysis || '暂无解析',
    questionAnswer: type === 'CHOICE' ? null : answer,
    questionChoiceList: type === 'CHOICE' ? ensureChoiceSort(choices) : []
  }
}

function normalizeJudgeAnswer(answer) {
  const text = String(answer ?? '').trim()
  if (text === '正确' || text === '对' || text.toLowerCase() === 'true') return 'true'
  if (text === '错误' || text === '错' || text.toLowerCase() === 'false') return 'false'
  return 'true'
}

function ensureChoiceSort(choices = []) {
  return choices
    .slice()
    .sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0))
    .map((choice, index) => ({ ...choice, sort: index + 1 }))
}

function sortedChoices(row) {
  return ensureChoiceSort(row.questionChoiceList || [])
}

function openPreviewEdit(row, index) {
  editDialog.index = index
  Object.assign(editForm, JSON.parse(JSON.stringify(normalizePreviewQuestion(row))))
  if (!editForm.questionAnswer) {
    editForm.questionAnswer = { answer: '', keywords: '' }
  }
  editDialog.visible = true
}

function handleEditChoiceCorrectChange(index) {
  if (editForm.multi) return
  if (!editForm.questionChoiceList[index]?.isCorrect) return
  editForm.questionChoiceList.forEach((choice, choiceIndex) => {
    choice.isCorrect = choiceIndex === index
  })
}

function savePreviewEdit() {
  if (!editForm.title?.trim()) {
    ElMessage.warning('题目内容不能为空')
    return
  }

  if (normalizeQuestionType(editForm.type) === 'CHOICE') {
    const choices = editForm.questionChoiceList || []
    if (choices.some(choice => !choice.content?.trim())) {
      ElMessage.warning('请填写完整的选项内容')
      return
    }

    const correctCount = choices.filter(choice => choice.isCorrect).length
    if (!correctCount) {
      ElMessage.warning('请至少设置一个正确答案')
      return
    }
    if (!editForm.multi && correctCount > 1) {
      ElMessage.warning('单选题只能有一个正确答案')
      return
    }
  }

  previewList.value.splice(editDialog.index, 1, normalizePreviewQuestion(JSON.parse(JSON.stringify(editForm))))
  editDialog.visible = false
}

async function confirmImport() {
  if (!previewList.value.length) {
    ElMessage.warning('没有可导入的题目')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定批量导入这 ${previewList.value.length} 道 AI 生成题目吗？`,
      '确认导入',
      {
        type: 'warning',
        confirmButtonText: '确认导入',
        cancelButtonText: '取消'
      }
    )
  } catch (_error) {
    return
  }

  importLoading.value = true
  try {
    for (const item of previewList.value) {
      await addQuestion(buildSavePayload(item))
    }
    importedCount.value = previewList.value.length
    activeStep.value = 3
    emit('imported')
    ElMessage.success('AI题目批量导入成功')
  } catch (error) {
    console.error('导入AI题目失败：', error)
    ElMessage.error(error.message || '导入AI题目失败')
  } finally {
    importLoading.value = false
  }
}

function buildSavePayload(question) {
  const type = normalizeQuestionType(question.type)
  const base = {
    title: question.title,
    type,
    categoryId: question.categoryId || generateForm.categoryId,
    multi: type === 'CHOICE' ? Boolean(question.multi) : false,
    difficulty: normalizeDifficulty(question.difficulty),
    score: Number(question.score || 5),
    analysis: question.analysis || '暂无解析'
  }

  if (type === 'CHOICE') {
    return {
      ...base,
      questionAnswer: null,
      questionChoiceList: ensureChoiceSort(question.questionChoiceList || []).map((choice, index) => ({
        content: choice.content,
        isCorrect: Boolean(choice.isCorrect),
        sort: index + 1
      }))
    }
  }

  return {
    ...base,
    questionAnswer: {
      answer: type === 'JUDGE' ? normalizeJudgeAnswer(question.questionAnswer?.answer) : question.questionAnswer?.answer,
      keywords: question.questionAnswer?.keywords || ''
    },
    questionChoiceList: null
  }
}

function resetToFirstStep() {
  clearProgressTimer()
  activeStep.value = 0
  progress.value = 0
  previewList.value = []
  importedCount.value = 0
  Object.assign(generateForm, createDefaultGenerateForm())
}

function closeAndRefresh() {
  dialogVisible.value = false
  emit('imported')
}

function handleClosed() {
  clearProgressTimer()
}
</script>

<style scoped>
.ai-steps-wrap {
  padding: 8px 16px 28px;
}

.ai-param-form {
  padding: 0 28px;
}

.ai-param-form :deep(.el-select) {
  width: 100%;
}

.form-tip {
  width: 100%;
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.4;
}

.inline-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.inline-tip {
  width: auto;
  margin-top: 0;
}

.generating-box {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.loading-icon {
  margin-bottom: 22px;
  font-size: 56px;
  color: var(--el-color-primary);
  animation: ai-rotate 1s linear infinite;
}

.generating-box h3 {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 800;
}

.generating-box p {
  margin: 4px 0;
  color: #303133;
  font-size: 14px;
}

.ai-progress {
  width: 92%;
  margin-top: 18px;
}

.preview-box {
  padding: 0 8px;
}

.result-alert {
  margin-bottom: 14px;
}

.preview-tip {
  margin: 0 0 16px;
  color: #606266;
  font-size: 14px;
}

.preview-title {
  font-weight: 600;
  line-height: 1.65;
}

.choice-line {
  margin: 4px 0;
  line-height: 1.55;
}

.choice-line.correct {
  color: #2f9e44;
  font-weight: 700;
}

.analysis-text {
  max-height: 96px;
  overflow: hidden;
  line-height: 1.6;
}

.finish-box {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-edit-form :deep(.el-select),
.preview-edit-form :deep(.el-input-number) {
  width: 100%;
}

.edit-choice-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 80px;
  gap: 12px;
  align-items: center;
}

@keyframes ai-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
