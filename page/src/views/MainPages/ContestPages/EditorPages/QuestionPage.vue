<template>
  <div class="editor-wrap">
    <!-- 顶部导航栏 -->
    <nav class="top-bar">
      <div class="left-area">
        <button class="bar-btn" @click="$emit('exit')">退出</button>
        <button class="bar-btn" @click="$emit('submit')">提交</button>
        <!-- <button class="bar-btn" @click="$emit('check')">检测</button> -->
      </div>

      <select v-model="selectedLanguage" class="language-select">
        <option value="c">C</option>
        <option value="cpp">C++</option>
        <option value="node">Node.js</option>
        <option value="py">Python</option>
        <option value="java">Java</option>
      </select>
    </nav>

    <!-- Monaco 容器 -->
    <div ref="el" class="monaco-target"></div>
  </div>
</template>

<script setup>
/* 下方 <script> 完全沿用你给出的代码，无需任何改动 */
import { ref, onMounted, onUnmounted, watch, nextTick, inject, computed } from 'vue'
import * as monaco from 'monaco-editor'
import { setupMyC } from '../../../../components/monaco-c'
import { setupMyCpp } from '../../../../components/monaco-cpp'
import { setupMyJava } from '../../../../components/monaco-java'
import { setupMyJs } from '../../../../components/monaco-js'
import { setupMyPy } from '../../../../components/monaco-py'

const lang    = inject('lang')
const setLang = inject('setLang')
const selectedLanguage = computed({
  get: () => lang.value,
  set: val => setLang(val)
})

const props = defineProps({ editorReady: Boolean })
const emit  = defineEmits(['exit', 'submit', 'check'])

const el = ref()
let ed = null
let dis = null

const getCode = () => ed?.getValue() || ''
const setCode = (userCode) => ed?.setValue(userCode)
defineExpose({ getCode, setCode, selectedLanguage })

const langConfig = {
  // 键是 <select> 中 option 的 value
  c: { 
    setup: setupMyC, 
    id: 'myC', 
    theme: 'myCTheme', 
    defaultCode: `#include <stdio.h>

int main() {
    printf("Hello,World");
    return 0;
    
    /* I/O 示例:
    
    // 输入/输出整数:
    int num;
    scanf("%d", &num);
    printf("%d\\n", num);
    
    // 输入/输出字符串:
    char str[100];
    scanf("%s", str); // 读取到空格
    // fgets(str, 100, stdin); // 读取整行
    printf("%s\\n", str);
    
    // 输出数组元素:
    // for (int i = 0; i < N; i++) { printf("%d ", arr[i]); }
    
    */
}` 
  },
  cpp: { 
    setup: setupMyCpp, 
    id: 'myCpp', 
    theme: 'myCppTheme', 
    defaultCode: `#include <iostream>
#include <string>

int main() {
    std::cout << "Hello,World" << std::endl;
    return 0;
    
    /* I/O 示例:
    
    // 输入/输出整数:
    int num;
    std::cin >> num;
    std::cout << num << std::endl;
    
    // 输入/输出字符串:
    std::string str;
    // std::cin >> str; // 遇到空格停止
    std::getline(std::cin, str); // 读取整行
    std::cout << str << std::endl;
    
    // 输出数组元素:
    // for (int num : arr) { std::cout << num << " "; }
    
    */
}` 
  },
  java: { 
    setup: setupMyJava, 
    id: 'myJava', 
    theme: 'myJavaTheme', 
    defaultCode: `import java.util.Scanner;

public class prog {
    public static void main(String[] args) {
        // Scanner scanner = new Scanner(System.in);
        System.out.println("Hello,World");
    }
    
    /* I/O 示例 (需在 main 函数内使用 Scanner):
    
    // 输入/输出整数:
    // int num = scanner.nextInt();
    // System.out.println(num);
    
    // 输入/输出字符串:
    // String str = scanner.nextLine(); // 读取整行
    // System.out.println(str);
    
    // 输入数组元素 (假设大小为 N):
    // int[] arr = new int[N];
    // for (int i = 0; i < N; i++) { arr[i] = scanner.nextInt(); }
    
    // 输出数组元素:
    // for (int val : arr) { System.out.print(val + " "); }
    
    */
}` 
  },
  node: { 
    setup: setupMyJs, // Node.js 使用 JavaScript 配置
    id: 'myJs', 
    theme: 'myJsTheme', 
    defaultCode: `console.log('Hello,World');

/* I/O 示例 (需封装在 readline 逻辑中):

// 导入和设置 readline:
// const readline = require('readline');
// const rl = readline.createInterface({ input: process.stdin, output: process.stdout });

// 输入/输出数字/字符串:
// rl.question('Prompt: ', (input) => {
//     const num = parseInt(input.trim());
//     console.log(num); 
//     rl.close();
// });

// 读取一行数据并处理成数组 (需配合 fs 模块或复杂 readline):
// const arr = input.trim().split(' ').map(Number);
// console.log(arr.join(' '));

*/` 
  },
  py: { 
    setup: setupMyPy, 
    id: 'myPy', 
    theme: 'myPyTheme', 
    defaultCode: `print("Hello,World")

''' I/O 示例:

# 输入一个数字/字符串:
s = input()
num = int(s) # 转换为整数
print(num)

# 输入一行数据并转换为数字数组 (以空格分隔):
arr = list(map(int, input().split()))
print(*arr) # 展开数组并以空格分隔输出

# 仅输出:
# print("Result:", result_value)

'''` 
  }
}

function initializeStorage() {
    const storedJson = localStorage.getItem('userCodeStorage');
    let initialStore = {};

    if (storedJson) {
        try {
            initialStore = JSON.parse(storedJson);
        } catch (e) {
            console.error('Failed to parse userCodeStorage from localStorage:', e);
            // 解析失败时，退回到默认配置
            initialStore = {};
        }
    }

    // 确保所有语言都有值，优先使用存储值，否则使用默认代码
    const storage = {};
    for (const key in langConfig) {
        storage[key] = initialStore[key] || langConfig[key].defaultCode;
    }
    return storage;
}

const userCodeStorage = ref(initializeStorage());

function create() {
    if (ed) return

    const currentLangKey = selectedLanguage.value;
    const config = langConfig[currentLangKey];
    
    if (!config) {
      console.error(`Unknown language selected: ${currentLangKey}`);
      return;
    }
    
    // 3. 注册当前语言的配置和主题
    config.setup(monaco);

    ed = monaco.editor.create(el.value, {
      // 使用当前语言的存储代码作为初始值
      value: userCodeStorage.value[currentLangKey], 
      language: config.id,
      theme: config.theme,
      automaticLayout: true,
      minimap: { enabled: false },
      autoClosingBrackets: 'always',
      autoClosingQuotes: 'always',
      autoSurround: 'languageDefined',
      formatOnType: true,
      formatOnPaste: true,
      autoIndent: 'full',
      suggestOnTriggerCharacters: true,
      quickSuggestions: true
    })
    
    // 4. 完善存取功能：监听内容变化，实时更新组件状态和 localStorage
    ed.onDidChangeModelContent(() => {
      // 实时更新组件内部存储
      const code = getCode();
      userCodeStorage.value[selectedLanguage.value] = code;
      
      // 更新 localStorage
      localStorage.setItem('userCodeStorage', JSON.stringify(userCodeStorage.value));
    });

    // 移除 onKeyDown 监听，因为 onDidChangeModelContent 已经足够强大
    dis = null; 
}


onMounted(() => { if (props.editorReady) create() })
onUnmounted(() => { dis?.dispose(); ed?.dispose() })
watch([() => props.editorReady, selectedLanguage], async ([ready, newLang], [oldReady, oldLang]) => { 
    if (ready && !ed) { 
        await nextTick(); 
        create(); 
    } else if (ed && newLang !== oldLang) {
        const config = langConfig[newLang];
        config.setup(monaco); 
        monaco.editor.setModelLanguage(ed.getModel(), config.id);
        monaco.editor.setTheme(config.theme);
        ed.setValue(userCodeStorage.value[newLang]);
        ed.setPosition({ lineNumber: 1, column: 1 });
    }
}, { immediate: true })
watch(() => props.editorReady, async r => { if (r) { await nextTick(); create() } }, { immediate: true })
</script>

<style scoped>
/* 布局：顶部栏 48px，下方 Monaco 占满剩余高度 */
.editor-wrap {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.top-bar {
  height: 48px;
  background: #d4efff;
  border-bottom: 1px solid #dbe2ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}
.left-area {
  display: flex;
  gap: 8px;
}
.bar-btn {
  padding: 6px 12px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.bar-btn:hover {
  background: #2563eb;
}
.language-select {
  padding: 4px 8px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #ffffff;
  background: #b9cfff;
  color: #151955;
  cursor: pointer;
}
.monaco-target {
  flex: 1;               /* 占满剩余空间 */
  width: 100%;
}
</style>
