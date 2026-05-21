import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { exportDocument, getDocument, getTemplateVersion, importDocument, saveAsTemplate, updateDocument, validateDocument } from '../api/service';
import type { DocumentBlock, DocumentMetadata, TemplateRuleProfile, TemplateVersionDetail, ValidationIssue } from '../api/types';
import { A4Preview } from '../components/A4Preview';
import { TiptapEditor } from '../components/TiptapEditor';
import { blocksToHtml, htmlToBlocks } from '../utils/editor';

function cloneProfile<T>(value: T): T {
  return JSON.parse(JSON.stringify(value));
}

export function DocumentEditorPage() {
  const { id = '' } = useParams();
  const [metadata, setMetadata] = useState<DocumentMetadata | null>(null);
  const [template, setTemplate] = useState<TemplateVersionDetail | null>(null);
  const [blocks, setBlocks] = useState<DocumentBlock[]>([]);
  const [editorHtml, setEditorHtml] = useState('');
  const [localStyle, setLocalStyle] = useState<TemplateRuleProfile | null>(null);
  const [issues, setIssues] = useState<ValidationIssue[]>([]);
  const [notice, setNotice] = useState('');
  const [importText, setImportText] = useState('');
  const [importFormat, setImportFormat] = useState('markdown');
  const [importFile, setImportFile] = useState<File | null>(null);

  useEffect(() => {
    getDocument(id).then(async (document) => {
      setMetadata(document.metadata);
      setBlocks(document.bodyBlocks);
      setEditorHtml(blocksToHtml(document.bodyBlocks));
      const templateDetail = await getTemplateVersion(document.templateVersionId);
      setTemplate(templateDetail);
      setLocalStyle(document.localStyleOverrides ?? cloneProfile(templateDetail.ruleProfile));
    });
  }, [id]);

  const activeTemplateRule = useMemo(() => localStyle ?? template?.ruleProfile ?? null, [localStyle, template]);
  const blockingCount = issues.filter((issue) => issue.severity === 'BLOCKING').length;
  const warningCount = issues.filter((issue) => issue.severity === 'WARNING').length;

  async function handleSave() {
    if (!metadata || !template || !activeTemplateRule) return;
    const document = await updateDocument(id, {
      title: metadata.title ?? '',
      templateVersionId: template.id,
      metadata,
      bodyBlocks: blocks,
      localStyleOverrides: activeTemplateRule,
    });
    setNotice(`已保存：${new Date(document.updatedAt).toLocaleTimeString()}`);
  }

  async function handleValidate() {
    const result = await validateDocument(id);
    setIssues(result.issues);
    setNotice(result.pass ? '校验通过，可导出正式件' : '存在阻断级问题，请先处理');
  }

  async function handleExport() {
    const result = await exportDocument(id);
    setNotice(result.status === 'SUCCESS' ? `导出成功：${result.outputPath}` : `导出失败：${result.errorMessage}`);
  }

  async function handleImport(event: FormEvent) {
    event.preventDefault();
    const form = new FormData();
    form.append('format', importFormat);
    if (importText) {
      form.append('rawContent', importText);
    }
    if (importFile) {
      form.append('file', importFile);
    }
    const result = await importDocument(form);
    setBlocks(result.blocks);
    setEditorHtml(blocksToHtml(result.blocks));
    setNotice('导入完成，已替换正文内容');
  }

  async function handleSaveAsTemplate() {
    if (!activeTemplateRule) return;
    const name = window.prompt('请输入新模板名称', `${metadata?.docType ?? 'NOTICE'}机构模板`);
    if (!name) return;
    const result = await saveAsTemplate(id, {
      name,
      description: '来自文稿编辑页保存的模板草稿',
      styleOverrides: activeTemplateRule,
    });
    setNotice(`已保存为模板草稿：${result.name}`);
  }

  if (!metadata || !template || !activeTemplateRule) {
    return <div className="panel">加载文稿中...</div>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>文稿编辑</h1>
        <p>左侧编辑公文元数据，中间编辑正文，右侧查看校验和预览。</p>
      </div>
      <div className="editor-grid">
        <section className="panel form-grid">
          <h2>元数据</h2>
          <label>
            标题
            <input value={metadata.title ?? ''} onChange={(event) => setMetadata({ ...metadata, title: event.target.value })} />
          </label>
          <label>
            发文机关
            <input value={metadata.issuingAuthority ?? ''} onChange={(event) => setMetadata({ ...metadata, issuingAuthority: event.target.value })} />
          </label>
          <label>
            发文字号
            <input value={metadata.documentNumber ?? ''} onChange={(event) => setMetadata({ ...metadata, documentNumber: event.target.value })} />
          </label>
          <label>
            主送机关
            <textarea value={metadata.mainRecipients ?? ''} onChange={(event) => setMetadata({ ...metadata, mainRecipients: event.target.value })} />
          </label>
          <label>
            签发人
            <input value={metadata.signer ?? ''} onChange={(event) => setMetadata({ ...metadata, signer: event.target.value })} />
          </label>
          <label>
            成文日期
            <input type="date" value={metadata.issuedAt ?? ''} onChange={(event) => setMetadata({ ...metadata, issuedAt: event.target.value })} />
          </label>
          <label>
            印发机关
            <input value={metadata.printAuthority ?? ''} onChange={(event) => setMetadata({ ...metadata, printAuthority: event.target.value })} />
          </label>
          <label>
            印发日期
            <input
              type="date"
              value={metadata.printIssuedAt ?? ''}
              onChange={(event) => setMetadata({ ...metadata, printIssuedAt: event.target.value })}
            />
          </label>
          <form className="import-box" onSubmit={handleImport}>
            <h3>导入内容</h3>
            <select value={importFormat} onChange={(event) => setImportFormat(event.target.value)}>
              <option value="markdown">Markdown</option>
              <option value="html">HTML</option>
              <option value="plain">纯文本</option>
              <option value="docx">DOCX</option>
            </select>
            <textarea value={importText} onChange={(event) => setImportText(event.target.value)} placeholder="粘贴 Markdown / HTML / 纯文本" />
            <input type="file" accept=".docx,.txt,.md,.html" onChange={(event) => setImportFile(event.target.files?.[0] ?? null)} />
            <button type="submit">导入正文</button>
          </form>
        </section>

        <section>
          <TiptapEditor
            html={editorHtml}
            onChange={(html) => {
              setEditorHtml(html);
              setBlocks(htmlToBlocks(html));
            }}
          />
          <div className="editor-actions">
            <button type="button" onClick={handleSave}>
              保存文稿
            </button>
            <button type="button" onClick={handleValidate}>
              执行校验
            </button>
            <button type="button" onClick={handleExport}>
              导出 DOCX
            </button>
            <button type="button" className="ghost" onClick={handleSaveAsTemplate}>
              保存为模板草稿
            </button>
          </div>
          {notice ? <div className="success-text">{notice}</div> : null}
        </section>

        <section className="panel form-grid">
          <h2>局部样式</h2>
          <label>
            标题字体
            <input
              value={activeTemplateRule.titleStyle.fontFamily ?? ''}
              onChange={(event) =>
                setLocalStyle({
                  ...cloneProfile(activeTemplateRule),
                  titleStyle: { ...activeTemplateRule.titleStyle, fontFamily: event.target.value },
                })
              }
            />
          </label>
          <label>
            标题字号
            <input
              type="number"
              value={activeTemplateRule.titleStyle.fontSize ?? 22}
              onChange={(event) =>
                setLocalStyle({
                  ...cloneProfile(activeTemplateRule),
                  titleStyle: { ...activeTemplateRule.titleStyle, fontSize: Number(event.target.value) },
                })
              }
            />
          </label>
          <label>
            正文字体
            <input
              value={activeTemplateRule.paragraphStyle.fontFamily ?? ''}
              onChange={(event) =>
                setLocalStyle({
                  ...cloneProfile(activeTemplateRule),
                  paragraphStyle: { ...activeTemplateRule.paragraphStyle, fontFamily: event.target.value },
                })
              }
            />
          </label>
          <label>
            正文字号
            <input
              type="number"
              value={activeTemplateRule.paragraphStyle.fontSize ?? 16}
              onChange={(event) =>
                setLocalStyle({
                  ...cloneProfile(activeTemplateRule),
                  paragraphStyle: { ...activeTemplateRule.paragraphStyle, fontSize: Number(event.target.value) },
                })
              }
            />
          </label>
          <label>
            左边距
            <input
              value={activeTemplateRule.page.marginLeft ?? ''}
              onChange={(event) =>
                setLocalStyle({
                  ...cloneProfile(activeTemplateRule),
                  page: { ...activeTemplateRule.page, marginLeft: event.target.value },
                })
              }
            />
          </label>
          <div>
            <h3>校验结果</h3>
            <div className="issue-summary">
              <span className="badge badge-blocking">阻断 {blockingCount}</span>
              <span className="badge badge-warning">提醒 {warningCount}</span>
            </div>
            <ul className="issue-list">
              {issues.map((issue) => (
                <li key={`${issue.field}-${issue.message}`} className={issue.severity === 'BLOCKING' ? 'issue-blocking' : 'issue-warning'}>
                  <strong>{issue.field}</strong>：{issue.message}
                </li>
              ))}
              {!issues.length ? <li>尚未执行校验</li> : null}
            </ul>
          </div>
        </section>

        <section className="panel preview-panel">
          <h2>分页预览</h2>
          <A4Preview metadata={metadata} blocks={blocks} templateRule={activeTemplateRule} />
        </section>
      </div>
    </div>
  );
}
