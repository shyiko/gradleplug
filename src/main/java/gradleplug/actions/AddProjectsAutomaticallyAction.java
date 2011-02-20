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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import gradleplug.GradleProjectsManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 08.02.2011
 */
public class AddProjectsAutomaticallyAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        GradleProjectsManager gradleProjectsManager = project.getComponent(GradleProjectsManager.class);
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            File buildGradle = locateFileInsideModule(module, "build.gradle");
            if (buildGradle != null) {
                gradleProjectsManager.addProject(buildGradle);
            }
        }
    }

    @Nullable
    private File locateFileInsideModule(Module module, String filename) {
        File result = null;
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            File file = new File(contentRoot.getPath(), filename);
            if (file.exists()) {
                result = file;
                break;
            }
        }
        return result;
    }
}
