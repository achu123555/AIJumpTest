<template>
  <section class="banner-page">
    <div class="page-title-block">
      <h2>轮播图管理</h2>
      <p>管理首页轮播图内容，包括添加、编辑、删除和排序功能</p>
    </div>

    <div class="toolbar-row">
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">添加轮播图</el-button>
      <el-button :icon="Refresh" @click="reloadList">刷新列表</el-button>
    </div>

    <el-card class="table-card" shadow="always">
      <el-table :data="sortedBanners" border stripe empty-text="No Data" class="banner-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="预览图" width="130">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              class="preview-img"
              fit="cover"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="210" show-overflow-tooltip />
        <el-table-column label="跳转链接" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="!row.jumpUrl" class="muted">无跳转</span>
            <a v-else class="link" :href="row.jumpUrl" target="_blank">{{ row.jumpUrl }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="185" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status ? 'warning' : 'success'"
                @click="toggleStatus(row)"
              >
                {{ row.status ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="removeBanner(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '添加轮播图'"
      width="620px"
      destroy-on-close
      align-center
      class="banner-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="banner-form">
        <el-form-item label="标题" prop="title" required>
          <el-input v-model.trim="form.title" placeholder="请输入轮播图标题" maxlength="30" show-word-limit />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model.trim="form.description"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="请输入轮播图描述"
          />
        </el-form-item>

        <el-form-item label="图片" required prop="imageUrl">
          <div class="image-area">
            <el-radio-group v-model="form.imageType" class="image-type-group" @change="handleImageTypeChange">
              <el-radio-button label="upload">上传图片到服务器</el-radio-button>
              <el-radio-button label="url">输入外部图片URL</el-radio-button>
            </el-radio-group>

            <div v-if="form.imageType === 'upload'" class="upload-box">
              <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif"
                :on-change="handleFileChange"
              >
                <el-button type="primary" :icon="UploadFilled">选择图片文件</el-button>
              </el-upload>
              <div class="upload-tip">支持 JPG、PNG、GIF 格式，文件大小不超过 5MB</div>
            </div>

            <el-input
              v-else
              v-model.trim="form.imageUrl"
              placeholder="请输入图片URL，例如：https://example.com/banner.jpg"
              clearable
            />

            <div v-if="form.imageUrl" class="image-preview-wrap">
              <img :src="form.imageUrl" alt="轮播图预览" class="dialog-preview" />
              <el-button text type="danger" @click="clearImage">清除图片</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="跳转链接" prop="jumpUrl">
          <el-input v-model.trim="form.jumpUrl" placeholder="请输入跳转链接（可选）" clearable />
          <div class="link-helper">
            <b>支持的链接格式：</b>
            <p>· 外部网站：https://www.baidu.com</p>
            <p>· 内部页面：/practice 或 /exam/list</p>
            <p>· 留空则点击无跳转效果</p>
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <span class="status-label">禁用</span>
          <el-switch v-model="form.status" class="status-switch" />
          <span class="status-label enabled">启用</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, UploadFilled } from '@element-plus/icons-vue'
import { loadBanners, saveBanners } from '../utils/storage'

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const banners = ref(loadBanners())

const emptyForm = () => ({
  id: null,
  title: '',
  description: '',
  imageType: 'upload',
  imageUrl: '',
  jumpUrl: '',
  sort: 0,
  status: true,
  createTime: ''
})

const form = reactive(emptyForm())

const sortedBanners = computed(() => {
  return [...banners.value].sort((a, b) => Number(a.sort) - Number(b.sort) || Number(a.id) - Number(b.id))
})

const validateImage = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请上传图片或输入图片URL'))
    return
  }
  callback()
}

const validateUrl = (_rule, value, callback) => {
  if (!value) {
    callback()
    return
  }

  const isExternal = /^https?:\/\//i.test(value)
  const isInternal = /^\/[\w\-/?.=&%#]*$/.test(value)

  if (!isExternal && !isInternal) {
    callback(new Error('请输入 http(s) 开头的外部链接，或 / 开头的内部路径'))
    return
  }
  callback()
}

const rules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
    { min: 2, max: 30, message: '标题长度为 2 到 30 个字符', trigger: 'blur' }
  ],
  imageUrl: [{ validator: validateImage, trigger: 'change' }],
  jumpUrl: [{ validator: validateUrl, trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'change' }]
}

const persist = () => saveBanners(banners.value)

const resetForm = () => {
  Object.assign(form, emptyForm())
  formRef.value?.clearValidate?.()
}

const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  form.sort = banners.value.length + 1
  dialogVisible.value = true
}

const openEditDialog = row => {
  isEdit.value = true
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

const reloadList = () => {
  banners.value = loadBanners()
  ElMessage.success('列表已刷新')
}

const formatTime = () => {
  const pad = num => String(num).padStart(2, '0')
  const date = new Date()
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getNextId = () => {
  if (!banners.value.length) return 1
  return Math.max(...banners.value.map(item => Number(item.id))) + 1
}

const submitForm = async () => {
  await formRef.value.validate()

  if (isEdit.value) {
    const index = banners.value.findIndex(item => item.id === form.id)
    if (index !== -1) {
      banners.value[index] = { ...form }
    }
    ElMessage.success('修改成功')
  } else {
    banners.value.push({
      ...form,
      id: getNextId(),
      createTime: formatTime()
    })
    ElMessage.success('添加成功')
  }

  persist()
  dialogVisible.value = false
}

const removeBanner = row => {
  ElMessageBox.confirm(`确定删除「${row.title}」吗？删除后不可恢复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    banners.value = banners.value.filter(item => item.id !== row.id)
    persist()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const toggleStatus = row => {
  const target = banners.value.find(item => item.id === row.id)
  if (!target) return
  target.status = !target.status
  persist()
  ElMessage.success(target.status ? '已启用' : '已禁用')
}

const handleImageTypeChange = () => {
  form.imageUrl = ''
  formRef.value?.clearValidate?.('imageUrl')
}

const clearImage = () => {
  form.imageUrl = ''
}

const handleFileChange = file => {
  const rawFile = file.raw
  if (!rawFile) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 图片')
    return
  }

  const maxSize = 5 * 1024 * 1024
  if (rawFile.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = e => {
    form.imageUrl = e.target.result
    formRef.value?.validateField?.('imageUrl')
  }
  reader.readAsDataURL(rawFile)
}
</script>

<style scoped>
.banner-page {
  min-height: 100%;
}

.page-title-block {
  margin-bottom: 18px;
}

.page-title-block h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 800;
}

.page-title-block p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.toolbar-row {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.table-card {
  border-radius: 7px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.banner-table {
  width: 100%;
}

.preview-img {
  width: 72px;
  height: 42px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
  display: block;
}

.muted {
  color: #909399;
}

.link {
  color: #303133;
}

.link:hover {
  color: #2674ff;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.action-buttons :deep(.el-button + .el-button) {
  margin-left: 0;
}

.banner-form {
  padding-right: 14px;
}

.image-area {
  width: 100%;
}

.image-type-group {
  margin-bottom: 14px;
}

.upload-box {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-tip {
  width: 100%;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.image-preview-wrap {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-preview {
  width: 180px;
  height: 90px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.link-helper {
  margin-top: 10px;
  padding: 10px 14px;
  width: 260px;
  line-height: 1.65;
  border: 1px solid #a8d5ff;
  border-radius: 6px;
  color: #123b6d;
  background: #eaf6ff;
  font-size: 13px;
}

.link-helper p {
  margin: 0;
}

.status-label {
  color: #606266;
  font-weight: 600;
}

.status-label.enabled {
  color: #2674ff;
}

.status-switch {
  margin: 0 10px;
}

.dialog-footer {
  display: inline-flex;
  gap: 10px;
}
</style>
