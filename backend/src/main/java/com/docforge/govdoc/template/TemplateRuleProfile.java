package com.docforge.govdoc.template;

public class TemplateRuleProfile {
    private TemplatePageSettings page = new TemplatePageSettings();
    private TemplateTextStyle titleStyle = new TemplateTextStyle();
    private TemplateTextStyle heading1Style = new TemplateTextStyle();
    private TemplateTextStyle heading2Style = new TemplateTextStyle();
    private TemplateTextStyle heading3Style = new TemplateTextStyle();
    private TemplateTextStyle paragraphStyle = new TemplateTextStyle();
    private TemplateTableStyle tableStyle = new TemplateTableStyle();
    private TemplateHeaderFooterSettings headerFooter = new TemplateHeaderFooterSettings();
    private ComplianceProfile compliance = new ComplianceProfile();

    public TemplatePageSettings getPage() {
        return page;
    }

    public void setPage(TemplatePageSettings page) {
        this.page = page;
    }

    public TemplateTextStyle getTitleStyle() {
        return titleStyle;
    }

    public void setTitleStyle(TemplateTextStyle titleStyle) {
        this.titleStyle = titleStyle;
    }

    public TemplateTextStyle getHeading1Style() {
        return heading1Style;
    }

    public void setHeading1Style(TemplateTextStyle heading1Style) {
        this.heading1Style = heading1Style;
    }

    public TemplateTextStyle getHeading2Style() {
        return heading2Style;
    }

    public void setHeading2Style(TemplateTextStyle heading2Style) {
        this.heading2Style = heading2Style;
    }

    public TemplateTextStyle getHeading3Style() {
        return heading3Style;
    }

    public void setHeading3Style(TemplateTextStyle heading3Style) {
        this.heading3Style = heading3Style;
    }

    public TemplateTextStyle getParagraphStyle() {
        return paragraphStyle;
    }

    public void setParagraphStyle(TemplateTextStyle paragraphStyle) {
        this.paragraphStyle = paragraphStyle;
    }

    public TemplateTableStyle getTableStyle() {
        return tableStyle;
    }

    public void setTableStyle(TemplateTableStyle tableStyle) {
        this.tableStyle = tableStyle;
    }

    public TemplateHeaderFooterSettings getHeaderFooter() {
        return headerFooter;
    }

    public void setHeaderFooter(TemplateHeaderFooterSettings headerFooter) {
        this.headerFooter = headerFooter;
    }

    public ComplianceProfile getCompliance() {
        return compliance;
    }

    public void setCompliance(ComplianceProfile compliance) {
        this.compliance = compliance;
    }
}

