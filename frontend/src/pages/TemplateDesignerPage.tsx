import { ChangeEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getTemplateVersion, publishTemplate, publishTemplateWithOverride, reviewTemplateAcceptance, updateTemplateDraft } from '../api/service';
import type { TemplateAcceptanceCriterion, TemplateRuleProfile, TemplateVersionDetail } from '../api/types';

function cloneRuleProfile(profile: TemplateRuleProfile): TemplateRuleProfile {
  return JSON.parse(JSON.stringify(profile)) as TemplateRuleProfile;
}

export function TemplateDesignerPage() {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const [template, setTemplate] = useState<TemplateVersionDetail | null>(null);
  const [form, setForm] = useState<TemplateVersionDetail | null>(null);
  const [message, setMessage] = useState('');
  const [acceptanceComments, setAcceptanceComments] = useState<Record<string, string>>({});
  const [overrideReason, setOverrideReason] = useState('');

  useEffect(() => {
    getTemplateVersion(id).then((result) => {
      setTemplate(result);
      setForm({ ...result, ruleProfile: cloneRuleProfile(result.ruleProfile) });
    });
  }, [id]);

  function updateRule(mutator: (profile: TemplateRuleProfile) => void) {
    if (!form) return;
    const next = cloneRuleProfile(form.ruleProfile);
    mutator(next);
    setForm({ ...form, ruleProfile: next });
  }

  async function saveDraft() {
    if (!form) return;
    const result = await updateTemplateDraft(form.id, {
      name: form.name,
      description: form.description,
      ruleProfile: form.ruleProfile,
    });
    setTemplate(result);
    setForm({ ...result, ruleProfile: cloneRuleProfile(result.ruleProfile) });
    return result;
  }

  async function handleSave() {
    if (!form) return;
    try {
      await saveDraft();
      setMessage('草稿已保存');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '保存失败');
    }
  }

  async function handlePublish(useOverride = false) {
    if (!form) return;
    try {
      const saved = await saveDraft();
      if (!saved) return;
      const result = useOverride
        ? await publishTemplateWithOverride(saved.id, { overrideReason, overrideReviewer: '默认管理员' })
        : await publishTemplate(saved.id);
      setMessage(useOverride ? '模板已强制发布' : '模板已发布');
      navigate(`/templates/${result.id}`);
      setTemplate(result);
      setForm({ ...result, ruleProfile: cloneRuleProfile(result.ruleProfile) });
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '发布失败');
    }
  }

  async function handleAcceptance(criterion: TemplateAcceptanceCriterion, status: 'PASS' | 'FAIL') {
    if (!form) return;
    try {
      const saved = await saveDraft();
      if (!saved) return;
      const result = await reviewTemplateAcceptance(saved.id, {
        code: criterion.code,
        status,
        reviewer: '默认管理员',
        comment: acceptanceComments[criterion.code] ?? criterion.comment,
      });
      setTemplate(result);
      setForm({ ...result, ruleProfile: cloneRuleProfile(result.ruleProfile) });
      setMessage(status === 'PASS' ? '验收项已标记通过' : '验收项已标记不通过');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '更新验收结果失败');
    }
  }

  function statusText(status: string) {
    switch (status) {
      case 'PASS':
        return '通过';
      case 'FAIL':
        return '不通过';
      default:
        return '待验收';
    }
  }

  if (!form || !template) return <div className="panel">加载模板中...</div>;
  const isDraft = form.status === 'DRAFT';

  return (
    <div className="page">
      <div className="page-header">
        <h1>模板设计</h1>
        <p>调整版式、标题、正文和页边距等基础规则，再发布为新模板版本。</p>
      </div>
      <div className="two-column">
        <section className="panel form-grid">
          <label>
            模板名称
            <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
          </label>
          <label>
            模板描述
            <textarea value={form.description ?? ''} onChange={(event) => setForm({ ...form, description: event.target.value })} />
          </label>
          <label>
            上边距
            <input
              value={form.ruleProfile.page.marginTop ?? ''}
              onChange={(event: ChangeEvent<HTMLInputElement>) =>
                updateRule((profile) => {
                  profile.page.marginTop = event.target.value;
                })
              }
            />
          </label>
          <label>
            左边距
            <input
              value={form.ruleProfile.page.marginLeft ?? ''}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.page.marginLeft = event.target.value;
                })
              }
            />
          </label>
          <label>
            标题字体
            <input
              value={form.ruleProfile.titleStyle.fontFamily ?? ''}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.titleStyle.fontFamily = event.target.value;
                })
              }
            />
          </label>
          <label>
            标题字号
            <input
              type="number"
              value={form.ruleProfile.titleStyle.fontSize ?? 22}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.titleStyle.fontSize = Number(event.target.value);
                })
              }
            />
          </label>
          <label>
            正文字体
            <input
              value={form.ruleProfile.paragraphStyle.fontFamily ?? ''}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.paragraphStyle.fontFamily = event.target.value;
                })
              }
            />
          </label>
          <label>
            正文字号
            <input
              type="number"
              value={form.ruleProfile.paragraphStyle.fontSize ?? 16}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.paragraphStyle.fontSize = Number(event.target.value);
                })
              }
            />
          </label>
          <label>
            首行缩进
            <input
              value={form.ruleProfile.paragraphStyle.firstLineIndent ?? ''}
              onChange={(event) =>
                updateRule((profile) => {
                  profile.paragraphStyle.firstLineIndent = event.target.value;
                })
              }
            />
          </label>
          <div className="actions">
            <button type="button" onClick={handleSave} disabled={!isDraft}>
              保存草稿
            </button>
            <button type="button" onClick={() => void handlePublish(false)} disabled={!isDraft}>
              发布模板
            </button>
          </div>
          <label>
            强制发布理由
            <textarea value={overrideReason} onChange={(event) => setOverrideReason(event.target.value)} placeholder="仅在线下 WPS/Word 已验收但线上记录不完整时使用" />
          </label>
          <button type="button" className="ghost" onClick={() => void handlePublish(true)} disabled={!isDraft || !overrideReason.trim()}>
            带理由强制发布
          </button>
          {message ? <div className="success-text">{message}</div> : null}
        </section>
        <section className="panel">
          <h2>当前模板</h2>
          <ul className="meta-list">
            <li>版本：{template.versionLabel}</li>
            <li>状态：{template.status}</li>
            <li>编译产物：{template.compiledTemplatePath || '待生成'}</li>
            <li>验收状态：{template.previewBaseline.reviewStatus}</li>
            <li>规范基线：{template.previewBaseline.standardRef}</li>
          </ul>
          <h3>验收清单</h3>
          <ul className="acceptance-list">
            {template.previewBaseline.criteria.map((item) => (
              <li key={item.code}>
                <span className={item.blocking ? 'badge badge-blocking' : 'badge badge-warning'}>{item.blocking ? '必验' : '建议'}</span>
                <strong>{item.name}</strong>
                <p>{item.description}</p>
                <div className="acceptance-meta">
                  <span className={`acceptance-status acceptance-status-${item.status.toLowerCase()}`}>{statusText(item.status)}</span>
                  {item.reviewer ? <small>验收人：{item.reviewer}</small> : null}
                  {item.reviewedAt ? <small>时间：{new Date(item.reviewedAt).toLocaleString()}</small> : null}
                </div>
                {item.comment ? <p className="acceptance-comment">意见：{item.comment}</p> : null}
                {isDraft ? (
                  <div className="acceptance-actions">
                    <input
                      value={acceptanceComments[item.code] ?? item.comment ?? ''}
                      onChange={(event) => setAcceptanceComments({ ...acceptanceComments, [item.code]: event.target.value })}
                      placeholder="验收意见"
                    />
                    <button type="button" onClick={() => void handleAcceptance(item, 'PASS')}>
                      通过
                    </button>
                    <button type="button" className="ghost" onClick={() => void handleAcceptance(item, 'FAIL')}>
                      不通过
                    </button>
                  </div>
                ) : null}
              </li>
            ))}
          </ul>
        </section>
      </div>
    </div>
  );
}
