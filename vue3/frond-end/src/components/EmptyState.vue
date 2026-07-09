<template>
  <div class="empty-state">
    <div class="empty-icon-wrap">
      <el-icon class="empty-icon" :size="28"><component :is="icon" /></el-icon>
    </div>
    <p class="empty-text">{{ description }}</p>
    <el-button v-if="retryable" type="primary" plain size="small" @click="$emit('retry')">
      {{ retryText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'
import type { Component } from 'vue'

withDefaults(
  defineProps<{
    description?: string
    icon?: Component
    retryable?: boolean
    retryText?: string
  }>(),
  {
    description: '暂无数据',
    icon: Document,
    retryable: false,
    retryText: '重新加载',
  },
)

defineEmits<{ retry: [] }>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 16px;
  color: var(--text-secondary);
}
.empty-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: var(--primary-light);
  border: 1px solid #99f6e4;
}
.empty-icon {
  color: var(--primary-color);
}
.empty-text {
  margin: 0;
  font-size: 14px;
}
</style>
