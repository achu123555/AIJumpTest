import { request } from './request'

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    searchParams.append(key, value)
  })
  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

/**
 * 学生端查询可参加考试的试卷。
 *
 * 后端接口：GET /api/exam/papers
 * 只返回 PUBLISHED 状态的试卷。
 */
export function getExamPaperList(params = {}) {
  return request(`/api/exam/papers${buildQuery(params)}`)
}

/**
 * 开始考试。
 *
 * 后端接口：POST /api/exam
 * 请求体：{ paperId, studentName }
 */
export function startExam(data) {
  return request('/api/exam', {
    method: 'POST',
    body: data
  })
}

/**
 * 查询考试记录详情。
 *
 * 后端接口：GET /api/exam/{id}
 * 返回考试记录 + 试卷 + 题目 + 已提交答案。
 */
export function getExamDetail(id) {
  return request(`/api/exam/${id}`)
}

/**
 * 提交试卷。
 *
 * 后端接口：POST /api/exam/{examRecordId}/submit
 * 请求体：{ answers: { [questionId]: answer }, windowSwitches }
 */
export function submitExam(examRecordId, data) {
  return request(`/api/exam/${examRecordId}/submit`, {
    method: 'POST',
    body: data,
    timeout: 180000
  })
}

/**
 * 后台查询考试记录。
 */
export function getExamRecords(params = {}) {
  return request(`/api/exam/records${buildQuery(params)}`)
}

/**
 * 查询考试排行榜。
 */
export function getExamRanking(params = {}) {
  return request(`/api/exam/ranking${buildQuery(params)}`)
}
