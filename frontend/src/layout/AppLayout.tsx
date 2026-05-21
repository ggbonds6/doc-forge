import { Link, Outlet, useNavigate } from 'react-router-dom';
import { setToken } from '../api/client';

export function AppLayout() {
  const navigate = useNavigate();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2>Doc Forge</h2>
        <nav>
          <Link to="/templates">模板库</Link>
          <Link to="/documents">文稿列表</Link>
          <Link to="/documents/new">新建文稿</Link>
          <Link to="/exports">导出记录</Link>
        </nav>
        <button
          type="button"
          className="ghost"
          onClick={() => {
            setToken(null);
            navigate('/login');
          }}
        >
          退出登录
        </button>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}

