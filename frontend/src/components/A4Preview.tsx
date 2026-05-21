import type { DocumentBlock, DocumentMetadata, TemplateRuleProfile } from '../api/types';

interface Props {
  metadata: DocumentMetadata;
  blocks: DocumentBlock[];
  templateRule: TemplateRuleProfile;
}

export function A4Preview({ metadata, blocks, templateRule }: Props) {
  const paragraphStyle = templateRule.paragraphStyle;
  const titleStyle = templateRule.titleStyle;

  return (
    <div className="a4-page">
      <div className="preview-header" style={{ color: '#b91c1c', fontFamily: titleStyle.fontFamily }}>
        {metadata.issuingAuthority || '发文机关'}
      </div>
      <div className="preview-number-line">
        <span>{metadata.documentNumber}</span>
        <span>{metadata.signer ? `签发人：${metadata.signer}` : ''}</span>
      </div>
      <h1
        style={{
          fontFamily: titleStyle.fontFamily,
          fontSize: `${titleStyle.fontSize ?? 22}px`,
          textAlign: (titleStyle.align as 'center') ?? 'center',
        }}
      >
        {metadata.title || '公文标题'}
      </h1>
      <p className="preview-recipient">{metadata.mainRecipients || '主送机关：'}</p>
      <div className="preview-body" style={{ fontFamily: paragraphStyle.fontFamily, fontSize: `${paragraphStyle.fontSize ?? 16}px` }}>
        {blocks.map((block, index) => {
          if (block.type === 'heading') {
            const style =
              block.level === 1
                ? templateRule.heading1Style
                : block.level === 2
                  ? templateRule.heading2Style
                  : templateRule.heading3Style;
            return (
              <p key={index} style={{ fontFamily: style.fontFamily, fontWeight: style.bold ? 700 : 400 }}>
                {block.text}
              </p>
            );
          }
          if (block.type === 'list') {
            return (
              <ul key={index}>
                {(block.items ?? []).map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            );
          }
          if (block.type === 'table') {
            return (
              <table key={index} className="preview-table">
                <tbody>
                  {(block.rows ?? []).map((row, rowIndex) => (
                    <tr key={rowIndex}>
                      {row.map((cell, cellIndex) => (
                        <td key={cellIndex}>{cell}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            );
          }
          if (block.type === 'image') {
            return <p key={index}>[图片] {block.caption}</p>;
          }
          return (
            <p key={index} className="preview-paragraph">
              {block.text}
            </p>
          );
        })}
      </div>
      <div className="preview-signature">
        <div>{metadata.signatory}</div>
        <div>{metadata.issuedAt}</div>
      </div>
      <div className="preview-footer">
        <div>{metadata.printAuthority}</div>
        <div>{metadata.printIssuedAt}</div>
      </div>
    </div>
  );
}

