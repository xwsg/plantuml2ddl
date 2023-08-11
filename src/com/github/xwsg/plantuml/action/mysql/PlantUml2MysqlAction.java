package com.github.xwsg.plantuml.action.mysql;

import com.github.xwsg.plantuml.generator.mysql.PlantUml2MysqlDdlGenerator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

/**
 * Generate MySQL DDL from PlantUML Action.
 *
 * @author xwsg
 */
public class PlantUml2MysqlAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile plantUmlFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project != null && plantUmlFile != null) {
            // Show background process indicator
            ProgressManager
                .getInstance().run(new Task.Backgroundable(project, "PlantUml2MySQL generation", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    // Generate DDLs
                    new PlantUml2MysqlDdlGenerator().generate(plantUmlFile);
                    // refresh
                    VirtualFileManager.getInstance().asyncRefresh(null);
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile vf = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        e.getPresentation().setVisible(vf != null &&
            ("PlantUML file".equalsIgnoreCase(vf.getFileType().getName())
                || "PLAIN_TEXT".equalsIgnoreCase(vf.getFileType().getName())));
        super.update(e);
    }
}
