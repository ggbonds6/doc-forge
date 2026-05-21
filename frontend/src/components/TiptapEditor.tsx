import { useEffect } from 'react';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Table from '@tiptap/extension-table';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import TableRow from '@tiptap/extension-table-row';
import Image from '@tiptap/extension-image';

interface Props {
  html: string;
  onChange: (html: string) => void;
}

export function TiptapEditor({ html, onChange }: Props) {
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: { levels: [1, 2, 3] },
      }),
      Table.configure({ resizable: true }),
      TableRow,
      TableHeader,
      TableCell,
      Image,
    ],
    content: html,
    onUpdate: ({ editor }) => onChange(editor.getHTML()),
  });

  useEffect(() => {
    if (editor && html !== editor.getHTML()) {
      editor.commands.setContent(html, false);
    }
  }, [editor, html]);

  if (!editor) {
    return <div className="panel">编辑器加载中...</div>;
  }

  return (
    <div className="editor-shell panel">
      <div className="editor-toolbar">
        <button type="button" onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}>
          标题1
        </button>
        <button type="button" onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}>
          标题2
        </button>
        <button type="button" onClick={() => editor.chain().focus().toggleBold().run()}>
          加粗
        </button>
        <button type="button" onClick={() => editor.chain().focus().toggleBulletList().run()}>
          列表
        </button>
        <button
          type="button"
          onClick={() => editor.chain().focus().insertTable({ rows: 2, cols: 2, withHeaderRow: true }).run()}
        >
          表格
        </button>
      </div>
      <EditorContent editor={editor} className="editor-content" />
    </div>
  );
}

