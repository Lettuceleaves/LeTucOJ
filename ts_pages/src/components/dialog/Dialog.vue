<template>

  <el-dialog
    v-model="dialogData.show"
    :title="dialogData.title"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :width="dialogData.width"
    :before-close="handleClose"
    append-to-body
  >
    <span>{{ dialogData.content }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { inject, reactive, getCurrentInstance, nextTick } from 'vue'

const rawConfig = inject('config') || {}

const dialogData = reactive({
  show: true,
  title: '提示',
  content: '',
  width: '600px',
  onConfirm: () => {},
  onCancel: () => {},
  ...rawConfig
})

const { proxy } = getCurrentInstance()

const closeDialog = () => {
  dialogData.show = false
  nextTick(() => {
    setTimeout(() => {
      document.body.removeChild(proxy.$el.parentNode)
    }, 300)
  })
}

const handleClose = () => {
  dialogData.onCancel()
  closeDialog()
}

const handleConfirm = () => {
  dialogData.onConfirm()
  closeDialog()
}
</script>
