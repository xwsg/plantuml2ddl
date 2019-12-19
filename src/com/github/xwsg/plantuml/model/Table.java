package com.github.xwsg.plantuml.model;

import java.util.List;

/**
 * Table model.
 *
 * @author xwsg
 */
public class Table {
    private String name;
    private List<Column> columns;
    private String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
