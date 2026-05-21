import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTemplateDraft, listTemplatePacks } from '../api/service';
import type { TemplatePackSummary } from '../api/types';

export function TemplateLibraryPage() {
  const [packs, setPacks] = useState<TemplatePackSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    listTemplatePacks()
      .then(setPacks)
      .catch((loadError) => setError((loadError as Error).message))
      .finally(() => setLoading(false));
  }, []);

  async function handleCreateDraft(pack: TemplatePackSummary) {
    const published = pack.versions.find((version) => version.status === 'PUBLISHED') ?? pack.versions[0];
    const draft = await createTemplateDraft({
      packCode: pack.code,
      sourceTemplateVersionId: published?.id,
      name: `${pack.name}机构版`,
      description: '从内置模板复制的机构模板',
    });
    navigate(`/templates/${draft.id}`);
  }

  if (loading) return <div className="panel">加载模板中...</div>;
  if (error) return <div className="panel error-text">{error}</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>模板库</h1>
        <p>内置通知、请示、函、报告标准模板包，可复制为机构模板后再发布。</p>
      </div>
      <div className="card-grid">
        {packs.map((pack) => (
          <section key={pack.id} className="panel">
            <h2>{pack.name}</h2>
            <p>{pack.description}</p>
            <ul className="meta-list">
              {pack.versions.map((version) => (
                <li key={version.id}>
                  <strong>{version.name}</strong> {version.versionLabel} / {version.status}
                </li>
              ))}
            </ul>
            <button type="button" onClick={() => handleCreateDraft(pack)}>
              复制为草稿模板
            </button>
          </section>
        ))}
      </div>
    </div>
  );
}

