/*
 * Copyright 2011 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gradleplug.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import gradleplug.GradleProjectsManager;
import gradleplug.bundles.GradlePlugBundle;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 08.02.2011
 */
public class AddProjectAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        GradleProjectsManager gradleProjectsManager = project.getComponent(GradleProjectsManager.class);
        FileTypeDescriptor fileTypeDescriptor =
                new FileTypeDescriptor(GradlePlugBundle.message("add.project.file.chooser.title"), "gradle");
        FileChooserFactory fileChooserFactory = FileChooserFactory.getInstance();
        FileChooserDialog fileChooser = fileChooserFactory.createFileChooser(fileTypeDescriptor, project);
        VirtualFile[] files = fileChooser.choose(null, project);
        if (files.length > 0) {
            for (VirtualFile file : files) {
                File buildFile = new File(file.getPath());
                gradleProjectsManager.addProject(buildFile);
            }
        }
    }
}
