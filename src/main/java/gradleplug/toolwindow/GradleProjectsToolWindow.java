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
package gradleplug.toolwindow;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import gradleplug.adapters.ProjectComponentAdapter;
import gradleplug.bundles.GradlePlugBundle;
import gradleplug.toolwindow.structure.ProjectContainerNode;
import gradleplug.toolwindow.structure.ProjectNode;
import gradleplug.toolwindow.structure.TaskNode;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 08.02.2011
 */
@State(name = "GradleProjectsToolWindow",
        storages = {@Storage(id = "other", file = "$PROJECT_FILE$")}
)
public class GradleProjectsToolWindow extends ProjectComponentAdapter implements PersistentStateComponent<Element> {

    private static final Icon GRADLE_ICON = IconLoader.getIcon("/icons/gradle/gradle.png");

    private static final String PROJECT_NODE = "project";
    private static final String PROJECT_NODE_BUILD_FILE_ATTR = "buildFile";
    private static final String TASKS_NODE = "tasks";
    private static final String TASK_NODE = "task";
    private static final String TASK_NODE_NAME_ATTR = "name";
    private static final String TASK_NODE_DESCRIPTION_ATTR = "description";

    private GradleProjectsToolPanel toolPanel;

    protected GradleProjectsToolWindow(Project project) {
        super(project);
        toolPanel = new GradleProjectsToolPanel(project);
    }

    @Override
    public void initComponent() {
        StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {

            @SuppressWarnings({"deprecation"})
            public void run() {
                final ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);
                ToolWindow toolWindow = manager.registerToolWindow(
                        GradlePlugBundle.message("gradle.projects.tool.window.title"), toolPanel, ToolWindowAnchor.RIGHT, project, true);
                toolWindow.setIcon(GRADLE_ICON);
            }
        });
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "GradleProjectsToolWindow";
    }

    public ProjectContainerNode getProjectContainer() {
        return (ProjectContainerNode) toolPanel.getTree().getModel().getRoot();
    }

    @SuppressWarnings({"unchecked"})
    public void loadState(Element state) {
        ProjectContainerNode projectContainerNode = getProjectContainer();
        projectContainerNode.removeAllChildren();
        List<Element> projects = state.getChildren(PROJECT_NODE);
        for (Element project : projects) {
            String buildFilePath = project.getAttributeValue(PROJECT_NODE_BUILD_FILE_ATTR);
            if (buildFilePath == null) {
                continue;
            }
            File buildFile = new File(buildFilePath);
            Element tasksNode = project.getChild(TASKS_NODE);
            List<TaskNode> taskNodes = new ArrayList<TaskNode>();
            if (tasksNode != null) {
                List<Element> tasks = tasksNode.getChildren(TASK_NODE);
                for (Element task : tasks) {
                    String taskName = task.getAttributeValue(TASK_NODE_NAME_ATTR);
                    String taskDescription = task.getAttributeValue(TASK_NODE_DESCRIPTION_ATTR);
                    taskNodes.add(new TaskNode(taskName, taskDescription));
                }
            }
            ProjectNode projectNode = projectContainerNode.addProject(buildFile);
            projectNode.getTaskContainerNode().setTasks(taskNodes);
        }
    }

    public Element getState() {
        Element result = new Element(getComponentName());
        ProjectContainerNode projectContainerNode = getProjectContainer();
        for (ProjectNode projectNode : projectContainerNode.getProjects()) {
            Element projectXmlNode = new Element(PROJECT_NODE);
            projectXmlNode.setAttribute(PROJECT_NODE_BUILD_FILE_ATTR, projectNode.getBuildFile().getAbsolutePath());
            Element tasksXmlNode = new Element(TASKS_NODE);
            for (TaskNode taskNode : projectNode.getTaskContainerNode().getTasks()) {
                Element task = new Element(TASK_NODE);
                task.setAttribute(TASK_NODE_NAME_ATTR, taskNode.getName());
                task.setAttribute(TASK_NODE_DESCRIPTION_ATTR, taskNode.getDescription());
                tasksXmlNode.addContent(task);
            }
            projectXmlNode.addContent(tasksXmlNode);
            result.addContent(projectXmlNode);
        }
        return result;
    }

}
