package com.docforge.govdoc.template;

public class TemplateTableStyle {
    private String fontFamily = "仿宋";
    private Integer fontSize = 16;
    private String border = "single";
    private Integer cellPadding = 100;

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public String getBorder() {
        return border;
    }

    public void setBorder(String border) {
        this.border = border;
    }

    public Integer getCellPadding() {
        return cellPadding;
    }

    public void setCellPadding(Integer cellPadding) {
        this.cellPadding = cellPadding;
    }
}

