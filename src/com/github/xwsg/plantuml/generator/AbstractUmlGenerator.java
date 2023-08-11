package com.github.xwsg.plantuml.generator;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.github.xwsg.plantuml.model.UmlAttribute;
import com.github.xwsg.plantuml.model.UmlEntity;
import com.github.xwsg.plantuml.util.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Abstract PlantUML generator.
 *
 * @author xwsg
 */
public abstract class AbstractUmlGenerator {
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
    protected static final String UML_START = "@startuml";
    protected static final String UML_END = "@enduml";
    protected static final String COLUMN_INDENT = "    ";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public void generate(VirtualFile ddlFile) {
        String filePath = "";
        if (ddlFile.getParent() != null) {
            filePath = ddlFile.getParent().getPath();
        }

        String nowString = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String plantUmlFileName = filePath + "/"
                + ddlFile.getName().substring(0, ddlFile.getName().lastIndexOf("."))
                + "_" + nowString + ".puml";

        String plantUml = ddl2PlantUml(ddlFile);
        if (plantUml != null && !plantUml.isEmpty()) {
            FileUtil.writeToFile(plantUml, plantUmlFileName);
        } else {
            JOptionPane.showMessageDialog(null, "DDL file is empty!", "Generate Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected String ddl2PlantUml(VirtualFile ddlFile) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = ddlFile.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, FileUtil.CHARSET));
            StringBuilder stringBuilder = new StringBuilder();
            String lineText = null;
            while ((lineText = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineText);
                stringBuilder.append("\n");
            }
            return ddl2PlantUml(stringBuilder.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(bufferedReader);
        }
        return null;
    }

    protected String ddl2PlantUml(String ddl) {
        List<SQLStatement> statementList = SQLUtils.parseStatements(ddl, dbType());
        List<UmlEntity> entityList = new ArrayList<>();
        Map<String, String> entityCommentMap = new HashMap<>();
        Map<String, String> attrCommentMap = new HashMap<>();
        statementList.forEach(sqlStatement -> {
            UmlEntity umlEntity = null;
            if (sqlStatement instanceof SQLCreateTableStatement) {
                umlEntity = new UmlEntity();
                SQLCreateTableStatement tbl = (SQLCreateTableStatement) sqlStatement;
                umlEntity.setEntityName(tbl.getTableName());
                if (tbl.getComment() != null) {
                    umlEntity.setEntityComment(tbl.getComment().toString());
                }
                if (tbl.getColumnDefinitions() != null) {
                    List<UmlAttribute> attributeList = new ArrayList<>();
                    tbl.getColumnDefinitions().forEach(clm -> {
                        UmlAttribute umlAttribute = new UmlAttribute();
                        umlAttribute.setAttrName(clm.getName().getSimpleName());
                        umlAttribute.setAttrType(clm.getDataType().toString());
                        umlAttribute.setPrimary(clm.isPrimaryKey());
                        umlAttribute.setBold(clm.containsNotNullConstaint());
                        if (clm.getDefaultExpr() != null) {
                            umlAttribute.setAttrExtDefaultExpr(clm.getDefaultExpr().toString());
                        }
                        umlAttribute.setGenerated(clm.isAutoIncrement());
                        if (clm.getComment() != null) {
                            umlAttribute.setAttrDesc(clm.getComment().toString());
                        }
                        attributeList.add(umlAttribute);
                    });
                    umlEntity.setAttributes(attributeList);
                }
            } else if (sqlStatement instanceof SQLCommentStatement) {
                SQLCommentStatement statement = (SQLCommentStatement) sqlStatement;
                if (statement.getType().equals(SQLCommentStatement.Type.TABLE)) {
                    entityCommentMap.put(statement.getOn().getTableName(), statement.getComment().toString());
                } else if (statement.getType().equals(SQLCommentStatement.Type.COLUMN)) {
                    attrCommentMap.put(statement.getOn().getSchema() + "." + statement.getOn().getTableName(), statement.getComment().toString());
                }
            }
            if (umlEntity != null) {
                entityList.add(umlEntity);
            }
        });

        entityList.forEach(entity -> {
            String entityComment = entityCommentMap.get(entity.getEntityName());
            if (entityComment != null) {
                entity.setEntityComment(entityComment);
            }
            entity.getAttributes().forEach(attr -> {
                String attrDesc = attrCommentMap.get(entity.getEntityName() + "." + attr.getAttrName());
                if (attrDesc != null) {
                    attr.setAttrDesc(attrDesc);
                }
            });
        });
        return umlEntityToUmlText(entityList);
    }

    protected abstract DbType dbType();
    protected abstract String trimQuote(String str);

    protected String umlEntityToUmlText(List<UmlEntity> entityList) {
        StringBuilder plantUmlSb = new StringBuilder();
        plantUmlSb.append(UML_START).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        if (!entityList.isEmpty()) {
            entityList.forEach(entity -> {
                plantUmlSb.append("entity").append(" \"").append(trimQuote(entity.getEntityName())).append("\" {").append(LINE_SEPARATOR);
                if (entity.getEntityComment() != null) {
                    plantUmlSb.append(COLUMN_INDENT).append(entity.getEntityComment().replaceAll("'", "")).append(LINE_SEPARATOR);
                    plantUmlSb.append(COLUMN_INDENT).append("==").append(LINE_SEPARATOR);
                }
                entity.getAttributes().forEach(attr -> {
                    plantUmlSb.append(COLUMN_INDENT);
                    if (attr.isPrimary()) {
                        plantUmlSb.append("#");
                    } else if (attr.isBold()) {
                        plantUmlSb.append("*");
                    }
                    plantUmlSb.append(trimQuote(attr.getAttrName())).append(" : ").append(attr.getAttrType());
                    if (attr.getAttrExtDefaultExpr() != null) {
                        plantUmlSb.append(" <<default:").append(attr.getAttrExtDefaultExpr()).append(">>");
                    }
                    if (attr.isGenerated()) {
                        plantUmlSb.append(" <<generated>>");
                    }
                    if (attr.getAttrDesc() != null) {
                        plantUmlSb.append(" --").append(attr.getAttrDesc().replaceAll("'", ""));
                    }
                    plantUmlSb.append(LINE_SEPARATOR);
                });
                plantUmlSb.append("}").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            });
        }
        plantUmlSb.append(UML_END).append(LINE_SEPARATOR);
        return plantUmlSb.toString();
    }
}
