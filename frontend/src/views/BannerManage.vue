<template>
  <section class="banner-page">
    <div class="page-title-block">
      <div>
        <h2>轮播图管理</h2>
        <p>管理学生端首页轮播图，已接入新增、编辑、删除、上传图片和状态切换接口</p>
      </div>
      <el-tag type="success" effect="light">后端接口已联通</el-tag>
    </div>

    <div class="toolbar-row">
      <el-button type="primary" :icon="Plus" @click="openAddDialog">添加轮播图</el-button>
      <el-button :icon="Refresh" :loading="loading" @click="loadBannerList">刷新列表</el-button>
    </div>

    <el-card class="table-card" shadow="always">
      <el-table
        v-loading="loading"
        :data="sortedBanners"
        border
        stripe
        empty-text="No Data"
        class="banner-table"
      >
        <el-table-column prop="id" label="ID" width="70" />

        <el-table-column label="预览图" width="130">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="getImageSrc(row.imageUrl)"
              class="preview-img"
              fit="cover"
              :preview-src-list="[getImageSrc(row.imageUrl)]"
              preview-teleported
            />
            <span v-else class="muted">暂无</span>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="210" show-overflow-tooltip />

        <el-table-column label="跳转链接" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="!row.linkUrl" class="muted">无跳转</span>
            <a v-else-if="isExternalLink(row.linkUrl)" class="link" :href="row.linkUrl" target="_blank">
              {{ row.linkUrl }}
            </a>
            <span v-else class="link internal-link">{{ row.linkUrl }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="90" />

        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="isActive(row)" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
              <el-button
                size="small"
                :type="isActive(row) ? 'warning' : 'success'"
                :loading="togglingId === row.id"
                @click="handleToggleStatus(row)"
              >
                {{ isActive(row) ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                :icon="Delete"
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '添加轮播图' : '编辑轮播图'"
      width="640px"
      destroy-on-close
      @closed="resetBannerForm"
    >
      <el-form
        ref="formRef"
        v-loading="dialogLoading"
        :model="bannerForm"
        :rules="formRules"
        label-width="92px"
        class="banner-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" maxlength="80" show-word-limit placeholder="请输入轮播图标题" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="bannerForm.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入轮播图描述，可为空"
          />
        </el-form-item>

        <el-form-item label="图片" prop="imageUrl">
          <div class="image-form-block">
            <el-upload
              class="banner-uploader"
              accept="image/*"
              :show-file-list="false"
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
            >
              <div v-if="bannerForm.imageUrl" class="uploaded-preview">
                <img :src="getImageSrc(bannerForm.imageUrl)" alt="轮播图预览" />
                <div class="preview-cover">
                  <el-icon><UploadFilled /></el-icon>
                  <span>重新上传</span>
                </div>
              </div>
              <div v-else class="upload-placeholder" v-loading="imageUploading">
                <el-icon><UploadFilled /></el-icon>
                <span>上传图片到服务器</span>
              </div>
            </el-upload>

            <el-input
              v-model="bannerForm.imageUrl"
              clearable
              placeholder="图片上传成功后会自动填入，也可以手动粘贴外部图片 URL"
            />
            <p class="form-tip">支持 JPG、PNG、GIF 等图片。上传接口：POST /api/banners/upload-image</p>
          </div>
        </el-form-item>

        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input
            v-model="bannerForm.linkUrl"
            clearable
            placeholder="可为空。外部链接如 https://www.baidu.com，内部路由如 /exam/list"
          />
        </el-form-item>

        <div class="form-two-cols">
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="bannerForm.sortOrder" :min="0" :max="9999" controls-position="right" />
          </el-form-item>

          <el-form-item label="状态" prop="isActive">
            <el-switch
              v-model="bannerForm.isActive"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBannerForm">
          {{ dialogMode === 'add' ? '确定添加' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh, UploadFilled } from '@element-plus/icons-vue'
import {
  addBanner,
  deleteBanner,
  getBannerById,
  getBannerList,
  toggleBannerStatus,
  updateBanner,
  uploadBannerImage
} from '../api/banner'

const loading = ref(false)
const banners = ref([])
const togglingId = ref(null)
const deletingId = ref(null)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const dialogLoading = ref(false)
const submitting = ref(false)
const imageUploading = ref(false)
const formRef = ref(null)

const createEmptyForm = () => ({
  id: null,
  title: '',
  description: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  isActive: true
})

const bannerForm = reactive(createEmptyForm())

const formRules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
    { min: 2, max: 80, message: '标题长度建议在 2 到 80 个字符之间', trigger: 'blur' }
  ],
  imageUrl: [{ required: true, message: '请上传图片或填写图片 URL', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请填写排序值', trigger: 'change' }]
}

const sortedBanners = computed(() => {
  return [...banners.value].sort((a, b) => {
    return Number(a.sortOrder || 0) - Number(b.sortOrder || 0) || Number(a.id || 0) - Number(b.id || 0)
  })
})

const loadBannerList = async () => {
  loading.value = true
  try {
    const list = await getBannerList()
    banners.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('获取轮播图列表失败：', error)
    banners.value = []
    ElMessage.error(error.message || '获取轮播图列表失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const isActive = row => {
  return row?.isActive === 1 || row?.isActive === true || row?.isActive === '1'
}

const isExternalLink = link => {
  return /^https?:\/\//i.test(link || '')
}

const formatDateTime = value => {
  if (!value) return '-'

  // Spring Boot 的 LocalDateTime 常见返回值：2026-06-09T13:47:00
  if (typeof value === 'string') {
    return value.replace('T', ' ').slice(0, 19)
  }

  return String(value)
}

const getImageSrc = imageUrl => {
  if (!imageUrl) return ''

  // OSS、外部图片、Base64、Blob 直接使用
  if (/^(https?:|data:|blob:)/i.test(imageUrl)) {
    return imageUrl
  }

  // 数据库存的是相对路径时，保持相对路径访问
  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
}

const normalizeBanner = banner => {
  return {
    id: banner?.id ?? null,
    title: banner?.title ?? '',
    description: banner?.description ?? '',
    imageUrl: banner?.imageUrl ?? '',
    linkUrl: banner?.linkUrl ?? '',
    sortOrder: Number(banner?.sortOrder ?? 0),
    isActive: isActive(banner)
  }
}

const resetBannerForm = () => {
  Object.assign(bannerForm, createEmptyForm())
  formRef.value?.clearValidate?.()
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  resetBannerForm()
  dialogVisible.value = true
}

const openEditDialog = async row => {
  dialogMode.value = 'edit'
  resetBannerForm()
  dialogVisible.value = true
  dialogLoading.value = true

  try {
    const detail = await getBannerById(row.id)
    Object.assign(bannerForm, normalizeBanner(detail || row))
  } catch (error) {
    console.error('查询轮播图详情失败，使用列表数据回填：', error)
    Object.assign(bannerForm, normalizeBanner(row))
    ElMessage.warning('轮播图详情接口查询失败，已使用列表数据回填')
  } finally {
    dialogLoading.value = false
  }
}

const beforeImageUpload = file => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 <= 10

  if (!isImage) {
    ElMessage.warning('请选择图片文件')
    return false
  }

  if (!isLt10M) {
    ElMessage.warning('图片大小不能超过 10MB')
    return false
  }

  return true
}

const handleImageUpload = async options => {
  imageUploading.value = true
  try {
    const imageUrl = await uploadBannerImage(options.file)
    bannerForm.imageUrl = imageUrl || ''
    ElMessage.success('图片上传成功')
    options.onSuccess?.(imageUrl)
  } catch (error) {
    console.error('图片上传失败：', error)
    ElMessage.error(error.message || '图片上传失败')
    options.onError?.(error)
  } finally {
    imageUploading.value = false
  }
}

const buildPayload = () => {
  const payload = {
    title: bannerForm.title.trim(),
    description: bannerForm.description?.trim() || '',
    imageUrl: bannerForm.imageUrl?.trim() || '',
    linkUrl: bannerForm.linkUrl?.trim() || '',
    sortOrder: Number(bannerForm.sortOrder || 0),
    isActive: Boolean(bannerForm.isActive)
  }

  if (dialogMode.value === 'edit') {
    payload.id = bannerForm.id
  }

  return payload
}

const submitBannerForm = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = buildPayload()

    if (dialogMode.value === 'add') {
      await addBanner(payload)
      ElMessage.success('添加轮播图成功')
    } else {
      await updateBanner(payload)
      ElMessage.success('更新轮播图成功')
    }

    dialogVisible.value = false
    await loadBannerList()
  } catch (error) {
    console.error('保存轮播图失败：', error)
    ElMessage.error(error.message || '保存轮播图失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async row => {
  const targetStatus = !isActive(row)
  const actionName = targetStatus ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${actionName}这个轮播图吗？`, '状态切换确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    togglingId.value = row.id
    await toggleBannerStatus(row.id, targetStatus)
    ElMessage.success(`${actionName}成功`)

    // 重新拉取后端最新列表，避免前端状态和数据库不一致
    await loadBannerList()
  } catch (error) {
    // Element Plus 取消弹窗时会进入 catch，这种情况不用报错
    if (error === 'cancel' || error === 'close') {
      return
    }

    console.error(`${actionName}轮播图失败：`, error)
    ElMessage.error(error.message || `${actionName}轮播图失败`)
  } finally {
    togglingId.value = null
  }
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title || row.id}」这张轮播图吗？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    deletingId.value = row.id
    await deleteBanner(row.id)
    ElMessage.success('删除轮播图成功')
    await loadBannerList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }

    console.error('删除轮播图失败：', error)
    ElMessage.error(error.message || '删除轮播图失败')
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  loadBannerList()
})
</script>

<style scoped>
.banner-page {
  min-height: 100%;
}

.page-title-block {
  margin-bottom: 18px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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
  text-decoration: none;
}

.link:hover {
  color: #2674ff;
}

.internal-link {
  color: #606266;
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
  padding-right: 8px;
}

.image-form-block {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.banner-uploader {
  width: 100%;
}

.upload-placeholder,
.uploaded-preview {
  width: 260px;
  height: 128px;
  border: 1px dashed #bfc6d1;
  border-radius: 10px;
  background: #f7f9fc;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  color: #606266;
  cursor: pointer;
  transition: 0.2s ease;
}

.upload-placeholder:hover,
.uploaded-preview:hover {
  border-color: #2674ff;
  color: #2674ff;
}

.upload-placeholder .el-icon {
  font-size: 28px;
}

.uploaded-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-cover {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.uploaded-preview:hover .preview-cover {
  opacity: 1;
}

.form-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.form-two-cols {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 20px;
}

@media (max-width: 720px) {
  .page-title-block {
    display: block;
  }

  .form-two-cols {
    grid-template-columns: 1fr;
  }

  .upload-placeholder,
  .uploaded-preview {
    width: 100%;
  }
}
</style>
