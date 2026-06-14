import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import BannerManage from '../views/BannerManage.vue'
import CategoryManage from '../views/CategoryManage.vue'
import Placeholder from '../views/Placeholder.vue'

const placeholderRoutes = [
  { path: 'question-manage', title: '题目管理' },
  { path: 'paper-manage', title: '试卷管理' },
  { path: 'score-manage', title: '成绩管理' },
  { path: 'notice-manage', title: '公告管理' },
  { path: 'video-manage', title: '视频管理' },
  { path: 'video-category', title: '视频分类' }
]

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'Home', component: Home, meta: { title: '学生端首页' } },
  {
    path: '/admin',
    redirect: '/admin/banner-manage',
    children: [
      { path: 'banner-manage', name: 'BannerManage', component: BannerManage, meta: { title: '轮播图管理' } },
      { path: 'category-manage', name: 'CategoryManage', component: CategoryManage, meta: { title: '类别管理' } },
      ...placeholderRoutes.map(item => ({
        path: item.path,
        name: item.path,
        component: Placeholder,
        meta: { title: item.title }
      }))
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach(to => {
  document.title = to.meta?.title ? `${to.meta.title} - 智学平台` : '智学平台'
})

export default router
