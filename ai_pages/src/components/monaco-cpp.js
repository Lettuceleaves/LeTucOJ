/**
 * 为 Monaco Editor 设置自定义的 C++ 语言（myCpp）配置。
 * @param {import('monaco-editor')} monaco Monaco Editor 的全局实例。
 */
export function setupMyCpp(monaco) {
    if (setupMyCpp._installed) return
    setupMyCpp._installed = true // 防止重复注册

    // C/C++ 关键字 (合并了 C99 和 C++17 的所有关键字)
    const keywords = [
        'alignas', 'alignof', 'and', 'and_eq', 'asm', 'auto', 'break', 'case', 'catch',
        'class', 'const', 'const_cast', 'continue', 'decltype', 'default', 'delete', 'do',
        'double', 'dynamic_cast', 'else', 'enum', 'explicit', 'export', 'extern', 'false',
        'float', 'for', 'friend', 'goto', 'if', 'inline', 'int', 'long', 'mutable', 'namespace',
        'new', 'noexcept', 'not', 'not_eq', 'nullptr', 'operator', 'or', 'or_eq', 'private',
        'protected', 'public', 'register', 'reinterpret_cast', 'return', 'short', 'signed',
        'sizeof', 'static', 'static_assert', 'static_cast', 'struct', 'switch', 'template',
        'this', 'thread_local', 'throw', 'true', 'try', 'typedef', 'typeid', 'typename', 'union',
        'unsigned', 'using', 'virtual', 'void', 'volatile', 'wchar_t', 'while', 'xor', 'xor_eq',

        // C++20 关键字
        'char8_t', 'char16_t', 'char32_t', 'concept', 'consteval', 'constexpr', 'constinit', 
        'co_await', 'co_return', 'co_yield', 'requires'
    ]

    // C++ 常见的标准库类、对象和函数
    const stdLib = [
        'std', 'cout', 'cin', 'endl', 'string', 'vector', 'list', 'map', 'set', 'pair',
        'unique_ptr', 'shared_ptr', 'thread', 'mutex',
        'printf', 'scanf', 'malloc', 'free', 'strlen', 'strcmp', 'strcpy', 'abs', 'max', 'min',
        'sort', 'find', 'swap', 'make_pair', 'make_unique', 'make_shared'
    ]

    // C/C++ 基本类型
    const typeKeywords = [
        'int', 'long', 'float', 'short', 'double', 'char', 'void', 'signed', 'unsigned', 'bool'
    ]

    // 注册语言
    monaco.languages.register({ id: 'myCpp' })

    // 设置 Monarch 词法分析器
    monaco.languages.setMonarchTokensProvider('myCpp', {
        keywords,
        stdLib,
        typeKeywords,
        operators: ['=','>','<','!','~','?',':','==','<=','>=','!=','&&','||','++','--','->*',
                    '+','-','*','/','&','|','^','%','<<','>>','+=','-=','*=','/=','&=','|=',
                    '^=','%=','<<=','>>=','->', '::'],
        symbols: /[=><!~?:&|+\-*\/\^%]+/,

        // ❗ 移除了顶层的 preprocessor 数组
        
        tokenizer: {
            // ❗ 修复后的 root 状态
            root: [
                // 预处理指令 (必须放在最前面)
                [/#include/, 'preprocessor', '@include'], // 👈 直接在 root 状态处理 #include
                [/#\w+/, 'preprocessor'],                 // 👈 直接在 root 状态处理其他预处理指令

                // 标识符 (关键字、类型、标准库、普通标识符)
                [/[a-zA-Z_]\w*/, {
                    cases: {
                        '@keywords': 'keyword',
                        '@stdLib': 'stdFunc', 
                        '@typeKeywords': 'type',
                        '@default': 'identifier'
                    }
                }],
                
                // 空白、注释
                { include: '@whitespace' },
                
                // 分隔符
                [/[{}()\[\]]/, '@brackets'],
                
                // 运算符 (包括 C++ 特有的 `->` 和 `::`)
                [/::/, 'operator'], 
                [/@symbols|->|\*\./, { cases: { '@operators': 'operator', '@default': '' } }],
                
                // 数字 (浮点、十六进制、整数)
                [/\d*\.\d+([eE][\-+]?\d+)?[fFlL]?/, 'number.float'],
                [/0[xX][0-9a-fA-F]+[uULl]?/, 'number.hex'],
                [/\d+[uULl]?/, 'number'],
                
                // 字符串
                [/"([^"\\]|\\.)*$/, 'string.invalid'], 
                [/"/, 'string', '@string'],
                
                // 字符
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
            
            string: [
                [/[^\\"]+/, 'string'],
                [/\\./, 'string.escape'],
                [/"/, 'string', '@pop']
            ],

            // 预处理器 include 路径 (这个状态保持不变，因为它被 root 状态中的 #include 引用)
            include: [
                [/\s*<[^>]+>/, 'string.include', '@pop'], 
                [/\s*".*?"/, 'string.include', '@pop'], 
                [/.*/, 'preprocessor', '@pop'],
            ]
        }
    })

    // 设置语言配置 (括号匹配、自动闭合、缩进)
    monaco.languages.setLanguageConfiguration('myCpp', {
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
            increaseIndentPattern: /.*\{[^}"']*$/,
            decreaseIndentPattern: /^\s*\}/
        }
    })

    // 自动补全 (保持不变)
    const userWords = new Set()
    monaco.languages.registerCompletionItemProvider('myCpp', {
        triggerCharacters: ['.', '>', ':', ' ', '(', '<'], 
        provideCompletionItems: (model, position) => {
            const textUntilPosition = model.getValueInRange({
                startLineNumber: 1, startColumn: 1, endLineNumber: position.lineNumber, endColumn: position.column
            })
            const words = textUntilPosition.match(/\b[A-Za-z_][A-Za-z0-9_]*\b/g) || []
            words.forEach(w => userWords.add(w))

            const keywordSuggestions = keywords.map(k => ({
                label: k, kind: monaco.languages.CompletionItemKind.Keyword, insertText: k
            }))

            const stdSuggestions = stdLib.map(item => ({
                label: item, 
                kind: ['std', 'cout', 'cin', 'endl'].includes(item) ? monaco.languages.CompletionItemKind.Variable : (
                    ['string', 'vector', 'map'].includes(item) ? monaco.languages.CompletionItemKind.Class : monaco.languages.CompletionItemKind.Function
                ),
                insertText: item === 'std' ? item + '::' : item,
            }))

            const userWordSuggestions = Array.from(userWords).map(w => ({
                label: w, kind: monaco.languages.CompletionItemKind.Text, insertText: w
            }))

            return { suggestions: [...keywordSuggestions, ...stdSuggestions, ...userWordSuggestions] }
        }
    })

    // 主题 (保持不变)
    monaco.editor.defineTheme('myCppTheme', {
        base: 'vs', 
        inherit: true,
        rules: [
            { token: 'keyword', foreground: '0000CD', fontStyle: 'bold' },         
            { token: 'type', foreground: '008B8B' },                               
            { token: 'stdFunc', foreground: '8B008B', fontStyle: 'italic' },       
            { token: 'preprocessor', foreground: '9400D3' },                        
            { token: 'string.include', foreground: 'A31515' },                     
            { token: 'number', foreground: 'B22222' },                             
            { token: 'string', foreground: 'A31515' },                             
            { token: 'comment', foreground: '008000', fontStyle: 'italic' },       
        ],
        colors: {
            'editor.background': '#FFFFFF', 
            'editor.foreground': '#000000',
            'editorLineNumber.foreground': '#237893',
        }
    })
}