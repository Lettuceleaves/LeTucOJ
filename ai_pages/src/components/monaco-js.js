// monaco-js.js
/**
 * 为 Monaco Editor 设置自定义的 JavaScript 语言（myJs）配置。
 * @param {import('monaco-editor')} monaco Monaco Editor 的全局实例。
 */
export function setupMyJs(monaco) {
  if (setupMyJs._installed) return
  setupMyJs._installed = true // 防止重复注册

  // JavaScript 关键字 (ES6+)
  const keywords = [
    'break', 'case', 'catch', 'class', 'const', 'continue', 'debugger', 'default', 'delete',
    'do', 'else', 'export', 'extends', 'finally', 'for', 'function', 'if', 'import', 'in',
    'instanceof', 'new', 'return', 'super', 'switch', 'this', 'throw', 'try', 'typeof',
    'var', 'void', 'while', 'with', 'yield', 'async', 'await', 'let', 'static', 'implements',
    'interface', 'package', 'private', 'protected', 'public', 'enum', 'abstract', 'as',
    'from', 'get', 'set', 'of'
  ]

  // JavaScript 内建类型、对象和全局函数
  const stdLib = [
    'Array', 'Object', 'String', 'Number', 'Boolean', 'Date', 'RegExp', 'Map', 'Set', 'WeakMap',
    'WeakSet', 'Promise', 'Symbol', 'Error', 'Math', 'JSON', 'console', 'document', 'window',
    'setTimeout', 'setInterval', 'fetch', 'require', 'module', 'exports', 'global'
  ]

  // 伪类型关键字 (在 JS 中不严格是类型，但通常用作类型名)
  const typeKeywords = [
    'null', 'undefined', 'true', 'false'
  ]

  // 注册语言
  monaco.languages.register({ id: 'myJs' })

  // 设置 Monarch 词法分析器
  monaco.languages.setMonarchTokensProvider('myJs', {
    keywords,
    stdLib,
    typeKeywords,
    // JS 运算符
    operators: [
      '=', '>', '<', '!', '~', '?', ':', '==', '===', '!=', '!==', '<=', '>=', '&&', '||',
      '++', '--', '+', '-', '*', '/', '&', '|', '^', '%', '<<', '>>', '>>>', '+=', '-=',
      '*=', '/=', '&=', '|=', '^=', '%=', '<<=', '>>=', '>>>='
    ],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    // 状态机
    tokenizer: {
      root: [
        // 标识符 (关键字、类型、标准库、普通标识符)
        // 区分 let, const, var
        [/(let|const|var)(\s+)([a-zA-Z_$][\w$]*)/, ['keyword', 'white', 'variable.lang']],

        // 普通标识符
        [/[a-zA-Z_$][\w$]*/, {
          cases: {
            '@keywords': 'keyword',
            '@typeKeywords': 'constant', // true/false/null/undefined 视为常量
            '@stdLib': 'stdFunc',       // 标准库对象和函数
            '@default': 'identifier'
          }
        }],

        // 空白、注释
        { include: '@whitespace' },

        // 分隔符
        [/[{}()\[\]]/, '@brackets'],

        // 运算符
        [/@symbols/, { cases: { '@operators': 'operator', '@default': '' } }],

        // 数字
        [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
        [/0[xX][0-9a-fA-F]+/, 'number.hex'],
        [/\d+/, 'number'],

        // 字符串 (双引号)
        [/"([^"\\]|\\.)*$/, 'string.invalid'], // 未闭合
        [/"/, 'string', '@string_double'],

        // 字符串 (单引号)
        [/'([^'\\]|\\.)*$/, 'string.invalid'], // 未闭合
        [/'/, 'string', '@string_single'],

        // 模板字符串 (反引号)
        [/`/, 'string.template', '@string_template'],

        // 正则表达式 (需要在 root 状态末尾处理，以避免与除法操作符冲突)
        [/(\/)(.+?)(\/)([gimusy]{0,6})/g, ['regexp.delimiter', 'regexp', 'regexp.delimiter', 'regexp.modifier']],
      ],

      // 双引号字符串
      string_double: [
        [/[^\\"]+/, 'string'],
        [/\\./, 'string.escape'],
        [/"/, 'string', '@pop']
      ],

      // 单引号字符串
      string_single: [
        [/[^\\']+/, 'string'],
        [/\\./, 'string.escape'],
        [/'/, 'string', '@pop']
      ],

      // 模板字符串
      string_template: [
        [/\$\{/, 'string.interpolation', '@string_interp'], // 变量插值开始
        [/[^\\`$]+/, 'string.template'],
        [/\\./, 'string.escape'],
        [/`/, 'string.template', '@pop']
      ],

      // 模板字符串插值内
      string_interp: [
        // 允许任何 token，直到遇到 '}'
        [/[^}]/, ''],
        [/\}/, 'string.interpolation', '@pop'] // 插值结束
      ],

      whitespace: [
        [/[ \t\r\n]+/, ''],
        [/\/\*/, 'comment', '@comment'],
        [/\/\/.*$/, 'comment']
      ],

      comment: [
        [/[^\/*]+/, 'comment'],
        [/\*\//, 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ],
    }
  })

  // 设置语言配置 (括号匹配、自动闭合、缩进)
  monaco.languages.setLanguageConfiguration('myJs', {
    brackets: [['{', '}' ], ['[', ']' ], ['(', ')' ]],
    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"', notIn: ['string'] },
      { open: '\'', close: '\'', notIn: ['string', 'comment'] },
      { open: '`', close: '`', notIn: ['string', 'comment'] },
    ],
    comments: {
        lineComment: '//',
        blockComment: ['/*', '*/'],
    },
    indentationRules: {
      increaseIndentPattern: /.*\{[^}"']*$/,
      decreaseIndentPattern: /^\s*\}/
    }
  })

  // 自动补全
  const userWords = new Set()
  monaco.languages.registerCompletionItemProvider('myJs', {
    // JS 常用触发字符：点(.) 成员访问
    triggerCharacters: ['.', ' ', '('],
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      })

      // 收集用户已输入的单词
      const words = textUntilPosition.match(/\b[A-Za-z_$][\w$]*\b/g) || []
      words.forEach(w => userWords.add(w))

      // 1. 关键字建议
      const keywordSuggestions = keywords.map(k => ({
        label: k,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: k
      }))

      // 2. 标准库建议 (对象/函数)
      const stdSuggestions = stdLib.map(item => ({
        label: item,
        kind: ['setTimeout', 'setInterval', 'fetch'].includes(item) ? monaco.languages.CompletionItemKind.Function : monaco.languages.CompletionItemKind.Class,
        insertText: item
      }))

      // 3. 伪类型/常量建议
      const constantSuggestions = typeKeywords.map(c => ({
        label: c,
        kind: monaco.languages.CompletionItemKind.Constant,
        insertText: c
      }))

      // 4. 用户自定义单词建议
      const userWordSuggestions = Array.from(userWords).map(w => ({
        label: w,
        kind: monaco.languages.CompletionItemKind.Text,
        insertText: w
      }))

      return { suggestions: [...keywordSuggestions, ...stdSuggestions, ...constantSuggestions, ...userWordSuggestions] }
    }
  })

  // 主题
  monaco.editor.defineTheme('myJsTheme', {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000CD', fontStyle: 'bold' },         // 深蓝 (let, const, function, if)
      { token: 'stdFunc', foreground: '8B008B', fontStyle: 'italic' },       // 深紫 (console, Array, fetch)
      { token: 'constant', foreground: 'B22222' },                           // 深红 (true, false, null, undefined)
      { token: 'variable.lang', foreground: '008B8B', fontStyle: 'italic' }, // 深青 (let/const/var 声明的变量)
      { token: 'number', foreground: 'B22222' },                             // 深红
      { token: 'string', foreground: 'A31515' },                             // 深棕
      { token: 'string.template', foreground: 'A31515' },                    // 模板字符串
      { token: 'string.interpolation', foreground: 'FF4500' },               // 插值表达式 ${}
      { token: 'regexp', foreground: '800080' },                             // 正则表达式
      { token: 'comment', foreground: '008000', fontStyle: 'italic' },       // 深绿
    ],
    colors: {
      'editor.background': '#FFFFFF',
      'editor.foreground': '#000000',
      'editorLineNumber.foreground': '#237893',
    }
  })
}
