# AIJumpTest 前端

Vue3 + Element Plus 前端项目，当前版本已经接入后端接口版：

- 管理端轮播图完整 CRUD
- 管理端类别管理
- 管理端题目管理
- 学生端首页启用轮播图展示
- 学生端热门题目展示
- 学生端题库练习与题目详情页

## 启动

```bash
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:3001/home
```

后端默认代理地址：

```text
http://localhost:8080
```

## 页面地址

```text
学生端首页：      /home
学生端题库：      /questions
学生端题目详情：  /question/:id
管理端题目管理：  /admin/question-manage
管理端类别管理：  /admin/category-manage
管理端轮播图：    /admin/banner-manage
```

## 题目接口

```text
GET    /api/questions/list
GET    /api/questions/{id}
POST   /api/questions
PUT    /api/questions
DELETE /api/questions/{id}
GET    /api/questions/popular?size=6
```

## 分类接口

```text
GET /api/categories/tree
```

## 轮播图点击逻辑

学生端首页轮播图点击保持最新版逻辑：

```js
if (/^https?:\/\//i.test(banner.linkUrl)) {
  window.open(banner.linkUrl, '_blank')
  return
}

if (/^www\./i.test(banner.linkUrl)) {
  window.open(`https://${banner.linkUrl}`, '_blank')
  return
}
```

## 说明

题目管理里的“批量导入”和“AI生成”当前只是保留入口，因为你后端还没有提供对应接口。

如果编辑选择题时报错，请检查后端 `QuestionServiceImpl.update` 的选择题分支：当前代码里 `questionAnswer` 为 `null` 时又调用了 `answer.setAnswer(...)`，可能会出现空指针，需要后端修一下后才能完整更新选择题选项。

## 本次更新：题库分类筛选逻辑

学生端题库页和管理端题目管理页已补上右侧分类树筛选逻辑：

- 点击「全部」：清空题型和分类，查询所有题目。
- 点击一级主干分类，例如「选择题 / 判断题 / 简答题」：前端按题目 `type` 查询，不传 `categoryId`。
- 点击二级分支分类，例如「Java基础 / Spring框架 / MySQL数据库」：前端按 `categoryId` 精确查询，并同步父级题型。

这样不用改后端分页接口，继续使用你已有的：

```text
GET /api/questions/list?type=CHOICE
GET /api/questions/list?type=TEXT&categoryId=7
```

注意：数据库里简答题类型是 `TEXT`，所以前端已把「简答题」的 value 从旧的 `SHORT_ANSWER` 统一改成 `TEXT`。

## 本次更新：题目练习确认与下一题

- 学生端题目详情页将“查看答案和解析”改为“确定”。
- 点击“确定”后，前端会根据当前作答判断对错，然后展示参考答案和题目解析。
- 去掉“重做一遍”按钮。
- 新增“下一题”按钮：优先按当前题目的 `categoryId` 查询同一分类题目，跳转到列表顺序里的下一道题；如果没有下一题，会提示“没有下一题了~”。
- 选择题支持单选/多选判断；判断题兼容“正确/错误、true/false、1/0”等答案格式；简答题按去空格后的文本一致性做基础判断。


## 请求工具说明

本版本已将前端请求工具切换为 axios，统一封装在 `src/api/request.js`。业务接口文件仍然保持原来的 `request(url, options)` 调用方式，方便后续继续扩展。

首次运行请执行：

```bash
npm install
npm run dev
```

## 本次更新：题目 Excel 批量导入导出

管理端题目管理页 `/admin/question-manage` 已接入 EasyExcel 相关接口：

```text
GET  /api/questions/template      下载题目导入模板
POST /api/questions/import        上传 Excel 批量导入题目，参数名：file
GET  /api/questions/export        按当前筛选条件导出题目 Excel
```

前端改动点：

- `src/api/question.js`：新增 `downloadQuestionTemplate`、`importQuestionExcel`、`exportQuestionExcel`。
- `src/api/request.js`：新增 `rawResponse` 支持，下载 Excel 时可以拿到 `Content-Disposition` 响应头。
- `src/utils/download.js`：新增 Blob 下载工具，统一处理中文文件名和 `UTF-8''` 前缀问题。
- `src/views/QuestionManage.vue`：题目管理页新增“下载模板、Excel导入、导出Excel”按钮和上传逻辑。

导入 Excel 时请选择 `.xls` 或 `.xlsx` 文件。导出 Excel 会自动携带当前页面筛选条件，例如题型、难度、分类和关键词。
