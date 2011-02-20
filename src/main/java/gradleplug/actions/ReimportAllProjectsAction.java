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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import gradleplug.DataKeys;
import gradleplug.GradleProjectsManager;
import gradleplug.toolwindow.structure.ProjectContainerNode;
import gradleplug.toolwindow.structure.ProjectNode;

import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 08.02.2011
 */
public class ReimportAllProjectsAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        ProjectContainerNode projectContainerNode = e.getData(DataKeys.PROJECT_CONTAINER_NODE);
        if (projectContainerNode == null) {
            return;
        }
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(projectContainerNode.hasProjects());
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        ProjectContainerNode projectContainerNode = e.getData(DataKeys.PROJECT_CONTAINER_NODE);
        if (projectContainerNode == null) {
            return;
        }
        GradleProjectsManager projectsManager = project.getComponent(GradleProjectsManager.class);
        List<ProjectNode> projects = projectContainerNode.getProjects();
        projectsManager.reimportProjects(projects);
    }
}
