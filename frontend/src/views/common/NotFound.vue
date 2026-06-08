<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-code">404</div>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被移除。</p>
      <router-link to="/dashboard" class="back-btn">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  text-align: center;
}

.not-found-code {
  font-family: 'Cormorant Garamond', serif;
  font-size: 6rem;
  font-weight: 700;
  color: var(--color-primary);
  opacity: 0.3;
  line-height: 1;
  margin-bottom: var(--space-md);
}

.not-found h2 {
  font-family: 'Cormorant Garamond', serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
}

.not-found p {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-xl);
}

.back-btn {
  display: inline-block;
  padding: 10px 24px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.85rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.back-btn:hover {
  background: var(--color-primary-light);
  transform: translateY(-1px);
}
</style>
