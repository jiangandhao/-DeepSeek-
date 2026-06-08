const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    },
    client: {
      overlay: {
        runtimeErrors: (error) => {
          // 屏蔽 ResizeObserver 无害警告
          if (error.message === 'ResizeObserver loop completed with undelivered notifications.') {
            return false
          }
          return true
        }
      }
    }
  }
})
