package com.github.xwsg.plantuml;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 * PlantUml generate tool.
 *
 * @author wsg
 */
public class Ddl2PlantUmlGenerator {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String UML_START = "@startuml";
    private static final String UML_END = "@enduml";
    private static final String COLUMN_INDENT = "    ";
    private static final Pattern TABLE_REGEX_PATTERN = Pattern.compile("\\s*CREATE\\s+TABLE(\\s+IF\\s+NOT\\s+EXISTS)?\\s+(\\S+)\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern END_TABLE_REGEX_PATTERN = Pattern.compile("^\\s*\\)");
    private static final Pattern TABLE_COMMENT_REGEX_PATTERN = Pattern.compile("COMMENT\\s*=\\s*('(.*?)')?", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLUMN_REGEX_PATTERN = Pattern.compile("\\s*(\\S+)\\s+(\\S+(\\s*\\(\\d+\\))?)");
    private static final Pattern COLUMN_AUTO_INCREMENT_REGEX_PATTERN = Pattern.compile("\\s+AUTO_INCREMENT", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLUMN_DEFAULT_REGEX_PATTERN = Pattern.compile("\\s+DEFAULT\\s+('.*?'|\\d+|([\\w|_]+(\\(\\d+\\))?))", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLUMN_NOT_NULL_REGEX_PATTERN = Pattern.compile("\\s+NOT\\s+NULL", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLUMN_COMMENT_REGEX_PATTERN = Pattern.compile("\\s+COMMENT\\s+'(.*?)'", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRIMARY_KEY_REGEX_PATTERN = Pattern.compile("\\s*PRIMARY\\s+KEY\\s*(\\((.+)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_REGEX_PATTERN = Pattern.compile("^(UNIQUE KEY|KEY|INDEX|UNIQUE INDEX)", Pattern.CASE_INSENSITIVE);

    public static void generate(VirtualFile ddlFile) {
        String filePath = "";
        if (ddlFile.getParent() != null) {
            filePath = ddlFile.getParent().getPath();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String nowString = LocalDateTime.now().format(formatter);
        String plantUmlFileName = filePath + "/"
            + ddlFile.getName().substring(0, ddlFile.getName().lastIndexOf("."))
            + "-" + nowString + ".puml";

        String plantUml = ddl2PlantUml(ddlFile);
        if (plantUml != null && !plantUml.isEmpty()) {
            FileUtil.writeToFile(plantUml, plantUmlFileName);
        } else {
            JOptionPane.showMessageDialog(null, "DDL file not found!", "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String ddl2PlantUml(VirtualFile ddlFile) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = ddlFile.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, FileUtil.CHARSET));
            boolean matchedTable = false;
            boolean matchedBlockComment = false;
            Table table = null;
            List<Table> tables = new ArrayList<>();
            List<Column> columns = new ArrayList<>();
            String lineText = null;
            while ((lineText = bufferedReader.readLine()) != null) {
                lineText = lineText.trim().replaceAll("\\*\\*", "");
                if (lineText.isEmpty()) {
                    continue;
                }
                if (matchedBlockCommentStart(lineText)) {
                    matchedBlockComment = true;
                    continue;
                }
                if (matchedBlockCommentEnd(lineText)) {
                    matchedBlockComment = false;
                    continue;
                }
                if (matchedBlockComment) {
                    continue;
                }
                if (matchedLineComment(lineText)) {
                    continue;
                }
                String tableName = extractTableName(lineText);
                if (tableName != null) {
                    table = new Table();
                    table.setName(tableName);
                    matchedTable = true;
                    continue;
                }
                if (!matchedTable) {
                    continue;
                }

                List<String> pkColumns = extractPkColumns(lineText);
                if (pkColumns != null) {
                    table.setPkColumns(pkColumns);
                    continue;
                }

                Matcher endTblMatcher = END_TABLE_REGEX_PATTERN.matcher(lineText);
                if (endTblMatcher.find()) {
                    table.setColumns(columns);
                    table.setComment(extractTableComment(lineText));
                    tables.add(table);
                    table = null;
                    columns = new ArrayList<>();
                    matchedTable = false;
                    continue;
                }

                if (matchedKey(lineText)) {
                    continue;
                }

                Column column = extractColumn(lineText);
                if (column != null) {
                    columns.add(column);
                }
            }
            if (tables.isEmpty()) {
                return null;
            }
            return tables2Text(tables);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(bufferedReader);
        }
        return null;
    }

    private static String tables2Text(List<Table> tables) {
        StringBuilder plantUmlSb = new StringBuilder();
        plantUmlSb.append(UML_START).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        tables.forEach(tbl -> {
            plantUmlSb.append("entity").append(" \"").append(tbl.getName()).append("\" {").append(LINE_SEPARATOR);
            if(tbl.getComment() != null) {
                plantUmlSb.append(COLUMN_INDENT).append(tbl.getComment()).append(LINE_SEPARATOR);
                plantUmlSb.append(COLUMN_INDENT).append("==").append(LINE_SEPARATOR);
            }
            if (tbl.getColumns() != null) {
                tbl.getColumns().forEach(clm -> {
                    plantUmlSb.append(COLUMN_INDENT);
                    if (tbl.getPkColumns().contains(clm.getName())) {
                        plantUmlSb.append("#");
                    } else if (clm.isNotNull()) {
                        plantUmlSb.append("*");
                    }
                    plantUmlSb.append(clm.getName()).append(" : ").append(clm.getType());
                    if (clm.getDefaultValue() != null) {
                        plantUmlSb.append(" <<default:").append(clm.getDefaultValue()).append(">>");
                    }
                    if (clm.isAutoIncrement()) {
                        plantUmlSb.append(" <<generated>>");
                    }
                    if (clm.getComment() != null) {
                        plantUmlSb.append(" --").append(clm.getComment().replaceAll("'", ""));
                    }
                    plantUmlSb.append(LINE_SEPARATOR);
                });
            }
            plantUmlSb.append("}").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        });
        plantUmlSb.append(UML_END).append(LINE_SEPARATOR);
        return plantUmlSb.toString();
    }

    private static String extractTableName(String lineText) {
        Matcher tblMatcher = TABLE_REGEX_PATTERN.matcher(lineText);
        if (tblMatcher.find()) {
            return tblMatcher.group(2).replaceAll("`", "");
        }
        return null;
    }

    private static Column extractColumn(String lineText) {
        Matcher columnMatcher = COLUMN_REGEX_PATTERN.matcher(lineText);
        if (columnMatcher.find()) {
            Column column = new Column();
            String columnName = columnMatcher.group(1).replaceAll("`", "");
            String columnType = columnMatcher.group(2);
            column.setName(columnName);
            column.setType(columnType);
            column.setNotNull(isColumnNotNull(lineText));
            column.setDefaultValue(extractColumnDefault(lineText));
            column.setAutoIncrement(isColumnAutoIncrement(lineText));
            column.setComment(extractColumnComment(lineText));

            return column;
        }
        return null;
    }

    private static boolean isColumnAutoIncrement(String lineText) {
        Matcher clmAutoIncrementMatcher = COLUMN_AUTO_INCREMENT_REGEX_PATTERN.matcher(lineText);
        return clmAutoIncrementMatcher.find();
    }

    private static boolean isColumnNotNull(String lineText) {
        Matcher clmNotNullMatcher = COLUMN_NOT_NULL_REGEX_PATTERN.matcher(lineText);
        return clmNotNullMatcher.find();
    }

    private static String extractColumnDefault(String lineText) {
        Matcher clmDefaultMatcher = COLUMN_DEFAULT_REGEX_PATTERN.matcher(lineText);
        if (clmDefaultMatcher.find()) {
            return clmDefaultMatcher.group(1);
        }
        return null;
    }

    private static String extractColumnComment(String lineText) {
        Matcher clmCommentMatcher = COLUMN_COMMENT_REGEX_PATTERN.matcher(lineText);
        if (clmCommentMatcher.find()) {
            return clmCommentMatcher.group(1);
        }
        return null;
    }

    private static List<String> extractPkColumns(String lineText) {
        Matcher pkMatcher = PRIMARY_KEY_REGEX_PATTERN.matcher(lineText);
        if (pkMatcher.find()) {
            String pkStr = pkMatcher.group(2);
            return Arrays.stream(pkStr.replaceAll("`", "").split(","))
                .filter(p -> !p.isEmpty()).collect(Collectors.toList());
        }
        return null;
    }

    private static String extractTableComment(String lineText) {
        Matcher tblCommentMatcher = TABLE_COMMENT_REGEX_PATTERN.matcher(lineText);
        if (tblCommentMatcher.find()) {
            return tblCommentMatcher.group(2);
        }
        return null;
    }

    private static boolean matchedLineComment(String lineText) {
        return lineText.startsWith("--") || lineText.startsWith("#");
    }

    private static boolean matchedBlockCommentStart(String lineText) {
        return lineText.startsWith("/*");
    }

    private static boolean matchedBlockCommentEnd(String lineText) {
        return lineText.endsWith("*/");
    }

    private static boolean matchedKey(String lineText) {
        return KEY_REGEX_PATTERN.matcher(lineText).find();
    }

    private static class Table {
        private String name;
        private String comment;
        private List<Column> columns;
        private List<String> pkColumns;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getComment() {
            return comment;
        }

        void setComment(String comment) {
            this.comment = comment;
        }

        List<Column> getColumns() {
            return columns;
        }

        void setColumns(List<Column> columns) {
            this.columns = columns;
        }

        List<String> getPkColumns() {
            return pkColumns;
        }

        void setPkColumns(List<String> pkColumns) {
            this.pkColumns = pkColumns;
        }
    }

    private static class Column {
        private String name;
        private String type;
        private String comment;
        private String defaultValue;
        private boolean notNull;
        private boolean pk;
        private boolean autoIncrement;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getType() {
            return type;
        }

        void setType(String type) {
            this.type = type;
        }

        String getComment() {
            return comment;
        }

        void setComment(String comment) {
            this.comment = comment;
        }

        String getDefaultValue() {
            return defaultValue;
        }

        void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        boolean isNotNull() {
            return notNull;
        }

        void setNotNull(boolean notNull) {
            this.notNull = notNull;
        }

        boolean isPk() {
            return pk;
        }

        void setPk(boolean pk) {
            this.pk = pk;
        }

        boolean isAutoIncrement() {
            return autoIncrement;
        }

        void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }
    }
}
