<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  width: 100%;
  height: 100%;
  font-family: var(--font-body);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  background: var(--bg-page);
}
</style>
