<template>
  <div class="markdown-body" v-html="renderedHtml" :class="containerClass"></div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import katex from 'katex';
import 'katex/dist/katex.min.css'; 

const props = defineProps({
  rawContent: {
    type: String,
    required: true,
  },
  containerClass: {
    type: String,
    default: '',
  }
});

const renderedHtml = ref('');

const renderMarkdownStructure = (content) => {
  if (!content) return '';
  let fixedContent = content;

  fixedContent = fixedContent.replace(/(^|\n)(#+)([^#\s])/g, '$1$2 $3');
  fixedContent = fixedContent.replace(/(\n#+)/g, '\n\n$1');
  fixedContent = fixedContent.replace(/(```)/g, '\n\n$1\n\n');
  fixedContent = fixedContent.replace(/(\n[*-] |\n\d+\. )/g, '\n\n$1');
  fixedContent = fixedContent.replace(/(\n\n\n+)/g, '\n\n');

  try {
    const rawHtml = marked(fixedContent);
    return DOMPurify.sanitize(rawHtml);
  } catch (error) {
    console.error('Markdown渲染错误:', error);
    return `<div class="error">渲染错误: ${error.message}</div>`;
  }
};

function finalRenderKaTeX(htmlContent) {
    if (!htmlContent) return '';
    let finalHtml = htmlContent;

    finalHtml = finalHtml.replace(/\$\$([\s\S]*?)\$\$/g, (match, formula) => {
        try {
            return katex.renderToString(formula.trim(), {
                throwOnError: false,
                displayMode: true 
            });
        } catch (e) {
            return `<div class="katex-error" style="color:red;font-weight:bold;">[公式错误] ${formula.trim()}</div>`;
        }
    });
    
    finalHtml = finalHtml.replace(/(^|[^$])\$([^$\n]+)\$(?![^$])/g, (match, prefix, formula) => {
        try {
            return prefix + katex.renderToString(formula.trim(), {
                throwOnError: false,
                displayMode: false 
            });
        } catch (e) {
            return prefix + `<span class="katex-error" style="color:red;font-weight:bold;">[公式错误] ${formula.trim()}</span>`;
        }
    });

    return finalHtml;
}

const updateRender = () => {
    const markedHtml = renderMarkdownStructure(props.rawContent);
    renderedHtml.value = finalRenderKaTeX(markedHtml);
};
watch(() => props.rawContent, updateRender, { immediate: true });
</script>

<style scoped>
</style>