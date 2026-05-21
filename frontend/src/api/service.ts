import { apiDownload, apiFetch } from './client';
import type {
  DocumentBlock,
  DocumentMetadata,
  DocumentView,
  ExportJobView,
  TemplatePackSummary,
  TemplateRuleProfile,
  TemplateVersionDetail,
  ValidationResult,
} from './types';

export function login(username: string, password: string) {
  return apiFetch<{ token: string; username: string }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export function listTemplatePacks() {
  return apiFetch<TemplatePackSummary[]>('/template-packs');
}

export function getTemplateVersion(id: string) {
  return apiFetch<TemplateVersionDetail>(`/templates/${id}`);
}

export function createTemplateDraft(payload: {
  packCode: string;
  sourceTemplateVersionId?: string;
  name: string;
  description?: string;
}) {
  return apiFetch<TemplateVersionDetail>('/templates/drafts', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateTemplateDraft(id: string, payload: { name: string; description?: string; ruleProfile: TemplateRuleProfile }) {
  return apiFetch<TemplateVersionDetail>(`/templates/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function publishTemplate(id: string) {
  return apiFetch<TemplateVersionDetail>(`/templates/${id}/publish`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
}

export function publishTemplateWithOverride(id: string, payload: { overrideReason: string; overrideReviewer?: string }) {
  return apiFetch<TemplateVersionDetail>(`/templates/${id}/publish`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function reviewTemplateAcceptance(
  id: string,
  payload: { code: string; status: 'PASS' | 'FAIL'; reviewer?: string; comment?: string },
) {
  return apiFetch<TemplateVersionDetail>(`/templates/${id}/acceptance`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function listDocuments() {
  return apiFetch<DocumentView[]>('/documents');
}

export function getDocument(id: string) {
  return apiFetch<DocumentView>(`/documents/${id}`);
}

export function createDocument(payload: {
  title: string;
  templateVersionId: string;
  metadata: DocumentMetadata;
  bodyBlocks: DocumentBlock[];
  localStyleOverrides?: TemplateRuleProfile | null;
}) {
  return apiFetch<DocumentView>('/documents', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateDocument(
  id: string,
  payload: {
    title: string;
    templateVersionId: string;
    metadata: DocumentMetadata;
    bodyBlocks: DocumentBlock[];
    localStyleOverrides?: TemplateRuleProfile | null;
  },
) {
  return apiFetch<DocumentView>(`/documents/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function validateDocument(id: string) {
  return apiFetch<ValidationResult>(`/documents/${id}/validate`, {
    method: 'POST',
  });
}

export function exportDocument(id: string) {
  return apiFetch<ExportJobView>(`/documents/${id}/export-docx`, {
    method: 'POST',
  });
}

export function listExportJobs() {
  return apiFetch<ExportJobView[]>('/export-jobs');
}

export function downloadExportJob(id: string) {
  return apiDownload(`/export-jobs/${id}/download`);
}

export function importDocument(form: FormData) {
  return apiFetch<{ blocks: DocumentBlock[] }>('/documents/import', {
    method: 'POST',
    body: form,
  });
}

export function saveAsTemplate(id: string, payload: { name: string; description?: string; styleOverrides?: TemplateRuleProfile | null }) {
  return apiFetch<TemplateVersionDetail>(`/documents/${id}/save-as-template`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
