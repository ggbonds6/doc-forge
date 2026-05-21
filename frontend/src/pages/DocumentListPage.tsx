import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listDocuments } from '../api/service';
import type { DocumentView } from '../api/types';

export function DocumentListPage() {
  const [documents, setDocuments] = useState<DocumentView[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    listDocuments().then(setDocuments).catch((loadError) => setError((loadError as Error).message));
  }, []);

  return (
    <div className="page">
      <div className="page-header">
        <h1>文稿列表</h1>
        <p>管理公文草稿、校验状态和已导出的正式件。</p>
      </div>
      {error ? <div className="panel error-text">{error}</div> : null}
      <div className="panel">
        <table className="data-table">
          <thead>
            <tr>
              <th>标题</th>
              <th>文种</th>
              <th>状态</th>
              <th>更新时间</th>
            </tr>
          </thead>
          <tbody>
            {documents.map((document) => (
              <tr key={document.id}>
                <td>
                  <Link to={`/documents/${document.id}`}>{document.title}</Link>
                </td>
                <td>{document.docType}</td>
                <td>{document.status}</td>
                <td>{new Date(document.updatedAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

