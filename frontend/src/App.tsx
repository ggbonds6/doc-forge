import { Navigate, Route, Routes } from 'react-router-dom';
import { getToken } from './api/client';
import { AppLayout } from './layout/AppLayout';
import { DocumentEditorPage } from './pages/DocumentEditorPage';
import { DocumentListPage } from './pages/DocumentListPage';
import { DocumentNewPage } from './pages/DocumentNewPage';
import { ExportJobsPage } from './pages/ExportJobsPage';
import { LoginPage } from './pages/LoginPage';
import { TemplateDesignerPage } from './pages/TemplateDesignerPage';
import { TemplateLibraryPage } from './pages/TemplateLibraryPage';

function ProtectedLayout() {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return <AppLayout />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedLayout />}>
        <Route path="/" element={<Navigate to="/templates" replace />} />
        <Route path="/templates" element={<TemplateLibraryPage />} />
        <Route path="/templates/:id" element={<TemplateDesignerPage />} />
        <Route path="/documents" element={<DocumentListPage />} />
        <Route path="/documents/new" element={<DocumentNewPage />} />
        <Route path="/documents/:id" element={<DocumentEditorPage />} />
        <Route path="/exports" element={<ExportJobsPage />} />
      </Route>
    </Routes>
  );
}

