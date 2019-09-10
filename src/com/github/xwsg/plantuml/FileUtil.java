package com.github.xwsg.plantuml;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;

/**
 * This guy is lazy, nothing left.
 *
 * @author wsg
 */
public class FileUtil {

    static final Charset CHARSET = StandardCharsets.UTF_8;

    static void writeToFile(String content, String outFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFileName);
            fos.write(content.getBytes(CHARSET));
            fos.flush();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Generate Failed",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            safeClose(fos);
        }
    }

    static void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }
}
