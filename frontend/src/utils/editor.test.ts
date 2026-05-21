import { describe, expect, it } from 'vitest';
import { blocksToHtml, htmlToBlocks } from './editor';

describe('editor converters', () => {
  it('should convert blocks to html and back', () => {
    const blocks = [
      { type: 'heading' as const, level: 1, text: '标题' },
      { type: 'paragraph' as const, text: '正文' },
    ];

    const html = blocksToHtml(blocks);
    const converted = htmlToBlocks(html);

    expect(converted[0].type).toBe('heading');
    expect(converted[1].text).toBe('正文');
  });
});

