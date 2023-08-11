package com.github.xwsg.plantuml.model;

import java.util.List;

/**
 * @author xwsg
 */
public class UmlEntity {

    private String entityName;
    private String entityComment;
    private List<UmlAttribute> attributes;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityComment() {
        return entityComment;
    }

    public void setEntityComment(String entityComment) {
        this.entityComment = entityComment;
    }

    public List<UmlAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<UmlAttribute> attributes) {
        this.attributes = attributes;
    }
}
