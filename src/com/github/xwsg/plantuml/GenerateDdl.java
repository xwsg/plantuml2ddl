package com.github.xwsg.plantuml;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileOutputStream;
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
public class GenerateDdl {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Pattern START_ENTITY_REGEX_PATTERN = Pattern.compile("\\s*entity\\s*\"(\\S+)\".*\\{");
    private static final Pattern FIELD_REGEX_PATTERN = Pattern.compile("\\s*([*#~+-]?)\\s*(\\S+)\\s*:\\s*(\\S+)\\s*(<<\\S+>>)*");
    private static final String UML_START = "@startuml";
    private static final String UML_END = "@enduml";
    private static final String COLUMN_INDENT = "    ";

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
            writeToFile(ddl, ddlFileName);
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
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            boolean matchedUml = false;
            boolean matchedField = false;
            boolean firstColumn = false;
            String pkColumn = null;
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
                if (matchedUmlEnd(lineText)) {
                    matchedUml = false;
                    continue;
                }
                if (matchedField && matchedEntityEnd(lineText)) {
                    matchedField = false;
                    if (pkColumn != null) {
                        ddlSb.append(",").append(LINE_SEPARATOR);
                        ddlSb.append(COLUMN_INDENT);
                        ddlSb.append("PRIMARY KEY (`").append(pkColumn).append("`)");
                        pkColumn = null;
                    }
                    ddlSb.append(LINE_SEPARATOR).append(");").append(LINE_SEPARATOR)
                        .append(LINE_SEPARATOR);
                    continue;
                }
                String tableName = extractTableName(lineText);
                if (Objects.nonNull(tableName)) {
                    matchedField = true;
                    firstColumn = true;
                    ddlSb.append("CREATE TABLE IF NOT EXISTS ");
                    ddlSb.append("`").append(tableName).append("` (");
                    ddlSb.append(LINE_SEPARATOR);
                    continue;
                }
                if (!matchedField || matchedFieldSeparator(lineText)) {
                    continue;
                }
                if (!firstColumn) {
                    ddlSb.append(",").append(LINE_SEPARATOR);
                }
                // column indent
                ddlSb.append(COLUMN_INDENT);
                Column column = extractColumn(lineText);
                if (Objects.nonNull(column)) {
                    ddlSb.append("`").append(column.getName()).append("` ")
                        .append(column.getDataType());
                    if (column.isNonNull()) {
                        ddlSb.append(" NOT NULL");
                    }
                    if (column.isPrimaryKey()) {
                        ddlSb.append(" NOT NULL");
                        pkColumn = column.getName();
                    }
                    if (column.isAutoIncrement()) {
                        ddlSb.append(" AUTO_INCREMENT");
                    }
                }
                firstColumn = false;
            }
            return ddlSb.toString();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            safeClose(inputStream);
            safeClose(bufferedReader);
        }
        return null;
    }

    private static boolean matchedFieldSeparator(String lineText) {
        return lineText.startsWith("--") || lineText.startsWith("==") || lineText
            .startsWith("..") || lineText.startsWith("__");
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
            Column column = new Column();
            column.setPrimaryKey("#".equals(matcher.group(1)));
            column.setNonNull("*".equals(matcher.group(1)));
            column.setName(matcher.group(2));
            column.setDataType(matcher.group(3));
            column.setAutoIncrement("<<generated>>".equals(matcher.group(4)));
            return column;
        }
        return null;
    }

    private static void writeToFile(String content, String outFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFileName);
            fos.write(content.getBytes());
            fos.flush();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            safeClose(fos);
        }
    }

    private static void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private static class Column {

        private String name;
        private String dataType;
        private boolean primaryKey;
        private boolean autoIncrement;
        private boolean nonNull;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getDataType() {
            return dataType;
        }

        void setDataType(String dataType) {
            this.dataType = dataType;
        }

        boolean isPrimaryKey() {
            return primaryKey;
        }

        void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        boolean isAutoIncrement() {
            return autoIncrement;
        }

        void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        boolean isNonNull() {
            return nonNull;
        }

        void setNonNull(boolean nonNull) {
            this.nonNull = nonNull;
        }
    }
}
