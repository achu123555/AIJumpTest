# Vue3 + Element Plus 轮播图管理端

这是一个按截图风格搭建的前端管理端 Demo，已完成「轮播图管理」模块，其他菜单暂时显示「待开发」。

## 技术栈

- Vue 3
- Vite
- Vue Router
- Element Plus
- @element-plus/icons-vue

## 运行方式

```bash
npm install
npm run dev
```

启动后访问：

```text
http://localhost:3001/admin/banner-manage
```

## 已完成内容

- 管理端左侧菜单布局
- 顶部栏和主内容区
- 轮播图列表展示
- 新增轮播图
- 编辑轮播图
- 删除轮播图
- 启用 / 禁用轮播图
- 排序字段
- 图片上传本地预览
- 外部图片 URL 输入
- 跳转链接格式校验
- localStorage 本地持久化

## 说明

当前项目是纯前端版本，没有接后端接口。

图片上传这里为了方便本地演示，使用 `FileReader` 转成 Base64 存到 localStorage。正式项目中应该把 `handleFileChange` 里的逻辑替换成真实上传接口，例如：

```js
// const res = await uploadBannerImage(rawFile)
// form.imageUrl = res.data.url
```

轮播图数据正式接后端时，可以把 `src/utils/storage.js` 替换成 axios 请求接口。
