<template>
  <section class="category-page">
    <div class="notice-block">
      <el-icon class="notice-icon"><InfoFilled /></el-icon>
      <div>
        <h3>操作提示</h3>
        <p>管理考试题目分类，支持两级分类。一级分类作为父分类展示，后台接口当前支持在父分类下新增、编辑和删除二级分类。</p>
      </div>
    </div>

    <div class="toolbar-row">
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadCategoryTree">刷新列表</el-button>
      <el-button :icon="FolderOpened" @click="toggleExpandState">
        {{ expandAll ? '收起分类' : '展开分类' }}
      </el-button>
    </div>

    <el-card class="table-card" shadow="always">
      <el-table
        v-if="tableVisible"
        v-loading="loading"
        :data="categoryTree"
        row-key="id"
        border
        stripe
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
        empty-text="暂无分类数据"
        class="category-table"
      >
        <el-table-column prop="id" label="ID" width="90" />

        <el-table-column label="分类名称" min-width="220">
          <template #default="{ row }">
            <span class="category-name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column label="级别" width="110">
          <template #default="{ row }">
            <el-tag v-if="isParent(row)" type="primary" effect="light">一级</el-tag>
            <el-tag v-else type="success" effect="light">二级</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="90" />

        <el-table-column label="题目数" width="110">
          <template #default="{ row }">
            <el-tag v-if="Number(row.questionCount || 0) > 0" type="warning" effect="plain">
              {{ row.questionCount }} 道
            </el-tag>
            <span v-else class="muted">0 道</span>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="openViewDialog(row)">查看</el-button>

              <el-button
                size="small"
                type="primary"
                :disabled="isParent(row)"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>

              <el-button
                v-if="isParent(row)"
                size="small"
                type="success"
                @click="openAddChildDialog(row)"
              >
                添加子分类
              </el-button>

              <el-button
                size="small"
                type="danger"
                :disabled="isParent(row)"
                :loading="deletingId === row.id"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="viewVisible" title="查看分类" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="分类ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="分类名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类级别">
          {{ isParent(viewData) ? '一级分类' : '二级分类' }}
        </el-descriptions-item>
        <el-descriptions-item label="父分类">
          {{ isParent(viewData) ? '无' : getParentName(viewData.parentId) }}
        </el-descriptions-item>
        <el-descriptions-item label="排序序号">{{ viewData.sort ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ viewData.questionCount ?? 0 }} 道</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(viewData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(viewData.updateTime) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" @click="viewVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="dialogMode === 'add' ? `新增子分类（父分类：${currentParentName}）` : '编辑子分类'"
      width="560px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="categoryForm" :rules="formRules" label-width="92px" class="category-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" maxlength="40" show-word-limit placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" disabled placeholder="请选择父分类" style="width: 100%">
            <el-option
              v-for="item in parentCategories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序序号" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="9999" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened, InfoFilled, Refresh } from '@element-plus/icons-vue'
import { addCategory, deleteCategory, getCategoryTree, updateCategory } from '../api/category'

const loading = ref(false)
const categoryTree = ref([])
const tableVisible = ref(true)
const expandAll = ref(true)
const deletingId = ref(null)

const viewVisible = ref(false)
const viewData = ref({})

const formVisible = ref(false)
const dialogMode = ref('add')
const submitting = ref(false)
const formRef = ref(null)

const createEmptyForm = () => ({
  id: null,
  parentId: null,
  name: '',
  sort: 0
})

const categoryForm = reactive(createEmptyForm())

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 40, message: '分类名称不能超过 40 个字符', trigger: 'blur' }
  ],
  parentId: [{ required: true, message: '请选择父分类', trigger: 'change' }],
  sort: [{ required: true, message: '请填写排序序号', trigger: 'change' }]
}

const parentCategories = computed(() => {
  return categoryTree.value.filter(item => isParent(item))
})

const currentParentName = computed(() => {
  return getParentName(categoryForm.parentId) || '-'
})

const isParent = row => {
  return Number(row?.parentId ?? 0) === 0
}

const getParentName = parentId => {
  const parent = parentCategories.value.find(item => Number(item.id) === Number(parentId))
  return parent?.name || '-'
}

const formatDateTime = value => {
  if (!value) return '-'
  if (typeof value === 'string') {
    return value.replace('T', ' ').slice(0, 19)
  }
  return String(value)
}

const sortTree = list => {
  return [...(list || [])]
    .sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0) || Number(a.id || 0) - Number(b.id || 0))
    .map(item => ({
      ...item,
      children: item.children?.length ? sortTree(item.children) : []
    }))
}

const loadCategoryTree = async () => {
  loading.value = true
  try {
    const list = await getCategoryTree()
    categoryTree.value = sortTree(Array.isArray(list) ? list : [])
  } catch (error) {
    console.error('获取分类树失败：', error)
    categoryTree.value = []
    ElMessage.error(error.message || '获取分类列表失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const refreshExpandState = async () => {
  tableVisible.value = false
  await nextTick()
  tableVisible.value = true
}

const toggleExpandState = async () => {
  expandAll.value = !expandAll.value
  await refreshExpandState()
}

const resetForm = () => {
  Object.assign(categoryForm, createEmptyForm())
  formRef.value?.clearValidate?.()
}

const openViewDialog = row => {
  viewData.value = { ...row }
  viewVisible.value = true
}

const openAddChildDialog = parent => {
  dialogMode.value = 'add'
  resetForm()
  categoryForm.parentId = parent.id
  categoryForm.sort = 0
  formVisible.value = true
}

const openEditDialog = row => {
  if (isParent(row)) {
    ElMessage.warning('当前后端接口只支持编辑二级分类')
    return
  }

  dialogMode.value = 'edit'
  resetForm()
  Object.assign(categoryForm, {
    id: row.id,
    parentId: row.parentId,
    name: row.name || '',
    sort: Number(row.sort || 0)
  })
  formVisible.value = true
}

const buildPayload = () => {
  const payload = {
    parentId: categoryForm.parentId,
    name: categoryForm.name.trim(),
    sort: Number(categoryForm.sort || 0)
  }

  if (dialogMode.value === 'edit') {
    payload.id = categoryForm.id
  }

  return payload
}

const submitForm = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = buildPayload()

    if (dialogMode.value === 'add') {
      await addCategory(payload)
      ElMessage.success('新增子分类成功')
    } else {
      await updateCategory(payload)
      ElMessage.success('更新子分类成功')
    }

    formVisible.value = false
    await loadCategoryTree()
  } catch (error) {
    console.error('保存分类失败：', error)
    ElMessage.error(error.message || '保存分类失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async row => {
  if (isParent(row)) {
    ElMessage.warning('一级分类不可删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」这个分类吗？删除前请确认该分类下没有关联试题。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    deletingId.value = row.id
    await deleteCategory(row.id)
    ElMessage.success('删除分类成功')
    await loadCategoryTree()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return

    console.error('删除分类失败：', error)
    ElMessage.error(error.message || '删除分类失败')
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped>
.category-page {
  min-height: 100%;
}

.notice-block {
  margin-bottom: 18px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  color: #303133;
}

.notice-icon {
  margin-top: 3px;
  font-size: 30px;
  color: #606266;
}

.notice-block h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
}

.notice-block p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.table-card {
  border-radius: 7px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.category-table {
  width: 100%;
}

.category-name {
  font-weight: 600;
  color: #303133;
}

.muted {
  color: #909399;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.category-form {
  padding-right: 8px;
}

@media (max-width: 900px) {
  .notice-block {
    padding: 0 4px;
  }
}
</style>
