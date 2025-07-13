<template>
  <div class="editor-container">
    <textarea ref="editor"></textarea>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import CodeMirror from 'codemirror';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';
import 'codemirror/mode/clike/clike';
import 'codemirror/addon/edit/matchbrackets';
import 'codemirror/addon/edit/closebrackets';
import 'codemirror/addon/hint/anyword-hint';
import 'codemirror/addon/hint/show-hint';

const editor = ref(null);
let cmInstance = null;

function cKeywordsHint(cm) {
  const keywords = ['int', 'return', 'if', 'else', 'while', 'for', 'printf', 'iff'];
  const cursor = cm.getCursor();
  const token = cm.getTokenAt(cursor);

  // 获取当前输入的词，注意处理光标在词中间的情况
  const start = token.start;
  const end = cursor.ch;
  const currentWord = token.string.slice(0, end - start);

  if (!currentWord) {
    return null;  // 没词不显示提示
  }

  const list = keywords.filter(k => k.startsWith(currentWord));
  if (list.length === 0) {
    return null;  // 没匹配词不显示提示
  }

  return {
    list,
    from: CodeMirror.Pos(cursor.line, start),
    to: CodeMirror.Pos(cursor.line, end)
  };
}


onMounted(() => {
  cmInstance  = CodeMirror.fromTextArea(editor.value, {
    mode: 'text/x-csrc', // 设置为 C 语言模式
    theme: 'material', // 使用 Material 主题
    lineNumbers: true, // 显示行号
    matchBrackets: true, // 匹配括号
    autoCloseBrackets: true, // 自动关闭括号
    extraKeys: { 'Ctrl': 'autocomplete' }, // 按 Ctrl+Space 触发代码提示
    hintOptions: {
      hint: cKeywordsHint
    },
  });
  cmInstance.showHint({ hint: cKeywordsHint });
});

const getCode = () => cmInstance?.getValue() ?? '';
const setCode = (code) => {
  if (cmInstance) {
    cmInstance.setValue(code)
  }
}

defineExpose({ getCode, setCode });

</script>

<style>
.editor-container {
  width: 100%;
  height: 1000%;
}

textarea {
  width: 100%;
  height: 100%;
}
</style>
