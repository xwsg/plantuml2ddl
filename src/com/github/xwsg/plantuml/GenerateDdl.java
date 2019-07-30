package com.github.xwsg.plantuml;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

/**
 * This guy is lazy, nothing left.
 *
 * @author xwsg
 */
public class GenerateDdl {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void generate(VirtualFile plantUmlFile) {
        String filePath = "";
        if (plantUmlFile.getParent() != null) {
            filePath = plantUmlFile.getParent().getPath();
        }
        String outFileName =
            filePath + "/" + plantUmlFile.getName()
                .substring(0, plantUmlFile.getName().lastIndexOf("."))
                + ".sql";

        String ddl = plantUml2Ddl(plantUmlFile);
        if (ddl != null) {
            writeToFile(ddl, outFileName);
        }
    }

    private static String plantUml2Ddl(VirtualFile plantUmlFile) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = plantUmlFile.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            boolean uml = false;
            boolean field = false;
            boolean firstField = false;
            String pkField = null;

            String lineText;
            StringBuilder ddlSb = new StringBuilder();
            while ((lineText = bufferedReader.readLine()) != null) {
                lineText = lineText.trim();

                if (lineText.isEmpty()) {
                    continue;
                }
                if (lineText.startsWith("@startuml")) {
                    uml = true;
                    continue;
                }
                if (!uml) {
                    continue;
                }

                if (lineText.startsWith("--") || lineText.startsWith("==") || lineText
                    .startsWith("..") || lineText.startsWith("__")) {
                    continue;
                }

                if (lineText.startsWith("@enduml")) {
                    uml = false;
                    continue;
                }

                if (field && lineText.startsWith("}")) {
                    field = false;
                    if (pkField != null) {
                        ddlSb.append(",").append(LINE_SEPARATOR);
                        ddlSb.append("  ");
                        ddlSb.append("PRIMARY KEY (`").append(pkField).append("`)");
                        pkField = null;
                    }
                    ddlSb.append(LINE_SEPARATOR).append(");").append(LINE_SEPARATOR);
                    continue;
                }

                String[] lineArr = lineText.split("\\s+");
                if (lineText.startsWith("entity")) {
                    field = true;
                    firstField = true;
                    String tableName = lineArr[1].replaceAll("\"", "");
                    ddlSb.append("CREATE TABLE IF NOT EXISTS ").append("`").append(tableName)
                        .append("` (").append(LINE_SEPARATOR);
                    continue;
                }

                if (!field) {
                    continue;
                }

                if (!firstField) {
                    ddlSb.append(",").append(LINE_SEPARATOR);
                }
                ddlSb.append("  ");
                String fieldName = lineArr[0];
                String fieldType = lineArr[2].toUpperCase();
                if (fieldName.charAt(0) == '*') {
                    fieldName = fieldName.substring(1);
                    ddlSb.append("`").append(fieldName).append("` ").append(fieldType)
                        .append(" NOT NULL");
                } else if (fieldName.charAt(0) == '#') {
                    fieldName = fieldName.substring(1);
                    ddlSb.append("`").append(fieldName).append("` ").append(fieldType)
                        .append(" NOT NULL");
                    pkField = fieldName;
                } else {
                    ddlSb.append("`").append(fieldName).append("` ").append(fieldType);
                }
                if (lineArr.length >= 4 && "<<generated>>".equals(lineArr[3])) {
                    ddlSb.append(" AUTO_INCREMENT");
                }
                firstField = false;
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
}
