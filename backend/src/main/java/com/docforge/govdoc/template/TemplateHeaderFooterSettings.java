package com.docforge.govdoc.template;

public class TemplateHeaderFooterSettings {
    private Boolean showPageNumber = Boolean.TRUE;
    private String footerPrefix = "印发";
    private String pageNumberPattern = "— {page} —";

    public Boolean getShowPageNumber() {
        return showPageNumber;
    }

    public void setShowPageNumber(Boolean showPageNumber) {
        this.showPageNumber = showPageNumber;
    }

    public String getFooterPrefix() {
        return footerPrefix;
    }

    public void setFooterPrefix(String footerPrefix) {
        this.footerPrefix = footerPrefix;
    }

    public String getPageNumberPattern() {
        return pageNumberPattern;
    }

    public void setPageNumberPattern(String pageNumberPattern) {
        this.pageNumberPattern = pageNumberPattern;
    }
}

