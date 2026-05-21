import { useEffect, useState } from 'react';
import { downloadExportJob, listExportJobs } from '../api/service';
import type { ExportJobView } from '../api/types';

export function ExportJobsPage() {
  const [jobs, setJobs] = useState<ExportJobView[]>([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    listExportJobs().then(setJobs);
  }, []);

  async function handleDownload(job: ExportJobView) {
    try {
      const blob = await downloadExportJob(job.id);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `govdoc-${job.id}.docx`;
      anchor.click();
      URL.revokeObjectURL(url);
      setMessage('下载已开始');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '下载失败');
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>导出记录</h1>
        <p>查看导出状态，并下载成功生成的 DOCX。</p>
      </div>
      <div className="panel">
        {message ? <div className="success-text">{message}</div> : null}
        <table className="data-table">
          <thead>
            <tr>
              <th>任务</th>
              <th>文稿</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <tr key={job.id}>
                <td>{job.id}</td>
                <td>{job.documentId}</td>
                <td>{job.status}</td>
                <td>
                  {job.status === 'SUCCESS' ? (
                    <button type="button" className="ghost" onClick={() => void handleDownload(job)}>
                      下载
                    </button>
                  ) : (
                    job.errorMessage || '-'
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
