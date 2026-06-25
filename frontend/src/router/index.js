import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import BannerManage from '../views/BannerManage.vue'
import CategoryManage from '../views/CategoryManage.vue'
import QuestionManage from '../views/QuestionManage.vue'
import PaperManage from '../views/PaperManage.vue'
import PaperEdit from '../views/PaperEdit.vue'
import ExamList from '../views/ExamList.vue'
import ExamStart from '../views/ExamStart.vue'
import ExamTake from '../views/ExamTake.vue'
import ExamResult from '../views/ExamResult.vue'
import ExamRecordManage from '../views/ExamRecordManage.vue'
import RankingManage from '../views/RankingManage.vue'
import StudentRanking from '../views/StudentRanking.vue'
import StudentQuestionList from '../views/StudentQuestionList.vue'
import StudentQuestionDetail from '../views/StudentQuestionDetail.vue'
import { getAuthUser } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'Home', component: Home, meta: { title: '首页' } },
  { path: '/questions', name: 'StudentQuestionList', component: StudentQuestionList, meta: { title: '题库练习', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/question/:id', name: 'StudentQuestionDetail', component: StudentQuestionDetail, meta: { title: '题目详情', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/ranking', name: 'StudentRanking', component: StudentRanking, meta: { title: '考试排行榜', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/exam/list', name: 'ExamList', component: ExamList, meta: { title: '选择考试', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/exam/start/:paperId', name: 'ExamStart', component: ExamStart, meta: { title: '开始考试', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/exam/take/:id', name: 'ExamTake', component: ExamTake, meta: { title: '考试中', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  { path: '/exam/result/:id', name: 'ExamResult', component: ExamResult, meta: { title: '考试结果', requiresAuth: true, roles: ['ADMIN', 'STUDENT'] } },
  {
    path: '/admin',
    redirect: '/admin/question-manage',
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: 'question-manage', name: 'QuestionManage', component: QuestionManage, meta: { title: '题目管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'category-manage', name: 'CategoryManage', component: CategoryManage, meta: { title: '类别管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'paper-manage', name: 'PaperManage', component: PaperManage, meta: { title: '试卷管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'paper-create', name: 'PaperCreate', component: PaperEdit, meta: { title: '创建试卷', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'paper-edit/:id', name: 'PaperEdit', component: PaperEdit, meta: { title: '编辑试卷', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'score-manage', name: 'ExamRecordManage', component: ExamRecordManage, meta: { title: '成绩管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'ranking-manage', name: 'RankingManage', component: RankingManage, meta: { title: '排行榜管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'banner-manage', name: 'BannerManage', component: BannerManage, meta: { title: '轮播图管理', requiresAuth: true, roles: ['ADMIN'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const user = getAuthUser()

  if (to.meta?.requiresAuth && !user?.token) {
    next({ path: '/home', query: { login: '1', redirect: to.fullPath } })
    return
  }

  const roles = to.meta?.roles
  if (roles?.length && user?.role && !roles.includes(user.role)) {
    next(user.role === 'ADMIN' ? '/admin/question-manage' : '/home')
    return
  }

  next()
})

router.afterEach(to => { document.title = to.meta.title ? `${to.meta.title} - AIJumpTest` : 'AIJumpTest' })
export default router
