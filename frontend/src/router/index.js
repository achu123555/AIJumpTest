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
import StudentQuestionList from '../views/StudentQuestionList.vue'
import StudentQuestionDetail from '../views/StudentQuestionDetail.vue'
import Placeholder from '../views/Placeholder.vue'

const placeholderRoutes = [
  { path: 'notice-manage', title: '公告管理' },
  { path: 'video-manage', title: '视频管理' },
  { path: 'video-category', title: '视频分类' }
]

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'Home', component: Home, meta: { title: '学生端首页' } },
  { path: '/questions', name: 'StudentQuestionList', component: StudentQuestionList, meta: { title: '题库练习' } },
  { path: '/question/:id', name: 'StudentQuestionDetail', component: StudentQuestionDetail, meta: { title: '题目详情' } },
  { path: '/exam/list', name: 'ExamList', component: ExamList, meta: { title: '选择考试' } },
  { path: '/exam/start/:paperId', name: 'ExamStart', component: ExamStart, meta: { title: '开始考试' } },
  { path: '/exam/take/:id', name: 'ExamTake', component: ExamTake, meta: { title: '考试中' } },
  { path: '/exam/result/:id', name: 'ExamResult', component: ExamResult, meta: { title: '考试结果' } },
  {
    path: '/admin',
    redirect: '/admin/question-manage',
    children: [
      { path: 'question-manage', name: 'QuestionManage', component: QuestionManage, meta: { title: '题目管理' } },
      { path: 'category-manage', name: 'CategoryManage', component: CategoryManage, meta: { title: '类别管理' } },
      { path: 'paper-manage', name: 'PaperManage', component: PaperManage, meta: { title: '试卷管理' } },
      { path: 'paper-create', name: 'PaperCreate', component: PaperEdit, meta: { title: '创建试卷' } },
      { path: 'paper-edit/:id', name: 'PaperEdit', component: PaperEdit, meta: { title: '编辑试卷' } },
      { path: 'score-manage', name: 'ExamRecordManage', component: ExamRecordManage, meta: { title: '成绩管理' } },
      { path: 'ranking-manage', name: 'RankingManage', component: RankingManage, meta: { title: '排行榜管理' } },
      { path: 'banner-manage', name: 'BannerManage', component: BannerManage, meta: { title: '轮播图管理' } },
      ...placeholderRoutes.map(item => ({ path: item.path, name: item.path, component: Placeholder, meta: { title: item.title } }))
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({ history: createWebHistory(), routes })
router.afterEach(to => { document.title = to.meta.title ? `${to.meta.title} - 智学平台` : '智学平台' })
export default router
