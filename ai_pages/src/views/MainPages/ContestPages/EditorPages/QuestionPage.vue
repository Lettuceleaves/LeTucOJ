<script setup>
import {
  ref,
  onMounted,
  onUnmounted,
  watch,
  nextTick,
  defineExpose,
} from 'vue'
import * as monaco from 'monaco-editor'
import { setupMyC } from '../../../../components/monaco-c'
import 'monaco-editor/min/vs/editor/editor.main.css'

const props = defineProps({ editorReady: Boolean })
const emit  = defineEmits(['exit', 'submit', 'check'])

const editorDom = ref(null)
let editor = null
let disposer = null

/* 父组件可调用的方法 */
const getCode = () => editor?.getValue() || ''
const setCode = (code) => editor?.setValue(code)
defineExpose({ getCode, setCode })

function createEditor() {
  if (editor) return

  setupMyC(monaco)          // ② 注册语言、主题、补全

  editor = monaco.editor.create(editorDom.value, {
    value:
      localStorage.getItem('userCode') ||
      '#include <stdio.h>\n\nint main() {\n    \n    return 0;\n}',
    language: 'myC',        // ③ 用我们注册的 language id
    theme: 'myCTheme',      // ④ 用我们注册的 theme
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 15,
    scrollBeyondLastLine: false,
    suggestOnTriggerCharacters: true,
    quickSuggestions: true,
  })

  // 自动保存
  disposer = editor.onKeyDown((e) => {
    const key = e.browserEvent.key
    if (key === ' ' || key === 'Enter') {
      const code = getCode()
      if (code) {
        localStorage.setItem('userCode', code)
        console.log('代码保存成功，按键:', key)
      }
    }
  })
}

function disposeEditor() {
  disposer?.dispose()
  editor?.dispose()
  editor = disposer = null
}

onMounted(() => {
  if (props.editorReady) createEditor()
})

onUnmounted(disposeEditor)

watch(
  () => props.editorReady,
  async (ready) => {
    if (!ready) return
    await nextTick()
    createEditor()
  },
  { immediate: true }
)
</script>
