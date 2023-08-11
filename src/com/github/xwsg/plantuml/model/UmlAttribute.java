package com.github.xwsg.plantuml.model;

/**
 * @author xwsg
 */
public class UmlAttribute {

    private String attrName;
    private String attrType;
    private boolean isBold;
    private boolean isPrimary;
    private boolean isGenerated;

    private String attrDesc;
    private String attrExtDefaultExpr;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrType() {
        return attrType;
    }

    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public String getAttrDesc() {
        return attrDesc;
    }

    public void setAttrDesc(String attrDesc) {
        this.attrDesc = attrDesc;
    }

    public String getAttrExtDefaultExpr() {
        return attrExtDefaultExpr;
    }

    public void setAttrExtDefaultExpr(String attrExtDefaultExpr) {
        this.attrExtDefaultExpr = attrExtDefaultExpr;
    }
}
