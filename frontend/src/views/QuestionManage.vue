<template>
  <section class="question-page">
    <el-card class="search-card" shadow="always">
      <div class="search-row">
        <el-input v-model.trim="query.keyword" clearable :prefix-icon="Search" placeholder="搜索题目内容" @keyup.enter="handleSearch" />
        <el-select v-model="query.type" clearable placeholder="选择类型" @change="handleTypeFilterChange"><el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        <el-select v-model="query.difficulty" clearable placeholder="选择难度"><el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
      <div class="action-row">
        <el-button type="primary" :icon="Plus" @click="openAddDialog">添加题目</el-button>
        <el-button type="danger" :icon="Delete" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除({{ selectedRows.length }})</el-button>
        <el-dropdown split-button type="success" @click="showPending('批量导入')"><span>批量导入</span><template #dropdown><el-dropdown-menu><el-dropdown-item @click="showPending('下载题目模板')">下载模板</el-dropdown-item><el-dropdown-item @click="showPending('Excel导入题目')">Excel导入</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
        <el-button type="warning" :icon="MagicStick" @click="showPending('AI生成题目')">AI生成</el-button>
      </div>
    </el-card>

    <div class="content-grid">
      <el-card class="table-card" shadow="always">
        <el-table v-loading="loading" :data="questionList" stripe border empty-text="暂无题目数据" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="52" />
          <el-table-column label="题目内容" min-width="520"><template #default="{ row }"><div class="question-title" :title="row.title">{{ row.title }}</div><div class="question-sub"><span>{{ getCategoryName(row.categoryId) }}</span><span v-if="row.score !== undefined && row.score !== null">{{ row.score }} 分</span></div></template></el-table-column>
          <el-table-column label="类型" width="120" align="center"><template #default="{ row }"><el-tag size="small" :type="getTypeTag(row.type)" effect="light">{{ getTypeLabel(row.type) }}</el-tag></template></el-table-column>
          <el-table-column label="难度" width="120" align="center"><template #default="{ row }"><el-tag size="small" :type="getDifficultyTag(row.difficulty)" effect="light">{{ getDifficultyLabel(row.difficulty) }}</el-tag></template></el-table-column>
          <el-table-column label="创建时间" width="180"><template #default="{ row }">{{ formatDate(row.createTime) }}</template></el-table-column>
          <el-table-column label="操作" width="210" fixed="right" align="center"><template #default="{ row }"><el-button size="small" :icon="View" @click="openDetail(row)">查看</el-button><el-button size="small" type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button><el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="pager-row"><span class="total-text">Total {{ page.total }}</span><el-pagination v-model:current-page="page.current" v-model:page-size="page.size" layout="sizes, prev, pager, next, jumper" :total="page.total" :page-sizes="[10, 20, 50, 100]" @current-change="loadQuestionPage" @size-change="handleSizeChange" /></div>
      </el-card>

      <el-card class="category-card" shadow="always">
        <div class="category-head"><h3>题目分类</h3><el-button size="small" plain @click="resetCategoryFilter">重置分类筛选</el-button></div>
        <el-tree v-loading="categoryLoading" :data="categoryTree" node-key="id" :props="treeProps" :default-expand-all="true" :highlight-current="true" @node-click="handleCategoryClick"><template #default="{ data }"><div class="category-node"><span>{{ data.name }}</span><em>({{ data.questionCount || 0 }})</em></div></template></el-tree>
      </el-card>
    </div>

    <el-dialog v-model="formDialog.visible" :title="formDialog.mode === 'add' ? '添加题目' : '编辑题目'" width="820px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="question-form">
        <el-form-item label="题目内容" prop="title"><el-input v-model.trim="form.title" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入题目内容" /></el-form-item>
        <el-form-item label="题目类型" prop="type"><el-select v-model="form.type" placeholder="请选择题目类型" @change="handleTypeChange"><el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item v-if="form.type === 'CHOICE'" label="是否为多选题"><el-switch v-model="form.isMultiple" @change="handleMultipleChange" /></el-form-item>
        <el-form-item label="分类" prop="categoryId"><el-tree-select v-model="form.categoryId" :data="categoryTree" :props="treeProps" node-key="id" check-strictly filterable clearable placeholder="请选择分类" /></el-form-item>
        <el-form-item label="难度等级" prop="difficulty"><el-select v-model="form.difficulty" placeholder="请选择难度"><el-option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="分值" prop="score"><el-input-number v-model="form.score" :min="1" :max="100" controls-position="right" /></el-form-item>
        <el-form-item label="题目解析"><el-input v-model.trim="form.analysis" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="请输入题目解析" /></el-form-item>
        <template v-if="form.type === 'CHOICE'">
          <el-form-item v-for="(choice, index) in form.questionChoiceList" :key="choice.uid" :label="`选项${optionLabel(index)}`" required><div class="choice-row"><el-input v-model.trim="choice.content" placeholder="请输入选项内容" /><el-checkbox v-model="choice.isCorrect" @change="handleChoiceCorrectChange(index)">正确答案</el-checkbox><el-button circle type="danger" :icon="Delete" :disabled="form.questionChoiceList.length <= 2" @click="removeChoice(index)" /></div></el-form-item>
          <el-form-item><el-button type="primary" plain :icon="Plus" @click="addChoice">添加选项</el-button></el-form-item>
        </template>
        <el-form-item v-else-if="form.type === 'JUDGE'" label="参考答案" prop="answer"><el-radio-group v-model="form.answer"><el-radio-button label="正确" /><el-radio-button label="错误" /></el-radio-group></el-form-item>
        <el-form-item v-else label="参考答案" prop="answer"><el-input v-model.trim="form.answer" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="请输入参考答案" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="formDialog.visible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="题目详情" width="760px"><div v-if="detailDialog.data" class="detail-box"><div class="detail-title">{{ detailDialog.data.title }}</div><div class="detail-tags"><el-tag :type="getTypeTag(detailDialog.data.type)">{{ getTypeLabel(detailDialog.data.type) }}</el-tag><el-tag :type="getDifficultyTag(detailDialog.data.difficulty)">{{ getDifficultyLabel(detailDialog.data.difficulty) }}</el-tag><el-tag type="info">{{ getCategoryName(detailDialog.data.categoryId) }}</el-tag><el-tag type="warning">{{ detailDialog.data.score || 0 }} 分</el-tag></div><div v-if="normalizeQuestionType(detailDialog.data.type) === 'CHOICE'" class="detail-section"><h4>选项</h4><p v-for="(choice, index) in detailDialog.data.questionChoiceList || []" :key="choice.id || index">{{ optionLabel(index) }}. {{ choice.content }} <el-tag v-if="choice.isCorrect" size="small" type="success">正确</el-tag></p></div><div class="detail-section"><h4>参考答案</h4><p>{{ detailDialog.data.questionAnswer?.answer || '暂无参考答案' }}</p></div><div class="detail-section"><h4>题目解析</h4><p>{{ detailDialog.data.analysis || '暂无解析' }}</p></div></div></el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, MagicStick, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { getCategoryTree } from '../api/category'
import { addQuestion, deleteQuestion, getQuestionById, getQuestionPage, updateQuestion } from '../api/question'
import { DIFFICULTY_OPTIONS, QUESTION_TYPES, getDifficultyLabel, getDifficultyTag, getTypeLabel, getTypeTag, normalizeDifficulty, normalizeQuestionType, optionLabel, inferQuestionTypeByCategoryName } from '../utils/questionMeta'

const loading = ref(false), categoryLoading = ref(false), submitLoading = ref(false)
const selectedRows = ref([]), questionList = ref([]), categoryTree = ref([]), flatCategories = ref([]), formRef = ref(null)
const query = reactive({ keyword: '', type: '', difficulty: '', categoryId: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const formDialog = reactive({ visible: false, mode: 'add' })
const detailDialog = reactive({ visible: false, data: null })
const form = reactive(createDefaultForm())
const rules = { title: [{ required: true, message: '请输入题目内容', trigger: 'blur' }], type: [{ required: true, message: '请选择题目类型', trigger: 'change' }], categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }], difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }], score: [{ required: true, message: '请输入分值', trigger: 'change' }], answer: [{ required: true, message: '请输入参考答案', trigger: 'blur' }] }
const treeProps = { label: 'name', children: 'children' }
const categoryNameMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item.name])))
const categoryByIdMap = computed(() => new Map(flatCategories.value.map(item => [Number(item.id), item])))
function createDefaultForm() { return { id: null, title: '', type: 'CHOICE', isMultiple: false, categoryId: null, difficulty: 'MEDIUM', score: 5, analysis: '', answer: '', answerId: null, questionChoiceList: [createChoice(1), createChoice(2)] } }
function createChoice(sort, content = '', isCorrect = false) { return { uid: `${Date.now()}_${Math.random()}`, sort, content, isCorrect } }
function resetForm(data = createDefaultForm()) { Object.assign(form, data) }
function flattenCategoryTree(list = []) { const result = []; const walk = nodes => nodes.forEach(node => { result.push(node); if (Array.isArray(node.children) && node.children.length) walk(node.children) }); walk(list); return result }
function getCategoryName(categoryId) { if (!categoryId) return '未分类'; return categoryNameMap.value.get(Number(categoryId)) || `分类 ${categoryId}` }
async function loadCategoryTree() { categoryLoading.value = true; try { const list = await getCategoryTree(); categoryTree.value = Array.isArray(list) ? list : []; flatCategories.value = flattenCategoryTree(categoryTree.value) } catch (error) { console.error('加载分类失败：', error); ElMessage.warning('分类加载失败，请确认 /api/categories/tree 接口是否正常') } finally { categoryLoading.value = false } }
async function loadQuestionPage() { loading.value = true; try { const result = await getQuestionPage({ current: page.current, size: page.size, keyword: query.keyword, type: query.type, difficulty: query.difficulty, categoryId: query.categoryId }); questionList.value = Array.isArray(result?.records) ? result.records : []; page.total = Number(result?.total || 0); page.current = Number(result?.current || page.current); page.size = Number(result?.size || page.size) } catch (error) { console.error('加载题目失败：', error); ElMessage.error(error.message || '题目列表加载失败') } finally { loading.value = false } }
function handleSearch() { page.current = 1; loadQuestionPage() }
function handleTypeFilterChange() { query.categoryId = ''; page.current = 1; loadQuestionPage() }
function resetQuery() { query.keyword = ''; query.type = ''; query.difficulty = ''; query.categoryId = ''; page.current = 1; loadQuestionPage() }
function handleSizeChange() { page.current = 1; loadQuestionPage() }
function handleCategoryClick(node) {
  page.current = 1
  const parentId = Number(node.parentId ?? node.parent_id ?? 0)

  // 点击一级主干分类：选择题 / 判断题 / 简答题。
  // 题目表已经有 type 字段，所以主干筛选走 type，不用 categoryId。
  if (parentId === 0) {
    query.categoryId = ''
    query.type = inferQuestionTypeByCategoryName(node.name)
    loadQuestionPage()
    return
  }

  // 点击二级分支分类：Java基础 / Spring框架等。
  // 后端按 categoryId 精确查，同时同步父级 type。
  query.categoryId = node.id
  const parent = categoryByIdMap.value.get(parentId)
  const parentType = inferQuestionTypeByCategoryName(parent?.name)
  if (parentType) query.type = parentType
  loadQuestionPage()
}
function resetCategoryFilter() { query.categoryId = ''; query.type = ''; page.current = 1; loadQuestionPage() }
function handleSelectionChange(rows) { selectedRows.value = rows }
function showPending(name) { ElMessage.info(`${name}接口还没接入，当前先保留入口`) }
function openAddDialog() { formDialog.mode = 'add'; resetForm(); formDialog.visible = true }
async function openEditDialog(row) { formDialog.mode = 'edit'; try { const detail = await getQuestionById(row.id); resetForm(transformQuestionToForm(detail)); formDialog.visible = true } catch (error) { console.error('查询题目详情失败：', error); ElMessage.error(error.message || '查询题目详情失败') } }
async function openDetail(row) { try { const detail = await getQuestionById(row.id); detailDialog.data = detail; detailDialog.visible = true } catch (error) { console.error('查看题目详情失败：', error); ElMessage.error(error.message || '查看题目详情失败') } }
function transformQuestionToForm(question = {}) { const type = normalizeQuestionType(question.type); const answer = question.questionAnswer?.answer || ''; const correctSet = new Set(String(answer).split(',').map(item => item.trim()).filter(Boolean)); const rawChoices = Array.isArray(question.questionChoiceList) ? question.questionChoiceList : []; const choices = rawChoices.length ? rawChoices.slice().sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0)).map((choice, index) => { const sort = Number(choice.sort || index + 1); return createChoice(sort, choice.content || '', Boolean(choice.isCorrect) || correctSet.has(optionLabel(sort - 1))) }) : [createChoice(1), createChoice(2)]; return { id: question.id || null, title: question.title || '', type, isMultiple: Boolean(question.isMultiple) || correctSet.size > 1 || choices.filter(item => item.isCorrect).length > 1, categoryId: question.categoryId || null, difficulty: normalizeDifficulty(question.difficulty), score: Number(question.score || 5), analysis: question.analysis || '', answer, answerId: question.questionAnswer?.id || null, questionChoiceList: choices } }
function handleTypeChange() { form.answer = form.type === 'JUDGE' ? '正确' : ''; if (form.type === 'CHOICE' && (!form.questionChoiceList || form.questionChoiceList.length < 2)) form.questionChoiceList = [createChoice(1), createChoice(2)] }
function handleMultipleChange(value) { if (!value) { let found = false; form.questionChoiceList.forEach(choice => { if (choice.isCorrect && !found) { found = true; return } choice.isCorrect = false }) } }
function handleChoiceCorrectChange(index) { if (!form.isMultiple && form.questionChoiceList[index].isCorrect) form.questionChoiceList.forEach((choice, choiceIndex) => { choice.isCorrect = choiceIndex === index }) }
function addChoice() { if (form.questionChoiceList.length >= 8) { ElMessage.warning('最多添加 8 个选项'); return } form.questionChoiceList.push(createChoice(form.questionChoiceList.length + 1)) }
function removeChoice(index) { form.questionChoiceList.splice(index, 1); form.questionChoiceList.forEach((choice, choiceIndex) => { choice.sort = choiceIndex + 1 }) }
function validateChoiceList() { if (form.type !== 'CHOICE') return true; const hasEmpty = form.questionChoiceList.some(choice => !choice.content?.trim()); if (hasEmpty) { ElMessage.warning('请把选项内容填写完整'); return false } const correctCount = form.questionChoiceList.filter(choice => choice.isCorrect).length; if (!correctCount) { ElMessage.warning('请选择至少一个正确答案'); return false } if (!form.isMultiple && correctCount > 1) { ElMessage.warning('单选题只能设置一个正确答案'); return false } return true }
function buildPayload() { const base = { title: form.title, type: form.type, categoryId: form.categoryId, difficulty: form.difficulty, score: form.score, analysis: form.analysis, isMultiple: form.type === 'CHOICE' ? form.isMultiple : false }; if (formDialog.mode === 'edit') base.id = form.id; if (form.type === 'CHOICE') return { ...base, questionAnswer: null, questionChoiceList: form.questionChoiceList.map((choice, index) => ({ content: choice.content, isCorrect: Boolean(choice.isCorrect), sort: index + 1 })) }; return { ...base, questionAnswer: { id: formDialog.mode === 'edit' ? form.answerId : undefined, answer: form.answer }, questionChoiceList: null } }
async function submitForm() { try { await formRef.value?.validate() } catch (error) { return } if (!validateChoiceList()) return; submitLoading.value = true; try { const payload = buildPayload(); if (formDialog.mode === 'add') { await addQuestion(payload); ElMessage.success('新增题目成功') } else { await updateQuestion(payload); ElMessage.success('更新题目成功') } formDialog.visible = false; await loadQuestionPage(); await loadCategoryTree() } catch (error) { console.error('保存题目失败：', error); ElMessage.error(error.message || '保存题目失败') } finally { submitLoading.value = false } }
async function handleDelete(row) { try { await ElMessageBox.confirm(`确定删除这道题吗？\n${row.title}`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }); await deleteQuestion(row.id); ElMessage.success('删除成功'); await loadQuestionPage(); await loadCategoryTree() } catch (error) { if (error === 'cancel' || error === 'close') return; console.error('删除题目失败：', error); ElMessage.error(error.message || '删除题目失败') } }
async function handleBatchDelete() { if (!selectedRows.value.length) return; try { await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 道题吗？`, '批量删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }); for (const row of selectedRows.value) await deleteQuestion(row.id); ElMessage.success('批量删除完成'); await loadQuestionPage(); await loadCategoryTree() } catch (error) { if (error === 'cancel' || error === 'close') return; console.error('批量删除题目失败：', error); ElMessage.error(error.message || '批量删除题目失败') } }
function formatDate(value) { if (!value) return '-'; return String(value).replace('T', ' ').slice(0, 19) }
onMounted(async () => { await loadCategoryTree(); await loadQuestionPage() })
</script>

<style scoped>
.question-page{min-height:100%}.search-card,.table-card,.category-card{border-radius:8px;border:0}.search-card{margin-bottom:18px}.search-row{display:grid;grid-template-columns:minmax(260px,1fr) 240px 240px 90px 90px;gap:16px;align-items:center}.action-row{display:flex;align-items:center;gap:12px;margin-top:16px}.content-grid{display:grid;grid-template-columns:minmax(0,1fr) 290px;gap:18px}.question-title{max-width:620px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;font-weight:600}.question-sub{margin-top:6px;display:flex;gap:12px;color:#8a8f99;font-size:12px}.pager-row{display:flex;align-items:center;justify-content:space-between;margin-top:18px}.total-text{font-size:13px;color:#606266}.category-card{min-height:480px}.category-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px}.category-head h3{margin:0;font-size:18px;font-weight:800}.category-node{width:100%;display:flex;justify-content:space-between;gap:8px;padding-right:6px;font-size:14px}.category-node em{color:#6b7280;font-style:normal}.question-form :deep(.el-select),.question-form :deep(.el-tree-select),.question-form :deep(.el-input-number){width:100%}.choice-row{width:100%;display:grid;grid-template-columns:minmax(0,1fr) 110px 34px;align-items:center;gap:12px}.detail-title{font-size:20px;font-weight:800;line-height:1.6;margin-bottom:14px}.detail-tags{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:20px}.detail-section{padding:16px 0;border-top:1px solid #eef0f3}.detail-section h4{margin:0 0 10px;font-size:15px;color:#303133}.detail-section p{margin:8px 0;line-height:1.75;color:#4b5563}@media(max-width:1280px){.content-grid{grid-template-columns:1fr}.search-row{grid-template-columns:1fr 1fr}}
</style>
