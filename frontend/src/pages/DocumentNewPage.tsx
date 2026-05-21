import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createDocument, listTemplatePacks } from '../api/service';
import type { DocType, DocumentMetadata, TemplatePackSummary } from '../api/types';

const EMPTY_BLOCKS = [{ type: 'paragraph' as const, text: '请在此输入正文内容。' }];

export function DocumentNewPage() {
  const [packs, setPacks] = useState<TemplatePackSummary[]>([]);
  const [packCode, setPackCode] = useState('NOTICE');
  const [title, setTitle] = useState('关于事项安排的通知');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    listTemplatePacks().then(setPacks).catch((loadError) => setError((loadError as Error).message));
  }, []);

  const currentPack = useMemo(() => packs.find((pack) => pack.code === packCode), [packCode, packs]);
  const currentTemplate = currentPack?.versions.find((version) => version.status === 'PUBLISHED') ?? currentPack?.versions[0];

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!currentTemplate) return;
    const metadata: DocumentMetadata = {
      docType: packCode as DocType,
      title,
      issuingAuthority: '某某机关',
      mainRecipients: '各有关单位：',
      documentNumber: '某政发〔2026〕1号',
      signatory: '某某机关',
      issuedAt: new Date().toISOString().slice(0, 10),
      printAuthority: '某某机关办公室',
      printIssuedAt: new Date().toISOString().slice(0, 10),
    };
    const document = await createDocument({
      title,
      templateVersionId: currentTemplate.id,
      metadata,
      bodyBlocks: EMPTY_BLOCKS,
      localStyleOverrides: null,
    });
    navigate(`/documents/${document.id}`);
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>新建文稿</h1>
        <p>先选择文种和模板版本，再进入正式编辑页面。</p>
      </div>
      <form className="panel form-grid" onSubmit={handleSubmit}>
        <label>
          文种
          <select value={packCode} onChange={(event) => setPackCode(event.target.value)}>
            <option value="NOTICE">通知</option>
            <option value="REQUEST">请示</option>
            <option value="LETTER">函</option>
            <option value="REPORT">报告</option>
          </select>
        </label>
        <label>
          标题
          <input value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <div>模板版本：{currentTemplate ? `${currentTemplate.name} / ${currentTemplate.versionLabel}` : '暂无可用模板'}</div>
        {error ? <div className="error-text">{error}</div> : null}
        <button type="submit" disabled={!currentTemplate}>
          创建并进入编辑
        </button>
      </form>
    </div>
  );
}

