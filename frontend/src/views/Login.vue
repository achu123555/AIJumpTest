<template>
  <section class="login-page">
    <div class="login-shell">
      <aside class="login-visual">
        <div class="visual-badge">AIJumpTest</div>
        <h1>在线学习与考试平台</h1>
        <p>统一账号登录，管理员进入后台管理，学生进入题库练习、在线考试和排行榜。</p>
        <div class="visual-grid">
          <div><strong>安全</strong><span>JWT登录鉴权</span></div>
          <div><strong>考试</strong><span>在线作答评分</span></div>
          <div><strong>排行</strong><span>成绩榜单展示</span></div>
        </div>
      </aside>

      <main class="login-card">
        <div class="card-head">
          <h2>{{ form.role === 'ADMIN' ? '管理员登录' : (mode === 'login' ? '学生登录' : '学生注册') }}</h2>
          <p>{{ form.role === 'ADMIN' ? '使用管理员账号进入管理后台' : '学生账号用于考试记录和排行榜身份识别' }}</p>
        </div>

        <el-radio-group v-model="form.role" class="role-tabs" @change="handleRoleChange">
          <el-radio-button label="STUDENT">学生用户</el-radio-button>
          <el-radio-button label="ADMIN">管理员</el-radio-button>
        </el-radio-group>

        <div v-if="form.role === 'STUDENT'" class="mode-switch">
          <el-button :type="mode === 'login' ? 'primary' : 'default'" plain @click="mode = 'login'">已有账号</el-button>
          <el-button :type="mode === 'register' ? 'primary' : 'default'" plain @click="mode = 'register'">注册学生账号</el-button>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
          <el-form-item :label="form.role === 'ADMIN' ? '管理员账号' : '学生账号'" prop="username">
            <el-input
              v-model.trim="form.username"
              size="large"
              clearable
              :placeholder="form.role === 'ADMIN' ? '例如：admin' : '请输入登录账号'"
            />
          </el-form-item>

          <el-form-item v-if="form.role === 'STUDENT' && mode === 'register'" label="考生姓名 / 昵称" prop="nickname">
            <el-input v-model.trim="form.nickname" size="large" clearable placeholder="例如：张三" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
          </el-form-item>

          <el-alert
            v-if="form.role === 'ADMIN'"
            title="管理员账号：admin / admin123 "
            type="info"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else-if="mode === 'login'"
            title="学生账号：student / student123，也可以注册自己的学生账号。"
            type="info"
            :closable="false"
            show-icon
          />

          <el-button type="primary" size="large" class="login-btn" :loading="submitting" @click="handleSubmit">
            {{ submitText }}
          </el-button>
        </el-form>
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { setAuthUser } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)
const mode = ref('login')

const form = reactive({
  role: 'STUDENT',
  username: '',
  nickname: '',
  password: ''
})

const submitText = computed(() => {
  if (form.role === 'ADMIN') return '进入管理后台'
  return mode.value === 'login' ? '进入学生端' : '注册并进入学生端'
})

const rules = computed(() => ({
  username: [
    { required: true, message: form.role === 'ADMIN' ? '请输入管理员账号' : '请输入学生账号', trigger: 'blur' },
    { min: 3, max: 32, message: '账号长度为 3-32 个字符', trigger: 'blur' }
  ],
  nickname: form.role === 'STUDENT' && mode.value === 'register'
    ? [{ required: true, message: '请输入考生姓名/昵称', trigger: 'blur' }]
    : [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 个字符', trigger: 'blur' }
  ]
}))

function handleRoleChange() {
  mode.value = 'login'
  formRef.value?.clearValidate?.()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      role: form.role
    }

    const user = form.role === 'STUDENT' && mode.value === 'register'
      ? await register({ username: form.username, password: form.password, nickname: form.nickname })
      : await login(payload)

    setAuthUser(user)
    ElMessage.success('登录成功')

    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
    } else {
      router.replace(user.role === 'ADMIN' ? '/admin/question-manage' : '/home')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px;
  background:
    radial-gradient(circle at 8% 12%, rgba(59, 130, 246, 0.24), transparent 28%),
    radial-gradient(circle at 88% 88%, rgba(14, 165, 233, 0.18), transparent 30%),
    linear-gradient(135deg, #eef4ff 0%, #f8fafc 48%, #ecfeff 100%);
}

.login-shell {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 420px;
  border-radius: 28px;
  overflow: hidden;
  background: rgba(255,255,255,.75);
  box-shadow: 0 30px 80px rgba(15, 23, 42, .18);
  border: 1px solid rgba(255,255,255,.9);
}

.login-visual {
  min-height: 590px;
  padding: 62px;
  color: #fff;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, .9), rgba(30, 64, 175, .82)),
    repeating-linear-gradient(45deg, rgba(255,255,255,.08) 0 2px, transparent 2px 18px);
}

.visual-badge {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255,255,255,.14);
  border: 1px solid rgba(255,255,255,.24);
  margin-bottom: 120px;
}

.login-visual h1 {
  font-size: 42px;
  margin: 0 0 18px;
}

.login-visual p {
  width: 78%;
  font-size: 17px;
  line-height: 1.8;
  color: rgba(255,255,255,.82);
}

.visual-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-top: 48px;
}

.visual-grid div {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255,255,255,.12);
  border: 1px solid rgba(255,255,255,.16);
}

.visual-grid strong,
.visual-grid span { display: block; }
.visual-grid strong { font-size: 22px; }
.visual-grid span { margin-top: 6px; color: rgba(255,255,255,.72); }

.login-card {
  padding: 46px 42px;
  background: rgba(255,255,255,.94);
}

.card-head h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.card-head p {
  margin: 0;
  color: #64748b;
}

.role-tabs {
  width: 100%;
  margin: 28px 0 16px;
}

.role-tabs :deep(.el-radio-button) { flex: 1; }
.role-tabs :deep(.el-radio-button__inner) { width: 100%; white-space: nowrap; }

.mode-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.mode-switch .el-button {
  margin-left: 0;
}

.login-form :deep(.el-alert) {
  margin-bottom: 18px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

@media (max-width: 900px) {
  .login-shell { grid-template-columns: 1fr; }
  .login-visual { display: none; }
}
</style>
