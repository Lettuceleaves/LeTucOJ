// monaco-py.js
/**
 * 为 Monaco Editor 设置自定义的 Python 语言（myPy）配置。
 * @param {import('monaco-editor')} monaco Monaco Editor 的全局实例。
 */
export function setupMyPy(monaco) {
  if (setupMyPy._installed) return
  setupMyPy._installed = true // 防止重复注册

  // Python 关键字 (Python 3)
  const keywords = [
    'False', 'None', 'True', 'and', 'as', 'assert', 'async', 'await', 'break', 'class',
    'continue', 'def', 'del', 'elif', 'else', 'except', 'finally', 'for', 'from', 'global',
    'if', 'import', 'in', 'is', 'lambda', 'nonlocal', 'not', 'or', 'pass', 'raise',
    'return', 'try', 'while', 'with', 'yield'
  ]

  // Python 内建函数 (常见部分)
  const builtins = [
    'print', 'len', 'range', 'input', 'list', 'dict', 'set', 'tuple', 'str', 'int',
    'float', 'bool', 'sum', 'min', 'max', 'open', 'id', 'type', 'dir', 'zip', 'map',
    'filter', 'sorted', 'enumerate', 'abs', 'round', 'chr', 'ord', 'hex', 'bin'
  ]

  // 伪类型/特殊常量 (None, True, False 已在 keywords 中)
  const typeKeywords = [
    // Python 中没有严格的类型关键字，但可以把常见的类型构造函数放在这里或 builtins 里
  ]

  // 注册语言
  monaco.languages.register({ id: 'myPy' })

  // 设置 Monarch 词法分析器
  monaco.languages.setMonarchTokensProvider('myPy', {
    keywords,
    builtins,
    typeKeywords,
    // Python 运算符
    operators: [
      '=', '>', '<', '!', '==', '>=', '<=', '!=', 'and', 'or', 'not', 'is', 'in', '+=',
      '-=', '*=', '/=', '//=', '%=', '**=', '<<=', '>>=', '&=', '|=', '^=', '~',
      '+', '-', '*', '/', '//', '%', '**', '<<', '>>', '&', '|', '^'
    ],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    tokenizer: {
      root: [
        // 标识符 (关键字、内建函数、普通标识符)
        // def/class 后面的名称作为特殊标识符
        [/(\b(def|class)\b)(\s+)([a-zA-Z_][\w]*)/, ['keyword', 'white', 'identifier.definition']],

        [/[a-zA-Z_]\w*/, {
          cases: {
            '@keywords': 'keyword',
            '@builtins': 'stdFunc',      // 内建函数
            '@typeKeywords': 'type',
            '@default': 'identifier'
          }
        }],

        // 空白、注释
        { include: '@whitespace' },

        // 分隔符 (Python 主要用 : 和 ,)
        [/[\[\]{}():,.]/, '@brackets'],

        // 运算符
        [/@symbols|~/, { cases: { '@operators': 'operator', '@default': '' } }],

        // 数字 (整数、浮点数、十六进制、二进制)
        [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
        [/0[xX][0-9a-fA-F]+/, 'number.hex'],
        [/0[bB][01]+/, 'number.binary'],
        [/\d+/, 'number'],

        // 字符串 (单引号、双引号、f-string 等)
        [/(?:[rR]|[fF]|[uU]|[bB])?"/, { token: 'string', next: '@string_double' }],
        [/(?:[rR]|[fF]|[uU]|[bB])?'/, { token: 'string', next: '@string_single' }],
        
        // 文档字符串 (三引号，必须放在单/双引号后，否则会被误判为普通字符串)
        [/"""/, { token: 'string.doc', next: '@docstring_double' }],
        [/'''/, { token: 'string.doc', next: '@docstring_single' }]
      ],

      // 双引号字符串 (用于普通字符串和 f-string)
      string_double: [
        [/\\./, 'string.escape'],
        [/"/, 'string', '@pop'],
        [/\{[^}]*?\}/, 'string.interpolation'], // f-string 插值
        [/[^\\"{]+/, 'string']
      ],

      // 单引号字符串
      string_single: [
        [/\\./, 'string.escape'],
        [/'/, 'string', '@pop'],
        [/\{[^}]*?\}/, 'string.interpolation'], // f-string 插值
        [/[^\\'{]+/, 'string']
      ],

      // 三引号文档字符串 (双引号)
      docstring_double: [
        [/[^"\\]+/, 'string.doc'],
        [/\\./, 'string.escape'],
        [/"""/, 'string.doc', '@pop'],
        [/"/, 'string.doc']
      ],

      // 三引号文档字符串 (单引号)
      docstring_single: [
        [/[^'\\]+/, 'string.doc'],
        [/\\./, 'string.escape'],
        [/'''/, 'string.doc', '@pop'],
        [/'/, 'string.doc']
      ],

      // 空白和注释状态
      whitespace: [
        [/[ \t\r\n]+/, ''],
        [/#.*$/, 'comment']
      ],
    }
  })

  // 设置语言配置 (括号匹配、自动闭合、缩进)
  monaco.languages.setLanguageConfiguration('myPy', {
    brackets: [['{', '}' ], ['[', ']' ], ['(', ')' ]],
    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"', notIn: ['string', 'comment', 'string.doc'] },
      { open: '\'', close: '\'', notIn: ['string', 'comment', 'string.doc'] }
    ],
    comments: {
        lineComment: '#',
    },
    // Python 的缩进规则基于行尾的冒号 (:)
    indentationRules: {
      increaseIndentPattern: /:$/,
      decreaseIndentPattern: /^\s*(return|raise|break|continue|pass|finally|except|else|elif)\b/
    }
  })

  // 自动补全
  const userWords = new Set()
  monaco.languages.registerCompletionItemProvider('myPy', {
    triggerCharacters: ['.', ' ', '('],
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      })

      const words = textUntilPosition.match(/\b[A-Za-z_][A-Za-z0-9_]*\b/g) || []
      words.forEach(w => userWords.add(w))

      // 1. 关键字建议
      const keywordSuggestions = keywords.map(k => ({
        label: k, kind: monaco.languages.CompletionItemKind.Keyword, insertText: k
      }))

      // 2. 内建函数建议 (带 () )
      const builtinSuggestions = builtins.map(fn => ({
        label: fn, kind: monaco.languages.CompletionItemKind.Function,
        insertText: fn + '($1)',
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
      }))
      
      // 3. 常见代码段 (Snippet) 建议
      const snippetSuggestions = [
          {
            label: 'if __name__ == "__main__"',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: "if __name__ == '__main__':\n\t$0",
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
          },
          {
            label: 'for loop',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: "for ${1:item} in ${2:iterable}:\n\t$0",
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
          },
          {
            label: 'try/except',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: "try:\n\t$1\nexcept ${2:Exception} as ${3:e}:\n\t$0",
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
          }
      ];

      // 4. 用户自定义单词建议
      const userWordSuggestions = Array.from(userWords).map(w => ({
        label: w, kind: monaco.languages.CompletionItemKind.Text, insertText: w
      }))

      return { suggestions: [
        ...keywordSuggestions, 
        ...builtinSuggestions, 
        ...snippetSuggestions,
        ...userWordSuggestions
      ]}
    }
  })

  // 主题
  monaco.editor.defineTheme('myPyTheme', {
    base: 'vs', 
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000CD', fontStyle: 'bold' },         // 深蓝 (if, def, class, for)
      { token: 'stdFunc', foreground: '8B008B', fontStyle: 'italic' },       // 深紫 (print, len, range)
      { token: 'identifier.definition', foreground: '008B8B', fontStyle: 'bold' }, // 深青 (def/class 后的名称)
      { token: 'number', foreground: 'B22222' },                             // 深红
      { token: 'string', foreground: 'A31515' },                             // 深棕
      { token: 'string.doc', foreground: 'A31515', fontStyle: 'italic' },    // 文档字符串
      { token: 'string.interpolation', foreground: 'FF4500' },               // f-string 插值
      { token: 'comment', foreground: '008000', fontStyle: 'italic' },       // 深绿
    ],
    colors: {
      'editor.background': '#FFFFFF', 
      'editor.foreground': '#000000',
      'editorLineNumber.foreground': '#237893',
    }
  })
}