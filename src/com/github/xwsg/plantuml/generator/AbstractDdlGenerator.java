package com.github.xwsg.plantuml.generator;

import com.github.xwsg.plantuml.model.Column;
import com.github.xwsg.plantuml.model.Table;
import com.github.xwsg.plantuml.util.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract DDL generator.
 *
 * @author xwsg
 */
public abstract class AbstractDdlGenerator {

    protected static final Pattern START_ENTITY_REGEX_PATTERN = Pattern.compile("\\s*entity\\s*\"(\\S+)\".*\\{");
    protected static final Pattern FIELD_REGEX_PATTERN = Pattern.compile("\\s*([*#~+-]?)\\s*(\\S+)\\s*:\\s*(\\S+)\\s*(<<\\S+>>)*");
    protected static final String UML_START = "@startuml";
    protected static final String UML_END = "@enduml";
    protected static final String NOTNULL_MODIFIER = "<<notnull>>";
    protected static final String GENERATED_MODIFIER = "<<generated>>";
    protected static final Pattern DEFAULT_MODIFIER_PATTERN = Pattern.compile("<<default:([\\S+\\s]+?)>>");
    protected static final String COMMENT_MODIFIER = "--";
    protected static final String PK_MODIFIER = "<<pk>>";

    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
    protected static final String COLUMN_INDENT = "    ";

    protected List<Table> tables = new ArrayList<>();

    public void generate(VirtualFile plantUmlFile) {
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

    protected String plantUml2Ddl(VirtualFile plantUmlFile) {
        try (InputStream inputStream = plantUmlFile.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, FileUtil.CHARSET);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            boolean matchedUml = false;
            boolean matchedTable = false;
            boolean matchedField = false;
            String guessTableComment = null;
            String tableComment = null;
            String lineText;
            List<Column> columns = new ArrayList<>();
            Table table = new Table();
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
                if (matchedUmlComment(lineText)) {
                    continue;
                }
                if (matchedUmlEnd(lineText)) {
                    matchedUml = false;
                    continue;
                }
                if (!matchedTable && matchedEntityStart(lineText)) {
                    table = new Table();
                    table.setName(extractTableName(lineText));
                    columns = new ArrayList<>();
                    matchedTable = true;
                }
                if (matchedTable && matchedEntityEnd(lineText)) {
                    table.setComment(tableComment);
                    table.setColumns(columns);
                    tables.add(table);
                    matchedTable = false;
                    matchedField = false;
                    continue;
                }
                if (matchedFieldSeparator(lineText)) {
                    if (matchedTable && !matchedField) {
                        tableComment = guessTableComment;
                        guessTableComment = null;
                    }
                    continue;
                }

                Column column = extractColumn(lineText);
                if (column != null) {
                    matchedField = true;
                    columns.add(column);
                    continue;
                }
                if (matchedTable && !matchedField) {
                    guessTableComment = lineText;
                }
            }
            return genDdlText();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed!",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    /**
     * generate DDL text
     *
     * @return ddl text
     */
    protected abstract String genDdlText();

    private static boolean matchedFieldSeparator(String lineText) {
        return lineText.startsWith("--") || lineText.startsWith("==")
                || lineText.startsWith("..") || lineText.startsWith("__");
    }

    private static boolean matchedUmlComment(String lineText) {
        return lineText.startsWith("'");
    }

    private static boolean matchedUmlStart(String lineText) {
        return UML_START.equals(lineText) || lineText.startsWith(UML_START);
    }

    private static boolean matchedUmlEnd(String lineText) {
        return UML_END.equals(lineText);
    }

    private static boolean matchedEntityStart(String lineText) {
        return START_ENTITY_REGEX_PATTERN.matcher(lineText).find();
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
            String name = matcher.group(2);
            String dataType = matcher.group(3);
            boolean primaryKey = "#".equals(matcher.group(1)) || lineText.toLowerCase().contains(PK_MODIFIER);
            boolean nonNull = "*".equals(matcher.group(1)) || lineText.toLowerCase().contains(NOTNULL_MODIFIER);
            boolean autoInc = lineText.toLowerCase().contains(GENERATED_MODIFIER);

            String defaultValue = null;
            Matcher defaultMatcher = DEFAULT_MODIFIER_PATTERN.matcher(lineText);
            if (defaultMatcher.find()) {
                defaultValue = defaultMatcher.group(1).trim();
            }
            String comment = null;
            String[] commentArr = lineText.split(COMMENT_MODIFIER);
            if (commentArr.length == 2) {
                comment = commentArr[1].trim();
            }
            return new Column(name, dataType, defaultValue, comment, nonNull, autoInc, primaryKey);
        }

        return null;
    }
}
