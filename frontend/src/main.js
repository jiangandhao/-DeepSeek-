import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 屏蔽 ResizeObserver 无害警告
const resizeObserverErr = window.onerror
window.onerror = (msg, ...args) => {
  if (msg === 'ResizeObserver loop completed with undelivered notifications.') return true
  return resizeObserverErr ? resizeObserverErr(msg, ...args) : false
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
