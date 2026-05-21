package com.docforge.govdoc.template;

public class TemplateTextStyle {
    private String fontFamily;
    private Integer fontSize;
    private Boolean bold;
    private String align;
    private Double lineHeight;
    private Integer spacingBefore;
    private Integer spacingAfter;
    private String color;
    private String firstLineIndent;

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

    public Boolean getBold() {
        return bold;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public Double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(Double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public Integer getSpacingBefore() {
        return spacingBefore;
    }

    public void setSpacingBefore(Integer spacingBefore) {
        this.spacingBefore = spacingBefore;
    }

    public Integer getSpacingAfter() {
        return spacingAfter;
    }

    public void setSpacingAfter(Integer spacingAfter) {
        this.spacingAfter = spacingAfter;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFirstLineIndent() {
        return firstLineIndent;
    }

    public void setFirstLineIndent(String firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
    }
}

