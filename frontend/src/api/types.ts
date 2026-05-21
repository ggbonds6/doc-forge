export type DocType = 'NOTICE' | 'REQUEST' | 'LETTER' | 'REPORT';

export interface DocumentMetadata {
  docType: DocType;
  secrecyLevel?: string;
  urgencyLevel?: string;
  issuingAuthority?: string;
  documentNumber?: string;
  signer?: string;
  mainRecipients?: string;
  title?: string;
  attachmentNote?: string;
  signatory?: string;
  issuedAt?: string;
  copyRecipients?: string[];
  printAuthority?: string;
  printIssuedAt?: string;
  note?: string;
}

export interface DocumentBlock {
  type: 'paragraph' | 'heading' | 'list' | 'table' | 'image';
  level?: number;
  text?: string;
  ordered?: boolean;
  items?: string[];
  rows?: string[][];
  src?: string;
  caption?: string;
}

export interface TemplateTextStyle {
  fontFamily?: string;
  fontSize?: number;
  bold?: boolean;
  align?: string;
  lineHeight?: number;
  spacingBefore?: number;
  spacingAfter?: number;
  color?: string;
  firstLineIndent?: string;
}

export interface TemplateRuleProfile {
  page: {
    size?: string;
    marginTop?: string;
    marginBottom?: string;
    marginLeft?: string;
    marginRight?: string;
  };
  titleStyle: TemplateTextStyle;
  heading1Style: TemplateTextStyle;
  heading2Style: TemplateTextStyle;
  heading3Style: TemplateTextStyle;
  paragraphStyle: TemplateTextStyle;
  tableStyle: {
    fontFamily?: string;
    fontSize?: number;
    border?: string;
    cellPadding?: number;
  };
  headerFooter: {
    showPageNumber?: boolean;
    footerPrefix?: string;
    pageNumberPattern?: string;
  };
  compliance: {
    allowedDocTypes?: DocType[];
    requireDocumentNumber?: boolean;
    requireSignerForRequest?: boolean;
    requirePrintFooter?: boolean;
    enforceTitleSuffix?: boolean;
  };
}

export interface TemplateVersionSummary {
  id: string;
  name: string;
  description: string;
  versionLabel: string;
  status: string;
  compiledTemplatePath?: string;
  updatedAt: string;
}

export interface TemplateAcceptanceCriterion {
  code: string;
  name: string;
  description: string;
  status: string;
  blocking: boolean;
  reviewer?: string;
  comment?: string;
  reviewedAt?: string;
}

export interface TemplatePreviewBaseline {
  standardRef: string;
  documentType: DocType;
  compiledTemplatePath?: string;
  compiledAt?: string;
  reviewStatus: string;
  publishOverrideReason?: string;
  publishOverrideReviewer?: string;
  publishedAt?: string;
  criteria: TemplateAcceptanceCriterion[];
}

export interface TemplatePackSummary {
  id: string;
  code: string;
  name: string;
  description: string;
  builtIn: boolean;
  versions: TemplateVersionSummary[];
}

export interface TemplateVersionDetail extends TemplateVersionSummary {
  templatePackId: string;
  ruleProfile: TemplateRuleProfile;
  previewBaseline: TemplatePreviewBaseline;
}

export interface DocumentView {
  id: string;
  title: string;
  docType: DocType;
  templateVersionId: string;
  status: string;
  metadata: DocumentMetadata;
  bodyBlocks: DocumentBlock[];
  localStyleOverrides?: TemplateRuleProfile | null;
  updatedAt: string;
}

export interface ValidationIssue {
  severity: 'BLOCKING' | 'WARNING';
  code: string;
  field: string;
  message: string;
}

export interface ValidationResult {
  documentId: string;
  pass: boolean;
  issues: ValidationIssue[];
}

export interface ExportJobView {
  id: string;
  documentId: string;
  templateVersionId: string;
  status: string;
  outputPath?: string;
  errorMessage?: string;
  updatedAt: string;
}
