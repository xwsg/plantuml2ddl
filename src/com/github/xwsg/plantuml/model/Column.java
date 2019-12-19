package com.github.xwsg.plantuml.model;

/**
 * Column model.
 *
 * @author xwsg
 */
public class Column {

    private String name;
    private String dataType;
    private String defaultValue;
    private String comment;
    private boolean notNull;
    private boolean autoInc;
    private boolean primaryKey;

    public Column() {};

    public Column(String name, String dataType, String defaultValue, String comment, boolean notNull, boolean autoInc, boolean primaryKey) {
        this.name = name;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.notNull = notNull;
        this.autoInc = autoInc;
        this.primaryKey = primaryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isAutoInc() {
        return autoInc;
    }

    public void setAutoInc(boolean autoInc) {
        this.autoInc = autoInc;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
