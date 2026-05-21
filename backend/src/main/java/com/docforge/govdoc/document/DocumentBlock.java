package com.docforge.govdoc.document;

import java.util.ArrayList;
import java.util.List;

public class DocumentBlock {
    private String type;
    private Integer level;
    private String text;
    private Boolean ordered;
    private List<String> items = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();
    private String src;
    private String caption;

    public static DocumentBlock paragraph(String text) {
        DocumentBlock block = new DocumentBlock();
        block.setType("paragraph");
        block.setText(text);
        return block;
    }

    public static DocumentBlock heading(int level, String text) {
        DocumentBlock block = new DocumentBlock();
        block.setType("heading");
        block.setLevel(level);
        block.setText(text);
        return block;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getOrdered() {
        return ordered;
    }

    public void setOrdered(Boolean ordered) {
        this.ordered = ordered;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}

