// setupMyJava.js
/**
 * 为 Monaco Editor 设置自定义的 Java 语言（myJava）配置。
 * @param {import('monaco-editor')} monaco Monaco Editor 的全局实例。
 */
export function setupMyJava(monaco) {
  if (setupMyJava._installed) return
  setupMyJava._installed = true // 防止重复注册

  // Java 关键字
  const keywords = [
    'abstract', 'continue', 'for', 'new', 'switch', 'assert', 'default', 'goto', 'package',
    'synchronized', 'boolean', 'do', 'if', 'static', 'this', 'break', 'double', 'implements',
    'strictfp', 'throw', 'byte', 'else', 'import', 'super', 'throws', 'case', 'enum',
    'instanceof', 'return', 'transient', 'catch', 'extends', 'int', 'short', 'try', 'char',
    'final', 'interface', 'finally', 'long', 'void', 'class', 'float', 'native', 'volatile',
    'const', 'private', 'protected', 'public'
  ]

  // Java 内建类型
  const typeKeywords = [
    'boolean', 'double', 'byte', 'int', 'short', 'char', 'long', 'float', 'void'
  ]

  // 常见的标准 Java 类和对象
  const stdClasses = ['String', 'System', 'Integer', 'Double', 'Math', 'Exception', 'Object', 'Runnable', 'Thread']

  // 注册语言
  monaco.languages.register({ id: 'myJava' })

  // 设置 Monarch 词法分析器
  monaco.languages.setMonarchTokensProvider('myJava', {
    keywords,
    typeKeywords,
    stdClasses,
    operators: [
      '=', '>', '<', '!', '~', '?', ':', '==', '<=', '>=', '!=', '&&', '||', '++', '--',
      '+', '-', '*', '/', '&', '|', '^', '%', '<<', '>>', '>>>', '+=', '-=', '*=', '/=',
      '&=', '|=', '^=', '%=', '<<=', '>>=', '>>>='
    ],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    // 状态机
    tokenizer: {
      root: [
        // 标识符（包括关键字、类型、标准类和普通标识符）
        [/[a-zA-Z_$][\w$]*/, {
          cases: {
            '@keywords': 'keyword',
            '@typeKeywords': 'type',
            '@stdClasses': 'type.identifier', // 标准类用不同的高亮
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
        [/\d*\.\d+([eE][\-+]?\d+)?[fFdD]?/, 'number.float'],
        [/0[xX][0-9a-fA-F]+[Ll]?/, 'number.hex'],
        [/\d+[lL]?/, 'number'],

        // 字符串
        [/"([^"\\]|\\.)*$/, 'string.invalid'], // 未闭合的字符串
        [/"/, 'string', '@string'],

        // 字符
        [/'[^\\']'/, 'string'],
        [/'/, 'string.invalid']
      ],

      // 字符串状态
      string: [
        [/[^\\"]+/, 'string'],
        [/\\./, 'string.escape'],
        [/"/, 'string', '@pop']
      ],

      // 空白和注释状态
      whitespace: [
        [/[ \t\r\n]+/, ''],
        [/\/\*/, 'comment', '@comment'], // 多行注释
        [/\/\/.*$/, 'comment'] // 单行注释
      ],

      // 多行注释状态
      comment: [
        [/[^\/*]+/, 'comment'],
        [/\*\//, 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ],
    }
  })

  // 设置语言配置（括号匹配、自动闭合、缩进）
  monaco.languages.setLanguageConfiguration('myJava', {
    brackets: [['{', '}' ], ['[', ']' ], ['(', ')' ]],
    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"', notIn: ['string'] },
      { open: '\'', close: '\'', notIn: ['string', 'comment'] }
    ],
    comments: {
        lineComment: '//',
        blockComment: ['/*', '*/'],
    },
    indentationRules: {
      // 匹配 `{` 后增加缩进
      increaseIndentPattern: /.*\{[^}"']*$/,
      // 匹配 `}` 前减少缩进
      decreaseIndentPattern: /^\s*\}/
    }
  })

  // 自动补全
  const userWords = new Set()
  monaco.languages.registerCompletionItemProvider('myJava', {
    triggerCharacters: ['.', ' ', '('],
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      })

      // 收集用户已输入的单词作为建议
      const words = textUntilPosition.match(/\b[A-Za-z_$][A-Za-z0-9_$]*\b/g) || []
      words.forEach(w => userWords.add(w))

      // 1. 关键字建议
      const keywordSuggestions = keywords.map(k => ({
        label: k, kind: monaco.languages.CompletionItemKind.Keyword, insertText: k,
      }))

      // 2. 标准类建议
      const classSuggestions = stdClasses.map(c => ({
        label: c, kind: monaco.languages.CompletionItemKind.Class, insertText: c
      }))

      // 3. 用户自定义单词建议
      const userWordSuggestions = Array.from(userWords).map(w => ({
        label: w, kind: monaco.languages.CompletionItemKind.Text, insertText: w
      }))

      return { suggestions: [...keywordSuggestions, ...classSuggestions, ...userWordSuggestions] }
    }
  })

  // 主题
  monaco.editor.defineTheme('myJavaTheme', {
    base: 'vs', // 白色基础主题
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000CD', fontStyle: 'bold' }, // 深蓝
      { token: 'type', foreground: '008B8B' }, // 深青 (基本类型)
      { token: 'type.identifier', foreground: '8B008B', fontStyle: 'italic' }, // 深紫 (标准类)
      { token: 'number', foreground: 'B22222' }, // 深红
      { token: 'string', foreground: 'A31515' }, // 深棕
      { token: 'comment', foreground: '008000', fontStyle: 'italic' }, // 深绿
    ],
    colors: {
      'editor.background': '#FFFFFF', // 纯白背景
      'editor.foreground': '#000000', // 黑色文字
      'editorLineNumber.foreground': '#237893', // 行号颜色
    }
  })
}