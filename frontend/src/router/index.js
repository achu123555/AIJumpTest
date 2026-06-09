import { createRouter, createWebHistory } from 'vue-router'
import BannerManage from '../views/BannerManage.vue'
import Placeholder from '../views/Placeholder.vue'

const placeholderRoutes = [
  { path: 'question-manage', title: '题目管理' },
  { path: 'category-manage', title: '类别管理' },
  { path: 'paper-manage', title: '试卷管理' },
  { path: 'score-manage', title: '成绩管理' },
  { path: 'notice-manage', title: '公告管理' },
  { path: 'video-manage', title: '视频管理' },
  { path: 'video-category', title: '视频分类' }
]

const routes = [
  { path: '/', redirect: '/admin/banner-manage' },
  {
    path: '/admin',
    redirect: '/admin/banner-manage',
    children: [
      { path: 'banner-manage', name: 'BannerManage', component: BannerManage, meta: { title: '轮播图管理' } },
      ...placeholderRoutes.map(item => ({
        path: item.path,
        name: item.path,
        component: Placeholder,
        meta: { title: item.title }
      }))
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/banner-manage' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
