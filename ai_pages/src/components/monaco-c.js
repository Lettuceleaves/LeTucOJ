// monaco-c.js
export function setupMyC(monaco) {
  if (setupMyC._installed) return
  setupMyC._installed = true   // 防止重复注册

  const keywords = [
    'auto','break','case','char','const','continue','default','do','double','else',
    'enum','extern','float','for','goto','if','inline','int','long','register',
    'restrict','return','short','signed','sizeof','static','struct','switch',
    'typedef','union','unsigned','void','volatile','while',
    '_Alignas','_Alignof','_Atomic','_Bool','_Complex','_Generic','_Imaginary',
    '_Noreturn','_Static_assert','_Thread_local'
  ]

  const stdFunc = ['printf','scanf','malloc','free','strlen','strcmp','strcpy']

  // 注册语言
  monaco.languages.register({ id: 'myC' })
  monaco.languages.setMonarchTokensProvider('myC', {
    keywords,
    stdFunc,
    typeKeywords: ['int','long','float','short','double','char','void','signed','unsigned'],
    operators: ['=','>','<','!','~','?',';',':','==','<=','>=','!=','&&','||','++','--',
                '+','-','*','/','&','|','^','%','<<','>>','+=','-=','*=','/=','&=','|=',
                '^=','%=','<<=','>>='],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,
    tokenizer: {
      root: [
        [/[a-zA-Z_]\w*/, {
          cases: {
            '@keywords': 'keyword',
            '@typeKeywords': 'type',
            '@stdFunc': 'stdFunc',
            '@default': 'identifier'
          }
        }],
        { include: '@whitespace' },
        [/[{}()\[\]]/, '@brackets'],
        [/@symbols/, { cases: { '@operators': 'operator', '@default': '' } }],
        [/\d*\.\d+([eE][\-+]?\d+)?[fF]?/, 'number.float'],
        [/0[xX][0-9a-fA-F]+/, 'number.hex'],
        [/\d+/, 'number'],
        [/".*?"/, 'string'],
        [/'[^\\']'/, 'string'],
        [/'/, 'string.invalid']
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

  monaco.languages.setLanguageConfiguration('myC', {
    brackets: [['{', '}' ], ['[', ']' ], ['(', ')' ]],
    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"', notIn: ['string'] },
      { open: '\'', close: '\'', notIn: ['string', 'comment'] }
    ],
    indentationRules: {
      increaseIndentPattern: /.*\{[^}"']*$/,
      decreaseIndentPattern: /^\s*\}/
    }
  })

  // 自动补全
  const userWords = new Set()
  monaco.languages.registerCompletionItemProvider('myC', {
    triggerCharacters: ['.', '>', ':', ' ', '('],
    provideCompletionItems: (model, position) => {
      const textUntilPosition = model.getValueInRange({
        startLineNumber: 1,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
      })

      const words = textUntilPosition.match(/\b[A-Za-z_][A-Za-z0-9_]*\b/g) || []
      words.forEach(w => userWords.add(w))

      const keywordSuggestions = keywords.map(k => ({
        label: k, kind: monaco.languages.CompletionItemKind.Keyword, insertText: k
      }))

      const funcSuggestions = stdFunc.map(fn => ({
        label: fn, kind: monaco.languages.CompletionItemKind.Function,
        insertText: fn + '()',
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
      }))

      const userWordSuggestions = Array.from(userWords).map(w => ({
        label: w, kind: monaco.languages.CompletionItemKind.Text, insertText: w
      }))

      return { suggestions: [...keywordSuggestions, ...funcSuggestions, ...userWordSuggestions] }
    }
  })

  // 主题
  monaco.editor.defineTheme('myCTheme', {
    base: 'vs',                       // ① 改为白色基础主题
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000CD', fontStyle: 'bold' }, // 深蓝
      { token: 'type', foreground: '008B8B' },                       // 深青
      { token: 'stdFunc', foreground: '8B008B', fontStyle: 'italic' }, // 深紫
      { token: 'number', foreground: 'B22222' },                     // 深红
      { token: 'string', foreground: 'A31515' },                     // 深棕
      { token: 'comment', foreground: '008000', fontStyle: 'italic' }, // 深绿
    ],
    colors: {
      'editor.background': '#FFFFFF',  // ② 纯白背景
      'editor.foreground': '#000000',  // 黑色文字
      'editorLineNumber.foreground': '#237893', // 行号颜色
    }
  })
}
