<template>
  <el-container v-if="showAdminLayout" class="admin-shell">
    <el-aside class="admin-aside" width="220px">
      <div class="logo-row">
        <el-icon class="logo-icon"><Grid /></el-icon>
        <span>管理菜单</span>
      </div>

      <el-menu
        router
        :default-active="route.path"
        class="side-menu"
        background-color="#ffffff"
        text-color="#1f2329"
        active-text-color="#2674ff"
      >
        <el-menu-item index="/admin/question-manage">
          <el-icon><Document /></el-icon>
          <span>题目管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/category-manage">
          <el-icon><Folder /></el-icon>
          <span>类别管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/paper-manage">
          <el-icon><Collection /></el-icon>
          <span>试卷管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/score-manage">
          <el-icon><Trophy /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/ranking-manage">
          <el-icon><Medal /></el-icon>
          <span>排行榜管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/banner-manage">
          <el-icon><Picture /></el-icon>
          <span>轮播图管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon><Grid /></el-icon>
          <span>AIJumpTest 管理后台</span>
        </div>
        <div class="header-right">
          <el-button size="small" plain @click="router.push('/home')">查看学生端</el-button>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="admin-user">{{ user?.nickname || user?.username || '管理员' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection, Document, Folder, Grid, Medal, Picture, Trophy } from '@element-plus/icons-vue'
import { clearAuthUser, getAuthUser } from './utils/auth'

const route = useRoute()
const router = useRouter()
const showAdminLayout = computed(() => route.path.startsWith('/admin'))
const user = computed(() => getAuthUser())

function handleCommand(command) {
  if (command === 'logout') {
    clearAuthUser()
    ElMessage.success('已退出登录')
    router.replace('/home')
  }
}
</script>

<style scoped>
.admin-shell {
  height: 100vh;
  overflow: hidden;
}

.admin-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 9px rgb(0 0 0 / 6%);
  z-index: 2;
}

.logo-row {
  height: 116px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  font-size: 20px;
  font-weight: 700;
  border-bottom: 1px solid #eef0f3;
}

.logo-icon {
  font-size: 18px;
}

.side-menu {
  border-right: 0;
  padding-top: 10px;
}

.side-menu :deep(.el-menu-item) {
  height: 52px;
  margin: 0 8px 2px;
  border-radius: 8px;
  font-size: 15px;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: #ecf4ff;
  font-weight: 600;
}

.admin-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left {
  font-size: 16px;
  font-weight: 700;
}

.admin-user {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
}

.admin-main {
  height: calc(100vh - 56px);
  overflow: auto;
  padding: 36px 36px 48px;
  background: #e9e9e9;
}
</style>
