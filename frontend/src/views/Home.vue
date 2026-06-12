<template>
  <main class="student-home">
    <header class="top-bar">
      <button class="brand" type="button" @click="goHome">
        <span class="brand-mark">学</span>
        <span class="brand-text">智学平台</span>
      </button>

      <nav class="top-actions">
        <el-button type="primary" :icon="DocumentChecked" @click="goTodo('考试入口')">考试入口</el-button>
        <el-button :icon="Trophy" @click="goTodo('考试排行榜')">考试排行榜</el-button>
        <el-button :icon="Setting" @click="router.push('/admin/banner-manage')">管理后台</el-button>
      </nav>
    </header>

    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-left">
          <el-skeleton v-if="bannerLoading" animated class="banner-skeleton">
            <template #template>
              <el-skeleton-item variant="image" class="skeleton-image" />
            </template>
          </el-skeleton>

          <el-carousel
            v-else-if="displayBanners.length"
            class="home-carousel"
            height="300px"
            arrow="hover"
            indicator-position="none"
            :interval="4200"
          >
            <el-carousel-item v-for="item in displayBanners" :key="item.id || item.title">
              <article
                class="banner-card"
                :class="{ clickable: !!item.linkUrl }"
                @click="handleBannerClick(item)"
              >
                <img v-if="item.imageUrl" :src="getImageSrc(item.imageUrl)" :alt="item.title" class="banner-img" />
                <div v-else class="banner-fallback-bg"></div>

                <div class="banner-mask"></div>
                <div class="banner-text">
                  <p class="banner-kicker">今日推荐</p>
                  <h1>{{ item.title || '开始今天的学习' }}</h1>
                  <p>{{ item.description || '刷几道题、看一个知识点，节奏不用太猛，但要稳。' }}</p>
                </div>
              </article>
            </el-carousel-item>
          </el-carousel>

          <article v-else class="banner-card empty-banner">
            <div class="banner-fallback-bg"></div>
            <div class="banner-mask"></div>
            <div class="banner-text">
              <p class="banner-kicker">学习看板</p>
              <h1>欢迎回来</h1>
              <p>后台启用轮播图后，这里会自动展示最新内容。</p>
            </div>
          </article>
        </div>

        <aside class="notice-card">
          <div class="notice-head">
            <div>
              <p>系统公告</p>
              <span>学习安排会放在这里</span>
            </div>
            <el-icon><Bell /></el-icon>
          </div>

          <div class="notice-body">
            <div class="date-box">
              <strong>{{ today.day }}</strong>
              <span>{{ today.month }}月</span>
            </div>
            <div class="notice-text">
              <h3>欢迎使用学习平台</h3>
              <p>先从练习题开始，遇到不会的题再回头补知识点。</p>
              <div class="notice-meta">
                <el-tag size="small" type="danger" effect="light">公告</el-tag>
                <span>{{ today.full }}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <section class="quick-section">
      <div class="section-title">
        <h2>快捷功能</h2>
        <p>常用入口放前面，用的时候不绕路</p>
      </div>

      <div class="quick-grid">
        <article v-for="item in quickItems" :key="item.title" class="quick-card" @click="goTodo(item.title)">
          <div class="quick-icon" :class="item.colorClass">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  DataAnalysis,
  DocumentChecked,
  EditPen,
  Notebook,
  Setting,
  TrendCharts,
  Trophy,
  VideoPlay
} from '@element-plus/icons-vue'
import { getActiveBanners } from '../api/banner'

const router = useRouter()
const bannerLoading = ref(false)
const activeBanners = ref([])

const quickItems = [
  {
    title: '开始考试',
    desc: '进入在线考试，提交后查看成绩',
    icon: DocumentChecked,
    colorClass: 'blue'
  },
  {
    title: '刷题练习',
    desc: '按分类练习，慢慢把薄弱点补上',
    icon: EditPen,
    colorClass: 'red'
  },
  {
    title: '学习排行',
    desc: '看看当前排名，也给自己一点动力',
    icon: Trophy,
    colorClass: 'orange'
  },
  {
    title: '学习分析',
    desc: '整理做题情况，找出最近容易错的地方',
    icon: TrendCharts,
    colorClass: 'green'
  },
  {
    title: '视频学习',
    desc: '用短视频复习知识点，适合碎片时间',
    icon: VideoPlay,
    colorClass: 'orange'
  },
  {
    title: '我的笔记',
    desc: '把常错题和重点内容记下来',
    icon: Notebook,
    colorClass: 'purple'
  },
  {
    title: '学习报告',
    desc: '回顾阶段学习情况，方便调整计划',
    icon: DataAnalysis,
    colorClass: 'cyan'
  }
]

const displayBanners = computed(() => {
  return activeBanners.value
    .filter(Boolean)
    .sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0))
})

const today = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return {
    month,
    day,
    full: `${year}/${month}/${day}`
  }
})

const loadActiveBanners = async () => {
  bannerLoading.value = true
  try {
    const list = await getActiveBanners()
    activeBanners.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('获取启用轮播图失败：', error)
    activeBanners.value = []
    ElMessage.warning('启用轮播图加载失败，请确认后端 /api/banners/active 接口是否正常')
  } finally {
    bannerLoading.value = false
  }
}

const getImageSrc = imageUrl => {
  if (!imageUrl) return ''

  if (/^(https?:|data:|blob:)/i.test(imageUrl)) {
    return imageUrl
  }

  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
}

const handleBannerClick = banner => {
  if (!banner.linkUrl) return

  // 完整协议链接：https://www.baidu.com
  if (/^https?:\/\//i.test(banner.linkUrl)) {
    window.open(banner.linkUrl, '_blank')
    return
  }

  // 外部链接但没写协议：www.baidu.com 拼接url
  if (/^www\./i.test(banner.linkUrl)) {
    window.open(`https://${banner.linkUrl}`, '_blank')
    return
  }

  router.push(banner.linkUrl).catch(() => {
    ElMessage.info('这个链接暂时还没有对应页面')
  })
}

const goTodo = name => {
  ElMessage.info(`${name}模块待开发`)
}

const goHome = () => {
  router.push('/home')
}

onMounted(() => {
  loadActiveBanners()
})
</script>

<style scoped>
.student-home {
  min-height: 100vh;
  background:
    radial-gradient(circle at 12% 20%, rgba(64, 217, 188, 0.45), transparent 28%),
    radial-gradient(circle at 86% 88%, rgba(247, 161, 196, 0.45), transparent 32%),
    linear-gradient(135deg, #dff9f2 0%, #dce8f3 48%, #f5dceb 100%);
  color: #111827;
}

.top-bar {
  height: 74px;
  padding: 0 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.82);
  border-bottom: 1px solid rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px rgba(31, 41, 55, 0.06);
  position: sticky;
  top: 0;
  z-index: 20;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, #246bfe, #3b2ee8);
  box-shadow: 0 10px 20px rgba(36, 107, 254, 0.24);
}

.brand-text {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #1f2a64;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-section {
  padding: 42px 40px 22px;
}

.hero-inner {
  max-width: 1220px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 34px;
  align-items: stretch;
}

.hero-left,
.home-carousel,
.banner-skeleton,
.skeleton-image,
.empty-banner {
  height: 300px;
}

.banner-skeleton :deep(.el-skeleton__item) {
  width: 100%;
  height: 300px;
  border-radius: 22px;
}

.home-carousel :deep(.el-carousel__container) {
  border-radius: 22px;
}

.home-carousel :deep(.el-carousel__arrow) {
  background: rgba(255, 255, 255, 0.68);
  color: #4b5563;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.banner-card {
  position: relative;
  height: 100%;
  min-height: 300px;
  overflow: hidden;
  border-radius: 22px;
  background: #0f172a;
  box-shadow: 0 22px 50px rgba(20, 33, 61, 0.22);
}

.banner-card.clickable {
  cursor: pointer;
}

.banner-card.clickable:hover .banner-img,
.banner-card.clickable:hover .banner-fallback-bg {
  transform: scale(1.04);
}

.banner-img,
.banner-fallback-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.banner-fallback-bg {
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.92), rgba(14, 165, 233, 0.76)),
    repeating-linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0 2px, transparent 2px 18px);
}

.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(4, 12, 28, 0.72) 0%, rgba(4, 12, 28, 0.35) 42%, rgba(4, 12, 28, 0.08) 100%);
}

.banner-text {
  position: absolute;
  left: 38px;
  bottom: 34px;
  width: min(560px, calc(100% - 76px));
  color: #fff;
}

.banner-kicker {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  margin: 0 0 12px;
  border-radius: 999px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
}

.banner-text h1 {
  margin: 0 0 10px;
  font-size: 34px;
  line-height: 1.15;
  letter-spacing: 0.02em;
}

.banner-text p:last-child {
  margin: 0;
  font-size: 16px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.notice-card {
  height: 300px;
  border-radius: 22px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 22px 50px rgba(20, 33, 61, 0.16);
}

.notice-head {
  height: 74px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  background: linear-gradient(135deg, #ef233c 0%, #ff7a00 100%);
}

.notice-head p {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 800;
}

.notice-head span {
  font-size: 12px;
  opacity: 0.88;
}

.notice-head .el-icon {
  font-size: 23px;
}

.notice-body {
  height: calc(100% - 74px);
  padding: 34px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.date-box {
  flex: 0 0 auto;
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #3f2dbd, #2b1b8f);
  box-shadow: 0 12px 24px rgba(63, 45, 189, 0.25);
}

.date-box strong {
  font-size: 24px;
  line-height: 1;
}

.date-box span {
  margin-top: 5px;
  font-size: 12px;
}

.notice-text {
  min-width: 0;
}

.notice-text h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 800;
}

.notice-text p {
  margin: 0 0 13px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.55;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #6b7280;
  font-size: 13px;
}

.quick-section {
  max-width: 1220px;
  margin: 0 auto;
  padding: 28px 40px 70px;
}

.section-title {
  text-align: center;
  color: #fff;
  text-shadow: 0 2px 12px rgba(30, 41, 59, 0.25);
  margin-bottom: 28px;
}

.section-title h2 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 900;
  letter-spacing: 0.05em;
}

.section-title p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.quick-card {
  min-height: 170px;
  padding: 28px 24px 24px;
  border-radius: 20px;
  text-align: center;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 36px rgba(20, 33, 61, 0.14);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.quick-card:hover {
  transform: translateY(-6px);
  background: #fff;
  box-shadow: 0 24px 44px rgba(20, 33, 61, 0.2);
}

.quick-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: #eff6ff;
  color: #2563eb;
}

.quick-icon.red {
  background: #fff1f2;
  color: #e11d48;
}

.quick-icon.orange {
  background: #fff7ed;
  color: #ea580c;
}

.quick-icon.green {
  background: #f0fdf4;
  color: #16a34a;
}

.quick-icon.purple {
  background: #f5f3ff;
  color: #7c3aed;
}

.quick-icon.cyan {
  background: #ecfeff;
  color: #0891b2;
}

.quick-card h3 {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 800;
}

.quick-card p {
  margin: 0 auto;
  max-width: 190px;
  color: #4b5563;
  line-height: 1.65;
  font-size: 14px;
}

@media (max-width: 1180px) {
  .hero-inner {
    grid-template-columns: 1fr;
  }

  .notice-card {
    height: auto;
  }

  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
