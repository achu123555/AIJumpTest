<template>
  <main class="home-page">
    <header class="top-bar">
      <button class="brand" type="button" @click="router.push('/home')">
        <span class="brand-mark">Test</span>
        <span class="brand-text">AIJumpTest</span>
      </button>

      <nav class="top-actions">
        <el-button text @click="goProtected('/questions')">题库</el-button>
        <el-button text @click="goProtected('/exam/list')">考试</el-button>
        <el-button text @click="goProtected('/ranking')">排行榜</el-button>
        <el-button v-if="admin" plain @click="router.push('/admin/question-manage')">管理后台</el-button>

        <template v-if="!authUser?.token">
          <el-button plain @click="openAuthDialog('login')">登录</el-button>
          <el-button type="primary" @click="openAuthDialog('register')">注册</el-button>
        </template>

        <el-dropdown v-else trigger="click" @command="handleUserCommand">
          <button class="user-chip" type="button">
            <span>{{ authUser?.nickname || authUser?.username || '用户' }}</span>
            <small>{{ admin ? '管理员' : '学生' }}</small>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="admin" command="admin">进入管理后台</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </nav>
    </header>

    <section class="hero-shell">
      <div class="hero-card" :style="heroBackgroundStyle">
        <div class="hero-copy">
          <p class="eyebrow">在线考试平台</p>
          <h1>让练习、考试和成绩反馈更清楚。</h1>
          <p class="hero-desc">
            面向课程测试与题库训练，支持学生在线作答、成绩查看，管理员维护题目、试卷和考试记录。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="goProtected('/exam/list')">开始考试</el-button>
            <el-button size="large" @click="goProtected('/ranking')">查看排行榜</el-button>
          </div>
        </div>

        <aside class="overview-card">
          <div class="overview-head">
            <span>今日面板</span>
            <strong>{{ today.full }}</strong>
          </div>
          <div class="overview-list">
            <div class="overview-item">
              <b>题库练习</b>
              <span>按分类巩固知识点</span>
            </div>
            <div class="overview-item">
              <b>限时考试</b>
              <span>按试卷完成作答</span>
            </div>
            <div class="overview-item">
              <b>成绩反馈</b>
              <span>查看分数与排名</span>
            </div>
          </div>
          <p v-if="authUser?.token" class="overview-note">
            当前登录：{{ authUser.nickname || authUser.username }}。可以先完成一套试卷，再回到题库补练薄弱分类。
          </p>
          <p v-else class="overview-note">
            登录后可以参加考试、查看题库解析和成绩排行；管理员可进入后台维护数据。
          </p>
        </aside>
      </div>
    </section>

    <section class="quick-section">
      <article v-for="item in quickItems" :key="item.title" class="quick-card" @click="goProtected(item.path)">
        <div class="quick-icon" :class="item.colorClass"><el-icon><component :is="item.icon" /></el-icon></div>
        <div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </article>
    </section>

    <section v-if="displayBanners.length" class="notice-section">
      <div class="section-head compact">
        <div>
          <h2>推荐内容</h2>
          <p>最新公告、课程安排和学习提醒。</p>
        </div>
      </div>
      <el-carousel class="notice-carousel" height="168px" arrow="hover" indicator-position="outside" :interval="4600">
        <el-carousel-item v-for="item in displayBanners" :key="item.id || item.title">
          <article class="notice-card" :class="{ clickable: !!item.linkUrl }" @click="handleBannerClick(item)">
            <div class="notice-main">
              <span class="notice-label">推荐</span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description || '查看最新课程与考试安排。' }}</p>
            </div>
            <img v-if="item.imageUrl" :src="getImageSrc(item.imageUrl)" :alt="item.title" />
          </article>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="popular-section">
      <div class="section-head">
        <div>
          <h2>热门题目</h2>
          <p>大家常看的题目，点击即可快速作答并查看解析。</p>
        </div>
        <el-button text @click="goProtected('/questions')">查看全部题目 →</el-button>
      </div>

      <el-skeleton v-if="popularLoading" animated :rows="4" />
      <div v-else-if="popularQuestions.length" class="popular-grid">
        <article v-for="item in popularQuestions" :key="item.id" class="popular-card" @click="openQuestionPopup(item)">
          <div class="tag-line">
            <el-tag size="small" :type="getTypeTag(item.type)" effect="light">{{ getTypeLabel(item.type) }}</el-tag>
            <el-tag size="small" :type="getDifficultyTag(item.difficulty)" effect="light">{{ getDifficultyLabel(item.difficulty) }}</el-tag>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.score || 0 }} 分 · 点击作答</p>
        </article>
      </div>
      <el-empty v-else description="暂无热门题目" />
    </section>

    <el-dialog
      v-model="authDialogVisible"
      :title="mode === 'login' ? '登录账号' : '学生注册'"
      width="460px"
      class="auth-dialog"
      append-to-body
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <div class="auth-switch">
        <button :class="{ active: mode === 'login' }" type="button" @click="switchMode('login')">登录</button>
        <button :class="{ active: mode === 'register' }" type="button" @click="switchMode('register')">注册</button>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleSubmit">
        <el-form-item v-if="mode === 'login'" label="身份">
          <el-radio-group v-model="form.role" class="dialog-role-tabs" @change="handleRoleChange">
            <el-radio-button label="STUDENT">学生</el-radio-button>
            <el-radio-button label="ADMIN">管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" size="large" clearable placeholder="请输入账号">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="mode === 'register'" label="姓名/昵称" prop="nickname">
          <el-input v-model.trim="form.nickname" size="large" clearable placeholder="例如：张三">
            <template #prefix><el-icon><EditPen /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <p class="auth-tip">
          <template v-if="mode === 'login' && form.role === 'ADMIN'">管理员演示：admin / admin123</template>
          <template v-else-if="mode === 'login'">学生演示：student / student123</template>
          <template v-else>注册账号默认是学生角色。</template>
        </p>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="authDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ submitText }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 热门题目作答弹窗 -->
    <el-dialog
      v-model="questionPopupVisible"
      :title="questionDetail ? '题目详情' : '加载中...'"
      width="680px"
      class="question-popup-dialog"
      append-to-body
      destroy-on-close
      @closed="resetQuestionPopup"
    >
      <div v-if="popupLoading" class="popup-loading">
        <el-skeleton animated :rows="6" />
      </div>

      <template v-else-if="questionDetail">
        <div class="popup-question-head">
          <div class="popup-tags">
            <el-tag :type="getTypeTag(questionDetail.type)">{{ getTypeLabel(questionDetail.type) }}</el-tag>
            <el-tag :type="getDifficultyTag(questionDetail.difficulty)">{{ getDifficultyLabel(questionDetail.difficulty) }}</el-tag>
            <el-tag type="warning">{{ questionDetail.score || 0 }} 分</el-tag>
          </div>
          <h3 class="popup-title">{{ questionDetail.title }}</h3>
        </div>

        <!-- 选择题 -->
        <div v-if="popupIsChoice" class="popup-answer-area">
          <el-checkbox-group v-if="popupIsMultiple" v-model="popupSelectedAnswers" :disabled="popupShowResult">
            <label
              v-for="(choice, index) in popupChoices"
              :key="choice.id || index"
              class="popup-option-line"
              :class="getPopupOptionClass(index)"
            >
              <el-checkbox :label="popupOptionLabel(index)">
                <span class="popup-option-label">{{ popupOptionLabel(index) }}.</span>
                <span>{{ choice.content }}</span>
              </el-checkbox>
            </label>
          </el-checkbox-group>

          <el-radio-group v-else v-model="popupSelectedAnswer" :disabled="popupShowResult">
            <label
              v-for="(choice, index) in popupChoices"
              :key="choice.id || index"
              class="popup-option-line"
              :class="getPopupOptionClass(index)"
            >
              <el-radio :label="popupOptionLabel(index)">
                <span class="popup-option-label">{{ popupOptionLabel(index) }}.</span>
                <span>{{ choice.content }}</span>
              </el-radio>
            </label>
          </el-radio-group>
        </div>

        <!-- 判断题 -->
        <div v-else-if="popupIsJudge" class="popup-answer-area">
          <el-radio-group v-model="popupSelectedAnswer" :disabled="popupShowResult">
            <el-radio-button label="正确" />
            <el-radio-button label="错误" />
          </el-radio-group>
        </div>

        <!-- 简答题 -->
        <div v-else class="popup-answer-area">
          <el-input
            v-model="popupShortAnswer"
            type="textarea"
            :rows="4"
            :disabled="popupShowResult"
            placeholder="写下你的答案，点击确定后查看参考答案和解析"
          />
        </div>

        <!-- 结果展示 -->
        <div v-if="popupShowResult" class="popup-result-box">
          <div class="popup-result-status" :class="popupIsCorrect ? 'right' : 'wrong'">
            <strong>{{ popupIsCorrect ? '✓ 回答正确' : '✗ 回答错误' }}</strong>
            <span>{{ popupIsCorrect ? '不错，继续保持！' : '别灰心，看看解析再试试~' }}</span>
          </div>
          <div class="popup-result-section">
            <h4>参考答案</h4>
            <p>{{ popupCorrectAnswer || '暂无参考答案' }}</p>
          </div>
          <div class="popup-result-section">
            <h4>题目解析</h4>
            <p>{{ questionDetail.analysis || '暂无解析' }}</p>
          </div>
        </div>
      </template>

      <template #footer>
        <div class="popup-footer">
          <el-button @click="questionPopupVisible = false">关闭</el-button>
          <el-button v-if="!popupShowResult" type="primary" @click="popupConfirmAnswer">确定</el-button>
          <el-button v-else type="warning" @click="resetPopupAnswerState">重新作答</el-button>
        </div>
      </template>
    </el-dialog>
  </main>
</template>


<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentChecked, EditPen, Lock, Trophy, User } from '@element-plus/icons-vue'
import { getActiveBanners } from '../api/banner'
import { login, register } from '../api/auth'
import { getPopularQuestions, getQuestionById } from '../api/question'
import { getDifficultyLabel, getDifficultyTag, getTypeLabel, getTypeTag, normalizeQuestionType, optionLabel } from '../utils/questionMeta'
import { clearAuthUser, getAuthUser, isAdmin, setAuthUser } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const bannerLoading = ref(false)
const activeBanners = ref([])
const popularLoading = ref(false)
const popularQuestions = ref([])

// --- 热门题目作答弹窗状态 ---
const questionPopupVisible = ref(false)
const popupLoading = ref(false)
const questionDetail = ref(null)
const popupSelectedAnswer = ref('')
const popupSelectedAnswers = ref([])
const popupShortAnswer = ref('')
const popupShowResult = ref(false)
const popupIsCorrect = ref(false)

const popupCurrentType = computed(() => normalizeQuestionType(questionDetail.value?.type))
const popupIsChoice = computed(() => popupCurrentType.value === 'CHOICE')
const popupIsJudge = computed(() => popupCurrentType.value === 'JUDGE')
const popupChoices = computed(() => {
  const list = Array.isArray(questionDetail.value?.questionChoiceList) ? questionDetail.value.questionChoiceList : []
  return list.slice().sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0))
})
const popupCorrectAnswer = computed(() => {
  const answer = questionDetail.value?.questionAnswer?.answer
  if (answer) return answer
  const labels = popupChoices.value
    .map((choice, index) => (choice.isCorrect ? optionLabel(index) : ''))
    .filter(Boolean)
  return labels.join(',')
})
const popupCorrectParts = computed(() => {
  return String(popupCorrectAnswer.value || '')
    .split(/[，,]/)
    .map(item => item.trim().toUpperCase())
    .filter(Boolean)
    .sort()
})
const popupIsMultiple = computed(() => {
  return popupCorrectParts.value.length > 1 || popupChoices.value.filter(item => item.isCorrect).length > 1
})

function popupOptionLabel(index) {
  return optionLabel(index)
}

function popupHasAnswered() {
  if (popupIsChoice.value && popupIsMultiple.value) return popupSelectedAnswers.value.length > 0
  if (popupIsChoice.value || popupIsJudge.value) return Boolean(popupSelectedAnswer.value)
  return Boolean(popupShortAnswer.value.trim())
}

function popupConfirmAnswer() {
  if (!popupHasAnswered()) {
    ElMessage.warning('请先作答，再点击确定')
    return
  }

  if (popupIsChoice.value && popupIsMultiple.value) {
    const selected = popupSelectedAnswers.value.map(item => String(item).trim().toUpperCase()).sort().join(',')
    popupIsCorrect.value = selected === popupCorrectParts.value.join(',')
  } else if (popupIsChoice.value) {
    popupIsCorrect.value = String(popupSelectedAnswer.value).trim().toUpperCase() === popupCorrectParts.value[0]
  } else if (popupIsJudge.value) {
    const normalize = (text) => {
      const t = String(text || '').trim().toLowerCase()
      if (['正确', '对', 'true', 't', '1', 'yes', '是'].includes(t)) return '正确'
      if (['错误', '错', 'false', 'f', '0', 'no', '否'].includes(t)) return '错误'
      return t
    }
    popupIsCorrect.value = normalize(popupSelectedAnswer.value) === normalize(popupCorrectAnswer.value)
  } else {
    const normalize = (text) => String(text || '').trim().replace(/\s+/g, '').toLowerCase()
    popupIsCorrect.value = normalize(popupShortAnswer.value) === normalize(popupCorrectAnswer.value)
  }

  popupShowResult.value = true
}

function getPopupOptionClass(index) {
  if (!popupShowResult.value) return ''
  const label = optionLabel(index)
  const isCorrectOption = popupCorrectParts.value.includes(label)
  const isSelected = popupIsMultiple.value ? popupSelectedAnswers.value.includes(label) : popupSelectedAnswer.value === label
  return {
    'is-correct': isCorrectOption,
    'is-wrong': isSelected && !isCorrectOption
  }
}

async function openQuestionPopup(item) {
  questionPopupVisible.value = true
  popupLoading.value = true
  questionDetail.value = null
  resetPopupAnswerState()

  try {
    questionDetail.value = await getQuestionById(item.id)
  } catch (error) {
    console.error('题目详情加载失败：', error)
    ElMessage.error(error.message || '题目详情加载失败')
    questionPopupVisible.value = false
  } finally {
    popupLoading.value = false
  }
}

function resetPopupAnswerState() {
  popupSelectedAnswer.value = ''
  popupSelectedAnswers.value = []
  popupShortAnswer.value = ''
  popupShowResult.value = false
  popupIsCorrect.value = false
}

function resetQuestionPopup() {
  questionDetail.value = null
  resetPopupAnswerState()
}

const authUser = ref(getAuthUser())
const submitting = ref(false)
const mode = ref('login')
const authDialogVisible = ref(false)
const pendingRedirect = ref('')

const form = reactive({
  role: 'STUDENT',
  username: '',
  nickname: '',
  password: ''
})

const roleOptions = [
  { label: '学生', value: 'STUDENT' },
  { label: '管理员', value: 'ADMIN' }
]

const admin = computed(() => authUser.value?.role === 'ADMIN' || isAdmin())

const submitText = computed(() => {
  if (mode.value === 'register') return '注册并登录'
  return form.role === 'ADMIN' ? '登录管理端' : '登录学生端'
})

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 32, message: '账号长度为 3-32 个字符', trigger: 'blur' }
  ],
  nickname: mode.value === 'register'
    ? [{ required: true, message: '请输入姓名/昵称', trigger: 'blur' }]
    : [],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 个字符', trigger: 'blur' }
  ]
}))

const quickItems = [
  { title: '参加考试', desc: '选择已发布试卷，限时完成作答', icon: DocumentChecked, colorClass: 'blue', path: '/exam/list' },
  { title: '题库练习', desc: '按题型和分类查看题目解析', icon: EditPen, colorClass: 'green', path: '/questions' },
  { title: '成绩排行', desc: '查看已批阅考试的榜单结果', icon: Trophy, colorClass: 'orange', path: '/ranking' }
]

const displayBanners = computed(() => activeBanners.value.filter(Boolean).sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0)))

// 首页主卡片背景图：优先取轮播图第一张，作为低透明度背景。
// 使用 CSS 变量是为了让 ::before 伪元素可以拿到动态图片地址。
const heroBackgroundStyle = computed(() => {
  const banner = displayBanners.value.find(item => item?.imageUrl)
  if (!banner?.imageUrl) return {}

  const imageSrc = getImageSrc(banner.imageUrl).replace(/"/g, '%22')
  return {
    '--hero-bg-image': `url("${imageSrc}")`
  }
})
const today = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return { full: `${y}.${m}.${d}` }
})

async function loadActiveBanners() {
  bannerLoading.value = true
  try {
    const list = await getActiveBanners()
    activeBanners.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('获取启用轮播图失败：', error)
    activeBanners.value = []
  } finally {
    bannerLoading.value = false
  }
}

async function loadPopularQuestions() {
  popularLoading.value = true
  try {
    const list = await getPopularQuestions(6)
    popularQuestions.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('热门题目加载失败：', error)
    popularQuestions.value = []
  } finally {
    popularLoading.value = false
  }
}

function getImageSrc(imageUrl) {
  if (!imageUrl) return ''
  if (/^(https?:|data:|blob:)/i.test(imageUrl)) return imageUrl
  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
}

function handleBannerClick(banner) {
  if (!banner.linkUrl) return
  if (/^https?:\/\//i.test(banner.linkUrl)) {
    window.open(banner.linkUrl, '_blank')
    return
  }
  goProtected(banner.linkUrl)
}

function switchMode(nextMode) {
  mode.value = nextMode
  if (nextMode === 'register') {
    form.role = 'STUDENT'
  }
  formRef.value?.clearValidate?.()
}

function openAuthDialog(nextMode = 'login', redirect = '') {
  switchMode(nextMode)
  pendingRedirect.value = redirect || pendingRedirect.value || ''
  authDialogVisible.value = true
}

function goProtected(path) {
  if (authUser.value?.token) {
    router.push(path)
    return
  }
  ElMessage.info('请先登录后继续操作')
  pendingRedirect.value = path
  openAuthDialog('login', path)
}

function handleRoleChange() {
  formRef.value?.clearValidate?.()
}

function handleDialogClosed() {
  formRef.value?.clearValidate?.()
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const user = mode.value === 'register'
      ? await register({ username: form.username, password: form.password, nickname: form.nickname })
      : await login({ username: form.username, password: form.password, role: form.role })

    setAuthUser(user)
    authUser.value = user
    authDialogVisible.value = false
    ElMessage.success('登录成功')

    const redirect = pendingRedirect.value || route.query.redirect
    pendingRedirect.value = ''

    if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
      return
    }

    router.replace(user.role === 'ADMIN' ? '/admin/question-manage' : '/home')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    submitting.value = false
  }
}

function handleUserCommand(command) {
  if (command === 'admin') {
    router.push('/admin/question-manage')
    return
  }
  if (command === 'logout') {
    clearAuthUser()
    authUser.value = null
    ElMessage.success('已退出登录')
    router.replace('/home')
  }
}

watch(
  () => route.query.login,
  value => {
    if (value === '1' && !authUser.value?.token) {
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
      openAuthDialog('login', redirect)
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadActiveBanners()
  loadPopularQuestions()
})
</script>


<style scoped>
.home-page {
  min-height: 100vh;
  color: #172033;
  background:
    radial-gradient(circle at 18% 0%, rgba(59, 130, 246, .08), transparent 32%),
    linear-gradient(180deg, #f6f8fc 0%, #eef3f8 100%);
  font-family: Inter, "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
}

.top-bar {
  height: 70px;
  padding: 0 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, .82);
  border-bottom: 1px solid rgba(226, 232, 240, .9);
  backdrop-filter: blur(14px);
  position: sticky;
  top: 0;
  z-index: 20;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: 0;
  background: transparent;
  cursor: pointer;
  color: #172033;
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

.brand-text {
  font-size: 19px;
  font-weight: 750;
  letter-spacing: -.02em;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  border: 1px solid #d9e2ef;
  border-radius: 999px;
  padding: 7px 12px;
  background: #fff;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-chip small {
  color: #64748b;
}

.hero-shell,
.quick-section,
.notice-section,
.popular-section {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.hero-shell {
  padding-top: 46px;
}

.hero-card {
  min-height: 360px;
  border-radius: 30px;
  padding: 44px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 34px;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, .94);
  border: 1px solid rgba(226, 232, 240, .95);
  box-shadow: 0 24px 70px rgba(15, 23, 42, .08);
}

/* 轮播图背景层：上一版遮罩太白，几乎看不出来。
   这里提高图片透明度，同时用柔和白色遮罩保证文字清楚。 */
.hero-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: var(--hero-bg-image);
  background-size: cover;
  background-position: center;
  opacity: .90;
  filter: saturate(1.08) contrast(1.04);
  transform: scale(1.04);
}

/* 白色柔光层：透明度不能太高，否则会把背景图完全盖住 */
.hero-card::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, .82) 0%, rgba(255, 255, 255, .74) 56%, rgba(248, 251, 255, .84) 100%),
    radial-gradient(circle at 10% 25%, rgba(37, 99, 235, .10), transparent 36%);
}

.hero-copy,
.overview-card {
  position: relative;
  z-index: 1;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  margin: 0 0 14px;
  color: #2563eb;
  font-size: 15px;
  font-weight: 700;
}

.hero-copy h1 {
  max-width: 680px;
  margin: 0 0 18px;
  color: #111827;
  font-size: 40px;
  line-height: 1.22;
  letter-spacing: -.04em;
  font-weight: 800;
}

.hero-desc {
  max-width: 670px;
  margin: 0;
  color: #526176;
  font-size: 17px;
  line-height: 1.85;
}

.hero-actions {
  margin-top: 30px;
  display: flex;
  gap: 12px;
}

.overview-card {
  border-radius: 24px;
  padding: 26px;
  background: #f8fbff;
  border: 1px solid #dbe7f6;
}

.overview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 18px;
  border-bottom: 1px solid #e3ebf6;
  color: #64748b;
}

.overview-head strong {
  color: #111827;
  font-size: 16px;
}

.overview-list {
  display: grid;
  gap: 12px;
  margin: 20px 0;
}

.overview-item {
  padding: 15px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #e4ebf5;
}

.overview-item b,
.overview-item span {
  display: block;
}

.overview-item b {
  color: #172033;
  font-size: 15px;
}

.overview-item span {
  margin-top: 5px;
  color: #64748b;
  font-size: 14px;
}

.overview-note {
  margin: 0;
  color: #526176;
  line-height: 1.75;
}

.quick-section {
  padding-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.quick-card {
  padding: 22px;
  border-radius: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
  background: rgba(255, 255, 255, .92);
  border: 1px solid #e4ebf5;
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease, border-color .18s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
  border-color: #c9d8ec;
  box-shadow: 0 18px 38px rgba(15, 23, 42, .08);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #2563eb;
  background: #eff6ff;
  font-size: 22px;
}

.quick-icon.green { color: #059669; background: #ecfdf5; }
.quick-icon.orange { color: #d97706; background: #fffbeb; }

.quick-card h3 {
  margin: 0 0 6px;
  color: #111827;
  font-size: 17px;
}

.quick-card p {
  margin: 0;
  color: #64748b;
  line-height: 1.5;
}

.notice-section,
.popular-section {
  padding-top: 34px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0 0 6px;
  color: #111827;
  font-size: 24px;
  letter-spacing: -.02em;
}

.section-head p {
  margin: 0;
  color: #64748b;
}

.section-head.compact {
  margin-bottom: 12px;
}

.notice-carousel :deep(.el-carousel__indicators--outside) {
  margin-top: 8px;
}

.notice-card {
  height: 168px;
  border-radius: 22px;
  padding: 26px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 230px;
  gap: 24px;
  align-items: center;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e4ebf5;
}

.notice-card.clickable {
  cursor: pointer;
}

.notice-label {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  color: #2563eb;
  background: #eff6ff;
  font-size: 13px;
  font-weight: 700;
}

.notice-card h3 {
  margin: 12px 0 8px;
  color: #111827;
  font-size: 24px;
}

.notice-card p {
  margin: 0;
  color: #526176;
  line-height: 1.7;
}

.notice-card img {
  width: 230px;
  height: 118px;
  object-fit: cover;
  border-radius: 16px;
  opacity: .82;
}

.popular-section {
  padding-bottom: 80px;
}

.popular-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.popular-card {
  min-height: 150px;
  padding: 20px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid #e4ebf5;
  cursor: pointer;
  transition: border-color .18s ease, box-shadow .18s ease;
}

.popular-card:hover {
  border-color: #c9d8ec;
  box-shadow: 0 16px 34px rgba(15, 23, 42, .07);
}

.tag-line {
  display: flex;
  gap: 8px;
}

.popular-card h3 {
  margin: 12px 0 10px;
  color: #111827;
  font-size: 16px;
  line-height: 1.65;
}

.popular-card p {
  margin: 0;
  color: #64748b;
}

.auth-switch {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding: 6px;
  border-radius: 14px;
  background: #f1f5f9;
  margin-bottom: 18px;
}

.auth-switch button {
  height: 38px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  color: #64748b;
  font-weight: 700;
}

.auth-switch button.active {
  color: #1d4ed8;
  background: #fff;
  box-shadow: 0 8px 22px rgba(37, 99, 235, .12);
}

.dialog-role-tabs {
  width: 100%;
  display: flex;
}
.dialog-role-tabs :deep(.el-radio-button) {
  flex: 1;
}
.dialog-role-tabs :deep(.el-radio-button__inner) {
  width: 100%;
  white-space: nowrap;
}

.auth-tip {
  margin: -4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* --- 热门题目作答弹窗样式 --- */
.question-popup-dialog :deep(.el-dialog__body) {
  padding: 20px 28px;
  max-height: 60vh;
  overflow-y: auto;
}

.popup-loading {
  padding: 20px 0;
}

.popup-question-head {
  margin-bottom: 20px;
}

.popup-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.popup-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.7;
  color: #111827;
}

.popup-answer-area {
  margin-bottom: 16px;
}

.popup-option-line {
  display: block;
  padding: 12px 16px;
  margin-bottom: 10px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: background .18s ease, border-color .18s ease;
}

.popup-option-line:hover {
  background: #f1f5f9;
}

.popup-option-line.is-correct {
  background: #ecfdf5;
  border-color: #86efac;
}

.popup-option-line.is-wrong {
  background: #fef2f2;
  border-color: #fca5a5;
}

.popup-option-label {
  display: inline-block;
  min-width: 24px;
  font-weight: 700;
}

.popup-result-box {
  margin-top: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.popup-result-status {
  padding: 14px 20px;
  display: flex;
  gap: 10px;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}

.popup-result-status strong {
  font-size: 16px;
}

.popup-result-status.right {
  background: #ecfdf5;
  color: #047857;
}

.popup-result-status.wrong {
  background: #fff7ed;
  color: #c2410c;
}

.popup-result-section {
  padding: 16px 20px;
}

.popup-result-section + .popup-result-section {
  border-top: 1px solid #e5e7eb;
}

.popup-result-section h4 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 800;
  color: #111827;
}

.popup-result-section p {
  margin: 0;
  line-height: 1.75;
  color: #374151;
  white-space: pre-wrap;
}

.popup-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 960px) {
  .top-bar {
    height: auto;
    padding: 18px 22px;
    align-items: flex-start;
    gap: 14px;
  }

  .top-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .hero-card {
    grid-template-columns: 1fr;
    padding: 30px;
  }

  .hero-copy h1 {
    font-size: 31px;
  }

  .quick-section,
  .popular-grid {
    grid-template-columns: 1fr;
  }

  .notice-card {
    grid-template-columns: 1fr;
    height: auto;
  }

  .notice-card img {
    display: none;
  }
}
</style>
