import type { DocumentBlock } from '../api/types';

function escapeHtml(text: string) {
  return text
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

export function blocksToHtml(blocks: DocumentBlock[]) {
  return blocks
    .map((block) => {
      switch (block.type) {
        case 'heading':
          return `<h${block.level ?? 1}>${escapeHtml(block.text ?? '')}</h${block.level ?? 1}>`;
        case 'list':
          return `<${block.ordered ? 'ol' : 'ul'}>${(block.items ?? [])
            .map((item) => `<li>${escapeHtml(item)}</li>`)
            .join('')}</${block.ordered ? 'ol' : 'ul'}>`;
        case 'table':
          return `<table>${(block.rows ?? [])
            .map((row) => `<tr>${row.map((cell) => `<td>${escapeHtml(cell)}</td>`).join('')}</tr>`)
            .join('')}</table>`;
        case 'image':
          return `<p><img src="${escapeHtml(block.src ?? '')}" alt="${escapeHtml(block.caption ?? '')}" /></p>`;
        default:
          return `<p>${escapeHtml(block.text ?? '')}</p>`;
      }
    })
    .join('');
}

export function htmlToBlocks(html: string): DocumentBlock[] {
  const parser = new DOMParser();
  const document = parser.parseFromString(html, 'text/html');
  const blocks: DocumentBlock[] = [];
  Array.from(document.body.children).forEach((element) => {
    const tag = element.tagName.toLowerCase();
    if (tag === 'h1' || tag === 'h2' || tag === 'h3') {
      blocks.push({ type: 'heading', level: Number(tag.substring(1)), text: element.textContent ?? '' });
      return;
    }
    if (tag === 'ul' || tag === 'ol') {
      blocks.push({
        type: 'list',
        ordered: tag === 'ol',
        items: Array.from(element.querySelectorAll('li')).map((node) => node.textContent ?? ''),
      });
      return;
    }
    if (tag === 'table') {
      blocks.push({
        type: 'table',
        rows: Array.from(element.querySelectorAll('tr')).map((row) =>
          Array.from(row.querySelectorAll('th,td')).map((cell) => cell.textContent ?? ''),
        ),
      });
      return;
    }
    const image = element.querySelector('img');
    if (image) {
      blocks.push({
        type: 'image',
        src: image.getAttribute('src') ?? '',
        caption: image.getAttribute('alt') ?? '',
      });
      return;
    }
    if ((element.textContent ?? '').trim()) {
      blocks.push({ type: 'paragraph', text: element.textContent ?? '' });
    }
  });
  return blocks;
}

