/**
 * 从 Content-Disposition 响应头中解析并还原文件名。
 */
export function getFileNameFromDisposition(disposition, fallbackName = 'download.xlsx') {
  if (!disposition) return fallbackName

  const decodeFileName = value => {
    if (!value) return ''

    let fileName = value.trim().replace(/^['"]|['"]$/g, '')

    // 兼容 filename*=UTF-8''xxx、filename*=utf_8''xxx
    fileName = fileName.replace(/^UTF[-_]8''/i, '')

    try {
      return decodeURIComponent(fileName)
    } catch (error) {
      console.warn('文件名 URL 解码失败，使用原始文件名：', error)
      return fileName
    }
  }

  // 优先解析 filename*=UTF-8''xxx
  const filenameStarMatch = disposition.match(/filename\*\s*=\s*([^;]+)/i)
  if (filenameStarMatch?.[1]) {
    const fileName = decodeFileName(filenameStarMatch[1])
    if (fileName) return fileName
  }

  // 再解析 filename="xxx"
  // 你的截图就是这里没 decode 导致的
  const filenameMatch = disposition.match(/filename\s*=\s*("[^"]+"|[^;]+)/i)
  if (filenameMatch?.[1]) {
    const fileName = decodeFileName(filenameMatch[1])
    if (fileName) return fileName
  }

  return fallbackName
}

export function downloadBlob(blob, fallbackName = 'download.xlsx', disposition = '') {
  const fileName = getFileNameFromDisposition(disposition, fallbackName)
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = fileName
  link.style.display = 'none'

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  window.URL.revokeObjectURL(url)
}