// dialogService.js
import { createApp } from 'vue'
import Dialog from './Dialog.vue'

class DialogService {
  show(options = {}) {
    return new Promise((resolve, reject) => {
      const mountNode = document.createElement('div')
      document.body.appendChild(mountNode)

      const app = createApp(Dialog)
      app.provide('config', {
        ...options,
        onConfirm: () => {
          resolve(true)
          app.unmount()
          document.body.removeChild(mountNode)
        },
        onCancel: () => {
          reject(false)
          app.unmount()
          document.body.removeChild(mountNode)
        }
      })
      app.mount(mountNode)
    })
  }

  /**
   * 简洁 confirm 函数式调用
   * @param {string} content 提示内容
   * @param {string} title 标题（可选）
   * @returns Promise<boolean>
   */
  async confirm(content, title = '提示') {
    try {
      await this.show({ title, content })
      return true
    } catch {
      return false
    }
  }
}

export default new DialogService()
