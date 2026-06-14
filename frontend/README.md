# Vue3 + Element Plus 学生端 + 管理端

项目已接入 Spring Boot 后端轮播图接口，包含：

- 学生端首页：`/home`
- 管理端轮播图管理：`/admin/banner-manage`
- 管理端类别管理：`/admin/category-manage`
- 其他管理模块：暂时显示「待开发」

## 技术栈

- Vue 3
- Vite
- Vue Router
- Element Plus
- @element-plus/icons-vue
- fetch 请求封装，不依赖 axios

## 运行方式

先启动后端，默认后端地址：

```text
http://localhost:8080
```

再启动前端：

```bash
npm install
npm run dev
```

启动后访问学生端：

```text
http://localhost:3001/home
```

管理端地址：

```text
http://localhost:3001/admin/banner-manage
http://localhost:3001/admin/category-manage
```

## 已接入的后端接口

### 学生端首页轮播图

```text
GET /api/banners/active
```

用途：只获取启用状态的轮播图，并按 `sortOrder` 升序展示。

### 管理端轮播图列表

```text
GET /api/banners/list
```

用途：管理端展示全部轮播图，包括启用和禁用状态。

### 根据 ID 查询轮播图详情

```text
GET /api/banners/{id}
```

用途：点击编辑时获取单条轮播图详情。

### 上传轮播图图片

```text
POST /api/banners/upload-image
Content-Type: multipart/form-data
file: 图片文件
```

用途：把图片上传到阿里云 OSS，后端返回可访问的图片 URL。

### 新增轮播图

```text
POST /api/banners/add
Content-Type: application/json
```

请求体示例：

```json
{
  "title": "Java + AI 应用开发",
  "description": "适合放在学生端首页展示",
  "imageUrl": "https://xxx.oss-cn-beijing.aliyuncs.com/banner/xxx.jpg",
  "linkUrl": "https://www.atguigu.com",
  "sortOrder": 1,
  "isActive": true
}
```

### 更新轮播图

```text
PUT /api/banners/update
Content-Type: application/json
```

请求体示例：

```json
{
  "id": 1,
  "title": "更新后的标题",
  "description": "更新后的描述",
  "imageUrl": "https://xxx.oss-cn-beijing.aliyuncs.com/banner/xxx.jpg",
  "linkUrl": "",
  "sortOrder": 1,
  "isActive": true
}
```

### 删除轮播图

```text
DELETE /api/banners/delete/{id}
```

### 管理端轮播图状态切换

```text
PUT /api/banners/toggle/{id}?isActive=true
PUT /api/banners/toggle/{id}?isActive=false
```



## 已接入的分类管理接口

### 查询分类平铺列表

```text
GET /api/categories
```

### 查询分类树状列表

```text
GET /api/categories/tree
```

用途：管理端类别管理页面展示一级分类和二级分类。

### 新增子分类

```text
POST /api/categories
Content-Type: application/json
```

请求体示例：

```json
{
  "parentId": 13,
  "name": "Java基础",
  "sort": 1
}
```

### 更新子分类

```text
PUT /api/categories
Content-Type: application/json
```

请求体示例：

```json
{
  "id": 16,
  "parentId": 13,
  "name": "Java基础",
  "sort": 1
}
```

### 删除子分类

```text
DELETE /api/categories/{id}
```

注意：当前后端逻辑中，一级分类不可删除；二级分类如果已经关联题目，也不可删除。

## 主要文件

```text
src/api/request.js          fetch 统一请求封装
src/api/banner.js            轮播图接口统一管理
src/api/category.js          分类接口统一管理
src/views/Home.vue           学生端首页，展示启用轮播图
src/views/BannerManage.vue   管理端轮播图完整 CRUD 页面
src/views/CategoryManage.vue 管理端类别管理页面
src/router/index.js         路由配置
vite.config.js              开发代理配置
```

## 返回格式要求

前端默认适配这种统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "轮播图标题",
      "description": "轮播图描述",
      "imageUrl": "图片地址",
      "linkUrl": "跳转地址",
      "sortOrder": 1,
      "isActive": true,
      "createTime": "2026-06-09T13:47:00"
    }
  ]
}
```

## 代理说明

开发环境通过 `vite.config.js` 代理接口：

```text
/api -> http://localhost:8080
```

如果数据库里的图片地址是 `/uploads/xxx`、`/upload/xxx`、`/files/xxx`、`/images/xxx`，Vite 也已经配置了代理，会转发到后端。
