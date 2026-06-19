import { request } from './request'

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }

    if (Array.isArray(value)) {
      value.forEach(item => {
        if (item !== undefined && item !== null && item !== '') {
          searchParams.append(key, item)
        }
      })
      return
    }

    searchParams.append(key, value)
  })

  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

export function getQuestionPage(params) {
  return request(`/api/questions/list${buildQuery(params)}`)
}

export function getQuestionById(id) {
  return request(`/api/questions/${id}`)
}

export function addQuestion(data) {
  return request('/api/questions', { method: 'POST', body: data })
}

export function updateQuestion(data) {
  return request('/api/questions', { method: 'PUT', body: data })
}

export function deleteQuestion(id) {
  return request(`/api/questions/${id}`, { method: 'DELETE' })
}

export function getPopularQuestions(size = 6) {
  return request(`/api/questions/popular${buildQuery({ size })}`)
}
