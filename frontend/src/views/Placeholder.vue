<template>
  <section class="placeholder-page">
    <el-card shadow="never" class="placeholder-card">
      <el-empty :description="`${title} 待开发`">
        <el-button type="primary" plain @click="goBanner">先去轮播图管理</el-button>
      </el-empty>
    </el-card>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const title = computed(() => route.meta.title || '当前模块')

const goBanner = () => router.push('/admin/banner-manage')
</script>

<style scoped>
.placeholder-page {
  min-height: 100%;
}

.placeholder-card {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}
</style>
