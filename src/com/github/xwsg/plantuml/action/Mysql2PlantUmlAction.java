package com.github.xwsg.plantuml.action;

import com.github.xwsg.plantuml.generator.Mysql2PlantUmlGenerator;
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
 * Generate DDL from PlantUML Action.
 *
 * @author xwsg
 */
public class Mysql2PlantUmlAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile ddlFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        Project project = e.getData(PlatformDataKeys.PROJECT);

        if (project != null && ddlFile != null) {
            // Show background process indicator
            ProgressManager
                .getInstance().run(new Task.Backgroundable(project, "PlantUml Generation", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    // Generate DDLs
                    Mysql2PlantUmlGenerator.generate(ddlFile);
                    // refresh
                    VirtualFileManager.getInstance().asyncRefresh(null);
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }
}
