export const QUESTION_TYPES = [
  { label: '选择题', value: 'CHOICE', tagType: 'primary' },
  { label: '判断题', value: 'JUDGE', tagType: 'success' },
  // 后端数据库里简答题类型是 TEXT，这里统一用 TEXT，避免筛选查不到数据。
  { label: '简答题', value: 'TEXT', tagType: 'warning' }
]

export const DIFFICULTY_OPTIONS = [
  { label: '简单', value: 'EASY', tagType: 'success' },
  { label: '中等', value: 'MEDIUM', tagType: 'warning' },
  { label: '困难', value: 'HARD', tagType: 'danger' }
]

const typeLabelMap = new Map([
  ...QUESTION_TYPES.map(item => [item.value, item.label]),
  ['选择题', '选择题'],
  ['判断题', '判断题'],
  ['简答题', '简答题'],
  ['SHORT', '简答题'],
  ['ANSWER', '简答题'],
  ['SHORT_ANSWER', '简答题']
])

const difficultyLabelMap = new Map([
  ...DIFFICULTY_OPTIONS.map(item => [item.value, item.label]),
  ['简单', '简单'],
  ['中等', '中等'],
  ['困难', '困难'],
  ['SIMPLE', '简单'],
  ['NORMAL', '中等'],
  ['DIFFICULT', '困难']
])

const typeTagMap = new Map(QUESTION_TYPES.map(item => [item.value, item.tagType]))
const difficultyTagMap = new Map(DIFFICULTY_OPTIONS.map(item => [item.value, item.tagType]))

export function getTypeLabel(type) {
  return typeLabelMap.get(type) || type || '未知类型'
}

export function getDifficultyLabel(difficulty) {
  return difficultyLabelMap.get(difficulty) || difficulty || '未设置'
}

export function getTypeTag(type) {
  if (type === '选择题') return 'primary'
  if (type === '判断题') return 'success'
  if (type === '简答题') return 'warning'
  return typeTagMap.get(type) || 'info'
}

export function getDifficultyTag(difficulty) {
  if (difficulty === '简单') return 'success'
  if (difficulty === '中等') return 'warning'
  if (difficulty === '困难') return 'danger'
  return difficultyTagMap.get(difficulty) || 'info'
}

export function normalizeQuestionType(type) {
  if (type === '选择题') return 'CHOICE'
  if (type === '判断题') return 'JUDGE'
  if (type === '简答题' || type === 'SHORT' || type === 'ANSWER' || type === 'SHORT_ANSWER') return 'TEXT'
  return type || 'CHOICE'
}

export function normalizeDifficulty(difficulty) {
  if (difficulty === '简单' || difficulty === 'SIMPLE') return 'EASY'
  if (difficulty === '中等' || difficulty === 'NORMAL') return 'MEDIUM'
  if (difficulty === '困难' || difficulty === 'DIFFICULT') return 'HARD'
  return difficulty || 'MEDIUM'
}

export function optionLabel(index) {
  return String.fromCharCode(65 + index)
}

export function inferQuestionTypeByCategoryName(name = '') {
  const text = String(name || '').trim()
  if (text.includes('选择')) return 'CHOICE'
  if (text.includes('判断')) return 'JUDGE'
  if (text.includes('简答') || text.includes('问答') || text.includes('文本')) return 'TEXT'
  return ''
}
