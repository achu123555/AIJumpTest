<template>
  <section class="start-page">
    <el-card class="start-card" shadow="always">
      <h1>开始考试</h1>
      <p class="sub-title">请填写您的信息开始考试</p>

      <div v-loading="loading" class="paper-box">
        <h3>{{ paper.name || '加载中...' }}</h3>
        <p>{{ paper.description || '暂无描述' }}</p>
        <div class="paper-meta">
          <span><el-icon><Document /></el-icon> 题目数量：{{ paper.questionCount || 0 }} 道</span>
          <span><el-icon><Trophy /></el-icon> 总分：{{ paper.totalScore || 0 }} 分</span>
          <span><el-icon><Timer /></el-icon> 考试时长：{{ paper.duration || 0 }} 分钟</span>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="start-form">
        <el-form-item label="考生姓名" prop="studentName">
          <el-input v-model.trim="form.studentName" maxlength="20" show-word-limit placeholder="请输入考生姓名" />
        </el-form-item>
      </el-form>

      <el-button type="primary" class="start-button" :loading="submitting" @click="handleStart">
        开始考试
      </el-button>

      <el-alert class="rule-box" type="warning" :closable="false" show-icon>
        <template #title>考试规则</template>
        <ul>
          <li>请确保网络连接稳定</li>
          <li>考试过程中请勿切换窗口或刷新页面</li>
          <li>考试时间到后将自动交卷</li>
          <li>提交后将无法修改答案</li>
        </ul>
      </el-alert>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Timer, Trophy } from '@element-plus/icons-vue'
import { getExamPaperList, startExam } from '../api/exam'
import { getAuthUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const paper = reactive({})
const authUser = getAuthUser()
const form = reactive({ studentName: authUser?.role === 'STUDENT' ? (authUser?.nickname || authUser?.username || '') : '' })

const rules = {
  studentName: [
    { required: true, message: '请输入考生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 个字符', trigger: 'blur' }
  ]
}

async function loadPaper() {
  loading.value = true
  try {
    // 学生端不直接调用管理端 /api/papers/{id}，而是从已发布试卷列表中取当前试卷。
    const list = await getExamPaperList()
    const detail = Array.isArray(list) ? list.find(item => Number(item.id) === Number(route.params.paperId)) : null
    if (!detail) {
      ElMessage.error('该试卷不存在或未发布')
      router.replace('/exam/list')
      return
    }
    Object.assign(paper, detail)
  } catch (error) {
    ElMessage.error(error.message || '加载试卷失败')
  } finally {
    loading.value = false
  }
}

async function handleStart() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const record = await startExam({
      paperId: Number(route.params.paperId),
      studentName: form.studentName
    })
    ElMessage.success('考试已开始')
    router.push(`/exam/take/${record.id}`)
  } catch (error) {
    ElMessage.error(error.message || '开始考试失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadPaper)
</script>

<style scoped>
.start-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1f4bff, #30156f);
  display: flex;
  align-items: center;
  justify-content: center;
}

.start-card {
  width: 520px;
  border-radius: 14px;
  padding: 18px 22px;
}

.start-card h1 {
  text-align: center;
  margin: 6px 0 8px;
}

.sub-title {
  text-align: center;
  margin: 0 0 24px;
  color: #606266;
}

.paper-box {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 18px;
  margin-bottom: 22px;
}

.paper-box h3 {
  margin: 0 0 10px;
}

.paper-box p {
  margin: 0 0 14px;
}

.paper-meta {
  display: flex;
  gap: 16px;
  color: #606266;
  font-size: 13px;
}

.paper-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.start-button {
  width: 100%;
  margin-bottom: 22px;
}

.rule-box ul {
  margin: 8px 0 0;
  padding-left: 18px;
  line-height: 2;
}
</style>
