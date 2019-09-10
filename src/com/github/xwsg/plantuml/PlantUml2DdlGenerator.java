package com.github.xwsg.plantuml;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * DDL generate tool.
 *
 * @author xwsg
 */
public class PlantUml2DdlGenerator {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Pattern START_ENTITY_REGEX_PATTERN = Pattern.compile("\\s*entity\\s*\"(\\S+)\".*\\{");
    private static final Pattern FIELD_REGEX_PATTERN = Pattern.compile("\\s*([*#~+-]?)\\s*(\\S+)\\s*:\\s*(\\S+)\\s*(<<\\S+>>)*");
    private static final String UML_START = "@startuml";
    private static final String UML_END = "@enduml";
    private static final String COLUMN_INDENT = "    ";

    private static final String NOTNULL_MODIFIER = "<<notnull>>";
    private static final String GENERATED_MODIFIER = "<<generated>>";
    private static final Pattern DEFAULT_MODIFIER_PATTERN = Pattern.compile("<<default:([\\S+\\s]+?)>>");
    private static final String COMMENT_MODIFIER = "--";

    private static final String PK_MODIFIER = "<<pk>>";

    public static void generate(VirtualFile plantUmlFile) {
        String filePath = "";
        if (plantUmlFile.getParent() != null) {
            filePath = plantUmlFile.getParent().getPath();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String nowString = LocalDateTime.now().format(formatter);
        String ddlFileName = filePath + "/"
            + plantUmlFile.getName().substring(0, plantUmlFile.getName().lastIndexOf("."))
            + "-" + nowString + ".sql";

        String ddl = plantUml2Ddl(plantUmlFile);
        if (ddl != null && !ddl.isEmpty()) {
            FileUtil.writeToFile(ddl, ddlFileName);
        } else {
            JOptionPane.showMessageDialog(null, "PlantUML file not found!", "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String plantUml2Ddl(VirtualFile plantUmlFile) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = plantUmlFile.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, FileUtil.CHARSET));

            boolean matchedUml = false;
            boolean matchedTable = false;
            boolean matchedField = false;
            boolean firstColumn = false;
            String pkColumn = null;
            String guessTableComment = null;
            String tableComment = null;
            String lineText;
            StringBuilder ddlSb = new StringBuilder();
            while ((lineText = bufferedReader.readLine()) != null) {
                // trim and exclude creole bold (**)
                lineText = lineText.trim().replaceAll("\\*\\*", "");
                if (lineText.isEmpty()) {
                    continue;
                }
                if (matchedUmlStart(lineText)) {
                    matchedUml = true;
                    continue;
                }
                if (!matchedUml) {
                    continue;
                }
                if (matchedComment(lineText)) {
                    continue;
                }
                if (matchedUmlEnd(lineText)) {
                    matchedUml = false;
                    continue;
                }
                if (matchedField && matchedEntityEnd(lineText)) {
                    matchedTable = false;
                    matchedField = false;
                    if (pkColumn != null) {
                        ddlSb.append(",").append(LINE_SEPARATOR);
                        ddlSb.append(COLUMN_INDENT);
                        ddlSb.append("PRIMARY KEY (`").append(pkColumn).append("`)");
                        pkColumn = null;
                    }
                    ddlSb.append(LINE_SEPARATOR).append(")");
                    if (tableComment != null && !tableComment.isEmpty()) {
                        ddlSb.append(" COMMENT=").append("'").append(tableComment).append("'");
                        tableComment = null;
                    }
                    ddlSb.append(";").append(LINE_SEPARATOR)
                        .append(LINE_SEPARATOR);
                    continue;
                }
                String tableName = extractTableName(lineText);
                if (Objects.nonNull(tableName)) {
                    matchedTable = true;
                    firstColumn = true;
                    ddlSb.append("CREATE TABLE IF NOT EXISTS ");
                    ddlSb.append("`").append(tableName).append("` (");
                    ddlSb.append(LINE_SEPARATOR);
                    continue;
                }
                if (matchedFieldSeparator(lineText)) {
                    if (matchedTable && !matchedField) {
                        tableComment = guessTableComment;
                        guessTableComment = null;
                    }
                    continue;
                }
                if (matchedField && !firstColumn) {
                    ddlSb.append(",").append(LINE_SEPARATOR);
                }

                Column column = extractColumn(lineText);
                if (column != null) {
                    ddlSb.append(COLUMN_INDENT);
                    ddlSb.append(column.getDefinition());
                    pkColumn = column.isPk() ? column.getName() : pkColumn;
                    matchedField = true;
                    firstColumn = false;
                    continue;
                }
                if (matchedTable && !matchedField) {
                    guessTableComment = lineText;
                }
            }
            return ddlSb.toString();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(bufferedReader);
        }
        return null;
    }

    private static boolean matchedFieldSeparator(String lineText) {
        return lineText.startsWith("--") || lineText.startsWith("==") || lineText
            .startsWith("..") || lineText.startsWith("__");
    }

    private static boolean matchedComment(String lineText) {
        return lineText.startsWith("'");
    }

    private static boolean matchedUmlStart(String lineText) {
        return UML_START.equals(lineText);
    }

    private static boolean matchedUmlEnd(String lineText) {
        return UML_END.equals(lineText);
    }

    private static boolean matchedEntityEnd(String lineText) {
        return "}".equals(lineText);
    }

    private static String extractTableName(String lineText) {
        Matcher matcher = START_ENTITY_REGEX_PATTERN.matcher(lineText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static Column extractColumn(String lineText) {
        Matcher matcher = FIELD_REGEX_PATTERN.matcher(lineText);
        if (matcher.find()) {
            StringBuilder columnSb = new StringBuilder();
            Column column = new Column();
            String columnName = matcher.group(2);
            String dataType = matcher.group(3).toUpperCase();
            boolean primaryKey = "#".equals(matcher.group(1)) || lineText.toLowerCase().contains(PK_MODIFIER);
            boolean nonNull = "*".equals(matcher.group(1)) || lineText.toLowerCase().contains(NOTNULL_MODIFIER);
            boolean autoIncrement = lineText.toLowerCase().contains(GENERATED_MODIFIER);

            columnSb.append("`").append(columnName).append("` ").append(dataType);
            if (nonNull || primaryKey) {
                columnSb.append(" NOT NULL");
            }
            if (primaryKey) {
                column.setPk(true);
                column.setName(columnName);
            }
            if (autoIncrement) {
                columnSb.append(" AUTO_INCREMENT");
            }
            Matcher defaultMatcher = DEFAULT_MODIFIER_PATTERN.matcher(lineText);
            if (defaultMatcher.find()) {
                columnSb.append(" DEFAULT ").append(defaultMatcher.group(1).trim());
            }
            String[] commentArr = lineText.split(COMMENT_MODIFIER);
            if (commentArr.length == 2) {
                columnSb.append(" COMMENT ").append("'").append(commentArr[1].trim()).append("'");
            }
            column.setDefinition(columnSb.toString());
            return column;
        }

        return null;
    }

    static class Column {

        private boolean pk;
        private String name;
        private String definition;

        boolean isPk() {
            return pk;
        }

        void setPk(boolean pk) {
            this.pk = pk;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getDefinition() {
            return definition;
        }

        void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}
